package mini.spring.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import mini.spring.core.ClasspathScannerOfCiamb;
import mini.spring.core.ContainerOfCiamb;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HttpServerOfCiamb {
    private final ContainerOfCiamb containerOfCiamb;
    private final List<Route> routes = new ArrayList<>();

    public record Route(String method, String path, Object controller, Method handler) {
    }

    public HttpServerOfCiamb(ContainerOfCiamb containerOfCiamb) {
        this.containerOfCiamb = containerOfCiamb;
    }

    public void scanControllers(String scanningBasePackages) {
        for (Class<?> clazz : ClasspathScannerOfCiamb.findClasses(scanningBasePackages)) {
            if (clazz.isAnnotationPresent(ControllerByCiamb.class)) {
                Object controllerInstance = containerOfCiamb.getCiambBean(clazz);
                String basePath = clazz.getAnnotation(ControllerByCiamb.class).basePath();

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetByCiamb.class)) {
                        String path = method.getAnnotation(GetByCiamb.class).path();
                        String fullPath = basePath + path;
                        routes.add(new Route("GET", fullPath, controllerInstance, method));
                    }
                }
            }
        }
    }

    //server related
    public void startServer(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", this::handleRequest);
        httpServer.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Server started on http://localhost:" + port);
        httpServer.start();
    }

    public void handleRequest(HttpExchange httpExchange) throws IOException {
        try {
            String upperCaseMethod = httpExchange.getRequestMethod().toUpperCase(Locale.ROOT);
            URI uri = httpExchange.getRequestURI();
            String path = uri.getPath();

            Route route = routes.stream()
                    .filter(r -> r.method.equalsIgnoreCase(upperCaseMethod) && r.path.equalsIgnoreCase(path))
                    .findFirst()
                    .orElse(null);

            if (route == null) {
                send(httpExchange, 404, "Not found: " + path);
                return;
            }

            Object result;
            Method method = route.handler;
            method.setAccessible(true);
            if (method.getParameterCount() == 0) {
                result = method.invoke(route.controller);
            } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == String.class) {
                Map<String, String> resultMap = parseQuery(uri.getRawQuery());
                result = method.invoke(route.controller, resultMap.get("name"));
            } else {
                send(httpExchange, 500, "Handler params not supported: " + method);
                return;
            }

            send(httpExchange, 200, String.valueOf(result));
        } catch (Exception e) {
            e.printStackTrace();
            send(httpExchange, 500, "Internal server error: " + e.getMessage());
        }
    }


    private void send(HttpExchange httpExchange, int status, String bodyMessage) throws IOException {
        byte[] bytes = bodyMessage.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> resultMap = new HashMap<>();

        if (rawQuery == null || rawQuery.isBlank()) {
            return resultMap;
        }

        for (String pair : rawQuery.split("&")) {
            int index = pair.indexOf("=");
            if (index > 0) {
                String key = URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8);
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }
}
