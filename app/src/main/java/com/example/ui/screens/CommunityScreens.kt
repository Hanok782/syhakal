package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommunityCommentEntity
import com.example.data.local.CommunityPostEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun CommunityScreen(
    viewModel: SyhakalViewModel,
    onRemixPrompt: (String) -> Unit,
    onOpenMessages: () -> Unit
) {
    val posts by viewModel.communityPosts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    var activeReportPostId by remember { mutableStateOf<String?>(null) }
    var followedCreators by remember { mutableStateOf(setOf<String>()) }

    val filteredPosts = if (searchQuery.isBlank()) {
        posts
    } else {
        posts.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.prompt.contains(searchQuery, ignoreCase = true) ||
                    it.authorName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Community Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlowingHeader(
                title = "GLOBAL SHOWCASE",
                subtitle = "Decentralized AI Creator Feed"
            )
            IconButton(
                onClick = onOpenMessages,
                modifier = Modifier
                    .testTag("open_messages_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceElevated)
                    .border(1.dp, CardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Direct Messages",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with Hashtag Filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by prompt, style, or creator DID...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("community_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Feed list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(filteredPosts, key = { it.id }) { post ->
                val isFollowing = followedCreators.contains(post.authorDid)
                CommunityPostCard(
                    post = post,
                    isFollowing = isFollowing,
                    onToggleFollow = {
                        followedCreators = if (isFollowing) {
                            followedCreators - post.authorDid
                        } else {
                            followedCreators + post.authorDid
                        }
                    },
                    onToggleLike = { viewModel.togglePostLike(post.id) },
                    onOpenComments = { activeCommentPostId = post.id },
                    onRemixPrompt = { onRemixPrompt(post.prompt) },
                    onReport = { activeReportPostId = post.id }
                )
            }
        }
    }

    // Comments Sheet / Dialog
    if (activeCommentPostId != null) {
        CommentsDialog(
            postId = activeCommentPostId!!,
            viewModel = viewModel,
            onDismiss = { activeCommentPostId = null }
        )
    }

    // Moderation Report Dialog
    if (activeReportPostId != null) {
        ReportPostDialog(
            postId = activeReportPostId!!,
            viewModel = viewModel,
            onDismiss = { activeReportPostId = null }
        )
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPostEntity,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    onRemixPrompt: () -> Unit,
    onReport: () -> Unit
) {
    val context = LocalContext.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        // Creator Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceElevated)
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = post.authorDid.take(16) + "...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onToggleFollow,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) CyberSurfaceElevated else NeonCyan.copy(alpha = 0.2f),
                        contentColor = if (isFollowing) TextSecondary else NeonCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "+ Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onReport,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Report",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Post Media Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            MediaThumbnail(
                mediaUrl = post.mediaUrl,
                contentDescription = post.title,
                modifier = Modifier.fillMaxSize()
            )

            // Modality badge overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${post.mediaType} • ${post.modelUsed}",
                    fontSize = 10.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Engagement Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) CyberPink else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onOpenComments,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    text = "${post.commentsCount}",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Look at this AI creation on SYHAKAL:\n\"${post.title}\"\n${post.prompt}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Remix Prompt CTA
            OutlinedButton(
                onClick = onRemixPrompt,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Remix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title and Prompt caption
        Text(
            text = post.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = post.prompt,
            fontSize = 12.sp,
            color = TextSecondary,
            maxLines = 3
        )
    }
}

@Composable
fun CommentsDialog(
    postId: String,
    viewModel: SyhakalViewModel,
    onDismiss: () -> Unit
) {
    val commentsFlow = remember(postId) { viewModel.repository.observePostComments(postId) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = {
            Text("COMMENTS & DISCUSSIONS", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                if (comments.isEmpty()) {
                    Text("No comments yet. Start the conversation below!", fontSize = 12.sp, color = TextSecondary)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(comments) { c ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(c.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonCyan)
                                    Text(c.authorDid.take(12), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(c.text, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write comment...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addPostComment(postId, commentText.trim())
                                commentText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF03161C))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
fun ReportPostDialog(
    postId: String,
    viewModel: SyhakalViewModel,
    onDismiss: () -> Unit
) {
    val reasons = listOf("Hate speech or harassment", "Violence or dangerous content", "Copyright violation", "Spam or scam", "Other safety violation")
    var selectedReason by remember { mutableStateOf(reasons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = {
            Text("REPORT CONTENT", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 16.sp)
        },
        text = {
            Column {
                Text("Select the violation category to submit to the SYHAKAL Content Moderation Board:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                reasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedReason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == r,
                            onClick = { selectedReason = r },
                            colors = RadioButtonDefaults.colors(selectedColor = ErrorRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(r, fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.reportPost(postId, selectedReason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Submit Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DirectMessagesScreen(
    viewModel: SyhakalViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.directMessages.collectAsState()
    var recipientDid by remember { mutableStateOf("did:syhakal:7f8a9b2c3d4e5f60") }
    var recipientName by remember { mutableStateOf("AuraSynthetix") }
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurface)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("DIRECT MESSAGES", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Text("Encrypted creator transmission", fontSize = 11.sp, color = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recipient Selection Pill
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Chat with: $recipientName", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text(recipientDid, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MatrixGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ONLINE", color = MatrixGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Messages list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No messages yet. Send a creative note or collaborate on a model!", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(messages) { msg ->
                    val isMine = msg.senderDid != recipientDid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMine) NeonPurple.copy(alpha = 0.35f) else CyberSurfaceElevated)
                                .border(1.dp, if (isMine) CardBorderPurple else CardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = msg.content,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type message...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendDirectMessage(recipientDid, recipientName, messageText.trim())
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF03161C))
            }
        }
    }
}
