package com.example.chatapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.chatapp.activity.LoginActivity
import com.example.chatapp.applic
import com.google.firebase.messaging.*

class FirebaseNotificationService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        println("Token $p0")
        getSharedPreferences("chat_app", MODE_PRIVATE)
            .edit()
            .putString("fcm_token",p0)
            .apply()
    }
    var mNotificationManager: NotificationManager? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if(applic.inBackground){

            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(this,notification)
            ringtone.play()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                ringtone.isLooping = false
            }
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            val resourceImage = resources.getIdentifier(remoteMessage.notification!!.icon,"drawable",packageName)
            val builder = NotificationCompat.Builder(this,"CHANNEL_ID")
            val resultIntent = Intent(this, LoginActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,1,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setSmallIcon(resourceImage)
            builder.setContentTitle(remoteMessage.notification!!.title)
            builder.setContentText(remoteMessage.notification!!.body)
            builder.setContentIntent(pendingIntent)
            builder.setStyle(
                NotificationCompat.BigTextStyle().bigText(remoteMessage.notification!!.body)
            )
            builder.setAutoCancel(true)

            mNotificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "coolexp"
                val channel = NotificationChannel(
                    channelId,
                    "Music Recommendation",
                    NotificationManager.IMPORTANCE_HIGH
                )
                mNotificationManager!!.createNotificationChannel(channel)
                builder.setChannelId(channelId)

            mNotificationManager!!.notify(100, builder.build())
        }
    }
}