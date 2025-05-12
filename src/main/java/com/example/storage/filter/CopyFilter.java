package com.example.storage.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/api/v1/storage/*/*/copy/*",
        "/api/v1/storage/*/copy/*"
})
public class CopyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        // Инициализация, если требуется
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Проверяем, что метод — POST
        if (!"COPY".equalsIgnoreCase(req.getMethod())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Требуется метод COPY");
            return;
        }

        // Извлекаем параметры из пути
        String pathInfo = req.getPathInfo(); // Например, "/nunana/ffff/copy/ffff_copy" или "/file1.txt/copy/file2.txt"
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректный путь");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length != 4 && parts.length != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Некорректный формат пути");
            return;
        }

        String filename = parts[parts.length - 3];
        String copyfile = parts[parts.length - 1];
        String dir = parts.length == 4 ? parts[1] : null;

        // Базовая валидация параметров
        if (filename == null || filename.isEmpty() || copyfile == null || copyfile.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Имя файла или копии не указано");
            return;
        }
        if (filename.contains("..") || copyfile.contains("..") || (dir != null && dir.contains(".."))) {
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