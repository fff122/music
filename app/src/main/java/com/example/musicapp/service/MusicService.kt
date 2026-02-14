package com.example.musicapp.service

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: Player

    // Called when Service is created
    @OptIn(UnstableApi::class) // Media3 some APIs are still marked as unstable
    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // 2. Create MediaSession
        // MediaSession connects the outside world (UI, notification bar, headset buttons) with the player (ExoPlayer)
        mediaSession = MediaSession.Builder(this, player)
            // Optional: Set Session callback for custom commands
            // .setCallback(MyMediaSessionCallback()) 
            .build()
    }

    // Called when other components (like UI) try to bind to this Service
    // Return the MediaSession we created, system handles the connection automatically
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Called when Service is destroyed, must release resources
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
