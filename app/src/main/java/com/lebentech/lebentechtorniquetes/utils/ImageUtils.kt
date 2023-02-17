package com.lebentech.lebentechtorniquetes.utils

import android.graphics.*
import android.media.Image
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Gerardo Garzon on 02/01/23.
 */
class ImageUtils {
    companion object {

        fun getRotatedImageFile(photoFile: File): File {
            val exifInterface = ExifInterface(photoFile.absolutePath)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val bmp: Bitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotateImage(bitmap, 90)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotateImage(bitmap, 180)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotateImage(bitmap, 270)
                }
                else -> {
                    bitmap
                }
            }

            return saveImage(bmp)
        }

        private fun saveImage(image: Bitmap): File {
            val filename = getImageFilePath()
            val imageFile = File(filename)
            val os = BufferedOutputStream(FileOutputStream(imageFile))

            image.compress(Bitmap.CompressFormat.JPEG, 75, os)
            os.close()

            return imageFile
        }

        private fun getImageFilePath(): String {
            val externalMediaDirs = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            )
            val file = File(externalMediaDirs, Constants.PHOTO_TAKEN_NAME)
            return file.absolutePath
        }

        fun toBitmap(image: Image): Bitmap {
            val yBuffer = image.planes[0].buffer // Y
            val vuBuffer = image.planes[2].buffer // VU

            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()

            val nv21 = ByteArray(ySize + vuSize)

            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        fun mirrorBitmap(bitmap: Bitmap): Bitmap? {
            val matrix = Matrix()
            matrix.preScale(-1.0f, 1.0f)
            return Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height, matrix, false
            )
        }

        private fun rotateImage(bitmap: Bitmap, degree: Int): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            return rotatedBitmap
        }
    }
}