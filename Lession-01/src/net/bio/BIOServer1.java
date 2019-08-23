package bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description: 多线程支持
 * @author: qbf
 * @create: 2019-08-19 15:02
 **/
public class BIOServer1 {
    private static ExecutorService thread = Executors.newCachedThreadPool();//创建线程池

    public static void main (String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);//创建socket服务端，并绑定端口
        System.out.println("服务启动成功！");
        while (!serverSocket.isClosed()) {
            Socket request = serverSocket.accept();//接收客户端请求
            System.out.println("收到新连接：" + request.toString());
            thread.execute(() -> {
                try {
                    InputStream inputStream = request.getInputStream();//获取输入流对象   net+io包
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String msg;
                    while ((msg = reader.readLine()) != null) {
                        if (msg.length() == 0) {
                            break;
                        }
                        System.out.println(msg);
                    }
                    System.out.println("接收请求结束：" + request.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        request.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        serverSocket.close();
    }
}
