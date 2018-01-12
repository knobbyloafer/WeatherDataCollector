package WeatherUpdater;

public class Logger {
    String temp = null;
    StringBuilder logFile = null;

    public Logger() {
        logFile = new StringBuilder(10000);
    }

    public String getLog() {
        return logFile.toString();
    }

    public void setLog(String logLine) {
        logFile.append(logLine);
    }
}
