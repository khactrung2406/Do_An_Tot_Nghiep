package com.example.app_yolo11.di

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import java.io.InputStream

object ImageUtils {
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error loading bitmap from URI", e)
            null
        }
    }


    fun resizeBitmapAndPad(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()


        val ratio = minOf(targetSize.toFloat() / width, targetSize.toFloat() / height)

        matrix.postScale(ratio, ratio)

        val scaledBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(scaledBitmap)


        canvas.drawColor(Color.BLACK)


        val dx = (targetSize - width * ratio) / 2
        val dy = (targetSize - height * ratio) / 2


        matrix.postTranslate(dx, dy)
        canvas.drawBitmap(bitmap, matrix, null)

        return scaledBitmap
    }
}