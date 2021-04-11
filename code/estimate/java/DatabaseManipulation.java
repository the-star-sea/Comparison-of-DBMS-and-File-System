

import java.sql.*;
import java.util.LinkedList;

class ConnectionPool {
    public static LinkedList<Connection> pool=new LinkedList<>();
    static private String host = "10.16.153.230";
    static private String dbname = "test";
    static private String user = "postgres";
    static private String password = "2303158704";
    static private String port = "5432";
    static String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;//+"?useServerPrepStmts=true&rewriteBatchedStatements=true";
    static int originsize=12;

    static{
        try {
            Class.forName("org.postgresql.Driver");
            for (int i = 0; i < originsize; i++) {
                Connection connection = DriverManager.getConnection(url, user, password);
                pool.add(connection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static LinkedList<Connection> getPool() {
        return pool;
    }

    public static void setPool(LinkedList<Connection> pool) {
        ConnectionPool.pool = pool;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        ConnectionPool.host = host;
    }

    public static String getDbname() {
        return dbname;
    }

    public static void setDbname(String dbname) {
        ConnectionPool.dbname = dbname;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        ConnectionPool.user = user;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ConnectionPool.password = password;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        ConnectionPool.port = port;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        ConnectionPool.url = url;
    }

    public static int getOriginsize() {
        return originsize;
    }

    public static void setOriginsize(int originsize) {
        ConnectionPool.originsize = originsize;
    }


    public Connection getConnection() throws SQLException {

        Connection connection;
        if(pool.size()>0)
        {
            connection = pool.removeFirst();
        }
        else
        {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
    public void close(Connection connection) throws SQLException {
        pool.add(connection);
    }
public void closeconnection(Connection con) throws SQLException {
    if (con != null) {
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
}
}}

public class DatabaseManipulation  {
    private Connection con;
    private ResultSet resultSet;

    public DatabaseManipulation(Connection con) {
        this.con = con;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public String namefromid(int id) {
        StringBuilder sb = new StringBuilder();
        String sql = "select name from student where student_id="+id+";";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("name")).append("\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


}
