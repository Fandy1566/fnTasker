package com.reee.fntasker.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.reee.fntasker.R
import com.reee.fntasker.ui.views.MultiFloatingView

class MainActivity : AppCompatActivity() {
    private lateinit var btnMulti: Button
    private val REQUEST_MEDIA_PROJECTION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        setContentView(R.layout.main_activity)

        btnMulti = findViewById(R.id.ma_btn_multi_auto_clicker)

        btnMulti.setOnClickListener {
            requestMediaProjectionPermission()
            if (hasDrawOverlaysPermission() || hasAccessibilityPermission() || hasMediaProjectionPermission()) {
                val intent = Intent(this@MainActivity, MultiFloatingView::class.java)
                startService(intent)
                finish()
            } else {
                getPermission()
            }
        }
    }


    private fun hasDrawOverlaysPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else false
    }

    private fun hasAccessibilityPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            accessibilityManager.isEnabled
        } else false
    }

    private fun hasMediaProjectionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION) == PackageManager.PERMISSION_GRANTED
        else false
    }

    private fun requestMediaProjectionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        }
    }

    private fun requestOverlaysPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 1)
        }
    }

    private fun requestAccessibilityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, 1)
        }
    }

    //Get Overlay and Accessibility Permission
    private fun getPermission() {
        if (hasDrawOverlaysPermission()) {
            requestOverlaysPermission()
        }
        if (hasAccessibilityPermission()) {
            requestAccessibilityPermission()
        }
        if (hasMediaProjectionPermission()) {
            requestMediaProjectionPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
//            if (hasDrawOverlaysPermission() || hasAccessibilityPermission() || hasMediaProjectionPermission()) {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
        }
    }


}