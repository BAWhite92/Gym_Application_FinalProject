
package gymbookingapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.*;

/**
 *
 * @author Ben
 */
public class Parties
{
    String username;
    char[] password;
    String name;
    LocalDate dateOfBirth;
    String securityAnswer;
    String dobString;
    String email;
    
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Connection connect = null;
    Statement statement = null;
    
    public void MySQLAccess() throws Exception {
    connect = DriverManager.getConnection("jdbc:mysql://localhost"
        + "/gym_app?user=root&password=Eairnon14");
            
    //Statements to issue SQL queries to the database
    statement = connect.createStatement();
    }
    
}
