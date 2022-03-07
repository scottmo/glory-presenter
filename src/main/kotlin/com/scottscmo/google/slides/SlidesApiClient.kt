package com.scottscmo.google.slides

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.slides.v1.Slides
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest
import com.google.api.services.slides.v1.model.Page
import com.google.api.services.slides.v1.model.Request
import com.scottscmo.google.AuthClient
import java.lang.Integer.min


class SlidesApiClient {
    private val appName = "Worship Service Tool"

    private val service: Slides by lazy {
        val auth = AuthClient.instance
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        Slides.Builder(httpTransport, auth.jsonFactory, auth.getCredentials(httpTransport))
            .setApplicationName(appName)
            .build()
    }

    fun getSlides(presentationId: String): List<Page> {
        val response = service.presentations()[presentationId].execute()
        return response.slides
    }

    fun updateSlides(presentationId: String, updateRequests: List<Request>): Boolean {
        if (updateRequests.isEmpty()) return false

        val batchSize = 500
        var startIndex = 0
        while (startIndex < updateRequests.size) {
            val endIndex = min(startIndex + batchSize, updateRequests.size)
            val body = BatchUpdatePresentationRequest().setRequests(updateRequests.subList(startIndex, endIndex))
            service.presentations().batchUpdate(presentationId, body).execute()
            startIndex = endIndex
        }
        return true
    }
}
