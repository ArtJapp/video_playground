package ru.surfstudio.android.playground.videoplayground

import android.app.PictureInPictureParams

interface Minimizable {
    fun minimize()
    fun setPictureInPictureParams(params: PictureInPictureParams)
}