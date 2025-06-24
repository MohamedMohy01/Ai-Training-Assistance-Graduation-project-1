package com.example.gradfinal.camera

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions

class PoseLandmarkerHelper(
    val context: Context,
    val runningMode: RunningMode = RunningMode.LIVE_STREAM,
    val listener: LandmarkerListener
) {

    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")

            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setRunningMode(runningMode)
                .setNumPoses(1) // Assuming one person for exercise detection

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult)
                optionsBuilder.setErrorListener(this::returnLivestreamError)
            }

            poseLandmarker = PoseLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            listener.onError("Pose Landmarker initialization failed: ${e.message}")
            Log.e(TAG, "MediaPipe failed to load the model", e)
        }
    }

    fun detectLiveStream(bitmap: Bitmap, imageProcessingOptions: ImageProcessingOptions) {
        val frameTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker?.detectAsync(mpImage, frameTime)
    }

    private fun returnLivestreamResult(result: PoseLandmarkerResult, input: MPImage) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()
        val landmarks = result.landmarks().firstOrNull() ?: emptyList()
        listener.onResults(landmarks, inferenceTime)
    }

    private fun returnLivestreamError(error: RuntimeException) {
        listener.onError(error.message ?: "An unknown error has occurred")
    }

    fun close() {
        poseLandmarker?.close()
    }

    interface LandmarkerListener {
        fun onError(error: String)
        fun onResults(landmarks: List<NormalizedLandmark>, inferenceTime: Long)
    }

    companion object {
        private const val TAG = "PoseLandmarkerHelper"
    }
} 