package ru.surfstudio.android.playground.videoplayground.util

import android.util.Rational

const val PIP_RATIO_CONTENT_GUIDELINE = 2.39

/**
 * Нормализация разрешения картинки
 *
 * Если картинка слишком высокая/широкая,
 * режим картинка-в-картинке не сможет нормально
 * проигрывать видео, поэтому подгоняем
 * под допустимые размеры
 */
fun normalizeAspectRatio(ratio: Rational, guideline: Double = PIP_RATIO_CONTENT_GUIDELINE): Rational {
    val ratioNumeric = ratio.toDouble()
    return when {
        ratioNumeric < 1 / guideline -> {
            // контент слишком высокий
            val h = ratio.numerator * guideline
            Rational(ratio.numerator, h.toInt())
        }
        ratioNumeric > guideline -> {
            // контент слишком широкий
            val h = ratio.numerator / guideline
            Rational(ratio.numerator, h.toInt())
        }
        else -> ratio
    }
}
