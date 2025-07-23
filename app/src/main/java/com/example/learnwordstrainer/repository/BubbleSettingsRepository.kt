import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.learnwordstrainer.model.BubblePosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "bubble_settings_main")

class BubbleSettingsRepository(private val context: Context) {

    private object Keys {
        val BUBBLE_X = intPreferencesKey("bubble_x")
        val BUBBLE_Y = intPreferencesKey("bubble_y")
        val IS_BUBBLE_ENABLED = booleanPreferencesKey("is_bubble_enabled")
        val BUBBLE_SIZE = floatPreferencesKey("bubble_size")
        val BUBBLE_TRANSPARENCY = floatPreferencesKey("bubble_transparency")
        val IS_VIBRATION_ENABLED = booleanPreferencesKey("is_vibration_enabled")
        val AUTO_HIDE_APP_LIST = stringSetPreferencesKey("auto_hide_app_list")
    }

    companion object {
        val DEFAULT_POSITION = BubblePosition(20, 100)
        const val DEFAULT_IS_ENABLED = true
        const val DEFAULT_SIZE = 40f
        const val DEFAULT_TRANSPARENCY = 100f // 0-100%
        const val DEFAULT_IS_VIBRATION_ENABLED = true
        val DEFAULT_AUTO_HIDE_APP_LIST = emptySet<String>()
    }

    val position: Flow<BubblePosition> = context.settingsDataStore.data.map { prefs ->
        val x = prefs[Keys.BUBBLE_X] ?: DEFAULT_POSITION.x
        val y = prefs[Keys.BUBBLE_Y] ?: DEFAULT_POSITION.y
        BubblePosition(x, y)
    }

    val isBubbleEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.IS_BUBBLE_ENABLED] ?: DEFAULT_IS_ENABLED
    }

    val bubbleSize: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.BUBBLE_SIZE] ?: DEFAULT_SIZE
    }

    val bubbleTransparency: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.BUBBLE_TRANSPARENCY] ?: DEFAULT_TRANSPARENCY
    }

    val isVibrationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.IS_VIBRATION_ENABLED] ?: DEFAULT_IS_VIBRATION_ENABLED
    }

    val autoHideAppList: Flow<Set<String>> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.AUTO_HIDE_APP_LIST] ?: DEFAULT_AUTO_HIDE_APP_LIST
    }

    suspend fun setBubbleEnabled(isEnabled: Boolean) = context.settingsDataStore.edit { prefs ->
        prefs[Keys.IS_BUBBLE_ENABLED] = isEnabled
    }

    suspend fun setBubbleSize(size: Float) = context.settingsDataStore.edit { prefs ->
        prefs[Keys.BUBBLE_SIZE] = size
    }

    suspend fun setBubbleTransparency(transparency: Float) = context.settingsDataStore.edit { prefs ->
        prefs[Keys.BUBBLE_TRANSPARENCY] = transparency
    }

    suspend fun setVibrationEnabled(isEnabled: Boolean) = context.settingsDataStore.edit { prefs ->
        prefs[Keys.IS_VIBRATION_ENABLED] = isEnabled
    }

    suspend fun addAppToAutoHideList(packageName: String) = context.settingsDataStore.edit { prefs ->
        val currentList = prefs[Keys.AUTO_HIDE_APP_LIST] ?: emptySet()
        prefs[Keys.AUTO_HIDE_APP_LIST] = currentList + packageName
    }

    suspend fun removeAppFromAutoHideList(packageName: String) = context.settingsDataStore.edit { prefs ->
        val currentList = prefs[Keys.AUTO_HIDE_APP_LIST] ?: emptySet()
        prefs[Keys.AUTO_HIDE_APP_LIST] = currentList - packageName
    }

    suspend fun savePosition(x: Int, y: Int) = context.settingsDataStore.edit { prefs ->
        prefs[Keys.BUBBLE_X] = x
        prefs[Keys.BUBBLE_Y] = y
    }
}