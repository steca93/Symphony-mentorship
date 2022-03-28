package JDBI.DBConfig;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

public class DBRestore {

    public static void restoreDB(Connection connection) throws FileNotFoundException, SQLException {
        ScriptRunner sr = new ScriptRunner(connection);
        //Creating a reader object
        Reader reader = new BufferedReader(new FileReader("./src/main/resources/restoreNorthWindDB.sql"));
        //Running the script
        sr.setLogWriter(null);
        sr.runScript(reader);
        connection.close();
    }
}
