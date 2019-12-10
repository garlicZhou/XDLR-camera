package com.xdlr.camera.face;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class NetClient {

    private static NVSSDK nvssdk = (NVSSDK) Native.loadLibrary(System.getProperty("user.dir") + "\\src\\dll\\NVSSDK.dll",
            NVSSDK.class);

    public static int SetNotifyFunction(NVSSDK.MAIN_NOTIFY _cbkMain) {
        return nvssdk.NetClient_SetNotifyFunction_V4(_cbkMain, null, null, null, null);
    }

    public static int SetPort(int _iServerPort, int _iClientPort) {
        return nvssdk.NetClient_SetPort(_iServerPort, _iClientPort);
    }

    public static int Startup() {
        return nvssdk.NetClient_Startup_V4(0, 0, 0);
    }

    public static int Cleanup() {
        return nvssdk.NetClient_Cleanup();
    }

    public static int Logon(int _iLogonType, Pointer _pBuf, int _iBufSize) {
        return nvssdk.NetClient_Logon_V4(_iLogonType, _pBuf, _iBufSize);
    }

    public static int Logoff(int _iLogonID) {
        return nvssdk.NetClient_Logoff(_iLogonID);
    }

    public static int GetLogonStatus(int _iLogonID) {
        return nvssdk.NetClient_GetLogonStatus(_iLogonID);
    }

    public static int GetVersion(NVSSDK.SDK_VERSION _ver) {
        return nvssdk.NetClient_GetVersion(_ver);
    }

    public static int SetPort(int _iServerPort) {
        return nvssdk.NetClient_SetPort(_iServerPort, _iServerPort);
    }

    public static int SetDsmConfig(int _iCommand, Pointer _pvBuf, int _iBufSize) {
        return nvssdk.NetClient_SetDsmConfig(_iCommand, _pvBuf, _iBufSize);
    }

    public static int GetDevInfo(int _iLogonID, NVSSDK.ENCODERINFO _pEncoderInfo) {
        return nvssdk.NetClient_GetDevInfo(_iLogonID, _pEncoderInfo);
    }

    public static int SetTime(int _iLogonID, int _iyy, int _imo, int _idd, int _ihh, int _imi, int _iss) {
        return nvssdk.NetClient_SetTime(_iLogonID, _iyy, _imo, _idd, _ihh, _imi, _iss);
    }

    public static int FaceConfig(int _iLogonId, int _iCmdId, int _iChanNo, Pointer _lpIn, int _iInLen, Pointer _lpOut, int _iOutLen) {
        return nvssdk.NetClient_FaceConfig(_iLogonId, _iCmdId, _iChanNo, _lpIn, _iInLen, _lpOut, _iOutLen);
    }

    public static int SetDevConfig(int _iLogonId, int _iCommand, int _iChannel, Pointer _lpInBuffer, int _iInBufferSize) {
        return nvssdk.NetClient_SetDevConfig(_iLogonId, _iCommand, _iChannel, _lpInBuffer, _iInBufferSize);
    }

    public static int GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, Pointer _lpOutBuffer, int _iOutBufferSize) {
        IntByReference pRet = new IntByReference();
        return nvssdk.NetClient_GetDevConfig(_iLogonID, _iCommand, _iChannel, _lpOutBuffer, _iOutBufferSize, pRet);
    }

    public static int StartRecvNetPicStream(int _iLogonID, NVSSDK.NetPicPara _ptPara, int _iBufLen, IntByReference _puiRecvID) {
        return nvssdk.NetClient_StartRecvNetPicStream(_iLogonID, _ptPara, _iBufLen, _puiRecvID);
    }

    public static int StopRecvNetPicStream(int _iRecvID) {
        return nvssdk.NetClient_StopRecvNetPicStream(_iRecvID);
    }
}
