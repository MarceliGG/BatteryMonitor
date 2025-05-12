package pl.marcelg.batterymanager

import android.content.Context
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EventType() {
    PLUGGED,
    UNPLUGGED
}

data class LogEntry(
    val timeStamp: String,
    val eventType: EventType,
) {
    override fun toString(): String {
        return "$timeStamp|$eventType"
    }
}

fun saveBatteryLog(context: Context, eventType: EventType) {
    val entry = LogEntry(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        eventType
    );


    val fileOutputStream: FileOutputStream
    try {
        fileOutputStream = context.openFileOutput("battery.log", Context.MODE_APPEND)
        fileOutputStream.write((entry.toString() + "\n").toByteArray())
        fileOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getBatteryLogs(context: Context): List<LogEntry> {
    val logs = mutableListOf<LogEntry>()
    val fileInputStream: FileInputStream
    try {
        fileInputStream = context.openFileInput("battery.log")
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        bufferedReader.use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line?.split("|")
                if (parts != null && parts.size == 2) {
                    val timestamp = parts[0]
                    val eventType = EventType.valueOf(parts[1])
                    logs.add(LogEntry(timestamp, eventType))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return logs
}
