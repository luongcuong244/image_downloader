package com.ko2ic.imagedownloader

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.PluginRegistry
import android.os.Build

class ImageDownloaderPermissionListener(private val activity: Activity) :
    PluginRegistry.RequestPermissionsResultListener {

    private val storagePermissions = mutableListOf<String>().apply {
        val readStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        this.add(readStoragePermission)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            this.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val permissionRequestId: Int = 2578166

    var callback: Callback? = null

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ): Boolean {

        if (!isStoragePermissionGranted()) {
            // when select deny.
            callback?.denied()
            return false
        }
        when (requestCode) {
            permissionRequestId -> {
                if (alreadyGranted()) {
                    callback?.granted()
                } else {
                    callback?.denied()
                }
            }
            else -> return false
        }
        return true
    }

    fun alreadyGranted(): Boolean {
        if (!isStoragePermissionGranted()) {
            // Request authorization. User is not yet authorized.
            ActivityCompat.requestPermissions(activity, storagePermissions.toTypedArray(), permissionRequestId)
            return false
        }
        // User already has authorization. Or below Android6.0
        return true
    }

    fun isStoragePermissionGranted(): Boolean {
        return storagePermissions.all {
            activity.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }

    interface Callback {
        fun granted()
        fun denied()
    }
}

