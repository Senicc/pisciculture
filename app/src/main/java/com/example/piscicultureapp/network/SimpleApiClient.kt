package com.example.piscicultureapp.network

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object SimpleApiClient {

    private const val TIMEOUT_MS = 8000

    fun getJson(path: String): JSONObject? {
        return try {
            val url = URL(MySqlApiConfig.baseUrl + path)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            readResponse(conn)
        } catch (_: Exception) {
            null
        }
    }

    fun postJson(path: String, body: JSONObject): JSONObject? {
        return try {
            val url = URL(MySqlApiConfig.baseUrl + path)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
            readResponse(conn)
        } catch (_: Exception) {
            null
        }
    }

    private fun readResponse(conn: HttpURLConnection): JSONObject? {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream ?: return null
        val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        return try {
            JSONObject(text)
        } catch (_: Exception) {
            null
        }
    }
}
