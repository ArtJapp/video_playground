package ru.surfstudio.android.playground.videoplayground.lean_back

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import ru.surfstudio.android.playground.videoplayground.databinding.ActivityLeanBackBinding

class LeanBackPictureActivityView : AppCompatActivity() {

    private val binding: ActivityLeanBackBinding by lazy {
        ActivityLeanBackBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)

        binding.fullTv.setOnClickListener {
            binding.contentIv.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val isFullMode = visibility and SYSTEM_UI_FLAG_FULLSCREEN != 0
            binding.fullTv.isVisible = !isFullMode



//            val rotationDegree = when {
//                isFullMode -> 90f
//                else -> 0f
//            }
//            binding.contentIv.rotation = rotationDegree
        }
    }
}