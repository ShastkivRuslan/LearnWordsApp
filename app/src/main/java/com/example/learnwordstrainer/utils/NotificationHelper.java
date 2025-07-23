package com.example.learnwordstrainer.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.learnwordstrainer.R;
import com.example.learnwordstrainer.ui.activities.MainActivity;
import com.example.learnwordstrainer.ui.activities.RepetitionActivity;
import com.example.learnwordstrainer.ui.activities.PracticeActivity;

import java.util.Random;

/**
 * Helper class for creating and managing notifications
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "learning_reminders";
    private static final String CHANNEL_NAME = "Нагадування про навчання";
    private static final String CHANNEL_DESCRIPTION = "Сповіщення для нагадування про вивчення слів";
    private static final int NOTIFICATION_ID = 1001;

    private static final String[] NOTIFICATION_TITLES = {
            "Час вивчати слова! 📚",
            "Не забувайте про навчання! 🎯",
            "Хвилинка для вивчення слів 💡",
            "Давайте повторимо слова! 🔄",
            "Час для практики! ⭐"
    };

    private static final String[] NOTIFICATION_MESSAGES = {
            "Повторіть кілька слів, щоб покращити словниковий запас",
            "Навіть 5 хвилин навчання принесуть користь",
            "Час закріпити знання! Повторіть вивчені слова",
            "Практика - ключ до успіху. Відкрийте додаток!",
            "Ваші слова чекають на повторення"
    };

    /**
     * Create notification channel for Android 8.0 and above
     */
    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Змінено на LOW щоб уникнути звуків
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableVibration(false); // Вимкнено вібрацію
        channel.enableLights(true); // Тільки світло
        channel.setSound(null, null); // Вимкнено звук

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Show learning reminder notification
     */
    public static void showLearningReminder(Context context) {
        createNotificationChannel(context);

        Random random = new Random();
        String title = NOTIFICATION_TITLES[random.nextInt(NOTIFICATION_TITLES.length)];
        String message = NOTIFICATION_MESSAGES[random.nextInt(NOTIFICATION_MESSAGES.length)];

        // Create intent for main activity
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create intent for repetition activity
        Intent repetitionIntent = new Intent(context, RepetitionActivity.class);
        repetitionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent repetitionPendingIntent = PendingIntent.getActivity(
                context, 1, repetitionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create intent for practice activity
        Intent practiceIntent = new Intent(context, PracticeActivity.class);
        practiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent practicePendingIntent = PendingIntent.getActivity(
                context, 2, practiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_LOW) // Змінено на LOW
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setDefaults(0) // Вимкнено всі дефолтні налаштування
                .setSound(null) // Явно без звуку
                .setVibrate(new long[]{0}) // Явно без вібрації
                .setOnlyAlertOnce(true) // Не дзвонити повторно
                .addAction(R.drawable.ic_repetition, "Повторити", repetitionPendingIntent)
                .addAction(R.drawable.ic_practice, "Практика", practicePendingIntent);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // Handle case where notification permission is not granted
            e.printStackTrace();
        }
    }

    /**
     * Cancel all notifications
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Check if notifications are enabled
     */
    public static boolean areNotificationsEnabled(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        return notificationManager.areNotificationsEnabled();
    }
}