package com.example.gradfinal.ml

import android.content.Context
import android.util.Log
import com.example.gradfinal.data.ExerciseResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ExerciseClassifier(
    private val context: Context,
    private val modelPath: String = "exercise_recognition_android_compatible.tflite",
    private val poseNormalizer: PoseNormalizer = PoseNormalizer()
) {
    private var interpreter: Interpreter
    private val frameBuffer = mutableListOf<List<Float>>()
    private val frameSequenceLength = 30
    private var isRecording = false

    private val labels = listOf("high_knee", "jumping_jacks", "situps", "steam_engine")

    init {
        val options = Interpreter.Options()
        interpreter = Interpreter(loadModelFile(), options)
    }

    fun startRecording() {
        frameBuffer.clear()
        isRecording = true
    }

    fun addFrame(landmarks: List<NormalizedLandmark>) {
        if (!isRecording || landmarks.size < 33) return
        val normalizedData = poseNormalizer.normalize(landmarks)
        frameBuffer.add(normalizedData)
    }

    fun stopAndClassify(): ExerciseResult? {
        isRecording = false
        if (frameBuffer.size < frameSequenceLength) {
            Log.w("ExerciseClassifier", "Not enough frames to classify. Got ${frameBuffer.size}, needed $frameSequenceLength")
            return null // Not enough frames
        }

        // Use the last 30 frames for classification
        val sequence = frameBuffer.takeLast(frameSequenceLength)
        
        // Log the data being sent to the model
        logSequenceData(sequence)

        // Prepare input buffer
        val inputBuffer = ByteBuffer.allocateDirect(1 * frameSequenceLength * 99 * 4).apply {
            order(ByteOrder.nativeOrder())
            sequence.forEach { frame ->
                frame.forEach { value ->
                    putFloat(value)
                }
            }
        }
        inputBuffer.rewind()

        // Prepare output buffer
        val outputBuffer = ByteBuffer.allocateDirect(1 * labels.size * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        // Process output
        val probabilities = FloatArray(labels.size)
        outputBuffer.asFloatBuffer().get(probabilities)

        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        frameBuffer.clear()

        return if (maxIndex != -1) {
            ExerciseResult(labels[maxIndex], probabilities[maxIndex])
        } else {
            null
        }
    }

    private fun logSequenceData(sequence: List<List<Float>>) {
        val sequenceData = StringBuilder()
        sequenceData.append("--- Start of Classification Sequence ---\n")
        sequence.forEachIndexed { frameIndex, frameData ->
            sequenceData.append("Frame ${frameIndex + 1}: ${frameData.joinToString(", ")}\n")
        }
        sequenceData.append("--- End of Classification Sequence ---\n")
        Log.d("ExerciseClassifierData", sequenceData.toString())
    }

    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        interpreter.close()
    }
} 