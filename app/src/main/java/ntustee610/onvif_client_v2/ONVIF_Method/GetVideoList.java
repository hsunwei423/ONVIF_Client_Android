package ntustee610.onvif_client_v2.ONVIF_Method;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ntustee610.onvif_client_v2.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetVideoList extends AppCompatActivity {

    private MyAdapter myAdapter;
    private RecyclerView mRecyclerview;
    private TextView showIP;
    String toArray[];
    protected ArrayList<String> getData ;
//    String serverUri = "http://140.118.7.11/storage.php";
    String serverUri = "http://140.118.7.11:8080";
//    String rtspServerUri = "http://140.118.7.11:8081/";
    String rtspServerUri = "http://140.118.7.11/";
    String responseStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_video_list);
        findViews();
        getVideoFromOkHttp();
    }

    private void findViews(){
        showIP = (TextView)findViewById(R.id.showServerIp);
        showIP.setText(serverUri);
    }

    private void getData(){
        getData = new ArrayList<>();
        String temp = "";
        for(int i = 0; i< toArray.length ; i++){
//            toArray[i] = toArray[i].replace("C:\\AppServ\\www\\videos\\",  rtspServerUri + "videos/");
            temp = rtspServerUri  + toArray[i] + ".ts";
            getData.add(temp);
            Log.v("toArray", toArray[i]);
        }
        myAdapter = new MyAdapter(getData);
        mRecyclerview = (RecyclerView)findViewById(R.id.showVideoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        mRecyclerview.setAdapter(myAdapter);

    }

    public void BT_refresh(View v){
        getVideoFromOkHttp();
    }

    private void getVideoFromOkHttp() {

        new getVideoListAsynv().execute(serverUri, "playlist", "");
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.info_text);
            }

        }

        public MyAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_items, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(mData.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = getData.get(position);
//                    String url = toArray[position];
                    Log.v("url = ", url);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", url);
                    Intent intent = new Intent(GetVideoList.this, PlayVideoFromServer.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

    private class getVideoListAsynv extends AsyncTask<String, Void, String>{
        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(GetVideoList.this);
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(1500);
                connection.setConnectTimeout(5000);
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
                    return "error";
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "error";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progDailog.dismiss();
            Log.v("recordingRes", s);
//            toArray = responseStr.split("\\r\\n");
//            getData();
            if(s.equals("error")){
                Toast.makeText(GetVideoList.this, "server not found", Toast.LENGTH_SHORT).show();
            }else{
                toArray = s.split(".ts");
                getData();
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
}
