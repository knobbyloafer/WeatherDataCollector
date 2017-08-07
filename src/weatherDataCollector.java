import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

import java.io.*;
import java.net.URL;
import java.time.Instant;

public class weatherDataCollector {
    public static void main(String[] args) {
        String previouslyUpdated = "";
        while (true) {
            System.out.println("getting weather data..."); // Display the string.
            try {
                //Document temperatureXML = weatherDataCollector.loadXMLDocument("http://www.weatherlink.com/xml.php?user=knobby&pass=kmecarra3");
                Document temperatureXML = weatherDataCollector.getXML("http://www.weatherlink.com/xml.php?user=knobby&pass=kmecarra3");
                temperatureXML.getDocumentElement().normalize();

                System.out.println("Root element :" + temperatureXML.getDocumentElement().getNodeName());

                Node nNode = temperatureXML.getFirstChild();
                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                String solarradiation = "";
                String UV = "";
                String windGuestMaxDay = "";
                String dailyRainInches = "";

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    // child node
                    NodeList children = eElement.getChildNodes();
                    Node current = null;
                    int count = children.getLength();
                    for (int i = 0; i < count; i++) {
                        current = children.item(i);
                        if (current.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) current;
                            if (element.getTagName().equalsIgnoreCase("davis_current_observation")) {
                                //element.setAttribute("showPageNumbers", "no");
                                System.out.println("Found it!: " + element.getTagName().toString());
                                String rain_day_in = element.getElementsByTagName("rain_day_in").item(0).getTextContent();
                                System.out.println("rain_day_in : " + rain_day_in);
                                solarradiation = element.getElementsByTagName("solar_radiation").item(0).getTextContent();
                                System.out.println("solarradiation : " + solarradiation);
                                UV = element.getElementsByTagName("uv_index").item(0).getTextContent();
                                System.out.println("UV : " + UV);
                                windGuestMaxDay = element.getElementsByTagName("wind_day_high_mph").item(0).getTextContent();
                                System.out.println("windGuestMaxDay : " + windGuestMaxDay);
                                dailyRainInches = element.getElementsByTagName("rain_day_in").item(0).getTextContent();
                                System.out.println("dailyRainInches : " + dailyRainInches);
                            }
                            //else
                                //System.out.println("Not it: " + element.getTagName().toString());
                        }
                    }
                    // end child node

                    String lastUpdated = eElement.getElementsByTagName("observation_time").item(0).getTextContent();
                    System.out.println("Last updated : " + lastUpdated);
                    if (lastUpdated.compareTo(previouslyUpdated) == 0) {
                        System.out.println("No change, no update to report");
                    } else {
                        previouslyUpdated = lastUpdated;
                        String tempF = eElement.getElementsByTagName("temp_f").item(0).getTextContent();
                        System.out.println("Temperature (f) : " + tempF);
                        String humidity = eElement.getElementsByTagName("relative_humidity").item(0).getTextContent();
                        System.out.println("Humidity : " + humidity);
                        String windSpeedMPH = eElement.getElementsByTagName("wind_mph").item(0).getTextContent();
                        System.out.println("Wind speed (mph) : " + windSpeedMPH);
                        String windDir = eElement.getElementsByTagName("wind_degrees").item(0).getTextContent();
                        System.out.println("Wind direction (degrees) : " + windDir);
                        String pressureIn = eElement.getElementsByTagName("pressure_in").item(0).getTextContent();
                        System.out.println("Baromin : " + pressureIn);
                        String dewpointF = eElement.getElementsByTagName("dewpoint_f").item(0).getTextContent();
                        System.out.println("Dewpoint (f) : " + dewpointF);

                        String utcDate = Instant.now().toString();

                        // non rapidfire URL: weatherstation.wunderground.com
                        String wunderURLString = "https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?" +
                                "ID=KMECARRA3&PASSWORD=c736d904&dateutc=" + utcDate + "&winddir=" + windDir + "&windspeedmph=" + windSpeedMPH +
                                "&windgustmph=" + windGuestMaxDay + "&dailyrainin=" + dailyRainInches +
                                "&tempf=" + tempF + "&rainin=&baromin=" + pressureIn + "&dewptf=" + dewpointF + "&humidity=" + humidity +
                                "&weather=&clouds=&solarradiation=3.0&x=" + solarradiation + "&UV=2&y=" + UV +
                                "&softwaretype=vws%20versionxx&action=updateraw&realtime=1&rtfreq=60";
                        System.out.println("Wunder Underground URL : " + wunderURLString);
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
                Thread.sleep(5000);
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

    private static Document loadXMLDocument(String url) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new URL(url).openStream());
    }
}
