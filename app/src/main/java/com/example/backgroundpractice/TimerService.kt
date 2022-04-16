package com.example.backgroundpractice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

class TimerService : Service() {

  // Notification section
  private lateinit var notificationManager: NotificationManager
  private lateinit var notificationBuilder: NotificationCompat.Builder
  private val notificationId = 1
  // end section

  private lateinit var timer: CountDownTimer

  // Service section
  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "onCreate executed")
  }

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "onStartCommand executed")
    setupNotification()
    setupTimer()

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "onDestroy executed")
  }

  override fun onBind(intent: Intent): IBinder {
    TODO()
  }
  // end section


  /*******************
  * create notification channel and setup notification for foreground service
   *******************/
  @RequiresApi(Build.VERSION_CODES.M)
  private fun setupNotification() {

    val notificationChannelId = "timer_service"

    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // create notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(
          notificationChannelId,
          "Timer Service",
          NotificationManager.IMPORTANCE_LOW
        )
      notificationManager.createNotificationChannel(channel)
    }

    // setup intent for foreground service that user can click notification to navigate back to the activity
    val intent = Intent(this, MainActivity::class.java)
    val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    notificationBuilder =
      NotificationCompat.Builder(this, notificationChannelId).setContentTitle("Timer Service")
        .setContentText("Timer Start").setContentIntent(pi)
    startForeground(notificationId, notificationBuilder.build())
  }

  /**************
   * show remaining seconds in the notification
   ***************/
  private fun updateNotificationText(millis: Long) {
    val secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millis)
    val notification = notificationBuilder.setContentText(secondsRemaining.toString())
      .setSmallIcon(R.drawable.ic_launcher_foreground).build()
    notificationManager.notify(notificationId, notification)
  }

  /**************
   * setup countdown timer and start
   ***************/
  private fun setupTimer() {
    timer = object : CountDownTimer(30000, 1000) {
      override fun onTick(millis: Long) {
        updateNotificationText(millis)
      }

      override fun onFinish() {
        this@TimerService.stopSelf()
      }
    }.start()
  }
}