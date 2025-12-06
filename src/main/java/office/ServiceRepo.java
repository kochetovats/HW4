package office;

import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

public interface ServiceRepo {

    @Getter
    static String url = "jdbc:postgresql://localhost:5432/postgres";
    @Getter
    static String user = "postgres";
    @Getter
    static String password = "postgres";

    public static  Connection getConnection() throws SQLException
    {
        return null;
    };


}
