package com.fredcodecrafts.moodlens.ml

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.fredcodecrafts.moodlens.ml.face.FaceDetectorHelper
import com.fredcodecrafts.moodlens.ml.utils.ImageUtils
import com.fredcodecrafts.moodlens.ml.classifier.EmotionClassifier
import com.google.mlkit.vision.common.InputImage

class FaceEmotionAnalyzer(
    private val classifier: EmotionClassifier,
    private val onPrediction: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceHelper = FaceDetectorHelper()

    override fun analyze(imageProxy: ImageProxy) {
        faceHelper.process(imageProxy) { faces, inputImage ->

            if (faces.isEmpty()) {
                onPrediction("No face detected")
                return@process
            }

            // Use the first face (single-face classifier)
            val faceBox = faces.first().boundingBox

            // Convert InputImage to Bitmap (ML Kit's internal function)
            val bitmap = inputImageToBitmap(inputImage) ?: run {
                onPrediction("Bitmap convert failed")
                return@process
            }

            // Crop face
            val faceBitmap = cropFace(bitmap, faceBox)

            // Resize to classifier input (48x48)
            val resized = Bitmap.createScaledBitmap(faceBitmap, 48, 48, true)

            // Convert to ByteBuffer (grayscale)
            val byteBuffer = ImageUtils.bitmapToGrayscaleByteBuffer(resized, 48, 48)

            // Run model
            val results = classifier.classifyImage(byteBuffer)

            // Pick highest confidence
            val top = results.maxByOrNull { it.confidence }
            val output = "${top?.label} (${top?.confidence?.times(100)?.toInt()}%)"

            onPrediction(output)
        }
    }

    /** Convert ML Kit InputImage → Bitmap */
    private fun inputImageToBitmap(inputImage: InputImage): Bitmap? {
        return try {
            val field = InputImage::class.java.getDeclaredField("bitmap")
            field.isAccessible = true
            field.get(inputImage) as? Bitmap
        } catch (e: Exception) {
            null
        }
    }

    /** Safely crop face from original frame */
    private fun cropFace(bitmap: Bitmap, box: Rect): Bitmap {
        val x = box.left.coerceAtLeast(0)
        val y = box.top.coerceAtLeast(0)
        val w = box.width().coerceAtMost(bitmap.width - x)
        val h = box.height().coerceAtMost(bitmap.height - y)
        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }
}
