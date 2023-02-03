/**
 * Created by Gerardo Garzon on 30/12/22.
 */
package com.lebentech.lebentechtorniquetes.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class FileManagerService {
    companion object {
        /**
         * It will delete records older than 15 days ago from the internal storage
         */
        @SuppressLint("SimpleDateFormat")
        fun deleteOldRecords(context: Context) {
            val mediaDirs = context.getExternalFilesDirs(null)
            val mediaDir = mediaDirs[0]
            val rootPath = File(mediaDir, "Lebentech")
            val appPath = File(rootPath, "Torniquetes")
            val containerPath = File(appPath, "Requests")
            val list = containerPath.listFiles()
            if (list != null) {
                for (tmpFile in list) {
                    val modified = Date(tmpFile.lastModified())
                    val today = Date()
                    val modifiedString = SimpleDateFormat("yyyy-MM-dd").format(modified)
                    val todayString = SimpleDateFormat("yyyy-MM-dd").format(today)
                    val difference = daysBetweenDates(modifiedString, todayString)
                    if (difference >= 0) {
                        tmpFile.delete()
                    }
                }
            }
        }

        /**
         * Obtain the difference in days between two dates, it is consumed by the delete old records
         * if the difference is 15 days it will delete the log
         */
        @SuppressLint("SimpleDateFormat")
        fun daysBetweenDates(date1: String, date2: String): Int {
            var diffDays = 0
            try {
                val dates = SimpleDateFormat("yyyy-MM-dd")
                val startDate = dates.parse(date1)
                val endDate = dates.parse(date2)
                val diff = endDate.time - startDate.time
                diffDays = (diff / (24 * 60 * 60 * 1000)).toInt()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return abs(diffDays)
        }
    }
}