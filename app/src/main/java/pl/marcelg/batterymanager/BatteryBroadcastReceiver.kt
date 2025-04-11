package pl.marcelg.batterymanager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class BatteryBroadcastReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "general";
    private val LOW_ID = 0;
    private val FULL_ID = 1;
    private var notifLowSent = false;
    private var notifFullSent = false;

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val level: Int = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val charging: Int = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            if (level != -1 && scale != -1) {
                val batteryPct = (level / scale.toFloat() * 100).toInt()
                if (batteryPct <= 40) {
                    if (!notifLowSent) {
                        sendNotification(
                            context,
                            "Low Battery!!!!!!",
                            "Battery fell to 20%!!!!!",
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
                if (charging > 0 && batteryPct >= 100) {
                    if (!notifFullSent) {
                        sendNotification(
                            context,
                            "Battery Full",
                            "Your battery is now fully charged",
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
    }

    @SuppressLint("MissingPermission")

    private fun sendNotification(context: Context, title: String, text: String, id: Int) {
        val notificationIntent = Intent(
            context,
            MainActivity::class.java
        )
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
