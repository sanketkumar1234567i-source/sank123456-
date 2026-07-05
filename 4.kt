import android.content.Context
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BertIntentClassifier(private val context: Context) {
    private var classifier: BertNLClassifier? = null

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val options = BertNLClassifier.BertNLClassifierOptions.builder()
            .setBaseOptions(
                org.tensorflow.lite.task.core.BaseOptions.builder()
                    .useNnapi() // Use hardware acceleration if available
                    .build()
            )
            .build()
        
        // Model must include TFLite metadata for tokenization
        classifier = BertNLClassifier.createFromFileAndOptions(
            context, 
            "mobilebert_intent.tflite", 
            options
        )
    }

    suspend fun classifyIntent(text: String): String = withContext(Dispatchers.Default) {
        val nlc = classifier ?: throw IllegalStateException("Classifier not initialized")
        
        // Returns a list of categories with confidence scores
        val results = nlc.classify(text)
        
        // Find the intent with the highest probability
        val bestMatch = results.maxByOrNull { it.score }
        
        // Confidence threshold (e.g., 0.6)
        if (bestMatch != null && bestMatch.score > 0.6f) {
            return@withContext bestMatch.label
        } else {
            return@withContext "UNKNOWN_COMMAND"
        }
    }

    fun release() {
        classifier?.close()
    }
}
