import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun createMoodBubble(context: Context, mood: String, backgroundColor: Float): BitmapDescriptor {
    // 1. Config: Set the size of the bubble
    val size = 120 // px (Adjust this for "Larger" or "Smaller" bubbles)
    val fontSize = 60f // px (Size of the emoji)

    // 2. Create a blank bitmap
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 3. Setup Paint for the Bubble Background
    val paintCircle = Paint().apply {
        color = getMoodColorInt(backgroundColor) // Helper to convert HUE to ARGB Color
        style = Paint.Style.FILL
        isAntiAlias = true
        // Optional: Add a shadow to make it look 3D/Spherical
        setShadowLayer(10f, 0f, 5f, Color.GRAY)
    }

    // 4. Draw the Circle (The Bubble)
    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius - 5, paintCircle)

    // 5. Determine the Emoji based on the Mood String
    val emoji = getEmojiForMood(mood)

    // 6. Setup Paint for the Emoji Text
    val paintText = Paint().apply {
        color = Color.WHITE // Or Color.BLACK depending on contrast
        textSize = fontSize
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    // 7. Calculate vertical center for text (Text drawing baseline is tricky)
    val bounds = Rect()
    paintText.getTextBounds(emoji, 0, emoji.length, bounds)
    val yOffset = (bounds.bottom - bounds.top) / 2f
    val yPosition = radius + (yOffset / 2) // Approximate vertical center

    // 8. Draw the Emoji
    canvas.drawText(emoji, radius, yPosition + 15, paintText)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// Helper to map your string mood to an actual Emoji
fun getEmojiForMood(mood: String): String {
    return when (mood.lowercase()) {
        "happy" -> "😊"
        "sad" -> "😢"
        "angry" -> "😡"
        "neutral" -> "😐"
        "excited" -> "🤩"
        else -> "📍" // Default fallback
    }
}

// Helper to convert the Google Maps HUE (Float) to an Int Color for the Canvas
fun getMoodColorInt(hue: Float): Int {
    return Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
}