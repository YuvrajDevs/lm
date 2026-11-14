package com.glowup.app.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalTime
import org.json.JSONObject

object SleepStore {
    private const val KEY = "sleep"

    var bedtime by mutableStateOf(LocalTime.of(23, 0))
        private set
    var wakeTime by mutableStateOf(LocalTime.of(7, 0))
        private set
    var bedtimeAlarmOn by mutableStateOf(false)
        private set
    var wakeAlarmOn by mutableStateOf(false)
        private set
    var sound by mutableStateOf("Night Rain")
        private set
    var timerMinutes by mutableStateOf(30)
        private set

    fun updateBedtime(time: LocalTime?) {
        if (time != null) bedtime = time
        bedtimeAlarmOn = time != null
        changed()
    }

    fun updateWakeTime(time: LocalTime?) {
        if (time != null) wakeTime = time
        wakeAlarmOn = time != null
        changed()
    }

    fun selectSound(name: String) {
        sound = name
        save()
    }

    fun selectTimer(minutes: Int) {
        timerMinutes = minutes
        save()
    }

    private fun changed() {
        save()
        Storage.onScheduleChanged()
    }

    /** Turns alarms off and restores default sleep settings. */
    fun resetAlarms() {
        Storage.remove(KEY)
        load()
        Storage.onScheduleChanged()
    }

    fun load() {
        val json = Storage.read(KEY)?.let(::JSONObject)
        bedtime = json?.let { LocalTime.parse(it.getString("bedtime")) } ?: LocalTime.of(23, 0)
        wakeTime = json?.let { LocalTime.parse(it.getString("wake")) } ?: LocalTime.of(7, 0)
        bedtimeAlarmOn = json?.getBoolean("bedtimeOn") ?: false
        wakeAlarmOn = json?.getBoolean("wakeOn") ?: false
        sound = json?.getString("sound") ?: "Night Rain"
        timerMinutes = json?.getInt("timer") ?: 30
    }

    private fun save() {
        Storage.write(
            KEY,
            JSONObject()
                .put("bedtime", bedtime.toString())
                .put("wake", wakeTime.toString())
                .put("bedtimeOn", bedtimeAlarmOn)
                .put("wakeOn", wakeAlarmOn)
                .put("sound", sound)
                .put("timer", timerMinutes)
                .toString()
        )
    }
}
