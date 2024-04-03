package com.scottmo.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.scottmo.data.song.Song;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.ppt.SongSlidesGenerator;
import com.scottmo.services.songs.SongService;
import com.scottmo.util.StringUtils;

@RestController
@RequestMapping("/api/song")
public class SongController {

    @Autowired
    private AppContextService appContextService;
    @Autowired
    private SongService songService;
    @Autowired
    private SongSlidesGenerator pptxGenerator;

    private RequestUtil requestUtil = new RequestUtil();

    @GetMapping("/titles")
    Map<Integer, String> getSongs() {
        Map<Integer, String> titles = new HashMap<>();
        for (var title : songService.getStore().getAllSongDescriptors(appContextService.getConfig().locales())) {
            titles.put(title.key(), title.value());
        }
        return titles;
    }

    @GetMapping("/{id}")
    Song getSong(@PathVariable Integer id) {
        return songService.getStore().get(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Map<String, Object>> deleteSong(@PathVariable Integer id) {
        boolean isSuccess = songService.getStore().delete(id);
        return isSuccess
            ? requestUtil.successResponse()
            : requestUtil.errorResponse("Failed to delete song with id %s!".formatted(id));
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
        pptxGenerator.generate(song, templatePath, outputPath.toString(), appContextService.getConfig().locales(),
                linesPerSlide);
    
        return requestUtil.download(outputPath);
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportSong(@PathVariable Integer id) throws IOException {
        Song song = songService.getStore().get(id);
        Path outputPath = Path.of(System.getProperty("java.io.tmpdir"), StringUtils.sanitizeFilename(song.getTitle()) + ".xml");
        String songXML = songService.getOpenLyricsConverter().serialize(song);
        Files.writeString(outputPath, songXML, StandardCharsets.UTF_8);
        return requestUtil.download(outputPath);
    }

    @PostMapping("/save")
    public Integer saveSong(@RequestBody Song song) {
        return songService.getStore().store(song);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importSongs(@RequestBody List<String> songPaths) {
        if (songPaths == null || songPaths.isEmpty()) {
            return requestUtil.errorResponse("No file to import!");
        }
        List<File> files = songPaths.stream()
            .map(path -> new File(path))
            .collect(Collectors.toList());
        for (File file : files) {
            try {
                songService.importOpenLyricSong(file);
            } catch (IOException e) {
                return requestUtil.errorResponse("Failed to import song [%s]!".formatted(file.getName()), e);
            }
        }
        return requestUtil.successResponse();
    }
}
