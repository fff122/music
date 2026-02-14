package com.example.musicapp.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String, // Can be network URL or local file://
    val duration: Long = 0L,
    val path: String = ""
)

// Simple Mock Data for Preview
val MOCK_SONG = Song(
    id = "1",
    title = "Blinding Lights",
    artist = "The Weeknd",
    coverUrl = "https://upload.wikimedia.org/wikipedia/en/e/e6/The_Weeknd_-_Blinding_Lights.png",
    duration = 200000L
)
