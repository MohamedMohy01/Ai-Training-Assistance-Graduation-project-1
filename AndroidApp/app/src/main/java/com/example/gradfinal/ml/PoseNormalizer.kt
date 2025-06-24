package com.example.gradfinal.ml

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.sqrt

class PoseNormalizer {

    fun normalize(landmarks: List<NormalizedLandmark>): List<Float> {
        if (landmarks.isEmpty()) {
            return List(99) { 0f }
        }

        // Get hip and shoulder landmarks
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftShoulder = landmarks[11]
        val rightShoulder = landmarks[12]

        // Calculate hip center
        val hipCenterX = (leftHip.x() + rightHip.x()) / 2f
        val hipCenterY = (leftHip.y() + rightHip.y()) / 2f
        val hipCenterZ = (leftHip.z() + rightHip.z()) / 2f

        // Calculate torso size for scaling
        val torsoSize = calculateDistance(leftShoulder, rightShoulder, leftHip, rightHip)

        // Normalize and flatten landmarks
        val normalizedFlattenedLandmarks = mutableListOf<Float>()
        landmarks.forEach { landmark ->
            normalizedFlattenedLandmarks.add((landmark.x() - hipCenterX) / torsoSize)
            normalizedFlattenedLandmarks.add((landmark.y() - hipCenterY) / torsoSize)
            normalizedFlattenedLandmarks.add((landmark.z() - hipCenterZ) / torsoSize)
        }

        return normalizedFlattenedLandmarks
    }

    private fun calculateDistance(
        shoulderLeft: NormalizedLandmark,
        shoulderRight: NormalizedLandmark,
        hipLeft: NormalizedLandmark,
        hipRight: NormalizedLandmark
    ): Float {
        val shoulderCenterX = (shoulderLeft.x() + shoulderRight.x()) / 2f
        val shoulderCenterY = (shoulderLeft.y() + shoulderRight.y()) / 2f
        val shoulderCenterZ = (shoulderLeft.z() + shoulderRight.z()) / 2f

        val hipCenterX = (hipLeft.x() + hipRight.x()) / 2f
        val hipCenterY = (hipLeft.y() + hipRight.y()) / 2f
        val hipCenterZ = (hipLeft.z() + hipRight.z()) / 2f

        val dx = shoulderCenterX - hipCenterX
        val dy = shoulderCenterY - hipCenterY
        val dz = shoulderCenterZ - hipCenterZ

        return sqrt(dx * dx + dy * dy + dz * dz)
    }
} 