package WeatherUpdater;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class WeatherDatabase {
    //private String url = "jdbc:postgresql://localhost:5432/";
    private String url;
    private Properties dbProperties;
    private Logger logger;

    Connection conn = null;
    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object
     */
    public WeatherDatabase(Properties properties, Logger log) {
        try {
            logger = log;
            dbProperties = new Properties();
            url = properties.getProperty("database");
            String dbuser = properties.getProperty("dbuser");
            String dbpassword = properties.getProperty("dbpassword");

            dbProperties.setProperty("url", url);
            dbProperties.setProperty("user", dbuser);
            dbProperties.setProperty("password", dbpassword);
        } catch (Exception e) {
            System.out.println("Error reading properties: " + e.getMessage());
        }
    }

    public Connection connect() {
        conn = null;
        //props.setProperty("currentSchema","weather");

        try {
            //System.out.println("connect.URL: " + url);
            //System.out.println("connect.user: " + dbProperties.getProperty("user"));
            //System.out.println("connect.pw: " + dbProperties.getProperty("password"));

            Class.forName("org.postgresql.Driver");
            //conn = DriverManager.getConnection(url, dbProperties.getProperty("dbuser"), dbProperties.getProperty("dbpassword"));
            conn = DriverManager.getConnection(url, dbProperties);
            logger.setLog("Connected to PostgreSQL successfully.\n");
            //System.out.println("Connected to PostgreSQL successfully.");
        } catch (Exception e) {
            System.out.println("Could not connect to PostgreSQL: " + e.getMessage() + "\n");
            logger.setLog("Could not connect to PostgreSQL: " + e.getMessage() + "\n");
            e.printStackTrace();
        }

        return conn;
    }

    public boolean update(WeatherData wd) {
        try {
            Statement stmt = null;
            if (conn.isClosed()) {
                System.out.println("DB Connection has been closed.  Re-opening.");
                this.connect();
            }
            conn.setAutoCommit(false);

            //LocalDateTime localDate = LocalDateTime.now();
            //localDate = LocalDateTime.parse(wd.dateutc);
            //System.out.println("new date: " + localDate.toString());
            stmt = conn.createStatement();
            String sql = "INSERT INTO weather.realtime (tenant,dateutc,windspeedmph,winddir,windgustmph,dailyrainin,tempf,baromin,dewptf,humidity,solarradiation,UV) "
                    + "VALUES ('TENANT0', '" + wd.dateutc + "'," + wd.windspeedmph + "," + wd.winddir + "," + wd.windgustmph + "," + wd.dailyrainin + ","
                    + wd.temp_f + "," + wd.baromin + "," + wd.dewptf + "," + wd.humidity + "," + wd.solarradiation + "," + wd.UV + ");";
            System.out.println("SQL statement: " + sql);
            logger.setLog("SQL statement: " + sql + "\n");
            try {
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                stmt.cancel();
                System.out.println("Could not execute statement, canceling: " + e.getMessage());
            }
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
