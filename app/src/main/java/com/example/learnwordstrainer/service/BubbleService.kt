package com.example.learnwordstrainer.service

import BubbleSettingsRepository
import com.example.learnwordstrainer.repository.BubbleRepository

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.learnwordstrainer.R
import com.example.learnwordstrainer.ui.activities.AddWordFloatingActivity
import com.example.learnwordstrainer.ui.activities.MainActivity
import com.example.learnwordstrainer.viewmodels.BubbleViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.abs


class BubbleService : Service() {
    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var deleteZoneView: View? = null

    private var bubbleParams: WindowManager.LayoutParams? = null
    private var deleteZoneParams: WindowManager.LayoutParams? = null

    private var screenWidth = 0
    private var screenHeight = 0
    private var isDeleteZoneVisible = false
    private var isDeleteZoneActive = false
    private var isScreenOn = true
    private var isBubbleAdded = false
    private var screenReceiver: BroadcastReceiver? = null

    private var viewModel: BubbleViewModel? = null
    private var repository: BubbleRepository? = null

    private var settingsRepository : BubbleSettingsRepository? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        initRepository()
        initViewModel()
        registerScreenReceiver()
        observeSettings()

        if (isScreenOn) {
            initViews()
            setupWindowManager()
            setupTouchListener()
            setupClickListener()
            loadBubblePosition()

            try {
                windowManager!!.addView(bubbleView, bubbleParams)
                isBubbleAdded = true
            } catch (e: IllegalStateException) {
                Log.w("BubbleService", "View already added during onCreate", e)
                isBubbleAdded = true
            }
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_STOP_SERVICE == intent.action) {
            stopSelf()

            return START_NOT_STICKY
        }

        if (isScreenOn && bubbleView == null) {
            restoreBubble()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bubble Service Channel",
            NotificationManager.IMPORTANCE_MIN
        )
        channel.description = "Channel for Word Trainer Bubble Service"
        channel.setShowBadge(false)
        channel.setSound(null, null)
        channel.enableVibration(false)
        channel.enableLights(false)
        channel.lockscreenVisibility =
            Notification.VISIBILITY_SECRET

        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(
            this,
            MainActivity::class.java
        )
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(
            this,
            BubbleService::class.java
        )
        stopIntent.setAction(ACTION_STOP_SERVICE)
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Vocab.")
            .setContentText(if (isScreenOn) "Бульбашка активна" else "Бульбашка призупинена")
            .setSmallIcon(R.drawable.ic_bubble)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_close, "Зупинити", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN) // PRIORITY_MIN - найнижчий пріоритет
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Приховати на екрані блокування
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // Категорія сервісу
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initRepository() {
        repository =
            BubbleRepository(this)

        settingsRepository = BubbleSettingsRepository(this)
    }

    private fun initViewModel() {
        viewModel = BubbleViewModel(repository)
    }

    private fun initViews() {
        if (bubbleView != null) return

        val themedContext: Context = ContextThemeWrapper(this, R.style.Theme_LearnWordsTrainer)
        val inflater = LayoutInflater.from(themedContext)
        bubbleView = inflater.inflate(R.layout.bubble_layout, null, false)
        deleteZoneView = inflater.inflate(R.layout.delete_zone_layout, null)

        val savedSize = viewModel?.getSavedBubbleSize() ?: 40
        setBubbleSize(savedSize)
    }

    private fun setupWindowManager() {
        if (windowManager == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        }

        val size = Point()
        windowManager!!.defaultDisplay.getSize(size)
        screenWidth = size.x
        screenHeight = size.y

        bubbleParams = createBubbleLayoutParams()
        deleteZoneParams = createDeleteZoneLayoutParams()
    }

    private fun createBubbleLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = MARGIN_HORIZONTAL
        params.y = 100
        return params
    }

    private fun createDeleteZoneLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.y = 100
        return params
    }

    private fun loadBubblePosition() {
        if (bubbleParams != null && viewModel != null) {
            val position = viewModel!!.loadSavedPosition()
            bubbleParams!!.x = position!!.x
            bubbleParams!!.y = position.y
        }
    }

    private fun setupTouchListener() {
        if (bubbleView != null) {
            val bubbleCardView = bubbleView!!.findViewById<View>(R.id.bubbleCardView)
            bubbleCardView.setOnTouchListener(BubbleTouchListener())
            bubbleCardView.setOnLongClickListener { view: View ->
                provideFeedback(view)
                showQuickActions()
                true
            }
        }
    }

    private fun setupClickListener() {
        if (bubbleView != null) {
            bubbleView!!.findViewById<View>(R.id.bubbleCardView).setOnClickListener { v: View ->
                provideFeedback(v)
                launchAddWordActivity()
            }
        }
    }

    fun restoreBubble() {
        try {
            if (bubbleView == null) {
                initViews()
                setupWindowManager()
                setupTouchListener()
                setupClickListener()
                loadBubblePosition()
            }

            if (bubbleView != null && windowManager != null && !isBubbleAdded) {
                if (!bubbleView!!.isAttachedToWindow) {
                    windowManager!!.addView(bubbleView, bubbleParams)
                    isBubbleAdded = true
                }
            }
        } catch (e: IllegalStateException) {
            Log.w("BubbleService", "View already added to window manager", e)
            isBubbleAdded = true
        } catch (e: Exception) {
            Log.e("BubbleService", "Error restoring bubble", e)
        }
    }

    fun hideBubble() {
        try {
            if (bubbleView != null && windowManager != null && isBubbleAdded) {
                if (bubbleView!!.isAttachedToWindow) {
                    windowManager!!.removeView(bubbleView)
                }
                isBubbleAdded = false
            }
        } catch (e: IllegalArgumentException) {
            Log.d("BubbleService", "View wasn't in window manager", e)
            isBubbleAdded = false
        } catch (e: Exception) {
            Log.e("BubbleService", "Error hiding bubble", e)
            isBubbleAdded = false
        }
        bubbleView = null
    }

    private inner class BubbleTouchListener : OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var startClickTime: Long = 0

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> return handleTouchDown(event)

                MotionEvent.ACTION_MOVE -> return handleTouchMove(event)

                MotionEvent.ACTION_UP -> return handleTouchUp(v, event)
            }
            return false
        }

        fun handleTouchDown(event: MotionEvent): Boolean {
            initialX = bubbleParams!!.x
            initialY = bubbleParams!!.y
            initialTouchX = event.rawX
            initialTouchY = event.rawY
            startClickTime = System.currentTimeMillis()
            return true
        }

        fun handleTouchMove(event: MotionEvent): Boolean {
            showDeleteZone()
            val newX = calculateNewPosition(initialX, event.rawX - initialTouchX)
            val newY = calculateNewPosition(initialY, event.rawY - initialTouchY)

            updateDeleteZoneVisibility(newX, newY)
            updateBubblePosition(newX, newY)
            return true
        }

        fun calculateNewPosition(initialPosition: Int, delta: Float): Int {
            return initialPosition + delta.toInt()
        }

        fun handleTouchUp(v: View, event: MotionEvent): Boolean {
            val clickDuration = System.currentTimeMillis() - startClickTime

            val wasQuickTap = isQuickTap(clickDuration, event)
            var wasOverDeleteZone = false

            if (!wasQuickTap) {
                snapToEdge()

                if (isDeleteZoneVisible && isBubbleOverDeleteZone) {
                    provideFeedback(v)
                    wasOverDeleteZone = true
                    stopSelf()
                }
            }

            if (isDeleteZoneVisible) {
                hideDeleteZone()
            }

            if (wasQuickTap && !wasOverDeleteZone) {
                v.performClick()
            }

            return true
        }

        fun isQuickTap(clickDuration: Long, event: MotionEvent): Boolean {
            return clickDuration < CLICK_DURATION_THRESHOLD && abs((event.rawX - initialTouchX).toDouble()) < DRAG_TOLERANCE && abs(
                (event.rawY - initialTouchY).toDouble()
            ) < DRAG_TOLERANCE
        }
    }

    private fun updateBubblePosition(x: Int, y: Int) {
        if (bubbleParams != null && windowManager != null && bubbleView != null) {
            bubbleParams!!.x = x
            bubbleParams!!.y = y
            windowManager!!.updateViewLayout(bubbleView, bubbleParams)
        }
    }

    private fun showQuickActions() {
        // TODO: Implement quick actions menu
    }

    private fun provideFeedback(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun updateDeleteZoneVisibility(x: Int, y: Int) {
        if (isDeleteZoneActive && isBubbleOverDeleteZone(x, y)) {
            animateDeleteZoneHighlight()
        } else if (isDeleteZoneActive && !isBubbleOverDeleteZone(x, y)) {
            animateDeleteZoneNormal()
        }
    }

    private val isBubbleOverDeleteZone: Boolean
        get() = isBubbleOverDeleteZone(bubbleParams!!.x, bubbleParams!!.y)

    private fun isBubbleOverDeleteZone(x: Int, y: Int): Boolean {
        if (bubbleView == null || deleteZoneView == null) return false

        val bubbleCenterX = x + bubbleView!!.width / 2
        val bubbleCenterY = y + bubbleView!!.height / 2

        val centerX = screenWidth / 2
        val deleteZoneY = screenHeight - 100 - deleteZoneView!!.height / 2

        val dx = bubbleCenterX - centerX
        val dy = bubbleCenterY - deleteZoneY

        return dx * dx + dy * dy < DELETION_ZONE_RADIUS * DELETION_ZONE_RADIUS
    }

    private fun showDeleteZone() {
        if (!isDeleteZoneVisible && deleteZoneView != null && windowManager != null) {
            try {
                windowManager!!.addView(deleteZoneView, deleteZoneParams)
                isDeleteZoneVisible = true

                deleteZoneView!!.alpha = 0f
                deleteZoneView!!.scaleX = 0f
                deleteZoneView!!.scaleY = 0f

                deleteZoneView!!.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .alpha(0.5f)
                    .setDuration(250)
                    .withEndAction { isDeleteZoneActive = true }
                    .start()
            } catch (e: Exception) {
                // Логування помилки
            }
        }
    }

    private fun animateDeleteZoneHighlight() {
        if (deleteZoneView != null) {
            deleteZoneView!!.animate().cancel()
            deleteZoneView!!.clearAnimation()

            deleteZoneView!!.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(150)
                .start()
        }
    }

    private fun animateDeleteZoneNormal() {
        if (deleteZoneView != null) {
            deleteZoneView!!.animate().cancel()
            deleteZoneView!!.clearAnimation()

            deleteZoneView!!.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .alpha(0.5f)
                .setDuration(150)
                .start()
        }
    }

    private fun snapToEdge() {
        if (bubbleView == null) return

        var bubbleWidth = bubbleView!!.measuredWidth
        if (bubbleWidth == 0) bubbleWidth = 40

        val targetX = calculateTargetX(bubbleWidth)
        val startX = bubbleParams!!.x

        val animatorSet = createSnapAnimations(startX, targetX)
        animatorSet.start()
    }

    private fun calculateTargetX(bubbleWidth: Int): Int {
        // Тепер отримуємо реальну ширину динамічно
        var bubbleWidth = bubbleWidth
        if (bubbleWidth == 0) {
            bubbleWidth = bubbleSizeDp
            if (bubbleWidth <= 0) bubbleWidth = 40 // резервне значення

            bubbleWidth = dpToPx(bubbleWidth)
        }

        return if (bubbleParams!!.x < screenWidth / 2) {
            MARGIN_HORIZONTAL
        } else {
            screenWidth - bubbleWidth - MARGIN_HORIZONTAL
        }
    }

    private fun createSnapAnimations(startX: Int, targetX: Int): AnimatorSet {
        val xAnimator = createHorizontalAnimator(startX, targetX)
        val scaleAnimator = createScaleAnimator()
        val wobbleAnimator = createWobbleAnimator()
        val alphaAnimator = createAlphaAnimator()

        val firstSet = AnimatorSet()
        firstSet.playTogether(wobbleAnimator, scaleAnimator)

        val secondSet = AnimatorSet()
        secondSet.playTogether(xAnimator, alphaAnimator)

        val animatorSet = AnimatorSet()
        animatorSet.play(firstSet).before(secondSet)
        animatorSet.setDuration(ANIMATION_DURATION)

        return animatorSet
    }

    private fun createHorizontalAnimator(startX: Int, targetX: Int): ValueAnimator {
        val xAnimator = ValueAnimator.ofInt(startX, targetX)
        xAnimator.addUpdateListener { animation: ValueAnimator ->
            if (bubbleParams != null && viewModel != null && windowManager != null && bubbleView != null) {
                bubbleParams!!.x = animation.animatedValue as Int
                viewModel!!.saveBubblePosition(bubbleParams!!.x, bubbleParams!!.y)
                windowManager!!.updateViewLayout(bubbleView, bubbleParams)
            }
        }
        xAnimator.interpolator = BounceInterpolator()

        return xAnimator
    }

    private fun createScaleAnimator(): ObjectAnimator {
        val scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 0.8f, 1f)
        val scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 0.8f, 1f)
        val scaleAnimator =
            ObjectAnimator.ofPropertyValuesHolder(bubbleView, scaleXHolder, scaleYHolder)
        scaleAnimator.interpolator = AnticipateOvershootInterpolator(1.5f)
        return scaleAnimator
    }

    private fun createWobbleAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(bubbleView, "rotation", 0f, -10f, 10f, -5f, 5f, 0f)
    }

    private fun createAlphaAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(bubbleView, "alpha", 1f, 0.8f, 1f)
    }

    private fun hideDeleteZone() {
        if (deleteZoneView != null) {
            deleteZoneView!!.animate().cancel()
            deleteZoneView!!.clearAnimation()

            if (isDeleteZoneVisible) {
                deleteZoneView!!.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(150)
                    .withEndAction { this.removeDeleteZoneView() }
                    .start()
            }
        }
    }

    private fun removeDeleteZoneView() {
        if (deleteZoneView != null && isDeleteZoneVisible) {
            try {
                windowManager!!.removeView(deleteZoneView)
                isDeleteZoneVisible = false
                isDeleteZoneActive = false
            } catch (e: IllegalArgumentException) {
                // View might already be removed
            }
        }
    }

    private fun launchAddWordActivity() {
        val intent = Intent(
            this,
            AddWordFloatingActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupViews()
        viewModel = null
        serviceScope.cancel()

        if (screenReceiver != null) {
            unregisterReceiver(screenReceiver)
            screenReceiver = null
        }
    }

    private fun cleanupViews() {
        if (bubbleView != null && isBubbleAdded) {
            try {
                if (bubbleView!!.isAttachedToWindow) {
                    windowManager!!.removeView(bubbleView)
                }
            } catch (e: IllegalArgumentException) {
                Log.d("BubbleService", "View wasn't in window manager during cleanup", e)
            }
            isBubbleAdded = false
            bubbleView = null
        }

        if (deleteZoneView != null && isDeleteZoneVisible) {
            try {
                windowManager!!.removeView(deleteZoneView)
            } catch (e: IllegalArgumentException) {
                Log.d("BubbleService", "Delete zone view wasn't in window manager", e)
            }
            deleteZoneView = null
            isDeleteZoneVisible = false
            isDeleteZoneActive = false
        }
    }

    private fun registerScreenReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_SCREEN_OFF == action) {
                    isScreenOn = false
                    hideBubble()
                    updateNotification()
                } else if (Intent.ACTION_SCREEN_ON == action || Intent.ACTION_USER_PRESENT == action) {
                    isScreenOn = true
                    if (!isBubbleAdded || bubbleView == null || !bubbleView!!.isAttachedToWindow) {
                        restoreBubble()
                    }
                    updateNotification()
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenReceiver, filter)
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager?.notify(NOTIFICATION_ID, createNotification())
    }

    private fun setBubbleSize(sizeDp: Int) {
        if (bubbleView == null) return

        val bubbleCardView =
            bubbleView!!.findViewById<MaterialCardView>(R.id.bubbleCardView) ?: return

        val sizePixels = dpToPx(sizeDp)

        val layoutParams = bubbleCardView.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = sizePixels
        layoutParams.height = sizePixels

        bubbleCardView.layoutParams = layoutParams

        val imageView = bubbleView!!.findViewById<ImageView>(R.id.bubbleIcon)
        val imageParams = imageView.layoutParams as FrameLayout.LayoutParams

        imageParams.width = sizePixels / 2
        imageParams.height = sizePixels / 2

        imageView.layoutParams = imageParams
    }

    private fun setBubbleTransparency(transparencyPercent: Float) {
        if (bubbleView == null) return

        val bubbleCardView =
            bubbleView!!.findViewById<MaterialCardView>(R.id.bubbleCardView) ?: return

        val alphaValue = (transparencyPercent / 100f).coerceIn(0.0f, 1.0f)

        bubbleCardView.alpha = alphaValue
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private val bubbleSizeDp: Int
        get() {
            if (bubbleView == null) return -1

            val bubbleCardView =
                bubbleView!!.findViewById<MaterialCardView>(R.id.bubbleCardView) ?: return -1

            val widthPixels = bubbleCardView.layoutParams.width
            return pxToDp(widthPixels)
        }

    private fun pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    private fun observeSettings() {
        settingsRepository?.bubbleSize
            ?.onEach { newSize ->
                setBubbleSize(newSize.toInt())
            }
            ?.launchIn(serviceScope)

        settingsRepository?.bubbleTransparency
            ?.onEach {
                newTransparency ->
                setBubbleTransparency(newTransparency)
            }
            ?.launchIn(serviceScope)
    }


    companion object {
        private const val DRAG_TOLERANCE = 20
        private const val MARGIN_HORIZONTAL = 20
        private const val ANIMATION_DURATION: Long = 300
        private const val CLICK_DURATION_THRESHOLD: Long = 200
        private const val DELETION_ZONE_RADIUS = 150
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "bubble_service_channel"
        private const val ACTION_STOP_SERVICE = "com.example.learnwordstrainer.STOP_BUBBLE_SERVICE"
    }
}
