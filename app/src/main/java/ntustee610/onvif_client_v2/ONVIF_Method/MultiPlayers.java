package ntustee610.onvif_client_v2.ONVIF_Method;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import ntustee610.onvif_client_v2.DB.TinyDB;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetProfiles;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetStreamUri;
import ntustee610.onvif_client_v2.R;
import ntustee610.onvif_client_v2.VXGPlayer.PlayerCallBack3;
import veg.mediaplayer.sdk.MediaPlayer;

public class MultiPlayers extends AppCompatActivity {

    private final String TAG = MultiPlayers.class.getName();

    MediaPlayer player1, player2, player3;
    ProgressBar playerLoader1, playerLoader2, playerLoader3;
    PlayerCallBack3 playercallback1 = null,
            playercallback2 = null,
            playercallback3 = null;
    String url,
            url1 = "rtsp://admin:123456@192.168.50.79:5566/cam/realmonitor?channel=1&subtype=0&unicast=true&proto=Onvif",
            url2 = "rtsp://admin:123456@192.168.50.131:5544/cam/realmonitor?channel=1&subtype=0&unicast=true&proto=Onvif",
            url3 = "rtsp://admin:123456@192.168.50.116:5555/live/ch0";

    String userName = "admin";
    String password = "123456";
    String[] userNameArr, passwordArr;
    String[] profileList, streamList;
    int currentCount = 0;
    int deviceNum = 0;
    ArrayList<String> deviceURIs;
    String profileStr = "",
            streamUriStr = "";
    String[] secureUriArr;

    int countNum = 0;   //alertDialogs showtimes

    //connection
    int connectionProtocol = -1;            // 0 - udp, 1 - tcp, 2 - http, 3 - https, -1 - AUTO
    int connectionDetectionTime = 5000;     // in milliseconds
    int connectionBufferingTime = 1000;     // in milliseconds

    // decoder
    int decoderType = 1;                    // 0 - soft, 1 - hard stagefright
    int decoderNumberOfCpuCores = 0;        // 0 - autodetect and use, >0 - manually set

    //render
    int rendererType = 1;                   // 0 - SDL, 1 - pure OpenGL
    int rendererEnableColorVideo = 1;       // grayscale, 1 - color
    int rendererAspectRatioMode = 1;        // 0 - resize, 1 - aspect

    // synchro
    int synchroEnable = 1;                  // enable audio video synchro
    int synchroNeedDropVideoFrames = 0;     // drop video frames if it older

    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_players);
        findViews();

        tinyDB = new TinyDB(this);

        checkDeviceURIs();
    }

    private void findViews(){
        player1 = (MediaPlayer)findViewById(R.id.player1);
        player2 = (MediaPlayer)findViewById(R.id.player2);
        player3 = (MediaPlayer)findViewById(R.id.player3);
        playercallback1 = new PlayerCallBack3(this, player1);
        playercallback2 = new PlayerCallBack3(this, player2);
        playercallback3 = new PlayerCallBack3(this, player3);
        playerLoader1 = (ProgressBar)findViewById(R.id.player1loaderIndicator);
        playerLoader2 = (ProgressBar)findViewById(R.id.player2loaderIndicator);
        playerLoader3 = (ProgressBar)findViewById(R.id.player3loaderIndicator);
    }

    private void checkDeviceURIs(){
        if(!tinyDB.getListString("deviceURIs").isEmpty()){
            deviceURIs = new ArrayList<>(tinyDB.getListString("deviceURIs"));
            Log.v(TAG, "device num = " + deviceURIs.size());
            deviceNum = deviceURIs.size();
            profileList = new String[deviceNum];
            streamList = new String[deviceNum];
            for(int i = 0; i < deviceNum; i++){
                Log.v(TAG, deviceURIs.get(i));
            }
            showAccountDetail();
            //start getProfiles & streamUri
//            new getStreamInfo().execute(deviceURIs.get(currentCount), "getProfiles");

        }else{
            final AlertDialog.Builder builder ;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                builder = new AlertDialog.Builder(MultiPlayers.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
            }else{
                builder = new AlertDialog.Builder(MultiPlayers.this);
            }
            builder.setTitle("ERROR");
            builder.setMessage("Please go to SEARCH DEVICE and select devices");


            builder.setPositiveButton(
                    "Confirm",
                    new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void showAccountDetail(){
        //check device number and create related alertDialogs to input username and password
        if(deviceNum == 1) {
            userNameArr = new String[deviceNum];
            passwordArr = new String[deviceNum];
            alertDialog(1);
        }else if(deviceNum == 2){
            userNameArr = new String[deviceNum];
            passwordArr = new String[deviceNum];
            alertDialog(2);
        }else{
            userNameArr = new String[deviceNum];
            passwordArr = new String[deviceNum];
            alertDialog(3);
        }



    }

    private void alertDialog(final int Num){
        Log.v(TAG, "alertDialog" + currentCount);
        final AlertDialog.Builder builder ;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder = new AlertDialog.Builder(MultiPlayers.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        }else{
            builder = new AlertDialog.Builder(MultiPlayers.this);
        }
        builder.setTitle(deviceURIs.get(currentCount));
        final EditText username = new EditText(this);
        final EditText password = new EditText(this);
        username.setHint("username");
        password.setHint("password");

        username.setText("admin");
        password.setText("123456");

        LinearLayout LL = new LinearLayout(this);
        LL.setOrientation(LinearLayout.VERTICAL);
        LL.addView(username);
        LL.addView(password);
        builder.setView(LL);


        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userNameArr[currentCount] = username.getText().toString();
                        passwordArr[currentCount] = password.getText().toString();
                        currentCount++;
                        if(currentCount < Num){
                            alertDialog(Num-1);
                        }else{
                            currentCount = 0;
                            for(int i = 0; i < deviceNum; i++){
                                Log.v(TAG, "userNameArr" + i + "=" + userNameArr[i] + " , " + "passwordArr" + i + "=" + passwordArr[i]);
                            }
                            new getStreamInfo().execute(deviceURIs.get(currentCount), "getProfiles");
                        }

                    }
                });


        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        builder.setCancelable(true);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void play(MediaPlayer currentPlayer){
        currentPlayer.Close();
        PlayerCallBack3 playercallback;
        if(currentPlayer == player1){
            playercallback = playercallback1;
        }else if(currentPlayer == player2){
            playercallback = playercallback2;
        }else{
            playercallback = playercallback3;
        }
        currentPlayer.Open(url,connectionProtocol, connectionDetectionTime, connectionBufferingTime,
                decoderType,
                rendererType,
                synchroEnable,
                synchroNeedDropVideoFrames,
                rendererEnableColorVideo,
                rendererAspectRatioMode,
                currentPlayer.getConfig().getDataReceiveTimeout(),
                decoderNumberOfCpuCores,
                playercallback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            player1.Close();
            player2.Close();
            player3.Close();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showProgressView(MediaPlayer currentPlayer){
        if(currentPlayer == player1){
            playerLoader1.setVisibility(View.VISIBLE);
        }else if(currentPlayer == player2){
            playerLoader2.setVisibility(View.VISIBLE);
        }else{
            playerLoader3.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressView(MediaPlayer currentPlayer){
        if(currentPlayer == player1){
            playerLoader1.setVisibility(View.GONE);
        }else if(currentPlayer == player2){
            playerLoader2.setVisibility(View.GONE);
        }else{
            playerLoader3.setVisibility(View.GONE);
        }
    }

    public boolean isPlayerBusy(MediaPlayer currentPlayer){
        if(currentPlayer == player1){
            if(player1 != null && (player1.getState() == MediaPlayer.PlayerState.Closing ||
                    player1.getState() == MediaPlayer.PlayerState.Opening)){
                return true;
            }else
                return false;
        }else if(currentPlayer == player2){
            if(player2 != null && (player2.getState() == MediaPlayer.PlayerState.Closing ||
                    player2.getState() == MediaPlayer.PlayerState.Opening)){
                return true;
            }else return false;
        }else{
            if(player3 != null && (player3.getState() == MediaPlayer.PlayerState.Closing ||
                    player3.getState() == MediaPlayer.PlayerState.Opening)){
                return true;
            }else return false;
        }
    }

    public int mOldMsg = 0;


    private void getStreamInfo(){

    }

    /**
     * param0 : url
     * param1 : method
     * param2 : profileName
     * return string
     * */

    private class getStreamInfo extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String method = params[1];

            switch (method){
                case "getProfiles":
                    GetProfiles getProfiles = new GetProfiles(url, userName, password);
                    profileStr = getProfiles.getProfiles();

                    if(!profileStr.equals("")){
                        profileList[currentCount] = profileStr;
                        currentCount++;
                        return "getProfiles";
                    }else
                        return "error";
                case "getStreamUri":
                    GetStreamUri getStreamUri = new GetStreamUri(url, profileList[currentCount], userName, password);
                    streamUriStr = getStreamUri.getStreamUri().getProperty(0).toString();

                    if(!streamUriStr.equals("")){
                        streamList[currentCount] = streamUriStr;
                        currentCount++;
                        return "getStreamUri";
                    }else{
                        return  "error";
                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);

            switch (status){
                case "getProfiles":
                    if(currentCount < deviceNum){
                        new getStreamInfo().execute(deviceURIs.get(currentCount), "getProfiles");
                    }else{
                        for(int i = 0; i < deviceNum; i++){
                            Log.v(TAG, "profle" + i + "= " + profileList[i]);
                        }
                        currentCount = 0;
                        new getStreamInfo().execute(deviceURIs.get(currentCount), "getStreamUri");
                        Log.v(TAG, "streamUri count = " + currentCount);
                    }
                    break;
                case "getStreamUri":
                    if(currentCount < deviceNum){
                        Log.v(TAG, "streamUri count = " + currentCount);
                        new getStreamInfo().execute(deviceURIs.get(currentCount), "getStreamUri");
                    }else{
                        currentCount = 0;
                        Log.v(TAG, "getAllStreamUri");

                        for(int i = 0; i < streamList.length; i++){
                            Log.v(TAG, "stream" + i + " = " + streamList[i]);
                        }

                        secureUriArr = new String[deviceNum];

                        for(int i = 0; i < deviceNum; i++){
                            secureUriArr[i] = streamList[i].replace("rtsp://", "rtsp://" + userNameArr[i] + ":" + passwordArr[i] + "@");
                            Log.v(TAG, "secureUriArr" + i + "=" +secureUriArr[i]);
                        }

                        if(deviceNum == 1){
                            url = secureUriArr[0];
                            play(player1);
                        }else if(deviceNum == 2){
                            url = secureUriArr[0];
                            play(player1);
                            url = secureUriArr[1];
                            play(player2);
                            url = "rtsp://admin:123456@192.168.50.225:5555/live/ch0";
                            play(player3);
                        }else if(deviceNum == 3){
                            url = secureUriArr[0];
                            play(player1);
                            url = secureUriArr[1];
                            play(player2);
                            url = secureUriArr[2];
                            play(player3);
                        }
                    }
                    break;
                default:
                    Toast.makeText(MultiPlayers.this, "IP CAM not found", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}
