package ru.surfstudio.android.playground.videoplayground.pip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.surfstudio.android.playground.videoplayground.MainActivity

/**
 * Слушатель событий в режиме картинка-в-картинке
 */
class PipActionsReceiver(
    val onPlay: () -> Unit,
    val onPause: () -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            if (intent.action != MainActivity.ACTION_MEDIA_CONTROL) {
                return
            }

            when (intent.getIntExtra(MainActivity.EXTRA_CONTROL_TYPE, 0)) {
                MainActivity.PLAY_CONTROL_TYPE -> {
                    onPlay()
                }
                MainActivity.PAUSE_CONTROL_TYPE -> {
                    onPause()
                }
            }
        }

    }
}