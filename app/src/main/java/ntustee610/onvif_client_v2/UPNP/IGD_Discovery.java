package ntustee610.onvif_client_v2.UPNP;

import android.util.Log;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;

import java.io.IOException;

/**
 * Created by weihsun on 2017/4/5.
 */

public class IGD_Discovery {
    final String TAG = IGD_Discovery.class.getName();

    int discoveryTimeout = 500;    // 0.5sec

    public String getExternalIP(){
        System.out.println("Looking for Internet Gateway Device");
        try {
            InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices(discoveryTimeout);
            if(IGDs != null){
                InternetGatewayDevice testIGD = IGDs[0];
//                System.out.println("\tFound device " + testIGD.getIGDRootDevice().getModelName());
//                System.out.println("External IP address: " + testIGD.getExternalIPAddress());
                return testIGD.getExternalIPAddress();
            }else{
                Log.v(TAG, "Unable to find IGD on your network");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (UPNPResponseException respEx){
//            System.err.println("UPNP device unhappy " + respEx.getDetailErrorCode() + " " + respEx.getDetailErrorDescription());
            return null;
        }
    }
}
