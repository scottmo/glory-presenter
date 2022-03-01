package com.scottscmo.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.slides.v1.SlidesScopes
import com.scottscmo.Config
import com.scottscmo.util.Cryptor
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

const val API_CONFIG_DIR = "google_api"
const val CREDENTIALS_FILE_PATH = "${API_CONFIG_DIR}/client.info"

class AuthClient {
    companion object {
        val instance = AuthClient()
    }

    private val tokensDirPath = API_CONFIG_DIR

    private val scopes = listOf(SlidesScopes.PRESENTATIONS)

    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        require(Config["clientInfoKey"].isNotEmpty()) { "clientInfoKey is missing from config.yaml!" }

        // Load client secrets.
        val credentials = Cryptor.decrypt(Config.getRelativePath(CREDENTIALS_FILE_PATH), Config["clientInfoKey"])
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(credentials.inputStream()))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, jsonFactory, clientSecrets, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(File(Config.getRelativePath(tokensDirPath))))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}
