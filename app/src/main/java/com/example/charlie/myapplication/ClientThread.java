package com.example.charlie.myapplication;

import android.os.Bundle;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by charlie on 2016/3/16.
 */
public class ClientThread implements Runnable {
    private Handler handler;
    private DataInputStream br = null;
    private final byte DANGER = 0x0C;
    private final byte MODE = 0x10;
    private final byte PLAYLIST = 0x20;
    private final byte TERMINATE = 0x1F;
    private final byte MUSICSTATUS = 0x3F;
    private final int wDANGER = 0;
    private final int wMODE = 1;
    private final int wPLAYLIST = 2;
    private final int wMUSICSTATUS = 3;
    private static ArrayList playlist;
    private boolean newList = true;

    public ClientThread(Socket socket, Handler handler) throws IOException {
        this.handler = handler;
        br = new DataInputStream(socket.getInputStream());

        playlist = new ArrayList<String>();
    }

    @Override
    public void run() {
        try {
            int temp;
            byte[] header = new byte[3];
            byte type;


            // 不断读取Socket输入流的内容
            //while((content = br.readLine())!=null){

            while(true){
                // 每当读到来自服务器的数据之后，发送消息通知程序界面显示该数据

                temp = br.read(header);

                Log.d("CTTEMP", ""+temp);

                if (temp != -1) {

                    type = (byte) (header[0]&0x3F);

                    if(type == DANGER){
                        int length = (int)getLength(header);

                        byte[] content = new byte[length];
                        temp = br.read(content);

                        Message msg = new Message();
                        msg.what = wDANGER;
                        msg.obj = content[0];                                                                                                                                                ;
                        handler.sendMessage(msg);
                    } else if(type == MODE){
                        Log.d("MODEMODE", "CHANGE");
                        int length = (int)getLength(header);

                        byte[] content = new byte[length];
                        temp = br.read(content);

                        Message msg = new Message();
                        msg.what = wMODE;
                        msg.obj = content[0];
                        handler.sendMessage(msg);
                    } else if(type == PLAYLIST){

                        if(newList == false){
                            playlist = new ArrayList();
                            newList = true;
                        }

                        int length = (int)getLength(header);

                        byte[] content = new byte[length];

                        temp = br.read(content);
                        String songName = new String(content);

                        playlist.add(songName);

                    } else if(type == TERMINATE){
                        Message msg = new Message();
                        msg.what = wPLAYLIST;
                        Bundle data = new Bundle();
                        data.putStringArrayList("playlist", playlist);
                        msg.setData(data);
                        handler.sendMessage(msg);
                        newList = false;
                    } else if(type == MUSICSTATUS){

                        int length = (int)getLength(header);

                        byte[] content = new byte[length];
                        temp = br.read(content);

                        Message msg = new Message();
                        msg.what = wMUSICSTATUS;
                        msg.obj = content[0];
                        handler.sendMessage(msg);
                    }

                }

                temp = temp -1;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Byte getLength(byte[] header){
        byte length;
        length = header[2];

        return length;
    }
}
