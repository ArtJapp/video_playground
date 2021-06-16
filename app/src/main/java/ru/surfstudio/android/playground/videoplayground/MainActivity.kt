package ru.surfstudio.android.playground.videoplayground

import android.content.pm.ActivityInfo.*
import android.content.res.Configuration
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.View.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import ru.surfstudio.android.playground.videoplayground.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var orientationEventListener: OrientationEventListener

    private var isFullMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)

        initListeners()
        configurePlayer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isFullMode = !isFullMode
        onFullModeChanged(isFullMode)
    }

    private fun initListeners() {
        initOrientationListener()
        findViewById<ImageView>(R.id.full_screen).setOnClickListener {
            isFullMode = !isFullMode
            if (isFullMode) {
                doPortraitMode()
            } else {
                doFullMode()
            }
        }
    }

    private fun initOrientationListener() {
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val epsilon = EPSILON_DEGREES
                val leftLandscape = LEFT_LANDSCAPE_DEGREES
                val rightLandscape = RIGHT_LANDSCAPE_DEGREES
                if (epsilonCheck(orientation, leftLandscape, epsilon) ||
                    epsilonCheck(orientation, rightLandscape, epsilon)
                ) {
                    this@MainActivity.requestedOrientation = SCREEN_ORIENTATION_SENSOR // возможна утечка
                }
            }

            private fun epsilonCheck(a: Int, b: Int, epsilon: Int): Boolean {
                return a > b - epsilon && a < b + epsilon
            }
        }
        orientationEventListener.enable()
    }

    private fun configurePlayer() {
        val player = SimpleExoPlayer.Builder(this)
            .build()
        player.setMediaItem(MediaItem.fromUri(EXAMPLE_M3U8_URL))
        binding.contentView.player = player
    }

    private fun doFullMode() {
        requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
    }

    private fun doPortraitMode() {
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    }

    private fun onFullModeChanged(isFullMode: Boolean) {
        if (isFullMode) {
            binding.contentView.systemUiVisibility = FULL_MODE_FLAG
            supportActionBar?.hide()
        } else {
            binding.contentView.systemUiVisibility = PORTRAIT_MODE_FLAG
            supportActionBar?.show()
        }
    }

    private companion object {
        const val FULL_MODE_FLAG = SYSTEM_UI_FLAG_IMMERSIVE_STICKY or SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        const val PORTRAIT_MODE_FLAG = SYSTEM_UI_FLAG_VISIBLE

        const val EPSILON_DEGREES = 10
        const val LEFT_LANDSCAPE_DEGREES = 90
        const val RIGHT_LANDSCAPE_DEGREES = 270

        const val EXAMPLE_M3U8_URL = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8"
    }
}