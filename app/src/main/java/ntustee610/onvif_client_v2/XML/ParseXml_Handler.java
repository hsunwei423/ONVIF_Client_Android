package ntustee610.onvif_client_v2.XML;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MADAO on 2016/11/12.
 */

public class ParseXml_Handler extends DefaultHandler {
    private String TAG = "ParseXml_Handler";
    private List<HashMap<String, String>> list = null; //解析後的XML內容
    private HashMap<String, String> map = null;  //存放目前需要紀錄的節點XML
    private String currentTag = null;//目前讀取的XML節點
    private String currentValue = null;//目前節點的XML值
    private String nodeName = null;//須解析節點的名稱

    public ParseXml_Handler(String nodeName) {
        //設置需要解析的節點名稱
        this.nodeName = nodeName;
    }

    @Override
    public void startDocument() throws SAXException {
        // 接收element開始的通知
        // 實作ArrayList用來存放解析XML後的data
        list = new ArrayList<HashMap<String, String>>();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // 接收element開始的通知
//        Log.v(TAG, "startElement");
        int endIndex = qName.length();
        int startIndex = qName.indexOf(":")+1;
        String find_string = qName.substring(startIndex, endIndex);
//        Log.v(TAG, "element = "+find_string);
//        Log.v(TAG, "attr = " + attributes.getValue(0));
        if (find_string.equals(nodeName)) {
            //如果目前執行的節點名稱與設定的節點名稱相同，則實作HashMap
            map = new HashMap<String, String>();
        }
        //attributes為目前節點的屬性值，如果存在屬性，則屬性質也讀取
//        if (attributes != null && map != null) {
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                //讀取到的屬性值，加入map中
                String getvalue = attributes.getValue(i);
//                Log.v(TAG, "attributes = " + getvalue);
                map.put(attributes.getQName(i), attributes.getValue(i));
//                Log.v(TAG, "map = " + map);
            }
        }
        //紀錄目前節點名稱
//        currentTag = qName;
        currentTag = find_string;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
//        Log.v(TAG, "characters");
        //接收element中字符數據的通知
        //目前節點有值的情況下才繼續執行
        if (currentTag != null && map != null) {
            //取得目前節點的值，ch是存放的值
            currentValue = new String(ch, start, length);
            if (currentValue != null && !currentValue.equals("")
                    && !currentValue.equals("\n")) {
                //取得的值不能為null, "" 和 "\n"
//                Log.v(TAG, "currentTag = " + currentTag);
//                Log.v(TAG, "currentValue = " + currentValue);
                map.put(currentTag, currentValue);
            }
        }
        //讀取完成後需要清空當前節點的標籤值和包含的文本值
        currentTag = null;
        currentValue = null;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // 接收element结束的通知。
//        Log.v(TAG, "endElement");
        int endIndex = qName.length();
        int startIndex = qName.indexOf(":")+1;
        String find_string = qName.substring(startIndex, endIndex);
        if (find_string.equals(nodeName)) {
            //如果讀取的節點是需要的，把map放入list保存
            list.add(map);
            //清空map，開始新一輪的讀取
            map = null;
        }
    }

    public List<HashMap<String, String>> getList() {
        return list;
    }
}
