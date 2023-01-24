/**
 * Created by Gerardo Garzon on 26/12/22.
 */

package com.lebentech.lebentechtorniquetes.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.lebentech.lebentechtorniquetes.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and


class Utils {
    companion object {
        /**
         * Saves the value on the shared preferences using private mode to keep it save
         */
        @SuppressLint("CommitPrefEdits")
        fun setPrivatePreferences(key: String, value: String, context: Context) {
            val editor: SharedPreferences.Editor
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.LEBENTECH_PREFERENCES_KEY, Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()
            editor.putString(key, value)
            editor.apply()
        }

        @SuppressLint("CommitPrefEdits")
        fun setPrivatePreferences(key: String, value: Int, context: Context) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.LEBENTECH_PREFERENCES_KEY, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        /**
         * Return the value from the shared preferences related to the given key
         */
        fun getPrivatePreferences(context: Context, key: String): String {
            val sharedPreferences = context.getSharedPreferences(Constants.LEBENTECH_PREFERENCES_KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getString(key, Constants.EMPTY_STRING) ?: ""
        }

        fun getPrivatePreferences(context: Context, key: String, defaultValue: Int): Int {
            val sharedPreferences = context.getSharedPreferences(Constants.LEBENTECH_PREFERENCES_KEY, Context.MODE_PRIVATE)
            return sharedPreferences.getInt(key, defaultValue)
        }

        /**
         * Return the Android ID needed to call the APIs to verify that the device is authorized to make
         * calls
         */
        fun getAndroidID(context: Context): String {
            // return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            return "7ef11e3b9c538754"
        }

        /**
         * Create the snack-bar on the given view, it will be displayed with the message and the color
         * from the parameters
         */
        @SuppressLint("ResourceType")
        fun createSnackBar(context: Context?, view: View?, body: String?, @ColorInt color: Int) {
            val snackbar = Snackbar.make(view!!, body!!, Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(ContextCompat.getColor(context!!, color))
            snackbar.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            snackbar.show()
        }

        /**
         * Encode the given string in SHA 256 to protect its content
         */
        fun getSha256(secretKey: String): String? {
            return try {
                val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
                digest.reset()
                bin2hex(digest.digest(secretKey.toByteArray()))
            } catch (ex: Exception) {
                null
            }
        }

        /**
         * Convert the byte array data to hexadecimal String
         */
        private fun bin2hex(data: ByteArray): String {
            val hex = StringBuilder(data.size * 2)
            for (b in data) hex.append(String.format("%02x", b and 0xFF.toByte()))
            return hex.toString()
        }

        /**
         * Obtain the string from the resources using its identifier name
         */
        @SuppressLint("DiscouragedApi")
        fun getStringByIdName(context: Context, idName: String?): String {
            val res = context.resources
            return res.getString(res.getIdentifier(idName, "string", context.packageName))
        }

        /**
         * Return the decoded string from the base64 string, it is used to decode the read QR to get the
         * server URL
         */
        fun getBase64String(base64String: String): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val base64Decoder = Base64.getDecoder().decode(base64String)
                    String(base64Decoder, StandardCharsets.UTF_8)
                } catch (ex: java.lang.IllegalArgumentException) {
                    Constants.EMPTY_STRING
                }
            } else {
                Constants.EMPTY_STRING
            }
        }

        /**
         * Return the ip from the device
         */
        fun getIpOrigin(context: Context): String {
            val wifiMan = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInf = wifiMan.connectionInfo
            val ipAddress = wifiInf.ipAddress
            val ip = String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
            return "10.50.89.4"
        }

        /**
         * Converts an image to signed byte array
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun convertImageToByteArray(path: String): ByteArray {
            return try {
                Files.readAllBytes(Paths.get(path))
            } catch (e: IOException) {
                byteArrayOf()
            }
        }

        fun batteryLevel(context: Context): Int {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        fun isBatteryCharging(context: Context): Boolean {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, ifilter)
            val status = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }

        fun getRamUsage(context: Context): Long {
            val memoryInfo = ActivityManager.MemoryInfo()
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memoryInfo)
            val nativeHeapSize = memoryInfo.totalMem
            val nativeHeapFreeSize = memoryInfo.availMem
            val usedMemInBytes = nativeHeapSize - nativeHeapFreeSize
            val usedMemInPercentage = usedMemInBytes * 100 / nativeHeapSize
            Log.d("AppLog", "total:${nativeHeapSize} " +
                    "free:${nativeHeapFreeSize} " +
                    "used:${usedMemInBytes} ($usedMemInPercentage%)")

            return usedMemInPercentage
        }

        fun cpuTemperature(): Float {
            val process: Process
            return try {
                process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
                process.waitFor()
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val line: String = reader.readLine()
                run {
                    val temp = line.toFloat()
                    temp / 1000.0f
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0.0f
            }
        }
    }
}