package com.scottscmo.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Request;
import com.scottscmo.Config;
import com.scottscmo.bibleReference.VerseRange;
import com.scottscmo.model.bible.BibleModel;
import com.scottscmo.bibleReference.BibleReference;
import com.scottscmo.model.bible.BibleVerse;
import com.scottscmo.model.song.Section;
import com.scottscmo.model.song.Song;
import com.scottscmo.model.song.SongLoader;
import com.scottscmo.util.StringUtils;
import com.scottscmo.config.SlideConfig;
import com.scottscmo.config.TextConfig;
import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GoogleSlidesService {
    private static final String appName = "Glory Presenter";

    private Slides _slidesApi;

    private Slides getSlidesApi() {
        if (_slidesApi == null) {
            AuthClient auth = new AuthClient();
            try {
                NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                _slidesApi = new Slides.Builder(httpTransport, GsonFactory.getDefaultInstance(), auth.getCredentials(httpTransport))
                        .setApplicationName(appName)
                        .build();
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException("Unable to connect to Google Slides API!");
            }
        }
        return _slidesApi;
    }

    private Drive _driveApi;

    private Drive getDriveApi() {
        if (_driveApi == null) {
            AuthClient auth = new AuthClient();
            try {
                NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                _driveApi = new Drive.Builder(httpTransport, GsonFactory.getDefaultInstance(), auth.getCredentials(httpTransport))
                        .setApplicationName(appName)
                        .build();
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException("Unable to connect to Google Slides API!");
            }
        }
        return _driveApi;
    }

    public String copyPresentation(String title, String folderId, String templatePresentationId) {
        try {
            File fileMetadata = new File()
                    .setName(title)
                    .setParents(List.of(folderId));
            File presentation = getDriveApi().files().copy(templatePresentationId, fileMetadata).execute();
            return presentation.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Page> getSlides(String presentationId) throws IOException {
        return getSlidesApi().presentations().get(presentationId).execute().getSlides();
    }

    public boolean updateSlides(String presentationId, List<Request> updateRequests) throws IOException {
        if (updateRequests.isEmpty()) return false;

        int batchSize = 500;
        int startIndex = 0;
        while (startIndex < updateRequests.size()) {
            int endIndex = Math.min(startIndex + batchSize, updateRequests.size());
            BatchUpdatePresentationRequest body = new BatchUpdatePresentationRequest()
                    .setRequests(updateRequests.subList(startIndex, endIndex));
            getSlidesApi().presentations().batchUpdate(presentationId, body).execute();
            startIndex = endIndex;
        }
        return true;
    }

    public void setDefaultTitleText(String presentationId) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder();
        List<Page> slides = getSlides(presentationId);
        slides.forEach(requestBuilder::setDefaultTitleText);
        updateSlides(presentationId, requestBuilder.build());
    }

    public void setBaseFont(String presentationId) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder();
        List<Page> slides = getSlides(presentationId);
        slides.forEach(slide -> {
            requestBuilder.setBaseFont(slide, Config.get().googleSlideConfig().textConfigs());
        });
        updateSlides(presentationId, requestBuilder.build());
    }

    public void insertBibleText(String presentationId, BibleReference bibleRef, int slideIndex) throws IOException {
        SlideConfig slideConfig = Config.get().googleSlideConfig();
        Map<String, String> bibleVersionToTextConfig = slideConfig.bibleVersionToTextConfig();

        // query bible data
        Map<String, String> bookNames = BibleModel.get().getBookNames(bibleRef.getBook());
        assert bookNames != null : "${bibleRef.book} does not exist!";

        Map<String, List<BibleVerse>> bibleVerses = BibleModel.get().getBibleVerses(bibleRef);
        assert bibleRef.getVersions().stream().allMatch(bibleVerses::containsKey);

        // building slide texts (version, text)
        ArrayList<Pair<String, String>> slideTexts = new ArrayList<>();

        // title
        String queriedBookNames = bibleRef.getVersions().stream()
                .map(version -> bookNames.getOrDefault(version, ""))
                .filter(version -> !version.isEmpty())
                .collect(Collectors.joining("\n"));
        String verses = bibleRef.getRangesString();
        String titleText = queriedBookNames + "\n" + verses;
        slideTexts.add(new Pair<>(bibleRef.getVersions().get(0), titleText));

        // verses
        int numVerses = bibleVerses.get(bibleRef.getVersions().get(0)).size();
        for (int i = 0; i < numVerses; i++) {
            for (String version : bibleRef.getVersions()) {
                String groupName = bibleVersionToTextConfig.get(version);
                TextConfig textConfig = slideConfig.textConfigs().get(groupName);
                BibleVerse verse = bibleVerses.get(version).get(i);
                List<String> verseTexts = StringUtils.distributeTextToBlocks(
                        verse.index() + " " + verse.text(),
                        textConfig.numberOfCharactersPerLine(),
                        textConfig.numberOfLinesPerSlide());
                for (String verseText : verseTexts) {
                    slideTexts.add(new Pair<>(version, verseText));
                }
            }
        }

        // create slide update requests from slide texts
        RequestBuilder requestBuilder = new RequestBuilder();
        slideTexts.stream()
                .sorted(Collections.reverseOrder())
                .forEachOrdered(verseText -> {
                    String version = verseText.getKey();
                    String text = verseText.getValue();
                    String lang = bibleVersionToTextConfig.get(version);
                    TextConfig textConfig = slideConfig.textConfigs().get(lang);
                    assert textConfig != null : "No matching text config for $version version";

                    String titleId = requestBuilder.createSlideWithFullText(slideIndex);
                    requestBuilder.insertText(titleId, text, slideConfig.paragraph(), textConfig);
                });

        updateSlides(presentationId, requestBuilder.build());
    }

    public void insertSong(String presentationId, Song song, int slideInsertIndex) throws IOException {
        SlideConfig slideConfig = Config.get().googleSlideConfig();
        TextConfig defaultTextConfig = slideConfig.textConfigs().get(slideConfig.defaultTextConfig());

        RequestBuilder requestBuilder = new RequestBuilder();
        int slideIndex = slideInsertIndex;

        // title
        String textBoxId = requestBuilder.createSlideWithFullText(slideIndex++);
        requestBuilder.insertText(textBoxId, song.title(), slideConfig.paragraph(), defaultTextConfig);

        // lyrics
        for (String sectionName : song.order()) {
            Section section = song.section(sectionName);
            assert section != null : "Unable to find section $sectionName";

            String slideId = requestBuilder.createSlide(slideIndex++);

            // section text
            String sectionTextBoxId = requestBuilder.getPlaceHolderId(slideId);
            requestBuilder.resizeToFullPage(sectionTextBoxId);
            requestBuilder.insertText(sectionTextBoxId, section.text(), slideConfig);

            // footer
            String footerTitleBoxId = requestBuilder.createTextBox(slideId,
                    DefaultSlideConfig.SLIDE_W, DefaultSlideConfig.FOOTER_TITLE_SIZE * 2,
                    0.0,
                    DefaultSlideConfig.FOOTER_TITLE_Y
            );
            TextConfig footerTextConfig = new TextConfig(
                    defaultTextConfig.wordDelimiter(),
                    defaultTextConfig.fontFamily(),
                    DefaultSlideConfig.FOOTER_TITLE_SIZE,
                    defaultTextConfig.fontColor(),
                    defaultTextConfig.fontStyles(),
                    defaultTextConfig.numberOfCharactersPerLine(),
                    defaultTextConfig.numberOfLinesPerSlide());
            requestBuilder.insertText(footerTitleBoxId, song.title(), slideConfig.paragraph(), footerTextConfig);
        }
        updateSlides(presentationId, requestBuilder.build());
    }

    private void insertSong(String presentationId, String songTitle, int slideIndex) throws IOException {
        Song song = SongLoader.getSong(Config.getRelativePath(Config.SONG_SLIDES_DIR), songTitle);
        if (song != null) {
            insertSong(presentationId, song, slideIndex);
        }
    }

    public void generateSlides(String presentationId, List<Action> actions) {
        ArrayList<Action> failedActions = new ArrayList<>();
        actions.stream().sorted(Collections.reverseOrder()).forEachOrdered(action -> {
            try {
                if ("bible".equals(action.type())) {
                    insertBibleText(presentationId, new BibleReference(action.input()), action.index());
                } else if ("hymn".equals(action.type())) {
                    insertSong(presentationId, action.input(), action.index());
                }
            } catch (Exception e) {
                failedActions.add(action);
            }
        });
        if (!failedActions.isEmpty()) {
            throw new RuntimeException("Failed to run these actions: " + failedActions);
        }
    }
}
