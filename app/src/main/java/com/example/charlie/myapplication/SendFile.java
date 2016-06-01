package com.example.charlie.myapplication;

/**
 * Created by Charlie on 2016/5/26.
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SendFile {

    public static void main(String[] args) {
        sendFile("gnash.mp3");
    }

    public static void sendFile(String fileName){
        if (fileName == null) return; //增加文件流用來讀取文件中的資料
        File file = new File(fileName);
        System.out.println("文件長度:" + (int) file.length()); // public Socket accept() throws
        System.out.println("文件名稱:" + (String) file.getName()); // public Socket accept() throws
        System.out.println("文件檔名長度:" + file.getName().getBytes().length); // public Socket accept() throws
        byte fileNameLen = (byte)(file.getName().getBytes().length);
        //System.out.println(fileNameLen);
        try {
            FileInputStream fos = new FileInputStream(file); //增加網絡服務器接受客戶請求
            Socket server = new Socket("127.0.0.1", 8080);
            OutputStream netOut = server.getOutputStream();
            OutputStream doc = new DataOutputStream(new BufferedOutputStream(netOut)); //增加文件讀取緩衝區
            byte[] buf = new byte[2048];
            int num = fos.read(buf);
            byte[] header = new byte[]{0x48,0x30,fileNameLen};
            doc.write(header);
            doc.write(fileName.getBytes());
            System.out.println("傳送文件中:" + (String) file.getName()); // public Socket accept() throws
            while (num != ( - 1)) { //是否讀完文件
                doc.write(buf, 0, num); //把文件資料寫出網絡緩衝區
                num = fos.read(buf); //繼續從文件中讀取資料
            }
            doc.flush(); //重整緩衝區把資料寫往客戶端
            fos.close();
            doc.close();
            server.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {}
    }

}
