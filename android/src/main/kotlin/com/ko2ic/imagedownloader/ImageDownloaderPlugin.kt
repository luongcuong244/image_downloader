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
import io.flutter.plugin.common.PluginRegistry.Registrar
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
        // check parameters
        if (svgCachedPath == null) {
            return result.error("parameters error", null, null)
        }
        // check applicationContext
        val context = applicationContext
            ?: return result.error("context is null", null, null)

        try {
            val svgFile: File = File(svgCachedPath)
            val inputStream: InputStream = FileInputStream(svgFile)
            val svg: SVG = SVG.getFromInputStream(inputStream)

            val bitmap: Bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
            val canvas: Canvas = Canvas(bitmap)
            canvas.drawPicture(svg.renderToPicture())

            val fileName = "${SimpleDateFormat(
                "yyyy-MM-dd.HH.mm.sss",
                Locale.getDefault()
            ).format(Date())}${Random.nextInt(1000)}.png"

            // Create the content values to hold the metadata
            val contentValues: ContentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)

            // Insert the image into the MediaStore
            val uri = context.getContentResolver()
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                // Get the output stream for the file
                val outputStream = context.getContentResolver().openOutputStream(uri)
                // Compress the bitmap and write to the output stream
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
                sendBroadcast(context, uri)
            }

            return result.success("unknown")
        } catch (e: Exception) {
            return result.error(e.message ?: "unknow", null, null)
        }
    }
}