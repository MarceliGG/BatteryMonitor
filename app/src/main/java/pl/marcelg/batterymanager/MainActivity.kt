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




        enableEdgeToEdge()
        setContent {
            BatteryMonitorTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BatteryPercentageDisplay(this@MainActivity)
                }
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

class BatteryReceiver(private val onBatteryLevelChanged: (Int, Boolean) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val level: Int = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val charging: Int = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        if (level != -1 && scale != -1) {
            val batteryPct = (level / scale.toFloat() * 100).toInt()
            onBatteryLevelChanged(batteryPct, charging != 0)
        }
    }
}

