package com.xdlr.camera.userAction;

import com.xdlr.camera.trashcan.Trashcan;
import com.xdlr.camera.util.Base64ImageUtils;
import com.xdlr.camera.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class UserActionManager {
    private static Logger logger = LoggerFactory.getLogger(UserActionManager.class);
    private static final String[] states = {"ENTRANCE", "UPDATE", "VENDING_MACHINE", "CIVILIZED_BEHAVIOR"};
    private static final String HOST_ADDRESS = "http://127.0.0.1:8080";
    private String state;

    public void register(String certNum, String imagePath) {
        state = states[0];
        logger.info("初始化用户");
        String base64Image = Base64.encode(imagePath.getBytes());
        String res = HttpUtil.doGet(HOST_ADDRESS + "/register?" + "userId=" + certNum + "&name=tom" + "&userImg=" +
                base64Image);
        logger.info(res);
    }

    public void updateTime(String certNum, String imagePath) {
        state = states[1];
        logger.info("更新用户状态");
        String res = HttpUtil.doGet(HOST_ADDRESS + "/updateUserInfo?" + "userId=" + certNum);
        logger.info(res);
    }

    public void renderGoods(String certNum, String imagePath) {
        state = states[2];
        logger.info("售货机查询用户积分");
        String res = HttpUtil.doGet(HOST_ADDRESS + "/renderingUserInfo?" + "userId=" + certNum);
        logger.info(res);
    }

    public void openCan(String certNum, String imagePath) {
        state = states[3];
        logger.info("打开垃圾桶");
        Trashcan trashcan = new Trashcan();
        int state = trashcan.getCanState();
        if (state == Trashcan.TRASHCAN_STATE_OFF) {
            boolean result = trashcan.openCan();
            if(result){
                String res = HttpUtil.doGet(HOST_ADDRESS + "/openCan?" + "userId=" + certNum);
                logger.info(res);
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        trashcan.closeCan();
    }
}
