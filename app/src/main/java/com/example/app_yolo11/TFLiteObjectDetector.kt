package com.example.app_yolo11

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.max
import kotlin.math.min

class TFLiteObjectDetector(private val context: Context) {

    private var interpreter: Interpreter
    private val inputSize = 640
    private val numClasses = 31

    init {
        val modelBuffer = FileUtil.loadMappedFile(context, "model16.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            useXNNPACK = true
        }
        interpreter = Interpreter(modelBuffer, options)

        Log.d("TFLite", "YOLO model loaded successfully")
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)


        val input = Array(1) {
            Array(inputSize) {
                Array(inputSize) { FloatArray(3) }
            }
        }

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val px = resized.getPixel(x, y)
                input[0][y][x][0] = Color.red(px) / 255f
                input[0][y][x][1] = Color.green(px) / 255f
                input[0][y][x][2] = Color.blue(px) / 255f
            }
        }


        val outputTensor = interpreter.getOutputTensor(0)
        val shape = outputTensor.shape()
        Log.d("TFLite", "Output shape = ${shape.joinToString()}")

        val c = shape[1]
        val b = shape[2]


        val rawOutput = Array(1) { Array(c) { FloatArray(b) } }

        interpreter.run(input, rawOutput)

        val yoloOutput = transpose(rawOutput)

        return decodeYOLO(yoloOutput[0])
    }


    private fun transpose(data: Array<Array<FloatArray>>): Array<Array<FloatArray>> {

        val C = data[0].size
        val B = data[0][0].size

        val result = Array(1) { Array(B) { FloatArray(C) } }

        for (c in 0 until C) {
            for (b in 0 until B) {
                result[0][b][c] = data[0][c][b]
            }
        }
        return result
    }

    private fun decodeYOLO(outputs: Array<FloatArray>): List<DetectionResult> {

        val results = mutableListOf<DetectionResult>()

        for (i in outputs.indices) {
            val row = outputs[i]

            val x = row[0]
            val y = row[1]
            val w = row[2]
            val h = row[3]

            val classScores = row.copyOfRange(4, 4 + numClasses)
            val bestClass = classScores.indices.maxByOrNull { classScores[it] } ?: continue
            val confidence = classScores[bestClass]

            if (confidence > 0.25f) {
                results.add(
                    DetectionResult(
                        label = "Class $bestClass",
                        score = confidence,
                        boundingBox = RectF(
                            x - w / 2,
                            y - h / 2,
                            x + w / 2,
                            y + h / 2
                        )
                    )
                )
            }
        }

        return nonMaxSuppression(results, 0.5f)
    }


    private fun nonMaxSuppression(
        detections: List<DetectionResult>,
        iouThreshold: Float
    ): List<DetectionResult> {

        val finalDetections = mutableListOf<DetectionResult>()
        val sorted = detections.sortedByDescending { it.score }.toMutableList()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            finalDetections.add(best)

            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val other = iterator.next()
                if (calculateIOU(best.boundingBox, other.boundingBox) > iouThreshold) {
                    iterator.remove()
                }
            }
        }
        return finalDetections
    }

    private fun calculateIOU(a: RectF, b: RectF): Float {
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)

        if (areaA <= 0 || areaB <= 0) return 0f

        val intersection =
            max(0f, min(a.right, b.right) - max(a.left, b.left)) *
                    max(0f, min(a.bottom, b.bottom) - max(a.top, b.top))

        return intersection / (areaA + areaB - intersection)
    }

    data class DetectionResult(
        val label: String,
        val score: Float,
        val boundingBox: RectF
    )
}
