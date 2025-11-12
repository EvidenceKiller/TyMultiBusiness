
package FWPubLib;

public class pl_javacall
{
    static
    {
       System.loadLibrary("fwcom");
    }
    public native static int _a_hex(char[]a,char[]hex,int len);

    /*  convert commond char to Hex
     * parameter:
    	len:the length of hex buffer
     * return null
     */
    public native static void _hex_a(char[]hex,char[]a,int len);

    // char <--> GB2312
    public native static void _CharsToWchar(char[] szChars, short[] pWCHARS, int[] wlen);
    public native static int _WcharToMultyBytes(short[] lpszText,  char[] cText);
    // chars <-->UNICODE
    public native static void _CharsToUnicodeWchar(char[] szChars, int ansiLen, short[] pWCHARS, int[] wlen);

    /* Convert from multiply bytes to int
     * iType: 0 -> little-endian, 1 -> big-endian 
     * eg. 
    	iType == 0   => e8 03 00 00 == 100
    	iType == 1   => 00 00 03 e8 == 100
     */
    public native static int MultiByteToUL(char[] pByte, int lenBytes, int iType, int[] pVal);
    /* Convert from int to multiply bytes
     * bInv: 0 ��λ��ǰ�� 1��λ��ǰ
     * eg. 
    	bInv == 0   => 100 == e8 03 00 00 
    	bInv == 1   => 100 == 00 00 03 e8
     */
    public native static void ULToMultiByte(int val, char[] pArr, int lenArr, int bInv);

    public native static int genXor (char[] buf,  int len);

    /* #######################################################
     * Communication functions  =====================================
     * #######################################################
     */
    public native static int _fw_init_ex(int comType, char[] szPathName,int baud);
    public native static int _fw_exit(int icdev);

    public native static int _uart_send(int icdev,char[]sbuff,int slen);
    public native static int _uart_receive(int icdev,char[]rbuff,int nBytesToRead,int[] nBytesRead);

    public native static int _usb_send(int icdev,char[]sbuff,int slen);
    public native static int _usb_receive(int icdev,char[]rbuff,int nBytesToRead);


    public native static int _fw_tcpip_server_start( short serverPort);
    public native static int _fw_tcpip_server_quit();
    public native static int _fw_tcpip_getClientList(char[] pListEnt);
    public native static int _fw_tcpip_sendData(int termDev, char[] sData, int slen);
    public native static int _fw_tcpip_revData(int termDev, char[] rData, int rlen, int delay_ms);


    /* #######################################################
     * Crypto functions  =====================================
     * #######################################################
     */
    /******
     * DES/3ES
     ******
     */
    public native static int encrypt_CBC(char[] keybuf,char[] icvbuf,char[] source,int sourcelen,char[] dest);
    public native static int decrypty_CBC(char[]keybuf,char[] icvbuf,char[] source_hex,int sourcelen,char[] dest);
    //3des_cbc����
    public native static int tripleencry_CBC(char[] key,char[] icvbuf,char[] source,int sourcelen,char[] dest);
    //3des_cbc����
    public native static int tripledecry_CBC(char[] key,char[] icvbuf,char[] source_Hex,int sourcelen,char[] dest);

    public native static int ic_encrypt(char[]keybuf,char[]source,  short msglen , char[] ptrdest);
    public native static int ic_decrypt(char[]keybuf,char[]ptrsource,  short msglen, char[]ptrdest);
    public native static int ic_encrypt_ex(char[]keybuf,char[]source,  short msglen , char[] ptrdest,
    							char[] initval,char isInitvalInvalid);
    public native static int ic_encrypt_3DES_ECB(char[]key,char[]ptrsource,  short msglen ,
    									 char[] ptrdest);
    public native static int ic_decrypt_3DES_ECB(char[]key,char[]ptrsource,  short msglen,
    									 char[]ptrdest);
    public native static int ic_encrypt_3DES_ECB_EX(char[]key,char[]ptrsource,  int msglen ,
    									 char[] ptrdest, char[] pIV);
    public native static int ic_decrypt_3DES_ECB_EX(char[]key,char[]ptrsource,  int msglen,
    									 char[]ptrdest, char[] pIV);
    public native static int ic_encrypt_3DES_ECB_MAC(char[]key,char[]ptrsource,  int msglen ,
    									 char[] ptrdest,char[] initval);
    public native static int ic_decrypt_3DES_ECB_MAC(char[]key,char[]ptrsource,  int msglen ,
    									 char[] ptrdest,char[] initval);
    //3des_cbc send mode����
    public native static int tripleencry_CBC_sendMode(char[] key,char[] icvbuf,char[] source,int sourcelen,char[] dest);
    //3des_cbc send mode ����
    public native static int tripledecry_CBC_sendMode(char[] key,char[] icvbuf,char[] source_Hex,int sourcelen,char[] dest);

    public native static void ComputeCrc(int CRCType, char[]Data, int Length,char[]TransmitFirst, char[]TransmitSecond);
    public native static void ComputeCrc32(char[]Data, int Length,char[]TransmitBuf);

    //get 4 bytes MAC
    public native static void getMAC(char[] data,int dlen,char[] MAC,char[] sessionKey);
    public native static int GetCipherReLen(int sourceLen);
    public native static int GetPlainValidLen(char[] plain,  int lenPlain);

    public native static int ic_encrypt_ECB(char[]keybuf,char[]source,  short msglen , char[] ptrdest,char[] initval);
    public native static int ic_decrypt_ECB(char[]keybuf,char[]ptrsource,  short msglen, char[]ptrdest);

    //******
    // * AES
    // ******
    public native static char[] Cipher_ECB(char[] key,char[] input, int length);
    public native static char[] InvCipher_ECB(char[] key,char[] input, int length);

    public native static char[] Cipher_CBC(char[] iv,char[] key,char[] input, int length);
    public native static char[] InvCipher_CBC(char[] iv,char[] key,char[] input, int length);
    
    public native static int fwcom_tea ( int[] key, char d, int[] src_dest);

}
