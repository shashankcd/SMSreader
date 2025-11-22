package com.example.sms_tcp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import java.io.File
import java.io.OutputStream
import java.net.Socket

class TcpService : Service() {

    private val serverIP = "192.168.0.100" // Replace with your TCP server IP
    private val serverPort = 12345          // Replace with your TCP port
    private val fileName = "unsent_sms.txt"

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "SMS_TCP_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMS TCP Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("SMS TCP Service Running")
            .setContentText("Monitoring incoming SMS and sending to server")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getStringExtra("sms_data") ?: return START_NOT_STICKY
        Thread { sendMessage(data) }.start()
        return START_STICKY
    }

    private fun sendMessage(data: String) {
        try {
            Socket(serverIP, serverPort).use { socket ->
                val out: OutputStream = socket.getOutputStream()
                out.write((data + "\n").toByteArray())
                out.flush()

                val file = File(filesDir, fileName)
                if (file.exists()) {
                    val unsent = file.readLines()
                    for (line in unsent) {
                        out.write((line + "\n").toByteArray())
                        out.flush()
                    }
                    file.delete()
                }
            }
        } catch (e: Exception) {
            val file = File(filesDir, fileName)
            file.appendText(data + "\n")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
