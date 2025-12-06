package edu.innotech;
import java.sql.*;
import java.util.ArrayList;

import static java.lang.System.exit;

import lombok.SneakyThrows;
import office.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class Service {

    private String url = "jdbc:postgresql://localhost:5432/postgres"; // замените на ваш URL базы данных
    private String user = "postgres"; // ваш логин
    private String password = "postgres"; // ваш пароль

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public Connection getConnection () throws SQLException {
        Connection con = null;
        try{
            con = DriverManager.getConnection(this.getUrl(),this.getUser(),this.getPassword());
        }
        catch(Exception e){
            System.out.println(e);
            exit(-1);
        }
        return con;
        }


    public void AddEmployee(int employeeid, String name, int departmentid) throws SQLException {
        try(Connection con = this.getConnection()){
         PreparedStatement stm = con.prepareStatement("INSERT INTO \"HW4\".employee VALUES(?,?,?)");
         stm.setInt(1,employeeid);
         stm.setString(2,name);
         stm.setInt(3,departmentid);
         stm.executeUpdate();
        } catch(Exception e){
            System.out.println(e);
        }

    }

    public void AddDepartment(String name, int departmentid) throws SQLException {
        try(Connection con = this.getConnection()){
            PreparedStatement stm = con.prepareStatement("INSERT INTO \"HW4\".department VALUES(?,?)");
            stm.setInt(1,departmentid);
            stm.setString(2,name);
            stm.executeUpdate();
    }
    }

    public void FindEmployeeByName(){
        Integer employeeId = null;
        String name="Ann";
        //1.  Найдите ID сотрудника с именем Ann. Если такой сотрудник только один, то установите его
        //департамент в HR.
        try(Connection con = this.getConnection()) {
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

    @SneakyThrows
    @Test
    public void checkDepartmentsDeletion() throws SQLException {
        int departmentId = 3;
            //get employee with department
        ArrayList<Integer> employees = new ArrayList();
            try(Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")){
                PreparedStatement stm = con.prepareStatement("SELECT  Employee.ID, Employee.Name,Department.Name as DepName" +
                        " from Employee join Department on Employee.DepartmentID = Department.ID where  Department.ID = ?");
                stm.setInt(1, departmentId);
                ResultSet rs = stm.executeQuery();
                System.out.println("------------------------------------");
                //ResultSetMetaData metaData= rs.getMetaData();
                while(rs.next()){
                    employees.add(rs.getInt("ID"));
                    //System.out.println(rs.getInt("ID")+"\t"+rs.getString("NAME")+"\t"+rs.getString("DepName"));
                }
                System.out.println("------------------------------------");
            }catch (SQLException e) {
                System.out.println(e);
            }
            //delete department
            office.Service  service = new office.Service();
            office.Service.removeDepartment(new Department(departmentId,"somename"), DriverManager.getConnection("jdbc:h2:.\\Office"));

            //check employee again
        boolean isEmployeePresent = false;
        try(Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")){
                PreparedStatement stm = con.prepareStatement("SELECT  Employee.ID, Employee.Name,Department.Name as DepName" +
                        " from Employee join Department on Employee.DepartmentID = Department.ID where  Department.ID = ?");
                stm.setInt(1, departmentId);
                ResultSet rs = stm.executeQuery();
                if (rs.next()){isEmployeePresent=true;}
            if (!isEmployeePresent)
            {System.out.println("Система работает корректно, после удаление сотрудников с удаленным  подразделением не найдено");}
            else System.out.println("Система работает НЕ корректно, после удаление сотрудники с удаленным  подразделением найдены");
        }catch (SQLException e) {
            System.out.println(e);
        }
        Assertions.assertFalse(isEmployeePresent);
    }
}



/*
    public class EmployeeOperations {

        public static void performOperations() {


            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                conn.setAutoCommit(false); // для возможности отката если что-то пойдет не так

                // 1. Найти ID сотрудника с именем Ann
                int annId = -1;
                try (PreparedStatement psFindAnn = conn.prepareStatement(
                        "SELECT employeeid FROM \"HW4\".employee WHERE \"Name\" = ?")) {
                    psFindAnn.setString(1, "Ann");
                    try (ResultSet rs = psFindAnn.executeQuery()) {
                        if (rs.next()) {
                            annId = rs.getInt("employeeid");
                            if (rs.next()) {
                                // Собственно, если есть еще один, то annId остается -1,
                                // т.к. условие "только один"
                                annId = -1;
                            }
                        }
                    }
                }

                // 2. Если только один сотрудник Ann, установить его департамент в HR
                if (annId != -1) {
                    try (PreparedStatement psUpdateDept = conn.prepareStatement(
                            "UPDATE \"HW4\".employee SET departmentid = (SELECT departmentid FROM \"HW4\".department WHERE name = 'HR') WHERE employeeid = ?")) {
                        psUpdateDept.setInt(1, annId);
                        psUpdateDept.executeUpdate();
                    }
                }

                // 3. Исправить имена с маленькой буквы на большую
                int correctedCount = 0;
                try (PreparedStatement psNames = conn.prepareStatement(
                        "SELECT employeeid, \"Name\" FROM \"HW4\".employee")) {
                    try (ResultSet rs = psNames.executeQuery()) {
                        while (rs.next()) {
                            String name = rs.getString("\"Name\"");
                            String employeeIdStr = rs.getString("employeeid");
                            int employeeId = rs.getInt("employeeid");
                            if (name != null && !name.isEmpty() && Character.isLowerCase(name.charAt(0))) {
                                String correctedName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                                try (PreparedStatement psUpdateName = conn.prepareStatement(
                                        "UPDATE \"HW4\".employee SET \"Name\" = ? WHERE employeeid = ?")) {
                                    psUpdateName.setString(1, correctedName);
                                    psUpdateName.setInt(2, employeeId);
                                    psUpdateName.executeUpdate();
                                    correctedCount++;
                                }
                            }
                        }
                    }
                }
                System.out.println("Исправлено имён: " + correctedCount);

                // 4. Количество сотрудников в IT-отделе
                int countItDept = 0;
                try (PreparedStatement psCountIT = conn.prepareStatement(
                        "SELECT COUNT(*) FROM \"HW4\".employee e JOIN \"HW4\".department d ON e.departmentid = d.departmentid WHERE d.name = 'IT'")) {
                    try (ResultSet rs = psCountIT.executeQuery()) {
                        if (rs.next()) {
                            countItDept = rs.getInt(1);
                        }
                    }
                }
                System.out.println("Количество сотрудников в IT-отделе: " + countItDept);

                conn.commit(); // зафиксировать все изменения, если всё прошло успешно

            } catch (SQLException e) {
                e.printStackTrace();
                // В случае ошибки можно откатить транзакцию
                // conn.rollback(); // но тут нельзя, т.к. try-with-resources закрывает соединение
            }
        }
    }
}
*/