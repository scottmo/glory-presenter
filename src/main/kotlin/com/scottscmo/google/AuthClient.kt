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
import com.google.api.services.drive.DriveScopes
import com.google.api.services.slides.v1.SlidesScopes
import com.scottscmo.Config
import com.scottscmo.util.Cryptor
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class AuthClient {
    companion object {
        val instance = AuthClient()
    }

    private val tokensDirPath = Config.GOOGLE_API_DIR

    private val scopes = listOf(SlidesScopes.PRESENTATIONS, DriveScopes.DRIVE)

    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        require(Config.get().clientInfoKey.isNotEmpty()) { "clientInfoKey is missing from config.json!" }

        // Load client secrets.
        val credentials = Cryptor.decrypt(Config.getRelativePath(Config.GOOGLE_API_CREDENTIALS_PATH), Config.get().clientInfoKey)
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
