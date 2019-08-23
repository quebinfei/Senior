package bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description: 多线程服务端处理，并进行返回
 * @author: qbf
 * @create: 2019-08-19 15:25
 **/
public class BIOServer2 {
    private static ExecutorService thread = Executors.newCachedThreadPool();

    public static void main (String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务端启动成功！");

        while (!serverSocket.isClosed()) {
            Socket request = serverSocket.accept();
            System.out.println("收到新的连接：" + request.toString());
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

                    //组织返回数据
                    OutputStream outputStream = request.getOutputStream();
                    outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                    outputStream.write("Content-Length: 11\r\n\r\n".getBytes());
                    outputStream.write("Hello World".getBytes());
                    outputStream.flush();

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
