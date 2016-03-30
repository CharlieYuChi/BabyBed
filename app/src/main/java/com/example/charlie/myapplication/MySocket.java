package com.example.charlie.myapplication;

/**
 * Created by charlie on 2016/3/16.
 */
import java.net.*;
import java.io.*;

public class MySocket {
    private String ip = "192.168.0.121";
    private String port = "8080";
    private Socket socket;
    BufferedReader reader;
    PrintStream writer;

    MySocket(){
        try {
            socket = new Socket(ip, 8080);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(inputStreamReader);
            writer = new PrintStream(socket.getOutputStream());
            writer.println("我 進入聊天室");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
