package office;

import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.Objects;

import static java.lang.System.exit;

public class Service  {

/*
    @Getter
    private static String url = "jdbc:postgresql://localhost:5432/postgres";
    @Getter
    private static String user = "postgres";
    @Getter
    private static String password = "postgres";
    */

    @Setter
    private  ServiceRepo repo;

    /*
    public static  Connection getConnection () throws SQLException {
        Connection con = null;
        try{
            con = DriverManager.getConnection(getUrl(),getUser(),getPassword());
        }
        catch(Exception e){
            System.out.println(e);
            exit(-1);
        }
        return con;
    }*/

    public static  void createDB(Connection con) throws SQLException {
        if (Objects.isNull(con)){
            exit(-1);
        }
        else{
            Statement stm = con.createStatement();
            //System.out.println("Start deleting table department...");
            stm.executeUpdate("DROP TABLE  IF EXISTS \"HW4\".Department CASCADE");
            //System.out.println("Finished deleting table department...");
            //System.out.println("Start creating table department...");
            stm.executeUpdate("CREATE TABLE \"HW4\".Department(ID INT PRIMARY KEY, NAME VARCHAR(255))");
            stm.executeUpdate("INSERT INTO \"HW4\".Department VALUES(1,'Accounting')");
            stm.executeUpdate("INSERT INTO \"HW4\".Department VALUES(2,'IT')");
            stm.executeUpdate("INSERT INTO \"HW4\".Department VALUES(3,'HR')");
            //System.out.println("Finished creating table department...");

            //System.out.println("Start deleting table employee...");
            stm.executeUpdate("DROP TABLE IF EXISTS \"HW4\".Employee");
            //System.out.println("Finished deleting table employee...");
            //System.out.println("Start creating table employee...");
            stm.executeUpdate("CREATE TABLE \"HW4\".Employee(ID INT PRIMARY KEY, NAME VARCHAR(255), DepartmentID INT, CONSTRAINT employee_department_fk FOREIGN KEY (DepartmentID) REFERENCES \"HW4\".Department(ID) ON DELETE CASCADE);");

            stm.executeUpdate("INSERT INTO \"HW4\".Employee VALUES(1,'Pete',1)");
            stm.executeUpdate("INSERT INTO \"HW4\".Employee VALUES(2,'Ann',1)");

            stm.executeUpdate("INSERT INTO \"HW4\".Employee VALUES(3,'Liz',2)");
            stm.executeUpdate("INSERT INTO \"HW4\".Employee VALUES(4,'Tom',2)");

            stm.executeUpdate("INSERT INTO \"HW4\".Employee VALUES(5,'Todd',3)");
            System.out.println("Tables department and employee were created.");

        }
    }

    public static  void addDepartment(Department d,Connection con) throws SQLException {
            if (Objects.isNull(con)){
                exit(-1);
            }
            else{
            PreparedStatement stm = con.prepareStatement("INSERT INTO \"HW4\".Department VALUES(?,?)");
            stm.setInt(1, d.departmentID);
            stm.setString(2, d.getName());
            stm.executeUpdate();
        }

    }

    public static  void removeDepartment(Department d, Connection con) throws SQLException {
        if (Objects.isNull(con)){
            exit(-1);
        }
        else{
            PreparedStatement stm = con.prepareStatement("DELETE FROM \"HW4\".Department WHERE ID=?");
            stm.setInt(1, d.departmentID);
            stm.executeUpdate();
        }
    }

    public static void addEmployee(Employee empl, Connection con) throws SQLException {
        if (Objects.isNull(con)){
            exit(-1);
        }
        else{
            PreparedStatement stm = con.prepareStatement("INSERT INTO \"HW4\".Employee VALUES(?,?,?)");
            stm.setInt(1, empl.getEmployeeId());
            stm.setString(2, empl.getName());
            stm.setInt(3, empl.getDepartmentId());
            stm.executeUpdate();
            ;
        }
    }


    public static void removeEmployee(Employee empl, Connection con) throws SQLException {
        if (Objects.isNull(con)){
            exit(-1);
        }
        else{
            PreparedStatement stm = con.prepareStatement("DELETE FROM \"HW4\".Employee WHERE ID=?");
            stm.setInt(1, empl.getEmployeeId());
            stm.executeUpdate();
        }
    }


    public void FindEmployeeByName(){
        Integer employeeId = null;
        String name="Ann";
        //1.  Найдите ID сотрудника с именем Ann. Если такой сотрудник только один, то установите его
        //департамент в HR.
        try(Connection con = ServiceRepoImpl.getConnection()) {
            PreparedStatement stm = con.prepareStatement("SELECT employeeid FROM \"HW4\".employee WHERE  employee.\"Name\"= ?");
            stm.setString(1, name);
            ResultSet rs = stm.executeQuery();
            int count = 0;
            while (rs.next()) {
                if (count == 0) {
                    employeeId = rs.getInt("employeeid");
                }
                count++;
            }
            if (count != 1) {
                employeeId = null;
            }

            if (employeeId!=null){ //т.е. если нашли одну Ann устноваить ей departmentId HR
                try(PreparedStatement updStm = con.prepareStatement("UPDATE \"HW4\".employee SET departmentid = (" +
                        "SELECT departmentid FROM \"HW4\".department WHERE department.\"name\" = 'HR') " +
                        "WHERE employeeid = ?")){
                    updStm.setInt(1,employeeId);
                    updStm.executeUpdate();}
                catch (Exception e){System.out.println(e);}
            }

            //2.  Проверьте имена всех сотрудников. Если чьё-то имя написано с маленькой буквы, исправьте её на большую. Выведите на экран количество исправленных имён.
            int updatedNames=0;
            try (PreparedStatement psGetNames = con.prepareStatement(
                    "SELECT employeeid, employee.\"Name\" as nm FROM \"HW4\".employee")) {
                try (ResultSet rsNames = psGetNames.executeQuery()) {
                    while (rsNames.next()) {
                        String n = rsNames.getString("nm");
                        int eId = rsNames.getInt("employeeid");
                        if (n != null && !n.isEmpty() && Character.isLowerCase(n.charAt(0))) {
                            String correctedName = Character.toUpperCase(n.charAt(0)) + n.substring(1);
                            try (PreparedStatement psUpdateName = con.prepareStatement(
                                    "UPDATE \"HW4\".employee SET \"Name\"= ? WHERE employeeid = ?")) {
                                psUpdateName.setString(1, correctedName);
                                psUpdateName.setInt(2, eId);
                                psUpdateName.executeUpdate();
                                updatedNames++;
                            }
                        }
                    }
                }
            }
            System.out.println("Количество исправленных имён: " + updatedNames);

            //3. Выведите на экран количество сотрудников в IT-отделе
            int countIT = 0 ;
            try (PreparedStatement psITEmployee = con.prepareStatement(
                    "SELECT count(*) FROM \"HW4\".employee WHERE departmentid in " +
                            "(SELECT departmentid FROM \"HW4\".department WHERE department.\"name\" = 'IT')")) {
                try (ResultSet rsCount = psITEmployee.executeQuery()) {
                    if (rsCount.next()){
                        countIT =  rsCount.getInt(1);
                    }
                }
            }
            System.out.println("Количество сотрудников IT-отдела: "+countIT);

        } catch (Exception e){System.out.println(e);}

    }

}

