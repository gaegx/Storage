package com.example.storage.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/api/v1/storage/*/*/move/*/*",
        "/api/v1/storage/*/move/*/*"
})
public class MoveFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        // Инициализация, если требуется
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Проверяем, что метод — POST
        if (!"MOVE".equalsIgnoreCase(req.getMethod())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Требуется метод MOVE");
            return;
        }

        // Извлекаем параметры из пути
        String pathInfo = req.getPathInfo(); // Например, "/nunana/ffff/move/nunana2/ffff_moved" или "/file1.txt/move/dir2/file2.txt"
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректный путь");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length != 5 && parts.length != 4) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректный формат пути");
            return;
        }

        String filename = parts[parts.length - 4]; // ffff или file1.txt
        String targetDir = parts[parts.length - 2]; // nunana2 или dir2
        String newFilename = parts[parts.length - 1]; // ffff_moved или file2.txt
        String dir = parts.length == 5 ? parts[1] : null; // nunana или null

        // Базовая валидация параметров
        if (filename == null || filename.isEmpty() || targetDir == null || targetDir.isEmpty() || newFilename == null || newFilename.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Имя файла, директория или новое имя не указаны");
            return;
        }
        if (filename.contains("..") || targetDir.contains("..") || newFilename.contains("..") || (dir != null && dir.contains(".."))) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Недопустимые символы в пути");
            return;
        }

        // Продолжаем цепочку обработки
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Очистка, если требуется
    }
}