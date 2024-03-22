package org.example.playmysongs_backend.controller;

import org.example.playmysongs_backend.entity.Song;
import org.example.playmysongs_backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSong(
            @RequestParam("name") String name,
            @RequestParam("style") String style,
            @RequestParam("artist") String artist,
            @RequestParam("file") MultipartFile file) {

        if (!file.getContentType().equals("audio/mpeg")) {
            return ResponseEntity.badRequest().body("Invalid file type.");
        }

        String uploadDir = "uploads";
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            Song song = new Song();
            song.setName(name);
            song.setStyle(style);
            song.setArtist(artist);
            song.setFilePath(filePath.toString());
            songService.uploadSong(song);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload the file: " + e.getMessage());
        }

        return ResponseEntity.ok("File uploaded successfully.");
    }


    @GetMapping
    public ResponseEntity<?> getSongs(@RequestParam(value = "filter", required = false) String filter) {
        if (filter != null && !filter.isEmpty()) {
            return ResponseEntity.ok(songService.searchSongs(filter));
        } else {
            return ResponseEntity.ok(songService.getAllSongs());
        }
    }
}
