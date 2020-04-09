package com.java;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;

public class JsoupHelper {

    private static String token;//傳送請求需要的
    private static HashMap<String, String> cookies;//網站cookie

    public interface state{
        public void stateChange(String state);
    }

    public static Document sendRequest(String Url, String year, String startD, String endD, String immigration) {
        System.out.println("寫入資料");
        HashMap<String, String> formData = new HashMap<>();//要傳出去的
        //寫入Form Data (用F12,Network得到)
        formData.put("__VIEWSTATE", token);
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$period", "yw");//資料期間:年週
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$date_type", "3");//日期種類:發病日
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$y_s", year);
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$w_s", startD);
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$y_e", year);
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$w_e", endD);
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$immigration", immigration);
        formData.put("ctl00$NIDSSContentPlace$NIDSS_query1$btnSend", "查詢");

        System.out.println("傳送請求");
        Document pageDoc = null;
        try {
            Connection.Response page = Jsoup.connect(Url)//傳送請求
                    .cookies(cookies)
                    .data(formData)
                    .method(Connection.Method.POST)
                    .userAgent("Mozilla")
                    .execute();

            pageDoc = page.parse();//轉成HTML

        } catch (IOException e) {
            e.printStackTrace();
        }
        token = pageDoc.select("#__VIEWSTATE")//更新token
                .first()
                .attr("value");

        System.out.println("完成查詢!");
        return pageDoc;
    }

    public static void setupWebSite(String Url) throws IOException {
        System.out.println("連線到網站: " + Url);
        Connection.Response homeForm = Jsoup.connect(Url).method(Connection.Method.GET).userAgent("Mozilla").execute();//連接網站
        Document homeDoc = homeForm.parse(); //這是html文件
        System.out.println("完成");

        //用CSS selector得到#__VIEWSTATE(token)的值
        token = homeDoc.select("#__VIEWSTATE")
                .first()
                .attr("value");
        cookies = new HashMap<>(homeForm.cookies());//儲存cookies
    }
}
