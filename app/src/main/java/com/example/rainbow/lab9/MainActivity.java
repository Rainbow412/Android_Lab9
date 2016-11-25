package com.example.rainbow.lab9;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private static final String url = "http://ws.webxml.com.cn/WebServices/WeatherWS.asmx/getWeather";
    private static final String UserId = "b76d4ab81ea04194ac5bd3ebd3df3df3";
    private static final int UPDATE_CONTENT = 0;
    private EditText city_edit;
    private Button search_btn;
    private RelativeLayout result_title, result_main;
    private ListView result_list;

    private TextView city_text, time_text;
    private TextView tem_text, tem_range_text, hum_text, air_text, wind_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        result_title.setVisibility(INVISIBLE);
        result_main.setVisibility(INVISIBLE);
        result_list.setVisibility(INVISIBLE);


        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connMgr = (ConnectivityManager)getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if(networkInfo != null&& networkInfo.isConnected()) //表示网络已连接
                {
                    sendRequestWithHttpUrlConnection();
                }
                else{
                    Log.d("key", "no connection");
                    Toast.makeText(MainActivity.this, "当前没有可用网络！",
                            Toast.LENGTH_LONG).show();
                }
            }
        });





    }
    private void findView(){
        city_edit = (EditText)findViewById(R.id.city_edit);
        search_btn = (Button)findViewById(R.id.search_btn);
        result_title = (RelativeLayout)findViewById(R.id.result_title);
        result_main = (RelativeLayout)findViewById(R.id.result_rectangle);
        result_list = (ListView)findViewById(R.id.result_list);

        city_text = (TextView)findViewById(R.id.city_text);
        time_text = (TextView)findViewById(R.id.time_text);

        tem_text = (TextView)findViewById(R.id.temperature_text);
        tem_range_text = (TextView)findViewById(R.id.temperature_range_text);
        hum_text = (TextView)findViewById(R.id.humidity_text);
        air_text = (TextView)findViewById(R.id.air_text);
        wind_text = (TextView)findViewById(R.id.wind_text);
    }

    private void sendRequestWithHttpUrlConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //http请求操作
                HttpURLConnection connection = null;
                try{
                    Log.d("key", "begin connect");
                    connection = (HttpURLConnection)((new URL(url.toString()).openConnection()));
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);

                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    String city_request = city_edit.getText().toString();
                    city_request = URLEncoder.encode(city_request, "utf-8");
                    Log.d("key request", "theCityCode="+city_request+"&theUserID="+UserId);
                    out.writeBytes("theCityCode="+city_request+"&theUserID="+UserId);

                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line=reader.readLine())!=null){
                        response.append(line);
                    }
                    Log.d("key response", response.toString());


                    Message msg = new Message();
                    msg.what = UPDATE_CONTENT;
                    msg.obj = parseXMLWithPull(response.toString());
                    handler.sendMessage(msg);


                }catch (Exception e)
                {
                    e.printStackTrace();
                }finally{
                    if(connection != null)
                        connection.disconnect();
                }


            }
        }).start();
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case UPDATE_CONTENT:
                    //对由子线程得到的字符串进行处理
                    //界面更新
                    ArrayList<String> list = new ArrayList<>();
                    list = (ArrayList<String>)message.obj;
                    if(list.size()==1){
                        if(list.get(0).toString().equals("查询结果为空"))
                            Toast.makeText(MainActivity.this, "当前城市不存在，请重新输入",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(MainActivity.this, list.get(0),
                                    Toast.LENGTH_LONG).show();
                    }
                    else{
                        result_title.setVisibility(VISIBLE);
                        result_main.setVisibility(VISIBLE);
                        result_list.setVisibility(VISIBLE);

                        city_text.setText(list.get(1));
                        int p0 = list.get(3).indexOf(" ");
                        String time = list.get(3).substring(p0+1);
                        time_text.setText(time+" 更新");

                        int p1 = list.get(4).indexOf("：",7);
                        int p2 = list.get(4).indexOf("；",p1);
                        String tem = list.get(4).substring(p1+1, p2);
                        tem_text.setText(tem);

                        int p3 = list.get(4).indexOf("：",p2);
                        int p4 = list.get(4).indexOf("；",p3);
                        String wind = list.get(4).substring(p3+1, p4);
                        wind_text.setText(wind);

                        String hum = list.get(4).substring(p4+1);
                        hum_text.setText(hum);

                        int p5 = list.get(5).indexOf("。");
                        int p6 = list.get(5).indexOf("。", p5+1);
                        String air = list.get(5).substring(p5+1, p6);
                        air_text.setText(air);

                        tem_range_text.setText(list.get(8));

                        List<Map<String, Object>> data = new ArrayList<>();
                        for(int i = 0; i < 6; i++){
                            Map<String, Object> temp = new LinkedHashMap<>();
                            int pp1 = list.get(6).indexOf("：");
                            int pp2 = list.get(6).indexOf("。",pp1);
                            String t = list.get(6).substring(0,pp1);
                            String s = list.get(6).substring(pp1+1,pp2);
                            temp.put("title", t);
                            temp.put("text", s);
                            data.add(temp);
                            list.set(6, list.get(6).substring(pp2+1));
                        }

                        SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, data,
                                R.layout.list1item, new String[]{"title","text"},
                                new int[]{R.id.title, R.id.text});

                        result_list.setAdapter(simpleAdapter);

                    }


                    break;
                default:
                    break;
            }
        }
    };

    ArrayList<String> parseXMLWithPull(String xml){
        ArrayList<String> list = new ArrayList<>();

        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if("string".equals(parser.getName())){
                            String str = parser.nextText();
                            Log.d("key str", str);
                            list.add(str);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

//    public void myClickHandler(View view){
////        String stringUrl;//需要访问的URL
//        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if(networkInfo != null&& networkInfo.isConnected()) //表示网络已连接
//        {
//            new DownloadWebpageText().execute(url);
//            //创建AsyncTask实例并执行
//        } else{
////            textView.setText("No network connection available.");
//        }
//    }


}
