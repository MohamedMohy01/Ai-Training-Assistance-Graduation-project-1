package com.example.gradfinal.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradfinal.data.ExerciseResult
import com.example.gradfinal.ml.ExerciseClassifier
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel(), PoseLandmarkerHelper.LandmarkerListener {

    private val _classificationResult = MutableStateFlow<ExerciseResult?>(null)
    val classificationResult = _classificationResult.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var exerciseClassifier: ExerciseClassifier? = null

    fun initialize(context: Context) {
        if (exerciseClassifier == null) {
            viewModelScope.launch {
                try {
                    exerciseClassifier = ExerciseClassifier(context)
                } catch (e: Exception) {
                    _errorMessage.value = "Classifier initialization failed: ${e.message}"
                }
            }
        }
    }

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
        if (_isRecording.value) {
            exerciseClassifier?.startRecording()
        } else {
            val result = exerciseClassifier?.stopAndClassify()
            _classificationResult.value = result
        }
    }

    override fun onError(error: String) {
        _errorMessage.value = error
    }

    override fun onResults(landmarks: List<NormalizedLandmark>, inferenceTime: Long) {
        if (_isRecording.value) {
            exerciseClassifier?.addFrame(landmarks)
        }
    }

    override fun onCleared() {
        super.onCleared()
        exerciseClassifier?.close()
    }
} 