package com.lebentech.lebentechtorniquetes.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.lebentech.lebentechtorniquetes.utils.LogUtils
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.views.activities.CameraActivity
import java.util.*

/**
 * Created by Gerardo Garzon on 05/01/23.
 */
class LifeTestRepository {
    fun sendLifeTest(context: Context) {
        val lastRecognitionDate = CameraActivity.lastRecognition
        val batteryLevel = Utils.batteryLevel(context)
        val isBatteryCharging = Utils.isBatteryCharging(context)
        val ramUsage = Utils.getRamUsage(context)
        val cpuTemperature = Utils.cpuTemperature()

        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Has not permissions for location services
            // 19.304933, -99.203779 GS Coordinates

            // send life test with static coordinates
            sendLifeTestRequest(lastRecognitionDate, batteryLevel, isBatteryCharging, ramUsage, cpuTemperature, 19.304933, -99.203779)
        } else {
            // Has permissions for location services
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val longitude = location.longitude
                    val latitude = location.latitude

                    // send life test with the given coordinates
                    sendLifeTestRequest(
                        lastRecognitionDate,
                        batteryLevel,
                        isBatteryCharging,
                        ramUsage,
                        cpuTemperature,
                        longitude,
                        latitude
                    )
                }
            }
                .addOnFailureListener {
                    // Send life test with static coordinates
                    sendLifeTestRequest(
                        lastRecognitionDate,
                        batteryLevel,
                        isBatteryCharging,
                        ramUsage,
                        cpuTemperature,
                        19.304933,
                        -99.203779
                    )
                }
        }
    }

    private fun sendLifeTestRequest(lastRecognition: Date, batteryLevel: Int, isBatteryCharging: Boolean,
                                    ramUsage: Long, cpuTemperature: Float, longitude: Double, latitude: Double) {
        LogUtils.printLog("LIFE_TEST", "Life test sent")
    }
}