package WeatherUpdater;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Properties;

public class WeatherDatabase {
    //private String url = "jdbc:postgresql://localhost:5432/";
    private String url;
    private Properties props;

    Connection conn = null;
    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object
     */
    public WeatherDatabase(Properties properties) {
        try {
            props = new Properties();
            url = properties.getProperty("database");
            props.setProperty("user", properties.getProperty("dbuser"));
            props.setProperty("password", properties.getProperty("dbpassword"));
        } catch (Exception e) {
            System.out.println("Error reading properties: " + e.getMessage());
        }
    }

    public Connection connect() {
        conn = null;
        //props.setProperty("currentSchema","weather");

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, props);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    public boolean update(WeatherData wd) {
        try {
            Statement stmt = null;
            conn.setAutoCommit(false);
            System.out.println("Opened database successfully");

            //LocalDateTime localDate = LocalDateTime.now();
            //localDate = LocalDateTime.parse(wd.dateutc);
            //System.out.println("new date: " + localDate.toString());
            stmt = conn.createStatement();
            String sql = "INSERT INTO weather.realtime (tenant,dateutc,windspeedmph,winddir,windgustmph,dailyrainin,tempf,baromin,dewptf,humidity,solarradiation,UV) "
                    + "VALUES ('TENANT0', '" + wd.dateutc + "'," + wd.windspeedmph + "," + wd.winddir + "," + wd.windgustmph + "," + wd.dailyrainin + ","
                    + wd.temp_f + "," + wd.baromin + "," + wd.dewptf + "," + wd.humidity + "," + wd.solarradiation + "," + wd.UV + ");";
            System.out.println("SQL statement: " + sql);
            stmt.executeUpdate(sql);
            stmt.close();
            conn.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    public void close() {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}