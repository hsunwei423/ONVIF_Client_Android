package ntustee610.onvif_client_v2.ONVIF_Method;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

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

import ntustee610.onvif_client_v2.R;
import ntustee610.onvif_client_v2.VXGPlayer.PlayerCallBacks2;
import veg.mediaplayer.sdk.MediaPlayer;

public class PlayVideoFromServer extends AppCompatActivity {

    private final static String TAG = PlayVideoFromServer.class.getName();

    MediaPlayer player2;
    ProgressBar loaderIndicator2 = null;
    private PlayerCallBacks2 Player2CallBacks = null;
    private String serverUri = "http://140.118.7.11:8081";
    private String url = "";
    private String serverUri2 = "http://140.118.7.11:8080";
    private String fileName = "";
    SeekBar seekPanelPlayerControlSeekbar;
    TextView textPanelPlayerControlPosition, textPanelPlayerControlDuration;
    Button BT_play_pause;

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


    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video_from_server);
        findViews();
        getBundleValue();
        seekBarListener();
        play();
        play_pause();

        PlayVideoFromServer.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mediaPositionUpdate();
                mHandler.postDelayed(this, 500);
            }

        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                break;
        }
    }

    private void findViews(){
        player2 = (MediaPlayer)findViewById(R.id.playerView2);
        loaderIndicator2 = (ProgressBar)findViewById(R.id.loaderIndicator2);
        Player2CallBacks = new PlayerCallBacks2(this, player2);
        seekPanelPlayerControlSeekbar = (SeekBar) findViewById(R.id.seekPanelPlayerControlSeekbar);
        textPanelPlayerControlPosition = (TextView)findViewById(R.id.textPanelPlayerControlPosition);
        textPanelPlayerControlDuration = (TextView)findViewById(R.id.textPanelPlayerControlDuration);
        BT_play_pause = (Button)findViewById(R.id.BT_play_pause);
    }

    private void getBundleValue(){
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        Log.v("url", url);
//        new sendParasAsync().execute(serverUri2, "streaming", fileName);
    }

    public void BT_play_pause(View v){
        switch (player2.getState()){
            case Started:
                BT_play_pause.setBackgroundResource(R.drawable.ic_play_circle_outline_white_36pt_3x);
                player2.Pause();
                break;
            case Paused:
                BT_play_pause.setBackgroundResource(R.drawable.ic_pause_circle_outline_white_36pt_3x);
                player2.Play();
                break;

            default:
                break;
        }
    }

    private void seekBarListener(){
        seekPanelPlayerControlSeekbar.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(player2 == null) return true;

                MediaPlayer.Position position = player2.getLiveStreamPosition();
                if(position == null)    return  true;

                if(position.getStreamType() != 2){
                    long pos = position.getDuration() - 1000;
                    if(pos <= 0 || (pos > (60 * 60 * 12 * 1000))){
                        return true;
                    }
                }
                return false;
            }
        });

        seekPanelPlayerControlSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if(player2 == null || !arg2) return;

                MediaPlayer.Position position = player2.getLiveStreamPosition();
                if(position == null)    return;
                if(position.getStreamType() != 2){
                    long pos = position.getDuration() - 1000;
                    if(pos > 0 && (pos <= (60 * 60 * 12 * 1000))){
                        player2.setStreamPosition(arg1);
                    }
                    return;
                }

                player2.setStreamPosition(position.getFirst() + arg1);
                mediaLivePositionUpdate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    synchronized private void mediaPositionUpdate(){
        Log.v(TAG, "=mediaPositionUpdate=");
        if(player2 ==  null)    return;

        long duration = player2.getStreamDuration();
        long position = player2.getStreamPosition();

        if (player2 != null && (player2.getState() != MediaPlayer.PlayerState.Started &&
                player2.getState() != MediaPlayer.PlayerState.Opened &&
                player2.getState() != MediaPlayer.PlayerState.Stopped &&
                player2.getState() != MediaPlayer.PlayerState.Paused))
            duration = 0;

        if(duration > 0)    duration -= 1000;
        if(duration <= 0 || (duration > (60 * 60 * 12 * 1000))){
            position = 0;
            duration = (60 * 60 * 12 * 1000);
        }
        if(position > duration) position = duration;

        Log.v(TAG, "=mediaPositionUpdate=" + position + ", " + duration);

        if((int)duration != seekPanelPlayerControlSeekbar.getProgress()){
            textPanelPlayerControlPosition.setText(convertStreamPositionToTime(position / 1000));
            seekPanelPlayerControlSeekbar.setProgress((int)position);
        }

        if((int) duration != seekPanelPlayerControlSeekbar.getMax()){
            seekPanelPlayerControlSeekbar.setMax((int)duration);
        }
        textPanelPlayerControlDuration.setText(convertStreamPositionToTime(duration / 1000));

    }

    synchronized private void mediaLivePositionUpdate(){
        Log.v(TAG, "=mediaPositionUpdate=");
        if(player2 == null) return;

        MediaPlayer.Position pos = player2.getLiveStreamPosition();
        if(pos == null) return;
        if(pos.getStreamType() != 2){
            mediaPositionUpdate();
            return;
        }

        long stream_position = player2.getRenderPosition();
//        if (lockChangePosition && lockedLatestStreamPosition == -1)
//            lockedLatestStreamPosition = (int) stream_position;

        long duration = pos.getDuration();
        long first = pos.getFirst();
        long current = player2.getRenderPosition();
        long last = pos.getLast();

        duration = last - first;
        current = current - first;

//        Log.i(TAG, "mediaLivePositionTimerMethod: " + lockChangePosition + ", cur: " + current + ", touched: " + lockedChangePosition);
//        if (lockChangePosition && lockedLatestStreamPosition != -1 &&
//                lockedLatestStreamPosition != (int) stream_position) {
//            lockChangePosition = false;
//            lockedLatestStreamPosition = -1;
//        }
//
//        if (lockChangePosition) current = lockedChangePosition;

        if(player2 != null && (player2.getState() != MediaPlayer.PlayerState.Started &&
                player2.getState() != MediaPlayer.PlayerState.Opened &&
                player2.getState() != MediaPlayer.PlayerState.Stopped &&
                player2.getState() != MediaPlayer.PlayerState.Paused))
            duration = 0;

        if(duration <= 0 || (duration > (60 * 60 * 12 * 1000))){
            current = 0;
            duration = (60 * 60 * 12 * 1000);
        }

        if(current > duration)  current = duration;

        if( (int)current != seekPanelPlayerControlSeekbar.getProgress()){
            textPanelPlayerControlPosition.setText("-" + convertStreamPositionToTime2(last / 1000 - first / 1000));
            seekPanelPlayerControlSeekbar.setProgress((int)current);
        }

        if((int)duration != seekPanelPlayerControlSeekbar.getMax()){
            seekPanelPlayerControlSeekbar.setMax((int)duration);
        }
        textPanelPlayerControlDuration.setText("Live");
    }

    private String convertStreamPositionToTime(long Time){
        long tns, thh, tmm, tss;
        tns = Time;
        thh = tns / 3600;
        tmm = (tns % 3600) / 60;
        tss = (tns % 60);
        return String.format("%02d:%02d:%02d", thh, tmm, tss);
    }

    private String convertStreamPositionToTime2(long Time) {
        long tns, thh, tmm, tss;
        tns = Time;
        thh = tns / 3600;
        tmm = (tns % 3600) / 60;
        tss = (tns % 60);

        return String.format("%02d:%02d:%02d", thh, tmm, tss);
    }

    private void play(){
        player2.Close();
        BT_play_pause.setBackgroundResource(R.drawable.ic_pause_circle_outline_white_36pt_3x);
        player2.Open(url, connectionProtocol, connectionDetectionTime, connectionBufferingTime,
                decoderType,
                rendererType,
                synchroEnable,
                synchroNeedDropVideoFrames,
                rendererEnableColorVideo,
                rendererAspectRatioMode,
                player2.getConfig().getDataReceiveTimeout(),
                decoderNumberOfCpuCores,
                Player2CallBacks);
    }

    private void play_pause(){

    }

    public void showProgressView(MediaPlayer currentPlayer){
        loaderIndicator2.setVisibility(View.VISIBLE);
    }

    public void hideProgressView(MediaPlayer currentPlayer){
        loaderIndicator2.setVisibility(View.GONE);
    }

    public boolean isPlayerBusy(){
        if(player2 != null && (player2.getState() == MediaPlayer.PlayerState.Closing ||
                player2.getState() == MediaPlayer.PlayerState.Opening)){
            return true;
        }else return false;
    }

    public int mOldMsg = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            player2.Close();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);   //remove thread
    }

    private class sendParasAsync extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(1500);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("data", params[1]);
                jsonObject.put("streamUri", params[2]);
                OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(jsonObject.toString().getBytes());
                outputStream.flush();

                int statusCode = connection.getResponseCode();

                if(statusCode == 200){
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    String responseStr = convertInputStreamToString(inputStream);
                    return responseStr;
                }else{

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            play();
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
}
