package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.JobEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SyhakalViewModel

@Composable
fun LibraryScreen(
    viewModel: SyhakalViewModel,
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val userJobs by viewModel.userJobs.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredJobs = when (selectedFilter) {
        "Images" -> userJobs.filter { it.type == "TEXT_TO_IMAGE" }
        "Videos" -> userJobs.filter { it.type.contains("VIDEO") }
        "Favorites" -> userJobs.filter { it.isFavorite }
        else -> userJobs
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        GlowingHeader(
            title = "CREATIVE VAULT",
            subtitle = "${userJobs.size} synthesized assets in local database",
            badgeText = "STORED ON-DEVICE"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Images", "Videos", "Favorites").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else CardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .background(if (isSelected) CyberSurfaceElevated else CyberSurface)
                        .clickable { selectedFilter = filter },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonCyan else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No creations found in $selectedFilter filter.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FuturisticButton(
                        text = "CREATE NEW ASSET",
                        icon = Icons.Default.Add,
                        onClick = onNavigateToCreate,
                        testTag = "empty_library_create_button"
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(filteredJobs, key = { it.id }) { job ->
                    LibraryGridItem(
                        job = job,
                        onClick = { onNavigateToJobDetail(job.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(job.id) },
                        onDelete = { viewModel.deleteJob(job.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryGridItem(
    job: JobEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                MediaThumbnail(
                    mediaUrl = job.resultUrl ?: "",
                    contentDescription = job.prompt,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite icon overlay
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x88000000))
                ) {
                    Icon(
                        imageVector = if (job.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (job.isFavorite) CyberPink else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Type badge overlay
                if (job.type.contains("VIDEO")) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xAA000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("VIDEO", fontSize = 9.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.prompt,
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = job.status)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
