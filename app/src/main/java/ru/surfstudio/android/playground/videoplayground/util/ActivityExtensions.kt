package ru.surfstudio.android.playground.videoplayground.util

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.surfstudio.android.playground.videoplayground.MainActivity

fun View.goFullMode() {
    systemUiVisibility = FULL_MODE_FLAG

//    supportActionBar?.hide()
}

fun Activity.isPictureInPictureAvailable(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
} else {
    false
}

private const val FULL_MODE_FLAG =
    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
private const val PORTRAIT_MODE_FLAG = View.SYSTEM_UI_FLAG_VISIBLE