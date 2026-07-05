dependencies {
    // TFLite Task Library handles tokenization and MobileBERT inference automatically
    implementation 'org.tensorflow:tensorflow-lite-task-text:0.4.0'
    implementation 'org.tensorflow:tensorflow-lite-gpu:2.11.0' // For GPU acceleration
    
    // Note: Whisper.cpp requires adding the whisper JNI bindings to your project.
    // See: https://github.com/ggerganov/whisper.cpp/tree/master/examples/whisper.android
    implementation project(':whisper') 
}
