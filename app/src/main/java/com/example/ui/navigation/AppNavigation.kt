package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.JobEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object ImageGen : Screen("image_gen")
    object VideoGen : Screen("video_gen")
    object ImgToVideo : Screen("img_to_video")
    object JobProgress : Screen("job_progress")
    object Library : Screen("library")
    object Preview : Screen("preview")
    object Community : Screen("community")
    object Messages : Screen("messages")
    object Subscription : Screen("subscription")
    object Profile : Screen("profile")
    object Support : Screen("support")
    object Admin : Screen("admin")
}

data class NavItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun AppNavigation(
    viewModel: SyhakalViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var activeJobId by remember { mutableStateOf<String?>(null) }
    var previewJob by remember { mutableStateOf<JobEntity?>(null) }
    var remixPromptText by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackBarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val bottomNavItems = listOf(
        NavItem("Home", Screen.Home, Icons.Default.Home, "nav_home"),
        NavItem("Create", Screen.ImageGen, Icons.Default.AddCircle, "nav_create"),
        NavItem("Showcase", Screen.Community, Icons.Default.Diversity3, "nav_community"),
        NavItem("Vault", Screen.Library, Icons.Default.FolderSpecial, "nav_vault"),
        NavItem("Profile", Screen.Profile, Icons.Default.Person, "nav_profile")
    )

    val showBottomBar = currentScreen !in listOf(
        Screen.Splash,
        Screen.Auth,
        Screen.JobProgress,
        Screen.Preview
    )

    // Hardware/Gesture back handler for nested screens
    if (currentScreen != Screen.Home && currentScreen != Screen.Splash && currentScreen != Screen.Auth) {
        BackHandler {
            currentScreen = Screen.Home
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CyberSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = item.screen },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) NeonCyan else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    color = if (isSelected) NeonCyan else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonCyan.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        containerColor = CyberBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        onSplashFinished = { currentScreen = Screen.Home }
                    )
                }

                Screen.Auth -> {
                    AuthScreen(
                        viewModel = viewModel,
                        onAuthSuccess = { currentScreen = Screen.Home }
                    )
                }

                Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToImageGen = { currentScreen = Screen.ImageGen },
                        onNavigateToVideoGen = { currentScreen = Screen.VideoGen },
                        onNavigateToImgToVideo = { currentScreen = Screen.ImgToVideo },
                        onNavigateToLibrary = { currentScreen = Screen.Library },
                        onNavigateToCommunity = { currentScreen = Screen.Community },
                        onNavigateToSubscription = { currentScreen = Screen.Subscription },
                        onNavigateToAdmin = { currentScreen = Screen.Admin },
                        onNavigateToJobDetail = { jobId ->
                            val job = viewModel.userJobs.value.firstOrNull { it.id == jobId }
                            if (job != null) {
                                previewJob = job
                                currentScreen = Screen.Preview
                            }
                        }
                    )
                }

                Screen.ImageGen -> {
                    ImageGeneratorScreen(
                        viewModel = viewModel,
                        onJobQueued = { jobId ->
                            activeJobId = jobId
                            currentScreen = Screen.JobProgress
                        }
                    )
                }

                Screen.VideoGen -> {
                    VideoGeneratorScreen(
                        viewModel = viewModel,
                        onJobQueued = { jobId ->
                            activeJobId = jobId
                            currentScreen = Screen.JobProgress
                        }
                    )
                }

                Screen.ImgToVideo -> {
                    ImageToVideoScreen(
                        viewModel = viewModel,
                        onJobQueued = { jobId ->
                            activeJobId = jobId
                            currentScreen = Screen.JobProgress
                        }
                    )
                }

                Screen.JobProgress -> {
                    JobProgressScreen(
                        jobId = activeJobId ?: "",
                        viewModel = viewModel,
                        onNavigateToPreview = { job ->
                            previewJob = job
                            currentScreen = Screen.Preview
                        },
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }

                Screen.Library -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onNavigateToJobDetail = { jobId ->
                            val job = viewModel.userJobs.value.firstOrNull { it.id == jobId }
                            if (job != null) {
                                previewJob = job
                                currentScreen = Screen.Preview
                            }
                        },
                        onNavigateToCreate = { currentScreen = Screen.ImageGen }
                    )
                }

                Screen.Preview -> {
                    if (previewJob != null) {
                        PreviewDetailScreen(
                            job = previewJob!!,
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = Screen.Home }
                        )
                    } else {
                        currentScreen = Screen.Home
                    }
                }

                Screen.Community -> {
                    CommunityScreen(
                        viewModel = viewModel,
                        onRemixPrompt = { prompt ->
                            remixPromptText = prompt
                            currentScreen = Screen.ImageGen
                        },
                        onOpenMessages = { currentScreen = Screen.Messages }
                    )
                }

                Screen.Messages -> {
                    DirectMessagesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Community }
                    )
                }

                Screen.Subscription -> {
                    SubscriptionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }

                Screen.Profile -> {
                    ProfileSettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onNavigateToSupport = { currentScreen = Screen.Support }
                    )
                }

                Screen.Support -> {
                    HelpSupportScreen(
                        onNavigateBack = { currentScreen = Screen.Profile }
                    )
                }

                Screen.Admin -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }
}
