import java.util.concurrent.locks.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;

public class Util {
	
	public static ReentrantLock l = new ReentrantLock();
	public static int port = 23500;
	public static boolean debug = false;
	public static int ChordSize = 1000;
	
	
	public static int hash(String key){
		String hashString =  generateSHA1(key);
		return Math.abs((int) Long.parseLong(hashString, 16)) % ChordSize;
		
	}

	
	public static String generateSHA1(String message) {
        return hashString(message, "SHA-1");
    }
 
    private static String hashString(String message, String algorithm){
            
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
 
            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            debug("no such algorithm");
            return (null);
        }
    }
 
    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i =  arrayBytes.length-5; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
	
	
	
	
	public static void debug(String msg){
		if (debug)
			System.out.println(msg);
	}
	public static int getPort(){
		l.lock();
		int tempPort = port++;
		l.unlock();
		return tempPort;
		
	}
	
	
}