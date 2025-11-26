package com.fredcodecrafts.moodlens.ml.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.fredcodecrafts.moodlens.ml.emotionPrediction.EmotionClassifier
import com.fredcodecrafts.moodlens.ml.emotionPrediction.PredictionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ImageAnalysis.Analyzer that extracts grayscale bytes and runs EmotionClassifier.
 *
 * Usage:
 *   val analyzer = ImageAnalyzer(context, classifier) { results -> /* update UI */ }
 *   imageAnalysis.setAnalyzer(executor, analyzer)
 */
class ImageAnalyzer(
    private val classifier: EmotionClassifier,
    private val inputWidth: Int = 48,
    private val inputHeight: Int = 48,
    private val onResults: (List<PredictionResult>) -> Unit
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun analyze(image: ImageProxy) {
        // Run conversion & inference off the main thread
        scope.launch {
            try {
                val bb = ImageUtils.yPlaneToGrayscaleByteBuffer(image, inputWidth, inputHeight, quantized = true)
                val results = classifier.classify(bb)
                withContext(Dispatchers.Main) {
                    onResults(results)
                }
            } catch (e: Exception) {
                // swallow or log — keep analyzer resilient
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }
}
