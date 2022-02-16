package com.silas;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.silas.utils.HttpKit;
import com.silas.utils.MailKit;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class App
{
    public static void main( String[] args )  throws Exception {
        procBond();
    }

    public static void procBond()
    {
        System.out.println("get my debit list");
        // System.out.println(App.class.getResource("mybond.txt").getPath());
        List<String> myBondList = readBondList("./mybond.txt");

        if (myBondList == null) {
            System.out.println("read myBondList fail");
            return;
        }

        List<String> sendBondList = readBondList("./send.txt");

        if (sendBondList == null) {
            System.out.println("read sendBondList fail");
            return;
        }


        System.out.println("get redeem bond from web");
        String respData = GetRedeem();
        if (respData == "") {
            System.out.println("GetRedeem fail");
            return;
        }
        //    System.out.println(respData);
        System.out.println("parse json result");
        JSONObject jsonObject = JSON.parseObject(respData);
        if (jsonObject == null) {
            System.out.println("parseObject fail");
            return;
        }
        System.out.println("jsonArray foreach");
        jsonObject.getJSONArray("rows").forEach(item -> {
            if (item instanceof JSONObject) {
                String imei = ((JSONObject) item).getString("id");
                //    System.out.println(imei);
                JSONObject cell = ((JSONObject) item).getJSONObject("cell");
                String bond_id = cell.getString("bond_id");
                String bond_nm = cell.getString("bond_nm");
                String redeem_flag = cell.getString("redeem_flag");
                if (redeem_flag.equals("Y")) {
                    System.out.println("redeem_flag Y-" + bond_id);
                    if (containBond(myBondList, bond_id)) {
                        System.out.println("match-" + bond_nm);
                        if (!containBond(sendBondList, bond_id))
                        {
                            System.out.println("not send");
                            try {
                                MailKit.sendEmail("13968613199@163.com", "title", "redeem bond name-" + bond_nm);
                                writeBondList("./send.txt", String.format("%s,%s", bond_id, bond_nm));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        });
    }
    public static boolean containBond(List<String> bondList,String bond)
    {
        for (String item : bondList) {
            if (item.contains(bond)) {
                return true;
            }
        }
        return false;
    }


    public static String GetRedeem()
    {
        String url="https://www.jisilu.cn/data/cbnew/redeem_list/?___jsl=LST___t=1636805518474";
        String res = HttpKit.post(url,"");
        return res;
    }



    public static List<String> readBondList(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String> bondList=new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                bondList.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
            return bondList;
        }
    }

    public static void writeBondList(String fileName,String bondInfo) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(bondInfo);
            bw.newLine();
            bw.flush(); //将数据更新至文件   bw.close();   fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}


