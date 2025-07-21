package com.ty.tybusbase.utils;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by cdy on 2016/2/3.
 * 快速生成适配工具类 ,直接运行不成功需手动放入目录文件中
 */
public class DimenTool {

    private static double defaultSw = 1080.0;

    @SuppressLint("DefaultLocale")
    public static void gen() {
        //以此文件夹下的dimens.xml文件内容为初始值参照
        /**
         * <?xml version="1.0" encoding="utf-8"?>
         * <resources>
         *     <dimen name="dp_0_5">0.25dp</dimen>
         *     <dimen name="sp_38">47.2889sp</dimen>
         */
        File file = new File("./app/src/main/res/values/dimens.xml");

        if (file.exists()) {
            file.delete();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
        sb.append("<resources>\r\n");
        sb.append("<dimen name=\"dp_0_5\">0.5dp</dimen>\r\n");
        //写dp
        for (int i = 1; i <= 1920; i++) {
            sb.append("<dimen name=\"dp_" + i + "\">" + i + "dp</dimen>\r\n");
        }

        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("\r\n");

        for (int i = 1; i <= 500; i++) {
            sb.append("<dimen name=\"sp_" + i + "\">" + i + "sp</dimen>\r\n");
        }

        sb.append("</resources>\r\n");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(sb.toString(), 0, sb.length());
            writer.flush();
            writer.close();
        } catch (Exception e) {

        }

        BufferedReader reader = null;
        StringBuilder sw240 = new StringBuilder();
        StringBuilder sw320 = new StringBuilder();
        StringBuilder sw360 = new StringBuilder();
        StringBuilder sw480 = new StringBuilder();
        StringBuilder sw540 = new StringBuilder();
        StringBuilder sw635 = new StringBuilder();
        StringBuilder sw720 = new StringBuilder();
        StringBuilder sw800 = new StringBuilder();
        StringBuilder sw1080 = new StringBuilder();
        StringBuilder sw1271 = new StringBuilder();

        try {

            System.out.println("生成不同分辨率：");

            reader = new BufferedReader(new FileReader(file));

            String tempString;

            int line = 1;

            // 一次读入一行，直到读入null为文件结束  

            while ((tempString = reader.readLine()) != null) {


                if (tempString.contains("</dimen>")) {

                    //tempString = tempString.replaceAll(" ", "");  

                    String start = tempString.substring(0, tempString.indexOf(">") + 1);

                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    //截取<dimen></dimen>标签内的内容，从>右括号开始，到左括号减2，取得配置的数字  
                    Double num = Double.parseDouble
                            (tempString.substring(tempString.indexOf(">") + 1,
                                    tempString.indexOf("</dimen>") - 2));

                    //根据不同的尺寸，计算新的值，拼接新的字符串，并且结尾处换行。  
                    sw240.append(start).append(String.format("%.2f", num * 240 / defaultSw)).append(end).append("\r\n");

                    sw320.append(start).append(String.format("%.2f", num * 320 / defaultSw)).append(end).append("\r\n");

                    sw360.append(start).append(String.format("%.2f", num * 360 / defaultSw)).append(end).append("\r\n");

                    sw480.append(start).append(String.format("%.2f", num * 480 / defaultSw)).append(end).append("\r\n");

                    sw540.append(start).append(String.format("%.2f", num * 540 / defaultSw)).append(end).append("\r\n");

                    sw635.append(start).append(String.format("%.2f", num * 635 / defaultSw)).append(end).append("\r\n");

                    sw720.append(start).append(String.format("%.2f", num * 720 / defaultSw)).append(end).append("\r\n");

                    sw800.append(start).append(String.format("%.2f", num * 800 / defaultSw)).append(end).append("\r\n");

                    sw1080.append(start).append(String.format("%.2f", num * 1080 / defaultSw)).append(end).append("\r\n");

                    sw1271.append(start).append(String.format("%.2f", num * 1271 / defaultSw)).append(end).append("\r\n");

                } else {
                    sw240.append(tempString).append("\r\n");

                    sw320.append(tempString).append("\r\n");

                    sw360.append(tempString).append("\r\n");

                    sw480.append(tempString).append("\r\n");

                    sw540.append(tempString).append("\r\n");

                    sw635.append(tempString).append("\r\n");

                    sw720.append(tempString).append("\r\n");

                    sw800.append(tempString).append("\r\n");

                    sw1080.append(tempString).append("\r\n");

                    sw1271.append(tempString).append("\r\n");

                }

                line++;

            }

            reader.close();
            System.out.println("<!--  sw240 -->");

            System.out.println(sw240);

            System.out.println("<!--  sw320 -->");

            System.out.println(sw320);

            System.out.println("<!--  sw360 -->");

            System.out.println(sw360);

            System.out.println("<!--  sw480 -->");

            System.out.println(sw480);

            System.out.println("<!--  sw635 -->");

            System.out.println(sw635);

            System.out.println("<!--  sw720 -->");

            System.out.println(sw720);

            System.out.println("<!--  sw800 -->");

            System.out.println(sw800);

            String sw240file = "./app/src/main/res/values-sw240dp";

            String sw320file = "./app/src/main/res/values-sw320dp";

            String sw360file = "./app/src/main/res/values-sw360dp";

            String sw480file = "./app/src/main/res/values-sw480dp";

            String sw540file = "./app/src/main/res/values-sw540dp";

            String sw635file = "./app/src/main/res/values-sw635dp";

            String sw720file = "./app/src/main/res/values-sw720dp";

            String sw800file = "./app/src/main/res/values-sw800dp";

            String sw1080file = "./app/src/main/res/values-sw1080dp";

            String sw1271file = "./app/src/main/res/values-sw1271dp";

            //将新的内容，写入到指定的文件中去
            writeFile(sw240file, sw240.toString());

            writeFile(sw320file, sw320.toString());

            writeFile(sw360file, sw360.toString());

            writeFile(sw480file, sw480.toString());

            writeFile(sw540file, sw540.toString());

            writeFile(sw635file, sw635.toString());

            writeFile(sw720file, sw720.toString());

            writeFile(sw800file, sw800.toString());

            writeFile(sw1080file, sw1080.toString());

            writeFile(sw1271file, sw1271.toString());


        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (reader != null) {

                try {

                    reader.close();

                } catch (IOException e1) {

                    e1.printStackTrace();

                }

            }

        }

    }


    /**
     * 写入方法
     */

    public static void writeFile(String path, String text) {

        PrintWriter out = null;

        try {
            File filePath = new File(path);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            File file = new File(path, "dimens.xml");
            if (file.exists()) {
                file.delete();
            }
            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            out.println(text);

        } catch (Exception e) {

            e.printStackTrace();

        }


        out.close();

    }

    public static void main(String[] args) {

        gen();

    }

}