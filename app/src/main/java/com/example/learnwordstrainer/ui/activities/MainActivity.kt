package com.example.learnwordstrainer.ui.activities

import BubbleSettingsRepository
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.learnwordstrainer.R
import com.example.learnwordstrainer.service.BubbleService
import com.example.learnwordstrainer.service.NotificationService
import com.example.learnwordstrainer.ui.compose.MainScreen
import com.example.learnwordstrainer.ui.theme.LearnWordsTrainerTheme
import com.example.learnwordstrainer.utils.NotificationHelper
import com.example.learnwordstrainer.viewmodels.MainViewModel
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateTheme()


        setContent {
            val currentTheme by viewModel.themeMode.collectAsState()
            LearnWordsTrainerTheme(
                themeMode = currentTheme
            ) {
                val context = LocalContext.current

                val overlayPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    if (Settings.canDrawOverlays(context)) {
                        startBubbleServiceWithPermission()
                    } else {
                        Toast.makeText(context, "Потрібен дозвіл для показу бульбашки", Toast.LENGTH_SHORT).show()
                    }
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "Сповіщення увімкнено", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Сповіщення вимкнено. Ви можете увімкнути їх у налаштуваннях", Toast.LENGTH_LONG).show()
                    }
                    startNotificationServiceWithPermission()
                }

                LaunchedEffect(key1 = Unit) {
                    viewModel.loadStatistics()
                    startNotificationService {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    startBubbleService { intent ->
                        overlayPermissionLauncher.launch(intent)
                    }
                }

                MainScreen(
                    viewModel = viewModel,
                    onSettingsClick = {
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    },
                    onAddWordClick = {
                        val intent = Intent(this@MainActivity, AddWordActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    },
                    onRepetitionClick = {
                        val intent = Intent(this@MainActivity, RepetitionActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    },
                    onAllWordsClick = {
                        //todo: all words screen
                    },
                    onPracticeClick = {
                        val intent = Intent(this@MainActivity, PracticeActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStatistics()
        viewModel.updateTheme()
    }

    private fun startNotificationService(onPermissionNeeded: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                onPermissionNeeded()
            } else {
                startNotificationServiceWithPermission()
            }
        } else {
            startNotificationServiceWithPermission()
        }
    }

    private fun startNotificationServiceWithPermission() {
        NotificationHelper.createNotificationChannel(this)
        val intent = Intent(this, NotificationService::class.java)
        startForegroundService(intent)
    }

    private fun startBubbleService(onPermissionNeeded: (Intent) -> Unit) {
        if (Settings.canDrawOverlays(this)) {
            startBubbleServiceWithPermission()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri())
            onPermissionNeeded(intent)
        }
    }

    private fun startBubbleServiceWithPermission() {
        val intent = Intent(this, BubbleService::class.java)
        startForegroundService(intent)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}