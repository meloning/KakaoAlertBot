package com.example.kakaoalertbot

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import org.json.JSONObject



class NotificationListener: NotificationListenerService() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = GsonBuilder().create()
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
        sharedPreferences = getSharedPreferences("actionRoom", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val wearableExtender = Notification.WearableExtender(sbn.notification)
        wearableExtender.actions.forEach { action ->
            if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) {
                val message = extras.getString("android.text")?: ""
                val sender = extras.getString("android.title")?: ""
                val room = action.title.toString()

                if (room.contains("정보처리 IT 전문연구/산업기능요원 비번:1024")) {
                    hashMap["정보처리"] = action
                }
                if (room.contains("메로닝봇")) {
                    hashMap["메로닝봇"] = action
                }

                Log.i("message/content", "$room:$sender:$message")

                editor.putString("roomHashMap", gson.toJson(hashMap))
                editor.apply()

                if (message.contains("[JunsuAlert]"))
                    send("[TEST] 병무청 NEW 공지사항이 있어요\n크롤링 -> slack -> 카톡알림", "메로닝봇")
            }
        }
        stopSelf()
    }

    // TODO: SQLite DB에 parcelable Data(Notification.Action) 저장하는 방법 search
    private fun send(message: String, roomKey: String) {
        val sendIntent = Intent()
        val msg = Bundle()
        val tempHashMap = HashMap<String, Notification.Action>()

        if (!sharedPreferences.contains("roomHashMap")) {
            Log.i("Listener/send:", "HashMap don't exist")
            Toast.makeText(this.applicationContext, "방 정보가 제대로 저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val actionJson = sharedPreferences.getString("roomHashMap", "")?: ""
        val jsonObject = JSONObject(actionJson)
        val keysIterator = jsonObject.keys()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            val value = jsonObject[key] as Notification.Action
            tempHashMap[key] = value
        }

        if (tempHashMap.isEmpty()) {
            Log.i("Listener/send:", "보낼 방의 정보가 아직 없습니다. 보내고자 하는 방의 메시지를 기다려주세요.")
            Toast.makeText(this.applicationContext, "보낼 방의 정보가 아직 없습니다. 보내고자 하는 방의 메시지를 기다려주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val session = tempHashMap[roomKey] as Notification.Action

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