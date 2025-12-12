package com.fredcodecrafts.moodlens.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import android.util.Log

class EmotionClassifier(context: Context) {

    private val interpreter: Interpreter
    private val inputHeight: Int
    private val inputWidth: Int
    private val inputChannels: Int
    private val numClasses: Int
    private val isFloatInput: Boolean
    
    // Emotions list - preserving this from original file for mapping
    val emotions = listOf(
        "angry",
        "disgust",
        "fear",
        "happy",
        "neutral",
        "sad",
        "surprise"
    )

    init {
        // Use existing model filename
        val modelFilename = "fer_cnn_classifier.tflite"
        val model = FileUtil.loadMappedFile(context, modelFilename)
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        // Read shape input & output
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()  // [1,H,W,C]
        
        // Log the shape for debugging
        Log.d("EmotionClassifier", "Input Shape: ${inputShape.joinToString()}")
        Log.d("EmotionClassifier", "Input Type: ${inputTensor.dataType()}")

        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
        inputChannels = inputShape[3]
        
        isFloatInput = inputTensor.dataType() == DataType.FLOAT32

        val outputTensor = interpreter.getOutputTensor(0)
        numClasses = outputTensor.shape()[1]  // [1,N]
        Log.d("EmotionClassifier", "Num Classes: $numClasses")
    }

    /**
     * Returns the index of the predicted emotion.
     * Returns -1 if an error occurs.
     */
    fun predict(bitmap: Bitmap): Int {
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

        if (isFloatInput) {
            return runFloatInference(resized)
        } else {
            return runUint8Inference(resized)
        }
    }

    private fun runUint8Inference(resized: Bitmap): Int {
        val input = Array(1) {
            Array(inputHeight) {
                Array(inputWidth) {
                    ByteArray(inputChannels)
                }
            }
        }

        for (y in 0 until inputHeight) {
            for (x in 0 until inputWidth) {
                val pixel = resized.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                if (inputChannels == 1) {
                    val gray = ((r + g + b) / 3).toByte()
                    input[0][y][x][0] = gray
                } else {
                    input[0][y][x][0] = r.toByte()
                    if (inputChannels > 1) input[0][y][x][1] = g.toByte()
                    if (inputChannels > 2) input[0][y][x][2] = b.toByte()
                }
            }
        }

        val output = Array(1) { ByteArray(numClasses) }
        interpreter.run(input, output)

        val probs = output[0].map { it.toInt() and 0xFF }
        return probs.indices.maxByOrNull { probs[it] } ?: -1
    }

    private fun runFloatInference(resized: Bitmap): Int {
        val input = Array(1) {
            Array(inputHeight) {
                Array(inputWidth) {
                    FloatArray(inputChannels)
                }
            }
        }

        for (y in 0 until inputHeight) {
            for (x in 0 until inputWidth) {
                val pixel = resized.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                val rNorm = r / 255.0f
                val gNorm = g / 255.0f
                val bNorm = b / 255.0f

                if (inputChannels == 1) {
                    val gray = (0.299f * rNorm + 0.587f * gNorm + 0.114f * bNorm)
                    input[0][y][x][0] = gray
                } else {
                    input[0][y][x][0] = rNorm
                    if (inputChannels > 1) input[0][y][x][1] = gNorm
                    if (inputChannels > 2) input[0][y][x][2] = bNorm
                }
            }
        }

        val output = Array(1) { FloatArray(numClasses) }
        interpreter.run(input, output)

        val probs = output[0]
        return probs.indices.maxByOrNull { probs[it] } ?: -1
    }

    fun getEmotionLabel(index: Int): String? {
        if (index in emotions.indices) {
            return emotions[index]
        }
        return null
    }

    fun close() {
        interpreter.close()
    }
}
