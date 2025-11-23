package com.fredcodecrafts.moodlens.ml.emotionPrediction

import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.collections.get
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.DataType

/**
 * Class that loads a quantized grayscale TFLite model and returns predictions.
 *
 * Assumes:
 *  - model file: assets/emotion_model_quantized.tflite
 *  - input shape: [1, 48, 48, 1] with UINT8 input (0..255)
 *  - output: either FLOAT32[1, numClasses] or UINT8[1, numClasses] (this code handles both)
 */
class EmotionClassifier(
    context: Context,
    modelFilename: String = "emotion_model_quantized.tflite",
    private val numThreads: Int = 4
) {
    private val interpreter: Interpreter
    private val TAG = "EmotionClassifier"

    init {
        val model = FileUtil.loadMappedFile(context, modelFilename)
        val options = Interpreter.Options().apply {
            setNumThreads(numThreads)
            // For quantized models NNAPI may be used — enable if you want:
            // setUseNNAPI(true)
        }
        interpreter = Interpreter(model, options)
        Log.d(TAG, "Interpreter created. input tensor count = ${interpreter.inputTensorCount}")
    }

    /**
     * Run classification. Input must be a direct ByteBuffer of size W*H (UINT8 bytes).
     * Returns top classes sorted by confidence (descending).
     */
    fun classify(input: ByteBuffer): List<PredictionResult> {
        // Prepare input (already uint8 bytes). TFLite expects the ByteBuffer in native order.
        input.order(ByteOrder.nativeOrder())

        // Prepare output container depending on output tensor type
        val outputTensor = interpreter.getOutputTensor(0)
        val outputDataType = outputTensor.dataType()

        val rawConfidencesFloat: FloatArray

        if (outputDataType == DataType.UINT8) {
            // Output is quantized uint8. Run into a byte array and dequantize.
            val numClasses = outputTensor.shape()[1]
            val outputRaw = Array(1) { ByteArray(numClasses) }
            interpreter.run(input, outputRaw)

            // get quantization params
            val qParams = outputTensor.quantizationParams()
            val scale = qParams.scale
            val zeroPoint = qParams.zeroPoint

            // dequantize
            rawConfidencesFloat = FloatArray(numClasses)
            for (i in 0 until numClasses) {
                val q = outputRaw[0][i].toInt() and 0xFF
                rawConfidencesFloat[i] = (q - zeroPoint) * scale
            }
        } else {
            // Output is float32 array
            val numClasses = outputTensor.shape()[1]
            rawConfidencesFloat = FloatArray(numClasses)
            val out = Array(1) { FloatArray(numClasses) }
            interpreter.run(input, out)
            System.arraycopy(out[0], 0, rawConfidencesFloat, 0, numClasses)
        }

        // Map confidences to labels and sort
        val labels = EmotionLabels.EMOTIONS
        val list = labels.mapIndexed { idx, label ->
            PredictionResult(label = label, confidence = if (idx < rawConfidencesFloat.size) rawConfidencesFloat[idx] else 0f)
        }.sortedByDescending { it.confidence }

        return list
    }

    fun close() {
        interpreter.close()
    }
}