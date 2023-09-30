package com.scottmo.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scottmo.data.song.Song;
import com.scottmo.services.appContext.AppContextService;
import com.scottmo.services.ppt.SongSlidesGenerator;
import com.scottmo.services.songs.SongService;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    @Autowired
    private AppContextService appContextService;
    @Autowired
    private SongService songService;
    @Autowired
    private SongSlidesGenerator pptxGenerator;

    private RequestUtil requestUtil = new RequestUtil();

    @GetMapping("/songs")
    Map<Integer, String> getSongs() {
        Map<Integer, String> titles = new HashMap<>();
        for (var title : songService.getStore().getAllSongDescriptors(appContextService.getConfig().locales())) {
            titles.put(title.getKey(), title.getValue());
        }
        return titles;
    }

    @GetMapping("/song/:id")
    Song getSong(@RequestParam Integer id) {
        return songService.getStore().get(id);
    }

    @PostMapping("/songs")
    ResponseEntity<Map<String, Object>> importBibles(@RequestBody List<String> songPaths) {
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

//    @GetMapping("/employees")
//    List<Employee> all() {
//        return repository.findAll();
//    }
//    // end::get-aggregate-root[]
//
//    @PostMapping("/employees")
//    Employee newEmployee(@RequestBody Employee newEmployee) {
//        return repository.save(newEmployee);
//    }
//
//    @DeleteMapping("/employees/{id}")
//    void deleteEmployee(@PathVariable Long id) {
//        repository.deleteById(id);
//    }
//
//    @GetMapping("/employees/{id}")
//    Employee one(@PathVariable Long id) {
//
//        return repository.findById(id)
//                .orElseThrow(() -> new EmployeeNotFoundException(id));
//    }
//
//    @PutMapping("/employees/{id}")
//    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
//
//        return repository.findById(id)
//                .map(employee -> {
//                    employee.setName(newEmployee.getName());
//                    employee.setRole(newEmployee.getRole());
//                    return repository.save(employee);
//                })
//                .orElseGet(() -> {
//                    newEmployee.setId(id);
//                    return repository.save(newEmployee);
//                });
//    }
}
