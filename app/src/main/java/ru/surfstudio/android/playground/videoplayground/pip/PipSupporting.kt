package ru.surfstudio.android.playground.videoplayground.pip

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import ru.surfstudio.android.playground.videoplayground.MainActivity
import ru.surfstudio.android.playground.videoplayground.Minimizable
import ru.surfstudio.android.playground.videoplayground.R
import ru.surfstudio.android.playground.videoplayground.VideoPlaybackListener

/**
 * Интерфейс экрана, поддерживающего режим картинка-в-картинке
 */
interface PipSupporting : Minimizable {

    var pictureInPictureBuilder: PictureInPictureParams.Builder

    @RequiresApi(Build.VERSION_CODES.O)
    fun configure() {
        pictureInPictureBuilder = PictureInPictureParams.Builder()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun configurePictureInPictureActions(
        context: Context,
    ): VideoPlaybackListener {
        return object : VideoPlaybackListener {
            override fun onVideoStarted() {
                updatePictureInPictureActions(
                    context,
                    R.drawable.exo_controls_pause,
                    "pause the video",
                    MainActivity.PAUSE_CONTROL_TYPE,
                    MainActivity.PAUSE_REQUEST_CODE
                )
            }

            override fun onVideoStopped() {
                updatePictureInPictureActions(
                    context,
                    R.drawable.exo_controls_play,
                    "play the video",
                    MainActivity.PLAY_CONTROL_TYPE,
                    MainActivity.PLAY_REQUEST_CODE
                )
            }

            override fun onVideoMinimized() {
                minimize()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureActions(
        context: Context,
        @DrawableRes iconId: Int,
        title: String,
        controlType: Int,
        requestCode: Int
    ) {
        pictureInPictureBuilder.setActions(
            listOf(
                createAction(context, iconId, title, controlType, requestCode)
                // тут можно добавить какие-то дополнительные экшены для режима картинка-в-картинке
            )
        )
        setPictureInPictureParams(pictureInPictureBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAction(
        context: Context,
        @DrawableRes iconId: Int,
        title: String,
        controlType: Int,
        requestCode: Int
    ) = RemoteAction(
        Icon.createWithResource(context, iconId),
        title,
        title,
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(MainActivity.ACTION_MEDIA_CONTROL).putExtra(
                MainActivity.EXTRA_CONTROL_TYPE,
                controlType
            ),
            0
        )
    )
}

