package coms.dypatil.noticeboard.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object DateUtil {
    const val DAY = "d"
    const val MONTH = "m"
    const val YEAR = "y"

    fun getCurrentDate(): Map<String, Int> {
        val c = Calendar.getInstance()
        return HashMap<String, Int>().apply {
            set(DAY, c.get(Calendar.DAY_OF_MONTH))
            set(MONTH, c.get(Calendar.MONTH))
            set(YEAR, c.get(Calendar.YEAR))
        }
    }

    fun getMillis(date: Int, month: Int, year: Int): Long? {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, date)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)

        if (System.currentTimeMillis() < calendar.timeInMillis) {
            return calendar.timeInMillis
        }
        return null
    }

    fun formatDate(d: Int, m: Int, y: Int, outputFormat: String = "dd MMM,yyyy"): String {
        return SimpleDateFormat(outputFormat, Locale.getDefault()).format(
                Date(y, m, d))
    }

    fun getDateFromMillis(epochMillis: Long): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = epochMillis

        val date: Calendar = Calendar.getInstance()
        val timeString = "h:mm aa"
        val dateString = "dd MMM,yyyy"

        return when {
            date.get(Calendar.DATE)==calendar.get(Calendar.DATE) -> "Today," + DateFormat.format(timeString, calendar)
            date.get(Calendar.DATE) - calendar.get(Calendar.DATE)==1 -> "Yesterday," + DateFormat.format(timeString, calendar)
            date.get(Calendar.YEAR)==calendar.get(Calendar.YEAR) -> DateFormat.format(dateString, calendar).toString()
            else -> DateFormat.format("MMMM dd yyyy", calendar).toString()
        }
    }


}



