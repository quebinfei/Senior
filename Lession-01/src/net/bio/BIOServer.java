package bio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {

    public static void main (String[] args) throws Exception {
        //创建socket服务端
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务启动成功！！！");

        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();//阻塞
            System.out.println("收到新的请求：" + socket.toString());

            InputStream inputStream = socket.getInputStream();//使用包 net+io
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String msg;
            while ((msg = reader.readLine()) != null) {
                if (msg.length() == 0) {
                    break;
                }
                System.out.println("msg:" + msg);
            }
//            System.out.println("收到的请求数据："+socket.toString());

            socket.close();
        }
        serverSocket.close();
    }
}
