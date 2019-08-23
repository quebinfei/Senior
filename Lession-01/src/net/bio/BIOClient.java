package bio;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BIOClient {
    public static void main (String[] args) {

        //创建socket对象，绑定IP和端口
        Socket socket = null;
        try {
            socket = new Socket("localhost", 8080);

            OutputStream outputStream = socket.getOutputStream();

            //获取输入流并发送输入的信息
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入：");
            String msg = scanner.nextLine();
            outputStream.write(msg.getBytes("UTF-8"));

            //资源关闭
            scanner.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
