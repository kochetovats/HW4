package office;

import com.sun.source.tree.AssertTree;
//import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    ServiceRepoMock repoMock ;

    @BeforeEach
    void setup() throws SQLException {
        try(Connection con = repoMock.getConnection()) {
            Service.createDB(con);
        } catch (SQLException e) {
            System.out.println(e);
        }//приводим базу к первоначальному виду
    }

    @AfterEach
    void teardown() throws SQLException {
        try(Connection con = repoMock.getConnection()) {
            Service.createDB(con);
        } catch (SQLException e) {
            System.out.println(e);
        }
        //приводим базу к первоначальному виду
    }

    @Test
    @DisplayName("Проверка удаление отдела - при этом должны удаляться все сотрудники этого подразделения")
    public void   testDepartmentDeletion(){
        Department dep = new Department(4,"Test");
        try(Connection con = repoMock.getConnection()){
            Employee emp = new Employee(6,"Ivan Ivanov",dep.departmentID);
            Service.addDepartment(dep,con);
            Service.addEmployee(emp, con);
            System.out.println("Finding employee with departmentId = "+dep.getDepartmentID());
            PreparedStatement stm = con.prepareStatement("SELECT employee.id, employee.name, employee.departmentid from \"HW4\".employee where employee.departmentid = ?;");
            stm.setInt(1, dep.getDepartmentID());
            //Statement stm = con.createStatement();
            //ResultSet rs = stm.executeQuery("Select * from \"HW4\".employee order by id desc;");
            //System.out.println("===================="+stm.toString());
            ResultSet rs = stm.executeQuery();
            if(rs.next()){
                System.out.println("Employee with departmenid = " +dep.getDepartmentID() + " is found.");
                System.out.println("ID: "+rs.getInt("id")+ ", name: "+rs.getString("name") + ", departmentid: "+rs.getInt("departmentid"));
            }
        }catch (SQLException e) {
            System.out.println(e);
        }


        boolean isEmployeePresent = false;
        try(Connection con = repoMock.getConnection()){
            Service.removeDepartment(dep,con);
            System.out.println("Deleted employee with departmentid "+dep.getDepartmentID());
            //System.out.println("Finding employee with departmentId = "+dep.getDepartmentID());
            PreparedStatement stm = con.prepareStatement("SELECT employee.id from \"HW4\".employee where employee.departmentId = ?");
            stm.setInt(1, dep.departmentID);
            ResultSet rs = stm.executeQuery();
            ArrayList<Integer>  employees = new ArrayList<Integer>();
            if(rs.next()){
                isEmployeePresent=true;
            }
        }catch (SQLException e) {
            System.out.println(e);
        }
        Assertions.assertFalse(isEmployeePresent);
    }


    @Test
    @DisplayName("Проверка добавление нового отдела.")
    public void   testDepartmentAdd(){
        Department dep = new Department(7,"Test");
        boolean DepartmentPresent = false;
        try (Connection con= repoMock.getConnection()){
            Service.addDepartment(dep,con);
            PreparedStatement stm = con.prepareStatement("SELECT id, name" +
                    " from \"HW4\".department where  Department.ID = ?");
            stm.setInt(1, dep.getDepartmentID());
            ResultSet rs = stm.executeQuery();
            if(rs.next() && rs.getInt("id")==dep.getDepartmentID() && rs.getString("name").equals(dep.getName())){
                DepartmentPresent=true;
        }
        }catch (SQLException e) {
            System.out.println(e);
        }
        Assertions.assertTrue(DepartmentPresent);
    }

}

