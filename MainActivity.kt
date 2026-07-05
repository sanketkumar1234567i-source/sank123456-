import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var whisperTranscriber: WhisperTranscriber
    private lateinit var intentClassifier: BertIntentClassifier
    private lateinit var actionExecutor: ActionExecutor

    private lateinit var btnRecord: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRecord = findViewById(R.id.btnRecord)
        tvStatus = findViewById(R.id.tvStatus)

        // Initialize modules
        audioRecorder = AudioRecorder()
        whisperTranscriber = WhisperTranscriber(this)
        intentClassifier = BertIntentClassifier(this)
        actionExecutor = ActionExecutor(this)

        requestPermissions()

        // Load ML Models asynchronously
        lifecycleScope.launch {
            tvStatus.text = "Loading ML Models natively..."
            whisperTranscriber.initialize()
            intentClassifier.initialize()
            tvStatus.text = "Ready. Hold button to speak."
        }

        setupRecordingButton()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecordingButton() {
        btnRecord.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    tvStatus.text = "Listening..."
                    audioRecorder.startRecording()
                }
                android.view.MotionEvent.ACTION_UP -> {
                    tvStatus.text = "Processing..."
                    processAudio()
                }
            }
            true
        }
    }

    private fun processAudio() {
        lifecycleScope.launch {
            try {
                // 1. Get raw audio data
                val audioData = audioRecorder.stopAndGetAudioData()

                // 2. Transcribe Audio (Whisper)
                tvStatus.text = "Transcribing (Whisper)..."
                val transcription = whisperTranscriber.transcribe(audioData)

                if (transcription.isBlank()) {
                    tvStatus.text = "No speech detected."
                    return@launch
                }

                // 3. Understand Intent (MobileBERT)
                tvStatus.text = "Understanding (BERT)..."
                val intent = intentClassifier.classifyIntent(transcription)

                // 4. Execute Native Action
                tvStatus.text = "Command: $transcription\nIntent: $intent"
                actionExecutor.execute(intent, transcription)

            } catch (e: Exception) {
                tvStatus.text = "Error: ${e.message}"
            }
        }
    }

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        whisperTranscriber.release()
        intentClassifier.release()
    }
}
