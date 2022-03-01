package com.scottscmo.google.slides

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.slides.v1.Slides
import com.google.api.services.slides.v1.model.Page
import com.scottscmo.google.AuthClient

class SlidesApiClient {
    private val appName = "Worship Service Tool"
    private val service: Slides

    init {
        val auth = AuthClient.instance
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        service = Slides.Builder(httpTransport, auth.jsonFactory, auth.getCredentials(httpTransport))
            .setApplicationName(appName)
            .build()
    }

    fun getSlides(presentationId: String): List<Page>? {
        val response = service.presentations()[presentationId].execute()
        return response.slides;
    }
}
