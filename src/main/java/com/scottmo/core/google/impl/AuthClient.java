package com.scottmo.core.google.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

public class AuthClient {
    private static final List<String> SCOPES = List.of(SlidesScopes.PRESENTATIONS);

    /**
     * Creates a GoogleCredentials object with the correct OAuth2 authorization for
     * the service account that calls the reseller API. The service endpoint invokes this method
     * when setting up a new service instance.
     *
     * @return an authorized GoogleCredentials object.
     * @throws IOException
     */
    public HttpRequestInitializer getRequestInitializer() throws IOException {
        InputStream in = AuthClient.class.getResourceAsStream("credentials.json");
        GoogleCredentials credential = ServiceAccountCredentials.fromStream(in).createScoped(SCOPES);
        return new HttpCredentialsAdapter(credential);
    }
}
