package com.example.gradfinal.ui.main

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.gradfinal.camera.CameraViewModel
import com.example.gradfinal.camera.PoseLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
internal fun MainScreen(viewModel: CameraViewModel) {
    val context = LocalContext.current
    val classificationResult by viewModel.classificationResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    var poseLandmarkerHelper: PoseLandmarkerHelper? by remember { mutableStateOf(null) }
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        viewModel.initialize(context)
        poseLandmarkerHelper = PoseLandmarkerHelper(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            listener = viewModel
        )
        onDispose {
            backgroundExecutor.shutdown()
            poseLandmarkerHelper?.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onFrame = { bitmap, rotation ->
                val imageProcessingOptions = ImageProcessingOptions.builder()
                    .setRotationDegrees(rotation)
                    .build()
                poseLandmarkerHelper?.detectLiveStream(bitmap, imageProcessingOptions)
            },
            executor = backgroundExecutor
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            classificationResult?.let { result ->
                Text(
                    text = "${result.exerciseName.replaceFirstChar { it.uppercase() }} (${String.format("%.2f", result.confidence)})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = { viewModel.toggleRecording() }) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = if (isRecording) Color.Red else Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error, style = TextStyle(color = Color.Red, fontSize = 18.sp))
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onFrame: (Bitmap, Int) -> Unit,
    executor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize()) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = CameraXPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(executor) { imageProxy ->
                        val bitmapBuffer = Bitmap.createBitmap(
                            imageProxy.width,
                            imageProxy.height,
                            Bitmap.Config.ARGB_8888
                        )
                        imageProxy.use {
                            imageProxy.planes[0].buffer.rewind()
                            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
                        }
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        onFrame(bitmapBuffer, rotation)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
} 