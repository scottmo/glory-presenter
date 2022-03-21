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
        val slides = getSlides(presentationId)
        val requests = slides.filter { it.pageElements.isNotEmpty() }
            .map { Actions.setDefaultTitleText(it) }
            .flatten()
        updateSlides(presentationId, requests)
    }

    fun setBaseFont(presentationId: String) {
        val slides = getSlides(presentationId)
        val requests = slides.filter { it.pageElements.isNotEmpty() }
            .map { Actions.setBaseFont(it, Config.get().googleSlideConfig.text) }
            .flatten()
        updateSlides(presentationId, requests)
    }

    fun insertBibleText(presentationId: String, bibleRef: BibleReference, insertionIndex: Int) {
        val slideConfig = Config.get().googleSlideConfig
        val bibleVersionToLanguage = Config.get().bibleVersionToLanguage

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
                val lang = bibleVersionToLanguage[version]

                val textConfig = slideConfig.text[lang]
                val verse = bibleVerses[version]!![i]
                val verseTexts = Util.distributeTextToSlides("${verse.index} ${verse.text}",
                    textConfig!!.numberOfCharactersPerLine,
                    textConfig.numberOfLinesPerSlide)
                verseTexts.forEach { verseText ->
                    slideTexts.add(Pair(version, verseText))
                }
            }
        }

        // create slide update requests from slide texts
        val requests = mutableListOf<Request>()
        slideTexts.filter { it.second.isNotEmpty() }.reversed().forEach { (version, text) ->
            val lang = bibleVersionToLanguage[version]
            val textConfig = slideConfig.text[lang]
            requireNotNull(textConfig) { "No matching text config for $version version" }

            val slideId = Util.generateObjectId(DefaultSlideConfig.ID_SLIDE_PREFIX)
            val createSlideReq = Actions.createSlide(slideId, insertionIndex)

            val titleId = createSlideReq.createSlide.placeholderIdMappings[0].objectId
            val resizeReq = Actions.resizeToFullPage(titleId)

            val insertReqs = Actions.insertText(titleId, text, slideConfig.paragraph, textConfig)

            requests.add(createSlideReq)
            requests.add(resizeReq)
            requests.addAll(insertReqs)
        }
        updateSlides(presentationId, requests)
    }
}
