import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.whispercpp.whisper.WhisperContext // From whisper.cpp android bindings

class WhisperTranscriber(private val context: Context) {
    private var whisperCtx: WhisperContext? = null

    suspend fun initialize() = withContext(Dispatchers.IO) {
        // Load the tiny quantized model from assets to internal storage, then initialize
        val modelFile = FileUtils.copyAssetToInternalStorage(context, "ggml-tiny.en.bin")
        whisperCtx = WhisperContext.createContext(modelFile.absolutePath)
    }

    suspend fun transcribe(audioData: FloatArray): String = withContext(Dispatchers.Default) {
        val ctx = whisperCtx ?: throw IllegalStateException("Whisper not initialized")
        
        // Run inference natively via C++
        val result = ctx.transcribeData(audioData)
        return@withContext result.trim()
    }

    fun release() {
        whisperCtx?.release()
    }
}
