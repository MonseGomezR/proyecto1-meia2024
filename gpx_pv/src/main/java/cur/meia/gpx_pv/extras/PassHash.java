package cur.meia.gpx_pv.extras;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PassHash {

    public static String[] passHashing(String pass) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        StringBuilder passHash = new StringBuilder();
        for (byte b : hash) {
            passHash.append(String.format("%02x", b));
        }

        StringBuilder saltHex = new StringBuilder();
        for (byte b : salt) {
            saltHex.append(String.format("%02x", b));
        }

        System.out.println(passHash.toString() + " " + saltHex.toString());

        return new String[]{passHash.toString(), saltHex.toString()};
    }

    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[storedSalt.length() / 2];
        for (int i = 0; i < salt.length; i++) {
            salt[i] = (byte) Integer.parseInt(storedSalt.substring(2 * i, 2 * i + 2), 16);
        }

        KeySpec spec = new PBEKeySpec(inputPassword.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        StringBuilder generatedHash = new StringBuilder();
        for (byte b : hash) {
            generatedHash.append(String.format("%02x", b));
        }

        return generatedHash.toString().equals(storedHash);
    }

}
