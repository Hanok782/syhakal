package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun ImageGeneratorScreen(
    viewModel: SyhakalViewModel,
    onJobQueued: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("Hyper-detailed cybernetic crystal falcon soaring across neon clouds") }
    var negativePrompt by remember { mutableStateOf("blurry, low quality, artifacts, watermark") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedModel by remember { mutableStateOf("gemini-2.5-flash-image") }
    var selectedQuality by remember { mutableStateOf("HD 1080p") }
    var isEnhancing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val styles = listOf("Cyberpunk", "Cinematic 3D", "Anime", "Photorealistic", "Synthwave", "Surrealist")
    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:4")
    val models = listOf("gemini-2.5-flash-image", "gemini-nano-banana-2.1")
    val qualities = listOf("Standard", "HD 1080p", "Ultra 4K")

    val settings by viewModel.currentSettings.collectAsState()
    val cost = settings?.creditCostImage ?: 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        GlowingHeader(
            title = "AI IMAGE GENERATOR",
            subtitle = "Synthesize visuals using Google Gemini 2.5 Flash Image",
            badgeText = "$cost CR"
        )

        // Prompt input with Gemini AI Enhancer
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CREATIVE PROMPT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                TextButton(
                    onClick = {
                        if (prompt.isNotBlank() && !isEnhancing) {
                            isEnhancing = true
                            viewModel.enhancePrompt(prompt, selectedStyle) { enhanced ->
                                prompt = enhanced
                                isEnhancing = false
                            }
                        }
                    },
                    enabled = !isEnhancing,
                    modifier = Modifier.testTag("enhance_prompt_button")
                ) {
                    if (isEnhancing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gemini AI Enhance", fontSize = 11.sp, color = NeonCyan)
                    }
                }
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Describe the visual you want to manifest...") },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("image_prompt_input")
            )
        }

        // Style Chips
        Column {
            Text(
                text = "ARTISTIC STYLE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.take(3).forEach { style ->
                    StyleChip(
                        name = style,
                        isSelected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.drop(3).forEach { style ->
                    StyleChip(
                        name = style,
                        isSelected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Aspect Ratio
        Column {
            Text(
                text = "ASPECT RATIO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspectRatios.forEach { ratio ->
                    StyleChip(
                        name = ratio,
                        isSelected = selectedAspectRatio == ratio,
                        onClick = { selectedAspectRatio = ratio },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Model & Quality
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "MODEL ARCHITECTURE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            models.forEach { model ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedModel == model) CyberSurfaceElevated else Color.Transparent)
                        .clickable { selectedModel = model }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedModel == model,
                        onClick = { selectedModel = model },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = model, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = ErrorRed,
                fontSize = 12.sp
            )
        }

        // Submit Button
        FuturisticButton(
            text = "SYNTHESIZE IMAGE ($cost CREDITS)",
            icon = Icons.Default.Bolt,
            onClick = {
                if (prompt.isBlank()) {
                    errorMessage = "Please enter a creative prompt"
                    return@FuturisticButton
                }
                errorMessage = null
                viewModel.submitImageJob(
                    prompt = prompt,
                    negativePrompt = negativePrompt,
                    modelName = selectedModel,
                    aspectRatio = selectedAspectRatio,
                    quality = selectedQuality,
                    onSuccess = { jobId -> onJobQueued(jobId) },
                    onError = { err -> errorMessage = err }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "submit_image_gen_button"
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun VideoGeneratorScreen(
    viewModel: SyhakalViewModel,
    onJobQueued: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("Futuristic interstellar mothership departing warp portal, cosmic dust vortices, 1080p cinematic camera sweep") }
    var motionStrength by remember { mutableStateOf(6.0f) }
    var selectedMotionType by remember { mutableStateOf("Orbital Sweep") }
    var selectedModel by remember { mutableStateOf("veo-3.1-fast-generate-preview") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val motionTypes = listOf("Orbital Sweep", "Dolly Zoom", "Drone Flyover", "Dynamic Pan", "Static Focus")
    val settings by viewModel.currentSettings.collectAsState()
    val cost = settings?.creditCostVideo ?: 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        GlowingHeader(
            title = "AI TEXT-TO-VIDEO",
            subtitle = "Render cinematic motion shots using Google Veo Fast",
            badgeText = "$cost CR"
        )

        // Video Prompt
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "MOTION BRIEF & ACTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Describe dynamic motion, lighting changes, camera angle...") },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = CardBorderPurple
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_prompt_input")
            )
        }

        // Camera Motion Preset
        Column {
            Text(
                text = "CAMERA TRAJECTORY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                motionTypes.take(3).forEach { m ->
                    StyleChip(
                        name = m,
                        isSelected = selectedMotionType == m,
                        onClick = { selectedMotionType = m },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                motionTypes.drop(3).forEach { m ->
                    StyleChip(
                        name = m,
                        isSelected = selectedMotionType == m,
                        onClick = { selectedMotionType = m },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Motion Dynamics Slider
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MOTION DYNAMICS INTENSITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "${motionStrength.toInt()}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple
                )
            }
            Slider(
                value = motionStrength,
                onValueChange = { motionStrength = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = NeonPurple,
                    activeTrackColor = NeonPurple,
                    inactiveTrackColor = CyberSurfaceElevated
                )
            )
        }

        // Model Selector
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "VEO VIDEO GENERATION ENGINE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf("veo-3.1-fast-generate-preview", "veo-3.1-generate-preview").forEach { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedModel == m) CyberSurfaceElevated else Color.Transparent)
                        .clickable { selectedModel = m }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedModel == m,
                        onClick = { selectedModel = m },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonPurple)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = m, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = ErrorRed, fontSize = 12.sp)
        }

        FuturisticButton(
            text = "RENDER VEO VIDEO ($cost CREDITS)",
            icon = Icons.Default.Videocam,
            onClick = {
                if (prompt.isBlank()) {
                    errorMessage = "Prompt cannot be empty"
                    return@FuturisticButton
                }
                errorMessage = null
                viewModel.submitVideoJob(
                    prompt = "$selectedMotionType: $prompt",
                    motionStrength = motionStrength,
                    durationSeconds = 5,
                    modelName = selectedModel,
                    aspectRatio = selectedAspectRatio,
                    quality = "1080p HD",
                    onSuccess = { jobId -> onJobQueued(jobId) },
                    onError = { err -> errorMessage = err }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "submit_video_gen_button"
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ImageToVideoScreen(
    viewModel: SyhakalViewModel,
    onJobQueued: (String) -> Unit
) {
    var motionPrompt by remember { mutableStateOf("Breathe gentle wind and camera tilt-up, bringing the portrait to life") }
    var motionStrength by remember { mutableStateOf(5.0f) }
    var selectedJobImage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val userJobs by viewModel.userJobs.collectAsState()
    val availableImages = userJobs.filter { it.resultUrl != null && it.type == "TEXT_TO_IMAGE" }

    val settings by viewModel.currentSettings.collectAsState()
    val cost = settings?.creditCostVideo ?: 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        GlowingHeader(
            title = "IMAGE-TO-VIDEO",
            subtitle = "Transform static artwork into animated video clips",
            badgeText = "$cost CR"
        )

        // Select Image Source
        Text(
            text = "SELECT SEED IMAGE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        if (availableImages.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No images created yet. You can create an image first or proceed with creative synthetic seed.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                availableImages.take(3).forEach { job ->
                    val isSelected = selectedJobImage == job.resultUrl
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CyberPink else CardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedJobImage = job.resultUrl }
                    ) {
                        MediaThumbnail(
                            mediaUrl = job.resultUrl ?: "",
                            contentDescription = job.prompt,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Motion Guidance
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ANIMATION INSTRUCTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberPink
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = motionPrompt,
                onValueChange = { motionPrompt = it },
                placeholder = { Text("Specify movement (e.g. hair flowing, camera orbiting, lighting shifting)...") },
                minLines = 3,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyberPink,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Motion Intensity
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ANIMATION DYNAMICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "${motionStrength.toInt()}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPink
                )
            }
            Slider(
                value = motionStrength,
                onValueChange = { motionStrength = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = CyberPink,
                    activeTrackColor = CyberPink,
                    inactiveTrackColor = CyberSurfaceElevated
                )
            )
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = ErrorRed, fontSize = 12.sp)
        }

        FuturisticButton(
            text = "ANIMATE IMAGE ($cost CREDITS)",
            icon = Icons.Default.MotionPhotosAuto,
            onClick = {
                errorMessage = null
                viewModel.submitVideoJob(
                    prompt = "Animate image: $motionPrompt",
                    motionStrength = motionStrength,
                    durationSeconds = 5,
                    modelName = "veo-3.1-fast-generate-preview",
                    aspectRatio = "1:1",
                    quality = "1080p HD",
                    inputImageUrl = selectedJobImage ?: "sample_seed",
                    onSuccess = { jobId -> onJobQueued(jobId) },
                    onError = { err -> errorMessage = err }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "submit_img_to_video_button"
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StyleChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) NeonCyan else CardBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (isSelected) CyberSurfaceElevated else CyberSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonCyan else TextPrimary,
            maxLines = 1
        )
    }
}
