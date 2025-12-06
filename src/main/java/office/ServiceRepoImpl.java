package office;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.System.exit;
import static org.postgresql.util.PGPropertyPasswordParser.getPassword;

public class ServiceRepoImpl implements  ServiceRepo{


    public  static Connection getConnection() throws SQLException {
        Connection con = null;
        try{
            con = DriverManager.getConnection(ServiceRepoImpl.url,ServiceRepoImpl.user,ServiceRepo.password);
        }
        catch(Exception e){
            System.out.println(e);
            exit(-1);
        }
        return con;
    }
}
