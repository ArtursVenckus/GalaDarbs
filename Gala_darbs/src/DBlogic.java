import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;

//pievienoties pie SQL
public class DBlogic {

    private static final String DB = "jdbc:mysql://localhost:3306/izdevumi";
    private static final String USER = "root";
    private static final String PASS= "Welcome2LV@";

    public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB, USER, PASS);

    }
}


