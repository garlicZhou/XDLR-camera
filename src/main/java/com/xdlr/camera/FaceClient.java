package com.xdlr.camera;

import com.xdlr.camera.face.FaceLoginInfo;
import com.xdlr.camera.face.FaceManager;
import com.xdlr.camera.face.NVSSDK;
import com.xdlr.camera.trashcan.Trashcan;
import com.xdlr.camera.userAction.UserActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceClient {
    private static final Logger logger = LoggerFactory.getLogger(FaceClient.class);
    private FaceManager faceManager;
    private UserActionManager userActionManager;

    public static void main(String args[]) {
        logger.debug("Start camera-client");
        FaceClient client = new FaceClient();
        try {
            logger.debug("Init camera-client");

            client.init();
            while (true) {
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 程序退出
            client.exit();
        }
    }

    private void init() {
        // 初始化积分工具
        logger.debug("Init userActionManager");
        userActionManager = new UserActionManager();

        // 初始化抓拍机
        faceManager = new FaceManager(FaceLoginInfo.NVR_LOGIN_INFO);
        // 初始化SDK
        logger.debug("Init SDK");
        faceManager.SDKInit();
        // 登录设备
        logger.debug("Login Device");
        faceManager.LogonDevice();
        if (faceManager.getLogonID() < 0) {
            logger.info("LoginID < 0, log back");
        }

        // 开启智能分析
        logger.debug("开启智能分析");
        resumeVca(faceManager);

        // 开启图片流
        logger.debug("开启图片流");
        logger.debug("创建抓拍目录");
        faceManager.createPicDir(); //创建抓拍目录
        logger.debug("连接图片流通道");
        startSnap(faceManager);
        faceManager.FaceLibraryQuery();
        faceManager.registerSnapListener(
                new FaceManager.SnapNotifyListener() {
                    @Override
                    public void snapNotify(String faceDeviceId, int channelNo, boolean isStranger, String faceId, String negativePicturePath) {
                        logger.debug("通道" + channelNo + "抓拍回调通知，开始处理用户信息");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                logger.debug("处理用户积分");
                                handleUserAction(faceDeviceId, channelNo, isStranger, faceId, negativePicturePath);
                            }
                        }).start();
                    }

                    @Override
                    public void vcaSuspendSucceed(FaceManager faceManager, int channelNo) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                logger.debug("添加人脸底图");
                                faceManager.FacePictureAdd(lastestNegativePicture.faceId, lastestNegativePicture.path, channelNo);
                                logger.debug("恢复智能分析");
                                // 恢复智能分析
                                faceManager.SetVcaStatue(NVSSDK.VCA_SUSPEND_STATUS_RESUME);
                            }
                        }).start();
                    }
                }
        );
    }

    void resumeVca(FaceManager faceManager) {
        int count = faceManager.getFaceLoginInfo().faceDeviceChannelCount;
        for (int i = 0; i < count; i++) {
            setVcaStatus(faceManager, i, NVSSDK.VCA_SUSPEND_STATUS_RESUME);
        }
    }

    void setVcaStatus(FaceManager faceManager, int channelNo, int status) {
        faceManager.SetVcaStatue(channelNo, status);
    }

    void startSnap(FaceManager faceManager) {
        int count = faceManager.getFaceLoginInfo().faceDeviceChannelCount;
        for (int i = 0; i < count; i++) {
            faceManager.StartSnap(i);
        }
    }

    private NegativePicture lastestNegativePicture;

    // 底图
    static class NegativePicture {
        String faceId;
        String path;

        public NegativePicture(String faceId, String path) {
            this.faceId = faceId;
            this.path = path;
        }
    }

    void handleUserAction(String faceDeviceId, int channelNo, boolean isStranger, String faceId, String negativePicturePath) {
        logger.info("handleUserAction isStranger: " + isStranger + " faceDeviceId: " + faceDeviceId + " faceId: " + faceId);
        if (isStranger) {
            logger.info("请等待智能分析暂停结果");
            lastestNegativePicture = new NegativePicture(faceId, negativePicturePath);
            faceManager.SetVcaStatue(NVSSDK.VCA_SUSPEND_STATUS_PAUSE);
            logger.info("抓拍到新人");
            userActionManager.register(faceId, negativePicturePath);
        } else {
            if (channelNo == 2) {
                // 售货机
                logger.info("出货");
                userActionManager.renderGoods(faceId, negativePicturePath);
            } else if (channelNo == 0) {
                logger.info("文明行为");
                userActionManager.openCan(faceId, negativePicturePath);
//                Trashcan trashcan = new Trashcan();
//                int state = trashcan.getCanState();
//                if (state == Trashcan.TRASHCAN_STATE_OFF) {
//                }
            } else if (channelNo == 1) {
                userActionManager.updateTime(faceId, negativePicturePath);
                logger.info("主抓拍机");
            }
        }
    }

    private void exit() {
        if (faceManager != null) {
            faceManager.Exit();
        }
    }
}