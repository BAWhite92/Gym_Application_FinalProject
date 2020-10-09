
package gymbookingapp;
import java.util.*;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;


/**
 * @author Ben
 */
public class GymBookingApp
{
    private static PreparedStatement preparedStatement = null;
    private static ResultSet resultSet = null;
    private static Connection connect = null;
    private static Statement statement = null;
    
  
    void createOwner(String usr, char[] pass, String nme, String dob, 
            String secAns, String email) throws Exception{
        Owner ownr = new Owner(usr, pass, nme, dob, secAns, email);
        ownr.addNewOwner(ownr);
        close();
    }
    
    void removeOwner(String usr) throws Exception{
        String usrName = null, password = null, name = null, dob = null, secAns = null;
        String gymRole = null, email = null;
        
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM trainers_owners"
                    + " WHERE Username = ?");
            preparedStatement.setString(1, usr);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                usrName = resultSet.getString("Username");
                password = resultSet.getString("Password");
                name = resultSet.getString("Name");
                dob = resultSet.getString("DOB");
                secAns = resultSet.getString("Security_Answer");
                gymRole = resultSet.getString("Role");
                email = resultSet.getString("Email");
            }
            char[] passw = password.toCharArray();
            if("Owner".equals(gymRole)){
                Owner ownr = new Owner(usrName, passw, name, dob, secAns, email);
                ownr.removeOwner();
            }
        } 
        catch (Exception e){
            throw e;
        }
        close();
    }
    
    
    void createTrainer(String usr, char[] pass, String nme, String dob, 
            String secAns, String mail) throws Exception{
        Trainer trnr = new Trainer(usr, pass, nme, dob, secAns, mail);
        trnr.addNewTrainer(trnr);
        close();
    }
    
    void removeTrainer(String usr) throws Exception{
        String usrName = null, password = null, name = null, dob = null, secAns = null;
        String gymRole = null, email = null;
        //get trainer info from database (cannot use getTrainerInfo as need Role to be checked)
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM trainers_owners"
                    + " WHERE Username = ?");
            preparedStatement.setString(1, usr);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                usrName = resultSet.getString("Username");
                password = resultSet.getString("Password");
                name = resultSet.getString("Name");
                dob = resultSet.getString("DOB");
                secAns = resultSet.getString("Security_Answer");
                gymRole = resultSet.getString("Role");
                email = resultSet.getString("Email");
            }
            char[] passw = password.toCharArray();
            if("Trainer".equals(gymRole)){
                Trainer trnr = new Trainer(usrName, passw, name, dob, secAns, email);
                trnr.removeTrainer();
            }
        } 
        catch (Exception e){
            throw e;
        }
        close();
    }
    
    void updateMember(String usr, String name, String usrname, String dob, 
            String secAns, String email) throws Exception{

        Member mem = getMemberInfo(usr);
        mem.updateMember(name, dob, secAns, email);
        close();
        }
    
    void updateTrainer(String usr, String name, String usrname, String dob, 
            String secAns, String email) throws Exception{

        Trainer trnr = getTrainerInfo(usr);
        trnr.updateTrainer(name, dob, secAns, email);
        close();
        }
    
    void updateOwner(String usr, String name, String usrname, String dob, 
            String secAns, String email) throws Exception{

        Owner ownr = getOwnerInfo(usr);
        ownr.updateOwner(name, dob, secAns, email);
        close();
        } 
        
    boolean logInAdmin(String usr, char[] pass) throws Exception{
        String usrName = null, password = null, name = null, dob = null, 
                secAns = null;
        String gymRole = null, email = null;
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM trainers_owners"
                    + " WHERE Username = ?");
            preparedStatement.setString(1, usr);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.isBeforeFirst()){
            while (resultSet.next()){
                    usrName = resultSet.getString("Username");
                    password = resultSet.getString("Password");
                    name = resultSet.getString("Name");
                    dob = resultSet.getString("DOB");
                    secAns = resultSet.getString("Security_Answer");
                    gymRole = resultSet.getString("Role");
                    email = resultSet.getString("Email");
                }
                char[] passw = password.toCharArray();
                switch (gymRole)
                {
                    case "Trainer":
                        Trainer trnr = new Trainer(usrName, passw, name, dob, 
                                secAns, email);
                        close();
                        return trnr.passwordCheck(pass);
                    case "Owner":
                        Owner ownr = new Owner(usrName, passw, name, dob, 
                                secAns, email);
                        close();
                        return ownr.passwordCheck(pass);
                    default:
                        close();
                        return false;
                    }
            }
            else{
                close();
                return false;
            } 
        }
        catch(Exception e){
            throw e;
        }
    }
    
    
    void createMember(String usr, char[] pass, String nme, String dob, 
            String secAns, String email) throws Exception{
        
        Member mem = new Member(usr, pass, nme, dob, secAns, email);
        mem.addNewMember(usr, pass, nme, dob, secAns, email);
        close();
    }
    
    void removeMember(String UserName) throws Exception{
        try{
            MySQLAccess();
            Member m = getMemberInfo(UserName);
        
            m.removeMember();
        }
        catch(Exception e){
            throw e;
        }
        close();
    }
    
    boolean logInMember(String usr, char[] pass) throws Exception{
        try{
            MySQLAccess();
            //establish whether the Username exists within the database.
            preparedStatement = connect.prepareStatement("SELECT * FROM gym_members"
                    + " WHERE Username = ?");
            preparedStatement.setString(1, usr);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.isBeforeFirst()){
                Member m = getMemberInfo(usr);
                close();
                //Now that User is confirmed to exist validate password supplied
                return m.passwordCheck(pass);
            } 
            else{
                close();
                return false;            
            }
        }
        catch(Exception e){
            throw e;
        }

    }
    
    //check on date may be removed once it can be put in UI at later date
    void addClass(String nme, String date, String time, String type, String info, 
            int spaces) throws Exception{
        if (DateFormatHelper.dateFormat(date)){
        Class c = new Class(nme, date, time, type, info, spaces);
        c.addClass(nme, date, time, type, info, spaces);    
        close();
        }
    }
    
    void removeClass(String className, String date, String time) throws Exception{
        Class c = getClassInfo(className, date, time);
        c.removeClass();
        close();
    }
    
    boolean bookingCheck(String className, String date, String time, 
            String UserName) throws Exception{
       //Create Class object to send onwards
        Class c = getClassInfo(className, date, time);
        for (String m : c.membersEnrolled){
            if(UserName.equals(m)) {
                close();
                return false;                
                }
        }
        close();
        return true;
    }
    
    
    boolean bookClass(String className, String date, String time, 
            String UserName) throws Exception{
        try{
            MySQLAccess();
            //Create Class object to send onwards
            Class c = getClassInfo(className, date, time);
        
            if (c.spacesAvailable > 0){
                //Create Member object to perform class add
                Member m = getMemberInfo(UserName);
            
                if(m.strikes < 3){
                    m.bookOnClass(c);
                    close();
                    return true;
                }
            }
        } catch (Exception e){
            throw e;
        }
        close();
        return false;
    }
    
    void cancelBooking(String className, String classDate, String classTime, 
            String UserName) throws Exception{
        MySQLAccess();
        Member m = getMemberInfo(UserName);
        Class c = getClassInfo(className, classDate, classTime);
        m.cancelReservation(c);
        close();
    }
    
    ArrayList getClasses() throws Exception{
        ArrayList<Class> classList = new ArrayList<>();
        String name, time, date, type, info;
        int spaces;
        
        MySQLAccess();
        preparedStatement = connect
                    .prepareStatement("SELECT * FROM gym_app.classes");
        resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
                name = resultSet.getString("Name");
                time = resultSet.getString("Class_Time");
                date = resultSet.getString("Class_Date");
                type = resultSet.getString("Class_Type");
                info = resultSet.getString("Class_Info");
                spaces = resultSet.getInt("Spaces_Available");
                
                classList.add(new Class(name, date, time, type, info, spaces));
            }
        close();
        return classList;

        }
    
        
        
    ArrayList viewUpcomingClasses(String UserName) throws SQLException, Exception{
        MySQLAccess();
        Member m = getMemberInfo(UserName);
        ArrayList<Class> classes = m.seeUpcomingClasses(m.memberNum);
        close();
        return classes;
    }
    
    
    void viewAccount(String UserName) throws SQLException, Exception {

        MySQLAccess();
        
        
        Member m = getMemberInfo(UserName);
        System.out.println("User is " + m.username + m.memberNum + m.name + 
                    m.goalProgress + m.achievementPoints);
        close();
    }
    
    
    boolean updateClass(String className, String date, String time, 
            String newName, String newDate, String newTime, String newType,
            String newInfo, int newSpaces) throws Exception{
        try{
            MySQLAccess();
        
            //Create Class object to send onwards
            Class c = getClassInfo(className, date, time);
            if(c != null){
            c.update(c.id, newName, newDate, newTime, 
                    newType, newInfo, newSpaces);
            close();
            return true;
            }
            else{
                close();
                return false;
            }
        } catch(Exception e){
            throw e;
        }
        
    }
    
    
    void memberPasswordReset(String UserName, char[] pass, 
            char[] newPass) throws Exception{
        try{
            MySQLAccess();
            //create member to get password info.
            Member m = getMemberInfo(UserName);
            if(m.passwordCheck(pass)){
                m.setNewPass(newPass);
                System.out.println("Success");
                close();
            } else{
                System.out.println("Incorrect Password try again!");
                close();
            }
            
        } catch (Exception e){
            throw e;
        }
    }
    
    void trainerPasswordReset(String UserName, char[] pass, 
            char[] newPass) throws Exception{
        try{
            Trainer trnr = getTrainerInfo(UserName);
            
            //make sure that the provided password matches the current password
            if(trnr.passwordCheck(pass)){
                trnr.setNewPass(newPass);
                System.out.println("Success");
                close();
            } else {
                System.out.println("Incorrect password try again");
                close();
            }
        } catch (Exception e){
            throw e;
        }
    }
    
    
    void ownerPasswordReset(String UserName, char[] pass, 
            char[] newPass) throws Exception{
        try{
            Owner ownr = getOwnerInfo(UserName);
            
            //make sure that the provided password matches the current password
            if(ownr.passwordCheck(pass)){
                ownr.setNewPass(newPass);
                System.out.println("Success");
                close();
            } else {
                System.out.println("Incorrect password try again");
                close();
            }
        } catch (Exception e){
            throw e;
        }
    }
    
    void addTrainerUpdate(String date, String news) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("INSERT INTO news"
                    + " (news_date, trainer_update) VALUES (?,?)");
            preparedStatement.setString(1, date);
            preparedStatement.setString(2, news);
            preparedStatement.executeUpdate();
        }
        catch(Exception e){
        throw e;
        }
        close();
    }
    
    void addNewsItem(String date, String news) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("INSERT INTO news"
                    + " (news_date, news_item) VALUES (?, ?)");
            preparedStatement.setString(1, date);
            preparedStatement.setString(2, news);
            preparedStatement.executeUpdate();
        }
        catch (Exception e){
            throw e;
        }
        close();
    }
    
    String[][] getNewsItems() throws Exception{
        String newsDate = null, newsItem = null;
        String[][] listedNewsItems = new String[5][2];
        ArrayList<String> newsTemp = new ArrayList<>();
        ArrayList<String> dateTemp = new ArrayList<>();
        
        
        LocalDate dateCheck = LocalDate.now().minusMonths(1);
        String date = dateCheck.toString();
        
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.toString();
        
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM news"
                    + " WHERE news_date >= ? AND news_date <= ? AND news_item IS NOT NULL");
            preparedStatement.setString(1, date);
            preparedStatement.setString(2, currentDateString);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                newsDate = resultSet.getString("news_date");
                newsItem = resultSet.getString("news_item");
                newsTemp.add(newsItem);
                dateTemp.add(newsDate);
            }
            //Need the last 5 entries in the database to ensure they are the newest entries.
            //set up variables to aid in getting these entries.
            if(!newsTemp.isEmpty()){
            int len = dateTemp.size() - 1;
            int x = 0;
            //iterate over last 5 entries in the arrays.
            for(int i = 0; i <= 4; i++){
                if(len >= 0){
                listedNewsItems[x][0] = dateTemp.get(len);
                listedNewsItems[x][1] = newsTemp.get(len);
                len--;
                x++;
            }}
            close();
            return listedNewsItems;
            }
        }
        catch (Exception e){
            throw e;
        }
        return null;
    }    
    
    String[][] getTrainerUpdates() throws Exception{
        String newsDate = null, newsItem = null;
        String[][] listedTrainerNews = new String[5][2];
        ArrayList<String> newsTemp = new ArrayList<>();
        ArrayList<String> dateTemp = new ArrayList<>();
        
        
        LocalDate dateCheck = LocalDate.now().minusMonths(1);
        String date = dateCheck.toString();
        
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.toString();
        
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM news"
                    + " WHERE news_date >= ? AND news_date <= ? AND "
                    + "trainer_update IS NOT NULL");
            preparedStatement.setString(1, date);
            preparedStatement.setString(2, currentDateString);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                newsDate = resultSet.getString("news_date");
                newsItem = resultSet.getString("trainer_update");
                newsTemp.add(newsItem);
                dateTemp.add(newsDate);
            }
            //Need the last 5 entries in the database to ensure they are the newest entries.
            //set up variables to aid in getting these entries.
            if(!newsTemp.isEmpty()){
            int len = dateTemp.size() - 1;
            int x = 0;
            //iterate over last 5 entries in the arrays.
            for(int i = 0; i <= 4; i++){
                if(len >= 0){
                listedTrainerNews[x][0] = dateTemp.get(len);
                listedTrainerNews[x][1] = newsTemp.get(len);
                len--;
                x++;
            }}
            close();
            return listedTrainerNews;
        }
        }
        catch (Exception e){
            throw e;
        }
        return null;
    }     
    
    boolean addFriendRequest(String requester, String friend) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT 1 FROM gym_members "
                    + "WHERE Username = ?");
            preparedStatement.setString(1, friend);
            resultSet = preparedStatement.executeQuery();
                        
            if(resultSet.isBeforeFirst()){
                preparedStatement = connect.prepareStatement("SELECT 1 FROM"
                    + " friend_request WHERE (Member_Name = ? AND "
                        + "Requesting_Mem_Username = ?)"
                        + " OR (Member_Name = ? AND Requesting_Mem_Username = ?)");
                preparedStatement.setString(1, friend);
                preparedStatement.setString(2, requester);
                preparedStatement.setString(3, requester);
                preparedStatement.setString(4, friend);
                resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    return false;
                }
                else{
                preparedStatement = connect.prepareStatement("SELECT 1 FROM"
                    + " friends_list WHERE (Member = ? AND Friend = ?)"
                        + " OR (Member = ? AND Friend = ?)");
                preparedStatement.setString(1, friend);
                preparedStatement.setString(2, requester);
                preparedStatement.setString(3, requester);
                preparedStatement.setString(4, friend);
                resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    return false;
                }
                else{
                    preparedStatement = connect.prepareStatement("INSERT INTO friend_request "
                        + "(Member_Name, Requesting_Mem_Username) VALUES (?, ?)");
                    preparedStatement.setString(1, friend);
                    preparedStatement.setString(2, requester);
                    preparedStatement.executeUpdate();
                    return true;
                }
                }
            } 
            else{
                return false;
            }
            
        }
        catch(Exception e){
            throw e;
        }
    }
    
    ArrayList getFriendRequests(String usrnme) throws Exception{
        ArrayList<String> requesters = new ArrayList<>();
        String name;
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM friend_request "
                    + "WHERE Member_Name = ?");
            preparedStatement.setString(1, usrnme);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                name = resultSet.getString("Requesting_Mem_Username");
                
                requesters.add(name);
            }
            close();
            return requesters;   
    }
    
    void rejectFriendRequest(String requester, String usr) throws Exception{
        MySQLAccess();
        preparedStatement = connect.prepareStatement("DELETE FROM friend_request "
                + "WHERE Requesting_Mem_Username = ? AND Member_Name = ? ");
        preparedStatement.setString(1, requester);
        preparedStatement.setString(2, usr);
        preparedStatement.executeUpdate();
        close();
    }
    
    void acceptFriendRequest(String requester, String usr) throws Exception{
        MySQLAccess();
        preparedStatement = connect.prepareStatement("INSERT INTO friends_list "
                + "(Member, Friend) VALUES (?, ?)");
        preparedStatement.setString(1, usr);
        preparedStatement.setString(2, requester);
        preparedStatement.executeUpdate();
        rejectFriendRequest(requester, usr);
        close();
    }
    
    ArrayList getFriends(String usr) throws Exception{
        String friend;
        ArrayList<String> friendsList = new ArrayList<>();
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("SELECT * FROM friends_list "
                    + "WHERE Member = ? ");
            preparedStatement.setString(1, usr);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                friend = resultSet.getString("Friend");
                friendsList.add(friend);
            }
            
            preparedStatement = connect.prepareStatement("SELECT * FROM friends_list "
                    + "WHERE Friend = ? ");
            preparedStatement.setString(1, usr);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                friend = resultSet.getString("Member");
                friendsList.add(friend);
            }
            close();
            return friendsList;
        }
        catch (Exception e){
            
        }
        close();
        return null;
    }
    
    void informFriendClass(String attendee, String friend, String className,
            String classDate, String classTime) throws Exception{
        MySQLAccess();
        preparedStatement = connect.prepareStatement("INSERT INTO "
                + "inform_friends_of_class_attendance (Attending_Member, Informed_Friend,"
                + " Class_Attended, Class_Date, Class_Time) VALUES (?, ?, ?, ?, ?)");
        preparedStatement.setString(1, attendee);
        preparedStatement.setString(2, friend);
        preparedStatement.setString(3, className);
        preparedStatement.setString(4, classDate);
        preparedStatement.setString(5, classTime);
        preparedStatement.executeUpdate();
        close();
    }
    
    HashMap getFriendsInformed(String usrnme) throws Exception{
        var informed = new HashMap<String, Class>();
        String className, classDate, classTime, user;
        
        MySQLAccess();
        preparedStatement = connect.prepareStatement("SELECT * FROM "
                + "inform_friends_of_class_attendance WHERE Informed_Friend = ? ");
        preparedStatement.setString(1, usrnme);
        resultSet = preparedStatement.executeQuery();
        
        while(resultSet.next()){
            System.out.println("row");
            className = resultSet.getString("Class_Attended");
            classDate = resultSet.getString("Class_Date");
            classTime = resultSet.getString("Class_Time");
            user = resultSet.getString("Attending_Member");
            Class cl = getClassInfo(className, classDate, classTime);
            informed.put(user, cl);
        }
        close();
        return informed;
    }
    
    void removeFriend(String friend, String mem) throws Exception{
        MySQLAccess();
        //remove friendship from database
        preparedStatement = connect.prepareStatement("DELETE FROM friends_list "
                + "WHERE (Friend = ? AND Member = ?) OR (Friend = ? AND Member = ?)");
        preparedStatement.setString(1, friend);
        preparedStatement.setString(2, mem);
        preparedStatement.setString(3, mem);
        preparedStatement.setString(4, friend);
        preparedStatement.executeUpdate();
        
        //remove any class invitations between friendship
        preparedStatement = connect.prepareStatement("DELETE FROM "
                + "inform_friends_of_class_attendance WHERE "
                + "(Attending_Member = ? AND Informed_Friend = ?) OR "
                + "(Attending_Member = ? AND Informed_Friend = ?)");
        preparedStatement.setString(1, mem);
        preparedStatement.setString(2, friend);
        preparedStatement.setString(3, friend);
        preparedStatement.setString(4, mem);
        preparedStatement.executeUpdate();
        close();
    }
   
    
    //Helper methods for class
    
    public final void MySQLAccess() throws Exception {
    connect = DriverManager.getConnection("jdbc:mysql://localhost"
        + "/gym_app?user=root&password=Eairnon14");
            
    //Statements to issue SQL queries to the database
    statement = connect.createStatement();
    }
    
    public Member getMemberInfo(String UserName) throws Exception{
        String username = null, pass = null, memName = null, DOB = null, 
                secAns = null, email = null;
        int memNum = 0, strikes = 0, goalProg = 0, achProg = 0;
        try{
        MySQLAccess();    
        preparedStatement = connect
                .prepareStatement("SELECT * FROM gym_app.gym_members WHERE "
                            + "Username = ?");
        preparedStatement.setString(1, UserName);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            username = resultSet.getString("Username");
            memNum = resultSet.getInt("Member_Number");
            pass = resultSet.getString("Password");
            memName = resultSet.getString("Name");
            DOB = resultSet.getString("DOB");
            secAns = resultSet.getString("Security_Answer");
            strikes = resultSet.getInt("Strikes");
            goalProg = resultSet.getInt("Goal");
            achProg = resultSet.getInt("Achieve_Progress");
            email = resultSet.getString("Email");
        }
        char[] passw = pass.toCharArray();
            
        Member m = new Member(username, memNum, passw, memName, DOB, secAns,
            strikes, goalProg, achProg, email);
        return m;
        } catch(Exception e){
            throw e;
        }
    }
    
    
    public Class getClassInfo(String className, String date, 
            String time) throws Exception{
        int ID = 0, spaces = 0;
        String type = null, info = null, classDate = null;
        String classTime = null, name = null;
        try{
        MySQLAccess();
        
        //Create Class object to send onwards
        preparedStatement = connect
                    .prepareStatement("SELECT * FROM gym_app.classes WHERE "
                            + "Name=? AND Class_Date=? AND Class_Time=?");
        preparedStatement.setString(1, className);
        preparedStatement.setString(2, date);
        preparedStatement.setString(3, time);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            ID = resultSet.getInt("Class_ID");
            name = resultSet.getString("Name");
            classDate = resultSet.getString("Class_Date");
            classTime = resultSet.getString("Class_Time");
            type = resultSet.getString("Class_Type");
            info = resultSet.getString("Class_Info");
            spaces = resultSet.getInt("Spaces_Available");
        }
        Class c = new Class(ID, name, classDate, classTime, type, info, spaces);
        return c;
        }
        catch(Exception e){
            throw e;
        }
    }
    
    
    public Trainer getTrainerInfo(String UserName) throws Exception{
        String username = null, pass = null, trnrName = null, DOB = null, 
                secAns = null; 
        String role = "Trainer", email = null;
        try{
        MySQLAccess();    
        preparedStatement = connect
                .prepareStatement("SELECT * FROM gym_app.trainers_owners WHERE "
                            + "Username = ? AND Role = ?");
        preparedStatement.setString(1, UserName);
        preparedStatement.setString(2, role);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.isBeforeFirst()){
            while (resultSet.next()){
                username = resultSet.getString("Username");
                pass = resultSet.getString("Password");
                trnrName = resultSet.getString("Name");
                DOB = resultSet.getString("DOB");
                secAns = resultSet.getString("Security_Answer");
                email = resultSet.getString("Email");
            }
            char[] passw = pass.toCharArray();
            Trainer trnr = new Trainer(username, passw, trnrName, DOB, secAns, email);
            return trnr;
        }
        } catch(Exception e){
            throw e;
        }
        return null;
    }
    
    
    public Owner getOwnerInfo(String UserName) throws Exception{
        String username = null, pass = null, ownrName = null, DOB = null, 
                secAns = null; 
        String role = "Owner", email = null;
        try{
        MySQLAccess();    
        preparedStatement = connect
                .prepareStatement("SELECT * FROM gym_app.trainers_owners WHERE "
                            + "Username = ? AND Role = ?");
        preparedStatement.setString(1, UserName);
        preparedStatement.setString(2, role);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.isBeforeFirst()){
            while (resultSet.next()){
                username = resultSet.getString("Username");
                pass = resultSet.getString("Password");
                ownrName = resultSet.getString("Name");
                DOB = resultSet.getString("DOB");
                secAns = resultSet.getString("Security_Answer");
                email = resultSet.getString("Email");
            }
            char[] passw = pass.toCharArray();
            
            Owner ownr = new Owner(username, passw, ownrName, DOB, secAns, email);
            return ownr;
            }
        } catch(Exception e){
            throw e;
        }
        return null;
    }
    
    static void close() throws SQLException {
        //close the database DateFormatHelper.
        try {
            if (resultSet != null) {
                resultSet.close();}
            if (statement != null) {
                statement.close();}
            if (connect != null) {
                connect.close();}
        } catch (SQLException e) {
            throw e;
        }
    }
}

