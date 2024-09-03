package com.scottmo.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.scottmo.core.config.ConfigService;
import com.scottmo.core.ppt.api.SongSlidesGenerator;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.StringUtils;

public class SongController {

    private ConfigService appContextService;
    private SongService songService;
    private SongSlidesGenerator pptxGenerator;

    public SongController(SongService songService, SongSlidesGenerator pptxGenerator) {
        this.songService = songService;
        this.pptxGenerator = pptxGenerator;
    }

    Map<Integer, String> getSongs() {
        Map<Integer, String> titles = new HashMap<>();
        for (var title : songService.getAllSongDescriptors(appContextService.getConfig().getLocales())) {
            titles.put(title.key(), title.value());
        }
        return titles;
    }

    Song getSong(@PathVariable Integer id) {
        return songService.get(id);
    }

    ResponseEntity<Map<String, Object>> deleteSong(@PathVariable Integer id) {
        boolean isSuccess = songService.delete(id);
        return isSuccess
            ? RequestUtil.successResponse()
            : RequestUtil.errorResponse("Failed to delete song with id %s!".formatted(id));
    }

    public ResponseEntity<Resource> generatePPTX(
            @RequestParam Integer id,
            @RequestParam Integer linesPerSlide,
            @RequestParam String templatePath) throws MalformedURLException, IOException {

        Song song = getSong(id);
        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), StringUtils.sanitizeFilename(song.getTitle()) + ".pptx");
        if (!templatePath.contains("/")) {
            templatePath = appContextService.getPPTXTemplate(templatePath);
        }
        pptxGenerator.generate(song, templatePath, outputPath.toString(), appContextService.getConfig().getLocales(),
                linesPerSlide);
    
        return RequestUtil.download(outputPath);
    }

    public ResponseEntity<Resource> exportSong(@PathVariable Integer id) throws IOException {
        Song song = songService.get(id);
        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), StringUtils.sanitizeFilename(song.getTitle()) + ".xml");
        String songXML = songService.serializeToOpenLyrics(song);
        Files.writeString(outputPath, songXML, StandardCharsets.UTF_8);
        return RequestUtil.download(outputPath);
    }

    public Integer saveSong(@RequestBody Song song) {
        return songService.store(song);
    }

    public ResponseEntity<Map<String, Object>> importSongs(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return RequestUtil.errorResponse("Please select a file to upload");
        }

        // TODO handle importing a zip of songs
        try {
            String content = new String(file.getBytes());
            songService.importOpenLyricSong(content);
        } catch (IOException e) {
            e.printStackTrace();
            return RequestUtil.errorResponse("Failed to import song [%s]!".formatted(file.getName()), e);
        }
        return RequestUtil.successResponse();
    }
}
