package Lib.LCReader.C1;
import android.content.Context;
import FWPubLib.pl_javacall;
import Lib.General.Convert;
import js3ht.s3ht_javacall;

public class function_C1 {
	
	public static final char PT_USB = 2;
	public static final char PT_SERIAL = 1;
	
	private protocol_C1 mProtocol;
	private char cpuCard_proType;
	private char mCurPortType = PT_SERIAL;
	
	public function_C1(Context hostAct)
	{
		mProtocol = new protocol_C1(hostAct);
	}
	
	public void SetTransPara(int usbTransLenOnce,int vendorID, int productID)
	{
		mProtocol._setTransPara(usbTransLenOnce,vendorID, productID);
	}
	
	public  long  configLogFile(char[] szFileName)
	{
		return s3ht_javacall.configLogFile(szFileName);
	}
	
	public int IC_InitPort_Ex(int portType, char[] fpath, int baud)
	{
		int st = 0;
		
		mCurPortType = (char)portType;
		
		if(mCurPortType == PT_SERIAL)//serial
			st = s3ht_javacall.IC_InitPort_Ex(PT_SERIAL, fpath, baud);
		else
			st = mProtocol.protocol_open(portType, fpath, baud);
		
		return st;
	}
	
	public void IC_ExitComm(int hdev)
	{
		if(mCurPortType == PT_SERIAL)//serial
			s3ht_javacall.IC_ExitComm(hdev);
		else
			mProtocol.protocol_close(hdev);
	}
	
	public int IC_ReadVer(int hdev, char[] version)
	{
		int st = 0,i =0;
		char _cmd_serial = 0xB0, _cmd_usb = 0xB0;
		char[] _userData_serial = new char[10];
		char[] _userData_usb = new char[10];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_ReadVer(hdev,version);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		if(st == 0)
		{
			_rBuf[_rLen[0]] = 0;
		
			for(i=0; i< _rLen[0]; i++)version[i] = _rBuf[i];
		}
		
		return st;
	}
	public int IC_GetRand(int hdev, int randLen, char[] rand_out)
	{
		int st = 0,i =0;
		char _cmd_serial = 0xDA, _cmd_usb = 0xDA;
		char[] _userData_serial = new char[10];
		char[] _userData_usb = new char[10];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		_userLen_serial = 0x01;//serial user len;
		_userData_serial[1] = (char) (randLen);
		_userLen_usb = 0x05;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0x00;
		_userData_usb[4] = (char)randLen;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		if(st == 0)
		{
			for(i=0; i< _rLen[0]; i++)rand_out[i] = _rBuf[i];
		}
		
		return st;
	}
	public int IC_Encrypt_DriverCard(char[] pOut, char[] pIn, int dataLen)
	{
		return (int)(s3ht_javacall.IC_Encrypt_DriverCard(pOut,pIn, dataLen));
	}
	public int IC_HandShake(int hdev, int handShakeDataLen, char[] data_in,
			char[] data_out, char[] dataOutLen)
	{
		int st = 0,i =0;
		char _cmd_serial = 0xDB, _cmd_usb = 0xDB;
		char[] _userData_serial = new char[1024];
		char[] _userData_usb = new char[1024];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		_userLen_serial = handShakeDataLen;//serial user len;
		_userLen_usb = 4 + handShakeDataLen;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0x00;
		
		for(i=0;i< handShakeDataLen;i++)
		{
			_userData_serial[1 + i] = (char)data_in[i];
			_userData_usb[4 + i] = (char)data_in[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		if(st == 0)
		{
			for(i=0; i< _rLen[0]; i++)data_out[i] = _rBuf[i];
		}
		
		return st;
	}
	public int IC_DevBeep(int hdev, int time)
	{
		int st = 0,i =0;
		char _cmd_serial = 0xB2, _cmd_usb = 0xB2;
		char[] _userData_serial = new char[10];
		char[] _userData_usb = new char[10];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_DevBeep(hdev,time);
		}
		
		_userLen_serial = 0x02;//serial user len;
		_userData_serial[1] = (char) (time >>8);
		_userData_serial[2] = (char)(time);
		
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = (char)(time>>8);
		_userData_usb[3] = (char)(time);
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		return st;
	}
	
	public int IC_Status(int ICDev)
	{
		int st = 0,i =0;
		char _cmd_serial = 0xB3, _cmd_usb = 0xB3;
		char[] _userData_serial = new char[10];
		char[] _userData_usb = new char[10];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Status(ICDev);
		}
		
		_userLen_serial = 0x00;//serial user len;
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return (int)(_rBuf[0]);
	}
	
	public int IC_SetCardSeat(int hdev, int seatType)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x40, _cmd_usb = 0x40;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		char  cardType = 0xCA ;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_SetCardSeat(hdev,seatType);
		}
		
		switch(seatType)
		{
		case 0: cardType = 0xCA;break;//CPU
		case 1: cardType = 0xCB;break;//PSAM1
		case 2: cardType = 0xCC;break;//PSAM2
		case 3: cardType = 0xCD;break;//PSAM3
		case 4: cardType = 0xCE;break;//PSAM4
		case 5: cardType = 0xCF;break;//ACPU
		}
		
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x05;//usb user len
		_userData_usb[0] = (char)(0x00);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		_userData_usb[4] = (char)cardType;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public int IC_CpuReset(int hdev, char[] rlen, char[] rdata)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x70, _cmd_usb = 0x70;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_CpuReset(hdev,rlen, rdata);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st)
		{
			if(_rLen[0] <1) return 0xFF;
					
			cpuCard_proType = _rBuf[0];
			
			rlen[0] = (char)(_rLen[0]-1);
					
			for(i=0; i<rlen[0]; i++)
				rdata[i] = _rBuf[1+i];
		}

		
		return st;
	}
	
	public int IC_CpuApdu(int hdev, int slen, char[] sCmd, int[] rlen,  char[] rdata)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x75, _cmd_usb = 0x75;
		char[] _userData_serial = new char[1024];
		char[] _userData_usb = new char[1024];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		char chk =0;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_CpuApdu(hdev,slen, sCmd, rlen, rdata);
		}
		
		_userLen_serial = slen;//serial user len;
		
		if(cpuCard_proType == 0)
		{
			_userLen_usb = 0x04+slen;//usb user len
			_userData_usb[0] = 0x00;
			_userData_usb[1] = 0x00;
			_userData_usb[2] = 0x00;
			_userData_usb[3] = 0x00;
			for(i=0;i<slen;i++)_userData_usb[4+i] =(char)( sCmd[i] & 0xff);
		}
		else
		{
			_userLen_usb = 0x04+3+slen+1;//usb user len
			_userData_usb[0] = 0x00;
			_userData_usb[1] = 0x00;
			_userData_usb[2] = 0x00;
			_userData_usb[3] = 0x00;
			_userData_usb[4] = 0x00;//nad
			_userData_usb[5] = 0x00;//pcb
			_userData_usb[6] = (char)slen;
			for(i=0;i<slen;i++)_userData_usb[7+i] = (char)(sCmd[i] & 0xff);
			for(i=0;i< (3+slen); i++ )chk ^= _userData_usb[4+i];
			_userData_usb[4+3+slen] = chk;
		}

		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st)
		{
			if(_rLen[0] == 0)
				return 0xFE;
						
			for(i=0; i<_rLen[0]; i++)
				rdata[i] =(char)( _rBuf[i] & 0xff);
			
			rlen[0] = _rLen[0];
		}
		
		return st;
	}
	
	public int IC_GetPBOC_PAN(int hdev, int fMode, char[] pPANOut)
	{
		int st = 0,i =0;
		char _cmd_serial = 0xC0, _cmd_usb = 0xC0;
		char[] _userData_serial = new char[1024];
		char[] _userData_usb = new char[1024];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		char chk =0;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return protocol_C1.ERR_NO_SUPPORT_FUNC;
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x05;//usb user len
		_userData_usb[0] = 0x00;
		_userData_usb[1] = 0x00;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0x00;
		_userData_usb[4] = (char)fMode;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		if(0 == st)
		{
			if(_rLen[0] == 0)
				return 0xFE;
						
			for(i=0; i<_rLen[0]; i++)
				pPANOut[i] =(char)( _rBuf[i] & 0xff);
			pPANOut[i] = 0;//end flag
		}
		
		return st;
	}
	public int hex2asc(char[] strasc, char[] strhex, int lenASC)
	{
		Convert obj_convert = new Convert();
		return obj_convert.hex2asc(strasc, strhex, lenASC);
	}
	
	public int asc2hex(char[] strhex, char[] strasc, int lenASC)
	{
		Convert obj_convert = new Convert();
		return obj_convert.asc2hex(strhex, strasc, lenASC);
	}
	
	public int IC_Read (int ICDev,int Offset,int len, char[] rdata)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x41, _cmd_usb = 0x41;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Read(ICDev,Offset, len, rdata);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = (char)(Offset>>8 & 0xff);
		_userData_usb[1] = (char)(Offset & 0xff);
		_userData_usb[2] = (char)(len>>8 & 0xff);
		_userData_usb[3] = (char)(len & 0xff);
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st)
		{	
			for(i=0; i<_rLen[0]; i++)
				rdata[i] = _rBuf[i];
		}
		
		return st;
	}
	
	public int IC_Write (int ICDev, int Offset,int len, char[] DataBuffer)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x42, _cmd_usb = 0x42;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Write(ICDev,Offset, len, DataBuffer);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04 + len;//usb user len
		_userData_usb[0] = (char)(Offset>>8 & 0xff);
		_userData_usb[1] = (char)(Offset & 0xff);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		for(i=0; i<len; i++)_userData_usb[4+i] = (char)DataBuffer[i];
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public int IC_Erase(int lDevice, int offset, int len)
	{
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Erase(lDevice, offset, len);
		}
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}
	public int IC_Choose_24Cxx(int ICDev)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x40, _cmd_usb = 0x40;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Choose_24Cxx(ICDev);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x05;//usb user len
		_userData_usb[0] = (char)(0x00);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		_userData_usb[4] = (char)0xC0;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	private int IC_Check_24C01(int ICDev)
	{
		int st;
		int testRWAddr = 127;
		char[] back_max24c01 = new char[2];
		char[] cmp_max24c01 = new char[2];
		char[] cur_max24c01 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c01);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c01[0] = (char)(back_max24c01[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c01);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c01);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c01[0]&0xff) != (cmp_max24c01[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c01);
		
		return 0;//Success
	}
	private int IC_Check_24C02(int ICDev)
	{
		int st;
		int testRWAddr = 255;
		char[] back_max24c02 =new char[2];
		char[] cmp_max24c02 = new char[2];
		char[] cur_max24c02 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c02);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c02[0] = (char)(back_max24c02[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c02);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c02);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c02[0]&0xff) != (cmp_max24c02[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c02);
		
		return 0;//Success
	}
	private int IC_Check_24C04(int ICDev)
	{
		int st;
		int testRWAddr = 511;
		char[] back_max24c04 = new char[2];
		char[] cmp_max24c04 = new char[2];
		char[] cur_max24c04 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c04);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c04[0] = (char)(back_max24c04[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c04);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c04);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c04[0]&0xff) != (cmp_max24c04[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c04);
		
		return 0;//Success
	}
	private int IC_Check_24C08(int ICDev)
	{
		int st;
		int testRWAddr = 1023;
		char[] back_max24c08 = new char[2];
		char[] cmp_max24c08  = new char[2];
		char[] cur_max24c08 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c08);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c08[0] = (char)(back_max24c08[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c08);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c08);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c08[0]&0xff) != (cmp_max24c08[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c08);
		
		return 0;//Success
	}
	private int IC_Check_24C16(int ICDev)
	{
		int st;
		int testRWAddr = 2047;
		char[] back_max24c16 = new char[2];
		char[] cmp_max24c16 = new char[2];
		char[] cur_max24c16 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c16);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c16[0] = (char)(back_max24c16[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c16);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c16);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c16[0]&0xff) != (cmp_max24c16[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c16);
		
		return 0;//Success
	}
	private int IC_Check_24C32(int ICDev)
	{
		int st;
		int testRWAddr = 4095;
		char[] back_max24c32 = new char[2];
		char[] cmp_max24c32 = new char[2];
		char[] cur_max24c32 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c32);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c32[0] = (char)(back_max24c32[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c32);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c32);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c32[0]&0xff) != (cmp_max24c32[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c32);
		
		return 0;//Success
	}
	
	private int IC_Check_24C64(int ICDev)
	{
		int st;
		int testRWAddr = 8191;
		char[] back_max24c64 = new char[2];
		char[] cmp_max24c64 = new char[2];
		char[] cur_max24c64 = new char[2];
		
		st = IC_Read(ICDev, testRWAddr, 1, back_max24c64);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		cmp_max24c64[0] = (char)(back_max24c64[0] + 1);
		st = IC_Write(ICDev, testRWAddr,1 ,cmp_max24c64);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		st = IC_Read(ICDev, testRWAddr, 1, cur_max24c64);
		if(st != 0)
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		if((cur_max24c64[0]&0xff) != (cmp_max24c64[0]&0xff))
		{
			return protocol_C1.ERR_S3HT_CHECK;
		}
		
		//Write back
		IC_Write(ICDev, testRWAddr,1 ,back_max24c64);
		
		return 0;//Success
	}
	public int IC_Check_24Cxx(int ICDev)
	{
		int st;
		
		st = IC_Check_24C01(ICDev);
		if(st != 0)
			return protocol_C1.ERR_S3HT_CHECK;
		
		st = IC_Check_24C02(ICDev);
		if(st != 0)
		{
			return 1;//CARD_24C01A
		}
		
		st = IC_Check_24C04(ICDev);
		if( st != 0)
		{
			return 2;//CARD_24C02
		}
		
		st = IC_Check_24C08(ICDev);
		if( st != 0)
		{
			return 3;//CARD_24C04
		}
		
		st = IC_Check_24C16(ICDev);
		if( st!= 0)
		{
			return 4;//CARD_24C08
		}
		
		st = IC_Check_24C32(ICDev);
		if( st != 0)
		{
			return 5;//CARD_24C16
		}
		
		st = IC_Check_24C64(ICDev);
		if(st != 0)
		{
			return 6;//CARD_24C32
		}
		
		return protocol_C1.ERR_S3HT_CHECK;
	}
	public int IC_Choose_4442(int ICDev)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x40, _cmd_usb = 0x40;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Choose_4442(ICDev);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x05;//usb user len
		_userData_usb[0] = (char)(0x00);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		_userData_usb[4] = (char)0xC2;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public int IC_ReadCount_SLE4442 (int ICDev)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x6A, _cmd_usb = 0x6A;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_ReadCount_SLE4442(ICDev);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = (char)(0x00);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return _rBuf[0];
	}
	
	public int IC_CheckPass_SLE4442 (int ICDev, char[] Password)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x61, _cmd_usb = 0x61;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(Password.length < 3)return protocol_C1.ERR_S3HT_LEN;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_CheckPass_SLE4442(ICDev,Password);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04 + 3;//usb user len
		_userData_usb[0] = (char)(0x01);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		for(i=0; i<3; i++)_userData_usb[4+i] = Password[i];
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public int IC_ChangePass_SLE4442 (int ICDev, char[] Password)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x62, _cmd_usb = 0x62;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(Password.length < 3)return protocol_C1.ERR_S3HT_LEN;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_ChangePass_SLE4442(ICDev,Password);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04 + 3;//usb user len
		_userData_usb[0] = (char)(0x01);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		for(i=0; i<3; i++)_userData_usb[4+i] = Password[i];
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public int IC_Choose_4428(int ICDev)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x40, _cmd_usb = 0x40;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_Choose_4428(ICDev);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x05;//usb user len
		_userData_usb[0] = (char)(0x00);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		_userData_usb[4] = (char)0xC3;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public int IC_ReadCount_SLE4428 (int ICDev)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x6A, _cmd_usb = 0x6A;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_ReadCount_SLE4428(ICDev);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04;//usb user len
		_userData_usb[0] = (char)(0x00);
		_userData_usb[1] = (char)(0x00);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return _rBuf[0];
	}
	
	public int  IC_CheckPass_SLE4428 (int ICDev,char[] Password)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x61, _cmd_usb = 0x61;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		int keyLe = 2;
		
		if(Password.length < keyLe)return protocol_C1.ERR_S3HT_LEN;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_CheckPass_SLE4428(ICDev,Password);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04 + keyLe;//usb user len
		_userData_usb[0] = (char)(0x03);
		_userData_usb[1] = (char)(0xFE);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		for(i=0; i<keyLe; i++)_userData_usb[4+i] = Password[i];
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}

	public int IC_ChangePass_SLE4428 (int ICDev, char[] Password)
	{
		int st = 0,i =0;
		char _cmd_serial = 0x62, _cmd_usb = 0x62;
		char[] _userData_serial = new char[512];
		char[] _userData_usb = new char[512];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		int keyLe = 2;
		
		if(Password.length < keyLe)return protocol_C1.ERR_S3HT_LEN;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			return s3ht_javacall.IC_ChangePass_SLE4428(ICDev,Password);
		}
		
		_userLen_serial = 0x00;//serial user len;
		
		_userLen_usb = 0x04 + keyLe;//usb user len
		_userData_usb[0] = (char)(0x03);
		_userData_usb[1] = (char)(0xFE);
		_userData_usb[2] = (char)(0x00);
		_userData_usb[3] = (char)(0x00);
		
		for(i=0; i<keyLe; i++)_userData_usb[4+i] = Password[i];
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st = mProtocol.common_transfer(ICDev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	/// functions of atsc102
	public int IC_Choose_102(int lDevice)
	{
		if(mCurPortType  == PT_SERIAL)
		{
			return s3ht_javacall.IC_Choose_102(lDevice);
		}
		
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}
	public int IC_ReadErrorCnt_102(int lDevice, char[] eCnt)
	{
		if(mCurPortType  == PT_SERIAL)
		{
			return s3ht_javacall.IC_ReadErrorCnt_102(lDevice, eCnt);
		}
		
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}
	public int IC_CheckUserCode_102(int lDevice, char[] pPwd)
	{
		if(mCurPortType  == PT_SERIAL)
		{
			return s3ht_javacall.IC_CheckUserCode_102(lDevice, pPwd);
		}
		
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}
	public int IC_CheckFuseCode_102(int lDevice, int iZone, char[] key)
	{
		if(mCurPortType  == PT_SERIAL)
		{
			return s3ht_javacall.IC_CheckFuseCode_102(lDevice, iZone, key);
		}
		
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}
	public int IC_ChangeUserCode_102(int lDevice, char[] newPwd)
	{
		if(mCurPortType  == PT_SERIAL)
		{
			return s3ht_javacall.IC_ChangeUserCode_102(lDevice, newPwd);
		}
		
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}
	public int IC_ChangeFuseCode_102(int lDevice, int iZone, char[] key)
	{
		if(mCurPortType  == PT_SERIAL)
		{
			return s3ht_javacall.IC_ChangeFuseCode_102(lDevice, iZone, key);
		}
		
		return protocol_C1.ERR_NO_SUPPORT_FUNC;
	}


}
