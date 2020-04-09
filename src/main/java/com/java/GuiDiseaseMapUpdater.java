package com.java;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static com.java.GoogleSheetHelper.*;
import static com.java.JsoupHelper.*;

public class GuiDiseaseMapUpdater {
    //Gui
    static JFrame frame = new JFrame("DiseaseMap資料更新器");//視窗主體
    static Button btSearch = new Button("查詢");
    static Button btUpdate = new Button("更新到Google Sheet");
    static TextArea ctName = new TextArea();//文字輸入
    static TextArea ctValue = new TextArea();//文字輸入
    static Label state = new Label();//狀態
    static Choice startW = new Choice();//下拉選單
    static Choice endW = new Choice();
    static Choice immigration = new Choice();
    static GridBagConstraints setting = new GridBagConstraints();//GridBag設定
    //網站
    private static String Url = "https://nidss.cdc.gov.tw/ch/NIDSS_DiseaseMap.aspx?disease=19cov";
    private static Document pageDoc;//查詢完畢的HTML
    private static String token;//傳送請求需要的
    private static HashMap<String, String> cookies;//網站cookie
    //googleSheet
    private static String SPREADSHEET_ID = "1haTOjjWN8vg-6W5CWFn8f3nTEBH581CS1zIk4KfJ9QE";

    public static void main(String[] args) throws IOException {
        BuildGui();
        setupWebSite(Url);
    }

    private static void printData() {
        Elements allData = pageDoc.select("span#ctl00_NIDSSContentPlace_Table tbody");//資料範圍
        Elements cityData = allData.select("tr:gt(0):lt(23)");//篩選資料(只剩下城市名+病例數)
        //print查詢的年跟週
        state.setText(pageDoc.select("#ctl00_NIDSSContentPlace_NIDSS_query1_y_e [selected=selected]").text()//結束年
                + "|" + pageDoc.select("#ctl00_NIDSSContentPlace_NIDSS_query1_w_s [selected=selected]").text()//開始週
                + "~" + pageDoc.select("#ctl00_NIDSSContentPlace_NIDSS_query1_w_e [selected=selected]").text());//結束週

        //print出來
        String[] total = allData.select("tr:eq(23)").text().split(" ");//總確診人數

        if (ctName.getText().length() < 1) {
            String th = cityData.select("th").text();
            ctName.setText(th.replace(" ", "\n").concat("\n" + total[0]));
            ctName.setFont(ctName.getFont().deriveFont(15f));
        }

        String td = cityData.select("td").text();
        ctValue.setText(td.replace(" ", "\n").concat("\n" + total[1]));
        ctValue.setFont(ctValue.getFont().deriveFont(15f));
    }

    private static Integer getWeek(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("w");
        return Integer.valueOf(format.format(date));
    }

    private static void BuildGui() {
        frame.setSize(400, 500);//設置長寬
        frame.setLocation(0, 0);
        frame.setLayout(new GridBagLayout());//設定排序方式

        for (int i = 1; i < 54; i++) {
            String item = i < 10 ? "0".concat(String.valueOf(i)) : String.valueOf(i);
            startW.add(item);//增加值
            endW.add(item);
        }
        endW.select(getWeek(new Date()) - 1);//今天第幾週

        immigration.add("本土病例及境外移入病例");
        immigration.add("本土病例");
        immigration.add("境外移入病例");

        Label sw = new Label("start week");//文字
        Label ew = new Label("end week");

        ctName.setEditable(false);//只能讀取
        ctValue.setEditable(false);//只能讀取

        Clicklistener click = new Clicklistener();
        btSearch.addActionListener(click);//button 按下的事件
        btUpdate.addActionListener(click);

        //增加元件到視窗中
        frame.add(state, setLoc(0, 0, 1, 1));
        frame.add(ctName, setLoc(0, 1, 1, 1, 1, 1));
        frame.add(ctValue, setLoc(1, 1, 1, 1, 1, 1));
        frame.add(sw, setLoc(0, 2, 1, 1));
        frame.add(startW, setLoc(0, 3, 1, 1));
        frame.add(ew, setLoc(1, 2, 1, 1));
        frame.add(endW, setLoc(1, 3, 1, 1));
        frame.add(immigration, setLoc(0, 4, 2, 1));
        frame.add(btUpdate, setLoc(0, 5, 2, 1));
        frame.add(btSearch, setLoc(0, 6, 2, 1));

        frame.setVisible(true);//是否要顯示視窗
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//關閉時
    }

    private static GridBagConstraints setLoc(int... a) {
        setting.gridx = a[0];//位置X
        setting.gridy = a[1];//位置y
        setting.gridwidth = a[2];//寬
        setting.gridheight = a[3];//長

        if (a.length > 4) {
            setting.weightx = a[4];//行權值
            setting.weighty = a[5];//列權值
        } else {
            setting.weightx = 0;
            setting.weighty = 0;
        }
        setting.fill = GridBagConstraints.BOTH;//談滿方式
        setting.anchor = GridBagConstraints.NORTH;//定位點
        return setting;
    }

    private static class Clicklistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String year = "2020";

            //input第幾週
            String startD = startW.getSelectedItem();
            String endD = endW.getSelectedItem();
            String type = String.valueOf(immigration.getSelectedIndex() - 1).replace("-1", "");

            pageDoc = sendRequest(Url, year, startD, endD, type);//抓資料
            ctValue.setText("");
            printData();

            if (e.getSource() == btUpdate) {
                String getText = ctValue.getText();//顯示在text視窗的字
                List<String> ValueList = Arrays.asList(getText.split("\n"));//拆成list

                List<String> date = new ArrayList<>();//處存所有日期
                for (List<Object> i : getData(SPREADSHEET_ID, immigration.getSelectedItem() + "(總)" + "!A2:A")) {
                    date.add(i.get(0).toString().replace("01~", ""));
                }

                SimpleDateFormat format = new SimpleDateFormat("MM/dd");
                String location = String.valueOf(date.indexOf(format.format(new Date())) + 1);//今天的前一天在第幾行

                writeData(SPREADSHEET_ID, immigration.getSelectedItem() + "(總)" + "!B" + location, ValueList);
            }
        }
    }
}
