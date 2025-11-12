/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
package js3ht;

public class s3ht_javacall
{
        static
        {
         System.loadLibrary("FwS3Dll");
        }


    public native static int IC_InitPort ( int port,long baud);
    public native static int IC_InitPort_Ex(int comType, char[] pathName, long exPara);
    public native static short IC_ExitComm(int lDevice);
    public native static short IC_Write (int lDevice, int offset, int len, char[] pWData);
    public native static short IC_Read (int lDevice, int offset, int len, char[] pRData);
    public native static short IC_Erase(int lDevice, int offset, int len);
    public native static short IC_WriteProtection(int lDevice, int offset, int len, char[] pWData);
    public native static short IC_ReadProtection (int lDevice, int offset, int len, char[] pRData);
    public native static short asc2hex(char[] strASC,char[] strHex,int length);
    public native static short hex2asc(char[] strHex,char[] strASC,int length);
    
    // device functions
    public native static short IC_ReadVer(int lDevice, char[] pVer);
    public native static short IC_Status(int lDevice);
    public native static short IC_DevBeep(int lDevice, int time);
    
    // 24C functions
    public native static short IC_Check_24Cxx(int lDevice, char[] pCardType);
    public native static short IC_Choose_24Cxx(int lDevice);
    public native static short IC_Choose_24C64(int lDevice);
    
    //4442 functions
    public native static short IC_Choose_4442(int lDevice);
    public native static short IC_ReadCount_SLE4442 (int lDevice);
    public native static short IC_CheckPass_SLE4442 (int lDevice, char[] pPwd);
    public native static short IC_ChangePass_SLE4442 (int lDevice, char[] pNewPwd);

    //4428 functions
    public native static short IC_Choose_4428(int lDevice);
    public native static short IC_ReadCount_SLE4428 (int lDevice);
    public native static short IC_CheckPass_SLE4428 (int lDevice, char[] pPwd);
    public native static short IC_ChangePass_SLE4428 (int lDevice, char[] pNewPwd);
    
    //102 functions
    public native static short IC_Choose_102(int lDevice);
    public native static short IC_ReadErrorCnt_102(int lDevice, char[] eCnt);
    public native static short IC_CheckUserCode_102(int lDevice, char[] pPwd);
    public native static short IC_CheckFuseCode_102(int lDevice, int iZone, char[] key);
    public native static short IC_ChangeUserCode_102(int lDevice, char[] newPwd);
    public native static short IC_ChangeFuseCode_102(int lDevice, int iZone, char[] key);
    
    
    //CPU card functions
    public native static short IC_Choose_Cpu(int lDevice);
    public native static short IC_SetCardSeat(int lDevice, int seatType);
    public native static short IC_CpuReset(int lDevice, char[] rlen, char[] pRData);
    public native static short IC_CpuApdu(int lDevice, int slen, char[] cmdBuf, int[] pRlen, char[] pRData);

    //HNCOS function
    public native static long  FWCosCreateMF(int lDevint, char[] MFName,int MFNameLen);
    public native static long  FWCosCreateDF(int lDevint,char[] DFID,char[] DFName,int DFNameLen);
    public native static long  FWCosCreateEndMF(int lDevint);
    public native static long  FWCosCreateEndDF(int lDevint, char[]  DFID);
    public native static long  FWCosDeleteMF(int lDevint);
    public native static long  FWCosCreateEF(int lDevint,char[] EFID,char[] EFType,char[] EFLen,int ReadPermission,int UpdatePermission);
    public native static long  FWCosSelectFile(int lDevint, char[] FileType,char[] FileID);
    public native static long  FWCosAppendRecord(int lDevint,char[] RecordSID,char[] RecordDATA,int  RecordDATALen,int  Mode, char[] keybuf);
    public native static long  FWCosReadRecord(int  lDevint,char[]  RecordNum,char[]  RecordSID, char[]  RecordLen,char[]  pvStr);
    public native static long  FWCosUpdateRecord(int  lDevint,char[]  RecordNum, char[]  RecordSID, char[]  RecordDATA, int  RecordLen,int  Mode, char[]  keybuf);
    public native static long  FWCosWriteKEY(int  lDevint,char[]  KEYType,char[]  KEYDATA,int  KEYDATALen,int  Mode,char[] keybuf);
    public native static long  FWCosUpdateKEY(int  lDevint,char[]  KEYType, char[]  KEYDATA,int  KEYDATALen,int  Mode,char[] keybuf);
    public native static long  FWCosGetChallenge(int  lDevint, char Le,char[]  Challenge,  long[]  ChallengeLen);
    public native static long  FWCosExternalAuthentication(int  lDevint,char[]  KEYbuf,int  KEYbuflen);
    public native static long  FWCosVerify(int  lDevint, char[]  PINbuf,int  PINlen);
    public native static long  FWCosGetData(int  lDevint,char[]  DataType, char[]  Data);
    public native static long  FWCosUpdateBinary(int  lDevint,char[]  BinarySID, char[]  Offset,char[]  BinaryDATA,int  BinaryDATALen,int  Mode, char[]  keybuf);
    public native static long  FWCosUpdateBinaryEx(int  lDevint,char[]  Offset,char[]  BinaryDATA,int  BinaryDATALen,int  Mode,char[]  keybuf);
    public native static long  FWCosReadBinary(int  lDevint,char[]  BinarySID, char[]  Offset,char[]  ReadLen, char[]  pvStr);
    public native static long  FWCosReadBinaryEx(int  lDevint,char[]  Offset,char[]  ReadLen,char[]  pvStr);
    public native static long  IC_Encrypt_DriverCard(char[] pOut, char[] pIn, int dataLen);

    public native static long  configLogFile(char[] szFileName);

}
