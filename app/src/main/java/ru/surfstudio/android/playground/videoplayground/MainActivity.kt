package ru.surfstudio.android.playground.videoplayground

import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.util.Rational
import android.view.OrientationEventListener
import android.view.View.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import ru.surfstudio.android.playground.videoplayground.databinding.ActivityMainBinding
import ru.surfstudio.android.playground.videoplayground.pip.PipActionsReceiver
import ru.surfstudio.android.playground.videoplayground.pip.PipSupporting
import ru.surfstudio.android.playground.videoplayground.util.isPictureInPictureAvailable
import ru.surfstudio.android.playground.videoplayground.util.normalizeAspectRatio

class MainActivity : AppCompatActivity(), PipSupporting, ControllableView {

    private var isFullMode = false
    private var videoPlaybackListener: VideoPlaybackListener? = null
    private var orientationEventListener: OrientationEventListener? = null

    private var isShowingTrackSelectionDialog = false
    private lateinit var player: SimpleExoPlayer
    private var trackSelector: DefaultTrackSelector? = null

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override var pictureInPictureBuilder = PictureInPictureParams.Builder()

    private val pipActionsReceiver = PipActionsReceiver(
        onPlay = {
            binding.contentView.player?.play()
            videoPlaybackListener?.onVideoStarted()
            hideControls()
        },
        onPause = {
            binding.contentView.player?.pause()
            videoPlaybackListener?.onVideoStopped()
            hideControls()
        }
    )

    private val pipActionsOldReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                if (intent.action != ACTION_MEDIA_CONTROL) {
                    return
                }

                when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                    PLAY_CONTROL_TYPE -> {
                        binding.contentView.player?.play()
                        videoPlaybackListener?.onVideoStarted()
                        hideControls()
                    }
                    PAUSE_CONTROL_TYPE -> {
                        binding.contentView.player?.pause()
                        videoPlaybackListener?.onVideoStopped()
                        hideControls()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)

        findViewById<ImageView>(R.id.start_picture_in_picture)?.isVisible =
            isPictureInPictureAvailable()

        initListeners()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configurePictureInPictureActions(this)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.contentView.player = configurePlayer()
    }

    private fun configurePlayer(): SimpleExoPlayer {
        trackSelector = DefaultTrackSelector(this)
        trackSelector?.currentMappedTrackInfo?.getTrackGroups(2)
            ?.let { group ->
                (0 until group.length).forEach {
                Log.d("AAA", "subtitle ${group[it]}")
                }
            }
        player = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector!!)
            .build()
        player.setMediaItem(MediaItem.fromUri(EXAMPLE_M3U8_URL))
        val mediaSession = MediaSessionCompat(this, packageName)
        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(player)
        mediaSession.isActive = true
        return player
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode) {
            showControls()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode)) {
            isFullMode = !isFullMode
            onFullModeChanged(isFullMode)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        onFullModeChanged(isInPictureInPictureMode)
        binding.contentView.setControllerVisibilityListener {
            if (isInPictureInPictureMode && it == VISIBLE) {
                binding.contentView.hideController()
            }
        }

        if (isInPictureInPictureMode) {
            registerReceiver(pipActionsReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
            isFullMode = true
        } else {
            unregisterReceiver(pipActionsReceiver)
            // Show the video controls if the video is not playing
            if (binding.contentView.player?.isPlaying == false) {
                binding.contentView.showController()
            }
            showControls()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            binding.contentView.player?.play()
            videoPlaybackListener?.onVideoStarted()
        } else {
            binding.contentView.player?.pause()
            videoPlaybackListener?.onVideoStopped()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    protected fun releasePlayer() {
            player.release()
            trackSelector = null
    }

    override fun onDestroy() {
        orientationEventListener?.disable()
        orientationEventListener = null
        videoPlaybackListener?.onVideoStopped()
        videoPlaybackListener = null
        super.onDestroy()
    }

    private fun initListeners() {
        initOrientationListener()
        findViewById<ImageView>(R.id.go_full_screen).setOnClickListener {
            onFullScreenClicked()
        }
        findViewById<ImageView>(R.id.start_picture_in_picture).setOnClickListener {
            minimize()
        }

        findViewById<ImageView>(R.id.quality_button).setOnClickListener {
            if (!isShowingTrackSelectionDialog
                && TrackSelectionDialog.willHaveContent(trackSelector!!)
            ) {
                isShowingTrackSelectionDialog = true
                val trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(
                    trackSelector!!
                )  /* onDismissListener= */
                { dismissedDialog -> isShowingTrackSelectionDialog = false }
                trackSelectionDialog.show(supportFragmentManager,  /* tag= */null)
            }
        }
    }

    private fun initOrientationListener() {
        orientationEventListener = OrientationChangedListener(this)
        orientationEventListener?.enable()
    }

    private fun onFullScreenClicked() {
        isFullMode = !isFullMode
        if (isFullMode) {
            doPortraitMode()
        } else {
            doFullMode()
        }
    }

    override fun minimize() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        hideControls()
        // если контент слишком широкий или высокий, то он может не минимизироваться,
        // тк слишком крупные пропорции
        val aspectRatio = normalizeAspectRatio(
            Rational(
                binding.contentView.width,
                binding.contentView.height
            )
        )
        pictureInPictureBuilder.setAspectRatio(aspectRatio)
        enterPictureInPictureMode(pictureInPictureBuilder.build())
    }

    override fun hideControls() {
        findViewById<ImageView>(R.id.go_full_screen).isVisible = false
        findViewById<ImageView>(R.id.start_picture_in_picture).isVisible = false
        binding.contentView.hideController()
    }

    override fun showControls() {
        findViewById<ImageView>(R.id.go_full_screen).isVisible = true
        findViewById<ImageButton>(R.id.start_picture_in_picture).isVisible = true
        binding.contentView.showController()
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
            // тут нужно будет скрывать прочий контент экрана
        } else {
            binding.contentView.systemUiVisibility = PORTRAIT_MODE_FLAG
            supportActionBar?.show()
            // тут нужно будет вернуть видимость контента экрана
        }
    }

    companion object {
        const val FULL_MODE_FLAG =
            SYSTEM_UI_FLAG_IMMERSIVE_STICKY or SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        const val PORTRAIT_MODE_FLAG = SYSTEM_UI_FLAG_VISIBLE

        const val ACTION_MEDIA_CONTROL = "media_control"
        const val EXTRA_CONTROL_TYPE = "control_type"
        const val PAUSE_REQUEST_CODE = 1
        const val PAUSE_CONTROL_TYPE = 1
        const val PLAY_REQUEST_CODE = 2
        const val PLAY_CONTROL_TYPE = 2

        const val EPSILON_DEGREES = 10
        const val LEFT_LANDSCAPE_DEGREES = 90
        const val RIGHT_LANDSCAPE_DEGREES = 270

        const val EXAMPLE_M3U8_URL = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/master.m3u8"

        // если наскучит верхний видос
        const val ANOTHER_EXAMPLE_M3U8_URL =
            "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
    }
}