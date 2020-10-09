
package gymbookingapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Ben
 */
public class Reservation
{
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Connection connect = null;
    Statement statement = null;
    
    
    String classDay;
    LocalTime classTime;
    LocalDate classDate;
    String className;
    int reserver;
    String classType;
    
    //used to add new reservations
    Reservation(String day, LocalTime time, LocalDate date, String name, int memNum, String type){
        classDay = day;
        classTime = time;
        classDate = date;
        className = name;
        reserver = memNum;
        classType = type;
        
    }
    
    //used to access reservation methods by removeMember
    Reservation(){
        
    }
    
    void addReservation(Reservation res) throws Exception{
        DateTimeFormatter dtfT = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dtfD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try{   
            MySQLAccess();
            preparedStatement = connect
                    .prepareStatement("INSERT INTO upcoming_classes"
                            + " VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, res.reserver);
            preparedStatement.setString(2, res.classDay);
            preparedStatement.setString(3, res.classTime.format(dtfT));
            preparedStatement.setString(4, res.classDate.format(dtfD));
            preparedStatement.setString(5, res.className);
            preparedStatement.setString(6, res.classType);
            preparedStatement.executeUpdate();
        } 
        catch (Exception e){
            throw e;
        }
    }
    
    
    void removeReservation() throws Exception{
        DateTimeFormatter dtfD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("DELETE FROM upcoming_classes "
                    + "WHERE Member_Reserved = ? AND Class_Date = ? AND Class_Name = ?");
            preparedStatement.setInt(1, this.reserver);
            preparedStatement.setString(2, this.classDate.format(dtfD));
            preparedStatement.setString(3, this.className);
            preparedStatement.executeUpdate();
        }
        catch (Exception e){
            throw e;
        }
    }
    
    void removeAllReservations(int memNo) throws Exception{
        try{
            MySQLAccess();
            System.out.println(memNo);
            preparedStatement = connect.prepareStatement("DELETE FROM upcoming_classes "
                    + "WHERE Member_Reserved = ?");
            preparedStatement.setInt(1, memNo);
            preparedStatement.executeUpdate();
        }
        catch(Exception e){
            throw e;
        }
    }
    
    public void MySQLAccess() throws Exception {
    connect = DriverManager.getConnection("jdbc:mysql://localhost"
        + "/gym_app?user=root&password=Eairnon14");
            
    //Statements to issue SQL queries to the database
    statement = connect.createStatement();
}
}
