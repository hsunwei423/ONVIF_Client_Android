package ntustee610.onvif_client_v2.ONVIF_Method;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sbbi.upnp.DiscoveryAdvertisement;
import net.sbbi.upnp.DiscoveryEventHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import ntustee610.onvif_client_v2.DB.TinyDB;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetDeviceInformation;
import ntustee610.onvif_client_v2.ONVIF_Method.onvif.GetService;
import ntustee610.onvif_client_v2.UPNP.IGD_Discovery;
import ntustee610.onvif_client_v2.XML.ParseXml_SaxService;
import ntustee610.onvif_client_v2.R;

public class ONVIF_GetDevice extends AppCompatActivity {

    protected static final String TAG = "ONVIF_GetDevice";
    protected static final String TAG2 = "getMessage";
    private Handler mHandler;
    private Handler delay_half_sec = new Handler();
    String discovered_device;   //find XML of device
    String service_uri;
    String showDevice;
    List<String> Uri_ArrayList;
    ListView listInput;
    Button search_btn;
    ArrayAdapter<String> adapter;
    ArrayList<String> items, replaceItems, nodataItems = null;
    TextView showTV, selectedTV;
    String UUID_String, XML_String;
    byte[] buf, rev;
    DatagramPacket packet;
    RadioGroup connectTypeGroup;
    boolean isBT_loadHistoryPress = false;
    EditText inputIp;
    String IP;

    public TinyDB tinyDB;

    String Manufacturer, Model, FirmwareVersion, SerialNumber, HardwareId;

    boolean checkUser = false;
    boolean supportHttpDigest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_device);
        findViews();
        tinyDB = new TinyDB(this);

        initshow();
//        HandlerThread  handlerThread = new HandlerThread("MultiSocket");
//        handlerThread.start();
//        mHandler = new Handler(handlerThread.getLooper());
//        mHandler.post(mRunnable);
//        listInput.setAdapter(adapter);


    }

    public void findViews(){
        search_btn = (Button)findViewById(R.id.gd_search_btn);
        Log.v(TAG, "do findViews() ..");
        listInput = (ListView)findViewById(R.id.gd_listview);
        selectedTV = (TextView)findViewById(R.id.gd_selectedTV);
        connectTypeGroup = (RadioGroup)findViewById(R.id.btn_controlType);
        connectTypeGroup.setOnCheckedChangeListener(listener);
        inputIp = (EditText)findViewById(R.id.inputIp);
    }

    public void initshow(){
//            tinyDB.remove("localIP");
            items = new ArrayList<String>();
            Uri_ArrayList = new ArrayList<>();
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
            listInput.setAdapter(adapter);
    }

    public void showLocalHistory(){
        if(!tinyDB.getListString("localIP").isEmpty()){
            items = tinyDB.getListString("localIP");
            Uri_ArrayList = new ArrayList<>();
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
            listInput.setAdapter(adapter);
            listInput.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    tinyDB.putListString("deviceURIs", items);
                    showTV = (TextView) view;
                    showDevice = showTV.getText().toString();
                    selectedTV.setText(showDevice);
                    tinyDB.putString("deviceURI", showDevice);
                    clearValues();
                    finishView();
                }
            });
        }else{
            Toast.makeText(this, "no local data", Toast.LENGTH_SHORT).show();
        }

    }
        /*  select new device, so clear preview settings*/
    private void clearValues(){
        tinyDB.remove("profileName");
        tinyDB.remove("streamUri");
        tinyDB.remove("PTZUri");
        tinyDB.remove("authentication");
        tinyDB.remove("profileName");
    }

    public void searchBTN(View v){    //button listener
//        items.clear();
        HandlerThread  handlerThread = new HandlerThread("MultiSocket");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        mHandler.post(mRunnable);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listInput.setAdapter(adapter);
        listInput.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tinyDB.putListString("deviceURIs", items);
                showTV = (TextView) view;
                showDevice = showTV.getText().toString();
                selectedTV.setText(showDevice);
                tinyDB.putString("deviceURI", showDevice);
                clearValues();
//                finishView();
                getinfo();
            }
        });
    }

    private void finishView(){
        Intent intent = new Intent(ONVIF_GetDevice.this, ONVIF_MAIN.class);
        startActivity(intent);
        finish();
    }

    public void BT_finish(View v){
        inputIp.setText("http://192.168.50.225:8081/onvif/device_service");
        IP = inputIp.getText().toString();
        if(!IP.equals(null)){
            selectedTV.setText(IP);
            tinyDB.putString("deviceURI", IP);
            getinfo();
        }
        else Toast.makeText(this, "IP not found", Toast.LENGTH_SHORT);

//        finishView();
    }

    public void BT_loadHistory(View v){

        if(!tinyDB.getListString("externalIP").isEmpty()){
            isBT_loadHistoryPress = true;
            connectTypeGroup.check(R.id.BT_external);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tinyDB.getListString("externalIP"));
            listInput.setAdapter(adapter);
            isBT_loadHistoryPress = false;
            listInput.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    tinyDB.putListString("deviceURIs", items);
                    showTV = (TextView) view;
                    showDevice = showTV.getText().toString();
                    selectedTV.setText(showDevice);
                    tinyDB.putString("deviceURI", showDevice);
                    clearValues();
//                    finishView();
                    getinfo();
                }
            });
        }else{
            Toast.makeText(this, "no data found", Toast.LENGTH_SHORT).show();
        }
    }

    public void BT_clearHistory(View v){
        tinyDB.remove("externalIP");
        tinyDB.remove("localIP");
        selectedTV.setText("");
        nodataItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nodataItems);
        listInput.setAdapter(adapter);
        connectTypeGroup.check(R.id.BT_local);
    }

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.BT_local:
                    search_btn.setEnabled(true);
                    tinyDB.putString("connectType", "local");
                    showLocalHistory();
                    selectedTV.setText("");
                    break;
                case R.id.BT_external:
                    search_btn.setEnabled(false);
                    tinyDB.putString("connectType", "external");
                    if(isBT_loadHistoryPress == false){
//                        getExternalIP = new detectNetwork();
//                        getExternalIP.execute();

//                        selectedTV.setText(tinyDB.getString("external"));
                        replaceItems();
                    }
                    break;
            }
        }
    };

    private void replaceItems(){
        Log.v("localIP is emply", String.valueOf(tinyDB.getListString("localIP").isEmpty()));
        Log.v("external is empty", String.valueOf(tinyDB.getListString("external").isEmpty()));
        if((!tinyDB.getListString("localIP").isEmpty()) && (!tinyDB.getString("external").isEmpty())){
            replaceItems = new ArrayList<>(tinyDB.getListString("localIP"));
            Log.v("replaceItems ", replaceItems.toString());
//            replaceItems = tinyDB.getListString("localIP");
            String Url = tinyDB.getString("external");
            for(int i = 0 ;i < replaceItems.size(); i++){
                String str = replaceItems.get(i);
                String[] strArray = str.split(":");
                if(strArray.length == 3)    replaceItems.set(i, strArray[0].trim() + "://" + Url.trim() + ":" + strArray[2].trim());
                else if(strArray.length == 2){  //without port
                    //http
                    // //192.168.1.31/onvif/device_service
                    replaceItems.set(i, strArray[0].trim() + "://" + Url.trim() + "/onvif/device_service");
                }
            }

            tinyDB.putListString("externalIP", replaceItems);

            Uri_ArrayList = new ArrayList<>();
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, replaceItems);
            listInput.setAdapter(adapter);
            listInput.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    tinyDB.putListString("deviceURIs", replaceItems);
                    showTV = (TextView) view;
                    showDevice = showTV.getText().toString();
                    selectedTV.setText(showDevice);
                    tinyDB.putString("deviceURI", showDevice);
                    clearValues();
//                    finishView();
                    getinfo();
                }
            });
        }else{
            Toast.makeText(this, "no local or external IP data", Toast.LENGTH_SHORT).show();
        }


    }

    private void getinfo(){
        final AlertDialog.Builder builder ;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder = new AlertDialog.Builder(ONVIF_GetDevice.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        }else{
            builder = new AlertDialog.Builder(ONVIF_GetDevice.this);
        }
        builder.setTitle("Account information");
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
                        String userName = username.getText().toString();
                        String userPassword = password.getText().toString();
                        if(!userName.equals("") && !userPassword.equals("")){
                            tinyDB.putString("userName", userName);
                            tinyDB.putString("userPassword", userPassword);
//                            ONVIF_Storage.userName = userName;
//                            ONVIF_Storage.userPassord = userPassword;

                            new getDeviceInfo().execute(tinyDB.getString("deviceURI"), userName, userPassword);
                            if(checkUser == true){
                                tinyDB.putString("userName", userName);
                                tinyDB.putString("userPassword", userPassword);
                            }else{
                                Log.v("Account", "failed");
                            }
                        }else{
                            Toast.makeText(ONVIF_GetDevice.this, "please input your username and password", Toast.LENGTH_SHORT).show();
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

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try{
                for(int i = 0; i < 10; i++){
                    Log.v(TAG, "the "+ i + 1 +"times");
                    sendMulticast();
                    parse();
                }

                for(int i = 0 ; i <items.size(); i++){
                    Log.v("get items", items.get(i));
                }
                tinyDB.remove("localIP");
                tinyDB.putListString("localIP", items);


            }catch (Exception e){
                e.printStackTrace();
            }

        }

        public void sendMulticast() throws IOException {    //SOAP
            UUID_String = UUID.randomUUID().toString();
            XML_String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<e:Envelope " +
                    "xmlns:w=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" " +
                    "xmlns:d=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\" " +
                    "xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\" " +
                    "xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\"" +
                    "xmlns:e=\"http://www.w3.org/2003/05/soap-envelope\">" +
                    "<e:Header>" +
                    "<w:MessageID>" + UUID_String + "</w:MessageID>" +
                    "<w:To e:mustUnderstand=\"true\">urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>" +
                    "<w:Action e:mustUnderstand=\"true\">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>" +
                    "</e:Header>" +
                    "<e:Body>" +
                    "<d:Probe>" +
                    "<d:Types>dn:Device dn:NetworkVideoTransmitter</d:Types>"+
                    "</d:Probe>" +
                    "</e:Body>" +
                    "</e:Envelope>";

            MulticastSocket socket = new MulticastSocket(37022);    //指定port e.g.. 37022
            InetAddress address = InetAddress.getByName("239.255.255.250");
            /**
             * multicast提供特殊的IP位址，範圍224.0.0.0 ~ 239.255.255.255
             * 建立MulticastSocket對象後，還需要將MulticastSocket加入指定的多點群播地址
             * 使用joinGroup()方法加入指定組，使用LeaveGroup離開
             */

            socket.joinGroup(address);
            /**
             * 有些系統有多個網路接口，對multicast可能會有問題，這時候需要一個指定的方法監聽網路接口
             * 可用setInterface選擇MulticastSocket使用的網路接口
             * 可用getInterface查詢MulticastSocket監聽的網路接口
             * socket.setInterface(address);
             */
            //send
//            buf = XML_String.getBytes();
            buf = XML_String.getBytes();
            packet = new DatagramPacket(buf, buf.length, address, 3702);
            socket.send(packet);
            /**
             * Datagram socket使用UDP實現通訊，但不保證數據能夠到達目的地
             * Datagram以packet的方式發送數據，但不能保證以特定的順序到達目的地，因此packet裡需要有序號列，
             * 接收端可依據序號列決定是否收到所有的packet，在按照順序排列
             * DatagramPacket(byte buf[], int ilength); 用來接收數據
             * DatagramPacket(byte buf[], int ilength, InetAddress address, int port); 用來發送數據
             */
            //receive
            rev = new byte[2048];
            packet = new DatagramPacket(rev, rev.length);
            socket.receive(packet);
            discovered_device = new String(packet.getData()).trim();  //不加trim，則會打出2048byte，後面是亂碼
//            Log.v(TAG, "receive packet done");
//            Log.v(TAG, "get data : "+ discovered_device);

            socket.leaveGroup(address);
            socket.close();
        }
    };

    public void parse(){
        //read XML form discover_device
        InputStream inputStream = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            inputStream = new ByteArrayInputStream(discovered_device.getBytes(StandardCharsets.UTF_8));
//            Log.v(TAG, "inputStream = "+inputStream);
        }

        try{
            List<HashMap<String, String>> list = ParseXml_SaxService.readXML(inputStream, "XAddrs");

//            Log.v(TAG, "list :" +list);
//            Log.v(TAG, "list2 :" + list2);
            if(list!=null) {
                for (HashMap<String, String> map : list) {
                    service_uri = map.get("XAddrs");
//                    Log.v(TAG, "service_uri = " + service_uri);
                    if (!(Uri_ArrayList.contains(service_uri))) {
                        Uri_ArrayList.add(service_uri);
                        String[] mStringArray = new String[Uri_ArrayList.size()];
                        mStringArray = Uri_ArrayList.toArray(mStringArray);
//                        Log.v(TAG, "mStringArray.length:" + mStringArray.length);
                        for (int i = 0; i < mStringArray.length; i++) {
                            AddItem(mStringArray[i]);
//                            Log.v(TAG, "i = " + i);
//                            Log.v(TAG2, "mStringArray[" + i + "] = " + mStringArray[i]);

                        }
                    }
                }


            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void AddItem(String deviceUri){
        if(!deviceUri.equals("")){
            if(!(items.contains(deviceUri))){
                items.add(deviceUri);
                this.runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }

    }

    private class getDeviceInfo extends AsyncTask<String, Void, Vector>{

        @Override
        protected Vector doInBackground(String... params) {
            String url = params[0];
            String userName = params[1];
            String password = params[2];
            GetDeviceInformation getDeviceInformation = new GetDeviceInformation(url, userName, password);
            return getDeviceInformation.getDeviceInfo();
        }

        @Override
        protected void onPostExecute(Vector vector) {
            super.onPostExecute(vector);
            if(vector!=null){
                Manufacturer = vector.get(0).toString();
                Model = vector.get(1).toString();
                FirmwareVersion = vector.get(2).toString();
                SerialNumber = vector.get(3).toString();
                HardwareId = vector.get(4).toString();

                tinyDB.putString("deviceName", Manufacturer);

                Log.v("Manufacturer ", Manufacturer);
                Log.v("Model ", Model);
                Log.v("FirmwareVersion ", FirmwareVersion);
                Log.v("SerialNumber ", SerialNumber);
                Log.v("HardwareId ", HardwareId);

                AlertDialog.Builder builder ;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    builder = new AlertDialog.Builder(ONVIF_GetDevice.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
                }else{
                    builder = new AlertDialog.Builder(ONVIF_GetDevice.this);
                }
                builder.setTitle("Device Information");
                builder.setMessage("Manufacturer : " + Manufacturer + "\n" +
                        "Model : " + Model + "\n" +
                        "FirmwareVersion : " + FirmwareVersion + "\n" +
                        "serialNumber : " + SerialNumber + "\n" +
                        "HardwareId : " + HardwareId);
//                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Confirm",
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkUser = true;
                                finishView();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                new getAuth().execute(tinyDB.getString("deviceURI"));

            }else{
                checkUser = false;
                Toast.makeText(ONVIF_GetDevice.this, "Network failed or wrong username or password", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class getAuth extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            GetService getService = new GetService(url);
            return getService.getSupportHttpDigest();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.v("supportHttp", s);
            if(s.equals("true")){
                supportHttpDigest = true;
                tinyDB.putString("supportHttpDigest", "true");
            }else{
                supportHttpDigest = false;
                tinyDB.putString("supportHttpDigest", "false");
            }
        }
    }


}
