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
            String form = """
                    <html>
                    <head>
                        <title>Interactive App</title>
                        <style>
                            body {
                                background-color: #121212;
                                color: #ffffff;
                                font-family: Arial, sans-serif;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                height: 100vh;
                                overflow: hidden;
                                margin: 0;
                            }
                            .container {
                                text-align: center;
                                position: relative;
                                z-index: 1;
                            }
                            h1 {
                                color: #ffffff;
                            }
                            input[type='text'] {
                                padding: 10px;
                                font-size: 16px;
                                border: none;
                                border-radius: 4px;
                            }
                            input[type='submit'] {
                                padding: 10px 20px;
                                font-size: 16px;
                                color: #ffffff;
                                background-color: #333333;
                                border: none;
                                border-radius: 4px;
                                cursor: pointer;
                            }
                            .floating-shapes {
                                position: absolute;
                                top: 0;
                                left: 0;
                                width: 100%;
                                height: 100%;
                                z-index: 0;
                                overflow: hidden;
                            }
                            .shape {
                                position: absolute;
                                background-color: rgba(255, 255, 255, 0.1);
                                border-radius: 50%;
                                animation: float 8s infinite ease-in-out;
                            }
                            .shape:nth-child(1) { width: 80px; height: 80px; left: 10%; top: 20%; animation-duration: 6s; }
                            .shape:nth-child(2) { width: 50px; height: 50px; left: 80%; top: 30%; animation-duration: 8s; }
                            .shape:nth-child(3) { width: 100px; height: 100px; left: 50%; top: 80%; animation-duration: 10s; }
                            .shape:nth-child(4) { width: 60px; height: 60px; left: 30%; top: 60%; animation-duration: 12s; }
                            .shape:nth-child(5) { width: 40px; height: 40px; left: 70%; top: 10%; animation-duration: 14s; }
                            @keyframes float {
                                0% { transform: translateY(0); }
                                50% { transform: translateY(-20px); }
                                100% { transform: translateY(0); }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Enter your name</h1>
                            <form action='/greet' method='post'>
                                <input type='text' name='name' placeholder='Your name' />
                                <input type='submit' value='Greet Me' />
                            </form>
                        </div>
                        <div class="floating-shapes">
                            <div class="shape"></div>
                            <div class="shape"></div>
                            <div class="shape"></div>
                            <div class="shape"></div>
                            <div class="shape"></div>
                        </div>
                    </body>
                    </html>
                    """;
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
                String response = "<html><body style='background-color: #121212; color: #ffffff; font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>"
                                + "<h1>Hello, " + name + "!</h1>"
                                + "<a href='/' style='color: #ff9800; text-decoration: none;'>Back</a>"
                                + "</body></html>";
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
