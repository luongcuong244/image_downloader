package com.ko2ic.imagedownloader

import androidx.annotation.NonNull
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Build
import android.provider.MediaStore
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.OutputStream
import android.util.Log
import com.caverock.androidsvg.SVG
import android.graphics.Canvas
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class ImageDownloaderPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var methodChannel: MethodChannel
    private var applicationContext: Context? = null

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        this.applicationContext = binding.applicationContext
        methodChannel = MethodChannel(binding.binaryMessenger, "plugins.ko2ic.com/image_downloader")
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall,@NonNull result: Result): Unit {
        when (call.method) {
            "downloadImage" -> {
                val cachedPath = call.argument<String?>("cachedPath")

                saveImageToGallery(cachedPath, result)
            }
            else -> {

            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = null
        methodChannel.setMethodCallHandler(null);
    }

    private fun sendBroadcast(context: Context, fileUri: Uri?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = fileUri
            context.sendBroadcast(mediaScanIntent)
        }
    }

    private fun saveImageToGallery(
        svgCachedPath: String?,
        result: Result
    ) {
        if (svgCachedPath == null) {
            return result.error("parameters error", null, null)
        }

        val context = applicationContext ?: return result.error("context is null", null, null)

        try {
            val bitmap: Bitmap? = BitmapFactory.decodeFile(svgCachedPath)
            if (bitmap == null) {
                return result.error("Failed to decode image", null, null)
            }

            val fileName = "${SimpleDateFormat(
                "yyyy-MM-dd.HH.mm.sss",
                Locale.getDefault()
            ).format(Date())}${Random.nextInt(1000)}.png"

            var uri: Uri? = null  // ✅ Khai báo ở ngoài để dùng chung

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 trở lên
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
            } else {
                // Android 9 trở xuống
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val imageFile = File(downloadsDir, fileName)
                FileOutputStream(imageFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                // Cập nhật MediaStore để ảnh hiển thị trong Gallery
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                uri = Uri.fromFile(imageFile)
            }

            uri?.let {
                sendBroadcast(context, it)
            }

            return result.success("saved")
        } catch (e: Exception) {
            return result.error(e.message ?: "unknown error", null, null)
        }
    }
}