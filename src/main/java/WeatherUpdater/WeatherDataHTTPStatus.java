package WeatherUpdater;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WeatherDataHTTPStatus {
    public void startServer() throws Exception {
        System.out.println("HTTP Server starting");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        //server.setExecutor(null); // creates a default executor
        server.createContext("/", new MyHandler());
        server.start();
        System.out.println("HTTP Server started");
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String sResponse = "Weather Data Collector status information: " + Math.random();
            byte [] response = sResponse.getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}
