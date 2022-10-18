package com.scottscmo;

import com.scottscmo.model.song.Document;
import com.scottscmo.model.song.DocumentUtil;
import com.scottscmo.model.song.Section;
import com.scottscmo.model.song.Song;
import com.scottscmo.model.song.converters.KVMDConverter;
import com.scottscmo.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SongToDocumentConverter {
    public static void main(String[] args) {
        String srcDir = "C:\\Users\\max61\\Sync\\glory-presenter\\data\\songs_slide";
        String dstDir = "C:\\Users\\max61\\Sync\\glory-presenter\\data\\songs_slide_toml";
        File[] songFiles = new File(Path.of(srcDir).toString()).listFiles();
        int limit = 999;
        if (songFiles != null) {
            for (var songFile : songFiles) {
                System.out.println("Processing file " + songFile.getName());
                try {
                    String content = Files.readString(songFile.toPath());
                    Song song = KVMDConverter.parse(content);
                    Map<String, String> title = new HashMap<>();
                    Map<String, String> metadata = new HashMap<>();
                    StringUtils.splitByCharset(song.title(), true)
                            .forEach(s -> {
                                String value = s.value().trim();
                                if (s.isAscii()) {
                                    try {
                                        int index = Integer.parseInt(value);
                                        metadata.put("index", String.valueOf(index));
                                    } catch (NumberFormatException e) {
                                        title.put("en", value);
                                    }
                                } else {
                                    title.put("zh", value);
                                }
                            });
                    metadata.put("from", song.tags());
                    Map<String, Map<String, String>> sections = song.sections().stream()
                            .collect(Collectors.toMap(
                                    Section::name,
                                    Section::text
                            ));
                    Document doc = new Document();
                    doc.setTitle(title);
                    doc.setMetadata(metadata);
                    doc.setSectionOrder(song.order());
                    doc.setSections(sections);
                    Files.writeString(Path.of(dstDir, songFile.getName().replace(".md", ".toml")), DocumentUtil.stringify(doc));
                } catch (IOException e) {
                    System.err.println("Failed to convert " + songFile.getName());
                    e.printStackTrace();
                }
                limit--;
                if (limit <= 0) {
                    return;
                }
            }
        }
    }
}
