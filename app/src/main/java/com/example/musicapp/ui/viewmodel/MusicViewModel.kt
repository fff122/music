package com.example.musicapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.model.Song
import com.example.musicapp.data.model.MOCK_SONG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {
    
    // UI State
    private val _currentSong = MutableStateFlow<Song?>(MOCK_SONG) // Default load a Mock song
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0.3f) // 0.0 ~ 1.0
    val progress = _progress.asStateFlow()

    // Simulate playback control
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun skipToNext() {
        // TODO: Implement skip logic
    }
    
    fun seekTo(value: Float) {
        _progress.value = value
    }
}
