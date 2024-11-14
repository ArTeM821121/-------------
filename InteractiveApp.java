import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class InteractiveApp {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new FormHandler());
        server.createContext("/greet", new GreetHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    // Отправляет HTML-форму для ввода имени
    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String form = "<html><body>" +
                          "<h1>Enter your name</h1>" +
                          "<form action='/greet' method='post'>" +
                          "<input type='text' name='name' />" +
                          "<input type='submit' value='Greet Me' />" +
                          "</form>" +
                          "</body></html>";
            exchange.sendResponseHeaders(200, form.length());
            OutputStream os = exchange.getResponseBody();
            os.write(form.getBytes());
            os.close();
        }
    }

    // Обрабатывает запрос с именем и возвращает приветствие
    static class GreetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String name = getRequestBody(exchange);
                String response = "<html><body>" +
                                  "<h1>Hello, " + name + "!</h1>" +
                                  "</body></html>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }

        private String getRequestBody(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            String[] params = body.toString().split("=");
            return params.length > 1 ? params[1] : "Guest";
        }
    }
}