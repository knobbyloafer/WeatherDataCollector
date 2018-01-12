package WeatherUpdater;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WeatherDataHTTPStatus {
    Logger logInfo = null;

    public void startServer(Logger log) throws Exception {
        System.out.println("HTTP Server starting");
        logInfo = log;
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        //server.setExecutor(null); // creates a default executor
        server.createContext("/", new MyHandler());
        server.start();
        System.out.println("HTTP Server started");
        logInfo.setLog("HTTP Server started");
    }

    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            //String sResponse = "Weather Data Collector status information: " + Math.random();
            String sResponse = logInfo.getLog();
            byte [] response = sResponse.getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}
