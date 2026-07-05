import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private var isRecording = false

    @SuppressLint("MissingPermission") // Ensure RECORD_AUDIO permission is checked in Activity
    fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording = true
    }

    suspend fun stopAndGetAudioData(): FloatArray = withContext(Dispatchers.IO) {
        isRecording = false
        val shortBuffer = ShortArray(bufferSize)
        val audioData = mutableListOf<Float>()

        // Read lingering data in the buffer
        while (audioRecord?.read(shortBuffer, 0, bufferSize) ?: 0 > 0) {
            for (shortAudio in shortBuffer) {
                // Whisper expects float arrays normalized between -1.0 and 1.0
                audioData.add(shortAudio / 32768.0f)
            }
        }
        
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        return@withContext audioData.toFloatArray()
    }
}
