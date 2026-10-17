package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
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
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var bootStage by remember { mutableStateOf("INITIALIZING CORE...") }
    var bootProgress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(Unit) {
        delay(400)
        bootStage = "ESTABLISHING DID CRYPTO VAULT..."
        bootProgress = 0.45f
        delay(500)
        bootStage = "CONNECTING GEMINI NEURAL MODELS..."
        bootProgress = 0.85f
        delay(500)
        bootStage = "SYHAKAL v2.0 READY"
        bootProgress = 1.0f
        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CyberBackground, Color(0xFF140D26), CyberBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Glowing AI Core
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(NeonCyan, NeonPurple, Color.Transparent)))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(CyberBackground)
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "SYHAKAL Logo",
                        tint = NeonCyan,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SYHAKAL",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 4.sp
            )

            Text(
                text = "DECENTRALIZED AI CREATIVE STUDIO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = { bootProgress },
                modifier = Modifier
                    .width(220.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = NeonCyan,
                trackColor = CyberSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = bootStage,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AuthScreen(
    viewModel: SyhakalViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var generatedDid by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isRegisterMode) "CREATE IDENTITY" else "AUTHENTICATE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = "Decentralized DID & Google Sign-In Gateway",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Creator Alias / Handle") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_alias_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Creator Email (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                FuturisticButton(
                    text = if (isRegisterMode) "GENERATE DID WALLET" else "ENTER STUDIO",
                    icon = Icons.Default.Fingerprint,
                    onClick = {
                        val name = displayName.ifBlank { "Quantum Creator" }
                        val mail = email.ifBlank { "creator@syhakal.ai" }
                        viewModel.loginOrRegister(name, mail)
                        onAuthSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_submit_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Sign in with Google Button (Standard provisioned pattern)
                OutlinedButton(
                    onClick = {
                        viewModel.loginOrRegister("Google Creator", "google.user@syhakal.ai")
                        onAuthSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("google_signin_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)))
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign in with Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = { isRegisterMode = !isRegisterMode },
                modifier = Modifier.testTag("auth_toggle_mode")
            ) {
                Text(
                    text = if (isRegisterMode) "Already have a DID? Authorize here" else "New to SYHAKAL? Generate DID Keypair",
                    color = NeonCyan,
                    fontSize = 13.sp
                )
            }
        }
    }
}
