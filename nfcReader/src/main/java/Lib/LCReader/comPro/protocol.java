package Lib.LCReader.comPro;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.*;
import java.util.*;

import FWPubLib.pl_javacall;
import Lib.Error.ErrorCode;
import Lib.Transfer.SerialPort;
import Lib.Transfer.USB;
import Lib.Transfer.Bluetooth;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;


public class protocol {
	

	//public SerialPort mSerialPort;
	private OutputStream m_OutputStream;
	private InputStream m_InputStream;
	private USB		m_usb;
	//private Bluetooth m_bluetooth;
	public Bluetooth m_bluetooth;
	private SerialPort m_serial;
	private char m_portType;
	int size;
	int save_log = 0;
	
	//error code
	public static final int ERR_TRANS_INVALID_HEAD = 0x01;
	public static final int ERR_TRANS_FRM_LEN_EXP = 0x02;
	public static final int ERR_TRANS_INVALID_CMD = 0x03;
	public static final int ERR_TRANS_INVALID_USER_LEN = 0x04;
	public static final int ERR_TRANS_INVALID_DEVICE_CODE = 0x05;
	public static final int ERR_TRANS_INVALID_RCC = 0x06;
	public static final int ERR_TRANS_PORT_BUSY = 0x07;
	public static final int ERR_TRANS_FRM_LEN_LEAK = 0x08;
	public static final int ERR_FIND_NO_CARD = 0x10;
	public static final int ERR_AUTHEN_CLS = 0x12;
	public static final int ERR_READ_CARD = 0x13;
	public static final int ERR_WRITE_CARD = 0x14;
	public static final int ERR_RST_CTL_CARD = 0x15;
	public static final int ERR_CPUCARD_TRANS_TIMEOUT = 0x16;
	public static final int ERR_RF_CMD_EXE = 0x17;
	public static final int ERR_IVALID_USER_DATA = 0x18;
	public static final int ERR_IVALID_VALUE = 0x19;
	public static final int ERR_TIMEOUT = 0x26;
	public static final int ERR_FUNC = 0xFF;
	public static final int ERR_CMD = 0x017C;
	
	
	public static final char port_serial = 1;
	public static final char port_usb = 2;
	public static final char port_bluetooth =3;
	//public static final char USBOVERFLOW = 0x82;
	
	public static final byte STX = 0x02;
	public static final byte ETX = 0x03;
	public static final byte DLE = 0x10;
	public static final byte NAK = 0x15;
	public static final byte FM30TAG = 0x05;
	public static final byte FM30MASK = 0x10;
	
	private int lenOnceTransUSB = 0;
	private int gl_packageIndex = 0;
	
	public static final char FW30W_UART_TX_ADR_STX	=0;
	public static final char FW30W_UART_TX_STXLEN	=0x01;
	public static final char FW30W_UART_TX_ADR_INX	=(FW30W_UART_TX_ADR_STX + FW30W_UART_TX_STXLEN);
	public static final char FW30W_UART_TX_INXLEN	=0x01;
	public static final char FW30W_UART_TX_ADR_LEN =	(FW30W_UART_TX_ADR_INX + FW30W_UART_TX_INXLEN);
	public static final char FW30W_UART_TX_LENLEN	=0x02;
	public static final char FW30W_UART_TX_ADR_DEV	=(FW30W_UART_TX_ADR_LEN + FW30W_UART_TX_LENLEN);
	public static final char FW30W_UART_TX_DEVLEN	=0x02;
	public static final char FW30W_UART_TX_ADR_CMD	=(FW30W_UART_TX_ADR_DEV + FW30W_UART_TX_DEVLEN);
	public static final char FW30W_UART_TX_CMDLEN	=0x02;

	public static final char FW30W_UART_TX_ADR_DATA =(FW30W_UART_TX_ADR_CMD + FW30W_UART_TX_CMDLEN);
	public static final char FW30W_UART_TX_CHKLEN	=0x01;
	public static final char FW30W_UART_TX_VAL_STX	=0xAA;
	public static final char FW30W_UART_TX_VAL_DEV	=0x0000;

	public static final char FW30W_UART_RX_ADR_STX	=0;
	public static final char FW30W_UART_RX_STXLEN	=0x01;
	public static final char FW30W_UART_RX_ADR_INX	=(FW30W_UART_RX_ADR_STX + FW30W_UART_RX_STXLEN);
	public static final char FW30W_UART_RX_INXLEN	=0x01;
	public static final char FW30W_UART_RX_ADR_LEN	=(FW30W_UART_RX_ADR_INX + FW30W_UART_RX_INXLEN);
	public static final char FW30W_UART_RX_LENLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_DEV	=(FW30W_UART_RX_ADR_LEN + FW30W_UART_RX_LENLEN);
	public static final char FW30W_UART_RX_DEVLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_CMD	=(FW30W_UART_RX_ADR_DEV + FW30W_UART_RX_DEVLEN);
	public static final char FW30W_UART_RX_CMDLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_ST	=(FW30W_UART_RX_ADR_CMD + FW30W_UART_RX_CMDLEN);
	public static final char FW30W_UART_RX_STLEN		=0x01;

	public static final char FW30W_UART_RX_ADR_DATA =(FW30W_UART_RX_ADR_ST + FW30W_UART_RX_STLEN);
	public static final char FW30W_UART_RX_CHKLEN	=0x01;
	public static final char FW30W_UART_RX_VAL_STX	=0xAA;
	
//command list
	public static final char	COMPRO_GETVER						=0x0000;
	public static final char	COMPRO_SET_ADDR						=0x0005;
	public static final char	COMPRO_GET_ADDR						=0x0006;
	public static final char	CMD_SET_DEVPWD						=0x0008;
	public static final char	COMPRO_READER_MODE					=0x1036;
	public static final char	GET_READER_MODE						=0x1037;
	public static final char	COMPRO_REQUEST						=0x1000;
	public static final char	COMPRO_HALT							=0x1004;
	public static final char	COMPRO_M1AUTHENPASS					=0x1001;
	public static final char	COMPRO_M1READ						=0x1002;
	public static final char	COMPRO_M1READ_CONV					=0x1005;
	public static final char	COMPRO_M1WRITE						=0x1003;
	public static final char	COMPRO_M1WRITE_CONV					=0x1006;
	public static final char	COMPRO_INITVAL						=0x1007;
	public static final char	COMPRO_INCREMENT					=0x1008;
	public static final char	COMPRO_DECREMENT					=0x1009;
	public static final char	COMPRO_READVAL						=0x100A;
	public static final char	COMPRO_READ_TAG						=0x1010;
	public static final char	COMPRO_WRITE_TAG					=0x1011;
	public static final char	COMPRO_CLCPU_ATR					=0x101E;
	public static final char	COMPRO_CLCPU_APDU					=0x101F;
	public static final char	CMD_RAW_EXC							=0x1020;
	public static final char	CMD_GET_SSC							=0x1027;
	public static final char	COMPRO_RF_RST						=0x102E;
	public static final char	COMPRO_RF_ANT						=0x102F;
	public static final char	SETROPARA							=0x1030;
	public static final char	GETROPARA							=0x1031;
	public static final char	SET_APPEND_DATA						=0x1032;
	public static final char	GET_APPEND_DATA						=0x1033;
	public static final char	COMPRO_REQUESTB						=0x1050;
	public static final char	COMPRO_APDU_B						=0x1052;
	public static final char 	CMD_15693_REQ						=0x1060;
	public static final char 	CMD_15693_EXCH_COMMOM				=0x1061;
	public static final char	CMD_15693_EXCH_CUSTOM				=0x1062;
	public static final char	CMD_REQUEST_TYPEF					=0x1070;
	public static final char	CMD_TYPEF_APDU						=0x1072;
	
	public static final char	CMD_ICC_GET_CARDST					=0x1800;
	public static final char	CMD_ICC_GET_ATR						=0x1880;
	public static final char	CMD_ICC_APDU						=0x1881;
	
	public static final char	COMPRO_BEEP							=0xFF00;
	public static final char	COMPRO_LED							=0xFF01;
	public static final char	COMPRO_SET_BTNAME					=0x0110;
	public static final char	COMPRO_GET_MAXUSER_MEMSIZE			=0x0002;
	public static final char	COMPRO_RFLASH						=0x0003;
	public static final char	COMPRO_WFLASH						=0x0004;
	public static final char	CMD_SET_IDTOBJ_RESP					=0x00F1;
	public static final char	CMD_GET_IDTOBJ_RESP					=0x00F2;
	public static final char	COMPRO_SET_DATATYPE_AUTOREP			=0x00F3;
	public static final char	COMPRO_GET_DATATYPE_AUTOREP			=0x00F4;
	public static final char	COMPRO_GET_POWER					=0xFF10;
	public static final char	COMPRO_SET_PWR_OFF_TIME 			=0xFFFE;
	public static final char	COMPRO_POWER_OFF					=0xFFFF;
	
	public static final char	COMPRO_MULTY_NEXT					=0xFEFF;
	
	//ISO15693 Commands ---
	public static final char	ISO15693_INVENTORY      				=0x01;
	public static final char	ISO15693_STAY_QUIET                     =0x02;
	public static final char	ISO15693_READ_SINGLE_BLOCK              =0x20;
	public static final char	ISO15693_WRITE_SINGLE_BLOCK             =0x21;
	public static final char	ISO15693_LOCK_BLOCK                     =0x22;
	public static final char	ISO15693_READ_MULTIPLE_BLOCKS           =0x23;
	public static final char	ISO15693_WRITE_MULTIPLE_BLOCKS          =0x24;
	public static final char	ISO15693_SELECT                         =0x25;
	public static final char	ISO15693_RESET_TO_READY                 =0x26;
	public static final char	ISO15693_WRITE_AFI                      =0x27;
	public static final char	ISO15693_LOCK_AFI                       =0x28;
	public static final char	ISO15693_WRITE_DSFID                    =0x29;
	public static final char	ISO15693_LOCK_DSFID                     =0x2A;
	public static final char	ISO15693_GET_SYSTEM_INFO                =0x2B;
	public static final char	ISO15693_GET_MULTIPLE_BLOCK_SECURITY    =0x2C;
	public static final char 	ISO15693_GET_RANDDOM					=0xB2; 
	public static final char 	ISO15693_SET_PASS						=0xB3; 
	public static final char 	ISO15693_WRITE_PASS						=0xB4; 
	public static final char 	ISO15693_LOCK_PASS						=0xB5; 
	public static final char 	ISO15693_PROTECT_PAGE					=0xB6; 
	public static final char 	ISO15693_PROTECT_64BIT					=0xBB; 
	
	public protocol(/*Activity*/Context hostAct, Handler handler)
	{
		m_usb = new USB(hostAct);
		m_bluetooth = new Bluetooth(hostAct, handler);
		
	}
	
	public void _setTransPara(int usbTransLenOnce,int vendorID, int productID)
	{
		lenOnceTransUSB = usbTransLenOnce;
		m_usb.setDevicePara(vendorID, productID);
	}
	
	public int protocol_open(int fport,char[] fpath, int baud)
	{
		int st = -1;
		int linkSt = 0;
		int btSt = 0;
		long timeEslap = 0;
		long runStart = 0;
		
		m_portType = (char)fport;
		
		if(port_serial == m_portType)
		{

			try {
				m_serial = new SerialPort(new File(String.valueOf(fpath)), baud, 0);
				
				if(null == m_serial)
					st = ERR_TRANS_PORT_BUSY;
				else
				{
					m_InputStream = m_serial.getInputStream();
					m_OutputStream = m_serial.getOutputStream();
					st = 1;
				}
				
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(port_usb == m_portType)
		{
			st = m_usb.usbOpen();
			
			if(st != -1)
				gl_packageIndex = 0;//+2022.4.13
		}
		else if(port_bluetooth == m_portType)
		{
			
			m_bluetooth.connect(String.valueOf(fpath));
			
			runStart = System.currentTimeMillis();
			
			do{
				
				System.currentTimeMillis();
			
				btSt = m_bluetooth.getState();
			
				switch(btSt)
				{
				case Bluetooth.STATE_NONE:
					m_bluetooth.start();
				
				case Bluetooth.STATE_CONNECTING:
					timeEslap = System.currentTimeMillis() - runStart;
					if(timeEslap > 60000)
					{
						protocol_close(1);
						st = -1;
						linkSt = 1;
					}
					break;
				
				case Bluetooth.STATE_CONNECTED:
					st = 1;
					linkSt = 1;
					break;
				case Bluetooth.STATE_LISTEN:
				default:
					st = -1;
					linkSt = 1;
					break;
				}
				
				
			}while(linkSt == 0);
			
		}
		
		return st;
		
	}
	
	public int protocol_close(int fport)
	{
		if(port_serial == m_portType)
			m_serial.ReleaseSerial();
		else if(port_usb == m_portType)
			m_usb.usbClose(fport);
		else if(port_bluetooth == m_portType)
			m_bluetooth.stop();
		return 0;
	}
	
	
	void ShowLog(String title, byte[] pBuf,  int bufLen , String sText, int showType)
	{
		int i =0;
		String strtemp;
		
		if(0 == save_log)
			return;
		
		if(0 == showType)
		{
			Log.d(title,">>> "+ bufLen +"<<<");
			for(i=0; i< bufLen; i++)
			{
				strtemp = Integer.toHexString(pBuf[i]);
				if(1 == strtemp.length())
					strtemp = "0" + strtemp;
				Log.d(title, strtemp);
			}
		}
		else
		{
			Log.d(title, sText);
		}
			
	}
	
	
	//********************************************/
	//* commond communication with USB func USB
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
	 int rfab_fw_usbInfo(int icdev,int slen, char[] sdata,int[] rlen,char[] rdata)
	 {
		 return  0;
	 }
	
	/////////////////////////////////////////////////////////////////////////////////////////
	public static final int MAX_TRANSFER_LEN	=256;
	int				gl_Transfer_bufIn_len;
	int				gl_Transfer_bufOut_len;
	public static final boolean stateControler = false;

	void FillMaskDataToCmdBuffer(byte[] pIn, byte[] pOut, int pAdr,int validDataLen,  int[] maskNum)
	{
		int _maskCnt = 0;
		int i;
		int outDataLen = 0;

		for(i=0; i< validDataLen; i++)
		{
			pOut[pAdr + outDataLen++] = pIn[i];

			//if(pIn[i] == FW30W_UART_MASK)
			//{
			//	pOut[pAdr + outDataLen++] = 0x00;
			//	_maskCnt++;
			//}
		}

		maskNum[0] = _maskCnt;
	}
	public static class PROTOCOL_CMM
	{
		public int STX;
		public int CMD;
		public int DATALEN;
		public byte[] pData;
		public int CHK;
		public int TYPECMDLEN;
		
		public PROTOCOL_CMM()
		{
		}
	}

	//*******************************************************/
//		 output buffer
	/*  PC -> mmc 
	/*   
	/*
	/*  STX + CMD + LEN + DATA + CHK
	/*  HK = TEMP ^ CMD ^Data[0] ^ Data[1] ^  Data[n]
	/*  
	/********************************************************/

	void genSendBuf(PROTOCOL_CMM _pProData,byte[] oBuf,int[] allLen)
	{	
		int i;
		char chk =0;
		PROTOCOL_CMM pProData = _pProData;
		char[] tmpBuf = new char[16];
		
		//Start byte
		pl_javacall.ULToMultiByte(FW30W_UART_TX_VAL_STX,tmpBuf,FW30W_UART_TX_STXLEN,1);
		for(i=0; i<FW30W_UART_TX_STXLEN; i++)
			oBuf[FW30W_UART_TX_ADR_STX + i] = (byte)(tmpBuf[i] & 0xff);
		
		//INX
		pl_javacall.ULToMultiByte(gl_packageIndex,tmpBuf,FW30W_UART_TX_INXLEN,1);
		for(i=0; i<FW30W_UART_TX_INXLEN; i++)
			oBuf[FW30W_UART_TX_ADR_INX + i] = (byte)(tmpBuf[i] & 0xff);
		
		//LEN
		pl_javacall.ULToMultiByte(pProData.DATALEN+FW30W_UART_TX_CMDLEN+FW30W_UART_TX_DEVLEN,tmpBuf,FW30W_UART_TX_LENLEN,1);
		for(i=0; i< FW30W_UART_TX_LENLEN; i++)
			oBuf[FW30W_UART_TX_ADR_LEN + i] = (byte)(tmpBuf[i] & 0xff);

		//DEV
		pl_javacall.ULToMultiByte(FW30W_UART_TX_VAL_DEV,tmpBuf,FW30W_UART_TX_DEVLEN,1);
		for(i=0; i< FW30W_UART_TX_DEVLEN; i++)
			oBuf[FW30W_UART_TX_ADR_DEV + i] = (byte)(tmpBuf[i] & 0xff);
		
		//CMD
		pl_javacall.ULToMultiByte(pProData.CMD,tmpBuf,FW30W_UART_TX_CMDLEN,1);
		for(i=0; i<FW30W_UART_TX_CMDLEN; i++)
			oBuf[FW30W_UART_TX_ADR_CMD + i] = (byte)(tmpBuf[i] & 0xff);
		
		//Data
		for(i=0; i<pProData.DATALEN; i++)
			oBuf[FW30W_UART_TX_ADR_DATA + i] = (byte)(pProData.pData[i] & 0xff);

		//CHK Byte
		for(i=0;i< (FW30W_UART_TX_INXLEN + FW30W_UART_TX_LENLEN + FW30W_UART_TX_DEVLEN+FW30W_UART_TX_CMDLEN+pProData.DATALEN ); i++ )
			chk ^= oBuf[FW30W_UART_TX_ADR_INX + i];
		pl_javacall.ULToMultiByte(chk,tmpBuf,FW30W_UART_TX_CHKLEN,1);
		for(i=0; i<FW30W_UART_TX_CHKLEN; i++)
			oBuf[FW30W_UART_TX_ADR_DATA + pProData.DATALEN + i ] = (byte)(tmpBuf[i] & 0xff);

		//Package length
		allLen[0] = FW30W_UART_TX_STXLEN+ FW30W_UART_TX_INXLEN + FW30W_UART_TX_DEVLEN + FW30W_UART_TX_CMDLEN + FW30W_UART_TX_LENLEN + pProData.DATALEN  + FW30W_UART_TX_CHKLEN ;
		
	}
	
	int rfab_readOneByte(long icdev, byte[] rData)
	{
		int st;
		int settingTimeout = 1000;
		long timeEslap = 0;
		long runStart = 0;
		char[] getbuf = new char[128];
		int lenOnceRev = lenOnceTransUSB;
		
		runStart = System.currentTimeMillis();
		
		while(true)
		{
			timeEslap = System.currentTimeMillis() - runStart;
			
			if(timeEslap > settingTimeout)
			{
				st = ErrorCode.ERR_TIMEOUT;
				break;
			}
			//st = m_bluetooth.read(rData, 1);
			getbuf[0]=0x00;
			st = m_usb.usb_onceReceive((int)icdev,getbuf, lenOnceRev);
			//if(st != 0) 
			//	return ERR_TIMEOUT;
			
			//if(st != ErrorCode.ERR_NODATA_FOR_GET)
			//	break;
			
			if( st == 0)
			{
				rData[0] = (byte)getbuf[0];
				break;
			}
		}
		
		return st;
	}
	public int rfab_readBytes(long icdev, byte[] in, int readLen)
	{
		int st = 0;
		int i;
		byte[] tmpBuf = new byte[2];
		
		for(i=0; i< readLen; i++)
		{
			st = rfab_readOneByte(icdev, tmpBuf);
			if(st != 0)
				break;
			
			in[i] = tmpBuf[0];
		}
		
		return st;
	}

	//********************************************/
	//* commond communication with COM func
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
	/*
	 int rfab_commInfo(long icdev,int slen,byte[] sdata,
		 int[] rlen,byte[] rdata)
	 {
		 //if(icdev!=-1){
		 if(icdev == 1){
			 
			 byte[] OutReport = new byte[1024];
			 int flag ,i,j;
			 int nReaded = 0;
			 int counter = 0;
			 char[] tempbuf = new char[16];
			 byte[] tmpBytes = new byte[16];
			 int[] _rlen = new int[2];
			 int nError = 0;
			 int userDataLen = 0;
			 int userDataReaded = 0;
			 
			 //send data
			 flag = m_bluetooth.write(sdata, 0, slen);
			 if(flag != ErrorCode.ERR_OK)
				 return flag;

			 //get state
			 flag = rfab_readBytes(icdev, OutReport, FW30W_UART_RX_STXLEN + FW30W_UART_RX_LENLEN);
			 if(flag != ErrorCode.ERR_OK)
				 return flag;
			 
			 for(i=0; i< FW30W_UART_RX_LENLEN; i++)
				 tempbuf[i] = (char)OutReport[FW30W_UART_RX_STXLEN + i];
			 
			 counter += FW30W_UART_RX_STXLEN + FW30W_UART_RX_LENLEN;
			 
			 FWPubLib.pl_javacall.MultiByteToUL(tempbuf, FW30W_UART_RX_LENLEN, 1, _rlen);

			 userDataLen = _rlen[0] - FW30W_UART_RX_DEVLEN - FW30W_UART_RX_CMDLEN - FW30W_UART_RX_STLEN;
		
			 for(i=0;i<_rlen[0];i++)
			 {

				 flag = rfab_readBytes(icdev, tmpBytes, 1);
				 if(flag != ErrorCode.ERR_OK)
					 return flag;
				 
				 OutReport[counter] = tmpBytes[0];

				 if(i >= (FW30W_UART_RX_DEVLEN + FW30W_UART_RX_CMDLEN + FW30W_UART_RX_STLEN) && i != _rlen[0]-1)
				 {
					 rdata[userDataReaded] = OutReport[counter];
					 userDataReaded++;
				 }

				 counter++;
			 }
			 
			 rlen[0] = userDataReaded;
			 
			 if(OutReport[FW30W_UART_RX_ADR_ST] != 0)
				 return OutReport[FW30W_UART_RX_ADR_ST];

			 return ErrorCode.ERR_OK;
		 }
		 else return (ErrorCode.ERR_NOLINK);//handle error
		 
	 }
	 */

	 int rfab_usb_onceSend(int icdev,byte[] userBuf,int slen)
	 {
		 return 0;
	 }

	 int rfab_usb_onceReceive(int icdev,byte[] rbuff,int[] nBytesReaded)
	 {
		 int st;
		 int i;
		 char[] getbuf = new char[128];
		 int lenOnceRev = lenOnceTransUSB;
		
		 getbuf[0]=0x00;
		 st = m_usb.usb_onceReceive((int)icdev,getbuf, lenOnceRev);
		 if(st == 0)
		 {
			 nBytesReaded[0] = lenOnceRev;
			 for(i=0; i< lenOnceRev; i++)
			 {
				 rbuff[i] = (byte)getbuf[i];
			 }
		 }
			
		 return st;
	 }
	 
	 void rfab_usbTrans_send_next(int icdev, int slen)
	 {
		 byte[]	outBuf = new byte[255];
		 char[]	bufToSend = new char[255];
		 int[]	lenAll = new int[2];
		 PROTOCOL_CMM proData_obj;
		 int		 datalen;
		 int		i;

		 proData_obj = new PROTOCOL_CMM();
		 datalen = 0;

		 proData_obj.CMD = COMPRO_MULTY_NEXT;
		 proData_obj.DATALEN = datalen;
		 proData_obj.pData = new byte[proData_obj.DATALEN];

		 genSendBuf(proData_obj,outBuf,lenAll);
		 
		 for(i=0; i< lenAll[0]; i++)
			 bufToSend[i] = (char)outBuf[i];

		 m_usb.usb_onceSend(icdev, bufToSend, lenOnceTransUSB);
	 }

	 int rfab_tcpip_onceSend(int icdev,byte[] userBuf,int slen)
	 {
		 return 0;
	 }

	 int rfab_tcpip_onceReceive(int icdev,byte[] rbuff,int nBytesToRead)
	 {
		 return 0;
	 }

	 int rfab_usbTrans_rev_next(int icdev, int rlen)
	 {
		 int status; 
		 char[] buf = new char[256];
		 status = m_usb.usb_onceReceive(icdev, buf, lenOnceTransUSB);
		 if(status != 0)
			 return ERR_TIMEOUT;//
		 else
			 return 0;
	 }

	 void rfab_fw_usbTrans_send_next(int icdev, int slen)
	 {
	 }
	 
	 int SENDLAST(int icdev,int cmdCode, int slen, char[] sdata,int[] rlen,byte[] rdata)
	 {
		 int status;
		 char flag;
		 char[] setbuf = new char[128];
		 char[] getbuf = new char[128];
		 int rrlen = 0;
		 int pRDataAdr = 0;
		 int i = 0;
		 int times = 0;
		 int lenOnceRev = lenOnceTransUSB;
		 char[] tempbuf = new char[16];
		 byte[] tmpBytes = new byte[16];
		 int[] _rlen = new int[2];
		 int[] revUserLen = new int[2];
		 int[] tmpInt = new int[2];
		 int outBufLength;
		 char revPackageIndex = 0;
		 boolean fPackageIndexValid = true;
		 boolean fCommandPatch = true;
		 
		 status = m_usb.usb_onceSend(icdev, sdata, lenOnceRev);
		 if(status != 0) 
		 {
			 return ERR_TIMEOUT;
		 }

		 //==========================
		 status = m_usb.usb_onceReceive(icdev, getbuf, lenOnceRev);
		 if(status !=0)
			 return ERR_TIMEOUT;


		 flag=getbuf[FW30W_UART_RX_ADR_ST];
		 for(i=0; i< FW30W_UART_RX_LENLEN; i++)
			 tempbuf[i] = (char)getbuf[FW30W_UART_RX_ADR_LEN + i];
		 pl_javacall.MultiByteToUL(tempbuf, FW30W_UART_RX_LENLEN, 1, revUserLen);
		 rlen[0]= revUserLen[0] - FW30W_UART_RX_DEVLEN - FW30W_UART_RX_CMDLEN  - FW30W_UART_RX_STLEN;
		 rrlen=rlen[0];
		 
		 outBufLength = revUserLen[0] + FW30W_UART_RX_STXLEN + FW30W_UART_RX_INXLEN + FW30W_UART_RX_LENLEN + FW30W_UART_RX_CHKLEN;

		 //Get package index
		 for(i=0; i< FW30W_UART_RX_INXLEN; i++)
			 tempbuf[i] = (char)getbuf[FW30W_UART_RX_ADR_INX + i];
		 pl_javacall.MultiByteToUL(tempbuf, FW30W_UART_RX_INXLEN, 1, tmpInt);
		 revPackageIndex = (char) (tmpInt[0] & 0xff);
		 if(revPackageIndex == (gl_packageIndex%256) )
		 {
			 //Check CMD Code in the response
			 for(i=0; i< FW30W_UART_RX_CMDLEN; i++)
				 tempbuf[i] = (char)getbuf[FW30W_UART_RX_ADR_CMD + i];
			 pl_javacall.MultiByteToUL(tempbuf, FW30W_UART_RX_CMDLEN, 1, tmpInt);
			 
			 if(cmdCode != tmpInt[0])
				 fCommandPatch = false;
			 
		     if(rrlen <= 22)
			 {
				for(i=0;i<rrlen;i++)
					rdata[pRDataAdr++] = (byte)getbuf[ FW30W_UART_RX_ADR_DATA + i];
			 }
		     else
		     {//Large amount data will receiving
				 //Other package
				 //First 23 bytes

				 for(i=0;i<23;i++)
					 rdata[pRDataAdr++] = (byte)getbuf[FW30W_UART_RX_ADR_DATA +i];//maybe change
				 rrlen-=23;

				 //Data in middle
				 times=rrlen/ lenOnceRev;
				 if((rrlen % lenOnceRev) != 0 ) times += 1;

				 while(times > 1)
				 {
					 rfab_usbTrans_send_next(icdev, lenOnceRev);

					 getbuf[0]=0x00;
					 status = m_usb.usb_onceReceive(icdev,getbuf, lenOnceRev);
					 if(status != 0) 
						 return ERR_TIMEOUT;

					 for(i=0;i< lenOnceRev;i++)
						 rdata[pRDataAdr++] = (byte)getbuf[i];

					 rrlen-= lenOnceRev;
					 times--;
				 }

				 rfab_usbTrans_send_next(icdev, lenOnceRev);

				 //The last
				 getbuf[0]=0x00;
				 status = m_usb.usb_onceReceive(icdev,getbuf,lenOnceRev);
				 if(status != 0) 
					 return ERR_TIMEOUT;

				 for(i=0;i<rrlen;i++)
					 rdata[pRDataAdr++] = (byte)getbuf[i];
		     }
		     
			 if(fCommandPatch == false)
				 //status = ERR_CMD;//-2022.5.24
			 	 flag = ERR_CMD; //+2022.5.24
			 else
				 gl_packageIndex++;
		 }
		 else
		 {
			 fPackageIndexValid = false;
			 //status = ERR_TRANS_INVALID_DEVICE_CODE;//-2022.5.24
			 flag = ERR_TRANS_INVALID_DEVICE_CODE;//+2022.5.24
		 }
		 
		// return status;
		 return flag;//+2022.5.24
	}
	//********************************************/
	//* commond communication with USB func USB
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
	 int rfab_usbInfo(int icdev,int slen,byte[] sdata,int[] rlen,byte[] rdata)
	 {
		 char[] setbuf = new char[128];
		 char[] getbuf = new char[128];
		 char alen=0;
		 int lastlen=0;
		 int i;
		 int times=0;
		 char flag;
		 int rrlen;
		 int		status;
		 int lenOnceTime = lenOnceTransUSB;
		 int pDataAdr = 0;
		 int[] cmdCode = new int[2];
		 
		 for(i=0; i< FW30W_UART_TX_CMDLEN; i++)
			 setbuf[i] = (char)sdata[FW30W_UART_TX_ADR_CMD + i];
		 pl_javacall.MultiByteToUL(setbuf, FW30W_UART_TX_CMDLEN, 1, cmdCode);

		 if(slen< (lenOnceTime + 1))
		 {
			 for(i=0;i<slen;i++)
				 setbuf[i]= (char)sdata[pDataAdr++];
			 return  SENDLAST(icdev, cmdCode[0], slen, setbuf, rlen, rdata);
		 }
		 //===========================
		 for(i=0;i<lenOnceTime;i++)
			 setbuf[i]=(char)sdata[pDataAdr++];

		 status = m_usb.usb_onceSend(icdev,setbuf,lenOnceTime);
		 if(status !=0) 
			 return ERR_TIMEOUT;
		 status = rfab_usbTrans_rev_next(icdev, lenOnceTime);
		 if(status != 0)
			 return ERR_TIMEOUT;
		 
		 alen += lenOnceTime;
		 times = (slen-lenOnceTime)/lenOnceTime;
		 if((slen-lenOnceTime)% lenOnceTime !=0)times += 1;

		 while(1!=times)
		 {
			 for(i=0;i< lenOnceTime;i++)
				 setbuf[i]= (char)sdata[pDataAdr++];
			 status = m_usb.usb_onceSend(icdev,setbuf,lenOnceTime);
			 if(status != 0)
				 return ERR_TIMEOUT;
			 status = rfab_usbTrans_rev_next(icdev, lenOnceTime);
			 if(status != 0)
				 return ERR_TIMEOUT;

			 alen+= lenOnceTime;
			 times--;
		 }
	
		 lastlen=slen-alen;
		 for(i=0;i<lastlen;i++)
			 setbuf[i]= (char)sdata[pDataAdr++];

		 //==========================
		 return  SENDLAST(icdev,cmdCode[0], slen, setbuf, rlen, rdata);
	 }

	//********************************************/
	//* commond communication with TCPIP func TCPIP鍙ｉ�璁嚱鏁�鍏辩�?
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
	 int rfab_fw_tcpipInfo(int icdev,int slen,byte[] sdata,int[] rlen,byte[] rdata)
	 {
		 return 0;
	 }

	 int rfab_CommonTransfer(int icdev, PROTOCOL_CMM _pProData,byte[] pRev, int[] lenRev)
	 {
		 byte[] outBuf = new byte[1024];
		 int[]	lenAll= new int[2];
		 
		 genSendBuf(_pProData,outBuf,lenAll);

		 return rfab_usbInfo(icdev,lenAll[0],outBuf,lenRev,pRev);
	 }
	 
	 //////////////

}
