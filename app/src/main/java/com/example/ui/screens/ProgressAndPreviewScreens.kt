package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.crypto.DidManager
import com.example.data.local.JobEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun JobProgressScreen(
    jobId: String,
    viewModel: SyhakalViewModel,
    onNavigateToPreview: (JobEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val userJobs by viewModel.userJobs.collectAsState()
    val job = userJobs.firstOrNull { it.id == jobId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (job == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Locating job in decentralized queue...", color = TextSecondary)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header badge
                StatusBadge(status = job.status)

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceElevated)
                        .border(2.dp, CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (job.status == "PROCESSING" || job.status == "QUEUED") {
                        CircularProgressIndicator(
                            progress = { job.progress },
                            modifier = Modifier.size(136.dp),
                            color = NeonCyan,
                            strokeWidth = 6.dp,
                            trackColor = CyberSurfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(job.progress * 100).toInt()}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = if (job.status == "QUEUED") "WAITING" else "DIFFUSING",
                                fontSize = 10.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (job.status == "COMPLETED") {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MatrixGreen,
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (job.status == "COMPLETED") "GENERATION COMPLETE" else if (job.status == "FAILED") "SYNTHESIS FAILED" else "ASYNCHRONOUS PIPELINE ACTIVE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"${job.prompt}\"",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pipeline Stages Breakdown
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    StageRow("1. Prompt Safety & Tokenization", isDone = job.progress >= 0.2f, isCurrent = job.progress in 0.05f..0.25f)
                    Spacer(modifier = Modifier.height(10.dp))
                    StageRow("2. Latent Neural Diffusion (${job.modelName})", isDone = job.progress >= 0.8f, isCurrent = job.progress in 0.25f..0.85f)
                    Spacer(modifier = Modifier.height(10.dp))
                    StageRow("3. Final Rendering & Disk Cache", isDone = job.status == "COMPLETED", isCurrent = job.progress in 0.85f..0.99f)
                }

                if (job.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: ${job.errorMessage}",
                        color = ErrorRed,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (job.status == "COMPLETED") {
                    FuturisticButton(
                        text = "OPEN FULL PREVIEW & SAVE",
                        icon = Icons.Default.Visibility,
                        onClick = { onNavigateToPreview(job) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "open_preview_button"
                    )
                } else if (job.status == "FAILED") {
                    FuturisticButton(
                        text = "DISMISS",
                        isSecondary = true,
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Run in Background (Navigate Back)")
                    }
                }
            }
        }
    }
}

@Composable
fun StageRow(title: String, isDone: Boolean, isCurrent: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else if (isCurrent) Icons.Default.Timelapse else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isDone) MatrixGreen else if (isCurrent) NeonCyan else TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (isDone || isCurrent) TextPrimary else TextSecondary,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PreviewDetailScreen(
    job: JobEntity,
    viewModel: SyhakalViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsState()

    var showPublishDialog by remember { mutableStateOf(false) }
    var publishTitle by remember { mutableStateOf("") }
    var isVideoPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "CREATION INSPECTOR",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            IconButton(
                onClick = { viewModel.toggleFavorite(job.id) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurface)
            ) {
                Icon(
                    imageVector = if (job.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (job.isFavorite) CyberPink else TextSecondary
                )
            }
        }

        // Fullscreen Media Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            MediaThumbnail(
                mediaUrl = job.resultUrl ?: "",
                contentDescription = job.prompt,
                modifier = Modifier.fillMaxSize()
            )

            // Video indicator / play toggle
            if (job.type.contains("VIDEO")) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .clickable { isVideoPlaying = !isVideoPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Video timeline scrubber badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xBB000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isVideoPlaying) "00:03 / 00:05 • 60 FPS" else "00:00 / 00:05 (VEO 1080p)",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Download / Save to Gallery Button
            FuturisticButton(
                text = "SAVE TO GALLERY",
                icon = Icons.Default.Download,
                onClick = {
                    if (job.resultUrl != null) {
                        viewModel.saveToGallery(job.resultUrl, job.prompt)
                    }
                },
                modifier = Modifier.weight(1f),
                testTag = "save_gallery_button"
            )

            // Share via Android Intent
            FuturisticButton(
                text = "SHARE",
                icon = Icons.Default.Share,
                isSecondary = true,
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Created with SYHAKAL AI")
                        putExtra(Intent.EXTRA_TEXT, "Generated with SYHAKAL AI Studio:\n\"${job.prompt}\"\nModel: ${job.modelName}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share AI Creation"))
                },
                modifier = Modifier.weight(1f),
                testTag = "share_creation_button"
            )
        }

        // Publish to Community Showcase Button
        OutlinedButton(
            onClick = {
                publishTitle = job.prompt.take(30)
                showPublishDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("publish_to_community_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)))
        ) {
            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publish to Global Community Showcase", fontWeight = FontWeight.Bold)
        }

        // Project Metadata Specs
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PROJECT METADATA",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(10.dp))

            MetadataItem("Prompt", job.prompt, onCopy = {
                clipboardManager.setText(AnnotatedString(job.prompt))
            })
            MetadataItem("Architecture Model", job.modelName)
            MetadataItem("Aspect Ratio", job.aspectRatio)
            MetadataItem("Quality Tier", job.quality)
            MetadataItem("Credits Consumed", "${job.creditsCost} CR")
            MetadataItem("Decentralized ID", currentUser?.did ?: "did:syhakal:verified")
            val signature = DidManager.signPayload(currentUser?.did ?: "core", job.id)
            MetadataItem("Digital Signature", signature)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Publish Dialog
    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            containerColor = CyberSurface,
            title = {
                Text("PUBLISH TO COMMUNITY", color = NeonCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Share this creation on the global feed for creators worldwide to view, like, comment, and remix your prompt.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = publishTitle,
                        onValueChange = { publishTitle = it },
                        label = { Text("Post Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val media = job.resultUrl ?: "sample_creation"
                        val type = if (job.type.contains("VIDEO")) "VIDEO" else "IMAGE"
                        viewModel.publishToCommunity(
                            title = publishTitle.ifBlank { "Neural Creation" },
                            prompt = job.prompt,
                            mediaPath = media,
                            mediaType = type,
                            aspectRatio = job.aspectRatio,
                            modelUsed = job.modelName
                        ) { success ->
                            if (success) showPublishDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Publish Now", color = Color(0xFF03161C), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun MetadataItem(label: String, value: String, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 12.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            if (onCopy != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = NeonCyan,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = onCopy)
                )
            }
        }
    }
}
