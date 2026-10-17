package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: SyhakalViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.currentSettings.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val moderationReports by viewModel.moderationReports.collectAsState()

    var selectedTab by remember { mutableStateOf("Pricing & Revenue") }
    val tabs = listOf("Pricing & Revenue", "Users", "Job Monitor", "Moderation Hub", "Settings")

    var newPriceInput by remember { mutableStateOf((settings?.subscriptionPriceINR ?: 1.0).toString()) }
    var announcementInput by remember { mutableStateOf(settings?.announcementText ?: "") }

    val totalRevenue = allTransactions.filter { it.status == "SUCCESS" }.sumOf { it.amountINR }
    val activeSubscribers = allUsers.count { it.role == "SUBSCRIBER" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
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
            GlowingHeader(
                title = "OWNER CONTROL CENTER",
                subtitle = "Authorized Backend Database Administration",
                badgeText = "OWNER SECURE"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = CyberSurface,
            contentColor = NeonPurple,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NeonPurple else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Contents
        when (selectedTab) {
            "Pricing & Revenue" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Revenue Overview Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RevenueMetricCard(
                            label = "Total Gross Revenue",
                            value = "₹${"%.2f".format(totalRevenue)}",
                            modifier = Modifier.weight(1f)
                        )
                        RevenueMetricCard(
                            label = "Active Subscribers",
                            value = "$activeSubscribers",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Dynamic Pricing Control Center
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = NeonPurple
                    ) {
                        Text(
                            text = "DYNAMIC SUBSCRIPTION PRICING",
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Modify the active subscription price in the backend database. Changes take effect across all mobile app checkout screens instantly without publishing a new app release.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newPriceInput,
                                onValueChange = { newPriceInput = it },
                                label = { Text("Price in INR (₹)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_price_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = NeonPurple,
                                    unfocusedBorderColor = CardBorderPurple
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    val price = newPriceInput.toDoubleOrNull()
                                    if (price != null && price >= 0) {
                                        viewModel.updateSubscriptionPrice(price)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("admin_save_price_button")
                            ) {
                                Text("Update Live", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Current Active Database Price: ₹${settings?.subscriptionPriceINR ?: 1.0} per month",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }

                    // Transaction Log
                    Text(
                        text = "LIVE PAYMENT TRANSACTIONS (${allTransactions.size})",
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    allTransactions.take(10).forEach { txn ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "₹${txn.amountINR} • ${txn.gateway}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "User: ${txn.userId} • ${txn.gatewayOrderId}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MatrixGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(txn.status, color = MatrixGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            "Users" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(allUsers) { user ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (user.role == "SUBSCRIBER") NeonPurple.copy(alpha = 0.2f) else CyberSurfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(user.role, color = if (user.role == "SUBSCRIBER") NeonPurple else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (user.isBanned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("BANNED", color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(user.did, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                    Text("Credits Remaining: ${user.creditsRemaining} CR", fontSize = 11.sp, color = NeonCyan)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.addCreditsToUser(user.userId, 100) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.AddCard, contentDescription = "Add Credits", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleUserBan(user.userId, user.isBanned) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (user.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                                            contentDescription = "Ban/Unban",
                                            tint = if (user.isBanned) MatrixGreen else ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Job Monitor" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(allJobs) { job ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(job.prompt, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 12.sp, maxLines = 2)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${job.type} • ${job.modelName} • Cost: ${job.creditsCost} CR", fontSize = 10.sp, color = TextSecondary)
                                    if (job.errorMessage != null) {
                                        Text("Error: ${job.errorMessage}", fontSize = 10.sp, color = ErrorRed)
                                    }
                                }
                                StatusBadge(status = job.status)
                            }
                        }
                    }
                }
            }

            "Moderation Hub" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    if (moderationReports.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No pending moderation reports. Community feed is safe.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(moderationReports) { rep ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Reason: ${rep.reason}", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 13.sp)
                                        Text(rep.status, color = WarningAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Reported Post ID: ${rep.targetId} by ${rep.reportedByDid.take(14)}...", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.resolveModerationReport(rep.id, rep.targetId, "APPROVE") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Approve Post", fontSize = 11.sp, color = MatrixGreen)
                                        }
                                        Button(
                                            onClick = { viewModel.resolveModerationReport(rep.id, rep.targetId, "REMOVE") },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Remove Post", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Settings" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("GLOBAL SYSTEM ANNOUNCEMENT", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = announcementInput,
                            onValueChange = { announcementInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val s = settings ?: return@Button
                                viewModel.updateGlobalSettings(s.copy(announcementText = announcementInput))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Broadcast Announcement", color = Color(0xFF03161C), fontWeight = FontWeight.Bold)
                        }
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("APP SECURITY & ACCESS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        MetadataItem("Admin PIN", settings?.adminPin ?: "8899")
                        MetadataItem("Owner Email", settings?.ownerEmail ?: "owner@syhakal.ai")
                        MetadataItem("Initial Free Credits", "${settings?.initialFreeCredits ?: 50} CR")
                        MetadataItem("Credit Cost (Image)", "${settings?.creditCostImage ?: 5} CR")
                        MetadataItem("Credit Cost (Video)", "${settings?.creditCostVideo ?: 15} CR")
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun RevenueMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .border(1.dp, CardBorderPurple, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = NeonPurple)
        }
    }
}
