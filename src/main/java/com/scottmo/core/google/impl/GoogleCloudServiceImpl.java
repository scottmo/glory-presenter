package com.scottmo.core.google.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.Request;
import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.google.api.GoogleCloudService;
import com.scottmo.core.google.api.SlideConfig;

public class GoogleCloudServiceImpl implements GoogleCloudService {
    private ConfigService configService = ConfigService.get();

    private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private Slides _slidesApi;
    private Slides getSlidesApi() {
        if (_slidesApi == null) {
            AuthClient auth = new AuthClient();
            try {
                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                _slidesApi = new Slides.Builder(httpTransport, jsonFactory, auth.getRequestInitializer())
                        .setApplicationName(Labels.get("appName"))
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
                _driveApi = new Drive.Builder(httpTransport, jsonFactory, auth.getRequestInitializer())
                        .setApplicationName(Labels.get("appName"))
                        .build();
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException("Unable to connect to Google Drive API!");
            }
        }
        return _driveApi;
    }

    @Override
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

    @Override
    public List<Page> getSlides(String presentationId) throws IOException {
        return getSlidesApi().presentations().get(presentationId).execute().getSlides();
    }

    @Override
    public Presentation getPresentation(String presentationId) throws IOException {
        return getSlidesApi().presentations().get(presentationId).execute();
    }

    @Override
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

    @Override
    public void setDefaultTitleText(String presentationId, SlideConfig slideConfig) throws IOException {
        Presentation ppt = getPresentation(presentationId);
        RequestBuilder requestBuilder = new RequestBuilder(ppt, slideConfig, configService.getConfig().getLocales());
        ppt.getSlides().forEach(requestBuilder::setDefaultTitleText);
        updateSlides(presentationId, requestBuilder.build());
    }

    @Override
    public void setBaseFont(String presentationId, SlideConfig slideConfig) throws IOException {
        Presentation ppt = getPresentation(presentationId);
        RequestBuilder requestBuilder = new RequestBuilder(ppt, slideConfig, configService.getConfig().getLocales());
        ppt.getSlides().forEach(slide -> {
            requestBuilder.setBaseFont(slide, slideConfig.getFont());
        });
        updateSlides(presentationId, requestBuilder.build());
    }

    // public void insertBibleText(String presentationId, BibleReference bibleRef, int slideIndex) throws IOException {
    //     SlideConfig slideConfig = Config.get().googleSlideConfig();
    //     Map<String, String> bibleVersionToTextConfig = slideConfig.bibleVersionToTextConfig();

    //     // query bible data
    //     Map<String, String> bookNames = BibleModel.get().getBookNames(bibleRef.getBook());
    //     assert bookNames != null : "${bibleRef.book} does not exist!";

    //     Map<String, List<BibleVerse>> bibleVerses = BibleModel.get().getBibleVerses(bibleRef);
    //     assert bibleRef.getVersions().stream().allMatch(bibleVerses::containsKey);

    //     // building slide texts (version, text)
    //     ArrayList<Pair<String, String>> slideTexts = new ArrayList<>();

    //     // title
    //     String queriedBookNames = bibleRef.getVersions().stream()
    //             .map(version -> bookNames.getOrDefault(version, ""))
    //             .filter(version -> !version.isEmpty())
    //             .collect(Collectors.joining("\n"));
    //     String verses = bibleRef.getRangesString();
    //     String titleText = queriedBookNames + "\n" + verses;
    //     slideTexts.add(new Pair<>(bibleRef.getVersions().get(0), titleText));

    //     // verses
    //     int numVerses = bibleVerses.get(bibleRef.getVersions().get(0)).size();
    //     for (int i = 0; i < numVerses; i++) {
    //         for (String version : bibleRef.getVersions()) {
    //             String groupName = bibleVersionToTextConfig.get(version);
    //             TextConfig textConfig = slideConfig.textConfigs().get(groupName);
    //             BibleVerse verse = bibleVerses.get(version).get(i);
    //             List<String> verseTexts = StringUtils.distributeTextToBlocks(
    //                     verse.index() + " " + verse.text(),
    //                     textConfig.numberOfCharactersPerLine(),
    //                     textConfig.numberOfLinesPerSlide());
    //             for (String verseText : verseTexts) {
    //                 slideTexts.add(new Pair<>(version, verseText));
    //             }
    //         }
    //     }

    //     // create slide update requests from slide texts
    //     RequestBuilder requestBuilder = new RequestBuilder();
    //     slideTexts.stream()
    //             .sorted(Collections.reverseOrder())
    //             .forEachOrdered(verseText -> {
    //                 String version = verseText.getKey();
    //                 String text = verseText.getValue();
    //                 String lang = bibleVersionToTextConfig.get(version);
    //                 TextConfig textConfig = slideConfig.textConfigs().get(lang);
    //                 assert textConfig != null : "No matching text config for $version version";

    //                 String titleId = requestBuilder.createSlideWithFullText(slideIndex);
    //                 requestBuilder.insertText(titleId, text, slideConfig.paragraph(), textConfig);
    //             });

    //     updateSlides(presentationId, requestBuilder.build());
    // }

    // public void insertSong(String presentationId, Content content, int slideInsertIndex) throws IOException {
    //     SlideConfig slideConfig = Config.get().googleSlideConfig();
    //     TextConfig defaultTextConfig = slideConfig.textConfigs().get(slideConfig.defaultTextConfig());

    //     RequestBuilder requestBuilder = new RequestBuilder();
    //     int slideIndex = slideInsertIndex;

    //     // title
    //     String joinedTitle = content.getJoinedTitle(slideConfig.textConfigsOrder());
    //     String textBoxId = requestBuilder.createSlideWithFullText(slideIndex++);
    //     requestBuilder.insertText(textBoxId, content.getTitle(), slideConfig);
    //     // requestBuilder.insertText(textBoxId, joinedTitle, slideConfig.paragraph(), defaultTextConfig);

    //     // lyrics
    //     for (String sectionName : content.getSectionOrder()) {
    //         var section = content.getRawSection(sectionName);
    //         assert section.isPresent() : "Unable to find section " + sectionName;

    //         String slideId = requestBuilder.createSlide(slideIndex++);

    //         // section text
    //         String sectionTextBoxId = requestBuilder.getPlaceHolderId(slideId);
    //         requestBuilder.resizeToFullPage(sectionTextBoxId);
    //         requestBuilder.insertText(sectionTextBoxId, section.get(), slideConfig);

    //         // footer
    //         String footerTitleBoxId = requestBuilder.createTextBox(slideId,
    //                 DefaultSlideConfig.SLIDE_W, DefaultSlideConfig.FOOTER_TITLE_SIZE * 2,
    //                 0.0,
    //                 DefaultSlideConfig.FOOTER_TITLE_Y
    //         );
    //         TextConfig footerTextConfig = new TextConfig(
    //                 defaultTextConfig.wordDelimiter(),
    //                 defaultTextConfig.fontFamily(),
    //                 DefaultSlideConfig.FOOTER_TITLE_SIZE,
    //                 defaultTextConfig.fontColor(),
    //                 defaultTextConfig.fontStyles(),
    //                 defaultTextConfig.numberOfCharactersPerLine(),
    //                 defaultTextConfig.numberOfLinesPerSlide());
    //         requestBuilder.insertText(footerTitleBoxId, joinedTitle, slideConfig.paragraph(), footerTextConfig);
    //     }
    //     updateSlides(presentationId, requestBuilder.build());
    // }

    // private void insertSong(String presentationId, String songTitle, int slideIndex) throws IOException {
    //     Content content = ContentUtil.parseFromFuzzySearch(Config.getRelativePath(Config.CONTENTS_SLIDE_DIR), songTitle);
    //     if (content != null) {
    //         insertSong(presentationId, content, slideIndex);
    //     }
    // }

    // public void generateSlides(String presentationId, List<Action> actions) {
    //     ArrayList<Action> failedActions = new ArrayList<>();
    //     actions.stream().sorted(Collections.reverseOrder()).forEachOrdered(action -> {
    //         try {
    //             if ("bible".equals(action.type())) {
    //                 insertBibleText(presentationId, new BibleReference(action.input()), action.index());
    //             } else if ("hymn".equals(action.type())) {
    //                 insertSong(presentationId, action.input(), action.index());
    //             }
    //         } catch (Exception e) {
    //             failedActions.add(action);
    //         }
    //     });
    //     if (!failedActions.isEmpty()) {
    //         throw new RuntimeException("Failed to run these actions: " + failedActions);
    //     }
    // }
}
