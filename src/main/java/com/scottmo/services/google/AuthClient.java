package com.scottmo.services.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.slides.v1.SlidesScopes;
import com.scottmo.services.config.AppConfigProvider;
import com.scottmo.services.security.CipherService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

final class AuthClient {
    private static final String TOKENS_DIR_PATH = AppConfigProvider.GOOGLE_API_DIR;
    private static final List<String> SCOPES = List.of(SlidesScopes.PRESENTATIONS, DriveScopes.DRIVE);

    /**
     * Creates an authorized Credential object.
     */
    public Credential getCredentials(NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets;
        try {
            byte[] credentials = CipherService.decrypt(AppConfigProvider.getRelativePath(AppConfigProvider.GOOGLE_API_CREDENTIALS_PATH), AppConfigProvider.get().clientInfoKey());
            clientSecrets = GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(new ByteArrayInputStream(credentials)));
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Unable to load encrypted Google API credentials! " + e.getMessage());
        }

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GsonFactory.getDefaultInstance(), clientSecrets, SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File(AppConfigProvider.getRelativePath(TOKENS_DIR_PATH))))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}