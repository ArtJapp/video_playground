package ru.surfstudio.android.playground.videoplayground

import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.appcompat.app.AppCompatActivity

class OrientationChangedListener(
    private val activity: AppCompatActivity
) : OrientationEventListener(activity) {
    override fun onOrientationChanged(orientation: Int) {
        if (checkOrientationChanged(orientation)) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    private fun checkOrientationChanged(orientationDegrees: Int): Boolean {
        val isTurnedLeft = epsilonCheck(
            orientationDegrees,
            MainActivity.LEFT_LANDSCAPE_DEGREES,
            MainActivity.EPSILON_DEGREES
        )

        val isTurnedRight = epsilonCheck(
            orientationDegrees,
            MainActivity.RIGHT_LANDSCAPE_DEGREES,
            MainActivity.EPSILON_DEGREES
        )

        return isTurnedLeft || isTurnedRight
    }

    private fun epsilonCheck(a: Int, b: Int, epsilon: Int): Boolean {
        return a > b - epsilon && a < b + epsilon
    }
}