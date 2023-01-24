/**
 * Created by Gerardo Garzon on 22/12/22.
 */
package com.lebentech.lebentechtorniquetes.repositories

import com.lebentech.lebentechtorniquetes.utils.Constants

class AppStatusRepository {
    private val statusListString = ArrayList<String>()
    private val statusListIcon = ArrayList<String>()

    init {
        statusListIcon.add("maintenance")
        statusListIcon.add("wifi_connection")
        statusListIcon.add("door")
        statusListIcon.add("server")
        statusListString.add("lbl_error_maintenance")
        statusListString.add("lbl_error_network")
        statusListString.add("lbl_error_open_system")
        statusListString.add("lbl_error_server")
    }

    fun getAppStatus(index: Int): Pair<String, String> {
        return if (index < 0 || index > statusListString.size) {
            Pair(Constants.EMPTY_STRING, Constants.EMPTY_STRING)
        } else {
            Pair(statusListIcon[index], statusListString[index])
        }
    }
}