
package gymbookingapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

/**
 * @author Ben
 */
public class Class
{
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Connection connect = null;
    Statement statement = null;
    
    String classDateString;
    String classTimeString;
    String classDay;
    int spacesAvailable;
    LocalDate classDate;
    LocalTime classTime;
    String classType;
    String name;
    String classInfo; //Find way to show paragraph, possibly using UI elements like html?
    ArrayList<String> membersEnrolled = new ArrayList<>();
    int id;
    
    Class(String nme, String date, String time, String type, String info, int spaces){
        //this is for a new class, fill spaces etc no checks for spaces taken by bookings
        name = nme;
        classDate = LocalDate.parse(date);
        classTime = LocalTime.parse(time);
        classType = type;
        classInfo = info;
        spacesAvailable = spaces;
        
    }
    
    Class(String nme, String date, String time, String day, String type){
        //this is skeleton class used for upcoming classes.
        name = nme;
        classDateString = date;
        classTimeString = time;
        classDay = day; 
        classType = type;
    }
    
    
    Class(int ID, String nme, String date, String time, String type, String info, int spaces) throws Exception{
        //this is for a class which will be checked for bookings already present and is created from database info
        //is complete Class instance not a partial one
        id = ID;
        name = nme;
        classDate = LocalDate.parse(date);
        classTime = LocalTime.parse(time);
        classType = type;
        classInfo = info;
        spacesAvailable = spaces;

        try{
            MySQLAccess();
            preparedStatement = connect
                    .prepareStatement("SELECT * FROM members_enrolled "
                            + "WHERE Booked_Class_ID=?");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                membersEnrolled.add(resultSet.getString("User_Enrolled"));
            }
        } catch (Exception e) {
            throw e;
        }
    }
    
    Class(){
        
    }
    
    
    void addClass(String nme, String date, String time, String type, String info, int spaces) throws Exception{
        try{
            MySQLAccess();
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO classes (Name, Class_Date, "
                            + "Class_Time, Class_Type, Class_Info, Spaces_Available)"
                            + " VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, nme);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, time);
            preparedStatement.setString(4, type);
            preparedStatement.setString(5, info);
            preparedStatement.setInt(6, spaces);
            preparedStatement.executeUpdate();
        } 
        catch (Exception e){
            throw e;
        }
        
    }
    
    void removeClass() throws Exception{
        try{
            MySQLAccess();
            //remove the reservations for each member enrolled on the class
            for(int i = 0; i < this.membersEnrolled.size(); i++){
                String username = null, pass = null, memName = null, DOB = null, secAns = null, email = null;
                int memNum = 0, strikes = 0, goalProg = 0, achProg = 0;
        
                preparedStatement = connect
                    .prepareStatement("SELECT * FROM gym_app.gym_members WHERE "
                            + "Username=?");
                preparedStatement.setString(1, membersEnrolled.get(i));
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()){
                    username = resultSet.getString("Username");
                    memNum = resultSet.getInt("Member_Number");
                    pass = resultSet.getString("Password");
                    memName = resultSet.getString("Name");
                    DOB = resultSet.getString("DOB");
                    secAns = resultSet.getString("Security_Answer");
                    strikes = resultSet.getInt("Strikes");
                    goalProg = resultSet.getInt("Goal_Progress");
                    achProg = resultSet.getInt("Achieve_Progress");
                    email = resultSet.getString("Email");
                }
                char[] passw = pass.toCharArray();
                Member m = new Member(username, memNum, passw, memName, DOB, secAns,
                    strikes, goalProg, achProg, email);
                m.cancelReservation(this);
            }    
        //Last delete the actual class from the database
            preparedStatement = connect.prepareStatement("DELETE FROM gym_app.classes "
                    + "WHERE Class_ID = ?");
            preparedStatement.setInt(1, this.id);
            preparedStatement.executeUpdate();
        } 
        catch(Exception e){
            throw e;
        }
    }

    
    
    void decreaseSpaces() throws Exception{
        int space = this.spacesAvailable - 1;
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("UPDATE classes "
                    + "SET Spaces_Available = ? WHERE Class_ID = ?");
            preparedStatement.setInt(1, space);
            preparedStatement.setInt(2, this.id);
            preparedStatement.executeUpdate();
        } catch (Exception e){
            throw e;
        }                 
    }
    
    void increaseSpaces() throws Exception{
        int space = this.spacesAvailable + 1;
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("UPDATE classes "
                    + "SET Spaces_Available = ? WHERE Class_ID = ?");
            preparedStatement.setInt(1, space);
            preparedStatement.setInt(2, this.id);
            preparedStatement.executeUpdate();
        } catch (Exception e){
            throw e;
        }
    }
        
    
    void addEnrolledMem(String mem) throws Exception{
            try{
                MySQLAccess();
                preparedStatement = connect.prepareStatement("INSERT INTO "
                        + "members_enrolled (Booked_Class_ID, User_Enrolled)"
                        + " VALUES (?, ?)");
                preparedStatement.setInt(1, this.id);
                preparedStatement.setString(2, mem);
                preparedStatement.executeUpdate();

            } catch (Exception e){
                throw e;
            }
        }
    
    
    void removeEnrolledMember(String mem) throws Exception{ //WHEN FRIENDS ADDED UPDATE THIS
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("DELETE FROM members_enrolled"
                    + " WHERE Booked_Class_ID = ? AND User_Enrolled = ?");
            preparedStatement.setInt(1, this.id);
            preparedStatement.setString(2, mem);
            preparedStatement.executeUpdate();
        } catch (Exception e){
            throw e;
        }
    }
    
    void removeAllMemEnrollments(String mem) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("DELETE FROM gym_app.members_enrolled "
                + "WHERE User_Enrolled = ?");
            preparedStatement.setString(1, mem);
            preparedStatement.executeUpdate();
        }
        catch (Exception e){
            throw e;
        }
    }
    
    void update(int ID, String nme, String date, String time, String type, 
                                    String info, int spaces) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement(
                    "UPDATE gym_app.classes "
                  + "SET "
                            + "Name = ?, "
                            + "Class_Date = ?, "
                            + "Class_Time = ?, "
                            + "Class_Type = ?, "
                            + "Class_Info = ?, "
                            + "Spaces_Available = ? "
                  + "WHERE "
                            + "Class_ID = ?");
            preparedStatement.setString(1, nme);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, time);
            preparedStatement.setString(4, type);
            preparedStatement.setString(5, info);
            preparedStatement.setInt(6, spaces);
            preparedStatement.setInt(7, ID);
            preparedStatement.executeUpdate();
            
            //NEED TO INFORM ALL ENROLLED MEMBERS HERE OF THE UPDATE
            LocalDate cLocDate = LocalDate.parse(date);
            //Get day of week for date
            DayOfWeek classD = cLocDate.getDayOfWeek();
            String classDay = classD.getDisplayName(TextStyle.FULL, Locale.UK);
            preparedStatement = connect.prepareStatement(
                    "UPDATE gym_app.upcoming_classes "
                  + "SET "
                            + "Class_Day = ?, "
                            + "Class_Time = ?, "
                            + "Class_Date = ?, "
                            + "Class_Name = ? "
                  + "WHERE "
                            + "Class_Name = ? AND Class_Date = ? AND "
                            + "Class_Time = ?");
            preparedStatement.setString(1, classDay);
            preparedStatement.setString(2, time);
            preparedStatement.setString(3, date);
            preparedStatement.setString(4, nme);
            preparedStatement.setString(5, this.name);
            preparedStatement.setString(6, this.classDate.toString());
            preparedStatement.setString(7, this.classTime.toString());
            preparedStatement.executeUpdate();
            } catch (Exception e){
            throw e;
        }}
    
    public final void MySQLAccess() throws Exception {
    connect = DriverManager.getConnection("jdbc:mysql://localhost"
        + "/gym_app?user=root&password=Eairnon14");
            
    //Statements to issue SQL queries to the database
    statement = connect.createStatement();
}
}
