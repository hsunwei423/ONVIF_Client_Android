package ntustee610.onvif_client_v2.ONVIF_Method.onvif;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ntustee610.onvif_client_v2.Authentication.Security;

/**
 * Created by weihsun on 2017/5/26.
 */

public class GetService {

    String username, passwordDigest, nonce, created;
    String httpDigest, wsToken;

    public GetService(String URL){

        try{
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
//            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
//                    "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
//                    "  <s:Header>\n" +
//                    "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
//                    "      <wsse:UsernameToken>\n" +
//                    "        <wsse:Username>" + username +"</wsse:Username>\n" +
//                    "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + passwordDigest +"</wsse:Password>\n" +
//                    "        <wsse:Nonce>" + nonce +"</wsse:Nonce>\n" +
//                    "        <wsu:Created>" + created + "</wsu:Created>\n" +
//                    "      </wsse:UsernameToken>\n" +
//                    "    </wsse:Security>\n" +
//                    "  </s:Header>\n" +
//                    "  <s:Body>\n" +
//                    "    <tds:GetServices>\n" +
//                    "      <tds:IncludeCapability>true</tds:IncludeCapability>\n" +
//                    "    </tds:GetServices>\n" +
//                    "  </s:Body>\n" +
//                    "</s:Envelope>";

            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                    "  <s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                    "    <GetServiceCapabilities xmlns=\"http://www.onvif.org/ver10/device/wsdl\" />\n" +
                    "  </s:Body>\n" +
                    "</s:Envelope>";

            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(body.getBytes());
            outputStream.flush();

            int statusCode = connection.getResponseCode();
            switch (statusCode){
                case 200:
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    String responseStr = convertInputStreamToString(inputStream);
//                    System.out.println(responseStr);

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(new ByteArrayInputStream(responseStr.getBytes()));
                    doc.getDocumentElement().normalize();

                    NodeList nodeList = doc.getElementsByTagName("*");
                    boolean nextIsPTZ = false;
                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
//                            System.out.println(node.getNodeName());
//                            System.out.println(node.getTextContent());
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
//                            System.out.println(nodeName);
                            if(nodeName.equals("Security")){

                                httpDigest = node.getAttributes().getNamedItem("HttpDigest").getNodeValue();
                                wsToken = node.getAttributes().getNamedItem("UsernameToken").getNodeValue();
                                Log.v("httpDigest", httpDigest);
                                Log.v("wsToken", wsToken);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

        }catch (IOException e){
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
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

    public String getSupportHttpDigest(){
        return httpDigest;
//        return "false";
    }

    public String getSupportWsToken(){
        return wsToken;
    }
}
