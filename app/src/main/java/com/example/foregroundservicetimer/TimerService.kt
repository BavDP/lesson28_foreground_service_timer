package com.example.foregroundservicetimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

const val CHANNEL_ID = "TimerChannel"
const val NOTIFICATION_ID = 100
const val DATA_KEY = "Data"

class TimerService: Service() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var timer: Deferred<Unit>
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var serviceStarted = false
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        notificationBuilder = createNotification()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        timer = createTimer(intent?.getIntExtra(DATA_KEY, 60)?:60, notificationBuilder)
        timer.start()
        serviceStarted = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        serviceStarted = false
        timer.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descText
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun createNotification(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
    }

    private fun  createTimer(startValue: Int, builder: NotificationCompat.Builder): Deferred<Unit> {
        return CoroutineScope(Dispatchers.Default).async {
            var i = startValue
            while (true) {
                builder.setContentTitle(i.toString())
                withContext(Dispatchers.Main) {
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                }
                delay(1000)
                i--
                if (i == -1) break
            }
        }
    }

    companion object {
        fun startForegroundService(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            intent.putExtra(DATA_KEY, 60)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopForegroundService(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            context.stopService(intent)
        }
    }
}