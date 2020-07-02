package com.example.tcpserverdemo;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    ServerSocket serverSocket;//创建ServerSocket对象
    Socket clicksSocket;//连接通道，创建Socket对象
    Button startButton;//发送按钮
    EditText portEditText;//端口号
    EditText receiveEditText;//接收消息框
    Button sendButton;//发送按钮
    EditText sendEditText;//发送消息框
    InputStream inputstream;//创建输入数据流
    OutputStream outputStream;//创建输出数据流


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 读一下手机wifi状态下的ip地址
         */
        Toast.makeText(MainActivity.this, getLocalIpAddress(), Toast.LENGTH_SHORT).show();
        init();

    }

    /**
     * 初始化
     */
    private void init() {
        startButton = findViewById(R.id.start_button);
        portEditText = findViewById(R.id.port_EditText);
        receiveEditText = findViewById(R.id.receive_EditText);
        sendButton = findViewById(R.id.send_button);
        sendEditText = findViewById(R.id.message_EditText);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 启动服务器监听线程
                 */
                ServerSocket_thread serversocket_thread = new ServerSocket_thread();
                serversocket_thread.start();
            }
        });
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 发送消息按钮事件
                 */
                Send_Thread send_thread = new Send_Thread();
                send_thread.start();
            }
        });
    }


    /**
     * 服务器监听线程
     */
    class ServerSocket_thread extends Thread {
        public void run()//重写Thread的run方法
        {
            try {
                int port = Integer.valueOf(portEditText.getText().toString());//获取portEditText中的端口号
                serverSocket = new ServerSocket(port);//监听port端口，这个程序的通信端口就是port了
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            while (true) {
                try {
                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                    clicksSocket = serverSocket.accept();
                    //连接超时
                    serverSocket.setSoTimeout(50000);
                    inputstream = clicksSocket.getInputStream();
                    //启动接收线程
                    Receive_Thread receive_Thread = new Receive_Thread();
                    receive_Thread.start();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 接收线程
     */
    class Receive_Thread extends Thread//继承Thread
    {
        public void run()//重写run方法
        {
            while (true) {
                try {
                    final byte[] buf = new byte[1024];
                    final int len = inputstream.read(buf);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //if判读防止字符串越位错误
                            if (buf != null && len > 0) {
                                receiveEditText.setText(new String(buf, 0, len));
                            }
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送线程
     */
    class Send_Thread extends Thread {
        public void run() {
            try {
                outputStream = clicksSocket.getOutputStream();
                outputStream.write(sendEditText.getText().toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取WIFI下ip地址
     */
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
}