package gymbookingapp;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author Ben
 */
public class Member extends Parties
{
    List<Member> friendslist =  new ArrayList<>();
    List<String> friendRequest = new ArrayList<>();
    int goalProgress;
    int strikes;
    int achievementPoints;
    int memberNum;
    HashMap<Class, Reservation> upcomingClasses = new HashMap<>();
    List<Reservation> reservationsHeld = new ArrayList<>();  
    
    //used for addMember
    Member(String usr, char[] pass, String nme, String dob, String secAns, String mail){
        username = usr;
        name = nme;
        password = pass;
        securityAnswer = secAns;
        email = mail;
        if (DateFormatHelper.dateFormat(dob)){
            dateOfBirth = LocalDate.parse(dob);
            dobString = dob;
        } else {
            System.out.println("Incorrect Date Format");
        }
    }
    
    
   //Full Member class for multiple uses.
    Member(String usr, int memNum, char[] pass, String nme, String dob, 
            String secAns, int str, int goalProg, int achProg, String mail){
        username = usr;
        memberNum = memNum;
        password = pass;
        name = nme;
        dateOfBirth = LocalDate.parse(dob);
        dobString = dob;
        securityAnswer = secAns;
        strikes = str;
        goalProgress = goalProg;
        achievementPoints = achProg;
        email = mail;
                
    }
    
    
    void addNewMember(String usr, char[] pass, String name, String dob, 
            String secAns, String email) throws Exception {
        try{
            MySQLAccess();
            //generate a secure has of the password provided
            String passwordHash = generatePasswordHash(pass);
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO gym_members"
                            + " (Username, Password, Name, DOB, Email, Security_Answer,"
                            + " Strikes, Goal, Achieve_Progress) "
                            + "VALUES (?, ?,"
                            + " ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, usr);
            preparedStatement.setString(2, passwordHash);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, dob);
            preparedStatement.setString(5, email);
            preparedStatement.setString(6, secAns);
            preparedStatement.setInt(7, 0);
            preparedStatement.setInt(8, 0);
            preparedStatement.setInt(9, 0);
            preparedStatement.executeUpdate();
            
            //set up achievement points
        } 
        catch (Exception e){
            throw e;
        }
    }
    
        void updateMember(String name, String dob, String secAns, String email) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement(
                    "UPDATE gym_app.gym_members "
                    + "SET "
                            + "Name = ?, "
                            + "DOB = ?, "
                            + "Security_Answer = ?, "
                            + "Email = ? "
                    + "WHERE Username = ?");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, dob);
            preparedStatement.setString(3, secAns);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, this.username);
            preparedStatement.executeUpdate();
                       
        }
        catch(Exception e){
            throw e;
        }
    }
    
    void removeMember() throws Exception{
        try{
            MySQLAccess();
            
            //Create Class obj to access method to remove all enrollments member has
            Class c = new Class();
            c.removeAllMemEnrollments(this.username);
            
            //create Reservation obj to access method to remove all reservations
            Reservation r = new Reservation();
            r.removeAllReservations(this.memberNum);
            
            //Lastly remove member from database
            preparedStatement = connect.prepareStatement("DELETE FROM gym_app.gym_members "
                    + "WHERE Username = ?");
            preparedStatement.setString(1, this.username);
            preparedStatement.executeUpdate();
        } 
        catch(Exception e){
            throw e;
        }
    }
    
    void bookOnClass(Class c) throws Exception{
        LocalTime classTime = c.classTime;
        LocalDate classDate = c.classDate;

        DayOfWeek classD = classDate.getDayOfWeek();
        String classDay = classD.getDisplayName(TextStyle.FULL, Locale.UK);
        
        Reservation r = new Reservation(classDay, classTime, classDate,
                c.name, this.memberNum, c.classType);
        r.addReservation(r);
        
        c.decreaseSpaces();
        c.addEnrolledMem(this.username);
        
    }
    
    ArrayList seeUpcomingClasses(int memNum) throws Exception{
        ArrayList<Class> classList = new ArrayList<>();
        String name, time, date, day, type;
        
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.toString();

        try{
            MySQLAccess();
                //get the upcoming clases for that user number
            preparedStatement = connect
                    .prepareStatement("SELECT * FROM gym_app.upcoming_classes WHERE "
                            + "Class_Date >= ? AND Member_Reserved = ?");
            preparedStatement.setString(1, currentDateString);
            preparedStatement.setInt(2, memNum);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                day = resultSet.getString("Class_Day");
                time = resultSet.getString("Class_Time");
                date = resultSet.getString("Class_Date");
                name = resultSet.getString("Class_Name");
                type = resultSet.getString("Class_Type");
                classList.add(new Class(name, date, time, day, type)); 
            }
            return classList;
            

        } catch (Exception e){
            throw e;
        }
    }
    
    
    void cancelReservation(Class c) throws Exception{
        c.increaseSpaces();
        c.removeEnrolledMember(this.username);
        
        LocalTime cLocTime = c.classTime;
        LocalDate cLocDate = c.classDate;
        //get day of week for classDate
        DayOfWeek classD = cLocDate.getDayOfWeek();
        String classDay = classD.getDisplayName(TextStyle.FULL, Locale.UK);
        //create reservation obj and get it to remove the reservation it represents from database table.
        Reservation r = new Reservation(classDay, cLocTime, cLocDate, c.name, this.memberNum, c.classType);
        r.removeReservation();
        
    }
    
    boolean addStrike(String username) throws Exception{
        int strikesNew = this.strikes + 1;
        if (strikesNew > 3){
            return true;
        } else {
            MySQLAccess();
            preparedStatement = connect.prepareStatement("UPDATE gym_app.gym_members"
                    + " SET "
                            + "Strikes = ? "
                    + "WHERE "
                            + "Username = ?");
            preparedStatement.setInt(1, strikesNew);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
            return false;
        }
            
    }
    
    void resetStrikes(String username) throws Exception{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("UPDATE gym_app.gym_members"
                    + " SET "
                            + "Strikes = ? "
                    + "WHERE "
                            + "Username = ?");
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
    }
    
    void addAchievePoints(String user) throws Exception{
        MySQLAccess();
        int points = this.achievementPoints + 1;
        preparedStatement = connect.prepareStatement("UPDATE gym_members "
                + "SET Achieve_Progress = ? WHERE Username = ?");
        preparedStatement.setInt(1, points);
        preparedStatement.setString(2, user);
        preparedStatement.executeUpdate();
    }
    
    void resetAchievementPoints(String usr) throws Exception{
        MySQLAccess();
        preparedStatement = connect.prepareStatement("UPDATE gym_members"
                    + " SET "
                            + "Achieve_Progress = ? "
                    + "WHERE "
                            + "Username = ?");
        preparedStatement.setInt(1, 0);
        preparedStatement.setString(2, usr);
        preparedStatement.executeUpdate();
    }
    
    void setAchieveGoal(String usr, int val) throws Exception{
        MySQLAccess();
        preparedStatement = connect.prepareStatement("UPDATE gym_members"
                    + " SET "
                            + "Goal = ? "
                    + "WHERE "
                            + "Username = ?");
        preparedStatement.setInt(1, val);
        preparedStatement.setString(2, usr);
        preparedStatement.executeUpdate();
    }
    
    
    boolean passwordCheck(char[] origPass) throws NoSuchAlgorithmException, InvalidKeySpecException{
        String storedPass = new String(this.password);
        //compares the two passwords.
        boolean matched = validatePassword(origPass, storedPass);
        return matched;
    }
    
    
    void setNewPass(char[] newPass) throws NoSuchAlgorithmException, InvalidKeySpecException, Exception{
        //set up new password with hash.
        String passwordHash = generatePasswordHash(newPass);
        //add new password to table by updating.
        MySQLAccess();
        try{
            preparedStatement = connect.prepareStatement(
                            "UPDATE gym_members "
                          + "SET "
                                    + "Password = ? "
                          + "WHERE "
                                    + "Username = ?");
            preparedStatement.setString(1, passwordHash);
            preparedStatement.setString(2, this.username);
            preparedStatement.executeUpdate();
        } catch (Exception e){
            throw e;
        }
        
    }
    
    
    
    
    
    //THIS CREATES A SECURE HASH OF PROVIDED PASSWORD
    private static String generatePasswordHash(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 1000;
        char[] chars = password;
        byte[] salt = getSalt();
         
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8); //takes password, salt, iteration count and 
        //to-be-derived key length for generating PBEKey of variable-key-size PBE ciphers.
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }
     
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG"); //SHA1PRNG is rng algorithm being used here
        byte[] salt = new byte[16];
        sr.nextBytes(salt); //generates a user specified number of random bytes
        return salt;
    }
     
    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }
    
    
    
    //THIS WILL ENABLE CHECKS FOR LOGGING IN AND FOR RESETTING PASSWORD (does not cover forgotten password)
    private static boolean validatePassword(char[] originalPassword, String storedPassword) 
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        String[] parts = storedPassword.split(":"); //breaks up the 3 parts of the stored password back to their individual sections
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);
         
        PBEKeySpec spec = new PBEKeySpec(originalPassword, salt, iterations, hash.length * 8);
        //gathers same info as used to set the original password hash
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] testHash = skf.generateSecret(spec).getEncoded();
        
        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }
    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
    
    
}
