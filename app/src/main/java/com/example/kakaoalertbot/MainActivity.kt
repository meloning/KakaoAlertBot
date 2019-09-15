package com.example.kakaoalertbot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNoticePermission()
    }

    private fun checkNoticePermission() {
        val noticePermission = Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        if (noticePermission == null || !noticePermission.contains("com.example.kakaoalertbot")) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            Toast.makeText(this, "알림읽기 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "알림 읽기 권한 허용", Toast.LENGTH_SHORT).show()
    }
}
