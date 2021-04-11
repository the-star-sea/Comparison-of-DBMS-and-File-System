import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class Client {

    public static void main(String[] args) {
        try {
ConnectionPool connectionPool=new ConnectionPool();
DatabaseManipulation dm=new DatabaseManipulation(connectionPool.getConnection());
            connectionPool.close(dm.getCon());

        } catch (IllegalArgumentException | SQLException e) {
            System.err.println(e.getMessage());

    }
}}

