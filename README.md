# Music App

A modern Android music player app built with Jetpack Compose, mimicking the Apple Music design.

## Features

- **Media3 (ExoPlayer)** for background playback.
- **Haze** for glassmorphism effects.
- **Coil** for image loading.
- **Jetpack Compose** for UI.
- **GitHub Actions** for CI/CD.
- **Local library** support via MediaStore (on-device music).

## Build

To build the project, run:

```bash
gradle assembleDebug
```

## Notes

- The app requests audio library permission on first launch to read local music.
