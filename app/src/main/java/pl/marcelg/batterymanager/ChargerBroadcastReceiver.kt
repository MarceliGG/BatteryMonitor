package pl.marcelg.batterymanager;

import android.content.BroadcastReceiver
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager

class ChargerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context:Context, intent:Intent ?){
        if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
            saveBatteryLog(context, EventType.PLUGGED)
        } else if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
            saveBatteryLog(context, EventType.UNPLUGGED)
        }
    }
}
