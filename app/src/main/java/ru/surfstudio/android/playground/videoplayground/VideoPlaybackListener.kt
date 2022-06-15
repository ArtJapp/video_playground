package ru.surfstudio.android.playground.videoplayground

/**
 * Листнер состояния проигрывания видео
 */
interface VideoPlaybackListener {

    /**
     * Видео запущено
     */
    fun onVideoStarted()

    /**
     * Видео приостановлено
     */
    fun onVideoStopped()

    /**
     * Видео минимизировано
     */
    fun onVideoMinimized()
}