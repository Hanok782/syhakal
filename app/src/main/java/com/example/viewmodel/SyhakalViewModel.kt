package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.SyhakalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SyhakalViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SyhakalRepository(application.applicationContext)

    // Current Session User ID (defaults to primary creator account)
    private val _currentUserId = MutableStateFlow("usr_creator_01")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { uid -> repository.observeUser(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentSettings: StateFlow<SettingsEntity?> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    val userJobs: StateFlow<List<JobEntity>> = _currentUserId
        .flatMapLatest { uid -> repository.observeUserJobs(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<JobEntity>> = repository.observeAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<TransactionEntity>> = _currentUserId
        .flatMapLatest { uid -> repository.observeUserTransactions(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.observeCommunityPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moderationReports: StateFlow<List<ModerationReportEntity>> = repository.observeModerationReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val directMessages: StateFlow<List<DirectMessageEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null) repository.observeDirectMessages(user.did) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    val selectedJobForPreview = MutableStateFlow<JobEntity?>(null)
    val activeJobId = MutableStateFlow<String?>(null)
    val isOwnerModeUnlocked = MutableStateFlow(false)

    private val _snackBarEvent = MutableSharedFlow<String>()
    val snackBarEvent: SharedFlow<String> = _snackBarEvent.asSharedFlow()

    fun switchUser(userId: String) {
        _currentUserId.value = userId
    }

    fun loginOrRegister(displayName: String, email: String, role: String = "USER") {
        viewModelScope.launch {
            val user = repository.registerOrLoginUser(displayName, email, role)
            _currentUserId.value = user.userId
            _snackBarEvent.emit("Identity authorized: ${user.did.take(18)}...")
        }
    }

    fun submitImageJob(
        prompt: String,
        negativePrompt: String,
        modelName: String,
        aspectRatio: String,
        quality: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.submitGenerationJob(
                userId = _currentUserId.value,
                type = "TEXT_TO_IMAGE",
                prompt = prompt,
                negativePrompt = negativePrompt,
                modelName = modelName,
                aspectRatio = aspectRatio,
                quality = quality,
                motionStrength = 1.0f
            )
            res.onSuccess { jobId ->
                activeJobId.value = jobId
                _snackBarEvent.emit("Image generation queued asynchronously")
                onSuccess(jobId)
            }.onFailure { err ->
                onError(err.message ?: "Job failed to queue")
            }
        }
    }

    fun submitVideoJob(
        prompt: String,
        motionStrength: Float,
        durationSeconds: Int,
        modelName: String,
        aspectRatio: String,
        quality: String,
        inputImageUrl: String? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val type = if (inputImageUrl != null) "IMAGE_TO_VIDEO" else "TEXT_TO_VIDEO"
            val res = repository.submitGenerationJob(
                userId = _currentUserId.value,
                type = type,
                prompt = prompt,
                negativePrompt = "",
                modelName = modelName,
                aspectRatio = aspectRatio,
                quality = quality,
                motionStrength = motionStrength,
                inputImageUrl = inputImageUrl
            )
            res.onSuccess { jobId ->
                activeJobId.value = jobId
                _snackBarEvent.emit("Veo video generation initiated")
                onSuccess(jobId)
            }.onFailure { err ->
                onError(err.message ?: "Video job failed")
            }
        }
    }

    fun enhancePrompt(prompt: String, style: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val enhanced = repository.enhancePromptWithGemini(prompt, style)
            onResult(enhanced)
            _snackBarEvent.emit("Prompt enhanced with Gemini AI")
        }
    }

    fun processSubscriptionPayment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.processSubscriptionPayment(_currentUserId.value)
            res.onSuccess { txn ->
                _snackBarEvent.emit("Subscription Activated! ₹${txn.amountINR} paid. +500 credits granted.")
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Payment failed")
            }
        }
    }

    fun updateSubscriptionPrice(newPrice: Double) {
        viewModelScope.launch {
            repository.updateSubscriptionPrice(newPrice)
            _snackBarEvent.emit("Dynamic membership price updated to ₹$newPrice")
        }
    }

    fun updateGlobalSettings(settings: SettingsEntity) {
        viewModelScope.launch {
            repository.updateGlobalSettings(settings)
            _snackBarEvent.emit("Global settings updated")
        }
    }

    fun addCreditsToUser(targetUserId: String, credits: Int) {
        viewModelScope.launch {
            repository.addCreditsToUser(targetUserId, credits)
            _snackBarEvent.emit("Allocated +$credits credits to user")
        }
    }

    fun toggleUserBan(targetUserId: String, currentBan: Boolean) {
        viewModelScope.launch {
            repository.setUserBanned(targetUserId, !currentBan)
            _snackBarEvent.emit(if (!currentBan) "Account suspended" else "Account restored")
        }
    }

    fun toggleFavorite(jobId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(jobId)
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
            _snackBarEvent.emit("Project removed")
        }
    }

    fun saveToGallery(filePath: String, title: String) {
        viewModelScope.launch {
            val res = repository.saveImageToDeviceGallery(filePath, title)
            res.onSuccess {
                _snackBarEvent.emit("Saved to device Gallery in Pictures/SYHAKAL")
            }.onFailure { err ->
                _snackBarEvent.emit("Download failed: ${err.message}")
            }
        }
    }

    fun publishToCommunity(
        title: String,
        prompt: String,
        mediaPath: String,
        mediaType: String,
        aspectRatio: String,
        modelUsed: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.publishPost(user, title, prompt, mediaPath, mediaType, aspectRatio, modelUsed)
            res.onSuccess {
                _snackBarEvent.emit("Published to Global Community Showcase!")
                onComplete(true)
            }.onFailure { err ->
                _snackBarEvent.emit(err.message ?: "Failed to publish")
                onComplete(false)
            }
        }
    }

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            repository.togglePostLike(postId)
        }
    }

    fun addPostComment(postId: String, text: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.addComment(postId, user, text)
            res.onSuccess {
                _snackBarEvent.emit("Comment posted")
            }.onFailure { err ->
                _snackBarEvent.emit(err.message ?: "Comment failed")
            }
        }
    }

    fun sendDirectMessage(recipientDid: String, recipientName: String, text: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.sendDirectMessage(user.did, recipientDid, recipientName, text)
            res.onSuccess {
                _snackBarEvent.emit("Message transmitted securely")
            }.onFailure { err ->
                _snackBarEvent.emit(err.message ?: "Message failed")
            }
        }
    }

    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.reportPost(postId, user.did, reason)
            _snackBarEvent.emit("Report submitted to moderation queue")
        }
    }

    fun resolveModerationReport(reportId: String, postId: String, action: String) {
        viewModelScope.launch {
            repository.resolveModeration(reportId, postId, action)
            _snackBarEvent.emit("Moderation action applied: $action")
        }
    }

    fun verifyOwnerPin(pin: String): Boolean {
        val expected = currentSettings.value?.adminPin ?: "8899"
        val ok = pin == expected
        if (ok) isOwnerModeUnlocked.value = true
        return ok
    }
}
