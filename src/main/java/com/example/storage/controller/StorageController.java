package com.example.storage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {
    private static final String STORAGE_DIR = "storage";

    public StorageController() throws IOException {
        Files.createDirectories(Paths.get(STORAGE_DIR));
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> readFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get(STORAGE_DIR, filename);
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Файл не найден");
        }
        byte[] content =Files.readAllBytes(path);
        return new ResponseEntity<>(content, HttpStatus.OK);
    }
    @PutMapping("/{filename}")
    public ResponseEntity<String> writeFile(@PathVariable String filename, @RequestBody String content) throws IOException {
        Path path = Paths.get(STORAGE_DIR, filename);
        if (!Files.exists(path)) {
            Files.createFile(Paths.get(path.toString()));

        }

        Files.write(path,content.getBytes());
        return ResponseEntity.ok("Данные добавлены");
    }
    @PostMapping("/{filename}")
    public ResponseEntity<String> appendFile(@PathVariable String filename, @RequestBody String content) throws IOException {
        Path path = Paths.get(STORAGE_DIR, filename);
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Файл не найден");
        }
        Files.write(path,content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return ResponseEntity.ok("Данные добавлены");
    }
    @DeleteMapping("/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get(STORAGE_DIR, filename);
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Файл не найден");
        }
        Files.delete(path);
        return ResponseEntity.ok("Файл удален");

    }
    @RequestMapping(value = "/{filename}/copy/{copyfile}")
    public ResponseEntity<String> copyFile(@PathVariable String filename, @PathVariable String copyfile) throws IOException {
        Path sourcepath = Paths.get(STORAGE_DIR, filename);
        Path targetpath = Paths.get(STORAGE_DIR, copyfile);

        if (!Files.exists(sourcepath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
        }

        if (Files.exists(targetpath)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Файл с таким именем уже существует");
        }

        try {
            Files.copy(sourcepath, targetpath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при копировании файла", e);
        }

        return ResponseEntity.ok("Файл скопирован в " + targetpath.toAbsolutePath());
    }
    @RequestMapping(value = "/{filename}/move/{dir}/{newFilename}")
    public ResponseEntity<String> moveFile(
            @PathVariable String filename,
            @PathVariable String dir,
            @PathVariable String newFilename) throws IOException {

        // Определяем путь исходного файла
        Path sourcepath = Paths.get(STORAGE_DIR, filename);

        // Проверяем существует ли исходный файл
        if (!Files.exists(sourcepath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
        }

        // Формируем путь назначения, включая директорию и новое имя файла
        Path targetpath = Paths.get(STORAGE_DIR, dir, newFilename);

        // Перемещаем файл
        Files.move(sourcepath, targetpath);

        return ResponseEntity.ok("Файл перемещен в " + targetpath.toAbsolutePath());
    }




}
