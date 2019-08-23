package com.study.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @description: 获取百度响应的server信息
 * @author: qbf
 * @create: 2019-08-23 10:28
 **/
public class TEST1615675008 {
    public static void main (String[] args) {
        try {
            //获取客户端，建立连接
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("www.baidu.com", 80));//默认80端口
            while (!socketChannel.finishConnect()) {
                Thread.yield();
            }
            System.out.println("客户端已启动，准备发送请求数据!");
            //组织请求数据并进行发送
            String msg = "GET / HTTP/1.1\n" +
                    "Host: www.baidu.com\n" +
                    "\r\n\r\n";
            ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
            while (byteBuffer.hasRemaining()) {
                socketChannel.write(byteBuffer);
            }

            System.out.println("请求数据已发送，准备接收响应数据!");
            //接收响应的数据
            ByteBuffer respBuffer = ByteBuffer.allocate(1024);
            while (socketChannel.isOpen() && socketChannel.read(respBuffer) != -1) {
                if (respBuffer.position() > 0) {//大于0，说明已开始读取数据
                    break;
                }
            }
            System.out.println("开始读取响应数据!");
            respBuffer.flip();//读取数据
            byte[] resp = new byte[respBuffer.limit()];
            respBuffer.get(resp);
            String respStr = new String(resp);
            System.out.println("收到的响应数据为：" + respStr);

            //获取目标数据
            if (respStr != null && respStr.length() > 0) {
                String targetStr = "Server";//目标字符
                if (respStr.contains(targetStr)) {
                    respStr = respStr.substring(respStr.indexOf(targetStr));
                    respStr = respStr.substring(0, respStr.indexOf("\n") + 1);
                }
                System.out.println("我的qq号是1615675008，我解析到百度服务器server类型是：" + respStr);

            } else {
                System.out.println("响应数据为空，获取响应数据失败!");
            }

            //关闭资源
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
