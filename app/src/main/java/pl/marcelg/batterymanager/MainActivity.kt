package pl.marcelg.batterymanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.ContextCompat.getString
import pl.marcelg.batterymanager.ui.theme.BatteryMonitorTheme

class MainActivity : ComponentActivity() {
    private lateinit var batteryReceiver: BatteryBroadcastReceiver
    private lateinit var chargerReceiver: ChargerBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        batteryReceiver = BatteryBroadcastReceiver()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)

        chargerReceiver = ChargerBroadcastReceiver()
        val filter1 = IntentFilter()
        filter1.addAction(Intent.ACTION_POWER_CONNECTED)
        filter1.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(chargerReceiver, filter1)


        setContent {
            BatteryMonitorTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BatteryPercentageDisplay(this@MainActivity)
                    LogDisplay(this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun LogDisplay(context: Context) {
    var logs by remember { mutableStateOf(getBatteryLogs(context)) }
    Button(
        onClick = {
            logs = getBatteryLogs(context)
        }
    ) { Text("Refresh", color = MaterialTheme.colorScheme.onPrimary) }
    LazyColumn {
        items(logs) { item ->
            Row {
                Text(
                    item.timeStamp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    context.getString(
                        context.resources.getIdentifier(
                            "event_type_${item.eventType}",
                            "string",
                            context.packageName
                        )
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
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
    Text(
        text = "$batteryPercentage%", fontSize = 32.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(
        text = getString(
            context,
            if (batteryChargerState) R.string.charging else R.string.not_charging
        ), fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

class BatteryReceiver(private val onBatteryLevelChanged: (Int, Boolean) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val level: Int = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val charging: Int = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        if (level != -1 && scale != -1) {
            val batteryPct = (level / scale.toFloat() * 100).toInt()
            onBatteryLevelChanged(batteryPct, charging != 0)
        }
    }
}

