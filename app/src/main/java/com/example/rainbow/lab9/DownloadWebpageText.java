//package com.example.rainbow.lab9;
//
//
//import android.os.AsyncTask;
//
//import java.io.IOException;
//
//private class DownloadWebpageText extends AsyncTask {
//    @Override
//    protected String doInBackground(String... urls){
//        try{
//            return downloadUrl(urls[0]); //连接并下载数据
//        } catch(IOException e) {
//            return"Unable to retrieve web page.";
//        }
//        }
//    @Override
//    protected voido nPostExecute(String result){
//        textView.setText(result); //将结果字符串显示在UI界面
//    }
//}