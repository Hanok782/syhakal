package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JobEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun HomeScreen(
    viewModel: SyhakalViewModel,
    onNavigateToImageGen: () -> Unit,
    onNavigateToVideoGen: () -> Unit,
    onNavigateToImgToVideo: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToJobDetail: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.currentSettings.collectAsState()
    val userJobs by viewModel.userJobs.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(NeonCyan, NeonPurple)))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(CyberBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SYHAKAL",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentUser?.did?.take(18) ?: "did:syhakal:core",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CreditsBadge(
                        credits = currentUser?.creditsRemaining ?: 50,
                        onClick = onNavigateToSubscription
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (viewModel.isOwnerModeUnlocked.value) {
                                onNavigateToAdmin()
                            } else {
                                showPinDialog = true
                            }
                        },
                        modifier = Modifier
                            .testTag("admin_shield_button")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceElevated)
                            .border(1.dp, CardBorderPurple, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Owner Dashboard",
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Announcement Banner
        item {
            val announcement = settings?.announcementText ?: "Dynamic AI models online."
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x2200F5FF))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = announcement,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Membership / Studio Status Card
        item {
            val isSubscriber = currentUser?.role == "SUBSCRIBER"
            val price = settings?.subscriptionPriceINR ?: 1.0

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isSubscriber) CardBorderPurple else CardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSubscriber) "PRO CREATOR TIER" else "FREE STARTER TIER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubscriber) NeonPurple else NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSubscriber) "Unlimited Latent Diffusion & Veo Access" else "₹$price/Month Dynamic Membership Available",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Credits: ${currentUser?.creditsRemaining ?: 50} CR • Consumed: ${currentUser?.creditsConsumed ?: 0} CR",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    if (!isSubscriber) {
                        FuturisticButton(
                            text = "UPGRADE ₹$price",
                            onClick = onNavigateToSubscription,
                            testTag = "home_upgrade_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Creation Engine Hub (Grid)
        item {
            GlowingHeader(
                title = "CREATION SUITE",
                subtitle = "Select AI modality to generate",
                badgeText = "GEMINI & VEO"
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CreationActionTile(
                    title = "AI Image",
                    subtitle = "Text to Image",
                    model = "Gemini 2.5",
                    icon = Icons.Default.Image,
                    gradient = listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC)),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToImageGen,
                    testTag = "action_ai_image"
                )
                Spacer(modifier = Modifier.width(12.dp))
                CreationActionTile(
                    title = "Text-to-Video",
                    subtitle = "Prompt to 1080p",
                    model = "Veo Fast",
                    icon = Icons.Default.Videocam,
                    gradient = listOf(Color(0xFF7B1FA2), Color(0xFF9C27B0), Color(0xFFE040FB)),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToVideoGen,
                    testTag = "action_text_video"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CreationActionTile(
                    title = "Image-to-Video",
                    subtitle = "Animate Still Images",
                    model = "Veo Motion",
                    icon = Icons.Default.MotionPhotosAuto,
                    gradient = listOf(Color(0xFFC2185B), Color(0xFFE91E63), Color(0xFFFF4081)),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToImgToVideo,
                    testTag = "action_img_video"
                )
                Spacer(modifier = Modifier.width(12.dp))
                CreationActionTile(
                    title = "Community",
                    subtitle = "Showcase & Remix",
                    model = "Decentralized",
                    icon = Icons.Default.Diversity3,
                    gradient = listOf(Color(0xFF00796B), Color(0xFF009688), Color(0xFF00BFA5)),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCommunity,
                    testTag = "action_community"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Recent Generation Projects Carousel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowingHeader(
                    title = "RECENT JOBS",
                    subtitle = "Your latest synthesized assets"
                )
                TextButton(
                    onClick = onNavigateToLibrary,
                    modifier = Modifier.testTag("home_view_all_library")
                ) {
                    Text("View All (${userJobs.size})", color = NeonCyan, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (userJobs.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 12.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No generations yet. Synthesize your first masterpiece above!",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(userJobs.take(8)) { job ->
                        RecentJobCard(
                            job = job,
                            onClick = { onNavigateToJobDetail(job.id) }
                        )
                    }
                }
            }
        }
    }

    // Owner PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = false
            },
            containerColor = CyberSurface,
            title = {
                Text(
                    text = "OWNER ADMIN ACCESS",
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the 4-digit Owner PIN to access real-time database management, pricing control, and revenue reports (Default: 8899).",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) {
                                pinInput = it
                                pinError = false
                            }
                        },
                        label = { Text("Owner PIN") },
                        singleLine = true,
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = CardBorderPurple
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Invalid PIN. Try 8899.", color = ErrorRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyOwnerPin(pinInput)) {
                            showPinDialog = false
                            pinInput = ""
                            onNavigateToAdmin()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.testTag("admin_pin_confirm")
                ) {
                    Text("Unlock Dashboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun CreationActionTile(
    title: String,
    subtitle: String,
    model: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradient.map { it.copy(alpha = 0.25f) }))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = model,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun RecentJobCard(
    job: JobEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            MediaThumbnail(
                mediaUrl = job.resultUrl ?: "",
                contentDescription = job.prompt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = job.prompt,
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = job.status)
                Text(
                    text = "${job.creditsCost} CR",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
