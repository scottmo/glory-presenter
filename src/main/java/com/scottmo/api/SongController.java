package com.scottmo.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.scottmo.config.ConfigService;
import com.scottmo.core.ppt.api.SongSlidesGenerator;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.StringUtils;

public class SongController {

    private ConfigService configService = ConfigService.get();
    private SongService songService;
    private SongSlidesGenerator pptxGenerator;

    public SongController(SongService songService, SongSlidesGenerator pptxGenerator) {
        this.songService = songService;
        this.pptxGenerator = pptxGenerator;
    }

    public Map<String, Integer> getSongs() {
        Map<String, Integer> titles = new HashMap<>();
        for (var title : songService.getAllSongDescriptors(configService.getConfig().getLocales())) {
            titles.put(title.value(), title.key());
        }
        return titles;
    }

    public Song getSong(Integer id) {
        return songService.get(id);
    }

    public boolean deleteSong(Integer id) {
        boolean isSuccess = songService.delete(id);
        if (!isSuccess) {
            throw new RuntimeException("Failed to delete song with id %s!".formatted(id));
        }
        return true;
    }

    public String generatePPTX(Integer id, Integer linesPerSlide, String templatePath)
            throws MalformedURLException, IOException {

        Song song = getSong(id);
        String outputPath = configService.getOutputPath(StringUtils.sanitizeFilename(song.getTitle()) + ".pptx");
        if (!templatePath.contains("/")) {
            templatePath = configService.getPPTXTemplate(templatePath);
        }
        pptxGenerator.generate(song, templatePath, outputPath, configService.getConfig().getLocales(),
                linesPerSlide);
    
        return outputPath;
    }

    public boolean exportSong(Integer id) throws IOException {
        Song song = songService.get(id);
        String outputPath = configService.getOutputPath(StringUtils.sanitizeFilename(song.getTitle()) + ".xml");
        String songXML = songService.serializeToOpenLyrics(song);
        Files.writeString(Path.of(outputPath), songXML, StandardCharsets.UTF_8);
        return true;
    }

    public Integer saveSong(Song song) {
        return songService.store(song);
    }

    public boolean importSongs(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // TODO handle importing a zip of songs
        try {
            String content = Files.readString(Path.of(filePath));
            songService.importOpenLyricSong(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import song [%s]!".formatted(filePath), e);
        }
        return true;
    }
}
