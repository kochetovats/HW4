package edu.innotech;
import java.sql.*;


public class Starter {
    public static void notmain (String[] args) throws SQLException {
        System.out.println("Hi!");
        Service s = new Service();
        //s.AddDepartment("Tech",3);
        //s.AddEmployee(3,"Alex",3);
        s.FindEmployeeByName();

   }
}
