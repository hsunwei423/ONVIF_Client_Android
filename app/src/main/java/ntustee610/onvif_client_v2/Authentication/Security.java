package ntustee610.onvif_client_v2.Authentication;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by weihsun on 2016/12/17.
 */

public class Security {
    public String username;
    public String password;
    public String nonce = "LKqI6G/AikKCQrN0zqZFlg==";
    byte[] nonceBinaryData = null;
    byte[] nonceBase64;
    public String currentdate;
    public Security(String userMame, String password){
        username = userMame;
        this.password = password;
    }

//    PasswordDigest = B64ENCODE( SHA1( B64DECODE( Nonce ) + Date + Password ) )

    public String digest() {
        nonceBinaryData = Base64.decode(nonce, Base64.DEFAULT);
        nonceBase64 = Base64.encode(nonceBinaryData, Base64.DEFAULT);   //useless
//        System.out.println(Base64.encodeToString(nonceBinaryData, Base64.DEFAULT));
//        Log.d("nonce", "" + nonce);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        currentdate = df.format(new Date());
        //currentdate = "2010-09-16T07:50:45Z";
        byte[] currentdateBinaryData = currentdate.getBytes(Charset.forName("UTF-8"));
//        Log.d("date", "" + currentdate);

        byte[] passwordBinaryData = password.getBytes(Charset.forName("UTF-8"));
        byte[] combined = new byte[nonceBinaryData.length + currentdateBinaryData.length + passwordBinaryData.length];

        System.arraycopy(nonceBinaryData, 0, combined, 0, nonceBinaryData.length);
        System.arraycopy(currentdateBinaryData, 0, combined, nonceBinaryData.length, currentdateBinaryData.length);
        System.arraycopy(passwordBinaryData, 0, combined, nonceBinaryData.length + currentdateBinaryData.length, passwordBinaryData.length);

        StringBuilder sb = new StringBuilder();      //show nonceBinaryData
        for (byte b : nonceBinaryData) {              //
            sb.append(String.format("%X ", b));       //
        }

        StringBuilder sb3 = new StringBuilder();      //show nonceBinaryData
        for (byte b : currentdateBinaryData) {              //
            sb3.append(String.format("%X ", b));       //
        }

        StringBuilder sb4 = new StringBuilder();      //show nonceBinaryData
        for (byte b : passwordBinaryData) {              //
            sb4.append(String.format("%X ", b));       //
        }
        //System.out.println(sb);
        //System.out.println(sb3);
        //System.out.println(sb4);

        StringBuilder sb2 = new StringBuilder();      //show nonceBinaryData
        for (byte b : combined) {              //
            sb2.append(String.format("%X ", b));       //
        }
        //System.out.println(sb2);

        String result = sha1(combined);
        //System.out.println(result);
        return result;
    }

    public static String sha1(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            byte[] out = digest.digest(input);
            return Base64.encodeToString(out, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
