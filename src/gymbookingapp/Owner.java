
package gymbookingapp;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Ben White
 */
public class Owner extends Parties
{
    Owner(String usr, char[] pass, String nme, String dob, String secAns, String mail) {
        username = usr;
        name = nme;
        password = pass;
        securityAnswer = secAns;
        email = mail;
        String role = "Owner";
        if (DateFormatHelper.dateFormat(dob)){
            dateOfBirth = LocalDate.parse(dob);
            dobString = dob;
        } else {
            System.out.println("Incorrect Date Format");
        }
    }
    
    
    void addNewOwner(Owner owner) throws Exception {
        try{
            MySQLAccess();
            String passwordHash = generatePasswordHash(owner.password);
            
            preparedStatement = connect
                    .prepareStatement("INSERT INTO gym_app.trainers_owners"
                            + " VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, owner.username);
            preparedStatement.setString(2, passwordHash);
            preparedStatement.setString(3, owner.name);
            preparedStatement.setString(4, owner.dobString);
            preparedStatement.setString(5, owner.securityAnswer);
            preparedStatement.setString(6, "Owner");
            preparedStatement.executeUpdate();
        } 
        catch (Exception e){
            throw e;
        }
    }
    
    void updateOwner(String name, String dob, String secAns, String email) throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement(
                    "UPDATE gym_app.trainers_owners "
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
    
    void removeOwner() throws Exception{
        try{
            MySQLAccess();
            preparedStatement = connect.prepareStatement("DELETE FROM gym_app.trainers_owners "
                    + "WHERE Username = ?");
            preparedStatement.setString(1, this.username);
            preparedStatement.executeUpdate();
        } 
        catch(Exception e){
            throw e;
        }
    }
           
    
    
    
    boolean passwordCheck(char[] origPass) throws NoSuchAlgorithmException, InvalidKeySpecException{
        String storedPass = new String(this.password);
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
                            "UPDATE gym_app.trainers_owners "
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
    private static boolean validatePassword(char[] originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException
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
