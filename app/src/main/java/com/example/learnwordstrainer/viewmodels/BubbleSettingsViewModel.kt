import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnwordstrainer.service.BubbleService
import com.example.learnwordstrainer.viewmodels.BubbleSettingsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BubbleSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BubbleSettingsRepository(application)

    val uiState: StateFlow<BubbleSettingsUiState> = combine(
        repository.isBubbleEnabled,
        repository.bubbleSize,
        repository.bubbleTransparency,
        repository.isVibrationEnabled,
        repository.autoHideAppList
    ) { isEnabled, size, transparency, isVibrationOn, appList ->
        BubbleSettingsUiState(
            isBubbleEnabled = isEnabled,
            bubbleSize = size,
            bubbleTransparency = transparency,
            isVibrationEnabled = isVibrationOn,
            autoHideAppList = appList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BubbleSettingsUiState()
    )

    fun setBubbleEnabled(isEnabled: Boolean) = viewModelScope.launch {
        repository.setBubbleEnabled(isEnabled)
        if (isEnabled) {
            startBubbleService()
        } else {
            stopBubbleService()
        }
    }

    fun setBubbleSize(size: Float) = viewModelScope.launch {
        repository.setBubbleSize(size)
    }

    fun setBubbleTransparency(transparency: Float) = viewModelScope.launch {
        repository.setBubbleTransparency(transparency)
    }

    fun setVibrationEnabled(isEnabled: Boolean) = viewModelScope.launch {
        repository.setVibrationEnabled(isEnabled)
    }

    fun addAppToAutoHideList(packageName: String) = viewModelScope.launch {
        repository.addAppToAutoHideList(packageName)
    }

    fun removeAppFromAutoHideList(packageName: String) = viewModelScope.launch {
        repository.removeAppFromAutoHideList(packageName)
    }

    private fun startBubbleService() {
        try {
            val context = getApplication<Application>()
            val serviceIntent = Intent(context, BubbleService::class.java)
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e("BubbleSettingsVM", "Failed to start bubble service", e)
        }
    }

    private fun stopBubbleService() {
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, BubbleService::class.java)
        context.stopService(serviceIntent)
    }
}