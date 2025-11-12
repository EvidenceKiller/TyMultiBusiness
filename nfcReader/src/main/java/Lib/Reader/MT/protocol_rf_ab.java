package Lib.Reader.MT;


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


public class protocol_rf_ab {
	

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
	public static final int ERR_TRANS = 0x50;
	public static final int ERR_TIMEOUT = 0x60;
	public static final int ERR_HANDLE= -1;
	public static final int ERR_S8_EXECMD	=	-0x06;
	public static final int ERR_S8_LEN = 0x8D;
	public static final int ERR_PARA = 0x7D;
	public static final int ERR_FUNC = 0x9D;
	
	
	public static final char port_serial = 1;
	public static final char port_usb = 2;
	public static final char port_bluetooth =3;
	public static final char USBOVERFLOW = 0x82;
	
	public static final byte STX = 0x02;
	public static final byte ETX = 0x03;
	public static final byte DLE = 0x10;
	public static final byte NAK = 0x15;
	public static final byte FM30TAG = 0x05;
	public static final byte FM30MASK = 0x10;//閿熸埅鍑ゆ嫹閿熸枻鎷疯瘑
	
	private int lenOnceTransUSB = 0;
	
	public static final char FW30W_UART_TX_ADR_STX	=0;
	public static final char FW30W_UART_TX_STXLEN	=0x02;
	public static final char FW30W_UART_TX_ADR_LEN =	(FW30W_UART_TX_ADR_STX + FW30W_UART_TX_STXLEN);
	public static final char FW30W_UART_TX_LENLEN	=0x02;
	public static final char FW30W_UART_TX_ADR_DEV	=(FW30W_UART_TX_ADR_LEN + FW30W_UART_TX_LENLEN);
	public static final char FW30W_UART_TX_DEVLEN	=0x02;
	public static final char FW30W_UART_TX_ADR_CMD	=(FW30W_UART_TX_ADR_DEV + FW30W_UART_TX_DEVLEN);
	public static final char	FW30W_UART_TX_CMDLEN	=0x02;

	public static final char FW30W_UART_TX_ADR_DATA =(FW30W_UART_TX_ADR_CMD + FW30W_UART_TX_CMDLEN);
	public static final char FW30W_UART_TX_CHKLEN	=0x01;
	public static final char FW30W_UART_TX_VAL_STX	=0xAABB;
	public static final char FW30W_UART_TX_VAL_DEV	=0x0000;

	public static final char FW30W_UART_RX_ADR_STX	=0;
	public static final char FW30W_UART_RX_STXLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_LEN	=(FW30W_UART_RX_ADR_STX + FW30W_UART_RX_STXLEN);
	public static final char FW30W_UART_RX_LENLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_DEV	=(FW30W_UART_RX_ADR_LEN + FW30W_UART_RX_LENLEN);
	public static final char FW30W_UART_RX_DEVLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_CMD	=(FW30W_UART_RX_ADR_DEV + FW30W_UART_RX_DEVLEN);
	public static final char	FW30W_UART_RX_CMDLEN	=0x02;
	public static final char FW30W_UART_RX_ADR_ST	=(FW30W_UART_RX_ADR_CMD + FW30W_UART_RX_CMDLEN);
	public static final char FW30W_UART_RX_STLEN		=0x01;

	public static final char FW30W_UART_RX_ADR_DATA =(FW30W_UART_RX_ADR_ST + FW30W_UART_RX_STLEN);
	public static final char FW30W_UART_RX_CHKLEN	=0x01;
	public static final char FW30W_UART_RX_VAL_STX	=0xAABB;

	public static final char FW30W_UART_MASK		=0xAA;
	
	
//command list
	public static final char	RFAB_GETVER			=0x0401;
	public static final char	RFAB_READER_MODE	=0x0106;
	public static final char	RFAB_REQUEST		=0x0102;
	public static final char	RFAB_ANTICOLL		=0x0202;
	public static final char	RFAB_SELECT			=0x0302;
	public static final char	RFAB_HALT			=0x0402;
	public static final char	RFAB_M1AUTHENPASS	=0x0702;
	public static final char	RFAB_M1READ			=0x0802;
	public static final char	RFAB_M1READ_CONV	=0x0806;
	public static final char	RFAB_M1WRITE		=0x0902;
	public static final char	RFAB_M1WRITE_CONV	=0x0906;
	public static final char	RFAB_INITVAL		=0x0A02;
	public static final char	RFAB_READVAL		=0x0B02;
	public static final char	RFAB_INCREMENT		=0x0D02;
	public static final char	RFAB_DECREMENT		=0x0C02;
	public static final char	RFAB_BEEP			=0x0601;
	public static final char	RFAB_ANT			=0x0C01;
	public static final char	RFAB_RFLASH			=0x0A06;
	public static final char	RFAB_WFLASH			=0x0B06;
	public static final char	RFAB_DEVVERIFY		=0x0103;
	
	/* -2018.12.14
	public protocol_rf_ab(Activity hostAct, Handler handler)
	{
		m_usb = new USB(hostAct);
		m_bluetooth = new Bluetooth(hostAct, handler);
	}*/
	
	public protocol_rf_ab(Context hostAct, Handler handler)
	{
		m_usb = new USB(hostAct);
		m_bluetooth = new Bluetooth(hostAct, handler);
	}
	
	public void _setTransPara(int usbTransLenOnce,int vendorID, int productID)
	{
		//lenOnceTransUSB = usbTransLenOnce;
		//m_usb.setDevicePara(vendorID, productID);
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
					st = ERR_HANDLE;
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
					if(0 == dev_authen(1))
						st = 1;
					else
					{
						protocol_close(1);
						st = -1;
					}
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
	//* commond communication with USB func USB閿熸枻鎷烽�璁敓鏂ゆ嫹閿熸枻鎷�閿熸枻鎷烽敓鏂ゆ嫹
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

			if(pIn[i] == FW30W_UART_MASK)
			{
				pOut[pAdr + outDataLen++] = 0x00;
				_maskCnt++;
			}
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
//		濉厖涓插彛 output buffer
	/*  PC -> mmc 
	/*  鏍煎紡  
	/*
	/*  STX + CMD + LEN + DATA + CHK
	/*  娉細CHK = TEMP ^ CMD ^Data[0] ^ Data[1] ^ 鈥�^ Data[n]
	/*  鍙傛暟 exLen: 鏁版嵁涓惈鏈�0xAA鐨勬暟鐩紝杈撳嚭
	/********************************************************/

	void genSendBuf(PROTOCOL_CMM _pProData,byte[] oBuf,int[] allLen)
	{	
		int i;
		char chk =0;
		PROTOCOL_CMM pProData = _pProData;
		int[] maskCnt =new int[2];
		char[] tmpBuf = new char[16];
		
		for(i=0; i<FW30W_UART_TX_STXLEN; i++)
			oBuf[FW30W_UART_TX_ADR_STX + i] = (byte)(FW30W_UART_TX_VAL_STX >> 8*(FW30W_UART_TX_STXLEN-1-i));

		pl_javacall.ULToMultiByte(pProData.DATALEN+FW30W_UART_TX_CMDLEN+FW30W_UART_TX_DEVLEN + FW30W_UART_TX_CHKLEN,tmpBuf,FW30W_UART_TX_LENLEN,1);
		for(i=0; i< FW30W_UART_TX_LENLEN; i++)
			oBuf[FW30W_UART_TX_ADR_LEN + i] = (byte)(tmpBuf[i] & 0xff);

		pl_javacall.ULToMultiByte(FW30W_UART_TX_VAL_DEV,tmpBuf,FW30W_UART_TX_DEVLEN,0);
		for(i=0; i< FW30W_UART_TX_DEVLEN; i++)
			oBuf[FW30W_UART_TX_ADR_DEV + i] = (byte)(tmpBuf[i] & 0xff);

		for(i=0; i<FW30W_UART_TX_CMDLEN; i++)
			oBuf[FW30W_UART_TX_ADR_CMD + i] = (byte)(pProData.CMD >> 8*(FW30W_UART_TX_CMDLEN-1-i));

		FillMaskDataToCmdBuffer(pProData.pData, oBuf,FW30W_UART_TX_ADR_DATA, pProData.DATALEN , maskCnt);
		
		for(i=0;i< (FW30W_UART_TX_DEVLEN+FW30W_UART_TX_CMDLEN+pProData.DATALEN + maskCnt[0]); i++ )chk ^= oBuf[FW30W_UART_TX_ADR_DEV + i];

		for(i=0; i<FW30W_UART_TX_CHKLEN; i++)
			oBuf[FW30W_UART_TX_ADR_DATA + pProData.DATALEN + maskCnt[0]+  i ] = (byte)(chk >> 8*(FW30W_UART_TX_CHKLEN-1-i));

		allLen[0] = FW30W_UART_TX_STXLEN+ FW30W_UART_TX_DEVLEN + FW30W_UART_TX_CMDLEN+FW30W_UART_TX_LENLEN+pProData.DATALEN + maskCnt[0] + FW30W_UART_TX_CHKLEN ;
		
	}
	
	int rfab_readOneByte(long icdev, byte[] rData)
	{
		int st;
		int settingTimeout = 1000;
		long timeEslap = 0;
		long runStart = 0;
		
		runStart = System.currentTimeMillis();
		
		while(true)
		{
			timeEslap = System.currentTimeMillis() - runStart;
			
			if(timeEslap > settingTimeout)
			{
				st = ErrorCode.ERR_TIMEOUT;
				break;
			}
			st = m_bluetooth.read(rData, 1);
			if(st != ErrorCode.ERR_NODATA_FOR_GET)
				break;
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
	//* commond communication with COM func 涓插彛閫氳鍑芥暟锛屽叕鐢�
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
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
			 
			 pl_javacall.MultiByteToUL(tempbuf, FW30W_UART_RX_LENLEN, 0, _rlen);

			 userDataLen = _rlen[0] - FW30W_UART_RX_DEVLEN - FW30W_UART_RX_CMDLEN - FW30W_UART_RX_STLEN;
		
			 for(i=0;i<_rlen[0];i++)
			 {
				// flag=_uart_receive(icdev,&OutReport[counter ],1,&nReaded);
				// if(flag) return flag;
				 flag = rfab_readBytes(icdev, tmpBytes, 1);
				 if(flag != ErrorCode.ERR_OK)
					 return flag;
				 
				 OutReport[counter] = tmpBytes[0];

				 if( (i >= (FW30W_UART_RX_DEVLEN + FW30W_UART_RX_CMDLEN + FW30W_UART_RX_STLEN)) && (i != _rlen[0]-1))
				 {
//					 rdata[userDataReaded] = OutReport[counter];
//					 userDataReaded++;

//					 if( (OutReport[counter] == FW30W_UART_MASK) && ( userDataReaded < userDataLen))
//					 {
//						 counter++;
//						 flag = rfab_readBytes(icdev, tmpBytes, 1);
//						 if(flag != ErrorCode.ERR_OK)
//							 return flag;
//						 
//						 OutReport[counter] = tmpBytes[0];
//					 }
					 userDataReaded++;
					 rdata[userDataReaded] = OutReport[counter];
				 }
				 
				if( OutReport[counter] == FW30W_UART_MASK)
				{
					counter++;
					flag = rfab_readBytes(icdev, tmpBytes, 1);
					 if(flag != ErrorCode.ERR_OK)
						 return flag;
					 OutReport[counter] = tmpBytes[0];
					 
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

	 int rfab_usb_onceSend(int icdev,byte[] userBuf,int slen)
	 {
		 return 0;
	 }

	 int rfab_usb_onceReceive(int icdev,byte[] rbuff,int nBytesToRead)
	 {
		 return 0;
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
		 return 0;
	 }

	 void rfab_fw_usbTrans_send_next(int icdev, int slen)
	 {
	 }
	//********************************************/
	//* commond communication with USB func USB鍙ｉ�璁嚱鏁�鍏辩敤
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
	 int rfab_fw_usbInfo(int icdev,int slen,byte[] sdata,int[] rlen,byte[] rdata)
	 {
		 return 0;
	 }

	//********************************************/
	//* commond communication with TCPIP func TCPIP鍙ｉ�璁嚱鏁�鍏辩敤
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
		 byte[] outBuf = new byte[256];
		 int[]	lenAll= new int[2];
		 
		 genSendBuf(_pProData,outBuf,lenAll);

		 return rfab_commInfo(icdev,lenAll[0],outBuf,lenRev,pRev);
	 }
	 
	 //////////////
	public int dev_authen(int icdev)
	{
		int i;
		int[] rlen =  new int[2];
		PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		int[] key ={0x20,0x17,0x05,0x22};
		int[] cryptByMe = new int[4];
		Random random = new Random();
		
		proData_obj = new PROTOCOL_CMM();

		datalen = 4;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_DEVVERIFY;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0; i< 2; i++)
			cryptByMe[i] = random.nextInt();
		
		proData_obj.pData[0] = (byte)(cryptByMe[0]>> 8 & 0xff);
		proData_obj.pData[1] = (byte)(cryptByMe[0] & 0xff);
		proData_obj.pData[2] = (byte)(cryptByMe[1]>> 8 & 0xff);
		proData_obj.pData[3] = (byte)(cryptByMe[1] & 0xff);
			
		flag = rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if( flag ==0 )
		{
			if(rlen[0] != 0)
			{
				pl_javacall.fwcom_tea(key, (char)32, cryptByMe);
				
				if((byte)(cryptByMe[0] & 0xff) != inBuf[0])
					flag = ErrorCode.ERR_SHAKE_AUTHEN;
				
				if((byte)(cryptByMe[0]>> 8 & 0xff) != inBuf[1])
					flag = ErrorCode.ERR_SHAKE_AUTHEN;
				
				if((byte)(cryptByMe[1] & 0xff) != inBuf[2])
					flag = ErrorCode.ERR_SHAKE_AUTHEN;
				
				if((byte)(cryptByMe[1]>> 8 & 0xff) != inBuf[3])
					flag = ErrorCode.ERR_SHAKE_AUTHEN;
				
			}
			else
				flag = ErrorCode.ERR_NO_USERDATA;
		}		

		return (short)flag;
	}

}
