/**
 * Created by Gerardo Garzon on 27/12/22.
 */
package com.lebentech.lebentechtorniquetes.managers

import android.os.Environment
import com.google.gson.GsonBuilder
import com.lebentech.lebentechtorniquetes.LebentechApplication
import okhttp3.Request
import java.io.*
import java.util.*


class WriterManager {

    /**
     * Create an error log in the Android application data folder
     * It will contain the path of the request and the information from the error response
     */
    fun createErrorLog(request: Request, response: okhttp3.Response?, timeStart: Long, timeFinish: Long, exception: String?) {
        val organizationName = "Lebentech"
        val projectName = "Torniquetes"

        val mediaDirs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (mediaDirs == null) {
            return
        } else {
            val organizationDir = File(mediaDirs, organizationName)
            val projectDir = File(organizationDir, projectName)
            val requestsLogsDir = File(projectDir, "Requests")

            val fileAbsolutePath = requestsLogsDir.absolutePath + File.separator
            val requestPath = request.url().pathSegments().size
            val fileName = request.url().pathSegments()[requestPath - 1]

            if (!requestsLogsDir.exists()) {
                requestsLogsDir.mkdirs()
            }

            val logFile = File("$fileAbsolutePath$fileName" + "_$timeFinish.txt")
            if (logFile.exists()) {
                logFile.delete()
            }

            try {
                logFile.createNewFile()

                // REQUEST
                writeTitle("Request", logFile)
                writeLog("Time start (millis): $timeStart", logFile)
                writeLog("Time end (millis): $timeFinish", logFile)
                writeLog("Request path: ${request.url().url().path}", logFile)
                writeLog("Method: ${request.method()}", logFile)

                // RESPONSE
                writeTitle("Error Response", logFile)
                if (response != null) {
                    writeLog("Response code: ${response.code()}", logFile)

                    if (response.body() != null) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val json = gson.toJson(response.body())

                        writeLog("Response body: $json", logFile)
                    } else {
                        writeLog("Response body: No body response", logFile)
                    }
                } else {
                    writeLog("Response exception: $exception", logFile)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun writeLog(line: String, file: File) {
        try {
            val contentFile = readContent(file)
            val fileWriter = FileWriter(file.absolutePath)
            fileWriter.write(contentFile)
            fileWriter.write("\n")
            fileWriter.write(line)
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeTitle(title: String, file: File) {
        try {
            val contentFile = readContent(file)
            val fileWriter = FileWriter(file.absolutePath)
            fileWriter.write(contentFile)
            fileWriter.write("\n")
            fileWriter.write("\n")
            fileWriter.write("<<------------" + title.uppercase(Locale.getDefault()) + "------------>>")
            fileWriter.write("\n")
            fileWriter.write("\n")
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun readContent(file: File?): String {
        val inputStream: InputStream = FileInputStream(file)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var isFinished = false
        while (!isFinished) {
            val line = bufferedReader.readLine()
            if (line == null) {
                isFinished = true
            } else {
                stringBuilder.append(line)
                stringBuilder.append(System.getProperty("line.separator"))
            }
        }
        bufferedReader.close()
        inputStream.close()
        return stringBuilder.toString()
    }
}