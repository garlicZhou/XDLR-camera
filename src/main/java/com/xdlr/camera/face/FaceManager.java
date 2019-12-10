package com.xdlr.camera.face;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.xdlr.camera.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

//人脸参数相关
public class FaceManager {
    private static final Logger logger = LoggerFactory.getLogger(FaceManager.class);

    public String tag;
    private FaceLoginInfo faceLoginInfo;

    int m_iLogonID = -1;

    int m_iConnectID = -1;
    int m_iLibKey = 0;        //保存最后一个库key值，便于修改和删除操作
    int m_iFaceKey = 0;        //保存最后一个人脸key值，便于修改和删除操作
    public int m_iVcaStatus = 0;    //智能分析状态
    String m_strSavePath;

    SnapNotifyListener snapNotifyListener;

    public FaceManager(FaceLoginInfo faceLoginInfo) {
        this.faceLoginInfo = faceLoginInfo;
        tag = "FaceManager " + faceLoginInfo.ip + " || ";
        m_strSavePath = "images/" + faceLoginInfo.faceDeviceId + "-PicStream";
    }

    public FaceLoginInfo getFaceLoginInfo() {
        return faceLoginInfo;
    }

    public interface SnapNotifyListener {

        void snapNotify(String faceDeviceId, int channelNo, boolean isStranger, String faceId, String negativePicturePath);

        void vcaSuspendSucceed(FaceManager faceManager, int channelNo);
    }

    public void registerSnapListener(SnapNotifyListener snapNotifyListener) {
        this.snapNotifyListener = snapNotifyListener;
    }

    //byte[]转String,JAVA8可以使用BASE64直接转
    String ByteToStr(byte[] bt) {
        int len = bt.length;
        String str = new String();
        byte[] bLast = new byte[2];
        for (int i = 0; i < len; ++i) {
            if (0 == bt[i]) {
                break;
            } else if (bt[i] > 0) {    //英文或者符号
                byte[] b = new byte[]{bt[i]};
                String s = new String(b);
                str += s;
            } else {        //中文,2字节为1个汉字
                if (0 != bLast[0]) {
                    bLast[1] = bt[i];
                    String s = new String(bLast);
                    str += s;
                    bLast[0] = 0;
                } else {
                    bLast[0] = bt[i];
                }
            }
        }
        return str;
    }

    //主回调
    NVSSDK.MAIN_NOTIFY cbkMain = new NVSSDK.MAIN_NOTIFY() {
        public void MainNotify(int iLogonID, int wParam, Pointer lParam, Pointer notifyUserData) {
//            logger.debug("设备状态回调！！！");
            int iMsgType = wParam & 0xFFFF;
            if (NVSSDK.WCM_LOGON_NOTIFY == iMsgType) {    //设备登录消息
                NVSSDK.ENCODERINFO tDevInfo = new NVSSDK.ENCODERINFO();
                NetClient.GetDevInfo(iLogonID, tDevInfo);
                String strIP = new String(tDevInfo.m_cEncoder).trim();
                String strID = new String(tDevInfo.m_cFactoryID).trim();
                //处理设备登录状态
                LogonNotify(strIP, strID, iLogonID, wParam >> 16);
            } else if (NVSSDK.WCM_VCA_SUSPEND == iMsgType) {
                NVSSDK.VCASuspendResult tParam = new NVSSDK.VCASuspendResult();
                tParam.iSize = tParam.size();
                tParam.write();
                NetClient.GetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_VCA_SUSPEND, 0, tParam.getPointer(), tParam.size());
                tParam.read();
                m_iVcaStatus = tParam.iResult;
                if (NVSSDK.VCA_SUSPEND_STATUS_PAUSE == tParam.iStatus) {
                    if (NVSSDK.VCA_SUSPEND_RESULT_SUCCESS == tParam.iResult) {
                        logger.info(tag + "智能分析暂停成功");
                        if (snapNotifyListener != null) {
                            snapNotifyListener.vcaSuspendSucceed(FaceManager.this, 0);
                        }
                    } else {
                        logger.info(tag + "智能分析暂停失败");
                    }
                }
            }
        }
    };

    //登录状态处理
    public void LogonNotify(String strIP, String strID, int iLogonID, int iLogonState) {
        String strMsg = new String();
        m_iLogonID = -1;
        iLogonState = NetClient.GetLogonStatus(iLogonID);
        if (NVSSDK.LOGON_SUCCESS == iLogonState) {            //登录成功
            m_iLogonID = iLogonID;
            strMsg = "LOGON_SUCCESS";
            //登录成功后跟设备校时,将本地时间同步到设备
            Calendar c = Calendar.getInstance();
            NetClient.SetTime(iLogonID, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        } else if (NVSSDK.LOGON_FAILED == iLogonState) {    //登录失败
            strMsg = "LOGON_FAILED";
        } else if (NVSSDK.LOGON_TIMEOUT == iLogonState) {    //登录超时
            strMsg = "LOGON_TIMEOUT";
        } else if (NVSSDK.LOGON_RETRY == iLogonState) {        //重新登录
            strMsg = "LOGON_RETRY";
        } else if (NVSSDK.LOGON_ING == iLogonState) {        //正在登录
            strMsg = "LOGON_ING";
        } else {
            strMsg = "LOGON_UNKNOW" + iLogonState;
        }
        System.out.println("[WCM_LOGON_NOTIFY][" + strMsg + "] IP(" + strIP
                + "),ID(" + strID + "),LogonID(" + iLogonID + ")");
    }

    //登录设备
    public int LogonDevice() {
        String strIp = faceLoginInfo.ip;
        if ("".equals(strIp)) {
            return -1;
        }
        String strUser = faceLoginInfo.userName;
        if ("".equals(strUser)) {
            strUser = "admin";
        }
        String strPsw = faceLoginInfo.password;
        if ("".equals(strPsw)) {
            strPsw = "admin";
        }
        int iPort = faceLoginInfo.port;
        if (iPort <= 0) {
            iPort = 3000;
        }

        NVSSDK.LogonPara tInfo = new NVSSDK.LogonPara();
        //必须字段
        tInfo.iSize = tInfo.size();                     //结构体大小
        tInfo.cNvsIP = strIp.getBytes();                //设备ip
        tInfo.cUserName = strUser.getBytes();           //用户名
        tInfo.cUserPwd = strPsw.getBytes();             //密码
        tInfo.iNvsPort = iPort;                             //设备端口
        tInfo.write();
        m_iLogonID = NetClient.Logon(NVSSDK.SERVER_NORMAL, tInfo.getPointer(), tInfo.size());

        int iTimeOut = 0;
        while (true) {
            int iLogonStatus = NetClient.GetLogonStatus(m_iLogonID);
            if (iLogonStatus == NVSSDK.LOGON_SUCCESS) {
                break;
            }

            iTimeOut = iTimeOut + 1;
            if (iTimeOut > 5) {
                System.out.println(tag + "LogonDevice, str=" + strIp + ", time out.");
                NetClient.Logoff(m_iLogonID);
                m_iLogonID = -1;
                return -1;
            }

            try {
                Thread.currentThread();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(tag + "Interrupted");
            }
        }

        System.out.println(tag + "[LogonDevice], ip=" + strIp + ", logonId=" + m_iLogonID);
        return 0;
    }

    //sdk初始化
    public int SDKInit() {
        //获取sdk版本
        NVSSDK.SDK_VERSION ver = new NVSSDK.SDK_VERSION();
        int iRet = NetClient.GetVersion(ver);
        System.out.println(tag + "SDK Version is " + ver.m_ulMajorVersion + "."
                + ver.m_ulMinorVersion + "." + ver.m_ulBuilder + " "
                + ver.m_cVerInfo);
        //设置主回调
        iRet = NetClient.SetNotifyFunction(cbkMain);
        System.out.println(tag + "SetNotifyFunction(" + iRet + ")");
        //启动sdk
        iRet = NetClient.Startup();
        System.out.println(tag + "SDK Startup(" + iRet + ")");
        return 0;
    }

    //人脸库查询
    public int FaceLibraryQuery() {
        int iRet = -1;
        int iPageCount = 20;//NVSSDK.FACE_MAX_PAGE_COUNT; //每页个数，每页最大查询20个
        NVSSDK.FaceLibQuery tQuery = new NVSSDK.FaceLibQuery();
        tQuery.iSize = tQuery.size();
        tQuery.iChanNo = 0;        //通道号，0表示第一通道，IPC只有1个通道
        tQuery.iPageCount = iPageCount;

        NVSSDK.FaceLibQueryResult tSingle = new NVSSDK.FaceLibQueryResult();
        int iSingleSize = tSingle.size();
        m_iLibKey = 0;

        int iPageNo = 0;        //查询页码，0表示第一页
        while (true) {
            tQuery.iPageNo = iPageNo;
            tQuery.write();
            //查询库信息
            NVSSDK.FaceLibQueryResultArr tResult = new NVSSDK.FaceLibQueryResultArr();
            tResult.write();
            iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_LIB_QUERY, tQuery.iChanNo, tQuery.getPointer(), tQuery.size(), tResult.getPointer(), iSingleSize);
            if (0 != iRet) {
                System.out.println(tag + "人脸库查询失败：" + iRet);
                return iRet;
            }

            //查询完后，打印出来
            tResult.read();
            int iToltalCount = tResult.tResult[0].iTotal;

            System.out.println(tag + "人脸库信息(" + iToltalCount + ")：------------------------");
            for (int i = 0; i < iToltalCount && i < NVSSDK.FACE_MAX_PAGE_COUNT; ++i) {
                int iIndex = i + 1;
                String strType = "上传";
                if (0 == tResult.tResult[i].tFaceLib.iAlarmType) {
                    strType = "不上传";
                }
                String sLibName = ByteToStr(tResult.tResult[i].tFaceLib.cName);
                String sExtrInfo = ByteToStr(tResult.tResult[i].tFaceLib.cExtrInfo);
                System.out.println(tag + "序号：" + iIndex + ", 库键值：" + tResult.tResult[i].tFaceLib.iLibKey + ", 识别阀值：" +
                        tResult.tResult[i].tFaceLib.iThreshold + ", 库名称：" + sLibName + ", 识别信息：" +
                        strType + ", 描述：" + sExtrInfo);
                m_iLibKey = tResult.tResult[i].tFaceLib.iLibKey;
            }

            //计算总页数
            int iTotalPage = iToltalCount / iPageCount;
            if (iToltalCount % iPageCount > 0) {
                iTotalPage = iTotalPage + 1;
            }
            iPageNo++;
            if (iPageNo >= iTotalPage || iPageNo > 1) {
                break;
            }
        }

        return iRet;
    }

    //人脸库添加
    public int FaceLibraryAdd() {
        NVSSDK.FaceLibEdit tInfo = new NVSSDK.FaceLibEdit();
        //必须字段
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;        //通道号，0表示第一通道，IPC只有1个通道
        tInfo.tFaceLib.iThreshold = 70;    //识别阀值，范围0~100
        tInfo.tFaceLib.iLibKey = 0;        //0表示添加
        tInfo.tFaceLib.iAlarmType = 0;    //0上传，1不上传
        //end

        //非必需字段
        tInfo.tFaceLib.cName = "face_lib3".getBytes();
//        tInfo.tFaceLib.cName = String.valueOf(new Random().nextInt()+100).getBytes();
        tInfo.tFaceLib.cExtrInfo = "facelib3~~~~~~add~~~~~".getBytes();
        //end

        //普通设备不需要此字段
        tInfo.tFaceLib.iOptType = 1;    //1添加,2修改
        //end

        tInfo.write();
        //添加
        NVSSDK.FaceReply tReply = new NVSSDK.FaceReply();
        tReply.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_LIB_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
        if (0 != iRet) {
            System.out.println(tag + "人脸库添加失败：" + iRet);
        } else {
            System.out.println(tag + "人脸库添加结果：" + tReply.iResult);
        }

        //显示添加人脸库结果
        FaceLibraryQuery();
        return iRet;
    }

    //人脸库修改
    int FaceLibraryModify() {
        if (m_iLibKey <= 0) {
            System.out.println(tag + "人脸库修改失败：请先查询或添加人脸库.");
            return -1;
        }

        NVSSDK.FaceLibEdit tInfo = new NVSSDK.FaceLibEdit();
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;        //通道号，0表示第一通道，IPC只有1个通道
        tInfo.tFaceLib.iThreshold = 80;    //识别阀值，范围0~100
        tInfo.tFaceLib.iLibKey = m_iLibKey;//大于0表示修改，此处默认修改第一个库
        tInfo.tFaceLib.iAlarmType = 1;    //0上传，1不上传
        tInfo.tFaceLib.cExtrInfo = "facelib~~~~~~modify~~~~~".getBytes();

        //普通设备不需要此字段
        tInfo.tFaceLib.iOptType = 2;    //1添加,2修改
        //end

        tInfo.write();

        //修改
        NVSSDK.FaceReply tReply = new NVSSDK.FaceReply();
        tReply.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_LIB_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
        if (0 != iRet) {
            System.out.println(tag + "人脸库修改失败：" + iRet);
        } else {
            System.out.println(tag + "人脸库修改结果：" + tReply.iResult);
        }

        //显示修改人脸库结果
        FaceLibraryQuery();
        return iRet;
    }

    //人脸库删除
    int FaceLibraryDelete() {
        if (m_iLibKey <= 0) {
            System.out.println(tag + "人脸库删除失败：请先查询或添加人脸库.");
            return -1;
        }

        NVSSDK.FaceLibDelete tInfo = new NVSSDK.FaceLibDelete();
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;        //通道号，0表示第一通道，IPC只有1个通道
        tInfo.iLibKey = m_iLibKey;//此处默认删除第一个库;
        tInfo.write();

        NVSSDK.FaceReply tReply = new NVSSDK.FaceReply();
        tReply.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_LIB_DELETE, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
        if (0 != iRet) {
            System.out.println(tag + "人脸库删除失败：" + iRet);
        } else {
            System.out.println(tag + "人脸库删除结果：" + tReply.iResult);
        }
        //显示删除人脸库结果
        FaceLibraryQuery();
        return iRet;
    }

    //人脸底图查询
    int FacePictureQuery(int _iPageNo) {
        //选择人脸库，默认最后一个库
        if (m_iLibKey <= 0) {
            System.out.println(tag + "人脸底图查询失败：请先添加或查询人脸库");
            return -1;
        }

        //查询条件
        NVSSDK.FaceQuery tInfo = new NVSSDK.FaceQuery();
        //必须字段
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;        //通道号，0表示第一通道，IPC只有1个通道
        tInfo.iLibKey = m_iLibKey;
        tInfo.iPageNo = _iPageNo;
        tInfo.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT;
        tInfo.cBirthStart = "1970-01-01".getBytes();    //开始出生日期
        tInfo.cBirthEnd = "2111-10-31".getBytes();    //结束出生日期
        //end
        //非必需字段
        tInfo.iSex = 0;            //性别，0未知，1男，2女
        tInfo.iNation = 0;        //民族，0未知
        tInfo.iPlace = 0;        //籍贯，0未知
        tInfo.iCertType = 0;    //证件类型，0未知，1二代身份证，2军官证
        tInfo.iModeling = 0;    //建模状态，0未知, 1建模成功, 2建模失败, 3未建模
        tInfo.cCertNum = "".getBytes();    //证件号码
        tInfo.cName = "".getBytes();    //底图姓名
        //end
        tInfo.write();
        //查询1
        NVSSDK.FaceQueryResult tSingle = new NVSSDK.FaceQueryResult();
        NVSSDK.FaceQueryResultArr tResult = new NVSSDK.FaceQueryResultArr();
        tResult.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_QUERY, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tResult.getPointer(), tSingle.size());
        if (0 != iRet) {
            System.out.println(tag + "人脸底图查询失败：" + iRet);
        } else {
            tResult.read();
            int iTotal = tResult.tResult[0].iPageCount;
            System.out.println(tag + "人脸底图信息(" + iTotal + ")：------------------------");
            for (int i = 0; i < iTotal && i < NVSSDK.FACE_MAX_PAGE_COUNT; ++i) {
                int iIndex = i + 1;
                System.out.println(tag + "序号：" + iIndex + ", 库键值：" + tResult.tResult[i].tFace.iLibKey + ", 人脸键值：" +
                        tResult.tResult[i].tFace.iFaceKey + ", 姓名：" + ByteToStr(tResult.tResult[i].tFace.cName) + ", 出生日期：" +
                        ByteToStr(tResult.tResult[i].tFace.cBirthTime) + ", 建模状态：" + tResult.tResult[i].tFace.iModeling);
                m_iFaceKey = tResult.tResult[i].tFace.iFaceKey;
            }
        }
        return iRet;
    }

    //人脸底图添加
    public int FacePictureAdd(String certNum, String negativePicturePath, int channelNo) {
        //选择人脸库，默认第一个库
        if (m_iLibKey <= 0) {
            System.out.println(tag + "人脸底图添加失败：请先添加或查询人脸库");
            return -1;
        }

        NVSSDK.FaceEdit tInfo = new NVSSDK.FaceEdit();
        //必须字段
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;                //通道号，0表示第一通道，IPC只有1个通道
        tInfo.tFace.iLibKey = m_iLibKey;    //人脸库键值
        tInfo.tFace.iModeling = 1;        //是否建模，1建模，0不建模
        tInfo.tFace.iFaceKey = 0;        //人脸底图键值，0表示添加
        tInfo.tFace.cName = "张三".getBytes();            //底图姓名
        tInfo.tFace.cBirthTime = "2000-01-01".getBytes();//出生日期
        tInfo.cFacePic = negativePicturePath.getBytes();//底图图片全路径
        tInfo.tFace.iFileType = 0;//文件扩展类型，iFaceKey=0时有效，0-jpg，1-png
        //end

        //非必需字段
        tInfo.tFace.iSex = 0;            //性别，0未知，1男，2女
        tInfo.tFace.iNation = 0;        //民族，0未知
        tInfo.tFace.iPlace = 0;            //籍贯，0未知;
        tInfo.tFace.iCertType = 1;        //证件类型，0未知，1二代身份证，2军官证;
        tInfo.tFace.cCertNum = certNum.getBytes();//证件号码
        tInfo.tFace.iOptType = 1;        //1=添加，2=修改，3=复制，4=迁移
        tInfo.write();

        NVSSDK.FaceReply tReply = new NVSSDK.FaceReply();
        tReply.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
        tReply.read();
        if (0 != iRet) {
            System.out.println(tag + "人脸底图添加失败：" + iRet);
        } else {
            System.out.println(tag + "人脸底图添加结果：" + tReply.iResult);
        }

        //显示人脸底图添加后结果
        FacePictureQuery(0);
        return iRet;
    }

    //人脸底图修改
    int FacePictureModify() {
        //此处默认修改最后一张底图
        if (m_iFaceKey <= 0) {
            System.out.println(tag + "人脸底图修改失败：请先查询或添加人脸底图");
            return -1;
        }

        NVSSDK.FaceEdit tInfo = new NVSSDK.FaceEdit();
        //必须字段
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;                //通道号，0表示第一通道，IPC只有1个通道
        tInfo.tFace.iLibKey = m_iLibKey;    //人脸库键值
        tInfo.tFace.iFaceKey = m_iFaceKey;//人脸底图键值
        tInfo.tFace.cName = "李四".getBytes();            //底图姓名
        tInfo.tFace.cBirthTime = "2014-04-04".getBytes();//出生日期
        //end

        //非必需字段
        tInfo.tFace.iSex = 0;            //性别，0未知，1男，2女
        tInfo.tFace.iNation = 0;        //民族，0未知
        tInfo.tFace.iPlace = 0;            //籍贯，0未知;
        tInfo.tFace.iCertType = 1;        //证件类型，0未知，1二代身份证，2军官证;
        tInfo.tFace.cCertNum = "232321201404040404".getBytes();//证件号码
        //end
        tInfo.write();

        NVSSDK.FaceReply tReply = new NVSSDK.FaceReply();
        tReply.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_EDIT, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
        tReply.read();
        if (0 != iRet) {
            System.out.println(tag + "人脸底图修改失败：" + iRet);
        } else {
            System.out.println(tag + "人脸底图修改结果：" + tReply.iResult);
        }
        return iRet;
    }

    //人脸底图删除
    int FacePictureDelete() {
        //此处默认删除最后一张底图
        if (m_iFaceKey <= 0) {
            System.out.println(tag + "人脸底图删除失败：请先查询或添加人脸底图");
            return -1;
        }

        NVSSDK.FaceDelete tInfo = new NVSSDK.FaceDelete();
        tInfo.iSize = tInfo.size();
        tInfo.iChanNo = 0;        //通道号，0表示第一通道，IPC只有1个通道
        tInfo.iLibKey = m_iLibKey;
        tInfo.iFaceKey = m_iFaceKey;
        tInfo.write();

        NVSSDK.FaceReply tReply = new NVSSDK.FaceReply();
        tReply.write();
        int iRet = NetClient.FaceConfig(m_iLogonID, NVSSDK.FACE_CMD_DELETE, tInfo.iChanNo, tInfo.getPointer(), tInfo.size(), tReply.getPointer(), tReply.size());
        tReply.read();
        if (0 != iRet) {
            System.out.println(tag + "人脸底图删除失败：" + iRet);
        } else {
            System.out.println(tag + "人脸底图删除结果：" + tReply.iResult);
        }

        //显示人脸底图删除后结果
        FacePictureQuery(0);
        return iRet;
    }


    public int SetVcaStatue(int _iStatus) {
        return SetVcaStatue(0, _iStatus);
    }

    /**
     * 设置智能分析状态
     *
     * @param channelNo 通道号，0表示第一通道，IPC只有1个通道
     * @param _iStatus
     * @return
     */
    public int SetVcaStatue(int channelNo, int _iStatus) {
        NVSSDK.VcaStatue tInfo = new NVSSDK.VcaStatue();
        tInfo.iStatus = _iStatus;
        tInfo.write();
        return NetClient.SetDevConfig(m_iLogonID, NVSSDK.NET_CLIENT_VCA_SUSPEND, channelNo, tInfo.getPointer(), tInfo.size());
    }

    //保存图片
    public int SavePicture(String FileName, Pointer picData, int len) {
        if (null == picData || len <= 0) {
            return -1;
        }

        FileOutputStream fop = null;
        File file;

        try {
            file = new File(FileName);
            fop = new FileOutputStream(file);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            // get the content in bytes
            byte[] contentInBytes = picData.getByteArray(0, len);
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    //图片流回调
    NVSSDK.NET_PICSTREAM_NOTIFY CallBack_PicStreamInfo = new NVSSDK.NET_PICSTREAM_NOTIFY() {
        public int PicDataNotify(int _ulID, int _lCommand, Pointer _tInfo, int _iLen, Pointer _lpUserData) {
            if (_lCommand != NVSSDK.NET_PICSTREAM_CMD_FACE) {
                return -1;
            }
            logger.debug("图片流回调");
            String id = IdUtil.generateId();
            NVSSDK.FacePicStream tFacePicStream = new NVSSDK.FacePicStream();
            tFacePicStream.write();
            Pointer pFaceBuffer = tFacePicStream.getPointer();
            byte[] RecvBuffer = _tInfo.getByteArray(0, _iLen);
            int iCopySize = Math.min(tFacePicStream.size(), _iLen);
            pFaceBuffer.write(0, RecvBuffer, 0, iCopySize);
            tFacePicStream.read();

            //拷贝抓拍全景图数据
            NVSSDK.PicData tFullData = new NVSSDK.PicData();   //从指针获取全景图数据
            tFullData.write();
            Pointer pFullBuffer = tFullData.getPointer();
            Pointer pFullData = tFacePicStream.tFullData;
            byte[] bFullBuffer = pFullData.getByteArray(0, tFacePicStream.iSizeOfFull);
            int iFullPicSize = Math.min(tFullData.size(), tFacePicStream.iSizeOfFull);
            pFullBuffer.write(0, bFullBuffer, 0, iFullPicSize);
            tFullData.read();

            //拷贝抓拍人脸数据
            NVSSDK.FacePicData[] tPicData = (NVSSDK.FacePicData[]) new NVSSDK.FacePicData().toArray(32);
            for (int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++) {
                tPicData[i].write();
                Pointer pPicBuffer = tPicData[i].getPointer();
                Pointer pData = tFacePicStream.tPicData[i];
                byte[] bBuffer = pData.getByteArray(0, tFacePicStream.iSizeOfFace);
                int iPicSize = Math.min(tPicData[i].size(), tFacePicStream.iSizeOfFace);
                pPicBuffer.write(0, bBuffer, 0, iPicSize);
                tPicData[i].read();
            }

            //抓拍时间
            int uiYear = tFullData.tPicTime.uiYear;
            int uiMonth = tFullData.tPicTime.uiMonth;
            int uiDay = tFullData.tPicTime.uiDay;
            int uiWeek = tFullData.tPicTime.uiWeek;
            int uiHour = tFullData.tPicTime.uiHour;
            int uiMinute = tFullData.tPicTime.uiMinute;
            int uiSecondsr = tFullData.tPicTime.uiSecondsr;
            int uiMilliseconds = tFullData.tPicTime.uiMilliseconds;

            String sFileNameBase;
            sFileNameBase = m_strSavePath + "/channelNo" + _ulID + "/";
            sFileNameBase += "" + uiYear + uiMonth + uiDay + uiWeek + uiHour + uiMinute + uiSecondsr + uiMilliseconds;

            //保存小图和底图
            int max = 0;
            int record = 0;
            boolean isStranger = true;
            for (int i = 0; i < tFacePicStream.iFaceCount && i < 32; i++) {
                // 获取最高质量人脸图像，作为底图
                if (max < tPicData[i].iFaceLevel) {
                    max = tPicData[i].iFaceLevel;
                    record = i;
                }

                // 人脸小图
                SavePicture(sFileNameBase + "face" + i + ".jpg", tPicData[i].pcPicData, tPicData[i].iDataLen);

                // 人脸底图，合法人脸
                if (1 == tPicData[i].iAlramType) {
                    // 有底图，是熟人
                    isStranger = false;
                } else {
                    logger.debug("没有底图或者不合法人脸");
                }
            }

            String preparedNegativePicturePath = sFileNameBase + "face" + record + ".jpg";
            String c = new String(tPicData[record].cCertNum);
            String r = c.substring(0, 18);

            String faceId = String.valueOf(isStranger ? id : r);
            if (snapNotifyListener != null) {
                snapNotifyListener.snapNotify(faceLoginInfo.faceDeviceId, _ulID, isStranger, faceId, preparedNegativePicturePath);
            }
            return 0;
        }
    };

    public int StartSnap() {
        return StartSnap(0);
    }

    //开启图片流
    public int StartSnap(int channelNo) {
        NVSSDK.NetPicPara tNetPicParam = new NVSSDK.NetPicPara();
        tNetPicParam.iStructLen = tNetPicParam.size();
        tNetPicParam.iChannelNo = channelNo;
        tNetPicParam.cbkPicStreamNotify = CallBack_PicStreamInfo; //抓拍回调函数
        tNetPicParam.pvUser = null;

        IntByReference pConnectID = new IntByReference();
        int iRet = NetClient.StartRecvNetPicStream(m_iLogonID, tNetPicParam, tNetPicParam.size(), pConnectID);
        if (iRet < 0) {
            m_iConnectID = -1;
            System.out.println(tag + "StartRecvNetPicStream Failed!");
        } else {
            m_iConnectID = pConnectID.getValue();
            System.out.println(tag + "StartRecvNetPicStream Success! ConnectID(" + m_iConnectID + ")");
        }

        return 0;
    }

    public int Exit() {
        //停止图片流
        NetClient.StopRecvNetPicStream(m_iConnectID);
        m_iConnectID = -1;

        //注销登陆
        NetClient.Logoff(m_iLogonID);
        m_iLogonID = -1;

        NetClient.Cleanup();
        return 0;
    }

    //创建图片保存目录
    public boolean createPicDir() {
        int count = faceLoginInfo.faceDeviceChannelCount;
        for (int i = 0; i < count; i++) {
            String destDirName = m_strSavePath + "/channelNo" + i;
            File dir = new File(destDirName);
            if (dir.exists()) {// 判断目录是否存在
                //	System.out.println(tag + "目标目录已存在！");
                return false;
            }
            if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
                destDirName = destDirName + File.separator;
            }
            if (!dir.mkdirs()) {
                return false;
            }
        }
        return true;
    }

    public int getLogonID() {
        return m_iLogonID;
    }
}