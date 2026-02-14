package com.example.musicapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.musicapp.data.model.Song
import com.example.musicapp.ui.theme.AppleRed
import com.example.musicapp.ui.theme.TextPrimary
import com.example.musicapp.ui.theme.TextSecondary
import com.example.musicapp.ui.viewmodel.MusicViewModel

@Composable
fun MainScreen(viewModel: MusicViewModel = viewModel()) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    
    // Control whether full screen player is shown
    var showNowPlaying by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // Core layout: MiniPlayer above NavigationBar
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini Player (Only show when there is a song)
                AnimatedVisibility(
                    visible = currentSong != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onClick = { showNowPlaying = true }
                        )
                    }
                }
                
                // Bottom Navigation
                MusicNavigationBar()
            }
        }
    ) { innerPadding ->
        // Main Content Area
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White)
        ) {
            Text(
                text = "Library / Browse / Search Content",
                modifier = Modifier.align(Alignment.Center),
                color = TextSecondary
            )
        }
    }

    // Full Screen Now Playing (Cover)
    if (showNowPlaying && currentSong != null) {
        NowPlayingScreen(
            song = currentSong!!,
            isPlaying = isPlaying,
            onClose = { showNowPlaying = false },
            onPlayPause = { viewModel.togglePlayPause() },
            viewModel = viewModel
        )
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }, // Click to expand full screen
        color = Color(0xFFF5F5F5), // Light gray background, similar to frosted glass base
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Small Cover
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Title
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            // 3. Play Control (Play/Pause)
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = TextPrimary
                )
            }

            // 4. Next Song
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
fun MusicNavigationBar() {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.95f),
        contentColor = AppleRed
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = null) },
            label = { Text("Library") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleRed,
                selectedTextColor = AppleRed,
                indicatorColor = AppleRed.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
            label = { Text("Browse") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("Search") }
        )
    }
}
