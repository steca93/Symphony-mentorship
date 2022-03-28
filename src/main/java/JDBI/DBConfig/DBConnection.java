package JDBI.DBConfig;
import org.jdbi.v3.core.Jdbi;
import java.util.Properties;

public class DBConnection {

    Jdbi jdbi;
    Properties properties;

    public DBConnection() {
        properties = new Properties();
        properties.setProperty("user", "postgres");
        properties.setProperty("password", "421401pizza");
        jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/NorthWind", properties);
    }

    public Jdbi getConnection() {
        return jdbi;
    }
}
