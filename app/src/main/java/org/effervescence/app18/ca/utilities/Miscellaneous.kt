package org.effervescence.app18.ca.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.drm.DrmStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

fun compressImage(context: Context?, imageUri: Uri, title: String): Uri? {

    if (context != null) {

        val imageStream: InputStream = context.contentResolver.openInputStream(imageUri)
        val options = BitmapFactory.Options()
        val size = imageStream.available()

        when {
            size >= (1 * 1024 * 1024) -> options.inSampleSize = 8
            size >= (512 * 1024) -> options.inSampleSize = 4
            size >= (64 * 1024) -> options.inSampleSize = 2
            else -> options.inSampleSize = 1
        }

        val compressedImage = BitmapFactory.decodeStream(imageStream, null, options)
        return getImageUri(context, compressedImage, title)
    } else return null
}

fun getImageUri(context: Context, imageBitmap: Bitmap, title: String): Uri {
    val bytes = ByteArrayOutputStream()
    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, imageBitmap, title, null)
    return Uri.parse(path)
}