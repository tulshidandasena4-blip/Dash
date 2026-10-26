package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val currencyFormat = DecimalFormat("##,##,##0.00")
    private val wholeCurrencyFormat = DecimalFormat("##,##,##0")
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val displayMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatCurrency(amount: Double, includeDecimals: Boolean = false): String {
        return if (includeDecimals) {
            "₹ " + currencyFormat.format(amount)
        } else {
            "₹ " + wholeCurrencyFormat.format(amount)
        }
    }

    fun getTodayIsoDate(): String {
        return isoDateFormat.format(Date())
    }

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val date = isoDateFormat.parse(isoDate)
            if (date != null) displayDateFormat.format(date) else isoDate
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatMonthYear(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            displayMonthYearFormat.format(cal.time)
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun getCurrentYear(): String {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR).toString()
    }

    fun getCurrentMonthPrefix(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return "%04d-%02d".format(year, month)
    }

    fun parseDoubleOrZero(text: String): Double {
        return text.trim().toDoubleOrNull() ?: 0.0
    }

    fun parseIntOrZero(text: String): Int {
        return text.trim().toIntOrNull() ?: 0
    }
}
