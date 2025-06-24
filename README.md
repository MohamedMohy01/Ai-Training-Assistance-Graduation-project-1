# Graduation Project 1 (Exercise AI): AI-Powered Exercise Recognition

## Overview
Exercise AI is a graduation project solution to recognize and classify physical exercises in real-time using the device camera and on-device machine learning. The app leverages pose estimation and a custom-trained neural network to identify exercises such as sit-ups, jumping jacks, high knees, and steam engines, providing instant feedback to users.

## Features
- **Real-Time Exercise Recognition:** Uses the device camera and MediaPipe pose estimation to track body movements.
- **On-Device Machine Learning:** Classifies exercises using a TensorFlow Lite model trained on custom pose data.
- **User Authentication:** Simple login and registration screens for user access.
- **Intuitive UI:** Modern, clean interface built with Jetpack Compose.
- **Live Feedback:** Displays the detected exercise and confidence score during recording.

## Technology Stack
- **Android (Kotlin, Jetpack Compose)**
- **CameraX** for camera integration
- **MediaPipe** for pose landmark detection
- **TensorFlow Lite** for on-device ML inference
- **Custom LSTM Model** trained on pose sequences (see `GradProject_1_Notebook.ipynb` for training pipeline)

## How It Works
1. **Pose Detection:** The app uses MediaPipe to extract 33 body landmarks from the camera feed in real time.
2. **Pose Normalization:** Landmarks are normalized to be invariant to position and scale.
3. **Sequence Buffering:** A sequence of 30 frames is collected for each exercise attempt.
4. **Exercise Classification:** The sequence is fed into a TensorFlow Lite LSTM model to classify the exercise.
5. **Result Display:** The recognized exercise and confidence are shown to the user.

## Getting Started
### Prerequisites
- Android Studio (latest version recommended)
- Android device or emulator (Camera required for full functionality)

### Setup Instructions
1. **Clone the Repository:**
   ```
   git clone <repo-link>
   ```
2. **Open in Android Studio:**
   - Open the `AndroidApp` folder as a project.
3. **Build the Project:**
   - Let Gradle sync and download dependencies.
4. **Run the App:**
   - Connect an Android device or start an emulator with camera support.
   - Click 'Run' in Android Studio.

### Assets
- The app includes the following assets in `AndroidApp/app/src/main/assets/`:
  - `exercise_recognition_android_compatible.tflite`: The trained exercise classification model.
  - `pose_landmarker_lite.task`: MediaPipe pose landmark model.

## Model Training
- The model was trained using pose landmark data and an LSTM neural network. For details, see the `GradProject_1_Notebook.ipynb` notebook in the root directory.
- Achieved test accuracy: **~99.8%** on the validation set.

## Team Members
- **Ali Gaballah** - 120210005
- **Ahmed Aboelnaga** - 120210203
- **Yahia Ali** - 120210302
- **Mohamed Mohy** - 120210268
- **Mazen Seif** - 120210200

---
*This project was developed as part of the Graduation Project 1 course.*
