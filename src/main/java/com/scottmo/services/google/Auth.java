package com.scottmo.services.google;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

public class Auth {
    private static final String APPLICATION_NAME = "Google Slides API Java Quickstart";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SlidesScopes.PRESENTATIONS_READONLY);

    // Global shared instances
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates a GoogleCredentials object with the correct OAuth2 authorization for
     * the service
     * account that calls the reseller API. The service endpoint invokes this method
     * when setting up a
     * new service instance.
     *
     * @return an authorized GoogleCredentials object.
     * @throws IOException
     */
    public static GoogleCredentials authorize() throws IOException {
        // Load service account key.
        InputStream in = Auth.class.getResourceAsStream("/credentials.json");

        // Create the credential scoped to the zero-touch enrollment customer APIs.
        GoogleCredentials credential = ServiceAccountCredentials.fromStream(in).createScoped(SCOPES);
        return credential;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        GoogleCredentials credential = authorize();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
        Slides service = new Slides.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Prints the number of slides and elements in a sample presentation:
        // https://docs.google.com/presentation/d/1EAYk18WDjIG-zp_0vLm3CsfQh_i8eXc67Jo2O9C6Vuc/edit
        String presentationId = "1dlTGZvAuOmi3NCRDIYNO8wUhPbUhuZCkc-BfdKnglkk";
        Presentation response = service.presentations().get(presentationId).execute();
        List<Page> slides = response.getSlides();

        System.out.printf("The presentation contains %s slides:\n", slides.size());
        for (int i = 0; i < slides.size(); ++i) {
            System.out.printf("- Slide #%s contains %s elements.\n", i + 1,
                    slides.get(i).getPageElements().size());
        }
    }
}
