package ntustee610.onvif_client_v2.XML;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ntustee610.onvif_client_v2.XML.ParseXml_Handler;

/**
 * Created by MADAO on 2016/11/12.
 */

public class ParseXml_SaxService {
    public ParseXml_SaxService() {
        // TODO Auto-generated constructor stub
    }

    public static List<HashMap<String, String>> readXML(InputStream inputStream, String nodeName) {
        try {
            //實作SAX工具
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //實作SAX解析器
            SAXParser sParser = factory.newSAXParser();
            //實作DdefaltHandler，設定需要解析的節點
            ParseXml_Handler myHandler = new ParseXml_Handler(nodeName);
            // 開始解析
            sParser.parse(inputStream, myHandler);
            // 解析完成後關閉inputStream
            inputStream.close();
            //回傳解析結果
            return myHandler.getList();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
}
