package ntustee610.onvif_client_v2.Authentication;

import java.util.Random;

/**
 * Created by weihsun on 2017/5/10.
 */

public class generateNonce {
    String saltStr;
    public generateNonce(){
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while(salt.length() < 32){
            int index = (int)(rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        saltStr = salt.toString();
//        System.out.println("nonce = " + saltStr);
    }

    public String getNonce(){
        return saltStr;
    }
}
