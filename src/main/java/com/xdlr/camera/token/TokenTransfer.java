package com.xdlr.camera.token;

import com.xdlr.camera.util.Base64ImageUtils;
import com.xdlr.camera.util.GlobalVariable;
import com.xdlr.camera.util.HttpUtil;
import com.xdlr.camera.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TokenTransfer {
    private static Logger logger = LoggerFactory.getLogger(TokenTransfer.class);
    private static final String[] states = {"ENTRANCE", "UPDATE", "VENDING_MACHINE", "CIVILIZED_BEHAVIOR"};
    private static final String hostAddress = "http://127.0.0.1:8080";
    private String state;

    public void initToken(String certNum, String imagePath) {
        state = states[0];
        logger.info("初始化用户");
        String res = HttpUtil.doGet(hostAddress + "/register?" + "userId=" + certNum + "&name=tom" + "&tokenValues="
        + GlobalVariable.INIT_TOKEN);
        logger.info(res);
    }

    public void updateTime(String certNum, String imagePath) {
        state = states[1];
        logger.info("更新用户状态");
        String res = HttpUtil.doGet(hostAddress + "/updateUserInfo?" + "userId=" + certNum);
        logger.info(res);
    }

    public void renderGoods(String certNum, String imagePath) {
        state = states[2];
        logger.info("售货机查询用户积分");
        String res = HttpUtil.doGet(hostAddress + "/renderingUserInfo?" + "userId=" + certNum);
        logger.info(res);

        UserActionResponse userInfo = JsonUtils.jsonToPojo(res, UserActionResponse.class);
        logger.info("用户id: " + userInfo.getUserId() + "  用户积分: " + userInfo.getUserToken());

    }

    public void addToken(String certNum, String imagePath) throws Exception{
        state = states[3];
        logger.info("增加积分");
        Trashcan trashcan = new Trashcan();
        boolean result = trashcan.openCan();
        if(result){
            String res = HttpUtil.doGet(hostAddress + "/rubbish?" + "userId=" + certNum + "&value=" + GlobalVariable.TOKEN_AWARD_PER_PHOTO);
            logger.info(res);
        }
        trashcan.closeCan();
    }
//    private void request(String certNum, String imagePath, String state) {
//        String base64Image = Base64ImageUtils.ImageToBase64ByLocal(imagePath);
//        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//
//        String res = HttpUtil.doPost(hostAddress + "/user/PutUserState",
//                "Uid=" + certNum + "&Ustate=" + state + "&Uimage=" + base64Image + "&Utime=" + time);
//        System.out.println("ready to send request");
//        System.out.println(res);
//    }
}
