package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val did: String,
    val publicKey: String,
    val displayName: String,
    val email: String,
    val role: String = "USER", // "OWNER", "SUBSCRIBER", "USER"
    val creditsRemaining: Int = 50,
    val creditsConsumed: Int = 0,
    val isBanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generation_jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "TEXT_TO_IMAGE", "TEXT_TO_VIDEO", "IMAGE_TO_VIDEO"
    val prompt: String,
    val negativePrompt: String = "",
    val inputImageUrl: String? = null,
    val resultUrl: String? = null,
    val modelName: String,
    val aspectRatio: String = "1:1",
    val quality: String = "Standard",
    val motionStrength: Float = 5.0f,
    val status: String = "QUEUED", // "QUEUED", "PROCESSING", "COMPLETED", "FAILED"
    val progress: Float = 0.0f,
    val errorMessage: String? = null,
    val creditsCost: Int = 5,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isFavorite: Boolean = false,
    val isSavedToGallery: Boolean = false
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: String = "global_config",
    val subscriptionPriceINR: Double = 1.0, // Dynamic ₹1 initial price!
    val initialFreeCredits: Int = 50,
    val creditCostImage: Int = 5,
    val creditCostVideo: Int = 15,
    val announcementText: String = "Welcome to SYHAKAL AI Studio. High-speed generation active.",
    val adminPin: String = "8899",
    val ownerEmail: String = "owner@syhakal.ai"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountINR: Double,
    val planId: String = "syhakal_pro_monthly",
    val gateway: String = "Razorpay / UPI Instant",
    val gatewayOrderId: String,
    val gatewayPaymentId: String,
    val status: String = "SUCCESS", // "SUCCESS", "FAILED", "REFUNDED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey val id: String,
    val authorDid: String,
    val authorName: String,
    val authorAvatar: String = "",
    val title: String,
    val prompt: String,
    val mediaUrl: String,
    val mediaType: String = "IMAGE", // "IMAGE", "VIDEO"
    val aspectRatio: String = "1:1",
    val modelUsed: String = "gemini-2.5-flash-image",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val moderationStatus: String = "APPROVED" // "APPROVED", "FLAGGED", "REMOVED"
)

@Entity(tableName = "community_comments")
data class CommunityCommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorDid: String,
    val authorName: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey val id: String,
    val senderDid: String,
    val recipientDid: String,
    val recipientName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "moderation_reports")
data class ModerationReportEntity(
    @PrimaryKey val id: String,
    val targetId: String,
    val targetType: String, // "POST", "USER"
    val reportedByDid: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // "PENDING", "RESOLVED", "DISMISSED"
)
