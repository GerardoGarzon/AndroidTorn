/**
 * Created by Gerardo Garzon on 22/12/22.
 */
package com.lebentech.lebentechtorniquetes.utils

import com.lebentech.lebentechtorniquetes.R

class Constants {
    companion object {
        // Activities tags
        const val LAUNCH_ACTIVITY = "LAUNCH_SCREEN_ACTIVITY"
        const val CAMERA_ACTIVITY = "CAMERA_ACTIVITY"
        const val LOGIN_ACTIVITY = "LOGIN_ACTIVITY"
        const val SEDE_ACTIVITY = "SEDE_ACTIVITY"
        const val SCANNER_ACTIVITY = "SCANNER_ACTIVITY"
        const val STATUS_ACTIVITY = "STATUS_ACTIVITY"
        const val DETECTION_TAG = "FACE_ANALYZER"
        const val DIALOG_TAG = "WELCOME_DIALOG"

        // Alerts texts
        const val PERMISSIONS_ALERT_TITLE = "Permisos negados"
        const val PERMISSIONS_LOCATION_ALERT_BODY = "Accede a las configuraciones y otorga los permisos de ubicación a la Lebentech"
        const val PERMISSIONS_NOTIFICATION_ALERT_BODY = "Accede a las configuraciones y otorga los permisos de notificaciones a la Lebentech"
        const val PERMISSIONS_CAMERA_ALERT_BODY = "Accede a las configuraciones y otorga los permisos de acceso a la camara a la Lebentech"
        const val PERMISSIONS_ALERT_OPTION = "Ok"

        // LOGS
        const val LOG_TAG = "LB"

        // PREFERENCES
        const val LEBENTECH_PREFERENCES_KEY = "LebentechPreferences"
        const val TOKEN_KEY = "JWT_TOKEN"
        const val TOKEN_REFRESH_KEY = "TOKEN_REFRESH"
        const val ID_SEDE_KEY = "SEDE_KEY"
        const val SEDE_PRIORITY_ID = "PRIORITY_KEY"
        const val SEDE_NAME_KEY = "SEDE_NAME_KEY"
        const val SERVER_ERROR_KEY = "SERVER_ERROR_KEY"

        // LOGIN PROCESS
        const val NOT_LOGGED_IN = 0
        const val LOGIN_PROCESS = 1
        const val ERROR_IN_LOGIN = -1
        const val LOGGED_IN = 2
        const val UNAUTHORIZED_DEVICE = -2

        // APPLICATION SETTINGS
        const val ENDPOINT_KEY = "ENDPOINT_KEY"
        const val EMPTY_STRING = ""

        // REQUEST CONSTANTS
        const val DEVICE_ORIGIN = "Android"

        // SECRET KEY
        const val SECRET_KEY = "L3b3NT3cH2023!"
        const val APP_NAME = "Lebentech"

        // APP STATUS INDEX
        const val APP_IN_MAINTENANCE = 0
        const val APP_NETWORK_ERROR = 1
        const val APP_DOOR_SYSTEM_ERROR = 2
        const val APP_SERVER_ERROR = 3

        // RECOGNITION STATE
        const val NO_FACE_IN_FRONT = 0
        const val FACE_TOO_CLOSE = 1
        const val FACE_TOO_FAR = 2
        const val FACE_DETECTED = 3
        const val PHOTO_TAKEN = 4
        const val CORRECT_DETECTION = 5
        const val NO_DETECTION_COINCIDENCES = -1
        const val ERROR_IN_DETECTION = -2

        // ANIMATIONS
        val ANIMATIONS_VALUE = arrayOf(
            R.raw.b_1,
            R.raw.b_2,
            R.raw.b_3,
            R.raw.b_4,
            R.raw.b_5,
            R.raw.b_6,
            R.raw.b_7,
            R.raw.b_8,
            R.raw.b_9,
            R.raw.b_10,
            R.raw.b_11
        )

        // SERVER ERROR FLAG
        const val SERVER_ERROR_ON = 1
        const val SERVER_ERROR_OFF = 0
    }
}