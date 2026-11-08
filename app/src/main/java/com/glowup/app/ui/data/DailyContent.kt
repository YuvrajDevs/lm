package com.glowup.app.ui.data

import java.time.LocalDate
import java.time.LocalTime

data class QuoteItem(val text: String, val tag: String)

val quoteLibrary = listOf(
    QuoteItem("\"Small, boring habits — done daily — beat big plans done never.\"", "Habits"),
    QuoteItem("\"Confidence is a skill you practise, not a trait you're born with.\"", "Confidence"),
    QuoteItem("\"Dress for the day you want, not just the one you're having.\"", "Style"),
    QuoteItem("\"Sleep is the easiest glow-up most people skip.\"", "Sleep"),
    QuoteItem("\"Nobody is thinking about you nearly as much as you think they are.\"", "Confidence"),
    QuoteItem("\"The person you'll be in a year is built by what you repeat this week.\"", "Habits"),
    QuoteItem("\"Say less, listen more — people remember how you made them feel.\"", "Communication"),
    QuoteItem("\"Posture is a sentence you say before you've said a word.\"", "Style"),
    QuoteItem("\"Discipline is just choosing between what you want now and what you want most.\"", "Habits"),
    QuoteItem("\"A good night's sleep solves more problems than a strong cup of coffee.\"", "Sleep"),
)

private val goalsByFocus = mapOf(
    "Looks" to listOf("Do your full skincare routine morning and night", "Wear SPF today, even indoors", "Drink 2 litres of water"),
    "Style" to listOf("Lay out tomorrow's outfit tonight", "Try one new combination from your wardrobe", "Iron or steam what you'll wear tomorrow"),
    "Confidence" to listOf("Start one new conversation today", "Accept a compliment without deflecting it", "Hold eye contact a beat longer than usual"),
    "Talk" to listOf("Replace one filler word with a pause", "Ask a follow-up question before sharing your own story", "Speak out loud for 2 minutes on any topic"),
    "Sleep" to listOf("Screens off 30 minutes before bed", "Be in bed by your bedtime tonight", "No caffeine after 2 PM"),
    "Fitness" to listOf("Take a 10-minute walk", "Do 20 push-ups across the day", "Stretch for 5 minutes before bed"),
    "Mind" to listOf("Journal for 5 minutes", "Take 2 minutes for slow breathing", "Write down 3 things that went well today"),
)

object DailyContent {
    fun quoteOfTheDay(today: LocalDate = LocalDate.now()) = quoteLibrary[today.dayOfYear % quoteLibrary.size]

    fun goalOfTheDay(focus: List<String>, today: LocalDate = LocalDate.now()): Pair<String, String> {
        val areas = focus.filter { it in goalsByFocus }.ifEmpty { listOf("Confidence") }
        val area = areas[today.dayOfYear % areas.size]
        val goals = goalsByFocus.getValue(area)
        return area to goals[(today.dayOfYear / areas.size) % goals.size]
    }

    fun greeting(now: LocalTime = LocalTime.now()) = when (now.hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Up late"
    }
}
