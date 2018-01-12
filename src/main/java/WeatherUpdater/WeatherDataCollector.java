package WeatherUpdater;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

import java.io.*;
import java.net.Inet4Address;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.sql.Timestamp;
import java.util.Properties;
import org.jsoup.Jsoup;

public class WeatherDataCollector {
    public static void main(String[] args) {
        Properties prop = new Properties();
        WeatherDatabase dbConnection = null;
        InputStream input = null;
        Logger logger = null;

        try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            System.out.println("Local IP address: " + Inet4Address.getLocalHost().getHostAddress());

            input = new FileInputStream("weatherdata.properties");

            // load a properties file
            prop.load(input);
            logger = new Logger();

            logger.setLog("**** Weather Data Collector log " + Version.label + " ****\n");
//            WeatherDataHTTPStatus httpConnection = new WeatherDataHTTPStatus();
//            httpConnection.startServer(logger);

            // get the property value and print it out
            //System.out.println(prop.getProperty("database"));
            //System.out.println(prop.getProperty("dbuser"));
            //System.out.println(prop.getProperty("dbpassword"));

//            dbConnection = new WeatherDatabase(prop, logger);
//            dbConnection.connect();

            String previouslyUpdated = "";
            WeatherData wd = new WeatherData();
            int timeToSleepMS = 10000;
            int observationAge = 0;

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
                        //System.out.println("Last updated : " + lastUpdated + " converted to " + lastUpdated);
                        if (lastUpdated.compareTo(previouslyUpdated) == 0) {
                            System.out.println("No change, no update to report");
                            // we may have just missed the updated time, don't sleep for another 60 seconds, just sleep for a few
                            timeToSleepMS = 5000;
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
                                        wd.raininlasthour = element.getElementsByTagName("rain_rate_in_per_hr").item(0).getTextContent();
                                        //System.out.println("rain_in : " + rain_in);
                                        wd.solarradiation = element.getElementsByTagName("solar_radiation").item(0).getTextContent();
                                        //System.out.println("solarradiation : " + solarradiation);
                                        wd.UV = element.getElementsByTagName("uv_index").item(0).getTextContent();
                                        //System.out.println("UV : " + UV);
                                        wd.windgustmph = element.getElementsByTagName("wind_day_high_mph").item(0).getTextContent();
                                        //System.out.println("windGuestMaxDay : " + windGuestMaxDay);
                                        // The Davis XML reports how old the data is.  It is also knownthe XML is regenerated every 60 seconds so see how old it is and subtract
                                        // that from 60 seconds to decide when we should check again.  Add a second to the total to be safe
                                        try {
                                            observationAge = Integer.parseInt(element.getElementsByTagName("observation_age").item(0).getTextContent());
                                            if (observationAge > 60)
                                                timeToSleepMS = 5000; // sometimes the XML isn't updated every minute so try every 5s if longer than a minute
                                            else
                                                timeToSleepMS = (60 - observationAge) * 1000 + 1000;
                                        } catch (Exception e) {
                                            System.out.println("Observation age exception: " + e.getMessage());
                                            timeToSleepMS = 5000;
                                        }
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
//                            dbConnection.update(wd);

                            // weather underground section
                            // non rapidfire URL: weatherstation.wunderground.com
                            // use NOW for date for weather underground
                            //  need to move weatherunderground stuff to a dedicated method
                            String utcDate = Instant.now().toString();
                            String wunderURLString = "https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?" +
                                    "ID=KMECARRA3&PASSWORD=c736d904&dateutc=" + utcDate + "&winddir=" + wd.winddir + "&windspeedmph=" + wd.windspeedmph +
                                    "&windgustmph=" + wd.windgustmph + "&dailyrainin=" + wd.dailyrainin +
                                    "&tempf=" + wd.temp_f + "&rainin=" + wd.raininlasthour + "&baromin=" + wd.baromin + "&dewptf=" + wd.dewptf + "&humidity=" + wd.humidity +
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
                    System.out.println("Sleeping " + timeToSleepMS + " ms before checking again");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
                    Thread.sleep(timeToSleepMS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (dbConnection != null)
                dbConnection.close();
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // HTML parse - get Wind Guest over last 10 minutes from HTML page since the data is not in the XML
    private static String getWindGuest() {
        try {
            org.jsoup.nodes.Document doc = Jsoup.connect("http://www.weatherlink.com/user/knobby/index.php?view=summary&headers=1").get();
            org.jsoup.select.Elements rows = doc.select("tr");
            for (org.jsoup.nodes.Element row : rows) {
                org.jsoup.select.Elements columns = row.select("td");
                for (org.jsoup.nodes.Element column : columns) {
                    if (column.text().compareTo("Wind Gust Speed") == 0) {
                        //System.out.print(column.text()); // header
                        column = column.nextElementSibling();
                        //System.out.print(column.text()); // space
                        column = column.nextElementSibling();
                        String gustString = column.text();
                        //System.out.print(column.text()); // actual wind gust over last 10 minutes
                        return gustString.replaceAll("[^0-9.]","");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.0"; // exception
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
