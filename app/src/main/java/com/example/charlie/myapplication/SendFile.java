package com.example.charlie.myapplication;

/**
 * Created by Charlie on 2016/5/26.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class SendFile {




    public static void main(String[] args) {

    }



    public static void sendFile(final String extpath) throws IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //if (fileName == null) return; //增加文件流用來讀取文件中的資料
                //File file = new File("/storage/emulated/0/Download/", "My Songs Know What You Did In The Dark (Light Em Up) - from YouTube.mp3");
                File file = new File(extpath);
                System.out.println("文件長度:" + (int) file.length()); // public Socket accept() throws
                System.out.println("文件名稱:" + file.getName()); // public Socket accept() throws
                System.out.println("文件檔名長度:" + file.getName().getBytes().length); // public Socket accept() throws
                byte fileNameLen = (byte) (file.getName().getBytes().length);
                //System.out.println(fileNameLen);
                try {
                    FileInputStream fos = new FileInputStream(file); //增加網絡服務器接受客戶請求
                    //Socket server = new Socket("127.0.0.1", 8080);
                    //Socket server = new Socket("140.115.204.92", 8080);
                    //Socket server = new Socket("192.168.0.112",8080);
                    Socket server = new Socket("192.168.1.109", 8080);
                    //Socket server = new Socket("192.168.0.103",8080);
                    OutputStream netOut = server.getOutputStream();
                    //DataOutputStream dos = new DataOutputStream(netOut);
                    //PrintStream writer = new PrintStream(netOut);
                    OutputStream doc = new DataOutputStream(new BufferedOutputStream(netOut)); //增加文件讀取緩衝區
                    byte[] buf = new byte[2048];
                    int num = fos.read(buf);
                    byte[] header = new byte[]{0x48, 0x30, fileNameLen};
                    //dos.write(header);
                    doc.write(header);
                    doc.write(file.getName().getBytes());
                    //writer.write(header);
                    System.out.println("傳送文件中:" + (String) file.getName()); // public Socket accept() throws
                    while (num != -1) { //是否讀完文件
                        doc.write(buf, 0, num); //把文件資料寫出網絡緩衝區
                        //writer.write(buf, 0, num);
                        //dos.write(buf, 0, num);
                        num = fos.read(buf); //繼續從文件中讀取資料
                    }
                    System.out.println("結束了");
                    doc.flush(); //重整緩衝區把資料寫往客戶端
                    fos.close();
                    doc.close();
                    server.close();
                    System.out.println("傳送完成!"); // public Socket accept() throws
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                }
            }
        }).start();

    }

}
