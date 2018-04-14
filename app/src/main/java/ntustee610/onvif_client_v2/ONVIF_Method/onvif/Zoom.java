package ntustee610.onvif_client_v2.ONVIF_Method.onvif;

import android.util.Log;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.util.ArrayList;

import ntustee610.onvif_client_v2.Authentication.Security;

/**
 * Created by weihsun on 2017/4/27.
 */

public class Zoom {
    SoapObject result_Zoom = null;
    String SOAP_ACTION = "http://www.onvif.org/ver20/ptz/wsdl/ContinuousMove";
    String METHOD_NAME = "ContinuousMove";
    String NAMESPACE_wsdl = "http://www.onvif.org/ver20/ptz/wsdl";
    String NAMESPACE_schema = "http://www.onvif.org/ver10/schema";

    public Zoom(String getUri, double x, double y, double z, String profileName, String userName, String password){
        try {
            SoapObject Request = new SoapObject(NAMESPACE_wsdl, METHOD_NAME); // METHOD_NAME = "AddPTZConfiguration"
            SoapObject Velocity = new SoapObject(NAMESPACE_wsdl, "Velocity");  // 1st Layer
            SoapObject PanTilt = new SoapObject(NAMESPACE_schema, "PanTilt");
            SoapObject Zoom = new SoapObject(NAMESPACE_schema, "Zoom");
            PanTilt.addAttribute("x", x);   // 2nd Layer
            PanTilt.addAttribute("y", y);
            Zoom.addAttribute("x", z);
            Velocity.addSoapObject(PanTilt);
            Velocity.addSoapObject(Zoom);
            Request.addProperty("ProfileToken", profileName);    // 1st Layer
            Request.addSoapObject(Velocity);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER12); // soapEnvelope
            soapEnvelope.dotNet = true;                                                                   // soapEnvelope
            soapEnvelope.implicitTypes = true;                                                                  // soapEnvelope
            soapEnvelope.setOutputSoapObject(Request);                                                   // soapEnvelope  <- Request
            soapEnvelope.headerOut = new Element[1];       //add security header to request xml
            soapEnvelope.headerOut[0] = buildAuthHeader(userName, password);   //add security header to request xml

            HttpTransportSE transport = new HttpTransportSE(getUri);   //generate xml and send to IP-CAMERA
            transport.debug = true;                                      //enable the debug mode so that we can see the xml we generated
            transport.call(SOAP_ACTION, soapEnvelope);             //generate xml and send to IP-CAMERA

            result_Zoom = (SoapObject) soapEnvelope.getResponse();   //getResponse from the IP-CAMERA
        } catch (Exception ex) {
            Log.e("result_Zoom", "Error: " + ex.getMessage());
        }
    }


    public SoapObject getZoomResponse(){    return  result_Zoom;    }

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

}
