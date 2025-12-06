package office;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.System.exit;

public class ServiceRepoMock implements ServiceRepo {

    @Getter
    static String url = "jdbc:postgresql://localhost:5432/postgresTEST";
    @Getter
    static String user = "postgres";
    @Getter
    static String password = "postgres";


    public static Connection getConnection() throws SQLException {
        Connection con = null;
        System.out.println("Trying connecto to "+url);
        try{
            con = DriverManager.getConnection(url,user,password);
        }
        catch(Exception e){
            System.out.println(e);
            exit(-1);
        }
        return con;
    }




}
