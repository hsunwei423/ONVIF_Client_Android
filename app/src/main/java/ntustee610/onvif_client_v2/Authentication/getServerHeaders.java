package ntustee610.onvif_client_v2.Authentication;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by weihsun on 2017/5/11.
 */

public class getServerHeaders {
    String authenticate;
    ArrayList<String> list;
    String realm, qop, nonce, opaque;
    public getServerHeaders(String uri){

        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(1500);
            connection.setConnectTimeout(1500);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                    "  <s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                    "    <GetDeviceInformation xmlns=\"http://www.onvif.org/ver10/device/wsdl\" />\n" +
                    "  </s:Body>\n" +
                    "</s:Envelope>";

            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(body.getBytes());
            outputStream.flush();

            int statusCode = connection.getResponseCode();

            if(statusCode == 200){
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                Log.v("HttpURL response ", response);
            }else if(statusCode == 401){
                /*  http 401*/
                authenticate = connection.getHeaderField("WWW-Authenticate");
                list = new ArrayList<>(Arrays.asList(authenticate.split(",")));
                String[] realmArr = list.get(0).split("\"");
                realm = realmArr[1];
                String[] qopArr = list.get(1).split("\"");
                qop = qopArr[1];
                String[] tempArr = list.get(2).split("\"");
                nonce = tempArr[1];
                String[] opaqueArr = list.get(3).split("\"");
                if(opaqueArr.length == 1){
                    opaque = "";
                }else{
                    opaque = opaqueArr[1];
                }
//                System.out.println("realm : " + realm);
//                System.out.println("qop : " + qop);
//                System.out.println("nonce : " + nonce);
//                System.out.println("opaque : " + opaque);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getNonce(){
        return nonce;
    }

    public String getRealm(){   return realm;    }

    public String getqop(){ return qop; }

    public String getopaque(){  return opaque;  }

    private String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = "";
        String result = "";

        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

        /* close stream*/
        if(inputStream != null){
            inputStream.close();
        }

        return result;
    }

}
