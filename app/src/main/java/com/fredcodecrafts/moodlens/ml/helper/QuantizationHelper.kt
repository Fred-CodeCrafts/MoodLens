package com.fredcodecrafts.moodlens.ml.helper

/**
 * Small helpers for quantization math if you want them available elsewhere.
 */
object QuantizationHelper {
    fun dequantize(qValue: Int, zeroPoint: Int, scale: Float): Float {
        return (qValue - zeroPoint) * scale
    }
}
