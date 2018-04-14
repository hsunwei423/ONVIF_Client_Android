package ntustee610.onvif_client_v2.ONVIF_Method;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

import ntustee610.onvif_client_v2.R;

public class RtspActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener{

    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private String videoSrc, path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_rtsp);
        createVideoView();
//        path = ONVIF_Storage.streamUri;
//        videoSrc = path.replace("rtsp://", "rtsp://" + ONVIF_Storage.userName + ":" + ONVIF_Storage.userPassord + "@");
    }

    private void createVideoView(){
        surfaceView = (SurfaceView)findViewById(R.id.rtspplayer);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        mediaPlayer.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mediaPlayer = new MediaPlayer();

            //setting up media player
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setDataSource(videoSrc);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.setOnErrorListener(this);


        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
