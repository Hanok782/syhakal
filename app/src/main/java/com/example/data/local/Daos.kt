package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("UPDATE users SET creditsRemaining = :credits, creditsConsumed = :consumed WHERE userId = :userId")
    suspend fun updateCredits(userId: String, credits: Int, consumed: Int)

    @Query("UPDATE users SET role = :role WHERE userId = :userId")
    suspend fun updateRole(userId: String, role: String)

    @Query("UPDATE users SET isBanned = :isBanned WHERE userId = :userId")
    suspend fun setBanned(userId: String, isBanned: Boolean)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM generation_jobs WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserJobs(userId: String): Flow<List<JobEntity>>

    @Query("SELECT * FROM generation_jobs ORDER BY createdAt DESC")
    fun observeAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJob(jobId: String): JobEntity?

    @Query("SELECT * FROM generation_jobs WHERE status IN ('QUEUED', 'PROCESSING')")
    suspend fun getActiveJobs(): List<JobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("UPDATE generation_jobs SET status = :status, progress = :progress, resultUrl = :resultUrl, errorMessage = :errorMessage, completedAt = :completedAt WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: String, status: String, progress: Float, resultUrl: String?, errorMessage: String?, completedAt: Long?)

    @Query("UPDATE generation_jobs SET isFavorite = NOT isFavorite WHERE id = :jobId")
    suspend fun toggleFavorite(jobId: String)

    @Query("UPDATE generation_jobs SET isSavedToGallery = 1 WHERE id = :jobId")
    suspend fun markSavedToGallery(jobId: String)

    @Query("DELETE FROM generation_jobs WHERE id = :jobId")
    suspend fun deleteJob(jobId: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 'global_config' LIMIT 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 'global_config' LIMIT 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)

    @Query("UPDATE app_settings SET subscriptionPriceINR = :newPrice WHERE id = 'global_config'")
    suspend fun updateSubscriptionPrice(newPrice: Double)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_posts WHERE moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun observePosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts WHERE id = :postId LIMIT 1")
    suspend fun getPost(postId: String): CommunityPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Query("UPDATE community_posts SET likesCount = CASE WHEN isLikedByMe = 1 THEN likesCount - 1 ELSE likesCount + 1 END, isLikedByMe = NOT isLikedByMe WHERE id = :postId")
    suspend fun toggleLike(postId: String)

    @Query("UPDATE community_posts SET moderationStatus = :status WHERE id = :postId")
    suspend fun updatePostModeration(postId: String, status: String)

    @Query("DELETE FROM community_posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    // Comments
    @Query("SELECT * FROM community_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeComments(postId: String): Flow<List<CommunityCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommunityCommentEntity)

    @Query("UPDATE community_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: String)

    // Direct Messages
    @Query("SELECT * FROM direct_messages WHERE senderDid = :userDid OR recipientDid = :userDid ORDER BY createdAt DESC")
    fun observeMessages(userDid: String): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectMessageEntity)
}

@Dao
interface ModerationDao {
    @Query("SELECT * FROM moderation_reports ORDER BY timestamp DESC")
    fun observeReports(): Flow<List<ModerationReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ModerationReportEntity)

    @Query("UPDATE moderation_reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)
}
