package com.example.storage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {
    private static final String STORAGE_DIR = "storage";

    public StorageController() throws IOException {
        Files.createDirectories(Paths.get(STORAGE_DIR));
    }


    private String normalizePath(String... parts) {
        String path = String.join("/", parts).replace("\\", "/");

        Path resolvedPath = Paths.get(STORAGE_DIR, path).normalize();
        if (!resolvedPath.startsWith(Paths.get(STORAGE_DIR))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый путь: выход за пределы хранилища");
        }
        return Paths.get(STORAGE_DIR).relativize(resolvedPath).toString().replace("\\", "/");
    }

    // Получение списка файлов
    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listFiles() throws IOException {
        List<FileInfo> fileList = new ArrayList<>();
        Path storagePath = Paths.get(STORAGE_DIR);
        try (Stream<Path> paths = Files.walk(storagePath, FileVisitOption.FOLLOW_LINKS)) {
            paths.forEach(path -> {
                if (!path.equals(storagePath)) {
                    try {

                        String relativePath = storagePath.relativize(path).toString().replace("\\", "/");
                        boolean isDirectory = Files.isDirectory(path);
                        fileList.add(new FileInfo(relativePath, isDirectory));
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при обработке пути: " + path, e);
                    }
                }
            });
        }
        return ResponseEntity.ok(fileList);
    }


    @GetMapping({"/{filename}", "/{dir}/{filename}"})
    public ResponseEntity<byte[]> readFile(
            @PathVariable String filename,
            @PathVariable(required = false) String dir) throws IOException {
        String relativePath = dir != null ? normalizePath(dir, filename) : normalizePath(filename);
        Path path = Paths.get(STORAGE_DIR, relativePath);
        if (!Files.exists(path) || Files.isDirectory(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден или это директория");
        }
        byte[] content = Files.readAllBytes(path);
        return new ResponseEntity<>(content, HttpStatus.OK);
    }


    @PutMapping({"/{filename}", "/{dir}/{filename}"})
    public ResponseEntity<String> writeFile(
            @PathVariable String filename,
            @PathVariable(required = false) String dir,
            @RequestBody String content) throws IOException {
        String relativePath = dir != null ? normalizePath(dir, filename) : normalizePath(filename);
        Path path = Paths.get(STORAGE_DIR, relativePath);
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, content.getBytes());
        return ResponseEntity.ok("Данные записаны");
    }


    @PostMapping({"/{filename}", "/{dir}/{filename}"})
    public ResponseEntity<String> appendFile(
            @PathVariable String filename,
            @PathVariable(required = false) String dir,
            @RequestBody String content) throws IOException {
        String relativePath = dir != null ? normalizePath(dir, filename) : normalizePath(filename);
        Path path = Paths.get(STORAGE_DIR, relativePath);
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
        }
        Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
        return ResponseEntity.ok("Данные добавлены");
    }


    @DeleteMapping({"/{filename}", "/{dir}/{filename}"})
    public ResponseEntity<String> deleteFile(
            @PathVariable String filename,
            @PathVariable(required = false) String dir) throws IOException {
        String relativePath = dir != null ? normalizePath(dir, filename) : normalizePath(filename);
        Path path = Paths.get(STORAGE_DIR, relativePath);
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
        }
        Files.delete(path);
        return ResponseEntity.ok("Файл удален");
    }


    @PostMapping({"/{filename}/copy/{copyfile}", "/{dir}/{filename}/copy/{copyfile}"})
    public ResponseEntity<String> copyFile(
            @PathVariable String filename,
            @PathVariable String copyfile,
            @PathVariable(required = false) String dir) throws IOException {
        String sourceRelativePath = dir != null ? normalizePath(dir, filename) : normalizePath(filename);
        String targetRelativePath = dir != null ? normalizePath(dir, copyfile) : normalizePath(copyfile);
        Path sourcePath = Paths.get(STORAGE_DIR, sourceRelativePath);
        Path targetPath = Paths.get(STORAGE_DIR, targetRelativePath);

        if (!Files.exists(sourcePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Исходный файл не найден");
        }
        if (Files.exists(targetPath)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Файл с таким именем уже существует");
        }
        if (!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }
        Files.copy(sourcePath, targetPath);
        return ResponseEntity.ok("Файл скопирован в " + targetRelativePath);
    }


    @PostMapping({"/{filename}/move/{targetDir}/{newFilename}", "/{dir}/{filename}/move/{targetDir}/{newFilename}"})
    public ResponseEntity<String> moveFile(
            @PathVariable String filename,
            @PathVariable String targetDir,
            @PathVariable String newFilename,
            @PathVariable(required = false) String dir) throws IOException {
        String sourceRelativePath = dir != null ? normalizePath(dir, filename) : normalizePath(filename);
        String targetRelativePath = normalizePath(targetDir, newFilename);
        Path sourcePath = Paths.get(STORAGE_DIR, sourceRelativePath);
        Path targetPath = Paths.get(STORAGE_DIR, targetRelativePath);

        if (!Files.exists(sourcePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Исходный файл не найден");
        }
        if (!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }
        Files.move(sourcePath, targetPath);
        return ResponseEntity.ok("Файл перемещен в " + targetRelativePath);
    }


    public static class FileInfo {
        private String path;
        private boolean isDirectory;

        public FileInfo(String path, boolean isDirectory) {
            this.path = path;
            this.isDirectory = isDirectory;
        }

        public String getPath() {
            return path;
        }

        public boolean isDirectory() {
            return isDirectory;
        }
    }
}