package ntustee610.onvif_client_v2.Authentication;



import android.util.Log;

import org.ksoap2.HeaderProperty;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 * Created by weihsun on 2017/1/7.
 */

public class HTTP_Digest {
    String userName;
    String realm;
    String password;
    String HTTP_Medthod = "POST";
    String uri = "/onvif/device_service";
    String nonce;
    String nc = "00000001";
    String cnonce;
    String qop;
    String opaque;
    String response;
    String HA1, HA2, HA3;
    ArrayList<HeaderProperty> header;
    String authorization;
    public HTTP_Digest(String userName, String realm, String password,
                        String nonce, String cnonce, String qop, String opaque){
        this.userName = userName;
        this.realm = realm;
        this.password = password;
        this.nonce = nonce;
        this.cnonce = cnonce;
        this.qop = qop;
        this.opaque = opaque;
    }

    public void digest() throws NoSuchAlgorithmException{
        generateNonce CNonce = new generateNonce();
        cnonce = CNonce.getNonce();
        String username_realm_password = userName + ":" + realm + ":" + password;

        HA1 = MD5_Algorithm(username_realm_password);

        String HTTPMethod_uri = HTTP_Medthod + ":" +uri;
        HA2 = MD5_Algorithm(HTTPMethod_uri);

        String ha1_nonce_nc_cnonce_qop_ha2 = HA1.toString().toLowerCase() + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + HA2.toString().toLowerCase();
        HA3 = MD5_Algorithm(ha1_nonce_nc_cnonce_qop_ha2);

        response = HA3.toString().toLowerCase();

        authorization = "Digest " +
                        "username=" + "\"" + userName + "\", " +
                        "realm=" + "\"" + realm + "\", " +
                        "nonce=" + "\"" +  nonce + "\", " +
                        "uri=" + "\"" + uri + "\", " +
                        "qop=" + "\"" + qop + "\", " +
                        "nc=" + "00000001" + ", " +
                        "cnonce=" + "\"" + cnonce + "\", " +
                        "response=" + "\"" + response + "\", " +
                        "opaque=" + "\"\"";

    }


    private String MD5_Algorithm(String inputString) throws NoSuchAlgorithmException{
        String msg = inputString;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(msg.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        String hashtext = number.toString(16);

        while(hashtext.length() < 32){
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    public String getDigest(){  return  authorization;  }
}
