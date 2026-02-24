package com.aiposts.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aiposts.MainActivity
import com.aiposts.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("ReminderReceiver", "ALARM TRIGGERED")
        val draftId = intent.getStringExtra(EXTRA_DRAFT_ID) ?: return
        val topic = intent.getStringExtra(EXTRA_TOPIC).orEmpty()

        val openIntent = Intent(context, MainActivity::class.java)
            .putExtra(DraftReminderManager.EXTRA_OPEN_DRAFT_ID, draftId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            draftId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DraftReminderManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Draft reminder")
            .setContentText(if (topic.isBlank()) "Your scheduled draft is ready" else topic)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(draftId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_DRAFT_ID = "draft_id"
        const val EXTRA_TOPIC = "draft_topic"
    }
}
