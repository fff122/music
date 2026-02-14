package com.example.musicapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.musicapp.data.model.Song
import com.example.musicapp.ui.theme.AppleRed
import com.example.musicapp.ui.theme.BackgroundLight
import com.example.musicapp.ui.theme.TextPrimary
import com.example.musicapp.ui.theme.TextSecondary
import com.example.musicapp.ui.viewmodel.MusicViewModel

private enum class MusicTab(val label: String) {
    Library("Library"),
    Browse("Browse"),
    Search("Search")
}

@Composable
fun MainScreen(viewModel: MusicViewModel = viewModel()) {
    val context = LocalContext.current
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val progress by viewModel.progress.collectAsState()

    var showNowPlaying by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(MusicTab.Library) }

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var permissionGranted by remember {
        mutableStateOf(permissions.all { hasPermission(context, it) })
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionGranted = results.values.all { it }
        if (permissionGranted) {
            viewModel.loadSongs()
        }
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            viewModel.loadSongs()
        }
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = currentSong != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            progress = progress,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNext = { viewModel.skipToNext() },
                            onClick = { showNowPlaying = true }
                        )
                    }
                }
                MusicNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(BackgroundLight)
        ) {
            if (!permissionGranted) {
                PermissionScreen(
                    onRequest = { permissionLauncher.launch(permissions) }
                )
            } else {
                when (selectedTab) {
                    MusicTab.Library -> LibraryScreen(
                        songs = songs,
                        onSongClick = { viewModel.playSong(it) }
                    )
                    MusicTab.Browse -> BrowseScreen(
                        songs = songs,
                        onSongClick = { viewModel.playSong(it) }
                    )
                    MusicTab.Search -> SearchScreen(
                        songs = songs,
                        onSongClick = { viewModel.playSong(it) }
                    )
                }
            }
        }
    }

    if (showNowPlaying && currentSong != null) {
        NowPlayingScreen(
            song = currentSong!!,
            isPlaying = isPlaying,
            onClose = { showNowPlaying = false },
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.skipToNext() },
            onPrevious = { viewModel.skipToPrevious() },
            viewModel = viewModel
        )
    }
}

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Allow access to local music",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We need permission to read your on-device audio library.",
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = AppleRed)) {
            Text(text = "Grant Permission")
        }
    }
}

@Composable
private fun LibraryScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(title = "Library", subtitle = "Your local collection")
        if (songs.isEmpty()) {
            EmptyState(message = "No local songs found.")
        } else {
            SectionHeader(title = "Songs")
            SongList(songs = songs, onSongClick = onSongClick)
        }
    }
}

@Composable
private fun BrowseScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(title = "Browse", subtitle = "Recently added to this device")
        if (songs.isEmpty()) {
            EmptyState(message = "Add some music to see it here.")
        } else {
            val featured = songs.take(12)
            SectionHeader(title = "Recently Added")
            SongList(songs = featured, onSongClick = onSongClick)
        }
    }
}

@Composable
private fun SearchScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, songs) {
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, true) ||
                it.artist.contains(query, true) ||
                it.album.contains(query, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(title = "Search", subtitle = "Find songs, artists, albums")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search your library") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (filtered.isEmpty()) {
            EmptyState(message = "No matching results.")
        } else {
            SongList(songs = filtered, onSongClick = onSongClick)
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = subtitle, fontSize = 14.sp, color = TextSecondary)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = TextSecondary)
    }
}

@Composable
private fun SongList(songs: List<Song>, onSongClick: (Song) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(songs, key = { it.id }) { song ->
            SongRow(song = song, onClick = { onSongClick(song) })
        }
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(song = song, size = 56.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(text = song.artist, color = TextSecondary, fontSize = 12.sp)
        }
        Text(
            text = formatDuration(song.duration),
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AlbumArt(song: Song, size: Dp) {
    val placeholder = Brush.linearGradient(
        colors = listOf(Color(0xFFEEEFF3), Color(0xFFDADDE6))
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(placeholder)
    ) {
        if (!song.albumArtUri.isNullOrBlank()) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        color = Color.White,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = AppleRed,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArt(song = song, size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = song.title, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text(text = song.artist, color = TextSecondary, fontSize = 12.sp)
                }
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = TextPrimary
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicNavigationBar(
    selectedTab: MusicTab,
    onTabSelected: (MusicTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.98f),
        contentColor = AppleRed
    ) {
        NavigationBarItem(
            selected = selectedTab == MusicTab.Library,
            onClick = { onTabSelected(MusicTab.Library) },
            icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = null) },
            label = { Text("Library") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppleRed,
                selectedTextColor = AppleRed,
                indicatorColor = AppleRed.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == MusicTab.Browse,
            onClick = { onTabSelected(MusicTab.Browse) },
            icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
            label = { Text("Browse") }
        )
        NavigationBarItem(
            selected = selectedTab == MusicTab.Search,
            onClick = { onTabSelected(MusicTab.Search) },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("Search") }
        )
    }
}

private fun hasPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
