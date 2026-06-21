package com.scottmo.core.songs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.song.SongVerse;
import com.scottmo.core.songs.impl.SongServiceImpl;

class SongYamlExportTest {

    private SongService songService;
    private Song song;

    @BeforeEach
    void setUp() {
        songService = new SongServiceImpl();
        song = new Song(101);
        song.setDefaultLocale("zh_cn");
    }

    @Test
    void getExportFilename_concatenatesInOrderOfLocales() {
        song.setTitle("zh_cn", "  奇异恩典  ");
        song.setTitle("en_us", "  Amazing Grace  ");
        
        List<String> locales = List.of("zh_cn", "en_us");
        String filename = songService.getExportFilename(song, locales);
        assertEquals("奇异恩典AmazingGrace.yaml", filename);

        // Reverse order of locales in config
        List<String> reversedLocales = List.of("en_us", "zh_cn");
        String reversedFilename = songService.getExportFilename(song, reversedLocales);
        assertEquals("AmazingGrace奇异恩典.yaml", reversedFilename);
    }

    @Test
    void getExportFilename_omitsMissingLocales() {
        song.setTitle("en_us", "Amazing Grace");
        // zh_cn is not set on the song, but listed in config locales
        List<String> locales = List.of("zh_cn", "en_us");
        String filename = songService.getExportFilename(song, locales);
        assertEquals("AmazingGrace.yaml", filename);
    }

    @Test
    void getExportFilename_sanitizesPunctuationAndRemovesSpaces() {
        song.setTitle("en_us", "Amazing Grace! (How Sweet the Sound?)");
        List<String> locales = List.of("en_us");
        String filename = songService.getExportFilename(song, locales);
        // Space removed -> AmazingGrace!(HowSweettheSound?)
        // Punctuation replaced with _ -> AmazingGrace__HowSweettheSound__
        // Let's verify what replaced characters are:
        // StringUtils.sanitizeFilename uses .replaceAll("\\p{P}", "_")
        // Punctuation characters in "AmazingGrace!(HowSweettheSound?)" are:
        // ! -> _
        // ( -> _
        // ) -> _
        // ? -> _
        // result: AmazingGrace__HowSweettheSound__
        assertEquals("AmazingGrace__HowSweettheSound__.yaml", filename);
    }

    @Test
    void getExportFilename_fallsBackToDefaultTitle_whenNoLocaleMatches() {
        song.setTitle("fr_fr", "Grâce Infinie");
        List<String> locales = List.of("zh_cn", "en_us");
        String filename = songService.getExportFilename(song, locales);
        assertEquals("GrâceInfinie.yaml", filename);
    }

    @Test
    void getExportFilename_fallsBackToSongId_whenNoTitleExists() {
        List<String> locales = List.of("zh_cn", "en_us");
        String filename = songService.getExportFilename(song, locales);
        assertEquals("song_101.yaml", filename);
    }

    @Test
    void serializeToYaml_producesValidYaml() throws IOException {
        song.setTitle("zh_cn", "奇异恩典");
        song.setAuthors(List.of("John Newton"));
        song.setVerses(List.of(new SongVerse("v1", "Lyrics of verse 1", "zh_cn")));
        
        String yaml = songService.serializeToYaml(song);
        assertNotNull(yaml);
        assertTrue(yaml.contains("奇异恩典"));
        assertTrue(yaml.contains("John Newton"));
        assertTrue(yaml.contains("Lyrics of verse 1"));
        
        // Parse it back to check validity
        YAMLMapper yamlMapper = new YAMLMapper();
        Song parsedSong = yamlMapper.readValue(yaml, Song.class);
        assertEquals(song.getId(), parsedSong.getId());
        assertEquals("奇异恩典", parsedSong.getTitle("zh_cn"));
        assertEquals("John Newton", parsedSong.getAuthors().get(0));
        assertEquals("Lyrics of verse 1", parsedSong.getVerses().get(0).getText());
    }

    @Test
    void serializeToYaml_usesLiteralBlockStyleAndMinimizedQuotes() throws IOException {
        song.setTitle("en_us", "Amazing Grace");
        song.setVerses(List.of(new SongVerse("v1", "Line one\nLine two\nLine three", "en_us")));
        
        String yaml = songService.serializeToYaml(song);
        
        // Minimized quotes: "Amazing Grace" should not be quoted
        assertFalse(yaml.contains("\"Amazing Grace\""));
        assertTrue(yaml.contains("text: Amazing Grace"));

        // Literal block style for multiline text: should contain '|' and not double-quoted multiline with \n
        assertTrue(yaml.contains("|"));
        assertFalse(yaml.contains("\\n"));
        assertTrue(yaml.contains("Line one"));
        assertTrue(yaml.contains("Line two"));
        assertTrue(yaml.contains("Line three"));
    }
}
