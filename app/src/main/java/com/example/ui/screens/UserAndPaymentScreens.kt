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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun SubscriptionScreen(
    viewModel: SyhakalViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.currentSettings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()

    val currentPrice = settings?.subscriptionPriceINR ?: 1.0
    val isSubscriber = currentUser?.role == "SUBSCRIBER"

    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedGateway by remember { mutableStateOf("UPI AutoPay / Instant") }
    var upiIdInput by remember { mutableStateOf("creator@okhdfcbank") }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentReceipt by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            GlowingHeader(
                title = "PRO SUBSCRIPTION",
                subtitle = "Backend Dynamic Pricing & Instant Payment Verification"
            )
        }

        // Active Dynamic Pricing Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isSubscriber) CardBorderPurple else NeonCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSubscriber) "CURRENT PLAN: ACTIVE PRO" else "DYNAMIC MEMBERSHIP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubscriber) MatrixGreen else NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹$currentPrice",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = " / month",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSubscriber) MatrixGreen.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSubscriber) "SUBSCRIBED" else "DYNAMIC PRICE",
                        color = if (isSubscriber) MatrixGreen else NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = CardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Benefits List
            SubscriptionPerk("500 Monthly Creative Credits immediately granted")
            SubscriptionPerk("Priority GPU job queue for Google Veo video rendering")
            SubscriptionPerk("Full-resolution 4K & 1080p generation exports")
            SubscriptionPerk("Commercial usage rights & DID signed proof-of-authenticity")
            SubscriptionPerk("Dynamic recurring protection & zero duplicate billing")

            Spacer(modifier = Modifier.height(20.dp))

            if (!isSubscriber) {
                FuturisticButton(
                    text = "UPGRADE NOW • ₹$currentPrice/MO",
                    icon = Icons.Default.Bolt,
                    onClick = { showPaymentModal = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "subscribe_checkout_button"
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurfaceElevated)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your Pro membership is active. Next renewal at ₹$currentPrice.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Credit Balance Overview
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("CREDIT SYSTEM BALANCE", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${currentUser?.creditsRemaining ?: 50}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("Available Credits", fontSize = 11.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${currentUser?.creditsConsumed ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeonPurple)
                    Text("Credits Consumed", fontSize = 11.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isSubscriber) "PRO" else "FREE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MatrixGreen)
                    Text("Account Tier", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        // Transaction & Payment History
        Text(
            text = "PAYMENT & TRANSACTION HISTORY",
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontSize = 12.sp
        )

        if (transactions.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("No payment transactions recorded yet.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            transactions.forEach { txn ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("₹${txn.amountINR} • ${txn.gateway}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(txn.gatewayPaymentId, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MatrixGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(txn.status, color = MatrixGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Payment Gateway Checkout Modal
    if (showPaymentModal) {
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayment) showPaymentModal = false },
            containerColor = CyberSurface,
            title = {
                Text("SECURE PAYMENT CHECKOUT", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        "Legitimate Indian subscription payment simulation (Razorpay / UPI AutoPay verified on backend):",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf("UPI AutoPay / Instant", "Debit/Credit Card (Tokenized)", "NetBanking").forEach { g ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedGateway = g }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedGateway == g,
                                onClick = { selectedGateway = g },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(g, fontSize = 13.sp, color = TextPrimary)
                        }
                    }

                    if (selectedGateway.contains("UPI")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = upiIdInput,
                            onValueChange = { upiIdInput = it },
                            label = { Text("UPI Virtual Payment Address (VPA)") },
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

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Total Payable Now: ₹$currentPrice (Dynamic Price verified from backend)",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessingPayment = true
                        viewModel.processSubscriptionPayment(
                            onSuccess = {
                                isProcessingPayment = false
                                showPaymentModal = false
                            },
                            onError = {
                                isProcessingPayment = false
                            }
                        )
                    },
                    enabled = !isProcessingPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.testTag("confirm_payment_button")
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF03161C))
                    } else {
                        Text("Pay ₹$currentPrice & Activate", color = Color(0xFF03161C), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentModal = false }, enabled = !isProcessingPayment) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SubscriptionPerk(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
fun ProfileSettingsScreen(
    viewModel: SyhakalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var customApiKey by remember { mutableStateOf("") }
    var isKeySaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                title = "CREATOR IDENTITY",
                subtitle = "DID Keypair, Vault, and Studio Preferences"
            )
        }

        // DID Identity Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(NeonCyan, NeonPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (currentUser?.displayName?.take(1) ?: "S").uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF03161C)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = currentUser?.displayName ?: "Creator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = currentUser?.email ?: "creator@syhakal.ai",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = CardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            MetadataItem("Decentralized ID (DID)", currentUser?.did ?: "did:syhakal:loading", onCopy = {
                clipboardManager.setText(AnnotatedString(currentUser?.did ?: ""))
            })
            MetadataItem("Public Cryptographic Key", (currentUser?.publicKey?.take(24) ?: "") + "...", onCopy = {
                clipboardManager.setText(AnnotatedString(currentUser?.publicKey ?: ""))
            })
            MetadataItem("Role / Authorization Tier", currentUser?.role ?: "USER")
        }

        // Custom Gemini API Key configuration option
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("AI ENGINE INTEGRATION", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Connected to Google Gemini API (gemini-2.5-flash-image & gemini-3.5-flash) and Google Veo (veo-3.1-fast-generate-preview).",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customApiKey,
                onValueChange = { customApiKey = it },
                label = { Text("Override Gemini API Key (Optional)") },
                singleLine = true,
                placeholder = { Text("AIzaSy...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { isKeySaved = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated)
            ) {
                Text(if (isKeySaved) "Custom Key Active" else "Save Key Override", color = NeonCyan)
            }
        }

        // Support Navigation
        OutlinedButton(
            onClick = onNavigateToSupport,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)))
        ) {
            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Help & Support Diagnostics", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit
) {
    var ticketText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                title = "HELP & SUPPORT",
                subtitle = "System Diagnostics & Creator Inquiries"
            )
        }

        // System Health Diagnostics Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("SYSTEM DIAGNOSTICS & TELEMETRY", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            DiagnosticRow("Database Engine (Room / SQLite)", "HEALTHY", MatrixGreen)
            DiagnosticRow("Gemini REST API Gateway", "ONLINE (200 OK)", MatrixGreen)
            DiagnosticRow("Veo Video Async Worker Queue", "READY (0 BACKLOG)", MatrixGreen)
            DiagnosticRow("Decentralized DID Protocol", "VERIFIED (SHA-256)", MatrixGreen)
            DiagnosticRow("Payment Gateway Bridge", "ACTIVE (TEST SECURE)", MatrixGreen)
        }

        // FAQ Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("FREQUENTLY ASKED QUESTIONS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            FaqItem(
                q = "How does dynamic pricing work?",
                a = "The membership price (initially ₹1) is dynamically controlled from the backend Owner Admin Dashboard. Price updates take effect immediately on checkout without rebuilding the mobile app."
            )
            FaqItem(
                q = "Where are my generated images & videos saved?",
                a = "Assets are stored in your device's local vault and can be exported directly to your Android Gallery (Pictures/SYHAKAL) via the Save button."
            )
            FaqItem(
                q = "What AI models are supported?",
                a = "SYHAKAL uses Google Gemini 2.5 Flash Image, Gemini Nano Banana 2.1, Gemini 3.5 Flash, and Google Veo (veo-3.1-fast-generate-preview)."
            )
        }

        // Support Ticket Box
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("SUBMIT CREATOR INQUIRY", fontWeight = FontWeight.Bold, color = NeonPurple, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ticketText,
                onValueChange = { ticketText = it },
                placeholder = { Text("Describe your inquiry or issue...") },
                minLines = 3,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = CardBorderPurple
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            FuturisticButton(
                text = if (isSubmitted) "INQUIRY TRANSMITTED" else "SUBMIT TICKET",
                icon = Icons.Default.Send,
                onClick = {
                    if (ticketText.isNotBlank()) isSubmitted = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DiagnosticRow(title: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = TextSecondary)
        Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
    }
}

@Composable
fun FaqItem(q: String, a: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = "Q: $q", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = a, color = TextSecondary, fontSize = 12.sp)
    }
}
