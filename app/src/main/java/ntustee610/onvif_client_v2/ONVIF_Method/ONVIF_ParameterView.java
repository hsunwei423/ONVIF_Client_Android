package ntustee610.onvif_client_v2.ONVIF_Method;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import ntustee610.onvif_client_v2.DB.TinyDB;
import ntustee610.onvif_client_v2.R;

public class ONVIF_ParameterView extends AppCompatActivity {
    public String deviceURI = "";
    public String deviceName = "";
    public String deviceStatus = "";
    public String userName = "";
    public String userPassord = "";
    public String prfileName = "";
    public String streamUri = "";
    public String PTZUri = "";
    public String errorMsg = "";
    public String authentication = "ws-usernameToken";// ws-usernameToken, http-digest, both
    public String externalIP = "";

    TextView showDeviceUri;
    TextView showDeviceName;
    TextView showUserName;
    TextView showPassword;
    TextView showProfiles;
    TextView showStreamUri;
    TextView showPTZUri;
    TextView showAuthorization;
    TextView showExternalIP;

//    SharedPreferences tinyDB;
    private static final String  PREFERENCES_NAME = "tinyDB";
    
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onvif_parameter_view);
//        tinyDB = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        tinyDB = new TinyDB(this);
        findViews();
//        setText();
        show();
    }
    private void findViews(){
        showDeviceUri = (TextView)findViewById(R.id.showDeviceUri);
        showDeviceName = (TextView)findViewById(R.id.showDeviceName);
        showUserName = (TextView)findViewById(R.id.showUserName);
        showPassword = (TextView)findViewById(R.id.showPassword);
        showProfiles = (TextView)findViewById(R.id.showProfile);
        showStreamUri = (TextView)findViewById(R.id.showStreamUri);
        showPTZUri = (TextView)findViewById(R.id.showPTZUri);
        showAuthorization = (TextView)findViewById(R.id.showAuthorization);
        showExternalIP = (TextView)findViewById(R.id.showExternalIP);
    }


    private void show(){
        deviceName = tinyDB.getString("deviceName");
        deviceURI = tinyDB.getString("deviceURI");
        userName = tinyDB.getString("userName");
        userPassord = tinyDB.getString("userPassword");
        prfileName = tinyDB.getString("profileName");
        streamUri = tinyDB.getString("streamUri");
        PTZUri = tinyDB.getString("PTZUri");
        authentication = tinyDB.getString("authentication");
        externalIP = tinyDB.getString("external");

        showDeviceUri.setText(deviceURI);
        showDeviceName.setText(deviceName);
        showAuthorization.setText(authentication);
        showUserName.setText(userName);
        showPassword.setText(userPassord);
        showProfiles.setText(prfileName);
        showStreamUri.setText(streamUri);
        showPTZUri.setText(PTZUri);
        showExternalIP.setText(externalIP);
    }
}
