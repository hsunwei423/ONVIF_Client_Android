package ntustee610.onvif_client_v2.ONVIF_Method;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import org.apache.http.protocol.HTTP;
import org.ksoap2.serialization.SoapObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ntustee610.onvif_client_v2.Authentication.getServerHeaders;
import ntustee610.onvif_client_v2.DB.TinyDB;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetDeviceInformation;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetPTZUri;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetProfiles;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetStreamUri;
import ntustee610.onvif_client_v2.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ONVIF_MAIN extends AppCompatActivity {
    String TAG = ONVIF_MAIN.class.getName();
    TextView showDevice, showStatus, showUri, showStreamUri;
    RadioGroup securityGroup;
    RadioButton WS_Security, HTTP_DIGEST, BOTH;
    CircularProgressButton circularBTN1;

    String profileStr, streamUri, PTZUri;
    SoapObject streamUriSoap;

    TinyDB tinyDB;
    String qop, nonce, opaque, realm;
    static String[] profilesArr, digest_profilesArr, streamUriArr, hxwArr;
    int profileCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onvif__main);
        findViews();
        tinyDB = new TinyDB(this);
        loadExternalIp();
//        loadtinyDB();
        checkAuthorization();
        Log.v("tinydbVal", tinyDB.getString("authentication"));
        show();
        circularButton();

    }

    private void show(){
        showDevice.setText(tinyDB.getString("deviceName"));
        showUri.setText(tinyDB.getString("deviceURI"));
    }

    private void findViews(){
        showDevice = (TextView)findViewById(R.id.TV_device);
        showUri = (TextView)findViewById(R.id.TV_uri);
        showStreamUri = (TextView)findViewById(R.id.TV_streamUri);
        securityGroup = (RadioGroup)findViewById(R.id.securityGroup);
        WS_Security = (RadioButton)findViewById(R.id.BT_ws_usernameToken);
        HTTP_DIGEST = (RadioButton)findViewById(R.id.BT_http_digest);
        BOTH = (RadioButton)findViewById(R.id.BT_Both);
        securityGroup.setOnCheckedChangeListener(listener);
    }

    public void intentSearch(View v){
        circularBTN1.setProgress(0);
        Intent intent = new Intent(this, ONVIF_GetDevice.class);
        startActivity(intent);
    }

    private void loadExternalIp(){
        Log.v("check value ", "" + tinyDB.getString("external").equals(""));
        if(tinyDB.getString("external").equals("")){
            String ip = "https://ifcfg.me/ip";
            try {
                String external = new GetExternalIp().execute(ip).get();
                Log.v("external ip ", external);
                tinyDB.putString("external", external);
                Toast.makeText(this, "external ip = " + external, Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
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

    private void checkAuthorization(){
        String supportHttpDigest;
        if(!tinyDB.getString("supportHttpDigest").equals("")){
            supportHttpDigest = tinyDB.getString("supportHttpDigest");
            if(supportHttpDigest.equals("false")){
                HTTP_DIGEST.setVisibility(View.GONE);
                BOTH.setVisibility(View.GONE);
            }else{
                HTTP_DIGEST.setVisibility(View.VISIBLE);
                BOTH.setVisibility(View.VISIBLE);
            }
        }
        int radioButtonID = securityGroup.getCheckedRadioButtonId();
        View radioButton = securityGroup.findViewById(radioButtonID);
        int idx = securityGroup.indexOfChild(radioButton);

        RadioButton r = (RadioButton) securityGroup.getChildAt(idx);
        String selectBTN = r.getText().toString();
//        Log.v("secureBTN ", selectBTN);
        switch (selectBTN){
            case "WS-SECURITY":
                tinyDB.putString("authentication", "ws-usernameToken");
                break;
            case "HTTP-DIGEST":
                tinyDB.putString("authentication", "http-Digest");
                break;
            case "BOTH":
                tinyDB.putString("authentication", "both");
                break;
        }

    }

    public void BT_multiViews(View v){
        Intent intent = new Intent(ONVIF_MAIN.this, MultiPlayers.class);
        startActivity(intent);
    }


    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch(checkedId){
                case R.id.BT_ws_usernameToken:

                    tinyDB.putString("authentication", "ws-usernameToken");
                    circularBTN1.setProgress(0);
                    break;
                case R.id.BT_http_digest:

                    tinyDB.putString("authentication", "http-Digest");
                    circularBTN1.setProgress(0);
                    break;
                case R.id.BT_Both:

                    tinyDB.putString("authentication", "both");
                    circularBTN1.setProgress(0);
                    break;
            }
        }
    };

    Handler waitValueHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Log.v("waitValueHandler", "" + msg.what);
            switch (msg.what){
                case -1:
                    ONVIF_MAIN.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            circularBTN1.setProgress(-1);
                        }
                    });
                    break;
                case 0:
                    String deviceUri = tinyDB.getString("deviceURI");
                    GetONVIFinfo getONVIFStream = new GetONVIFinfo();
                    getONVIFStream.execute("streamUri", deviceUri);
                    break;
                case 1:
                    String deviceUri1 = tinyDB.getString("deviceURI");
                    GetONVIFinfo getONVIFPTZ = new GetONVIFinfo();
                    getONVIFPTZ.execute("PTZUri", deviceUri1);
                    break;
                case 2:
                    tinyDB.putString("profileName", profileStr);
                    tinyDB.putString("streamUri", streamUriArr[0]);
                    tinyDB.putString("PTZUri", PTZUri);
                    ONVIF_MAIN.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            circularBTN1.setProgress(100);
                            showStreamUri.setText(streamUriArr[0]);
                        }
                    });
                    break;
                case 3:
                    GetONVIFinfo getProfile = new GetONVIFinfo();
                    getProfile.execute("digest_profile", tinyDB.getString("deviceURI"));
                    break;
                case 4:
                    GetONVIFinfo getHeaders2 = new GetONVIFinfo();
                    getHeaders2.execute("getHeaders2", tinyDB.getString("deviceURI"));
                    break;
                case 5:
                    GetONVIFinfo getStreamUri = new GetONVIFinfo();
                    getStreamUri.execute("digest_streamUri", tinyDB.getString("deviceURI"));
                    break;
                case 6:
                    GetONVIFinfo getHeaders3 = new GetONVIFinfo();
                    getHeaders3.execute("getHeaders3", tinyDB.getString("deviceURI"));
                    break;
                case 7:
                    GetONVIFinfo getPTZUri = new GetONVIFinfo();
                    getPTZUri.execute("digest_PTZUri", tinyDB.getString("deviceURI"));

                    for(int i = 0;i < streamUriArr.length; i++){
                        Log.v("streamUri" + i, streamUriArr[i]);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    /**
     **/
    public void circularButton(){

        circularBTN1 = (CircularProgressButton)findViewById(R.id.BT_getStreamUri);
        circularBTN1.setIndeterminateProgressMode(true);
        circularBTN1.setProgress(0);
        circularBTN1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String auth = tinyDB.getString("authentication");
                circularBTN1.setProgress(1);
                String deviceUri = tinyDB.getString("deviceURI");
                Log.v("deviceURI ", deviceUri);
                switch (auth){
                    case "ws-usernameToken":
                        GetONVIFinfo getONVIFprofile = new GetONVIFinfo();
                        getONVIFprofile.execute("profile", deviceUri);
                        break;
                    case "http-Digest":
                        GetONVIFinfo digest = new GetONVIFinfo();
                        digest.execute("getHeaders1", deviceUri);
                        Log.v("circularbtn", "http");
                        break;
                    case "both":
                        GetONVIFinfo both = new GetONVIFinfo();
                        both.execute("getHeaders1", deviceUri);
                        break;
                }

            }
        });
    }

    public void BT_play(View v){
        circularBTN1.setProgress(0);

        if(streamUriArr == null){
            Toast.makeText(this, "Please choose a device and get streamUri", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(this, VXG_Player.class);

            Bundle bundle = new Bundle();
            bundle.putStringArray("streamUriArr", streamUriArr);
            bundle.putStringArray("resolutionArr", hxwArr);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void BT_Parameters(View v){
        circularBTN1.setProgress(0);
        Intent intent = new Intent(this, ONVIF_ParameterView.class);
        startActivity(intent);
    }

    public void BT_reset(View v){
        tinyDB.clear();
        Toast.makeText(this, "Clear all values", Toast.LENGTH_SHORT).show();
        loadExternalIp();
    }

    public void BT_getVideo(View v){
        Intent intent = new Intent(this, GetVideoList.class);
        startActivity(intent);
    }

    private class GetExternalIp extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(1500);
                connection.setConnectTimeout(1500);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.flush();

                int statusCode = connection.getResponseCode();

                switch (statusCode){
                    case 200:
                        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                        String response = convertInputStreamToString(inputStream);
                        System.out.println("external ip = " + response);
                        return response;
                    default:
                        return "Network failed";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }



    private class GetONVIFinfo extends AsyncTask<String, Void, String>{

        String status = "";
        String userName = tinyDB.getString("userName");
        String password = tinyDB.getString("userPassword");

        @Override
        protected String doInBackground(String... params) {
            String url = params[1];
            String method = params[0];

            if(!url.equals(null)){
                switch (method){
                    case "profile":
                        GetProfiles getProfiles = new GetProfiles(url, userName, password);
                        profilesArr = Arrays.copyOf(getProfiles.getProfilesArr(), getProfiles.getProfilesArr().length);
                        hxwArr = Arrays.copyOf(getProfiles.getHxWArr(), getProfiles.getHxWArr().length);

                        profileStr = profilesArr[0];
                        status = "getProfile";
                        streamUriArr = new String[profilesArr.length];
                        return profileStr;
                    case "streamUri":
                        if(profileStr.equals(null)){
                           Log.e(TAG, "profile not found");
                            return "error";
                        }else{
                            GetStreamUri getStreamUri = new GetStreamUri(url, profilesArr[profileCount], userName, password);
                            streamUriSoap = getStreamUri.getStreamUri();
                            if(streamUriSoap.equals(null)){
                                return "error";
                            }else{
                                streamUri = streamUriSoap.getProperty(0).toString();
                                streamUriArr[profileCount] = streamUri;
                                profileCount++;
                                Log.v(TAG, "streamUri = " + streamUri);
                                status = "getStreamUri";
                                return streamUri;
                            }

                        }
                    case "PTZUri":
                        GetPTZUri getPTZUri = new GetPTZUri(url, userName, password);
                        PTZUri = getPTZUri.getPTZUri();
                        status = "getPTZUri";
//                        Log.v("status ", status);
                        return PTZUri;
                    case "getHeaders1":
                        getServerHeaders getHeaders1 = new getServerHeaders(url);
                        realm = getHeaders1.getRealm();
                        qop = getHeaders1.getqop();
                        nonce = getHeaders1.getNonce();
                        opaque = getHeaders1.getopaque();
                        status = "getHeaders1";
//                        Log.v("status ", status);
                        return realm;
                    case "getHeaders2":
                        getServerHeaders getHeaders2 = new getServerHeaders(url);
                        realm = getHeaders2.getRealm();
                        qop = getHeaders2.getqop();
                        nonce = getHeaders2.getNonce();
                        opaque = getHeaders2.getopaque();
                        status = "getHeaders2";
//                        Log.v("status ", status);
                        return nonce;
                    case "getHeaders3":
                        getServerHeaders getHeaders3 = new getServerHeaders(url);
                        realm = getHeaders3.getRealm();
                        qop = getHeaders3.getqop();
                        nonce = getHeaders3.getNonce();
                        opaque = getHeaders3.getopaque();
                        status = "getHeaders3";
//                        Log.v("status ", status);
                        return nonce;
                    case "digest_profile":
                        GetProfiles getProfiles2 = new GetProfiles(url, realm, qop, nonce, opaque, userName, password);
                        digest_profilesArr = Arrays.copyOf(getProfiles2.getProfilesArr(), getProfiles2.getProfilesArr().length);
                        hxwArr = Arrays.copyOf(getProfiles2.getHxWArr(), getProfiles2.getHxWArr().length);
                        for(int i = 0; i < digest_profilesArr.length; i++){
                            Log.v("profilesArr" + i, digest_profilesArr[i]);
                        }
                        for(int i = 0; i < hxwArr.length; i++){
                            Log.v("profilesArr" + i, hxwArr[i]);
                        }
                        profileStr = digest_profilesArr[0];
//                        profileStr = getProfiles2.getProfiles();
                        Log.v(TAG , "profile = " + profileStr);
                        status = "digest_profile";
                        streamUriArr = new String[digest_profilesArr.length];
                        return profileStr;
                    case "digest_streamUri":
                        if(profileStr.equals(null)){
                            Log.e(TAG, "profile not found");
                            waitValueHandler.sendEmptyMessage(-1);
                        }else{
                            GetStreamUri getStreamUri = new GetStreamUri(url, digest_profilesArr[profileCount], realm, qop, nonce, opaque, userName, password);
                            streamUri = getStreamUri.getStreamUriStr();
                            streamUriArr[profileCount] = streamUri;
                            profileCount++;
                            Log.v(TAG, "streamUri = " + streamUri);
                        }
//                        Log.v("status ", status);
                        status = "digest_streamUri";
                        return streamUri;
                    case "digest_PTZUri":
                        GetPTZUri getPTZUri2 = new GetPTZUri(url, realm, qop, nonce, opaque, userName, password);
                        PTZUri = getPTZUri2.getPTZUri();
                        status = "digest_PTZUri";
//                        Log.v("status ", status);
                        return PTZUri;
                }
            }else{
                Log.e(TAG, "getONVIFinfo error");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
            switch (status){
                case "getProfile":
                    for(int i = 0; i < profilesArr.length; i++){
                        Log.v("profilesArr" + i, profilesArr[i]);
                    }
                    for(int i = 0; i < hxwArr.length; i++){
                        Log.v("hxwArr" + i, hxwArr[i]);
                    }
                    if(result == null){
                        waitValueHandler.sendEmptyMessage(-1);
                    }else {
                        waitValueHandler.sendEmptyMessage(0);
                    }
                    break;
                case "getStreamUri":
                    if(profileCount < profilesArr.length){
                        waitValueHandler.sendEmptyMessage(0);
                    }else{
                        profileCount = 0;
                        waitValueHandler.sendEmptyMessage(1);
                    }
                    break;
                case "getPTZUri":
                    waitValueHandler.sendEmptyMessage(2);
                    break;
                case "getHeaders1":
                    waitValueHandler.sendEmptyMessage(3);
                    break;
                case "digest_profile":
                    waitValueHandler.sendEmptyMessage(4);
                    break;
                case "getHeaders2":
                    waitValueHandler.sendEmptyMessage(5);
                    break;
                case "digest_streamUri":
                    if(profileCount < digest_profilesArr.length){
                        waitValueHandler.sendEmptyMessage(5);
                    }else{
                        profileCount = 0;
                        waitValueHandler.sendEmptyMessage(6);
                    }

                    break;
                case "getHeaders3":
                    waitValueHandler.sendEmptyMessage(7);
                    break;
                case "digest_PTZUri":
                    waitValueHandler.sendEmptyMessage(2);
                    break;
                default:
                    waitValueHandler.sendEmptyMessage(-1);
                    break;
            }

        }
    }
}
