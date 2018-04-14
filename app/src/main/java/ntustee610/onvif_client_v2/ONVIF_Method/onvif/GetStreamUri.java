package ntustee610.onvif_client_v2.ONVIF_Method.onvif;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ntustee610.onvif_client_v2.Authentication.HTTP_Digest;
import ntustee610.onvif_client_v2.Authentication.Security;
import ntustee610.onvif_client_v2.Authentication.generateNonce;

/**
 * Created by weihsun on 2017/3/16.
 */

public class GetStreamUri {
    SoapObject responseSoapObject = null;
    String SOAP_ACTION = "http://www.onvif.org/ver10/media/wsdl/GetStreamUri";
    String METHOD_NAME = "GetStreamUri";
    String NAMESPACE = "http://www.onvif.org/ver10/media/wsdl";
    String NAMESPACE_2 = "http://www.onvif.org/ver10/schema";
    String streamUri;
    String[] streamUriArr;

    public GetStreamUri(String URL, String profileName, String userName, String password){
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);       // METHOD_NAME = "GetStreamUri"
            SoapObject StreamSetup = new SoapObject(NAMESPACE, "StreamSetup");  // 1st Layer
            SoapObject Transport = new SoapObject(NAMESPACE_2, "Transport"); //2nd Layer
            Transport.addProperty("Protocol", "UDP");                        //3rd Layer
            StreamSetup.addProperty(NAMESPACE_2, "Stream", "RTP-Unicast");
            StreamSetup.addSoapObject(Transport); // 2nd Layer

            Request.addSoapObject(StreamSetup);  // 1st Layer
            Request.addProperty("ProfileToken", profileName);  // 1st Layer, add after main title

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);  // soapEnvelope
            soapEnvelope.dotNet = true;                                                                   // soapEnvelope
            soapEnvelope.implicitTypes = true;
            soapEnvelope.setOutputSoapObject(Request);                                                    // soapEnvelope  <- Request
            soapEnvelope.headerOut = new Element[1];       //add security header to request xml
            soapEnvelope.headerOut[0] = buildAuthHeader(userName, password);   //add security header to request xml

            HttpTransportSE transport = new HttpTransportSE(URL);  //generate xml and send to IP-CAMERA
            transport.debug = true; //this can see the xml file @ transport/response/
            transport.call(SOAP_ACTION, soapEnvelope);             //generate xml and send to IP-CAMERA

            responseSoapObject = (SoapObject) soapEnvelope.getResponse();  //getResponse from the IP-CAMERA

        } catch (Exception ex) {
            Log.e("GetStreamUri Error", "Error: " + ex.getMessage());
        }
    }


    public GetStreamUri(String URL, String profileName, String realm, String qop, String nonce, String opaque, String userMame, String password){

        try{
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
                        "    <GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\">\n" +
                        "      <StreamSetup>\n" +
                        "        <!-- Attribute Wild card could not be matched. Generated XML may not be valid. -->\n" +
                        "        <Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream>\n" +
                        "        <Transport xmlns=\"http://www.onvif.org/ver10/schema\">\n" +
                        "          <Protocol>UDP</Protocol>\n" +
                        "        </Transport>\n" +
                        "      </StreamSetup>\n" +
                        "      <ProfileToken>" + profileName + "</ProfileToken>\n" +
                        "    </GetStreamUri>\n" +
                        "  </soap:Body>\n" +
                        "</soap:Envelope>";

            String auth = httpDigest(userMame, password, realm, qop, nonce, opaque);
            connection.addRequestProperty("authorization", auth);
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
                    for(int i = 0; i < nodeList.getLength(); i++){
                        org.w3c.dom.Node node = nodeList.item(i);
                        if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
//                            System.out.println(node.getNodeName());
//                            System.out.println(node.getTextContent());
                            int endIndex = node.getNodeName().length();
                            int startIndex = node.getNodeName().indexOf(":") + 1;
                            String nodeName = node.getNodeName().substring(startIndex, endIndex);
//                            System.out.println(nodeName);
                            if(nodeName.equals("Uri")){
//                                System.out.println("streamUri = " + node.getTextContent());
                                streamUri = node.getTextContent();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }catch (MalformedURLException e) {
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


    public SoapObject getStreamUri(){
        return responseSoapObject;
    }

    public String getStreamUriStr(){    return  streamUri;  }

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
//        header = digest.getHeaders();
        return digest.getDigest();
    }
}
