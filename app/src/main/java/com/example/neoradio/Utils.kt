package com.example.neoradio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

suspend fun delayUntilTime(targetTimeString: String, additionalMs: Long) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
    val targetDate = sdf.parse(targetTimeString) ?: return

    val targetCalendar = Calendar.getInstance().apply {
        val parsedCal = Calendar.getInstance().apply { time = targetDate }
        set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
        set(Calendar.SECOND, parsedCal.get(Calendar.SECOND))
        set(Calendar.MILLISECOND, 0)
    }

    val now = Calendar.getInstance()

    if (now.after(targetCalendar)) {
        targetCalendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    val delayMillis = targetCalendar.timeInMillis - now.timeInMillis
    delay((delayMillis + additionalMs).milliseconds)
}

@Composable
fun rememberDominantColor(
    imageUrl: String?,
    defaultColor: Color = Color.Transparent
): State<Color> {
    val context = LocalContext.current

    return produceState(initialValue = defaultColor, key1 = imageUrl) {
        if (imageUrl.isNullOrBlank()) {
            value = defaultColor
            return@produceState
        }

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .size(64)
            .build()

        val result = ImageLoader(context).execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            val palette = Palette.from(bitmap).generate()
            val colorInt = palette.getDominantColor(defaultColor.toArgb())
            value = Color(colorInt)
        }
    }
}

fun String.unescapeXML() = this
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&apos;", "'")
    .replace("&amp;", "&")