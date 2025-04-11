package pl.marcelg.batterymanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import pl.marcelg.batterymanager.ui.theme.BatteryMonitorTheme

class MainActivity : ComponentActivity() {
    private lateinit var batteryReceiver: BatteryBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        batteryReceiver = BatteryBroadcastReceiver()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)



//        createNotificationChannel(this)

        enableEdgeToEdge()
        setContent {
            BatteryMonitorTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BatteryPercentageDisplay(this@MainActivity)
                }
//                }
            }
        }
    }
}

@Composable
fun BatteryPercentageDisplay(context: Context) {
    var batteryPercentage by remember { mutableIntStateOf(0) }
    var batteryChargerState by remember { mutableStateOf(false) }

    val batteryReceiver = rememberUpdatedState(BatteryReceiver { percentage, chargerState ->
        batteryPercentage = percentage
        batteryChargerState = chargerState
    })

    DisposableEffect(Unit) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver.value, filter)

        onDispose {
            context.unregisterReceiver(batteryReceiver.value)
        }
    }
    Text(text = "$batteryPercentage%", fontSize = 32.sp)
    Text(text = if (batteryChargerState) "Charging" else "Not charging", fontSize = 18.sp)
}

//const val CHANNEL_ID = "general"
//const val LOW_ID = 0
//
//fun createNotificationChannel(context: Context) {
//    val name = "General"
//    val descriptionText = "Main channel"
//    val importance = NotificationManager.IMPORTANCE_DEFAULT
//    val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
//    mChannel.description = descriptionText
//    // Register the channel with the system. You can't change the importance
//    // or other notification behaviors after this.
//    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
//    notificationManager.createNotificationChannel(mChannel)
//}

class BatteryReceiver(private val onBatteryLevelChanged: (Int, Boolean) -> Unit) :
    BroadcastReceiver() {
//    private var notifLowSent = false;

    override fun onReceive(context: Context, intent: Intent?) {
        val level: Int = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val charging: Int = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        if (level != -1 && scale != -1) {
            val batteryPct = (level / scale.toFloat() * 100).toInt()
//            if (batteryPct <= 20) {
//                if (!notifLowSent) {
//
//                    var builder = NotificationCompat.Builder(
//                        context,
//                        CHANNEL_ID
//                    ).setSmallIcon(R.drawable.ic_stat_battery_alert)
//                        .setContentTitle("Low Battery!!!!!!")
//                        .setContentText("Battery fell to 20%!!!!!")
//                    with(NotificationManagerCompat.from(context)) {
//                        notify(LOW_ID, builder.build())
//                    }
//
//                    println("low");
//                    notifLowSent = true;
//                }
//            } else {
//                with(NotificationManagerCompat.from(context)) {
//                    cancel(LOW_ID)
//                }
//                notifLowSent = false;
//            }
            onBatteryLevelChanged(batteryPct, charging != 0)
        }
    }
}

