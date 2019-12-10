package com.xdlr.camera.face;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.Callback;
import com.sun.jna.Structure.ByValue;
import com.sun.jna.ptr.IntByReference;

public interface NVSSDK extends Library {

    public static final int WM_USER = 0x0400; //

    public static final int WM_MAIN_MESSAGE = WM_USER + 1001; // 系统消息
    public static final int WM_PARACHG = WM_USER + 1002; // 参数改变消息
    public static final int WM_ALARM = WM_USER + 1003; // 报警消息
    public static final int WCM_ERR_ORDER = 2;
    public static final int WCM_ERR_DATANET = 3;
    public static final int WCM_LOGON_NOTIFY = 7;
    public static final int WCM_VIDEO_HEAD = 8;
    public static final int WCM_VIDEO_DISCONNECT = 9;
    public static final int WCM_RECORD_ERR = 13;
    public static final int WCM_QUERYFILE_FINISHED = 18;
    public static final int WCM_DWONLOAD_FINISHED = 19;
    public static final int WCM_DWONLOAD_FAULT = 20;
    public static final int WCM_DOWNLOAD_INTERRUPT = 29;
    public static final int WCM_VCA_SUSPEND = 103;

    public static final int LOGON_SUCCESS = 0;
    public static final int LOGON_ING = 1;
    public static final int LOGON_RETRY = 2;
    public static final int LOGON_DSMING = 3;
    public static final int LOGON_FAILED = 4;
    public static final int LOGON_TIMEOUT = 5;
    public static final int NOT_LOGON = 6;
    public static final int LOGON_DSMFAILED = 7;
    public static final int LOGON_DSMTIMEOUT = 8;
    public static final int PLAYER_PLAYING = 0x02;
    public static final int USER_ERROR = 0x10000000;

    public static final int DSM_CMD_SET_NET_WAN_INFO = 0;
    public static final int DSM_CMD_SET_DIRECTORY_INFO = 1;
    public static final int DSM_CMD_SET_NVSREG_CALLBACK = 2;

    public static final int SERVER_NORMAL = 0;
    public static final int SERVER_ACTIVE = 1;
    public static final int SERVER_DNS = 2;
    public static final int SERVER_FIND_PSW = 3;
    public static final int SERVER_REG_ACTIVE = 4;

    public static final int NET_CLIENT_MIN = 0;
    public static final int NET_CLIENT_VCA_SUSPEND = NET_CLIENT_MIN + 32;

    public static final int VCA_SUSPEND_STATUS_PAUSE = 0;        //暂停智能分析
    public static final int VCA_SUSPEND_STATUS_RESUME = 1;    //恢复智能分析

    public static final int VCA_SUSPEND_RESULT_SUCCESS = 1;        //智能分析暂停成功
    public static final int VCA_SUSPEND_RESULT_CONFIGING = 2;        //智能分析暂停失败，正在设置，不可设参

    //人脸相关
    public static final int FACE_MAX_PAGE_COUNT = 20;

    public static final int FACE_CMD_MIN = 0;
    public static final int FACE_CMD_EDIT = (FACE_CMD_MIN + 0x00);
    public static final int FACE_CMD_DELETE = (FACE_CMD_MIN + 0x01);
    public static final int FACE_CMD_QUERY = (FACE_CMD_MIN + 0x02);
    public static final int FACE_CMD_MODEL = (FACE_CMD_MIN + 0x03);
    public static final int FACE_CMD_LIB_EDIT = (FACE_CMD_MIN + 0x04);
    public static final int FACE_CMD_LIB_DELETE = (FACE_CMD_MIN + 0x05);
    public static final int FACE_CMD_LIB_QUERY = (FACE_CMD_MIN + 0x06);
    public static final int FACE_CMD_FEATURE_QUERY = (FACE_CMD_MIN + 0x07);

    public static final int NET_PICSTREAM_CMD_FACE = 3;

    public static interface MAIN_NOTIFY extends Callback {
        void MainNotify(int _iLogonID, int _iwParam, Pointer _ilParam, Pointer _pUserData);
    }

    public static class ENCODERINFO extends Structure {
        public byte[] m_cHostName = new byte[32];
        public byte[] m_cEncoder = new byte[16];
        public int m_iRecvMode;
        public byte[] m_cProxy = new byte[16];
        public byte[] m_cFactoryID = new byte[32]; //ProductID
        public int m_iPort;//NVS port
        public int m_nvsType; //NVS type(NVS_T or NVS_S or DVR ...eg)
        public int m_iChanNum;
        public int m_iLogonState;
        public int m_iServerType;
    }

    public static class PointerSize extends Structure {
        public Pointer pPointer;
    }

    public static class LogonPara extends Structure {
        public int iSize;
        public byte[] cProxy = new byte[32];
        public byte[] cNvsIP = new byte[32];
        public byte[] cNvsName = new byte[32];
        public byte[] cUserName = new byte[16];
        public byte[] cUserPwd = new byte[16];
        public byte[] cProductID = new byte[32];
        public int iNvsPort;
        public byte[] cCharSet = new byte[32];
        public byte[] cAccontName = new byte[16];
        public byte[] cAccontPasswd = new byte[16];
    }

    public static class DsmNvsRegInfo extends Structure {
        public int iSize;
        public byte[] cFactoryID = new byte[32];    //出厂ID，设备唯一标识
        public byte[] cNvsIP = new byte[32];        //设备IP
        public byte[] cNvsName = new byte[32];        //设备名称
        public byte[] cRegTime = new byte[32];        //到服务器注册时间
        public int iChanNum;    //设备通道个数
    }

    public static interface DSM_NVS_REG_NOTIFY extends StdCallCallback {
        void DsmNvsRegNotify(Pointer _pInfo, int _iLen, Pointer _lpUserData);
    }

    public static class ActiveNvsNotify extends Structure {
        public int iSize;
        public DSM_NVS_REG_NOTIFY cbkNotify;
        public Pointer pUser;
    }

    public static class SDK_VERSION extends Structure {
        public short m_ulMajorVersion;
        public short m_ulMinorVersion;
        public short m_ulBuilder;
        public String m_cVerInfo;
    }

    //人脸相关结构体
    public static class FaceLibInfo extends Structure {
        public int iSize;
        public int iLibKey;
        public byte[] cName = new byte[64];
        public int iThreshold;
        public byte[] cExtrInfo = new byte[64];
        public int iAlarmType;
        public int iOptType;
        public byte[] cLibUUID = new byte[64];
        public byte[] cLibVersion = new byte[64];
    }

    public static class FaceLibQuery extends Structure {
        public int iSize;
        public int iChanNo;
        public int iPageNo;
        public int iPageCount;
    }

    public static class FaceLibQueryResult extends Structure {
        public int iSize;
        public int iChanNo;
        public int iTotal;
        public int iPageNo;
        public int iIndex;
        public int iPageCount;
        public FaceLibInfo tFaceLib;
    }

    public static class FaceLibQueryResultArr extends Structure {
        public FaceLibQueryResult[] tResult = new FaceLibQueryResult[20];
    }

    public static class FaceLibEdit extends Structure {
        public int iSize;
        public int iChanNo;
        public FaceLibInfo tFaceLib;
    }

    public static class FaceLibDelete extends Structure {
        public int iSize;
        public int iChanNo;
        public int iLibKey;
    }

    public static class FaceReply extends Structure {
        public int iSize;
        public int iLibKey;
        public int iFaceKey;
        public int iOptType;
        public int iResult;
    }

    public static class FaceInfo extends Structure {
        public int iSize;
        public int iLibKey;
        public int iFaceKey;
        public int iCheckCode;
        public int iFileType;
        public int iModeling;
        public byte[] cName = new byte[64];
        public int iSex;
        public byte[] cBirthTime = new byte[16];
        public int iNation;
        public int iPlace;
        public int iCertType;
        public byte[] cCertNum = new byte[64];
        public int iOptType;
//        public byte[] cLibUUID = new byte[64];
//        public byte[] cFaceUUID = new byte[64];
    }

    public static class FaceQuery extends Structure {
        public int iSize;
        public int iChanNo;
        public int iPageNo;
        public int iPageCount;
        public int iLibKey;
        public int iModeling;
        public byte[] cName = new byte[64];
        public int iSex;
        public byte[] cBirthStart = new byte[16];
        public byte[] cBirthEnd = new byte[16];
        public int iNation;
        public int iPlace;
        public int iCertType;
        public byte[] cCertNum = new byte[64];
    }

    public static class FaceQueryResult extends Structure {
        public int iSize;
        public int iChanNo;
        public int iTotal;
        public int iPageNo;
        public int iPageCount;
        public int iIndex;
        public FaceInfo tFace;
    }

    public static class FaceQueryResultArr extends Structure {
        public FaceQueryResult[] tResult = new FaceQueryResult[20];
    }

    public static class FaceEdit extends Structure {
        public int iSize;
        public int iChanNo;
        public byte[] cFacePic = new byte[256];
        public FaceInfo tFace;
    }

    public static class FaceDelete extends Structure {
        public int iSize;
        public int iChanNo;
        public int iLibKey;
        public int iFaceKey;
    }

    public static class VcaStatue extends Structure {
        public int iStatus;
    }

    public static class VCASuspendResult extends Structure {
        public int iSize;
        public int iStatus;
        public int iResult;
    }

    public static class PICSTREAM_INFO extends Structure {
        public byte[] RecvBuffer = new byte[200 * 1024];
    }

    public static interface NET_PICSTREAM_NOTIFY extends StdCallCallback {
        int PicDataNotify(int _ulID, int _lCommand, Pointer _tInfo, int _iLen, Pointer _lpUserData);
    }

    public static class NetPicPara extends Structure {
        public int iStructLen;                //Structure length
        public int iChannelNo;
        public NET_PICSTREAM_NOTIFY cbkPicStreamNotify;
        public Pointer pvUser;
    }

    public static class RECT extends Structure implements ByValue {
        public int left;
        public int top;
        public int right;
        public int bottom;
    }

    public static class FacePicData extends Structure {
        public int iFaceId;
        public int iDrop;
        public int iFaceLevel;
        public RECT tFaceRect;
        public int iWidth;
        public int iHeight;
        public int iFaceAttrCount;            //Number of face attributes
        public int iFaceAttrStructSize;    //The size of strcut FaceAttribute
        public Pointer[] ptFaceAttr = new Pointer[256];        //Face attributes,supports up to 256 attribute types,the subscript is the face attribute type://0-age,1-gender,2-masks,3-beard,4-eye open,5-mouth,6-glasses,7-race,8-emotion,9-smile,10-value......
        public int iDataLen;
        public Pointer pcPicData;
        public long ullPts;
        public int iAlramType;                //
        public int iSimilatity;            //0~100
        public int iLibKey;                //library key id
        public int iFaceKey;                //face key id
        public int iNegPicLen;                //negative picture len
        public Pointer pcNegPicData;            //negative picture data
        public int iNegPicType;            //negative picture type
        public int iSex;
        public int iNation;
        public int iPlace;                    //negative place
        public int iCertType;                //credentials type
        public byte[] cCertNum = new byte[64];
        public byte[] cBirthTime = new byte[64];
        public byte[] cName = new byte[64];
        public byte[] cLibName = new byte[64];
    }

    public static class PicTime extends Structure {
        public int uiYear;
        public int uiMonth;
        public int uiDay;
        public int uiWeek;
        public int uiHour;
        public int uiMinute;
        public int uiSecondsr;
        public int uiMilliseconds;
    }

    public static class PicData extends Structure {
        public PicTime tPicTime;
        public int iDataLen;
        public Pointer pcPicData;
    }

    public static class FacePicStream extends Structure {

        public int iStructLen;
        public int iSizeOfFull;        //The size of strcut PicData
        public Pointer tFullData;
        public int iFaceCount;
        public int iSizeOfFace;        //The size of strcut FacePicData
        public Pointer[] tPicData = new Pointer[32];
        public int iFaceFrameId;        //The face jpeg frame no
    }

    int NetClient_SetPort(int _iServerPort, int _iClientPort);

    int NetClient_Startup_V4(int _iServerPort, int _iClientPort, int _iWnd);

    int NetClient_Cleanup();

    int NetClient_SetNotifyFunction_V4(MAIN_NOTIFY _cbkMain, Pointer _cbkAlarm, Pointer _cbkPara, Pointer _cbkCom, Pointer _cbkProxy);

    int NetClient_Logon_V4(int _iLogonType, Pointer _pBuf, int _iBufSize);

    int NetClient_Logoff(int _iLogonID);

    int NetClient_GetLogonStatus(int _iLogonID);

    int NetClient_GetVersion(SDK_VERSION _ver);

    //
    int NetClient_SetDsmConfig(int _iCommand, Pointer _pvBuf, int _iBufSize);

    //
    int NetClient_GetDevInfo(int _iLogonID, ENCODERINFO _pEncoderInfo);

    int NetClient_SetTime(int _iLogonID, int _iyy, int _imo, int _idd, int _ihh, int _imi, int _iss);

    int NetClient_FaceConfig(int _iLogonId, int _iCmdId, int _iChanNo, Pointer _lpIn, int _iInLen, Pointer _lpOut, int _iOutLen);

    int NetClient_SetDevConfig(int _iLogonId, int _iCommand, int _iChannel, Pointer _lpInBuffer, int _iInBufferSize);

    int NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _lpOutBuffer, int _iOutBufferSize, IntByReference _lpBytesReturned);

    int NetClient_StartRecvNetPicStream(int _iLogonID, NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID);

    int NetClient_StopRecvNetPicStream(int _iRecvID);
}
