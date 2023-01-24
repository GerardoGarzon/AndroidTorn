/**
 * Created by Gerardo Garzon on 27/12/22.
 */
package com.lebentech.lebentechtorniquetes.managers

import com.google.gson.GsonBuilder
import com.lebentech.lebentechtorniquetes.LebentechApplication
import okhttp3.Request
import java.io.*
import java.util.*


class WriterManager {

    fun createErrorLog(request: Request, response: okhttp3.Response, timeStart: Long, timeFinish: Long) {
        val organizationName = "Lebentech"
        val projectName = "Torniquetes"
        val context = LebentechApplication.appContext

        val mediaDirs = context.getExternalFilesDirs(null)
        if (mediaDirs.isEmpty()) {
            return
        } else {
            val organizationDir = File(mediaDirs[0], organizationName)
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
                writeTitle("Error request", logFile)
                writeLog("Time start (millis): $timeStart", logFile)
                writeLog("Time end (millis): $timeFinish", logFile)
                writeLog("Request path: ${request.url().url().path}", logFile)
                writeLog("Method: ${request.method()}", logFile)

                // RESPONSE
                writeTitle("Response", logFile)
                writeLog("Response code: ${response.code()}", logFile)

                if (response.body() != null) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val json = gson.toJson(response.body())

                    writeLog("Response body: $json", logFile)
                } else {
                    writeLog("Response body: No body response", logFile)
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