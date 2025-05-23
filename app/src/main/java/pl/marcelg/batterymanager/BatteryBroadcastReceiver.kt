package pl.marcelg.batterymanager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getString


class BatteryBroadcastReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "general";
    private val LOW_ID = 0;
    private val FULL_ID = 1;
    private var notifLowSent = false;
    private var notifFullSent = false;

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
            val charging: Int = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val batteryPct = (level / scale.toFloat() * 100).toInt()
            if (batteryPct <= 20) {
                if (!notifLowSent) {
                    sendNotification(
                        context,
                        LOW_ID
                    )
                    notifLowSent = true;
                }
            } else {
                with(NotificationManagerCompat.from(context)) {
                    cancel(LOW_ID)
                }
                notifLowSent = false;
            }
            if (charging != 0 && batteryPct >= 100) {
                if (!notifFullSent) {
                    sendNotification(
                        context,
                        FULL_ID
                    )
                    notifFullSent = true;
                }
            } else {
                with(NotificationManagerCompat.from(context)) {
                    cancel(FULL_ID)
                }
                notifFullSent = false;
            }
        }
    }

    @SuppressLint("MissingPermission")

    private fun sendNotification(context: Context, id: Int) {
        val notificationIntent = Intent(
            context,
            MainActivity::class.java
        )
        val text = when (id) {
            LOW_ID -> getString(context, R.string.low_text)
            FULL_ID -> getString(context, R.string.full_text)
            else -> "err"
        }

        val title = when (id) {
            LOW_ID -> getString(context, R.string.low_title)
            FULL_ID -> getString(context, R.string.full_title)
            else -> "err"
        }

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent = PendingIntent.getActivity(
            context, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        var builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_stat_battery_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(intent)
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
}
