package com.example.lab4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.SharedPreferences
import android.content.Intent
import android.appwidget.AppWidgetManager
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import java.util.*


class WidgetConfigActivity : AppCompatActivity() {
    companion object {
        const val WIDGET_PREF = "widget_pref"
        const val WIDGET_TIME = "widget_time_"
        const val WIDGET_TIME_UNIX = "widget_tu_"
    }
    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var resultValue: Intent
    private lateinit var sp: SharedPreferences
    private lateinit var calendar: CalendarView
    private var date:Long = Date().time
    private val testCalendar: Calendar = Calendar.getInstance().apply {
        set(
            get(Calendar.YEAR),
            get(Calendar.MONTH),
            get(Calendar.DAY_OF_MONTH),
            9, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        widgetID = extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)

        setResult(RESULT_CANCELED, resultValue)

        setContentView(R.layout.activity_widget_config)

        sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)
        calendar = findViewById<CalendarView>(R.id.calendarView)

        val curDate = Calendar.getInstance()
        calendar.minDate = (
                if (curDate.time.after(testCalendar.time) &&
                    curDate[Calendar.DAY_OF_MONTH] == testCalendar[Calendar.DAY_OF_MONTH] &&
                    curDate[Calendar.MONTH] == testCalendar[Calendar.MONTH] &&
                    curDate[Calendar.YEAR] == testCalendar[Calendar.YEAR]
                ) curDate.apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                else curDate
                ).apply {
            set(Calendar.HOUR, testCalendar[Calendar.HOUR])
            set(Calendar.MILLISECOND, testCalendar[Calendar.MILLISECOND])
            set(Calendar.SECOND, testCalendar[Calendar.SECOND])
            set(Calendar.MINUTE, testCalendar[Calendar.MINUTE])
        }.timeInMillis

        calendar.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            val c = Calendar.getInstance()
            c.set(year, month, dayOfMonth,
                testCalendar[Calendar.HOUR],
                testCalendar[Calendar.MINUTE],
                testCalendar[Calendar.SECOND]
            )
            c.set(Calendar.MILLISECOND, testCalendar[Calendar.MILLISECOND])
            date = c.timeInMillis
        }
        calendar.date = sp.getLong(WIDGET_TIME_UNIX+widgetID, curDate.timeInMillis)
        date = calendar.date

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            val chosen = Calendar.getInstance().apply { timeInMillis = date }
            if(chosen.time.after(testCalendar.time) &&
                chosen[Calendar.DAY_OF_MONTH] == testCalendar[Calendar.DAY_OF_MONTH] &&
                chosen[Calendar.MONTH] == testCalendar[Calendar.MONTH] &&
                chosen[Calendar.YEAR] == testCalendar[Calendar.YEAR]
            ){
                Toast.makeText(this,
                    "Date should be after today, today's alarm time had already passed!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            sp.edit().apply{
                putString(WIDGET_TIME + widgetID, Date(date).toString())
                putLong(WIDGET_TIME_UNIX + widgetID, date)
            }.apply()
            setResult(RESULT_OK, resultValue)
            val appWidgetManager = AppWidgetManager.getInstance(this)
            DateWidgetProvider().onUpdate(this, appWidgetManager, IntArray(1){widgetID})
            finish()
        }
    }


}
