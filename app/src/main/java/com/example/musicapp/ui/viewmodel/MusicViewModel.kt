package com.example.musicapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.data.LocalMusicRepository
import com.example.musicapp.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalMusicRepository(application)
    private val player = ExoPlayer.Builder(application).build()

    private var progressJob: Job? = null
    private var playlist: List<Song> = emptyList()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _progress = MutableStateFlow(0f) // 0.0 ~ 1.0
    val progress = _progress.asStateFlow()

    init {
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player.currentMediaItemIndex
                if (index in playlist.indices) {
                    _currentSong.value = playlist[index]
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = max(0L, player.duration)
                }
            }
        })

        startProgressUpdates()
    }

    fun loadSongs() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                repository.querySongs()
            }
            _songs.value = list
            if (_currentSong.value == null && list.isNotEmpty()) {
                _currentSong.value = list.first()
            }
        }
    }

    fun playSong(song: Song) {
        if (_songs.value.isEmpty()) return
        val index = _songs.value.indexOfFirst { it.id == song.id }.let { idx ->
            if (idx >= 0) idx else 0
        }
        setPlaylist(_songs.value, index)
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.currentMediaItem == null && _currentSong.value != null) {
                playSong(_currentSong.value!!)
            } else {
                player.play()
            }
        }
    }

    fun skipToNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }
    }

    fun seekTo(progress: Float) {
        val duration = _duration.value
        if (duration > 0) {
            val position = (duration * progress).toLong()
            player.seekTo(position)
        }
    }

    private fun setPlaylist(list: List<Song>, index: Int) {
        playlist = list
        val items = list.map { song ->
            MediaItem.Builder()
                .setUri(Uri.parse(song.contentUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )
                .build()
        }
        player.setMediaItems(items, index, 0L)
        player.prepare()
        _currentSong.value = list[index]
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val duration = max(1L, player.duration)
                val position = max(0L, player.currentPosition)
                _duration.value = if (player.duration > 0) player.duration else _duration.value
                _position.value = position
                _progress.value = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                delay(500)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        player.release()
    }
}
