package com.reee.fntasker.autoClicker.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Path
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.reee.fntasker.R
import com.reee.fntasker.ui.activities.MainActivity
import com.reee.fntasker.autoClicker.event.EventExecutor
import com.reee.fntasker.autoClicker.interfaces.Event
import com.reee.fntasker.model.EventData
import com.reee.fntasker.model.EventType
import kotlinx.coroutines.*

class AutoClickerService : AccessibilityService(){

    private val actions : EventExecutor = EventExecutor()
    private val eventList : MutableList<EventData> = mutableListOf()
    private val NOTIFICATION_ID = 1
    private val NOTIFICATION_CHANNEL = "FnAutoClicker"
    val ACTION_AUTO_SERVICE_COMPLETE = "com.fn.myapplication.AUTO_SERVICE_COMPLETE"
    private var mHandler: Handler? = null
    private var mRunnable: IntervalRunnable? = null
    private var loop = 0

    override fun onCreate() {
        super.onCreate()
        val mHandlerThread = HandlerThread("auto-handler")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
        intent?.let {
            eventList = intent.getParcelableArrayListExtra("action") ?: mutableListOf()

            loop = intent.getIntExtra("loop", 0)

            if (mRunnable == null) {
                mRunnable = IntervalRunnable()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    fun startExecuteEvents() {
        eventList.forEach {
            when (it.eventType) {
                EventType.CLICK -> {
                    Click(
                        it.params?.get("point") as? Point ?: Point(0, 0),
                        it.params?.get("delay") as? Int ?: 0,
                        it.params?.get("hold") as? Int ?: 0
                    )
                }
                EventType.SWIPE -> {
                    Swipe(
                        it.params?.get("from") as? Point ?: Point(0, 0),
                        it.params?.get("to") as? Point ?: Point(0, 0),
                        it.params?.get("hold") as? Int ?: 0,
                        it.params?.get("delay") as? Int ?: 0,
                    )
                }
                EventType.SLEEP -> {
                    Sleep(
                        it.params?.get("time") as? Int ?: 0
                    )
                }

                else -> {

                }
            }
        }
    }

    //Runnable is an interface that is to be implemented by a class whose instances are intended to be executed by a thread
    private inner class IntervalRunnable : Runnable {
        override fun run() {
            Toast.makeText(baseContext, "in run now", Toast.LENGTH_SHORT).show()

            for (i in 0 until loop)
            {
                startExecuteEvents()
            }
            sendCompletionBroadcast()
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("AutoClicker Service")
            .setContentText("AutoClicker service is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setLocalOnly(true)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                "AutoClicker Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendCompletionBroadcast() {
        val broadcastIntent = Intent(ACTION_AUTO_SERVICE_COMPLETE)
        // Add any additional data to the intent if needed
        sendBroadcast(broadcastIntent)
    }

    inner class Click(
        private var point: Point,
        private var delay : Int,
        private var hold: Int,
    ) : Event {
        lateinit var path : Path
        override fun execute() {
            path = Path()
            path.moveTo(point.x.toFloat(), point.y.toFloat())
            path.lineTo(point.x.toFloat(), point.y.toFloat())
            val gestbuildr = GestureDescription.Builder()
            gestbuildr.addStroke(StrokeDescription(path, 0, hold.toLong()))
            dispatchGesture(gestbuildr.build(), null, null)
            Sleep(delay)
        }
    }

    inner class Swipe(
        private val from: Point,
        private val to: Point,
        private val hold: Int,
        private val delay: Int
    ) : Event {
        lateinit var path : Path
        override fun execute() {
            path = Path()
            path.moveTo(from.x.toFloat(), from.y.toFloat())
            path.lineTo(to.x.toFloat(), to.y.toFloat())
            val gestbuildr = GestureDescription.Builder()
            gestbuildr.addStroke(StrokeDescription(path, 0, hold.toLong()))
            dispatchGesture(gestbuildr.build(), null, null)
            Sleep(delay)
        }
    }

    inner class Sleep(private var time : Int) : Event {
        override fun execute() {
            mHandler?.postDelayed({
                //Do Nothing
            }, time.toLong())
        }
    }

}