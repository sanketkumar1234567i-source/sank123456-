import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.provider.AlarmClock
import android.widget.Toast

class ActionExecutor(private val context: Context) {

    fun execute(intentLabel: String, rawText: String) {
        when (intentLabel) {
            "TURN_ON_FLASHLIGHT" -> toggleFlashlight(true)
            "TURN_OFF_FLASHLIGHT" -> toggleFlashlight(false)
            "SET_ALARM" -> setAlarm()
            "OPEN_SETTINGS" -> openSettings()
            "UNKNOWN_COMMAND" -> showToast("Sorry, I didn't understand: $rawText")
            else -> showToast("Command recognized but not implemented: $intentLabel")
        }
    }

    private fun toggleFlashlight(status: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0] // Usually the rear camera
            cameraManager.setTorchMode(cameraId, status)
            showToast("Flashlight " + if(status) "ON" else "OFF")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAlarm() {
        // In a full implementation, you'd extract entities (time) from the rawText here using Regex or a QA BERT model
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "Voice Alarm")
            putExtra(AlarmClock.EXTRA_HOUR, 7)
            putExtra(AlarmClock.EXTRA_MINUTES, 30)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
