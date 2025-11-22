package com.example.sms_tcp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            bundle?.let {
                val pdus = it["pdus"] as Array<*>
                for (pdu in pdus) {
                    val format = it.getString("format")
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray, format)
                    val sender = sms.displayOriginatingAddress
                    val message = sms.messageBody
                    val timestamp = sms.timestampMillis

                    val msgData = "$timestamp|$sender|$message"

                    val intentService = Intent(context, TcpService::class.java)
                    intentService.putExtra("sms_data", msgData)
                    context.startForegroundService(intentService)
                }
            }
        }
    }
}
