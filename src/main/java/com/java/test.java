package com.java;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class test {
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++)
            new thread().start();
    }
}

class thread extends Thread {

    static String Url = "https://docs.google.com/forms/u/0/d/e/1FAIpQLSdWwSERvf8rCeEw_BYqIyOIKc2QnJkeouilnhZqyxBqtAhnEg/formResponse";

    @Override
    public void run() {
        HashMap<String, String> formData = new HashMap<>();//要傳出去的

        for (int i = 0; i < 1000; i++) {
            formData.clear();
            String[] str = {"男", "女"};
            StringBuilder name = new StringBuilder();
            for (int j = 0; j < 10; j++) {
                int ranInt = new Random().nextInt(25) + 97;
                name.append((char) ranInt);
            }
            formData.put("entry.1091353327", name.toString());//名字
            formData.put("entry.939339160", str[new Random().nextInt(2)]);//性別
            formData.put("entry.938093488", "選項 1");//年齡
            formData.put("entry.1494318126", "選項 1");
            try {
                Connection.Response page = Jsoup.connect(Url)//傳送請求
                        .data(formData)
                        .method(Connection.Method.POST)
                        .userAgent("Mozilla")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(new Random().nextInt(1000) + 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }
}
