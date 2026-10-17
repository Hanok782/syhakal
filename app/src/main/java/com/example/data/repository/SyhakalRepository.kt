package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.data.crypto.DidManager
import com.example.data.local.*
import com.example.data.remote.GeminiApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileInputStream
import java.util.UUID

class SyhakalRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val jobDao = db.jobDao()
    private val settingsDao = db.settingsDao()
    private val transactionDao = db.transactionDao()
    private val communityDao = db.communityDao()
    private val moderationDao = db.moderationDao()

    private val geminiClient = GeminiApiClient(context)
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        repoScope.launch {
            bootstrapDefaults()
        }
    }

    private suspend fun bootstrapDefaults() {
        // 1. Ensure Global Settings exist
        if (settingsDao.getSettings() == null) {
            settingsDao.insertOrUpdate(
                SettingsEntity(
                    id = "global_config",
                    subscriptionPriceINR = 1.0, // Dynamic ₹1 initial price!
                    initialFreeCredits = 50,
                    creditCostImage = 5,
                    creditCostVideo = 15,
                    announcementText = "SYHAKAL v2.0 Operational: Decentralized Identity & Gemini AI Active.",
                    adminPin = "8899",
                    ownerEmail = "owner@syhakal.ai"
                )
            )
        }

        // 2. Ensure Default User exists
        val defaultUser = userDao.getUser("usr_creator_01")
        if (defaultUser == null) {
            val did = DidManager.createIdentity("creator_seed")
            userDao.insertOrUpdate(
                UserEntity(
                    userId = "usr_creator_01",
                    did = did.did,
                    publicKey = did.publicKeyHex,
                    displayName = "Quantum Creator",
                    email = "creator@syhakal.ai",
                    role = "USER",
                    creditsRemaining = 50,
                    creditsConsumed = 0
                )
            )
        }

        // 3. Pre-populate Community Showcase Posts if empty
        val posts = communityDao.observePosts().first()
        if (posts.isEmpty()) {
            val samplePosts = listOf(
                CommunityPostEntity(
                    id = "post_01",
                    authorDid = "did:syhakal:7f8a9b2c3d4e5f60",
                    authorName = "AuraSynthetix",
                    title = "Hyper-dimensional Cyberpunk City",
                    prompt = "Futuristic neo-tokyo megacity in volumetric rain, neon cyan & magenta reflections, 8k resolution octane render",
                    mediaUrl = "sample_cyber_city",
                    mediaType = "IMAGE",
                    aspectRatio = "16:9",
                    modelUsed = "gemini-2.5-flash-image",
                    likesCount = 142,
                    commentsCount = 18
                ),
                CommunityPostEntity(
                    id = "post_02",
                    authorDid = "did:syhakal:9b2c3d4e5f607f8a",
                    authorName = "VeoCinematics",
                    title = "Orbital Station Solar Flare",
                    prompt = "Cinematic slow camera orbit around an interstellar ring station, golden solar flares, hyper-detailed cosmic nebula",
                    mediaUrl = "sample_orbital",
                    mediaType = "VIDEO",
                    aspectRatio = "16:9",
                    modelUsed = "veo-3.1-fast-generate-preview",
                    likesCount = 389,
                    commentsCount = 47
                ),
                CommunityPostEntity(
                    id = "post_03",
                    authorDid = "did:syhakal:3d4e5f607f8a9b2c",
                    authorName = "NeuralSculptor",
                    title = "Bionic Android Goddess",
                    prompt = "Cyborg muse portrait with translucent obsidian glass casing, glowing microchips and luminescent gold wiring",
                    mediaUrl = "sample_android",
                    mediaType = "IMAGE",
                    aspectRatio = "1:1",
                    modelUsed = "gemini-nano-banana-2.1",
                    likesCount = 521,
                    commentsCount = 63
                )
            )
            for (p in samplePosts) {
                communityDao.insertPost(p)
            }
        }
    }

    // --- User & Identity ---
    fun observeUser(userId: String): Flow<UserEntity?> = userDao.observeUser(userId)
    fun observeAllUsers(): Flow<List<UserEntity>> = userDao.observeAllUsers()
    suspend fun getUser(userId: String): UserEntity? = userDao.getUser(userId)

    suspend fun registerOrLoginUser(displayName: String, email: String, role: String = "USER"): UserEntity {
        val existing = userDao.getUser("usr_creator_01")
        if (existing != null && existing.displayName == displayName) {
            return existing
        }
        val did = DidManager.createIdentity(displayName + email)
        val user = UserEntity(
            userId = "usr_" + UUID.randomUUID().toString().take(8),
            did = did.did,
            publicKey = did.publicKeyHex,
            displayName = displayName.ifBlank { "Cybernetic Creator" },
            email = email.ifBlank { "creator@syhakal.ai" },
            role = role,
            creditsRemaining = 50,
            creditsConsumed = 0
        )
        userDao.insertOrUpdate(user)
        return user
    }

    suspend fun updateUserRole(userId: String, role: String) = userDao.updateRole(userId, role)
    suspend fun setUserBanned(userId: String, isBanned: Boolean) = userDao.setBanned(userId, isBanned)
    suspend fun addCreditsToUser(userId: String, additionalCredits: Int) {
        val user = userDao.getUser(userId) ?: return
        userDao.updateCredits(userId, user.creditsRemaining + additionalCredits, user.creditsConsumed)
    }

    // --- Dynamic Settings & Pricing ---
    fun observeSettings(): Flow<SettingsEntity?> = settingsDao.observeSettings()
    suspend fun getSettings(): SettingsEntity = settingsDao.getSettings() ?: SettingsEntity()

    suspend fun updateSubscriptionPrice(newPrice: Double) {
        settingsDao.updateSubscriptionPrice(newPrice)
    }

    suspend fun updateGlobalSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    // --- Generation Jobs ---
    fun observeUserJobs(userId: String): Flow<List<JobEntity>> = jobDao.observeUserJobs(userId)
    fun observeAllJobs(): Flow<List<JobEntity>> = jobDao.observeAllJobs()
    suspend fun getJob(jobId: String): JobEntity? = jobDao.getJob(jobId)
    suspend fun toggleFavorite(jobId: String) = jobDao.toggleFavorite(jobId)
    suspend fun deleteJob(jobId: String) = jobDao.deleteJob(jobId)

    suspend fun enhancePromptWithGemini(prompt: String, style: String): String {
        return geminiClient.enhancePrompt(prompt, style)
    }

    /**
     * Queues an AI generation job and launches asynchronous processing
     */
    suspend fun submitGenerationJob(
        userId: String,
        type: String, // "TEXT_TO_IMAGE", "TEXT_TO_VIDEO", "IMAGE_TO_VIDEO"
        prompt: String,
        negativePrompt: String,
        modelName: String,
        aspectRatio: String,
        quality: String,
        motionStrength: Float,
        inputImageUrl: String? = null
    ): Result<String> {
        val user = userDao.getUser(userId) ?: return Result.failure(Exception("User not found"))
        if (user.isBanned) return Result.failure(Exception("Account suspended. Contact support."))

        val settings = getSettings()
        val cost = if (type == "TEXT_TO_IMAGE") settings.creditCostImage else settings.creditCostVideo

        if (user.creditsRemaining < cost) {
            return Result.failure(Exception("Insufficient credits. Need $cost credits, but have ${user.creditsRemaining}."))
        }

        // Deduct credits immediately
        userDao.updateCredits(userId, user.creditsRemaining - cost, user.creditsConsumed + cost)

        val jobId = "job_" + UUID.randomUUID().toString().take(12)
        val job = JobEntity(
            id = jobId,
            userId = userId,
            type = type,
            prompt = prompt,
            negativePrompt = negativePrompt,
            inputImageUrl = inputImageUrl,
            modelName = modelName,
            aspectRatio = aspectRatio,
            quality = quality,
            motionStrength = motionStrength,
            status = "QUEUED",
            progress = 0.05f,
            creditsCost = cost
        )
        jobDao.insertJob(job)

        // Launch async worker
        repoScope.launch {
            processJobAsync(job)
        }

        return Result.success(jobId)
    }

    private suspend fun processJobAsync(job: JobEntity) {
        try {
            // Stage 1: Analyzing & queuing
            jobDao.updateJobStatus(job.id, "PROCESSING", 0.20f, null, null, null)
            delay(800)

            // Content moderation check
            val (isSafe, modReason) = geminiClient.moderateContent(job.prompt)
            if (!isSafe) {
                jobDao.updateJobStatus(job.id, "FAILED", 0f, null, "Safety violation: $modReason", System.currentTimeMillis())
                return
            }

            // Stage 2: Latent Diffusion / Veo Transformer Synthesis
            jobDao.updateJobStatus(job.id, "PROCESSING", 0.55f, null, null, null)

            val resultPath = if (job.type == "TEXT_TO_IMAGE") {
                geminiClient.generateImage(job.prompt, job.aspectRatio, job.quality, job.modelName)
            } else {
                geminiClient.generateVideo(job.prompt, job.aspectRatio, job.motionStrength, 5, job.modelName)
            }

            // Stage 3: Rendering & Color Grading
            jobDao.updateJobStatus(job.id, "PROCESSING", 0.85f, resultPath, null, null)
            delay(600)

            // Completed!
            jobDao.updateJobStatus(job.id, "COMPLETED", 1.0f, resultPath, null, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e("SyhakalRepository", "Job processing failed", e)
            jobDao.updateJobStatus(job.id, "FAILED", 0f, null, e.localizedMessage ?: "Generation failed", System.currentTimeMillis())
        }
    }

    // --- Payment & Subscriptions ---
    fun observeUserTransactions(userId: String): Flow<List<TransactionEntity>> = transactionDao.observeUserTransactions(userId)
    fun observeAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAllTransactions()

    /**
     * Executes genuine simulated Razorpay/UPI checkout with dynamic price & server verification
     */
    suspend fun processSubscriptionPayment(
        userId: String,
        gateway: String = "Razorpay / UPI Instant"
    ): Result<TransactionEntity> {
        val user = userDao.getUser(userId) ?: return Result.failure(Exception("User not found"))
        val settings = getSettings()
        val currentPrice = settings.subscriptionPriceINR

        val orderId = "order_rzp_" + UUID.randomUUID().toString().take(10)
        val paymentId = "pay_" + UUID.randomUUID().toString().take(12)

        // Server-side simulated payment verification
        val transaction = TransactionEntity(
            id = "txn_" + UUID.randomUUID().toString().take(12),
            userId = userId,
            amountINR = currentPrice,
            planId = "syhakal_pro_monthly",
            gateway = gateway,
            gatewayOrderId = orderId,
            gatewayPaymentId = paymentId,
            status = "SUCCESS"
        )
        transactionDao.insertTransaction(transaction)

        // Upgrade user to SUBSCRIBER and award +500 bonus credits!
        userDao.updateRole(userId, "SUBSCRIBER")
        userDao.updateCredits(userId, user.creditsRemaining + 500, user.creditsConsumed)

        return Result.success(transaction)
    }

    // --- Community Showcase & Social ---
    fun observeCommunityPosts(): Flow<List<CommunityPostEntity>> = communityDao.observePosts()
    fun observePostComments(postId: String): Flow<List<CommunityCommentEntity>> = communityDao.observeComments(postId)
    fun observeDirectMessages(userDid: String): Flow<List<DirectMessageEntity>> = communityDao.observeMessages(userDid)

    suspend fun togglePostLike(postId: String) = communityDao.toggleLike(postId)

    suspend fun publishPost(
        user: UserEntity,
        title: String,
        prompt: String,
        mediaPath: String,
        mediaType: String,
        aspectRatio: String,
        modelUsed: String
    ): Result<String> {
        // Run automated safety moderation before publishing
        val (isSafe, reason) = geminiClient.moderateContent("$title $prompt")
        if (!isSafe) {
            return Result.failure(Exception("Cannot publish: $reason"))
        }

        val postId = "post_" + UUID.randomUUID().toString().take(10)
        val post = CommunityPostEntity(
            id = postId,
            authorDid = user.did,
            authorName = user.displayName,
            authorAvatar = "",
            title = title,
            prompt = prompt,
            mediaUrl = mediaPath,
            mediaType = mediaType,
            aspectRatio = aspectRatio,
            modelUsed = modelUsed,
            likesCount = 0,
            commentsCount = 0
        )
        communityDao.insertPost(post)
        return Result.success(postId)
    }

    suspend fun addComment(postId: String, user: UserEntity, text: String): Result<Unit> {
        val (isSafe, reason) = geminiClient.moderateContent(text)
        if (!isSafe) return Result.failure(Exception("Comment rejected: $reason"))

        val comment = CommunityCommentEntity(
            id = "comment_" + UUID.randomUUID().toString().take(8),
            postId = postId,
            authorDid = user.did,
            authorName = user.displayName,
            text = text
        )
        communityDao.insertComment(comment)
        communityDao.incrementCommentCount(postId)
        return Result.success(Unit)
    }

    suspend fun sendDirectMessage(senderDid: String, recipientDid: String, recipientName: String, content: String): Result<Unit> {
        val (isSafe, reason) = geminiClient.moderateContent(content)
        if (!isSafe) return Result.failure(Exception("Message blocked: $reason"))

        val msg = DirectMessageEntity(
            id = "msg_" + UUID.randomUUID().toString().take(10),
            senderDid = senderDid,
            recipientDid = recipientDid,
            recipientName = recipientName,
            content = content
        )
        communityDao.insertMessage(msg)
        return Result.success(Unit)
    }

    // --- Content Moderation Hub ---
    fun observeModerationReports(): Flow<List<ModerationReportEntity>> = moderationDao.observeReports()

    suspend fun reportPost(postId: String, reportedByDid: String, reason: String) {
        val report = ModerationReportEntity(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            targetId = postId,
            targetType = "POST",
            reportedByDid = reportedByDid,
            reason = reason
        )
        moderationDao.insertReport(report)
        communityDao.updatePostModeration(postId, "FLAGGED")
    }

    suspend fun resolveModeration(reportId: String, postId: String, action: String) {
        moderationDao.updateReportStatus(reportId, "RESOLVED")
        if (action == "REMOVE") {
            communityDao.updatePostModeration(postId, "REMOVED")
        } else if (action == "APPROVE") {
            communityDao.updatePostModeration(postId, "APPROVED")
        }
    }

    // --- Download and Save to Device Gallery ---
    suspend fun saveImageToDeviceGallery(filePath: String, title: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(filePath)
            if (!sourceFile.exists()) {
                return@withContext Result.failure(Exception("Source file does not exist"))
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "SYHAKAL_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SYHAKAL")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            resolver.openOutputStream(uri)?.use { outStream ->
                FileInputStream(sourceFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Log.e("SyhakalRepository", "Save to gallery failed", e)
            Result.failure(e)
        }
    }
}
