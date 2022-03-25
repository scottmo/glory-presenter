package com.scottscmo.google.slides

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.slides.v1.Slides
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest
import com.google.api.services.slides.v1.model.Page
import com.google.api.services.slides.v1.model.Request
import com.scottscmo.Config
import com.scottscmo.google.AuthClient
import com.scottscmo.model.bible.BibleModel
import com.scottscmo.model.bible.BibleReference
import com.scottscmo.util.StringUtils
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

    fun setDefaultTitleText(presentationId: String) {
        val requestBuilder = RequestBuilder()
        val slides = getSlides(presentationId)
        slides.forEach { slide ->
            requestBuilder.setDefaultTitleText(slide)
        }
        updateSlides(presentationId, requestBuilder.build())
    }

    fun setBaseFont(presentationId: String) {
        val requestBuilder = RequestBuilder()
        val slides = getSlides(presentationId)
        slides.forEach { slide ->
            requestBuilder.setBaseFont(slide, Config.get().googleSlideConfig.textConfigs)
        }
        updateSlides(presentationId, requestBuilder.build())
    }

    fun insertBibleText(presentationId: String, bibleRef: BibleReference, slideIndex: Int) {
        val slideConfig = Config.get().googleSlideConfig
        val bibleVersionToTextConfig = slideConfig.bibleVersionToTextConfig

        // query bible data
        val bookNames = BibleModel.get().getBookNames(bibleRef.book)
        requireNotNull(bookNames) { "${bibleRef.book} does not exist!" }

        val bibleVerses = BibleModel.get().getBibleVerses(bibleRef)
        require(bibleRef.versions.all { version -> bibleVerses.containsKey(version) })

        // building slide texts (version, text)
        val slideTexts = mutableListOf<Pair<String, String>>()

        // title
        val queriedBookNames = bibleRef.versions
            .map { version -> bookNames.getOrDefault(version, "") }
            .filter { it.isNotEmpty() }
        val titleText = queriedBookNames.joinToString("\n") + "\n" + bibleRef.ranges.joinToString(";")
        slideTexts.add(Pair(bibleRef.versions[0], titleText))

        // verses
        val numVerses = bibleVerses[bibleRef.versions[0]]!!.size
        for (i in 0 until numVerses) {
            bibleRef.versions.forEach { version ->
                val groupName = bibleVersionToTextConfig[version]

                val textConfig = slideConfig.textConfigs[groupName]
                val verse = bibleVerses[version]!![i]
                val verseTexts = StringUtils.distributeTextToBlocks("${verse.index} ${verse.text}",
                    textConfig!!.numberOfCharactersPerLine,
                    textConfig.numberOfLinesPerSlide)
                verseTexts.forEach { verseText ->
                    slideTexts.add(Pair(version, verseText))
                }
            }
        }

        // create slide update requests from slide texts
        val requestBuilder = RequestBuilder()
        slideTexts.filter { it.second.isNotEmpty() }.reversed().forEach { (version, text) ->
            val lang = bibleVersionToTextConfig[version]
            val textConfig = slideConfig.textConfigs[lang]
            requireNotNull(textConfig) { "No matching text config for $version version" }

            val titleId = requestBuilder.createSlideWithFullText(slideIndex)
            requestBuilder.insertText(titleId, text, slideConfig.paragraph, textConfig)
        }
        updateSlides(presentationId, requestBuilder.build())
    }

    fun insertSong(presentationId: String, title: String, lyrics: List<String>, insertionIndex: Int) {
        val requests = mutableListOf<Request>()

        // title



        updateSlides(presentationId, requests)
    }

    private fun insertSongTitle(title: String, slideIndex: Int): List<Request> {
        val slideConfig = Config.get().googleSlideConfig

        val requestBuilder = RequestBuilder()
        val titleId = requestBuilder.createSlideWithFullText(slideIndex)
        requestBuilder.insertText(titleId, title, slideConfig.paragraph,
                slideConfig.textConfigs[slideConfig.defaultTextConfig]!!)

        return requestBuilder.build()
    }
}
