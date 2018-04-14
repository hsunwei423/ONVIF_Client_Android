package ntustee610.onvif_client_v2.VXGPlayer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;

import ntustee610.onvif_client_v2.ONVIF_Method.MultiPlayers;
import ntustee610.onvif_client_v2.ONVIF_Method.VXG_Player;
import veg.mediaplayer.sdk.MediaPlayer;

/**
 * Created by weihsun on 2017/6/3.
 */

public class PlayerCallBack3 implements MediaPlayer.MediaPlayerCallback {
    private MultiPlayers act;
    MediaPlayer player = null;
    final public static String TAG = "PlayerCallBack";

    public Handler handler = new Handler(){

        String strText = "Status:";

        @Override
        public void handleMessage(Message msg) {

            MediaPlayer.PlayerNotifyCodes status = (MediaPlayer.PlayerNotifyCodes)msg.obj;
            Log.v(TAG, "Notify: " + status);

            switch (status){
                case PLP_TRIAL_VERSION:
//                    Toast.makeText(act.getApplicationContext(), "Demo Version!",
//                            Toast.LENGTH_SHORT).show();
                    act.play(player);
                    act.hideProgressView(player);
                    break;
                case CP_CONNECT_STARTING:
                    player_state_error = PlayerCallBacks.PlayerStatesError.None;
                    act.showProgressView(player);
                    break;
                case VRP_NEED_SURFACE:
                    break;
                case PLP_PLAY_SUCCESSFUL:
                    player_state_error = PlayerCallBacks.PlayerStatesError.None;
                    act.hideProgressView(player);
                    break;
                case PLP_CLOSE_STARTING:
                    break;
                case PLP_CLOSE_SUCCESSFUL:
                    act.hideProgressView(player);
                    System.gc();
                    break;
                case PLP_CLOSE_FAILED:
                    act.hideProgressView(player);
                    break;
                case CP_CONNECT_FAILED:
                    player_state_error = PlayerCallBacks.PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;
                case PLP_BUILD_FAILED:
                    player_state_error = PlayerCallBacks.PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;
                case PLP_PLAY_FAILED:
                    player_state_error = PlayerCallBacks.PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;
                case PLP_ERROR:
                    player_state_error = PlayerCallBacks.PlayerStatesError.Disconnected;
                    act.hideProgressView(player);
                    break;
                case CP_INTERRUPTED:
                    act.hideProgressView(player);
                    break;
//                case CP_ERROR_NODATA_TIMEOUT:
//                    player_state_error = PlayerStatesError.None;
//                    act.hideProgressView(player);
                case CP_STOPPED:
                case VDP_STOPPED:
                case VRP_STOPPED:
                case ADP_STOPPED:
                case ARP_STOPPED:
                    if (!act.isPlayerBusy(player))
                    {
                        //stopProgressTask();
                        //player_state = PlayerStates.Busy;
                        Log.e(TAG, "AUDIO_RENDERER_PROVIDER_STOPPED_THREAD Close.");
                        player.Close();
                    }
                    break;
                case PLP_EOS:
//                    Log.e(TAG, "PLP_EOS: " +act.isFileUrl + ", " + player.getState());
                    break;
                case CP_ERROR_DISCONNECTED:
                    break;
                default:
            }

            strText += " "+status;
        }
    };

    public enum PlayerStatesError{
        None,
        Disconnected,
        Eos
    };

    public PlayerCallBacks.PlayerStatesError player_state_error = PlayerCallBacks.PlayerStatesError.None;

    public PlayerCallBack3(MultiPlayers act, MediaPlayer player){
        this.act = act;
        this.player = player;
    }


    @Override
    public int Status(int arg0) {
        Log.v(TAG, "-Status arg-" + arg0);

        MediaPlayer.PlayerNotifyCodes status = MediaPlayer.PlayerNotifyCodes.forValue(arg0);
        if(handler == null || status == null)   return 0;
        if(player != null) Log.v(TAG, "Current state: " + player.getState());

        switch (status){
            case CP_CONNECT_FAILED:
            case PLP_BUILD_FAILED:
            case PLP_PLAY_FAILED:
            case PLP_ERROR:
            case CP_ERROR_DISCONNECTED:
            {
                player_state_error = PlayerCallBacks.PlayerStatesError.Disconnected;
                Message msg = new Message();
                msg.obj = status;
                msg.what = 1;
                handler.removeMessages(act.mOldMsg);
                act.mOldMsg = msg.what;
                handler.sendMessage(msg);
                break;
            }
            default:{
                Message msg = new Message();
                msg.obj = status;
                msg.what = 1;
                handler.removeMessages(act.mOldMsg);
                act.mOldMsg = msg.what;
                handler.sendMessage(msg);
            }

        }
        return 0;
    }

    @Override
    public int OnReceiveData(ByteBuffer byteBuffer, int i, long l) {
        return 0;
    }
}
