import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;

public class WeatherDataCollector {
    public static void main(String[] args) {
        WeatherDatabase dbConnection = new WeatherDatabase();

        String previouslyUpdated = "";
        WeatherData wd = new WeatherData();

        while (true) {
            //System.out.println("getting weather data..."); // Display the string.
            try {
                //Document temperatureXML = weatherDataCollector.loadXMLDocument("http://www.weatherlink.com/xml.php?user=knobby&pass=kmecarra3");
                Document temperatureXML = WeatherDataCollector.getXML("http://www.weatherlink.com/xml.php?user=knobby&pass=kmecarra3");
                temperatureXML.getDocumentElement().normalize();

                //System.out.println("Root element :" + temperatureXML.getDocumentElement().getNodeName());

                Node nNode = temperatureXML.getFirstChild();
                //System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String lastUpdated = eElement.getElementsByTagName("observation_time_rfc822").item(0).getTextContent();
                    String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
                    SimpleDateFormat format = new SimpleDateFormat(pattern);
                    wd.dateutc = new Timestamp(format.parse(lastUpdated).getTime());
                    lastUpdated = wd.dateutc.toString();

                    //wd.dateutc = LocalDateTime.parse(lastUpdated);
                    System.out.println("Last updated : " + lastUpdated + " converted to " + lastUpdated);
                    if (lastUpdated.compareTo(previouslyUpdated) == 0) {
                        //System.out.println("No change, no update to report");
                    } else {
                        previouslyUpdated = lastUpdated;

                        // child node
                        NodeList children = eElement.getChildNodes();
                        Node current = null;
                        int count = children.getLength();
                        for (int i = 0; i < count; i++) {
                            current = children.item(i);
                            if (current.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) current;
                                if (element.getTagName().equalsIgnoreCase("davis_current_observation")) {
                                    //System.out.println("Found it!: " + element.getTagName().toString());
                                    wd.dailyrainin = element.getElementsByTagName("rain_day_in").item(0).getTextContent();
                                    //System.out.println("rain_day_in : " + rain_day_in);
                                    wd.solarradiation = element.getElementsByTagName("solar_radiation").item(0).getTextContent();
                                    //System.out.println("solarradiation : " + solarradiation);
                                    wd.UV = element.getElementsByTagName("uv_index").item(0).getTextContent();
                                    //System.out.println("UV : " + UV);
                                    wd.windgustmph = element.getElementsByTagName("wind_day_high_mph").item(0).getTextContent();
                                    //System.out.println("windGuestMaxDay : " + windGuestMaxDay);
                                }
                                //else
                                //System.out.println("Not it: " + element.getTagName().toString());
                            }
                        }
                        // end child node

                        wd.temp_f = eElement.getElementsByTagName("temp_f").item(0).getTextContent();
                        //System.out.println("Temperature (f) : " + tempF);
                        wd.humidity = eElement.getElementsByTagName("relative_humidity").item(0).getTextContent();
                        //System.out.println("Humidity : " + humidity);
                        wd.windspeedmph = eElement.getElementsByTagName("wind_mph").item(0).getTextContent();
                        //System.out.println("Wind speed (mph) : " + windSpeedMPH);
                        wd.winddir = eElement.getElementsByTagName("wind_degrees").item(0).getTextContent();
                        //System.out.println("Wind direction (degrees) : " + windDir);
                        wd.baromin = eElement.getElementsByTagName("pressure_in").item(0).getTextContent();
                        //System.out.println("Baromin : " + pressureIn);
                        wd.dewptf = eElement.getElementsByTagName("dewpoint_f").item(0).getTextContent();
                        //System.out.println("Dewpoint (f) : " + dewpointF);

                        // database update section
                        dbConnection.connect();
                        dbConnection.update(wd);
                        dbConnection.close();

                        // weather underground section
                        // non rapidfire URL: weatherstation.wunderground.com
                        // use NOW for date for weather underground
                        String utcDate = Instant.now().toString();
                        String wunderURLString = "https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?" +
                                "ID=KMECARRA3&PASSWORD=c736d904&dateutc=" + utcDate + "&winddir=" + wd.winddir + "&windspeedmph=" + wd.windspeedmph +
                                "&windgustmph=" + wd.windgustmph + "&dailyrainin=" + wd.dailyrainin +
                                "&tempf=" + wd.temp_f + "&rainin=&baromin=" + wd.baromin + "&dewptf=" + wd.dewptf + "&humidity=" + wd.humidity +
                                "&weather=&clouds=&solarradiation=" + wd.solarradiation + "&UV=" + wd.UV +
                                "&softwaretype=vws%20versionxx&action=updateraw&realtime=1&rtfreq=60";
                        System.out.println("Weather Underground URL : " + wunderURLString);
                        URL wunderURL = new URL(wunderURLString);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(wunderURL.openStream()));

                        String inputLine;
                        while ((inputLine = in.readLine()) != null)
                            System.out.println(inputLine);
                        in.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //xml parse
    private static Document getXML(String url) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new URL(url).openStream());
    }

    private void getWeatherData(WeatherData wd) {

    }
}
