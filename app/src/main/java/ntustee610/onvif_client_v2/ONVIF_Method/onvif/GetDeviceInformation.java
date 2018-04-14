package ntustee610.onvif_client_v2.ONVIF_Method.onvif;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ntustee610.onvif_client_v2.Authentication.HTTP_Digest;
import ntustee610.onvif_client_v2.Authentication.Security;
import ntustee610.onvif_client_v2.Authentication.generateNonce;

/**
 * Created by weihsun on 2017/5/6.
 */

public class GetDeviceInformation {
    String response[];
    Vector responseVector;
    String SOAP_ACTION = "http://www.onvif.org/ver10/device/wsdl/GetDeviceInformation";
    String METHOD_NAME = "GetDeviceInformation";
    String NAMESPACE_wsdl = "http://www.onvif.org/ver10/device/wsdl";


    public GetDeviceInformation(String URL, String userMame, String password){
        try {
            SoapObject Request = new SoapObject(NAMESPACE_wsdl, METHOD_NAME);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
            soapEnvelope.dotNet = true;
            soapEnvelope.implicitTypes = true;
            soapEnvelope.setOutputSoapObject(Request);
            soapEnvelope.headerOut = new Element[1];       //add security header to request xml
            soapEnvelope.headerOut[0] = buildAuthHeader(userMame, password);   //add security header to request xml

            HttpTransportSE transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            transportSE.call(SOAP_ACTION, soapEnvelope);             //generate xml and send to IP-CAMERA

            responseVector = (Vector)soapEnvelope.getResponse();

            Log.v("getDeviceInfo ", responseVector.toString());

        }catch (HttpResponseException httpException){
            int statusCode = httpException.getStatusCode();
            List responseHeaders = httpException.getResponseHeaders();
            Log.e("statusCode ", "" + statusCode);
            Log.e("responseHeader ", responseHeaders.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Vector getDeviceInfo(){
        return responseVector;
    }

    public GetDeviceInformation(String URL, String realm, String qop, String nonce, String opaque, String userMame, String password){
        try {
            java.net.URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
                    "  <soap:Body>\n" +
                    "    <tds:GetDeviceInformation />\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            String auth = httpDigest(userMame, password, realm, qop, nonce, opaque);
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
                    System.out.println(responseStr);

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(new ByteArrayInputStream(responseStr.getBytes()));
                    doc.getDocumentElement().normalize();

                    NodeList nodeList = doc.getElementsByTagName("*");
                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
//                            System.out.println(node.getNodeName());
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
//                            System.out.println(nodeName);
//                            if(nodeName.equals("Profiles")){
//                                System.out.println("Attr = " + node.getAttributes().getNamedItem("token").getNodeValue());
//                                ProfileName = node.getAttributes().getNamedItem("token").getNodeValue();
//                            }
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

    private Element buildAuthHeader(String userName, String password) {         //add security header to request xml
        Security security = new Security(userName, password);

        String NAMESPACE_wsse = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
        String NAMESPACE_wsu = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

        Element Security = new Element().createElement(NAMESPACE_wsse, "Security");
        Element UsernameToken = new Element().createElement(NAMESPACE_wsse, "UsernameToken");
        Element Username = new Element().createElement(NAMESPACE_wsse, "Username");
        Username.addChild(Node.TEXT, userName);
        Security.addChild(Node.ELEMENT, UsernameToken);
        UsernameToken.addChild(Node.ELEMENT, Username);
        Element Password = new Element().createElement(NAMESPACE_wsse, "Password");
        Password.setAttribute("", "Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wssusername-token-profile-1.0#PasswordDigest");
        Password.addChild(Node.TEXT, security.digest());
        Element Nonce = new Element().createElement(NAMESPACE_wsse, "Nonce");
        Nonce.addChild(Node.TEXT, security.nonce);
        Element Created = new Element().createElement(NAMESPACE_wsu, "Created");
        Created.addChild(Node.TEXT, security.currentdate);
        UsernameToken.addChild(Node.ELEMENT, Password);
        UsernameToken.addChild(Node.ELEMENT, Nonce);
        UsernameToken.addChild(Node.ELEMENT, Created);
        //System.out.println("test");
        return Security;
    }

    private String httpDigest(String userName, String password, String realm, String qop, String nonce, String opaque) throws NoSuchAlgorithmException{
        generateNonce generateNonce = new generateNonce();
        String cnonce = generateNonce.getNonce();
        HTTP_Digest digest = new HTTP_Digest(userName, realm, password,
                 nonce, cnonce, qop, opaque);
        digest.digest();
        return digest.getDigest();
//        header = digest.getHeaders();
    }



}
