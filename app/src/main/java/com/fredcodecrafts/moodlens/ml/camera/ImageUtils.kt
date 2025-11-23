package com.fredcodecrafts.moodlens.ml.camera

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

/**
 * Utilities to convert ImageProxy (YUV_420_888) -> ByteBuffer expected by quantized grayscale model.
 *
 * This implementation:
 *  - reads the Y (luma) plane only (grayscale)
 *  - center-crops to square
 *  - nearest-neighbor resamples to targetWidth x targetHeight
 *  - outputs a direct ByteBuffer filled with unsigned bytes (0..255)
 *
 * NO Bitmap allocations; fast and low allocation.
 */
object ImageUtils {

    /**
     * Convert ImageProxy to a ByteBuffer [targetWidth x targetHeight] with raw bytes (0..255).
     *
     * @param image ImageProxy (will NOT be closed here)
     * @param targetWidth desired width (48)
     * @param targetHeight desired height (48)
     * @param quantized if true returns uint8 bytes, if false returns float32 bytes (not used here)
     */
    fun yPlaneToGrayscaleByteBuffer(
        image: ImageProxy,
        targetWidth: Int,
        targetHeight: Int,
        quantized: Boolean = true
    ): ByteBuffer {
        val width = image.width
        val height = image.height

        // Read Y-plane
        val yPlane = image.planes[0].buffer
        val yBytes = ByteArray(yPlane.remaining())
        yPlane.get(yBytes)

        // center-crop to square
        val cropSize = min(width, height)
        val cropX = (width - cropSize) / 2
        val cropY = (height - cropSize) / 2

        // prepare ByteBuffer for uint8 bytes
        val byteBuffer = ByteBuffer.allocateDirect(targetWidth * targetHeight)
        byteBuffer.order(ByteOrder.nativeOrder())

        // nearest neighbor sampling from cropped square -> target size
        for (ty in 0 until targetHeight) {
            val sy = cropY + (ty * cropSize) / targetHeight
            val srcRowStart = sy * width
            for (tx in 0 until targetWidth) {
                val sx = cropX + (tx * cropSize) / targetWidth
                val srcIndex = srcRowStart + sx
                // safety check
                val v = if (srcIndex in yBytes.indices) (yBytes[srcIndex].toInt() and 0xFF) else 0
                byteBuffer.put((v and 0xFF).toByte())
            }
        }
        byteBuffer.rewind()
        return byteBuffer
    }
}
