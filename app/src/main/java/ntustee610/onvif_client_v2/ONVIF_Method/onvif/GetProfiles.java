package ntustee610.onvif_client_v2.ONVIF_Method.onvif;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ntustee610.onvif_client_v2.Authentication.HTTP_Digest;
import ntustee610.onvif_client_v2.Authentication.Security;
import ntustee610.onvif_client_v2.Authentication.generateNonce;

/**
 * Created by weihsun on 2017/3/16.
 */

public class GetProfiles {
    String ProfileName = null;
    ArrayList<String> auth;
    String[] profileArr;
    String username, passwordDigest, nonce, created;
    String width, height;
    String[] heightxwidth;

    public GetProfiles(String URL, String userName, String password){
        Security security = new Security(userName, password);
        username = userName;
        passwordDigest = security.digest();
        nonce = security.nonce;
        created = security.currentdate;

        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
                    "  <s:Header xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                    "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                    "      <wsse:UsernameToken>\n" +
                    "        <wsse:Username>" + username + "</wsse:Username>\n" +
                    "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + passwordDigest + "</wsse:Password>\n" +
                    "        <wsse:Nonce>" + nonce + "</wsse:Nonce>\n" +
                    "        <wsu:Created>" + created + "</wsu:Created>\n" +
                    "      </wsse:UsernameToken>\n" +
                    "    </wsse:Security>\n" +
                    "  </s:Header>\n" +
                    "  <soap:Body>\n" +
                    "    <trt:GetProfiles />\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(body.getBytes());
            outputStream.flush();

            int statusCode = connection.getResponseCode();

            switch (statusCode){
                case 200:
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    String responseStr = convertInputStreamToString(inputStream);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(new ByteArrayInputStream(responseStr.getBytes()));
                    doc.getDocumentElement().normalize();

                    NodeList nodeList = doc.getElementsByTagName("*");
                    int index = 0;
                    int arrayIndex = 0;
                    int hxwIndex = 0;
                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
                            if(nodeName.equals("Profiles")){
                                arrayIndex++;
                            }
                        }
                    }
                    profileArr = new String[arrayIndex];
                    heightxwidth = new String[arrayIndex];

                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
                            if(nodeName.equals("Profiles")){
                                ProfileName = node.getAttributes().getNamedItem("token").getNodeValue();
                                profileArr[index] = ProfileName;
                                index++;
                            }
                            if(nodeName.equals("Width")){
                                height = node.getTextContent();
                            }
                            if(nodeName.equals("Height")){
                                width = node.getTextContent();
                                heightxwidth[hxwIndex] = height + " x " + width;
                                hxwIndex++;
                            }
                        }
                    }

                    break;
                default:
                    Map<String, List<String>> map = connection.getHeaderFields();
                    for(Map.Entry<String, List<String>> entry : map.entrySet()){
                        System.out.println("Key : " + entry.getKey() + " " + "Value : " + entry.getValue());
                    }
                    break;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public GetProfiles(String URL, String realm, String qop, String nonce, String opaque, String userName, String password){
        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
                    "  <soap:Body>\n" +
                    "    <trt:GetProfiles />\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            String auth = httpDigest(userName, password, realm, qop, nonce, opaque);
            System.out.println(auth);
            connection.addRequestProperty("Authorization", auth);
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
                    int index = 0;
                    int arrayIndex = 0;
                    int hxwIndex = 0;
                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
//                            System.out.println(node.getNodeName());
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
//                            System.out.println(nodeName);
                            if(nodeName.equals("Profiles")){
//                                System.out.println("Attr = " + node.getAttributes().getNamedItem("token").getNodeValue());
//                                ProfileName = node.getAttributes().getNamedItem("token").getNodeValue();
//                                profileArr[index] = ProfileName;
//                                index++;
                                arrayIndex++;
                            }
                        }
                    }
                    profileArr = new String[arrayIndex];
                    heightxwidth = new String[arrayIndex];

                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
//                            System.out.println(node.getNodeName());
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
//                            System.out.println(nodeName);
                            if(nodeName.equals("Profiles")){
//                                System.out.println("Attr = " + node.getAttributes().getNamedItem("token").getNodeValue());
                                ProfileName = node.getAttributes().getNamedItem("token").getNodeValue();
                                profileArr[index] = ProfileName;
                                index++;
                            }
                            if(nodeName.equals("Width")){
                                height = node.getTextContent();
                            }
                            if(nodeName.equals("Height")){
                                width = node.getTextContent();
                                heightxwidth[hxwIndex] = height + " x " + width;
                                hxwIndex++;
                            }
                        }
                    }

//                    for(int i = 0; i < hxwIndex; i++){
//                        Log.v("hxw", heightxwidth[i]);
//                    }

                    break;
                default:
                    Map<String, List<String>> map = connection.getHeaderFields();
                    for(Map.Entry<String, List<String>> entry : map.entrySet()){
                        System.out.println("Key : " + entry.getKey() + " " + "Value : " + entry.getValue());
                    }
                    break;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }


    public String convertInputStreamToString(InputStream inputStream) throws IOException {
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

    public String getProfiles(){
        return profileArr[0];
    }

    public String[] getProfilesArr(){ return profileArr; }

    public String[] getHxWArr(){
            return  heightxwidth;
    }

//    private Element buildAuthHeader() {         //add security header to request xml
//        Security security = new Security();
//
//        String NAMESPACE_wsse = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
//        String NAMESPACE_wsu = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
//
//        Element Security = new Element().createElement(NAMESPACE_wsse, "Security");
//        Element UsernameToken = new Element().createElement(NAMESPACE_wsse, "UsernameToken");
//        Element Username = new Element().createElement(NAMESPACE_wsse, "Username");
//        Username.addChild(Node.TEXT, "admin");
//        Security.addChild(Node.ELEMENT, UsernameToken);
//        UsernameToken.addChild(Node.ELEMENT, Username);
//        Element Password = new Element().createElement(NAMESPACE_wsse, "Password");
//        Password.setAttribute("", "Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wssusername-token-profile-1.0#PasswordDigest");
//        Password.addChild(Node.TEXT, security.digest());
//        Element Nonce = new Element().createElement(NAMESPACE_wsse, "Nonce");
//        Nonce.addChild(Node.TEXT, security.nonce);
//        Element Created = new Element().createElement(NAMESPACE_wsu, "Created");
//        Created.addChild(Node.TEXT, security.currentdate);
//        UsernameToken.addChild(Node.ELEMENT, Password);
//        UsernameToken.addChild(Node.ELEMENT, Nonce);
//        UsernameToken.addChild(Node.ELEMENT, Created);
//        //System.out.println("test");
//        return Security;
//    }

    private String httpDigest(String userName, String password, String realm, String qop, String nonce, String opaque) throws NoSuchAlgorithmException{
        generateNonce generateNonce = new generateNonce();
        String cnonce = generateNonce.getNonce();
        HTTP_Digest digest = new HTTP_Digest(userName, realm, password
                , nonce, cnonce, qop, opaque);
        digest.digest();
//        header = digest.getHeaders();
        return digest.getDigest();
    }

}
