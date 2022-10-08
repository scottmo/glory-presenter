package com.scottscmo.google

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.slides.v1.Slides
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest
import com.google.api.services.slides.v1.model.Page
import com.google.api.services.slides.v1.model.Request
import com.scottscmo.Config
import com.scottscmo.google.slides.Action
import com.scottscmo.google.slides.DefaultSlideConfig
import com.scottscmo.google.slides.RequestBuilder
import com.scottscmo.model.bible.BibleModel
import com.scottscmo.model.bible.BibleReference
import com.scottscmo.model.song.Song
import com.scottscmo.util.StringUtils
import java.lang.Integer.min

class GoogleService {
    private val appName = "Glory Presenter"

    private val slidesApi: Slides by lazy {
        val auth = AuthClient.instance
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        Slides.Builder(httpTransport, auth.jsonFactory, auth.getCredentials(httpTransport))
            .setApplicationName(appName)
            .build()
    }

    private val driveApi: Drive by lazy {
        val auth = AuthClient.instance
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

        Drive.Builder(httpTransport, auth.jsonFactory, auth.getCredentials(httpTransport))
            .setApplicationName(appName)
            .build()
    }

    fun copyPresentation(title: String, folderId: String, templatePresentationId: String): String? {
        var presentation: File? = null
        try {
            val fileMetadata = File().apply {
                name = title
                parents = listOf(folderId)
            }
            presentation = driveApi.files().copy(templatePresentationId, fileMetadata).execute()
        } catch (e: GoogleJsonResponseException) {
            val error = e.details
            if (error.code == 404) {
                System.out.printf("Presentation not found with id '%s'.\n", templatePresentationId)
            } else {
                throw e
            }
        }
        return presentation?.id
    }

    fun getSlides(presentationId: String): List<Page> {
        val response = slidesApi.presentations()[presentationId].execute()
        return response.slides
    }

    fun updateSlides(presentationId: String, updateRequests: List<Request>): Boolean {
        if (updateRequests.isEmpty()) return false

        val batchSize = 500
        var startIndex = 0
        while (startIndex < updateRequests.size) {
            val endIndex = min(startIndex + batchSize, updateRequests.size)
            val body = BatchUpdatePresentationRequest().setRequests(updateRequests.subList(startIndex, endIndex))
            slidesApi.presentations().batchUpdate(presentationId, body).execute()
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

    fun insertSong(presentationId: String, song: Song, slideInsertIndex: Int) {
        val slideConfig = Config.get().googleSlideConfig
        val defaultTextConfig = slideConfig.textConfigs[slideConfig.defaultTextConfig]!!

        val requestBuilder = RequestBuilder()
        var slideIndex = slideInsertIndex

        // title
        val textBoxId = requestBuilder.createSlideWithFullText(slideIndex++)
        requestBuilder.insertText(textBoxId, song.title, slideConfig.paragraph, defaultTextConfig)

        // lyrics
        song.order.forEach { sectionName ->
            val section = song.getSection(sectionName)
            requireNotNull(section) { "Unable to find section $sectionName" }

            val slideId = requestBuilder.createSlide(slideIndex++)

            // section text
            val sectionTextBoxId = requestBuilder.getPlaceHolderId(slideId)
            requestBuilder.resizeToFullPage(sectionTextBoxId)
            requestBuilder.insertText(sectionTextBoxId, section.text, slideConfig)

            // footer
            val footerTitleBoxId = requestBuilder.createTextBox(slideId,
                DefaultSlideConfig.SLIDE_W, DefaultSlideConfig.FOOTER_TITLE_SIZE * 2,
                0.0,
                DefaultSlideConfig.FOOTER_TITLE_Y
            )
            val footerTextConfig = defaultTextConfig.copy(
                fontSize = DefaultSlideConfig.FOOTER_TITLE_SIZE
            )
            requestBuilder.insertText(footerTitleBoxId, song.title, slideConfig.paragraph, footerTextConfig)
        }

        updateSlides(presentationId, requestBuilder.build())
    }

    fun generateSlides(presentationId: String, actions: List<Action>) {
        actions.reversed().forEach {
            if (it.type == "bible") {
                insertBibleText(presentationId, BibleReference(it.input), it.index)
            } else if (it.type == "hymn") {

            }
        }
    }
}
