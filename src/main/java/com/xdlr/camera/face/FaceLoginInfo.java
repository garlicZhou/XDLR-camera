package com.xdlr.camera.face;

public class FaceLoginInfo {
    public static int DEVICE_COUNT = 1;

    public static final String NVR_DEVICE_ID = "ID0000801941490740060503";
    public int faceDeviceChannelCount;
    public String faceDeviceId;
    public String ip;
    public int port;
    public String userName;
    public String password;

    // 进入抓拍机
    public static final FaceLoginInfo NVR_LOGIN_INFO;

    static {
        NVR_LOGIN_INFO = new FaceLoginInfo(3, NVR_DEVICE_ID, "192.168.1.200", 3000, "admin", "123456xd");
    }

    public FaceLoginInfo(int faceDeviceChannelCount, String faceDeviceId, String ip, int port, String userName, String password) {
        this.faceDeviceChannelCount = faceDeviceChannelCount;
        this.faceDeviceId = faceDeviceId;
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.password = password;

    }
}
