package com.example.charlie.myapplication;

import android.os.Message;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by charlie on 2016/3/16.
 */
public class ClientThread implements Runnable {
    private Handler handler;
    private DataInputStream br = null;

    public ClientThread(Socket socket, Handler handler) throws IOException {
        this.handler = handler;
        br = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            int temp;
            byte[] header = new byte[3];

            // 不断读取Socket输入流的内容
            Log.d("client", "while");
            //while((content = br.readLine())!=null){
            while(true){
                // 每当读到来自服务器的数据之后，发送消息通知程序界面显示该数据
                temp = br.read(header);
                if (temp != -1) {
                    Log.d("client", ""+header);

                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = header                                                                                                                                                ;
                    handler.sendMessage(msg);
                    Log.d("client", "handlersend");
                }
            }
        } catch (Exception e) {
            Log.d("client", "printstack");
            e.printStackTrace();
        }
        Log.d("client", "endrun");
    }

    public Byte getLength(Byte[] header){

    }
}
