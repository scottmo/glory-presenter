package com.scottmo.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.scottmo.core.appContext.api.AppContextService;
import com.scottmo.core.ppt.api.SongSlidesGenerator;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.core.songs.api.SongService;
import com.scottmo.shared.StringUtils;

@RestController
@RequestMapping("/api/song")
public class SongController {

    @Autowired
    private AppContextService appContextService;
    @Autowired
    private SongService songService;
    @Autowired
    private SongSlidesGenerator pptxGenerator;

    @GetMapping("/titles")
    Map<Integer, String> getSongs() {
        Map<Integer, String> titles = new HashMap<>();
        for (var title : songService.getAllSongDescriptors(appContextService.getConfig().getLocales())) {
            titles.put(title.key(), title.value());
        }
        return titles;
    }

    @GetMapping("/{id}")
    Song getSong(@PathVariable Integer id) {
        return songService.get(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Map<String, Object>> deleteSong(@PathVariable Integer id) {
        boolean isSuccess = songService.delete(id);
        return isSuccess
            ? RequestUtil.successResponse()
            : RequestUtil.errorResponse("Failed to delete song with id %s!".formatted(id));
    }

    @GetMapping("/pptx")
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

    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportSong(@PathVariable Integer id) throws IOException {
        Song song = songService.get(id);
        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), StringUtils.sanitizeFilename(song.getTitle()) + ".xml");
        String songXML = songService.serializeToOpenLyrics(song);
        Files.writeString(outputPath, songXML, StandardCharsets.UTF_8);
        return RequestUtil.download(outputPath);
    }

    @PostMapping("/save")
    public Integer saveSong(@RequestBody Song song) {
        return songService.store(song);
    }

    @PostMapping("/import")
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
