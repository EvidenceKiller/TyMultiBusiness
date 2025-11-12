package Lib.LCReader.T6;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.*;
import java.util.*;


import Lib.Transfer.SerialPort;
import Lib.Transfer.USB;
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
import android.os.SystemClock;
import android.util.Log;


public class protocol_T6 {
	

	//public SerialPort mSerialPort;
	private OutputStream m_OutputStream;
	private InputStream m_InputStream;
	private USB		m_usb;
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
	
	
	public static final char port_serial = 0;
	public static final char port_usb = 1;
	public static final char USBOVERFLOW = 0x82;
	
	public static final byte STX = 0x02;
	public static final byte ETX = 0x03;
	public static final byte DLE = 0x10;
	public static final byte NAK = 0x15;
	public static final byte FM30TAG = 0x05;
	public static final byte FM30MASK = 0x10;//閿熸埅鍑ゆ嫹閿熸枻鎷疯瘑
	
	public static final char FW30W_USB_ADR_STX =	0;
	public static final char FW30W_USB_STXLEN	=0x02;
	public static final char FW30W_USB_ADR_STX1= 0;
	public static final char FW30W_USB_STX1LEN =1;
	public static final char FW30W_USB_ADR_STX2 =(FW30W_USB_ADR_STX1+FW30W_USB_STX1LEN);
	public static final char FW30W_USB_STX2LEN	=(FW30W_USB_STXLEN - FW30W_USB_STX1LEN);
	public static final char FW30W_USB_ADR_CMD	=(FW30W_USB_ADR_STX+FW30W_USB_STXLEN);
	public static final char FW30W_USB_CMDLEN	=0x01;
	public static final char FW30W_USB_ADR_LEN	=(FW30W_USB_ADR_CMD + FW30W_USB_CMDLEN);
	public static final char FW30W_USB_LENLEN	=0x01;
	public static final char FW30W_USB_ADR_DATA =(FW30W_USB_ADR_LEN + FW30W_USB_LENLEN);
	public static final char FW30W_USB_CHKLEN	=0x01;
	public static final char FW30W_USB_VAL_STX	=	0x0205;
	public static final char FW30W_USB_RP_ADR_STX = 0;
	public static final char FW30W_USB_RP_LEN_STX = 0x02;
	public static final char FW30W_USB_RP_ADR_SW = (FW30W_USB_RP_ADR_STX + FW30W_USB_RP_LEN_STX) ;
	public static final char FW30W_USB_RP_LEN_SW = 0x01;
	public static final char FW30W_USB_RP_ADR_RLEN = (FW30W_USB_RP_ADR_SW + FW30W_USB_RP_LEN_SW);
	public static final char FW30W_USB_RP_LEN_RLEN = 0x01;
	public static final char FW30W_USB_RP_ADR_DATA = (FW30W_USB_RP_ADR_RLEN + FW30W_USB_RP_LEN_RLEN);
	public static final char FW30W_USB_RP_LEN_CHK = 0x01;
	
	public static final char FW30W_UART_TX_ADR_STX	=0;
	public static final char FW30W_UART_TX_STXLEN	=0x01;
	public static final char FW30W_UART_TX_ADR_CMD	=(FW30W_UART_TX_ADR_STX+FW30W_UART_TX_STXLEN);
	public static final char	FW30W_UART_TX_CMDLEN	=0x01;
	public static final char FW30W_UART_TX_ADR_LEN	=(FW30W_UART_TX_ADR_CMD + FW30W_UART_TX_CMDLEN);
	public static final char FW30W_UART_TX_LENLEN	=0x01;
	public static final char FW30W_UART_TX_ADR_DATA =(FW30W_UART_TX_ADR_LEN + FW30W_UART_TX_LENLEN);
	public static final char FW30W_UART_TX_CHKLEN	=0x01;
	public static final char FW30W_UART_TX_VAL_STX	=0x05;
	
	private int lenOnceTransUSB = 0;
	
	
	public protocol_T6(Context hostAct)
	{
		m_usb = new USB(hostAct);
	}
	
	public void _setTransPara(int usbTransLenOnce,int vendorID, int productID)
	{
		lenOnceTransUSB = usbTransLenOnce;
		m_usb.setDevicePara(vendorID, productID);
	}
	
	public int protocol_open(int fport,char[] fpath, int baud)
	{
		int st = 0;
		
		m_portType = (char)fport;
		
		if(port_serial == m_portType)
		{
			
/*			File device = new File(fpath);
			
			// Check access permission 
			if (!device.canRead() || !device.canWrite()) {
				try {
					// Missing read/write permission, trying to chmod the file 
					Process su;
					su = Runtime.getRuntime().exec("/system/bin/su");
					String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
							+ "exit\n";
					su.getOutputStream().write(cmd.getBytes());
					if ((su.waitFor() != 0) || !device.canRead()
							|| !device.canWrite()) {
						throw new SecurityException();
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new SecurityException();
				}
			}
			
			try
			{
				mFd = open(device.getAbsolutePath(), baud,0);
				if (mFd == null) {
					st = ERR_HANDLE;
				//	Log.e(TAG, "native open returns null");
					throw new IOException();
				}
				
				mFileInputStream = new FileInputStream(mFd);
				mFileOutputStream = new FileOutputStream(mFd);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new SecurityException();
			}*/
			
			
			try {
				//mSerialPort = new SerialPort(new File(fpath), baud, 0);
				m_serial = new SerialPort(new File(String.valueOf(fpath)), baud, 0);
				
				if(null == m_serial)
					st = ERR_HANDLE;
				else
				{
					m_InputStream = m_serial.getInputStream();
					m_OutputStream = m_serial.getOutputStream();
					
					//protocol = new protocol_mf30w(mFileInputStream,mFileOutputStream);
				}
				
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			st = m_usb.usbOpen();
		}
		
		return st;
		
	}
	
	public int protocol_close(int fport)
	{
		if(port_serial == m_portType)
			m_serial.ReleaseSerial();
		else
			m_usb.usbClose(fport);
		return 0;
	}


	public void s8_uart_genSendBuf(char cmd, char[] userData, int userLen, char[] genBuf, int[] genLen)
	{
		char chk = 0;
		int i,j;
		
		genLen[0] = 1 + 1 + userLen + 1 + 2;//len(cmd) + len(uselen) + userlen + len(chk) + len(ETX) + len(DLE);
		genBuf[0] = FM30TAG;
		genBuf[1]= cmd;
		genBuf[2] = (char)userLen;
		
		for(i=0;i< userLen; i++) genBuf[3+i] =  userData[i];
		for(j=0;j< (1 +1 +userLen+1); j++ )chk ^= genBuf[j];
		genBuf[3+i] = chk;
		genBuf[4+i] = DLE;
		genBuf[5 + i]= ETX;
		
		genLen[0] += 1;//閿熸枻鎷烽敓杈冩唻鎷烽敓锟介敓鏂ゆ嫹 
		
		return;
	}
	
	public void s8_usb_genSendBuf(char cmd, char[] userData, int userLen, char[] genBuf, int[] genLen)
	{
		char chk = 0;
		int i,j;
		
		genLen[0] = 2 + 1 + 1 + userLen + 1;//len(STX) +len(cmd) + len(userData) + userData + len(chk);
		genBuf[0] = (char)(FW30W_USB_VAL_STX>>8 & 0xff);
		genBuf[1] = (char)(FW30W_USB_VAL_STX & 0xff);
		genBuf[2]= cmd;
		genBuf[3] = (char)(userLen);
		
		for(i=0;i< userLen; i++) genBuf[4+i] =  userData[i];
		for(j=0;j< (1 + 1 +1 +userLen); j++ )chk ^= genBuf[1+j];
		genBuf[4+i] = chk;

		
		return;
	}
	
	
	public void genSendBuf(char cmd_serial, char[] userData_serial, int userLen_serial,char cmd_usb, char[] userData_usb, int userLen_usb, char[] genBuf, int[] genLen)
	{
		if(port_serial == m_portType)
			s8_uart_genSendBuf(cmd_serial, userData_serial, userLen_serial,genBuf, genLen);
		else
			s8_usb_genSendBuf(cmd_usb, userData_usb, userLen_usb,genBuf, genLen);
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
	
	public int readStreamByte(InputStream inStream, byte[] pbyte) throws Exception
	{
		
		int result =0;
		int lenInRbuf =0;
		int count =0;
		int max_try = 100;
		
		lenInRbuf = inStream.available();

		while(0 == lenInRbuf)
		{
			
			ShowLog("uart_common_transfer",null,0,"read:::::"+lenInRbuf,1);
		
			if(count < max_try)
			{
				SystemClock.sleep(10);
			}
			else
			{
				ShowLog("uart_common_transfer",null,0,"read:: TIMEOUT!!!!!"+lenInRbuf,1);
				
				
				result = ERR_TIMEOUT;
				break;
			}
		
			count++;
			lenInRbuf = inStream.available();
		}
		
		if(0 == result)
			pbyte[0] = (byte) inStream.read();
		
		return result;
	}
	//pSBuf: AllLen + CMD + UserLen + UserData + CHK + DLE + ETX
	public int s8_uart_common_transfer(char[] pSBuf, int slen, char[] pRBuf, int[] rlen)
	{
		int st = 0, rSt=0;
		byte[] t_rbuf = new byte[512];
		byte[] t_sbuf = new byte[512];
		byte[] tempbuf = new byte[512];
		int t_slen= 1, t_rlen, tempLen = 0;
		int i;
		int count , max_try =100, rParaLen;
		int bFlag = 0;
		int lenInRbuf = 0;
		
		ShowLog("uart_common_transfer",null,0," start========================================================>",1);
		
		try
		{	
			//step 1  
			//send 0x02
			//rec 0x10
			t_sbuf[0]= STX;
			t_slen = 1;
			t_rlen =0;
			count =0;
			
			m_OutputStream.write(t_sbuf, 0, t_slen);
			//ShowLog("uart_common_transfer, Write:",t_sbuf,t_slen,"",0);//test
			m_OutputStream.flush();
				
			try {
				rSt = readStreamByte(m_InputStream,t_rbuf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rSt != 0)
				return rSt;
			
			ShowLog("uart_common_transfer, Read:",t_rbuf,1,"",0);//test
			
			if(DLE != t_rbuf[0])
			{
				return ERR_TRANS;
			}
			
			//step 2
			// send useful data
			for(i=0; i< slen; i++)
			{
				//m_OutputStream.write(pSBuf, i, 1);
				tempbuf[i] = (byte)pSBuf[i];
			}
			m_OutputStream.write(tempbuf, 0, slen);
			ShowLog("uart_common_transfer, Write:",tempbuf,slen,"",0);//test
			
			//step 3 
			//receive 2 byte and check
			for(i=0;i<2;i++)
			{

				try {
					rSt = readStreamByte(m_InputStream,t_rbuf);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(rSt != 0)
					return rSt;
				
				ShowLog("uart_common_transfer, Read:",t_rbuf,1,"",0);//test
			}
			
			//step 4
			//send DLE
			t_sbuf[0] = DLE;
			t_slen = 1;
			m_OutputStream.write(t_sbuf, 0, t_slen);
			ShowLog("uart_common_transfer, Write:",t_sbuf,t_slen,"",0);//test
			
			//step pre-5-1
			// check MF30WTag
			try {
				rSt = readStreamByte(m_InputStream,t_rbuf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rSt != 0)
				return rSt;
			
			ShowLog("uart_common_transfer, Read:",t_rbuf,1,"",0);//test
			
			if(FM30TAG != t_rbuf[0])
			{
				return ERR_TRANS;
			}
			
			//step pre-5-2
			// get operation result flag
			try {
				rSt = readStreamByte(m_InputStream,t_rbuf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rSt != 0)
				return rSt;
			
			st = (int)t_rbuf[0];
			
			ShowLog("uart_common_transfer, Read:",t_rbuf,1,"",0);//test
			
			//step pre-5-3
			//get parmeter length
			try {
				rSt = readStreamByte(m_InputStream,t_rbuf);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rSt != 0)
				return rSt;
			
			ShowLog("uart_common_transfer, Read:",t_rbuf,1,"",0);//test
			
			rParaLen  = (int)t_rbuf[0];
			
			//step 5 
			//receive paramter data
			//t_rlen = 0;
			count =0;
			rlen[0] = rParaLen;
			
			
			
			//while(t_rlen < (rlen[0]+6) && count<max_try)
			t_rlen =0;
			while(t_rlen < rParaLen)
			{
				tempLen =1;
				bFlag =0;

				try {
					rSt = readStreamByte(m_InputStream,tempbuf);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(rSt != 0)
					return rSt;
				
				ShowLog("uart_common_transfer, Read:",tempbuf,1,"",0);//test
				
				//for(i=0;i < tempLen; i++ )
				//	t_rbuf[i+t_rlen] = tempbuf[i];
				if(0 == bFlag)
				{
					pRBuf[t_rlen] =(char) tempbuf[0];
					if(FM30MASK == tempbuf[0])
					{
						bFlag =1;
						
						try {
							rSt = readStreamByte(m_InputStream,tempbuf);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(rSt != 0)
							return rSt;
						
						ShowLog("uart_common_transfer, Read:",tempbuf,1,"",0);//test
					}
				}
				
				t_rlen += tempLen;
				
				//count++;
			}
			
			//step after-5
			//read CHK DLE ETX
			for(i=0;i<3;i++)
			{
				try {
					rSt = readStreamByte(m_InputStream,t_rbuf);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(rSt != 0)
					return rSt;
				
				ShowLog("uart_common_transfer, Read:",t_rbuf,t_rlen,"",0);//test
				
			}
			
			//step 6
			//send DLE
			t_sbuf[0] = DLE;
			t_slen = 1;
			m_OutputStream.write(t_sbuf, 0, t_slen);
			ShowLog("uart_common_transfer, Write:",t_sbuf,t_slen,"",0);//test
		}
		catch(IOException e)
		{
			
		}
		
		return st;
	}
	
	//********************************************/
	//* commond communication with USB func USB
	//* called by other funcs 
	//* 
	//*return: unzero if sucess,0 if failed 
	//********************************************/
	 int s8_fw_usbInfo(int icdev,int slen, char[] sdata,int[] rlen,char[] rdata)
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

		 getbuf[0]=0x00;
		 getbuf[1]=0x00;
		 getbuf[3]=0xff;

		 if(slen< (lenOnceTime + 1))
		 {
			 for(i=0;i<slen;i++)
				 setbuf[i]=sdata[pDataAdr++];
			 return  SENDLAST(icdev, slen, setbuf, rlen, rdata);
		 }
		 //===========================
		 for(i=0;i<lenOnceTime;i++)
			 setbuf[i]=sdata[pDataAdr++];
		 setbuf[0]=USBOVERFLOW;

		 status = m_usb.usb_onceSend(icdev,setbuf,lenOnceTime);
		 if(status !=0) 
			 return ERR_TRANS;
		 
		 alen += lenOnceTime;
		 times = (slen-lenOnceTime)/31;
		 if((slen-lenOnceTime)%31 !=0)times += 1;

		 //times=((slen-lenOnceTime)-1)/(lenOnceTime -1)+1;
		 while(1!=times)
		 {
			 setbuf[0]=USBOVERFLOW;
			 for(i=0;i<(lenOnceTime -1);i++)
				 setbuf[1+i]=sdata[pDataAdr++];;
			 status = m_usb.usb_onceSend(icdev,setbuf,lenOnceTime);
			 if(status != 0)
				 return ERR_TRANS;

			 alen+= (lenOnceTime -1);
			 times--;
		 }
	
		 lastlen=slen-alen;
		 setbuf[0]= (char)(FW30W_USB_VAL_STX >> 8* (FW30W_USB_STXLEN -1));

		 for(i=0;i<lastlen;i++)
			 setbuf[1+i]=sdata[pDataAdr++];;

		 //==========================
		 return  SENDLAST(icdev, slen, setbuf, rlen, rdata);
		
	 }
	 
	 int SENDLAST(int icdev,int slen, char[] sdata,int[] rlen,char[] rdata)
	 {
		 int status;
		 char flag;
		 char[] setbuf = new char[128];
		 char[] getbuf = new char[128];
		 int rrlen;
		 int pRDataAdr = 0, i, alen, times;
		 int lenOnceRev = lenOnceTransUSB;
		 
		 status = m_usb.usb_onceSend(icdev, sdata, lenOnceRev);
		 if(status != 0) 
		 {
			 return ERR_TRANS;
		 }

		 //==========================
		 status = m_usb.usb_onceReceive(icdev, getbuf, lenOnceRev);
		 if(status !=0)
			 return ERR_TRANS;


		 flag=getbuf[FW30W_USB_RP_ADR_SW]; //maybe change
		 if(flag != 0)
			 return flag;//ERR_S8_EXECMD;

		 rlen[0] = (int)(getbuf[FW30W_USB_RP_ADR_RLEN]);

		 rrlen=rlen[0];

		 if(USBOVERFLOW!=getbuf[FW30W_USB_RP_ADR_STX])
		 {
			 if(rrlen > lenOnceRev)
			 {
				 rlen[0] = 0;
				 return ERR_S8_LEN;//
			 }

			 for(i=0;i<rrlen;i++){
				rdata[pRDataAdr++]=getbuf[FW30W_USB_RP_ADR_DATA+i];
			 }
			 return 0;
		 }

		 alen = lenOnceRev - FW30W_USB_RP_ADR_DATA;
		 for(i=0;i<alen;i++)
			 rdata[pRDataAdr++]=getbuf[FW30W_USB_RP_ADR_DATA+i];//maybe change
		 rrlen-=alen;

		 times=(rrlen+FW30W_USB_RP_LEN_CHK)/(lenOnceRev -1);
		 if((rrlen+FW30W_USB_RP_LEN_CHK)%(lenOnceRev -1) !=0 ) times += 1;


		 while(1!=times)
		 {
			 getbuf[0]=0x00;
			 status = m_usb.usb_onceReceive(icdev,getbuf,lenOnceRev);
			 if(status != 0)
				 return ERR_TRANS;

			 for(i=0;i<(lenOnceRev -1);i++)
				 rdata[pRDataAdr++]=getbuf[1+i];

			 rrlen-= (lenOnceRev -1);
			 times--;
		 }

		 getbuf[0]=0x00;
		 status = m_usb.usb_onceReceive(icdev,getbuf, lenOnceRev);
		 if(status !=0)
			 return ERR_TRANS;

		 for(i=0;i<rrlen;i++)
			 rdata[pRDataAdr++]=getbuf[1+i];
		 

		 return 0;
	}
	
	public int common_transfer(int hdev, char[] pSBuf, int slen, char[] pRBuf, int[] rlen)
	{
		if(port_serial == m_portType)
			return s8_uart_common_transfer(pSBuf, slen, pRBuf, rlen);
		else
			return s8_fw_usbInfo(hdev,slen, pSBuf,rlen, pRBuf);
	}

}
