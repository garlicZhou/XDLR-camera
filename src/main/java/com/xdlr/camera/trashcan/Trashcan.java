package com.xdlr.camera.trashcan;

import com.xdlr.camera.userAction.UserActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Trashcan {
    private static Logger logger = LoggerFactory.getLogger(Trashcan.class);
    /**
     * 发送目标IP地址
     */
    private static final String SEND_IP = "192.168.1.48";

    /**
     * 发送目标端口
     */
    private static final int SEND_PORT = 11037;
    /**
     * 接收消息端口
     */
    private static final int RECEIVE_PORT = 14396;

    /**
     * 发送信号标志
     */
    public static final String OPEN_DATA = "11101000";
    public static final String CLOSE_DATA = "00001111";

    /**
     * 接收成功标志
     */
    public static final String OPEN_SUCCESS = "01011100\0";
    public static final String CLOSE_SUCCESS = "10101101\0";

    public static final String TRASHCAN_STATE_QUERY = "000000011";
    public static final int TRASHCAN_STATE_ON = 0;
    public static final int TRASHCAN_STATE_OFF = 1;
    public static final int TRASHCAN_STATE_UNKNOW = -1;


    /**
     * 接收数据的Socket
     */
    private DatagramSocket receiveSocket;

    /**
     * 连接Socket,用于发送与接收消息
     *
     * @param inetAddress 目标的IP地址
     * @param signal      发送的信号标志
     * @param send        发送远程目标端口
     * @param receive     指定本机接收端口
     * @return reply      返回服务器发送回来的消息
     * @throws IOException
     */
    private String connectSocket(String inetAddress, String signal, int send, int receive) {
        try {
            /**
             * 创建数据报，用于向服务器发送消息
             */
            //定义服务器的地址、端口号、数据
            InetAddress address = InetAddress.getByName(inetAddress);
            byte[] data = signal.getBytes();

            //创建数据报，包含发送的数据信息
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, send);

            //创建DatagramSocket对象
            DatagramSocket sendSocket = new DatagramSocket();

            //向服务器端发送数据报
            sendSocket.send(sendPacket);
            /**
             * 创建数据报，用于接收服务器端响应的数据
             */
            //设置接收数据的端口号
            receiveSocket = new DatagramSocket(receive);
            byte[] data2 = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(data2, data2.length);

            //接收服务器响应的数据
            receiveSocket.receive(receivePacket);

            //读取数据
            String reply = new String(data2, 0, receivePacket.getLength());
            logger.info("服务器返回消息:" + reply);

            return reply;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭Socket资源
     *
     * @throws IOException
     */
    private void closeSocket() {
        receiveSocket.close();
    }

    /**
     * 打开垃圾桶
     *
     * @return true:垃圾桶打开成功;false:垃圾桶打开失败
     * @throws IOException
     */
    public boolean openCan() {
        //获取接收到的信息
        String reply = connectSocket(SEND_IP, OPEN_DATA, SEND_PORT, RECEIVE_PORT);
        if (reply != null && reply.equals(OPEN_SUCCESS)) {
            logger.info("Trash can opens successfully");
            closeSocket();
            return true;
        } else {
            logger.info("Trash can opens unsuccessfully");
            closeSocket();
            return false;
        }
    }

    /**
     * 关闭垃圾桶
     *
     * @return true:垃圾桶关闭成功;false:垃圾桶关闭失败
     * @throws IOException
     */
    public boolean closeCan() {
        //获取接收到的消息
        String reply = connectSocket(SEND_IP, CLOSE_DATA, SEND_PORT, RECEIVE_PORT);

        if (reply != null && reply.equals(CLOSE_SUCCESS)) {
            logger.info("Trash can closes successfully");
            closeSocket();
            return true;
        } else {
            logger.info("Trash can closes unsuccessfully");
            closeSocket();
            return false;
        }
    }

    public int getCanState() {
        //查询状态
        String reply = connectSocket(SEND_IP, TRASHCAN_STATE_QUERY, SEND_PORT, RECEIVE_PORT);

        if (reply == null) {
            return -1;
        }

        int state = TRASHCAN_STATE_UNKNOW;
        if (reply.equals("on\0")) {
            logger.info("Trash can is now on");
            state = TRASHCAN_STATE_ON;
        } else if (reply.equals("off\0")) {
            logger.info("Trash can is now off");
            state = TRASHCAN_STATE_OFF;
        }
        closeSocket();
        return state;
    }
}