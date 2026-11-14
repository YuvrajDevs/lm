package com.glowup.app.ui.data

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val clockFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

fun parseClock(text: String): LocalTime = LocalTime.parse(text.uppercase(Locale.US), clockFormat)

fun formatClock(time: LocalTime): String = time.format(clockFormat)

sealed interface Schedule {
    val label: String

    /** The first trigger strictly after [now]. */
    fun nextAfter(now: LocalDateTime): LocalDateTime

    fun encode(): String

    data class Daily(val time: LocalTime) : Schedule {
        override val label get() = "${formatClock(time)} · Daily"

        override fun nextAfter(now: LocalDateTime): LocalDateTime {
            val today = now.toLocalDate().atTime(time)
            return if (today.isAfter(now)) today else today.plusDays(1)
        }

        override fun encode() = "daily|$time"
    }

    data class Weekly(val day: DayOfWeek, val time: LocalTime) : Schedule {
        override val label get() = "${day.getDisplayName(TextStyle.SHORT, Locale.US)} · ${formatClock(time)} · Weekly"

        override fun nextAfter(now: LocalDateTime): LocalDateTime {
            val candidate = now.toLocalDate().with(TemporalAdjusters.nextOrSame(day)).atTime(time)
            return if (candidate.isAfter(now)) candidate else candidate.plusWeeks(1)
        }

        override fun encode() = "weekly|$day|$time"
    }

    /** Every [hours] hours inside waking hours only, so interval nudges never fire overnight. */
    data class EveryHours(val hours: Int) : Schedule {
        override val label get() = if (hours == 1) "Every hour · 8 AM–10 PM" else "Every $hours hours · 8 AM–10 PM"

        override fun nextAfter(now: LocalDateTime): LocalDateTime {
            var day = now.toLocalDate()
            while (true) {
                var slot = day.atTime(WAKE_START)
                while (!slot.toLocalTime().isAfter(WAKE_END)) {
                    if (slot.isAfter(now)) return slot
                    slot = slot.plusHours(hours.toLong())
                    if (slot.toLocalDate() != day) break
                }
                day = day.plusDays(1)
            }
        }

        override fun encode() = "hours|$hours"
    }

    companion object {
        val WAKE_START: LocalTime = LocalTime.of(8, 0)
        val WAKE_END: LocalTime = LocalTime.of(22, 0)

        fun decode(text: String): Schedule? {
            val parts = text.split("|")
            return runCatching {
                when (parts[0]) {
                    "daily" -> Daily(LocalTime.parse(parts[1]))
                    "weekly" -> Weekly(DayOfWeek.valueOf(parts[1]), LocalTime.parse(parts[2]))
                    "hours" -> EveryHours(parts[1].toInt())
                    else -> null
                }
            }.getOrNull()
        }

        val presets: List<Schedule> = listOf(
            EveryHours(1),
            EveryHours(2),
            EveryHours(3),
            Daily(LocalTime.of(7, 0)),
            Daily(LocalTime.of(8, 0)),
            Daily(LocalTime.of(12, 30)),
            Daily(LocalTime.of(18, 0)),
            Daily(LocalTime.of(19, 0)),
            Daily(LocalTime.of(20, 0)),
            Daily(LocalTime.of(21, 0)),
            Daily(LocalTime.of(21, 30)),
            Daily(LocalTime.of(22, 0)),
            Daily(LocalTime.of(22, 30)),
            Weekly(DayOfWeek.SUNDAY, LocalTime.of(10, 0)),
            Weekly(DayOfWeek.SATURDAY, LocalTime.of(11, 0))
        )
    }
}
