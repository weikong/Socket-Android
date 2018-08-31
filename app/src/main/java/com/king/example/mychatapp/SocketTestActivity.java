package com.king.example.mychatapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketTestActivity extends AppCompatActivity {

    //定义相关变量,完成初始化
    private TextView tv_chat;
    private TextView txtshow;
    private EditText editsend;
    private Button btnsend;

    private LinearLayout layout_login;
    private EditText et_user, et_psd;
    private TextView tv_login;

    private String userName = "kong";

    private static final String HOST = "172.16.0.173";
    private static final int PORT = 6677;
    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";
    private StringBuilder sb = null;

    private boolean isInit = false;

    //定义一个handler对象,用来刷新界面
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                String strServer = (String) msg.obj;
                Log.e("handler", "客户端接收到:" + strServer);
                Toast.makeText(SocketTestActivity.this, "客户端接收到:" + strServer, Toast.LENGTH_SHORT).show();
            } else if (msg.what == 3) {
                isInit = true;
                getMessage();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        sb = new StringBuilder();
        tv_chat = (TextView) findViewById(R.id.tv_chat);
        txtshow = (TextView) findViewById(R.id.txtshow);
        editsend = (EditText) findViewById(R.id.editsend);
        btnsend = (Button) findViewById(R.id.btnsend);

        layout_login = (LinearLayout) findViewById(R.id.layout_login);
        et_user = (EditText) findViewById(R.id.et_user);
        et_psd = (EditText) findViewById(R.id.et_psd);
        tv_login = (TextView) findViewById(R.id.tv_login);
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = et_user.getText().toString();
                if (TextUtils.isEmpty(user)) {
                    Toast.makeText(SocketTestActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                userName = user;
                layout_login.setVisibility(View.GONE);
            }
        });
        //当程序一开始运行的时候就实例化Socket对象,与服务端进行连接,获取输入输出流
        //因为4.0以后不能再主线程中进行网络操作,所以需要另外开辟一个线程
        new Thread() {
            public void run() {
                try {
                    socket = new Socket(HOST, PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        //为发送按钮设置点击事件
        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editsend.getText().toString();
                sendSocketMsg(msg);
            }
        });
        handler.sendEmptyMessageDelayed(3, 5000);
    }

    private void sendSocketMsg(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("客户端启动...");
                try {
                    if (socket.isConnected()) {
                        if (!socket.isOutputShutdown()) {
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            String bindMsg = "kong:" + msg;
                            out.writeUTF(bindMsg);//客户端发送到服务端
                            try {
                                DataInputStream in = new DataInputStream(socket.getInputStream());
                                String strServer = in.readUTF();
                                Message msg = Message.obtain();
                                msg.what = 2;
                                msg.obj = strServer;
                                handler.sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("handler", "客户端异常:");
                            }
//                            out.close();
//                            in.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("客户端异常");
                    Log.e("handler", "客户端异常:");
                }
//                finally {
//                    if (socket != null) {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                        }
//                    }
//                }
            }
        }).start();
    }

    private void getMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("客户端启动...");
                try {
                    try {
                        while (isInit) {
                            if (socket != null && socket.isConnected()) {
                                if (!socket.isInputShutdown()) {
                                    Thread.sleep(5000);
                                    Log.e("handler", "读取服务器数据:");
                                    DataInputStream in = new DataInputStream(socket.getInputStream());
                                    String strServer = in.readUTF();
                                    Message msg = Message.obtain();
                                    msg.what = 2;
                                    msg.obj = strServer;
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("handler", "客户端异常:");
                    }
                } catch (Exception e) {
                    System.out.println("客户端异常");
                    Log.e("handler", "客户端异常:");
                }
            }
        }).start();
    }
}
