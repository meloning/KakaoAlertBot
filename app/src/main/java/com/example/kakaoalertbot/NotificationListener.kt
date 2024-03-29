package com.example.kakaoalertbot

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast



class NotificationListener: NotificationListenerService() {

    private val hashMap = HashMap<String, Notification.Action>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) {
            Log.d("Listener/sbn:","StatusBarNotification is null")
            return
        }

        Log.i("Listener/sbn:", sbn.clone().toString())
        Log.i("Listener/sbn:", sbn.clone().notification.extras.toString())

        val extras = sbn.notification.extras

        val wearableExtender = Notification.WearableExtender(sbn.notification)
        wearableExtender.actions.forEach { action ->
            if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) {
                val message = extras.getString("android.text")?: ""
                val sender = extras.getString("android.title")?: ""
                val room = action.title.toString()

                if (room.contains("roomName")) {
                    hashMap["roomName"] = action
                }
                if (room.contains("roomName2")) {
                    hashMap["roomName2"] = action
                }

                Log.i("message/content", "$room:$sender:$message")

                if (message.contains("MessageContent"))
                    send("[BOT] MessageContent", "roomName")
                    send("[BOT] MessageContent", "roomName2")
            }
        }
        stopSelf()
    }

    // TODO: SQLite DB에 parcelable Data(Notification.Action) 저장하는 방법 search
    private fun send(message: String, roomKey: String) {
        val sendIntent = Intent()
        val msg = Bundle()

        if (hashMap.isEmpty() || !hashMap.containsKey(roomKey)) {
            Log.i("Listener/send:", "보낼 방의 정보가 아직 없습니다. 보내고자 하는 방의 메시지를 기다려주세요.")
            Toast.makeText(this.applicationContext, "보낼 방의 정보가 아직 없습니다. 보내고자 하는 방의 메시지를 기다려주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val session = hashMap[roomKey] as Notification.Action

        for (inputable in session.remoteInputs) {
            msg.putCharSequence(inputable.resultKey, message)
        }
        RemoteInput.addResultsToIntent(session.remoteInputs, sendIntent, msg)

        try {
            session.actionIntent.send(this.applicationContext, 0, sendIntent)
            Log.i("send() complete", message)
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }

    }
}