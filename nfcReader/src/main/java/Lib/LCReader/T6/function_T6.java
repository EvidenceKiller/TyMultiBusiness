package Lib.LCReader.T6;

import FWPubLib.pl_javacall;



import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import Lib.Transfer.SerialPort;
import Lib.General.Convert;
import android.content.Context;
import android.util.Log;
import jcumf.umf_javacall;


public class function_T6 {
	

	private protocol_T6 mProtocol;
	public static final char PT_USB = 2;
	public static final char PT_SERIAL = 1;
	private char mCurPortType = PT_SERIAL;
	
	public static final char T_DES = 0x0A;
	public static final char T_AES = 0xAA;
	public static int 	bRedo = 0;
	public static char GL_PCB = 0x12;
	public static char KeyType = T_DES;
	public static byte[] gKeySetting =new byte[2] ;
	public static boolean bPICCKEY = true;
	public static final int CRC_A =  1;
	public static final int CRC_B = 2;
	public static final char FDEL = 0xF2;
	public static char f_SBlock = 0;
	public static char gl_WTXM_byte = 0x00;//used only when S-Block
	
	//global para for mifare plus
	public static final short PLUS_TT	=	9;
	public static final short PLUS_FG	=	52;
	//level 0 command
	public static final char MFPL0_WRITE_PERSO		=0xA8;
	public static final char MFPL0_COMMIT			=0xAA;
	// level 3 command
	public static final char MFPL3_AUTHEN_PART1		=0x70;
	public static final char MFPL3_AUTHEN_PART2		=0x72;
	public static final char MFPL3_AUTHEN_FOLLOW		=0x76;
	public static final char MFPL3_AUTHEN_RESET		=0x78;
	public static final char MFPL3_READ				=0x30; // encrypted NO MAC on response, MAC on cmd
	public static final char MFPL3_READ_MAC			=0x31; // encrypted MAC on response, MAC on cmd
	public static final char MFPL3_READ_PLAIN		=0x32; // plain, NO MAC on response, MAC on cmd
	public static final char MFPL3_READ_PLAINMAC		=0x33; //plain,MAC on response, MAC on cmd
	public static final char MFPL3_READ_NOMAC		=0x34; //encrypted,NO MAC on response, NO MAC on cmd
	public static final char MFPL3_READ_NOMAC_RESP_MAC		=0x35; //encryped,MAC on response,NO MAC on cmd
	public static final char MFPL3_READ_PLAIN_UNMAC			=0x36; //plain, NO MAC on response, NO MAC on cmd
	public static final char MFPL3_READ_PLAIN_UNMAC_RESEP	=0x37;//plain,MAC on response,NO MAC on cmd
	public static final char MFPL3_WRITE				=0xA0; //encrypted,NO MAC on response, MAC on cmd
	public static final char MFPL3_WRITE_MAC			=0xA1; //encrypted, MAC on response, MAC on cmd
	public static final char MFPL3_WRITE_PLAIN		=0xA2; //plain,NO MAC on response, MAC on cmd
	public static final char MFPL3_WRITE_PLAIN_MAC	=0xA3; //plain,MAC on response,MAC on cmd
	public static final char MFPL3_INCREMENT			=0XB0; //incrementing a value encrypted,NO MAC on response,MAC on cmd
	public static final char MFPL3_INCREMENT_MAC		=0xB1; //incrementing a value encrypted, MAC on response, MAC on cmd
	public static final char MFPL3_DECREMENT			=0xB2; //decrement a value encrypted,NO MAC on response,MAC on cmd
	public static final char MFPL3_DECREMENT_MAC		=0xB3; //decrement a value encrypted,MAC on response,MAC on cmd
	public static final char MFPL3_TRANSFER			=0xB4; //Transfer a value,NO MAC on response,MAC on cmd
	public static final char MFPL3_TRANSFER_MAC		=0xB5; //Transfer a value,MAC on response,MAC on cmd
	public static final char MFPL3_INCREMENT_TRANSFER		=0xB6; //combined incrementing and transfer a value encrypted,no mac on response
	public static final char MFPL3_INCREMENT_TRANSFER_MAC	=0xB7;//combined increment and transfer a value encrypted,mac on response
	public static final char MFPL3_DECREMENT_TRANSFER		=0xB8;//combined decrementing and transferring a value encrypted,no mac
	public static final char MFPL3_DECREMENT_TRANSFER_MAC	=0xB9;//combined decrementing and transferring a value encrypted,MAC on response and cmd
	public static final char MFPL3_RESTORE			=0xC2; //restore a value,NO MAC on response and cmd
	public static final char MFPL3_RESTORE_MAC		=0xC3; //restore a value,MAC on response,MAC on cmd

	private char[] gl_key_MAC = new char[17];
	private char[] gl_key_ENC = new char[17];
	private char[] gl_TI = new char[10];
	private short	gl_W_count = 0;
	private short	gl_R_count = 0;
	
	private void test_tripledecry_cbc()
	{
		char[] pKey = new char[32];
		char[] icv = new char[32];
		char[] rndb_hex = (("8B2044355E3183B2")).toCharArray();
		char[] rndb = new char[138];
		char[] catRndABhex =(("2923be84e16cd6ae62484701b8d375f5")).toCharArray();
		char[] cmdSendRndAB = new char[128];
		int st;
		
		st = pl_javacall.tripledecry_CBC(pKey,icv,rndb_hex,16,rndb);//des/3des
		st = pl_javacall.tripledecry_CBC_sendMode(pKey,icv,catRndABhex,32,cmdSendRndAB);
		
		
		return ;
	}
	
	public function_T6(Context hostAct)
	{
		int i;
		mProtocol = new protocol_T6(hostAct);
		
		//for(i=0; i< 10000; i++)
		//test_tripledecry_cbc();//test
	}
	
	public void SetTransPara(int usbTransLenOnce,int vendorID, int productID)
	{
		mProtocol._setTransPara(usbTransLenOnce,vendorID, productID);
	}
	
	public int fw_init_ex(int portType, char[] fpath, int baud)
	{
		int st = 0;
		
		mCurPortType = (char)portType;
		
		if(mCurPortType == PT_SERIAL)//serial
			st = umf_javacall.fw_init_ex(portType, fpath, baud);
		else
			st = mProtocol.protocol_open(portType, fpath, baud);
		
		return st;
	}
	
	public void fw_exit(int hdev)
	{
		if(mCurPortType == PT_SERIAL)//serial
			umf_javacall.fw_exit(hdev);
		else
			mProtocol.protocol_close(hdev);
	}
	
	public int a_hex(char[] strasc, char[] strhex, int lenASC)
	{
		Convert obj_convert = new Convert();
		return obj_convert.asc2hex(strhex, strasc, lenASC);
	}
	
	public int hex_a(char[] strhex, char[] strasc, int lenHex)
	{
		Convert obj_convert = new Convert();
		return obj_convert.hex2asc(strasc, strhex,  lenHex/2);
	}
	
	public short fw_getver(int hdev, char[] version)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x4F, _cmd_usb = 0x80;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_getver(hdev, version);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x0;

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		_rBuf[_rLen[0]] = 0;
		
		if( st ==0 )
		{
			if(_rLen[0] != 0)
				for(i=0; i< _rLen[0]; i++)version[i] = _rBuf[i];
			else st = protocol_T6.ERR_S8_EXECMD;
		}
		
		
		return st;
	}
	
	public short fw_beep(int hdev, int Msec)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x36, _cmd_usb = 0x57;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_beep(hdev, (short)Msec);
		
		_userLen_serial = 0x02;
		_userLen_usb = 0x02;
		
		for(i=0;i<2;i++)
		{
			_userData_serial[i] = _userData_usb[i] = (char)( Msec >> 8*i) ;
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	public short fw_led(int hdev, byte iLed, byte action)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xD4, _cmd_usb = 0xD4;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return protocol_T6.ERR_FUNC;
		
		_userLen_serial = 0x02;
		_userLen_usb = 0x02;
		
		_userData_usb[0] = (char)(iLed) ;
		_userData_usb[1] = (char)action;

		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short _mf30w_swr_eeprom(int hdev, int lOffset, int lLen, char[] pBuffer)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x38, _cmd_usb = 0x38;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 3 + lLen;
		_userLen_usb = 3 + lLen;
		
		pl_javacall.ULToMultiByte(lOffset, _userData_serial, 2, 1);
		pl_javacall.ULToMultiByte(lOffset, _userData_usb, 2, 1);
		_userData_serial[2] = _userData_usb[2] =(char) lLen;
		for(i=0;i<lLen;i++)
		{
			_userData_serial[3+i] = _userData_usb[3+i] = (char)pBuffer[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_swr_eeprom(int hdev, int lOffset, int lLen, char[] pBuffer)
	{
		short st = 0;
		short seclen = 100;
		int i, j;
		short times;//閹跺﹥鏆熼幑顔煎瀻閹存劗娈戝▓鍨殶
		char[] tmpData = new char[seclen + 1];
		int tmpOffset, tmpSendLen;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_swr_eeprom(hdev, lOffset, lLen, pBuffer);
		
		if(lLen == 0) return 0;
		else if(lLen <= seclen)
		{
			return _mf30w_swr_eeprom(hdev, lOffset, lLen, pBuffer);
		}
		else
		{
			times=(short)(lLen/seclen);
			if(lLen%seclen != 0)
				times+=1;
			
			for(i=0;i<times;i++)
			{			
				tmpOffset = lOffset+seclen*i;
				if(i!=(times-1))
				{
					tmpSendLen = seclen;
				}
				else
				{
					tmpSendLen = lLen-seclen*(times-1);
				}
				
				for(j =0; j < tmpSendLen; j++)tmpData[j] = pBuffer[seclen*i + j];
				
				st=_mf30w_swr_eeprom(hdev,tmpOffset,seclen,tmpData);

				if(st != 0) return st;
			}
		}
		
		return st;
	}
	
	public short _mf30w_srd_eeprom(int hdev, int lOffset, int lLen, char[] pBuffer)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x37, _cmd_usb = 0x37;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 3 ;
		_userLen_usb = 3 ;
		
		pl_javacall.ULToMultiByte(lOffset, _userData_serial, 2, 1);
		pl_javacall.ULToMultiByte(lOffset, _userData_usb, 2, 1);
		_userData_serial[2] = _userData_usb[2] =(char) lLen;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(st == 0)
		{
			for(i=0; i< _rLen[0]; i++)
				pBuffer[i] = _rBuf[i];
		}
		
		return st;
	}
	
	public short fw_srd_eeprom(int hdev, int lOffset, int lLen, char[] pBuffer)
	{
		short st = 0;
		short seclen = 60;
		int i, j;
		short times;
		char[] tmpData = new char[seclen + 1];
		int tmpOffset, tmpSendLen;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_srd_eeprom(hdev, lOffset, lLen, pBuffer);
		
		if(lLen == 0) return 0;
		else if(lLen <= seclen)
		{
			return _mf30w_srd_eeprom(hdev, lOffset, lLen, pBuffer);
		}
		else
		{
			times=(short)(lLen/seclen);
			if(lLen%seclen != 0)
				times+=1;
			
			for(i=0;i<times;i++)
			{			
				tmpOffset = lOffset+seclen*i;
				if(i!=(times-1))
				{
					tmpSendLen = seclen;
				}
				else
				{
					tmpSendLen = lLen-seclen*(times-1);
				}
			
				st=_mf30w_srd_eeprom(hdev,tmpOffset,seclen,tmpData);
				
				if(st == 0)
					for(j =0; j < tmpSendLen; j++)pBuffer[seclen*i + j] = tmpData[j] ;

				if(st != 0) return st;
			}
		}
		
		return st;
	}
	
	public void fw_reset_loadkey_para(short _SecNr,char[]_NKey,  char[] pOutKey_serial, char[] pOutKey_usb)
	{
		char[] usb_buf = new char[64];
		char[] serial_buf = new char[64];
		char[] prehex = new char[64];
		int[] tempuint = new int[16];
		int[] kWithUsb ={0x20,0x08,0x12,0x09};
		pl_javacall publib = new  pl_javacall();
		int i;
		
		switch((int)_SecNr)
		{
		case 0:
			{
				usb_buf[0] = serial_buf[0]=0xbd;
				usb_buf[1] = serial_buf[1]=0xde;
				usb_buf[2] =serial_buf[2]=0x6f;
				usb_buf[3] =serial_buf[3]=0x37;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x83;
				break;
			}
		case 1:
			{
				usb_buf[0] = serial_buf[0]=0x14;
				usb_buf[1] = serial_buf[1]=0x8a;
				usb_buf[2] =serial_buf[2]=0xc5;
				usb_buf[3] =serial_buf[3]=0xe2;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x28;
				break;
			}
		case 2:
			{
				usb_buf[0] = serial_buf[0]=0x7d;
				usb_buf[1] = serial_buf[1]=0x3e;
				usb_buf[2] =serial_buf[2]=0x9f;
				usb_buf[3] =serial_buf[3]=0x4f;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x95;
				break;
			}
		case 3:
			{
				usb_buf[0] = serial_buf[0]=0xad;
				usb_buf[1] = serial_buf[1]=0xd6;
				usb_buf[2] =serial_buf[2]=0x6b;
				usb_buf[3] =serial_buf[3]=0x35;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xc8;
				break;
			}
		case 4:
			{
				usb_buf[0] = serial_buf[0]=0xdf;
				usb_buf[1] = serial_buf[1]=0xef;
				usb_buf[2] =serial_buf[2]=0x77;
				usb_buf[3] =serial_buf[3]=0xbb;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xe4;
				break;
			}
		case 5:
			{
				usb_buf[0] = serial_buf[0]=0x09;
				usb_buf[1] = serial_buf[1]=0x84;
				usb_buf[2] =serial_buf[2]=0x42;
				usb_buf[3] =serial_buf[3]=0x21;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xbc;
				break;
			}
		case 6:
			{
				usb_buf[0] = serial_buf[0]=0x5f;
				usb_buf[1] = serial_buf[1]=0xaf;
				usb_buf[2] =serial_buf[2]=0xd7;
				usb_buf[3] =serial_buf[3]=0xeb;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xa5;
				break;
			}
		case 7:
			{
				usb_buf[0] = serial_buf[0]=0x29;
				usb_buf[1] = serial_buf[1]=0x14;
				usb_buf[2] =serial_buf[2]=0x8a;
				usb_buf[3] =serial_buf[3]=0xc5;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x9f;
				break;
			}
		case 8:
			{
				usb_buf[0] = serial_buf[0]=0xfa;
				usb_buf[1] = serial_buf[1]=0xfd;
				usb_buf[2] =serial_buf[2]=0xfe;
				usb_buf[3] =serial_buf[3]=0x7f;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xff;
				break;
			}
		case 9:
			{
				usb_buf[0] = serial_buf[0]=0x73;
				usb_buf[1] = serial_buf[1]=0x39;
				usb_buf[2] =serial_buf[2]=0x9c;
				usb_buf[3] =serial_buf[3]=0xce;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xbe;
				break;
			}
		case 10:
			{
				usb_buf[0] = serial_buf[0]=0xfc;
				usb_buf[1] = serial_buf[1]=0x7e;
				usb_buf[2] =serial_buf[2]=0xbf;
				usb_buf[3] =serial_buf[3]=0xdf;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0xbf;
				break;
			}
		case 11:
			{
				usb_buf[0] = serial_buf[0]=0xcf;
				usb_buf[1] = serial_buf[1]=0xe7;
				usb_buf[2] =serial_buf[2]=0x73;
				usb_buf[3] =serial_buf[3]=0x39;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x51;
				break;
			}
		case 12:
			{
				usb_buf[0] = serial_buf[0]=0xf7;
				usb_buf[1] = serial_buf[1]=0xfb;
				usb_buf[2] =serial_buf[2]=0x7d;
				usb_buf[3] =serial_buf[3]=0x3e;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x5a;
				break;
			}
		case 13:
			{
				usb_buf[0] = serial_buf[0]=0xf2;
				usb_buf[1] = serial_buf[1]=0x79;
				usb_buf[2] =serial_buf[2]=0x3c;
				usb_buf[3] =serial_buf[3]=0x1e;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x8d;
				break;
			}
		case 14:
			{
				usb_buf[0] = serial_buf[0]=0xcf;
				usb_buf[1] = serial_buf[1]=0xe7;
				usb_buf[2] =serial_buf[2]=0x73;
				usb_buf[3] =serial_buf[3]=0x39;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x45;
				break;
			}
		case 15:
			{
				usb_buf[0] = serial_buf[0]=0xb7;
				usb_buf[1] = serial_buf[1]=0xdb;
				usb_buf[2] =serial_buf[2]=0x6d;
				usb_buf[3] =serial_buf[3]=0xb6;
				usb_buf[4] = usb_buf[5] =serial_buf[4]=serial_buf[5]=0x7d;
				break;
			}
			
		default:
			break;
			}
			
			usb_buf[6] =0x78;
			usb_buf[7] =0xef;
			
			serial_buf[6]=0x8c;
			serial_buf[7]=0xf0;
			
			for( i=0;i<4;i++)
			{
				prehex[i]=_NKey[i];
			}
			tempuint[0]=(int)prehex[0];
			tempuint[0]=(int)(tempuint[0]<<8)+(int)prehex[1];
			
			tempuint[1]=(int)prehex[2];
			tempuint[1]=((int)tempuint[1]<<8)+(int)prehex[3];
			
			publib.fwcom_tea(kWithUsb, (char)32, tempuint);
			
			usb_buf[8]= (char)(tempuint[0]>>8);
			usb_buf[9]=(char)(tempuint[0]&0xff);
			usb_buf[10]=(char)(tempuint[1]>>8);
			usb_buf[11]=(char)(tempuint[1]&0xff);
			
			for(i=0;i<4;i++)
			{
				serial_buf[8+i]=prehex[i];
			}
			for( i=0;i<2;i++)
			{
				usb_buf[12+i]=serial_buf[12+i]=_NKey[4+i];
			}
			usb_buf[14]=  0x8e;
			usb_buf[15]= 0xc9; 
			
			serial_buf[14]=0x0c;
			serial_buf[15]=0x00;
			
			for(i=0; i<16; i++)
			{
				pOutKey_serial[i] = serial_buf[i];
				pOutKey_usb[i] = usb_buf[i];
			}
	}
	
	public short fw_load_key(int lDevice,short iMode,short iSecNr,char[] pKey)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x4C, _cmd_usb = 0x2C;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _userData_serial_t = new char[128];
		char[] _userData_usb_t = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_load_key(lDevice, iMode, iSecNr,pKey);
		
		_userLen_serial = 0x12;
		_userLen_usb = 0x12;
		
		_userData_usb[0] = _userData_serial[0] = (char)( iMode);
		_userData_usb[1] = _userData_serial[1] = (char)( iSecNr);
		
		fw_reset_loadkey_para(iSecNr, pKey, _userData_serial_t, _userData_usb_t);
		
		for(i=0; i< 16; i++)
		{
			_userData_usb[2+i] = _userData_usb_t[i];
			_userData_serial[2+i] = _userData_serial_t[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		return st;
	}
	
	public short fw_request(int hdev, short cardMode, int[] tag)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x41, _cmd_usb = 0x41;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_request(hdev, cardMode,  tag);
		
		_userLen_serial = 0x01;
		_userLen_usb = 0x01;
		
		_userData_serial[0] = _userData_usb[0] = (char)cardMode;

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(st != 0)
			return st;
		
		if(_rLen[0] != 2)
			return (short)protocol_T6.ERR_TRANS;
		
		for(i=0; i< _rLen[0]; i++)tag[i] = (byte)_rBuf[i];
		return st;
	}
	
	public short fw_anticoll(int hdev,  short iBcnt,int[] pSnr)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x42, _cmd_usb = 0x42;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_anticoll(hdev, iBcnt, pSnr);
		
		_userLen_serial = 0x05;
		_userLen_usb = 0x05;
		
		_userData_serial[0] = _userData_usb[0] = (char)iBcnt;
		_userData_serial[1] = 0x50;
		_userData_usb[2] = 0xb4;
		_userData_serial[3] = _userData_usb[3] = 0;
		_userData_serial[4] = _userData_usb[4] = 0;
		

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(hdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		if(st == 0)
		{
			if(_rLen[0] != 0)
			{
				pSnr[0] = 0;
				for(i = 0; i<4; i++)
				{
					pSnr[0]  += (int) ((_rBuf[3-i]&0xff) << 8* (3-i) );
				}
			}
			else st = protocol_T6.ERR_S8_EXECMD;

		}
		
		return st;
	}
	
	public short fw_select(int lDevice,int lSnr,short[] pSize)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x43, _cmd_usb = 0x43;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_select(lDevice, lSnr, pSize);
		
		_userLen_serial = 0x04;
		_userLen_usb = 0x04;
		
		for(i=0; i< 4; i++)
		{
			_userData_usb[i] = _userData_serial[i] = (char)( lSnr >> 8*i  & 0xff);
		}
		
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st)pSize[0] = (short)_rBuf[0];
		
		return st;
	}
	
	public short fw_card(int icdev,short _Mode,int[] _Snr)
	{
		short[] size= new short[2];
	    int[] tagtype = new int[2];
	    short st =0;

		st = fw_request(icdev,_Mode,tagtype);

		if(0 == st)
		{
			st = fw_anticoll(icdev,(short)0,_Snr);
			

			if(0 == st)
			{
				st = fw_select(icdev,_Snr[0],size);
			}
		}
		
		return st;
	}
	
	public short fw_card_str(int lDevice,short iMode,char[] pSnr)
	{
		short st, i;
		int[] pSN = new int[2];
		char[] arrSN = new char[8];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_card_str(lDevice, iMode,pSnr);
		
		st = fw_card(lDevice, iMode, pSN);
		if(0 ==st)
		{
			for(i=0; i< 4; i++) arrSN[i] = (char)( pSN[0] >> 8*i  & 0xff);
			hex_a(pSnr,arrSN, 8);
		}
		return st;
	}
	
	public short fw_authentication(int lDevice,short iMod,short iSec)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x44, _cmd_usb = 0x44;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_authentication(lDevice, iMod,iSec);
		
		_userLen_serial = 0x03;
		_userLen_usb = 0x02;
		
		_userData_usb[0] = _userData_serial[0] = (char)( iMod);
		_userData_usb[1] = _userData_serial[1] = (char)( iSec);
		
		if(iSec >15)
		{
			_userData_serial[1] =(char)( iSec%16);
			_userData_serial [2] =(char)( iSec*4);
			if(iSec>=32){
				_userData_serial[2]=(char)((iSec%16)*16+128);
			}
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
	
		
		return st;
	}
	
	public short fw_authentication_pass(int lDevice,short iMod,short iSec, char[] pKey)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x60, _cmd_usb = 0x60;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_authentication_pass(lDevice, iMod,iSec, pKey);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x08;
		
		_userData_usb[0] = _userData_serial[0] = (char)( iMod);
		_userData_usb[1] = _userData_serial[1] = (char)( iSec);
		
		for(i=0; i< 6; i++)_userData_usb[2+i] = pKey[i];
		
		if(iSec >15)
		{
			_userData_serial[1] =(char)( iSec%16);
			_userData_serial [2] =(char)( iSec*4);
			if(iSec>=32){
				_userData_serial[2]=(char)((iSec%16)*16+128);
			}
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
	
		
		return st;
	}
	
	public short fw_write(int lDevice,short iAdr,char[] pData)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x47, _cmd_usb = 0x47;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write(lDevice, iAdr, pData);
		
		if(pData.length < 16)
			return protocol_T6.ERR_S8_LEN;
		
		_userLen_serial = 0x11;
		_userLen_usb = 0x11;
		
		_userData_usb[0] = _userData_serial[0] = (char)iAdr;
		
		for(i=0; i< 16; i++)
		{
			_userData_usb[1+i] = _userData_serial[1+i] = (char)(pData[i]);
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_write_hex(int lDevice, short iAdr, char[] szData)
	{
		short st, i;
		int[] pSN = new int[2];
		char[] arrData = new char[32];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_hex(lDevice, iAdr, szData);
		
		if(szData.length < 16)
			return protocol_T6.ERR_S8_LEN;
		
		st = (short)a_hex(arrData, szData, 16);
		if(st != 0)return protocol_T6.ERR_PARA;
		
		st = fw_write(lDevice, iAdr, arrData);

		return st;
	}
	
	public short fw_read(int lDevice,short iAdr,char[] pData)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x46, _cmd_usb = 0x46;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_read(lDevice, iAdr, pData);
		
		_userLen_serial = 0x01;
		_userLen_usb = 0x01;
		
		_userData_usb[0] = _userData_serial[0] = (char)( iAdr);

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st) for(i=0; i<16; i++)pData[i] = (char)_rBuf[i];
		
		return st;
	}
	
	public short fw_read_hex(int lDevice, short iAdr, char[] szData)
	{
		short st, i;
		char[] arrData = new char[32];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_read_hex(lDevice, iAdr, szData);
		
		if(szData.length < 32)
			return protocol_T6.ERR_S8_LEN;
		
		st = fw_read(lDevice, iAdr, arrData);
		if(0 ==st)
		{
			hex_a(szData,arrData, 32);
		}
		return st;
	}
	
	public short fw_changeKey(int lDevice,short secNr,char[] keyA,char[] ctrlW,char[] keyB)
	{
		short st = 0,i =0;
		char[] _userData = new char[128];
		
		if(keyA.length < 6 || ctrlW.length < 4|| keyB.length <6)
			return protocol_T6.ERR_S8_LEN;
		
		for(i=0; i< 6; i++)
		{
			_userData[i] = keyA[i];
			_userData[10+i] = keyB[i];
		}
		for(i=0; i<4; i++) _userData[6+i] = ctrlW[i];
		
		st = fw_write(lDevice, (short)(secNr*4+3), _userData );
		
		return st;
	}
	
	public short fw_changeKey_hex(int lDevice, short secNr, char[] szKeyA, char[] szCtrlW, char[] szKeyB)
	{
		short st, i;
		int[] pSN = new int[2];
		char[] arrKeyA = new char[32];
		char[] arrCtrlW = new char[32];
		char[] arrKeyB = new char[32];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_changeKey_hex(lDevice, secNr, szKeyA,szCtrlW, szKeyB);
		
		if(szKeyA.length < 12 || szCtrlW.length< 8 || szKeyB.length < 12)
			return protocol_T6.ERR_S8_LEN;
		
		a_hex(arrKeyA, szKeyA, 6);
		a_hex(arrCtrlW, szCtrlW, 4);
		a_hex(arrKeyB, szKeyB, 6);
		
		st = fw_changeKey(lDevice, secNr, arrKeyA, arrCtrlW,arrKeyB);

		return st;
	}
	
	public short fw_initval(int lDevice,short _Adr,int lValue)
	{
		char[] wData =new char[17];
		int i;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_initval(lDevice, _Adr, lValue);

		for(i=0; i<4; i++)
		{
			wData[i] = wData[8+i] = (char)(lValue >> 8*i);
			wData[4+i] = (char)((~lValue) >> 8*i);
		}
		wData[12] = wData[14] = (char)_Adr;
		wData[13] = wData[15] = (char)( (~_Adr) & 0xff);

		return fw_write(lDevice, _Adr, wData);
	}
	
	public short fw_increment(int lDevice,short iAdr,int lValue)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x48, _cmd_usb = 0x48;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_increment(lDevice, iAdr, lValue);
		
		_userLen_serial = 0x05;
		_userLen_usb = 0x05;
		
		_userData_usb[0] = _userData_serial[0] = (char)iAdr;
		
		for(i=0; i< 4; i++)
		{
			_userData_usb[1+i] = _userData_serial[1+i] = (char)(lValue >> 8*i);
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_decrement(int lDevice,short iAdr,int lValue)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x49, _cmd_usb = 0x49;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_decrement(lDevice, iAdr, lValue);
		
		_userLen_serial = 0x05;
		_userLen_usb = 0x05;
		
		_userData_usb[0] = _userData_serial[0] = (char)iAdr;
		
		for(i=0; i< 4; i++)
		{
			_userData_usb[1+i] = _userData_serial[1+i] = (char)(lValue >> 8*i);
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_readval(int lDevice,short _Adr,int[] pValue)
	{
		short flag = 0;
		char[] rData =new char[17];
		int pVal =0;
		int	i;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_readval(lDevice, _Adr, pValue);

		flag = fw_read(lDevice, _Adr, rData);
		if(flag !=0) return flag;

		for(i=0; i< 4;i++)
			pVal +=(int)( (rData[i]& 0xff) << 8*i) ;

		pValue[0] = pVal;

		return 0;
	}
	
	public  short fw_transfer(int lDevice,short iAdr)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x4B, _cmd_usb = 0x4B;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_transfer(lDevice, iAdr);
		
		_userLen_serial = 0x01;
		_userLen_usb = 0x01;
		
		_userData_usb[0] = _userData_serial[0] = (char)iAdr;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_restore(int lDevice,short iAdr)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x4A, _cmd_usb = 0x4A;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_restore(lDevice, iAdr);
		
		_userLen_serial = 0x01;
		_userLen_usb = 0x01;
		
		_userData_usb[0] = _userData_serial[0] = (char)iAdr;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_halt(int lDevice)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x45, _cmd_usb = 0x45;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_halt(lDevice);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	public short fw_reset(int lDevice,int mSec)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x4E, _cmd_usb = 0x4E;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_reset(lDevice, mSec);
		
		_userLen_serial = 0x02;
		_userLen_usb = 0x02;
		
		for(i=0;i<2;i++)
		{
			_userData_serial[i] = _userData_usb[i] = (char)( mSec >> 8* (1 - i)) ;
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	
	///////////// Ultralight card 
	public short fw_read_ultralt(int lDevice,short ipage, char[] pData)
	{
		char[] _rbuf= new char[128];
		short st , i;
		
		st = fw_read(lDevice, ipage, _rbuf);
		if(st  != 0)  return st;
		
		for(i=0; i< 4; i++) pData[i] = _rbuf[i];
		
		return 0;
	}
	
	public short fw_write_ultralt(int lDevice, short ipage, char[] wpData)
	{
		char[] _wbuf= new char[17];
		short st , i;
		
		for(i=0; i< 4; i++) _wbuf[i] = wpData[i];
		
		st = fw_write(lDevice, ipage, _wbuf);
	
		return st;
	}
    //desfire
    public short fw_anticoll2(int lDevice,byte _Bcnt,long[] _Snr)
    {
		short st = 0,i =0;
		char _cmd_serial = 0x00, _cmd_usb = 0xB2;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_anticoll2(lDevice, _Bcnt, _Snr);
		
		_userLen_serial = 0x05;
		_userLen_usb = 0x05;
		
		_userData_usb[0] = (char)_Bcnt;
		_userData_usb[1] = 0x50;
		_userData_usb[2] = 0x00;
		_userData_usb[3] = 0;
		_userData_usb[4] = 0;
		

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		if(st == 0)
		{
			if(_rLen[0] != 0)
			{
				_Snr[0] = 0;
				for(i = 0; i<4; i++)
				{
					_Snr[0]  += (int) ((_rBuf[3-i]&0xff) << 8* (3-i) );
				}
			}
			else st = protocol_T6.ERR_S8_EXECMD;

		}
		
		return st;
    }
    public short fw_select2(int lDevice, long _Snr)
    {
		short st = 0,i =0;
		char _cmd_serial = 0, _cmd_usb = 0xB3;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_select2(lDevice, _Snr);
		
		_userLen_serial = 0x04;
		_userLen_usb = 0x04;
		
		for(i=0; i< 4; i++)
		{
			_userData_usb[i] = _userData_serial[i] = (char)( _Snr >> 8*i  & 0xff);
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
    }
    

    
    public short fw_reset_desfire(int lDevice,byte[] rlen,byte[] rdata)
    {
		short st = 0,i =0;
		char _cmd_serial = 0, _cmd_usb = 0xA5;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_reset_desfire(lDevice, rlen, rdata);
		
		_userLen_serial = 1;
		_userLen_usb = 1;
		
		_userData_usb[0] = 0x60;
		_userData_serial[0] = 0x50;

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		if(0 ==st)
		{
			rlen[0] = (byte)_rLen[0];
			for(i=0; i< rlen[0]; i++)
				rdata[i] = (byte)_rBuf[i];
			
			GL_PCB = 0x02;
		}
		
		return st;
    }
    

    
    public short fw_apdu_desfire(int lDevice,byte slen,byte[] sdata,byte[] rlen,byte[] rdata)
    {
		short st = 0,i =0;
		char _cmd_serial = 0, _cmd_usb = 0xA6;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[4 + slen + 2];
		char[] _cmdBuf = new char[8 + slen + 2];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
	    char tt=0x07;
		char FG = 60;
		char seclen=24;	
		char maxCmdLen = (char)(FG-2);
		int		rOffset = 3;
		byte[] fdele_2 = {0x01};
		int j  = 0, times=0;
		short flag;
		byte[] tmpBuf = new byte[128];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_apdu_desfire(lDevice, slen,sdata,rlen, rdata);
		
		if(slen <= maxCmdLen)
		{
			_userLen_serial = slen + 3;
			_userLen_usb = slen + 4;
			
			_userData_usb[0] = tt;
			_userData_usb[1] = (char)(slen +2);
			_userData_usb[2] = 0x00;
			_userData_usb[3] = GL_PCB;
			for(i=0; i< slen ; i++)
				_userData_usb[4 + i] = (char)sdata[i];
	
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(0 ==st)
			{
				if(_rLen[0] < rOffset)
					return protocol_T6.ERR_S8_EXECMD;
				
				rlen[0] = (byte)(_rLen[0] - rOffset);
				for(i=0; i< rlen[0]; i++)
					rdata[i] = (byte)_rBuf[rOffset + i];
			}
			if((GL_PCB & 1) != 0)GL_PCB &= ~1;
			else GL_PCB |= 1; //閺囧瓨宕�0xX2/0xX3

			if(0xF2 == _rBuf[rOffset-1] && 0x01 == _rBuf[rOffset])
			{
				bRedo = 1;
				GL_PCB = 0xF2;
			}
			else
			{
				GL_PCB &= ~(0xE0);

				if(1 == bRedo)
					GL_PCB = 0x02;

				bRedo = 0;
			}
		
		}
		else
		{
			times = slen / maxCmdLen;
			if(slen % seclen != 0)
				times += 1;
			for(j = 0; j<times; j++)
			{
				if(j != (times-1))
				{
					GL_PCB |= (1<<4);
					for(i =0; i< maxCmdLen; i++)
						tmpBuf[i] = sdata[maxCmdLen*j+i];
					flag = fw_apdu_desfire(lDevice,(byte)maxCmdLen,tmpBuf,rlen, rdata);
				}
				else
				{
					GL_PCB &= ~(1<<4);
					for(i =0; i< slen-maxCmdLen*(times-1); i++)
						tmpBuf[i] = sdata[maxCmdLen*j+i];
					flag = fw_apdu_desfire(lDevice, (byte)(slen-maxCmdLen*(times-1)),tmpBuf, rlen, rdata);
				}
				if(flag != 0)
					return flag;
				
			}
		
		}
		
		while(1 == bRedo)
		{
			if(0xF2 == GL_PCB)
				flag = fw_apdu_desfire(lDevice, (byte)1, fdele_2, rlen, rdata);
			else
				flag = fw_apdu_desfire(lDevice, (byte)0, null, rlen, rdata);
			
			if(flag != 0)
				return flag;
		}
		
		return 0;
		
    }

    public short fw_authen_desfire(int lDevice, byte keyNo, byte[] key,byte[] sessionKey)
    {
		short st = 0,i =0, j=0, count=0;
		char _cmd_serial = 0, _cmd_usb = 0xA5;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[64];
		char[] _rBuf =new char[64];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		short flag;
		byte[] keySetLen = new byte[2];
		byte[] setBuf = new byte[20];
		char[] pKey = new char[17];
		byte[] cmdGetRndb = new byte[3];
		byte[] cmdSendRndAB = new byte[20];
		byte[] rndA = new byte[17];
		char[] rndB = new char[17];
		byte[] rndBre = new byte[17];
		char[] rndAre = new char[64];
		byte[] revlen = new byte[2];
		byte[] revdata = new byte[100];
		char[] icv = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00, 0x00};
		char tmpbuf;
		byte[] catRndAB = new byte[20];
		byte[] catRndABhex = new byte[40];
		char[] rndBhex = new char[20];
		char[] rndAhex = new char[20];
		boolean isSingleDes = true;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_authen_desfire(lDevice, keyNo,key, sessionKey);
		
		//Log.v("fw_authen_desfire", "start++");
		flag=fw_getKeySetting_desfire(lDevice,keySetLen,setBuf);
		if(flag != 0)
			return flag;
		
		gKeySetting[0] = setBuf[0];
		gKeySetting[1] = setBuf[1];
		
		for(j=0; j< 16;j++)
			pKey[j] = (char)key[j];
		
		//step1: GetRndB

		cmdGetRndb[0]=0x0A;
		cmdGetRndb[1]=keyNo;
		flag=fw_apdu_desfire(lDevice,(byte)2,cmdGetRndb,revlen,revdata);
		if(flag != 0) 
			return flag;
		
		
		for(i=0; i< 8; i++)_cmdBuf[i] = (char)revdata[1+i];
		pl_javacall._hex_a(_rBuf,_cmdBuf,16);
		pl_javacall.tripledecry_CBC(pKey,icv,_rBuf,16,rndB);//des/3des
		
		
		//step2: GetRndA
		for( i=0;i<8;i++)
			rndA[i]= (byte)(255 * Math.random());
		
		//step3: revert RndB get RndB~(rndBre)
		tmpbuf=rndB[0];
		for(i=0;i<7;i++)
			rndBre[i]=(byte)rndB[i+1];
		rndBre[i]=(byte)tmpbuf;
		
	    //step4: concat rndA,rndB~ and encry with Des_cbc
		for(i=0;i<8;i++)
		{
			catRndAB[i]=rndA[i];
			catRndAB[8+i]=rndBre[i];
		}
		for(i=0; i< 16; i++)_cmdBuf[i] = (char)catRndAB[i];
		pl_javacall._hex_a(_rBuf,_cmdBuf,32);
		pl_javacall.tripledecry_CBC_sendMode(pKey,icv,_rBuf,32,_cmdBuf);
		for(i=0; i< 16; i++)
			cmdSendRndAB[1+i] = (byte)_cmdBuf[i];
		
		//step5: send next command and get en(RndA~)
		cmdSendRndAB[0]= (byte)0xAF;
		flag = fw_apdu_desfire(lDevice,(byte)17,cmdSendRndAB,revlen,revdata);
		if(flag != 0) 
			return flag;
		
		if(revlen[0]==0x01)
			return protocol_T6.ERR_S8_EXECMD;
		
		
		//step6: get returned rndA and compara it
		for(i=0; i< 8; i++)_cmdBuf[i] = (char)revdata[1+i];
		
		pl_javacall._hex_a(_rBuf,_cmdBuf,16);
		for(i=0; i< 16; i++)
			rndAhex[i] = (char)_rBuf[i];

		pl_javacall.tripledecry_CBC(pKey,icv,rndAhex,16,rndAre);//des/3des


		tmpbuf=rndAre[7];
		for(i=7;i>0;i--)
			rndAre[i]=rndAre[i-1];
		rndAre[0]=tmpbuf;

		for(i=0;i<8;i++)
			if(rndA[i]!= (byte)rndAre[i])
				return protocol_T6.ERR_S8_EXECMD;//0x01 authen error
		
		//step7: save session key
		for(i=0;i<8;i++)
		{
			if(pKey[i]!=pKey[8+i])
			{
				isSingleDes= false;
				break;
			}
		}
		if(isSingleDes)
		{//DES mode
			for( j=0;j<2;j++)
			{
				for(i=0;i<4;i++)
					sessionKey[i]=rndA[i];
				for(i=0;i<4;i++)
					sessionKey[4+i]=(byte)rndB[i];
			}
		}
		else
		{//3DES mode
			for( j=0;j<2;j++)
			{
				for(i=0;i<4;i++)
					sessionKey[count++]=rndA[i+4*j];

				for(i=0;i<4;i++)
					sessionKey[count++]= (byte)rndB[i+4*j];
			}
		}
		

		KeyType = T_DES;

		return 0;
    }
    public short fw_authen_desfire_aes(int icdev, byte keyNo, byte[] key,byte[] sessionKey)
    {
    	char[] tmpChars = new char[128];
    	byte[] cmdGetRndb = new byte[3];
    	char[] cmdSendRndAB = new char[40];
		byte[] rndA = new byte[9];
		char[] rndB = new char[9];
		byte[] rndBre = new byte[9];
		char[] rndAre = new char[9];
		byte[] revlen = new byte[2];
		byte[] revdata = new byte[100];
    	char tmpbuf;
        char[] pkey = new char[17];
    	char[] icv = new char[20];
    	short flag;
    	int i,j;
    	byte[] keySetLen = new byte[2];
    	byte[] setBuf = new byte[128];
    	int count = 0;

		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_authen_desfire_aes(icdev, keyNo,key, sessionKey);

    	flag = fw_getKeySetting_desfire(icdev, keySetLen,setBuf);
    	if(flag != 0) 
    		return flag;
    	
    	gKeySetting[0]=setBuf[0];
    	gKeySetting[1]=setBuf[1];

    	for( j=0;j<16;j++)
    		pkey[j]= (char) key[j];

    	//step1: GetRndB
    	cmdGetRndb[0]= (byte)0xAA;
    	cmdGetRndb[1]= keyNo;
    	flag=fw_apdu_desfire(icdev,(byte)2,cmdGetRndb,revlen,revdata);
    	if(flag != 0) return flag;

    	for(i=0; i<16; i++)
    		rndB[i] = (char)revdata[1 + i];
    	
    	pl_javacall.InvCipher_CBC(icv,pkey,rndB,16);//aes鐟欙絽鐦�

    	for(i=0; i< 16; i++)
    		icv[i] = (char)revdata[1 + i];
   

    	//step2: GetRndA
		for( i=0;i< 16;i++)
			rndA[i]= (byte)(255 * Math.random());
    	
    	//step3: concat rndA,rndB~ and encry with aes_cbc
		for(i=0; i< 16; i++)
			cmdSendRndAB[1+ i] = (char)rndA[i];
		
		for(i=0; i< 15; i++)
			cmdSendRndAB[17 + i] = (char)rndB[1 + i];
		
    	cmdSendRndAB[32] = rndB[0];

    	for(i=0; i< 32; i++)
    		tmpChars[i] = cmdSendRndAB[1 + i];
    	
    	pl_javacall.Cipher_CBC(icv,pkey,tmpChars,32);//aes閸旂姴鐦�

    	//step4: send next command and get en(RndA~)
    	cmdSendRndAB[0]=0xAF;
    	for(i=0; i< 33; i++)
    		setBuf[i] = (byte)cmdSendRndAB[i];
    	
    	flag = fw_apdu_desfire(icdev,(byte)33,setBuf,revlen,revdata);
    	if(flag != 0) return flag;
    	if(revlen[0] ==0x01)
    		return protocol_T6.ERR_S8_EXECMD;//0x01 authen error

    	//step5: get returned rndA and compara it
    	for(i=0; i< 16;i++)
    		rndAre[i] = (char)revdata[1 + i];
    	
    	for(i=0; i< 16; i++)
    		icv[i] = (char)cmdSendRndAB[17 + i];//娴ｈ法鏁k(RndA+RndB')閻ㄥ嫭娓堕崥搴濈缂佸嫭鏆熼幑顔荤稊娑撶k(RndA')閻ㄥ嫯袙鐎靛棗鎮滈柌锟�    	
    	pl_javacall.InvCipher_CBC(icv,pkey,rndAre,16);//aes鐟欙絽鐦�

    	tmpbuf=rndAre[15];
    	for(i=15;i>0;i--)
    		rndAre[i]=rndAre[i-1];  //RndA'缁夎缍呴敍灞界繁閸戠療ndA
    	rndAre[0]=tmpbuf;

    	for(i=0;i<16;i++)
    		if(rndA[i]!=rndAre[i])        //濮ｆ棁绶漅ndA
    			return protocol_T6.ERR_S8_EXECMD;//0x01 authen error

    	//step6: save session key
		for( j=0;j<2;j++)
		{
			for(i=0;i<4;i++)
				sessionKey[count++]=rndA[i+12*j];

			for(i=0;i<4;i++)
				sessionKey[count++]= (byte)rndB[i+12*j];
		}

        KeyType = T_AES;

    	return 0;
		
    }
    public short fw_getver_desfire(int icdev,byte[] rlen,byte[] version)
    {
    	short flag;
    	byte[] cmd={0x60};//command 
    	byte[] framest= {(byte)0xAF};
    	char cmpst;
    	byte[] revdata = new byte[300];
    	byte[] revlen = new byte[2];
    	int i,j, count=0;
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_getver_desfire(icdev, rlen,version);

    	flag = fw_apdu_desfire(icdev,(byte)1,cmd,revlen,revdata);
    	cmpst = (char)revdata[0];
    	rlen [0 ]= (byte)(revlen[0]-1);
    	while(framest[0]==cmpst&& 0==flag)
    	{
    		for(j=0;j< revlen[0]-1;j++)
    			version[count++] = revdata[j+1];
    		flag = fw_apdu_desfire(icdev, (byte)1,framest,revlen,revdata);
    		cmpst= (char)revdata[0];
    		rlen[0] += revlen[0] - 1;
    	}
    	if(0!=flag)
    		return flag;

    	for(i=0;i<revlen[0] -1;i++)
    		version[count++] = revdata[i+1];

    	return 0;
    }
    public short fw_getAIDs_desfire(int lDevice,byte[] rlen,byte[] AIDS)
    {
    	byte[] cmd= {0x6A};//cmd
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[560];
    	int i=1;
    	short flag;
    	int count =0;
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_getAIDs_desfire(lDevice, rlen, AIDS);

    	flag = fw_apdu_desfire(lDevice, (byte)1, cmd, revlen,revdata);
    	if(flag != 0) return flag;

    	if(0!=revdata[0])
    		return revdata[0];
    	
    	revlen[0]--;

    	rlen[0] =revlen[0];
    	while(revlen[0] != 0)
    	{
    		AIDS[count++] = revdata[i];
    		i++;
    		revlen[0]--;
    	}
    	return 0;
    }
    public short fw_selectApp_desfire(int lDevice, byte[] AID)
    {
    	byte[] cmdbuf = new byte[10];
    	byte[] revlen =new byte[2];
    	byte[] revbuf = new byte[10];
    	int i;
    	short flag;

		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_selectApp_desfire(lDevice, AID);
    	
    	cmdbuf[0]=0x5A;//cmd key
    	for(i=1;i<4;i++)
    		cmdbuf[i]=AID[i-1];
    	flag=fw_apdu_desfire(lDevice, (byte)4,cmdbuf, revlen,revbuf);
    	
    	if(flag != 0) 
    		return flag;
    	if(0!=revbuf[0])
    		return revbuf[0];
    	
    	if (cmdbuf[1]==0 && cmdbuf[2]==0 && cmdbuf[3]==0)
    		bPICCKEY = true;
    	else
    		bPICCKEY = false;

    	KeyType = T_DES;

    	return 0;
    }
    public short fw_getKeySetting_desfire(int lDevice, byte[] rlen,byte[] setbuf)
    {
    	byte[] cmd={0x45};//command
    	byte[] revlen = new byte[2];
    	byte[] revbuf = new byte[10];
    	int i;
    	short flag;
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_getKeySetting_desfire(lDevice, rlen, setbuf);
    	
    	flag=fw_apdu_desfire(lDevice,(byte)1,cmd,revlen,revbuf);
    	if(flag !=0) return flag;

    	if(0!=revbuf[0])
    		return revbuf[0];
    	rlen[0] = (byte)(revlen[0] -1);
    	for(i=0;i<revlen[0] -1;i++)
    		setbuf[i]=revbuf[1+i];

    	return 0;
    }
    public short fw_getKeyver_desfire(int lDevice,byte keyNo,byte[] keyVer)
    {
    	byte[] cmdbuf =new byte[4];
    	byte[] revlen = new byte[2];
    	byte[] revbuf = new byte[4];
    	short flag;
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_getKeyver_desfire(lDevice, keyNo, keyVer);

    	cmdbuf[0]=0x64;//cmd key
    	cmdbuf[1]=keyNo;

    	flag=fw_apdu_desfire(lDevice,(byte)2,cmdbuf,revlen,revbuf);
    	if(flag != 0)return flag;

    	if(0!=revbuf[0])
    		return (revbuf[0]);

    	keyVer[0] =revbuf[1];

    	return 0;
    }
    public short fw_createApp_desfire(int lDevice,byte[] AID,byte KeySetting,byte NumOfKey)
    {
    	byte[] cmdbuf = new byte[20];
    	byte[] revlen = new byte[2];
    	byte[] revdata =new byte[30];
    	int i;
    	short flag;
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_createApp_desfire(lDevice, AID, KeySetting, NumOfKey);

    	cmdbuf[0]= (byte)0xCA;//cmd 
    	for(i=1;i<4;i++)
    		cmdbuf[i]= AID[i-1];
    	cmdbuf[4]=KeySetting;
    	cmdbuf[5]=NumOfKey;

    	flag=fw_apdu_desfire(lDevice,(byte)6,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;

    	if(0!=revdata[0])
    		return revdata[0];
    	return 0;
    }
    public short fw_delAID_desfire(int lDevice,byte[] AID)
    {
    	byte[] cmdbuf = new byte[20];
    	byte[] revlen = new byte[2];
    	byte[] revdata =new byte[30];
    	int i;
    	short flag;
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_delAID_desfire(lDevice, AID);

    	cmdbuf[0]= (byte)0xDA;//cmd key
    	for(i=1;i<4;i++)
    		cmdbuf[i]= AID[i-1];
    	flag=fw_apdu_desfire(lDevice, (byte)4,cmdbuf,revlen,revdata);
    	if(flag != 0)
    		return flag;
    	if(0!=revdata[0])
    		return revdata[0];

    	return 0;
    }
    public short fw_changeKeySetting_desfire(int lDevice,byte newSet,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[20];
    	byte[] revlen = new byte[2];
    	byte[] revdata =new byte[30];
    	int i;
    	short flag;
    	char[] tmpbuf =new char[9];
    	char[] icv= new char[9];
    	char[] srvHex = new char[20];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	char[] chSessionKey = new char[128];
    	
    	
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_changeKeySetting_desfire(lDevice, newSet, sessionKey);

    	cmdbuf[0]=0x54;//cmd key
    	cmdbuf[1]=newSet;

    	//Get CRC(2bytes)
    	tmpbuf[0] = (char)cmdbuf[1];
    	pl_javacall.ComputeCrc(CRC_A,tmpbuf,1,chFirst,chSec);
    	cmdbuf[2] = (byte)chFirst[0];
    	cmdbuf[3] = (byte)chSec[0];

    	//padding with 0(5bytes)
    	for( i=0;i<5;i++)
    		cmdbuf[4+i]=0x00;

    	//decipher 8bytes data with sessionkey
    	for(i=0; i< 16; i++)
    		tmpbuf[i] = (char)cmdbuf[1 +i];
    	
    	pl_javacall._hex_a(srvHex,tmpbuf,16);
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] = (char)sessionKey[i];

    	pl_javacall.tripledecry_CBC_sendMode(chSessionKey,icv,srvHex,16,tmpbuf);
    	for(i=0;i<8;i++)
    		cmdbuf[1+i]= (byte) tmpbuf[i];

    	//send command
    	flag=fw_apdu_desfire(lDevice, (byte)9,cmdbuf,revlen,revdata);
    	if(flag !=0 ) return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);

    	return 0;
    }
    public short fw_changeKey_desfire(int lDevice,byte[] sessionKey,byte[] curKey,byte keyNo,byte[] newkey)
    {
    	byte[] cmdbuf = new byte[30];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[30];
    	char[] beforeDes = new char[40];
    	char[] session = new char[17];
    	int i;
    	
    	short flag;
    	int type=0;//deciphered 閺佺増宓侀惃鍕矋閸氬牊鏌熷蹇ョ礉0-娑撱倓閲淐RC,1-娑擄拷閲淐RC
    	char[] pnewkey= new char[128];//newkey;
    	char[] ppnewkey= new char[128];//newkey;
    	char[] xor = new char[20];
    	char[] icv = new char[20];
    	int sendLen;
    	char[] beforeDeshex = new char[50];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	char[] chTmp = new char[128];
    	char[] chTmp2 = new char[128];
    	
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_changeKey_desfire(lDevice,sessionKey, curKey, keyNo, newkey);

    	cmdbuf[0]= (byte)0XC4;//cmd key
    	cmdbuf[1]=  keyNo;
    	for( i=0;i<16;i++)
    	{
    		
    		beforeDes[i] = pnewkey[i] =(char) newkey[i];
    		session[i] = (char)sessionKey[i];
    	}

    	//step0: getkeytype
    	if(0xE==(gKeySetting[0]>>4)||keyNo==(gKeySetting[0]>>4))
    		type=1;

    	//step1: get CRC
    	if(1==type)//閻╁瓨甯寸拋锛勭暬閺傛澘鐦戦惍浣烘畱CRC
    	{
    		for(i=0; i< 16; i++)
    			cmdbuf[2 + i] = (byte)beforeDes[i];
    		
    		if(bPICCKEY && KeyType==T_AES)
    		{
    			for(i=0; i< 18; i++)
    				chTmp[i] = (char)cmdbuf[i];
    				
    			pl_javacall.ComputeCrc32(chTmp2,18,chTmp2);  //娣囶喗鏁奸崡锛勫娑撶粯甯剁�鍡涙寽(AES) to (DES)
    			for(i=0; i< 2; i++)
    				beforeDes[16 + i] = chTmp[i];
    			
    		}
    		else
    		{
    			for(i=0; i< 16; i++)
    				chTmp[i] = (char)newkey[i];
    			pl_javacall.ComputeCrc(CRC_A,chTmp,16,chFirst,chSec);
        		beforeDes[16] = chFirst[0];
        		beforeDes[17] = chSec[0];
    		}
    	
    	}
    	else
    	{
    		//閺傜増妫�鍡欑垳瀵倹鍨ㄩ崥搴礉鐠侊紕鐣婚崗鍓佺波閺嬫粎娈慍RC

    		for(i=0;i<16;i++)
    		{
    			beforeDes[i]=xor[i]= (char)(curKey[i] ^ ppnewkey[i]);
    		}

    		pl_javacall.ComputeCrc(CRC_A,xor,16,chFirst,chSec);
    		//閸愬秷顓哥粻妤佹煀鐎靛棛鐖滈惃鍑淩C
			for(i=0; i< 16; i++)
				chTmp[i] = (char)newkey[i];
    		pl_javacall.ComputeCrc(CRC_A,chTmp,16,chFirst,chSec);

    	}

    	//step2: pading remain with 0
    	if(1==type)
    	for(i=0;i<12;i++)
    	{
    		if(bPICCKEY && KeyType==T_AES)
    			beforeDes[20+i]=0x00;    //娣囶喗鏁奸崡锛勫娑撶粯甯剁�鍡涙寽(AES) to (DES)
    		else
    		    beforeDes[18+i]=0x00;
    	}
    	else
    		for(i=0;i<4;i++)
    			beforeDes[20+i]=0x00;

    	//step3: Des Decry with CBC send mode

    	if(bPICCKEY && KeyType==T_AES)
    	{                              //娣囶喗鏁奸崡锛勫娑撶粯甯剁�鍡涙寽(AES) to (DES)
    		pl_javacall.Cipher_CBC(icv,session,beforeDes,32);
    		for(i=0; i< 32; i++)
    			cmdbuf[2 + i] = (byte)beforeDes[i];
   
    		sendLen = 34;
    	}
    	else
    	{
    		pl_javacall._hex_a(beforeDeshex,beforeDes,48);
    		pl_javacall.tripledecry_CBC_sendMode(session,icv,beforeDeshex,48,chTmp);
    		
    		for(i=0; i< 24; i++ )
    			cmdbuf[2 +i] = (byte)chTmp[i];
    		
    		sendLen = 26;
    	}	

    	//step4: send command
    	flag= fw_apdu_desfire(lDevice, (byte)sendLen,cmdbuf, revlen,revdata);
    	if(flag != 0) return flag;

    	if(0!=revdata[0])
    		return (revdata[0]);

    	return 0;
    }
    public short fw_changeKey_desfire_aes(int lDevice,byte[] sessionKey,byte[] curKey,byte keyNo,byte[] newkey,byte keyVersion)
    {
    	byte[] cmdbuf = new byte[50];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[30];
    	char[] beforeAes = new char[50];
    	char[] session = new char [17];
    	char[] AIDs = new char[200];	
    	int i;
    	
    	short flag;
    	int type=0;//閸旂姴鐦戦弫鐗堝祦閻ㄥ嫮绮嶉崥鍫熸煙瀵骏绱�-娑撱倓閲淐RC,1-娑擄拷閲淐RC
    	
    	char[] pnewkey = new char[128];//newkey;
    	char[] ppnewkey= new char[128];//newkey;
    	char[] xor = new char[20];
    	int sendLen;
    	char[] icv =new char[20];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	char[] chTmp = new char[128];
    	char[] chTmp2 = new char[128];
    	char[] beforeAeshex = new char[60];//娣囶喗鏁奸崡锛勫娑撶粯甯剁�鍡涙寽
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_changeKey_desfire_aes(lDevice,sessionKey, curKey, keyNo, newkey, keyVersion);
    	
    	cmdbuf[0]= (byte)0xC4;//cmd key
    	if(bPICCKEY)
    		cmdbuf[1]= (byte)0x80;//娣囶喗鏁奸崡锛勫娑撶粯甯剁�鍡涙寽
    	else
    		cmdbuf[1]=keyNo;
    	
    	for( i=0;i<16;i++)
    	{
    		beforeAes[i]= pnewkey[i] = (char) newkey[i];
    		session[i]= (char)sessionKey[i];
    	}
    	beforeAes[16]= (char)keyVersion;	
    	
    	//step0: getkeytype
    	if(0xE==(gKeySetting[0]>>4)||keyNo==(gKeySetting[0]>>4))
    		type=1;
    	
    	//step1: get CRC      
    	if(1==type)//閻╁瓨甯寸拋锛勭暬閺傛澘鐦戦惍浣烘畱CRC
    	{		
    		
    		for(i=0; i< 17; i++)
    			cmdbuf[2 +i ] = (byte) beforeAes[i];
    		
    		if(bPICCKEY && KeyType==T_DES)
    		{
    			pl_javacall.ComputeCrc(CRC_A,beforeAes,17,chFirst,chSec);//娣囶喗鏁奸崡锛勫娑撶粯甯剁�鍡涙寽(DES) to (AES)
    			beforeAes[17] = chFirst[0];
    			beforeAes[18] = chSec[0];
    		}
    		else
    		{
    			for(i=0; i< 19; i++)
    				chTmp[i] = (char) cmdbuf[i];
    			
    			pl_javacall.ComputeCrc32(chTmp,19,chTmp2);
    			beforeAes[17] = chTmp2[0];
    			beforeAes[18] = chTmp2[0];
    		}
    	}		
    	else
    	{
    		//閺傜増妫�鍡欑垳瀵倹鍨ㄩ崥搴礉鐠侊紕鐣婚崗鍓佺波閺嬫粎娈慍RC
    		for(i=0;i<16;i++)
    		{
    			beforeAes[i]=xor[i]= (char)( curKey[i] ^  ppnewkey[i]);
    		}
    		
    		for(i=0; i< 17; i++)
    			cmdbuf[2 +i ] = (byte)beforeAes[i];
    		
    		for(i=0; i<19; i++)
    			chTmp[i]  = (char) cmdbuf[i];
    		
    		pl_javacall.ComputeCrc32(chTmp,19,chTmp2);
    		
    		for(i=0; i< 4; i++)
    			beforeAes[17 +i] = chTmp2[i];
    		//閸愬秷顓哥粻妤佹煀鐎靛棛鐖滈惃鍑淩C
    		pl_javacall.ComputeCrc32(pnewkey,16, chTmp);
    		
    		for(i=0; i< 4; i++)
    			beforeAes[21 +i] = chTmp2[i];
    		
    	}
    	
    	//step2: pading remain with 0
    	if(1==type)
    	{
    		for(i=0;i<11;i++)
    		{
    			if(bPICCKEY && KeyType==T_DES)
    				beforeAes[19+i]=0x00;//picc master key
    			else
    				beforeAes[21+i]=0x00;
    		}			
    	}
    	else
    	{
    		for(i=0;i<7;i++)
    			beforeAes[25+i]=0x00;
    	}
    	
    	//step3: Aes Encry with CBC send mode
    	
    	if(bPICCKEY && KeyType==T_DES)
    	{
    		pl_javacall._hex_a(beforeAeshex,beforeAes,48);
    		pl_javacall.tripledecry_CBC_sendMode(session,icv,beforeAeshex,48, chTmp);
    		
    		for(i=0; i< 24; i++)
    			cmdbuf[2 +i] = (byte)chTmp[i];
    		
    		sendLen = 26;
    	}
    	else
    	{
    		pl_javacall.Cipher_CBC(icv,session,beforeAes,32);
    		
    		for(i=0; i< 32; i++)
    			cmdbuf[2 +i] = (byte)beforeAes[i];
    		sendLen = 34;
    	}			
    	
    	//step4: send command
    	flag= fw_apdu_desfire(lDevice,(byte)sendLen,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;
    	
    	if(0!=revdata[0])
    		return (revdata[0]);
    	
    	KeyType = T_DES;
    	
    	return 0;
    }
    public short fw_getFileIDs_desfire(int lDevice, byte[] rlen, byte[] fileIDs)
    {
    	byte[] cmd={0x6F};//cmd key
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[256];
    	int i;
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_getFileIDs_desfire(lDevice,rlen, fileIDs);

    	flag= fw_apdu_desfire(lDevice, (byte)1,cmd,revlen,revdata);
    	if(flag !=0) return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);

    	if(KeyType == T_AES)
    		rlen[ 0] = (byte)(revlen[0] -1-8); //AES妤犲矁鐦夊Ο鈥崇础娑撳绱版径锟芥稉鐙笰C鐎涙濡�
    	else
    		rlen[0] = (byte)(revlen[0]-1);
    	if(0==rlen[0])
    		return 0;

    	for(i=0;i<rlen[0];i++)
    		fileIDs[i]=revdata[i+1];
    	
    	return 0;
    }
    public short fw_getFileProper(int lDevice,byte fileNo,byte[] rlen,byte[] fileProper)
    {

    	byte[] cmdbuf = new byte[3];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[256];
    	int i;
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_getFileProper(lDevice,fileNo, rlen, fileProper);

    	cmdbuf[0]= (byte)0xF5;//cmd key
    	cmdbuf[1]=fileNo;

    	flag= fw_apdu_desfire(lDevice,(byte)2,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);
    

    	if(KeyType == T_AES)
    	{
    		if(revlen[0] < 9)
    			return protocol_T6.ERR_S8_EXECMD;
    		
    		rlen[0]= (byte)(revlen[0]-1-8); //AES妤犲矁鐦夊Ο鈥崇础娑撳绱版径锟芥稉鐙笰C鐎涙濡�
    	}
    	else
    	{
    		if(revlen[0] < 1)
    			return protocol_T6.ERR_S8_EXECMD;
    					
    		rlen[0]= (byte)(revlen[0]-1);
    	}

    	if(0==rlen[0])
    		return 0;
    	for(i=0;i<rlen[0];i++)
    		fileProper[i]=revdata[i+1];

    	return 0;
    }
    public short fw_changeFileSetting(int lDevice,byte fileNo,byte comSet,byte[] accessRight,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[20];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[20];
    	short flag;
    	int i;
    	byte changeAccessRight;
    	byte[] plen = new byte[2];
    	byte[] proper = new byte[60];
        char[] tmpbuf =  new char[9];
    	char[] icv = new char[9];
    	char[] srvHex = new char[17];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	char[] chTmp = new char[128];
    	char[] chTmp2 = new char[128];
    	
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_changeFileSetting(lDevice,fileNo,  comSet, accessRight, sessionKey);

    	cmdbuf[0]=0x5F;//cmd key
    	cmdbuf[1]=fileNo;
    	cmdbuf[2]=comSet;
    	for( i=0;i<2;i++)
    		cmdbuf[3+i]= accessRight[i];

    	//step1 get file proper
    	flag=fw_getFileProper(lDevice,fileNo,plen,proper);
    	if(flag != 0) return flag;
    	changeAccessRight= (byte)(proper[2]&15);

    	//step2: send command depend cases
    	if(14==changeAccessRight)
    	{
    		flag=fw_apdu_desfire(lDevice, (byte)5,cmdbuf,revlen,revdata);
    		if(flag != 0)return flag;
    		if(0!=revdata[0])
    			return (revdata[0]);

    		return 0;
    	}
    	//get crc(2 bytes)
    	for(i=0; i< 3; i++)
    		chTmp[i ] =  (char)cmdbuf[2 +i];
    	
    	
    	
    	pl_javacall.ComputeCrc(CRC_A,chTmp,3, chFirst, chSec);
    	
    	cmdbuf[5] = (byte)chFirst[0];
    	cmdbuf[6] = (byte)chSec[0];
    	
    	
    	//padding with 0(3bytes)
    	for(i=0;i<3;i++)
    		cmdbuf[7+i]=0x00;

    	//decipher with sessionkey
    	for(i=0; i< 8; i++)
    		chTmp[i] = (char) cmdbuf[2 +i];
    	
    	pl_javacall._hex_a(srvHex,chTmp,16);
    	
    	for(i=0; i< 16; i++)
    		chTmp[i] = (char) sessionKey[i];
    	pl_javacall.tripledecry_CBC_sendMode(chTmp,icv,srvHex,16,tmpbuf);

    	for(i=0;i<8;i++)
    		cmdbuf[2+i]= (byte)tmpbuf[i];

    	//send command
    	flag=fw_apdu_desfire(lDevice, (byte)10,cmdbuf,revlen,revdata);
    	if(flag != 0)return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);

    	return 0;
    }
    public short fw_createDataFile_desfire(int lDevice,byte fileNo,byte ComSet,byte[] AccessRight,byte[] FileSize)
    {
    	byte[] cmdbuf = new byte[50];
    	byte[] revlen =new byte[2];
    	byte[] revdata = new byte[40];
    	int i;
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_createDataFile_desfire(lDevice,fileNo,  ComSet, AccessRight,  FileSize);

    	cmdbuf[0]= (byte)0xCD;//cmd key
    	cmdbuf[1]=fileNo;
    	cmdbuf[2]=ComSet;
    	for( i=0;i<2;i++)
    		cmdbuf[3+i]=AccessRight[i];
    	for(i=0;i<3;i++)
    		cmdbuf[5+i]=FileSize[i];

    	flag=fw_apdu_desfire(lDevice ,(byte)8,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;

    	if(0!=revdata[0])
    		return (revdata[0]);


    	return 0;
    }
    public short fw_createBackupDataFile_desfire(int lDevice,byte fileNo,byte ComSet,byte[] AccessRight,byte[] FileSize)
    {
    	byte[] cmdbuf = new byte[50];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[40];
    	int i;
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_createBackupDataFile_desfire(lDevice,fileNo,  ComSet, AccessRight,  FileSize);
    	
    	
    	cmdbuf[0]= (byte)0xCB;//cmd key
    	cmdbuf[1]=fileNo;
    	cmdbuf[2]=ComSet;
    	for( i=0;i<2;i++)
    		cmdbuf[3+i]=AccessRight[i];
    	for(i=0;i<3;i++)
    		cmdbuf[5+i]= FileSize[i];
    	
    	flag=fw_apdu_desfire(lDevice, (byte)8,cmdbuf, revlen,revdata);
    	if(flag != 0) return flag;
    	
    	if(0!=revdata[0])
    		return (revdata[0]);
    	
    	
    	return 0;
    }
    public short fw_createValueFile_desfire(int lDevice,byte fileNo,byte ComSet,byte[] AccessRight,byte[] lowerLimit,byte[] upperLimit,byte[] value,byte creditEnabled)
    {
    	byte[] cmdbuf =new byte[50];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[20];
    	int i;
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_createValueFile_desfire(lDevice,fileNo,  ComSet, AccessRight, lowerLimit, upperLimit,value, creditEnabled);
    	
    	cmdbuf[0]= (byte)0xCC;//cmd key
    	cmdbuf[1]=fileNo;
    	cmdbuf[2]=ComSet;
    	for( i=0;i<2;i++)
    		cmdbuf[3+i]=AccessRight[i];
    	for(i=0;i<4;i++)
    		cmdbuf[5+i]=lowerLimit[i];
    	for(i=0;i<4;i++)
    		cmdbuf[9+i]=upperLimit[i];
    	for(i=0;i<4;i++)
    		cmdbuf[13+i]=value[i];
    	cmdbuf[17]=creditEnabled;

    	flag=fw_apdu_desfire(lDevice,(byte)18,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;

    	if(0!=revdata[0])
    		return (revdata[0]);

    	return 0;
    }
    public short fw_createCsyRecord_desfire(int lDevice,byte fileNo,byte comSet,byte[] AccessRight,byte[] RecordSize,byte[] MaxNum)
    {
    	byte[] cmdbuf =new byte[50];
    	byte[] revlen = new byte[2];
    	byte[] revdata =new byte[20];
    	int i;
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_createCsyRecord_desfire(lDevice,fileNo, comSet, AccessRight, RecordSize, MaxNum);
    	
    	cmdbuf[0]= (byte)0xC0;//cmd key
    	cmdbuf[1]=fileNo;
    	cmdbuf[2]=comSet;
    	for( i=0;i<2;i++)
    		cmdbuf[3+i]=AccessRight[i];
    	for(i=0;i<3;i++)
    		cmdbuf[5+i]=RecordSize[i];
    	for(i=0;i<3;i++)
    		cmdbuf[8+i]= MaxNum[i];

    	flag=fw_apdu_desfire(lDevice, (byte)11,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;

    	if(0!=revdata[0])
    		return (revdata[0]);

    	return 0;
    }
    public short fw_delFile_desfire(int lDevice,byte fileNo)
    {
    	byte[] cmd =new byte[3];//cmd key
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[50];
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_delFile_desfire(lDevice,fileNo);

    	cmd[0]= (byte)0xDF;//cmd key
    	cmd[1]=fileNo;

    	flag=fw_apdu_desfire(lDevice, (byte)2,cmd,revlen,revdata);
    	if(flag != 0) return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);
    	return 0;
    }
    public short fw_write_desfire(int icdev,byte fileNo,int offset,int length,byte[] data,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[8 + length + 2];
    	byte[] revlen =new byte[2];
    	byte[] revdata = new byte[64];
    	short flag;
    	char[] tmpData = new char[8 + length + 2];
    	int k,i;
    	//byte[] pdata= new byte[4096];//data;
    	char[] tempbuf = new char[16];
    	byte[] conBuffer = new byte[4];
    	byte comSet;
    	byte[] plen = new byte[2];
    	byte[] proper = new byte[60];
    	char[] mac = new char[5];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	int padlen;
    	char[] srvHex = new char[2*(8 + length + 2)+1];
    	char[] icv = new char[9];
    	char[] destbuf = new char[length + 2];
    	char[] chSessionKey = new char[64];
    	int pDataAddr = 0;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_write_desfire(icdev,fileNo,offset,length,data,sessionKey);

    	for( k=0;k<(int)length;k++)
    	{
    		tmpData[k]= (char)data[k];
    	    //pdata[k] = data[k];
    	}
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] = (char)sessionKey[i];

    	cmdbuf[0]=0x3D;//cmd key
    	cmdbuf[1]=fileNo;

    	//convert int to unsigned char
    	//sprintf(tempbuf,"%06x",offset);
    	pl_javacall.ULToMultiByte(offset, tempbuf, 3, 1);
    	
    	for( k=0;k<3;k++)
    	{
    		cmdbuf[4-k]= (byte)tempbuf[k];
    	}//
    	
    	pl_javacall.ULToMultiByte(length, tempbuf, 3, 1);
    	for(k=0;k<3;k++)
    		cmdbuf[7-k]=(byte)tempbuf[k];
    	
    	//Get file ComSet
    	flag=fw_getFileProper(icdev,fileNo, plen,proper);
    	if(flag != 0) return flag;

    	comSet=proper[1];
    	
    	if(1==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    	{//MAC
    		
    		for(i=0; i< 16; i++)
    			tempbuf[i] = (char) sessionKey[i];
    		pl_javacall.getMAC( tmpData,length,mac,tempbuf);

    		for(i=0;i<4;i++)
    			tmpData[length+i]=mac[i];

    		length+=4;

    		for(i=0; i< length; i++)
    			data[i] = (byte)tmpData[i];
    	
    	}
    	if(3==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    	{//3(DES)
    		pl_javacall.ComputeCrc(CRC_A,tmpData,length,chFirst,chSec);
    		tmpData[length]= chFirst[0];
    		tmpData[length+1]= chSec[0];

    		length+=2;

    		if(length%8 != 0)
    			padlen=8-(length%8);
    		else
    			padlen=0;

    		for(i=0;i<(int)padlen;i++)
    			tmpData[length+i]=0x00;

    		length+=padlen;

    		pl_javacall._hex_a(srvHex, tmpData,length*2);
    		
    		
    		pl_javacall.tripledecry_CBC_sendMode(chSessionKey,icv,srvHex,length*2,destbuf);

    		for(i=0; i< length; i++)
    			data[i] = (byte)destbuf[i];
    		
    	}
    	//data
    	if(length>52)
    	{
    		for(k=0;k<52;k++)
    			cmdbuf[8+k]=data[k + pDataAddr];
    		pDataAddr+= 52;
    		
    		flag=fw_apdu_desfire(icdev,(byte)(8+52),cmdbuf,revlen,revdata);
    		if(flag != 0)return (flag);
    		
    		if( (int)0xAF != (int)(revdata[0] & 0xff) )
    		{
    			if( 0 != revdata[0])
    				return (revdata[0]);
    		}

    		length-=52;
    		
    		cmdbuf[0]= (byte)0xAF;
    		while(length>59)
    		{
    			for(k=0;k<59;k++)
    				cmdbuf[k+1]= data[ k+ pDataAddr];
    			
    			pDataAddr += 59;
    			
    			flag=fw_apdu_desfire(icdev,(byte)60,cmdbuf,revlen,revdata);
    			if(flag != 0) return flag;
    			if( 0xAF!= (revdata[0] & 0xff) &&0!=revdata[0])
    				return (revdata[0]);
    			length-=59;
    		}
    		if(length>0)
    		{
    			for(k=0;k<(int)length;k++)
    				cmdbuf[k+1]=data[ k+ pDataAddr];
    			
    			pDataAddr += length;
    			
    			flag=fw_apdu_desfire(icdev, (byte)(1+length),cmdbuf,revlen,revdata);
    			if(flag != 0) return flag;
    			if(0xAF!= (revdata[0]& 0xff) &&0!=revdata[0])
    				return (revdata[0]);
    		}
    	}//if(lengt
    	else
    	{//
    		for(k=0;k<(int)length;k++)
    			cmdbuf[k+8]=data[k + pDataAddr];
    		
    		pDataAddr += length;
    		
    		flag=fw_apdu_desfire(icdev, (byte)(8+length),cmdbuf,revlen,revdata);
    		if(flag != 0) return flag;
    		if(0xAf!= (revdata[0] & 0xff) &&0!=revdata[0])
    			return (revdata[0]);
    		
    	}


    	return 0;
    }
    public short fw_read_desfire(int icdev,byte fileNo,int offset,int length,byte[] revData,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[64];//[1024];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[128];//[1024];
    	byte[] framest={ (byte)0xAF,0};

    	//Get file ComSet
    	byte comSet;
    	byte[] plen = new byte[2];
    	byte[] proper = new byte[60];
    	int i,j,k;
    	short flag;
    	char[] tempbuf = new char[8 + length  + 2];
    	byte[] conBuffer =new byte[4];
    	byte cmpst;
    	byte[] tmpData = new byte[8 + length+2];
    	int itmpData=0;
    	byte[] frame= {(byte)0xAF};
    	char[] icv =  new char[9];
    	char[] srvHex = new char[12];
    	char[] destbuf = new char[8 + length  + 2];
    	char[] chSessionKey = new char[64];
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_read_desfire(icdev,fileNo,offset,length,revData,sessionKey);
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] =(char) sessionKey[i];

    	flag=fw_getFileProper(icdev,fileNo,plen,proper);
    	if(flag !=0) return flag;

    	comSet=proper[1];
    	
    	//start to comm
    	cmdbuf[0]= (byte)0xBD;//cmd key
    	cmdbuf[1]=fileNo;

    	//convert int to unsigned char
    	pl_javacall.ULToMultiByte(offset, tempbuf, 3, 1);
    	
    	for( k=0;k<3;k++)
    	{
    		cmdbuf[4-k]= (byte)tempbuf[k];
    	}//
    	
    	pl_javacall.ULToMultiByte(length, tempbuf, 3, 1);
    	for(k=0;k<3;k++)
    		cmdbuf[7-k]=(byte)tempbuf[k];
    	
    	flag=fw_apdu_desfire(icdev, (byte)8,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;
    	if(0xAF!= (revdata[0] & 0xff)&&0!=revdata[0])
    			return (revdata[0]);

        cmpst=revdata[0];

    	while(framest[0]==cmpst&& 0==flag)
    	{
    		for( j=0;j<revlen[0]-1;j++)
    		{
    			tmpData[itmpData]=revdata[j+1];
    			itmpData++;
    		}
    		
    		flag=fw_apdu_desfire(icdev,(byte)1,frame,revlen,revdata);
    		cmpst=revdata[0];
    	}
    	if(0!=flag)
    		return flag;
    	if(0xAF!= (revdata[0] & 0xff)&&0!=revdata[0])
    		return (revdata[0]);

    	for( i=0;i<revlen[0]-1;i++)
    	{
    		tmpData[itmpData]=revdata[i+1];
    		itmpData++;
    	}

    	if(3==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    	{//(3)DES mode file
    		//鐟欙絽鐦�
    		for(i=0; i< itmpData; i++)
    			tempbuf[i] = (char) tmpData[i];
    			
    		pl_javacall._hex_a(srvHex,tempbuf,itmpData*2);
    		pl_javacall.tripledecry_CBC(chSessionKey,icv,srvHex,itmpData*2,destbuf);

    		for(j=0;j<(int)itmpData;j++)
    			tmpData[j]= (byte)destbuf[j];
    		
    	}

    	for(i=0;i<(int)length;i++)
    		revData[i]=tmpData[i];


    	return 0;
    }
    public short fw_getvalue_desfire(int lDevice,byte fileNo,int[] value,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[128];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[200];
    	short flag;
    	int i,j;
    	char comSet;
    	byte[] plen = new byte[2];
    	byte[] proper = new byte[50];
    	char[] icv= new char[9];
    	char[] srvHex = new char[17];
    	char[] destbuf = new char[9];
    	char[] val = new char[5];
    	char[] sbuf = new char[16];
    	char[] chTmp = new char[128];
    	char[] chSessionKey = new char[64];
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_getvalue_desfire(lDevice, fileNo,value,sessionKey);
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] =(char) sessionKey[i];
    	
    	cmdbuf[0]=0x6C;
    	cmdbuf[1]=fileNo;

    	flag=fw_apdu_desfire(lDevice,(byte)2,cmdbuf,revlen,revdata);
    	if(flag != 0)return (flag);
    	if(0!=revdata[0])
    		return (revdata[0]);

    	//Get file ComSet

    	flag=fw_getFileProper(lDevice,(byte)fileNo,plen,proper);
    	if(flag != 0) return flag;

    	comSet= (char)proper[1];
    	if(3==comSet)
    	{//
    		if(0xEE!=proper[2]||0xEE!=proper[3])
    		{//DES鐟欙絽鐦戦弫鐗堝祦

    			for(i=0; i<8; i++)
    				chTmp[i] = (char)revdata[1 +i];
    			pl_javacall._hex_a(srvHex,chTmp,16);
    			pl_javacall.tripledecry_CBC_sendMode(chSessionKey,icv,srvHex,16,destbuf);
    			for(j=0;j<8;j++)
    				revdata[1+j]= (byte)destbuf[j];

    		}
    	}

    	//閹恒儲鏁�娑擃亜鐡ч懞鍌︾礉楠炴儼娴嗛幑顤嶈礋UINT
    	// convert hexArray to long
    	
    	for(i=0;i<4;i++)
    		val[3-i]= (char)revdata[1+i];
    	
    	//get card number
    	pl_javacall.MultiByteToUL(val, 4, 1, value);
    	
    	return 0;
    }
    public short fw_credit_desfire(int icdev,byte fileNo,int value,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[128];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[200];
    	char[] framest= {0xAF};
    	short flag;
    	int i;
    	char comSet;
    	char[] tempbuf = new char[9];
    	char[] conBuffer = new char[5];
    	char[] icv = new char[9];
    	char[] destbuf = new char[9];
    	char[] srvHex = new char[17];
    	char[] chSessionKey = new char[64];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_credit_desfire(icdev, fileNo,value,sessionKey);
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] =(char) sessionKey[i];
    	
    	cmdbuf[0]=0x0C;//cmd key
    	cmdbuf[1]=fileNo;

    	//convert int to unsigned char
    	pl_javacall.ULToMultiByte(value, conBuffer, 4, 0);

    	//LSB mode set value data
    	for( i=0;i<4;i++)
    		cmdbuf[2+i]= (byte)conBuffer[i];

    	//Get file ComSet
    	flag=fw_getFileProper(icdev,fileNo,revlen,revdata);
    	if(flag != 0) return flag;

    	comSet= (char)revdata[1];

    	if(0==comSet||2==comSet)
    	{//plain data mode
    	  flag=fw_apdu_desfire(icdev,(byte)6,cmdbuf,revlen,revdata);
    	  if(flag != 0) return flag;
    	  if(0!=revdata[0])
    		  return(revdata[0]);
    	  
    	}
    	if(1==comSet)//&&(revdata[2]!='e')&&(revdata[3]!='e')&&(revdata[4]!='e')&&(revdata[5]='e'))
    	{//MAC data mode
    		//compute MAC
    		for(i=0; i< 8; i++)
    			tempbuf[i] = (char)cmdbuf[2 +i];
    		pl_javacall.tripleencry_CBC_sendMode(chSessionKey,icv,tempbuf,8,destbuf);

    		//set MAC
    		for(i=0;i<4;i++)
    			cmdbuf[6+i]= (byte)destbuf[i];

    		if((0xEE==revdata[2])&&(0xEE==revdata[3]))
    			flag=fw_apdu_desfire(icdev,(byte)6,cmdbuf,revlen,revdata);
    		else
    			flag=fw_apdu_desfire(icdev,(byte)10,cmdbuf,revlen,revdata);

    		if(flag !=0)
    			return flag;
    		if(0!=revdata[0])
    			return (revdata[0]);
    		
    	}
    	if(3==comSet)
    	{//(3)0DES data mode
    		//compute CRC
    		if(0xEE!=revdata[2]||0xEE!=revdata[3])
    		{
    			for(i=0; i< 4; i++)
    				tempbuf[i] = (char)cmdbuf[2 +i];
    			
    			pl_javacall.ComputeCrc(CRC_A,tempbuf,4, chFirst, chSec);
    			cmdbuf[6]= (byte) chFirst[0];
    			cmdbuf[7]= (byte) chSec[0];
    			//send
    			
    			for(i=0; i< 8; i++)
    				tempbuf[i] = (char) cmdbuf[2 +i];
    			pl_javacall._hex_a(srvHex,tempbuf,16);
    			
    			pl_javacall.tripledecry_CBC_sendMode(chSessionKey,icv,srvHex,16,destbuf);
    			for(i=0;i<8;i++)
    				cmdbuf[2+i]= (byte)destbuf[i];
    			flag=fw_apdu_desfire(icdev,(byte)10,cmdbuf,revlen,revdata);
    		}
    		else
    		{
    			flag=fw_apdu_desfire(icdev,(byte)6,cmdbuf,revlen,revdata);
    		}

    		
    		if(flag != 0)return flag;
    		if(0!=revdata[0])
    			return (revdata[0]);
    	}
    	flag=fw_commitTransfer_desfire(icdev);
    	if(flag != 0) return flag;

    	return 0;
    }
    public short fw_debit_desfire(int icdev,byte fileNo,int value,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[128];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[200];
    	short flag;
    	int i;
    	char comSet;
    	char[] tempbuf = new char[9];
    	char[] conBuffer = new char[5];
    	char[] icv = new char[9];
    	char[] destbuf =new char[9];
    	char[] srvHex = new char[17];
    	char[] chSessionKey = new char[64];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_debit_desfire(icdev, fileNo,value,sessionKey);
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] =(char) sessionKey[i];
    	
    	cmdbuf[0]= (byte)0xDC;//cmd key
    	cmdbuf[1]=fileNo;

    	//convert int to unsigned char
    	pl_javacall.ULToMultiByte(value, conBuffer, 4, 0);

    	//LSB mode set value data
    	for( i=0;i<4;i++)
    		cmdbuf[2+i]= (byte)conBuffer[i];

    	//Get file ComSet
    	flag=fw_getFileProper(icdev,fileNo,revlen,revdata);
    	if(flag != 0) return flag;

    	comSet= (char)revdata[1];

    	if(0==comSet||2==comSet)
    	{//plain data mode
    	  flag=fw_apdu_desfire(icdev,(byte)6,cmdbuf,revlen,revdata);
    	  if(flag != 0) return flag;
    	  if(0!=revdata[0])
    		  return(revdata[0]);
    	  
    	}
    	if(1==comSet)//&&(revdata[2]!='e')&&(revdata[3]!='e')&&(revdata[4]!='e')&&(revdata[5]='e'))
    	{//MAC data mode
    		//compute MAC

    		for(i=0; i<8; i++)
    			tempbuf[i] = (char)cmdbuf[2 + i];
    		pl_javacall.tripleencry_CBC_sendMode(chSessionKey,icv,tempbuf,8,destbuf);

    		//set MAC
    		for(i=0;i<4;i++)
    			cmdbuf[6+i]= (byte)destbuf[i];

    		if((0xEE==revdata[2])&&(0xEE==revdata[3]))
    			flag=fw_apdu_desfire(icdev,(byte)6,cmdbuf,revlen,revdata);
    		else
    			flag=fw_apdu_desfire(icdev,(byte)10,cmdbuf,revlen,revdata);

    		if(flag != 0)
    			return flag;
    		if(0!=revdata[0])
    			return (revdata[0]);
    		
    	}
    	if(3==comSet)
    	{//(3)0DES data mode
    		//compute CRC
    		if(0xEE!=revdata[2]||0xEE!=revdata[3])
    		{
    			for(i=0; i< 4; i++)
    				tempbuf[i] = (char)cmdbuf[2 +i];
    			pl_javacall.ComputeCrc(CRC_A,tempbuf,4,chFirst,chSec);
    			cmdbuf[6]= (byte)chFirst[0];
    			cmdbuf[7]= (byte)chSec[0];
    			//send
    			
    			for(i=0; i< 8; i++)
    				tempbuf[i] = (char)cmdbuf[2 +i];
    			
    			pl_javacall._hex_a(srvHex,tempbuf,16);
    			
    			pl_javacall.tripledecry_CBC_sendMode(chSessionKey,icv,srvHex,16,destbuf);
    			for(i=0;i<8;i++)
    				cmdbuf[2+i]= (byte)destbuf[i];
    			flag=fw_apdu_desfire(icdev,(byte)10,cmdbuf,revlen,revdata);
    		}
    		else
    		{
    			flag=fw_apdu_desfire(icdev,(byte)6,cmdbuf,revlen,revdata);
    		}

    		
    		if(flag != 0)return flag;
    		if(0!=revdata[0])
    			return (revdata[0]);
    	}
    	flag=fw_commitTransfer_desfire(icdev);
    	if(flag!= 0) return flag;

    	return 0;
    }
    public short fw_writeRecord_desfire(int icdev,byte fileNo,int offset,int length,byte[] data,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[8 + length + 2];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[64];
    	short flag ;
    	int i,k;
    	char[] tmpData = new char[8 + length + 2];
    	char[] tempbuf = new char[7];
    	char[] conBuffer =new char[4];
    	byte comSet;
    	byte[] plen = new byte[2];
    	byte[] proper = new byte[60];
    	char[] mac = new char[5];
    	int padlen;
    	char[] srvHex =new char[2*(8 + length + 2) + 2];
    	char[] icv =new char[9];
    	char[] destbuf = new char[8 + length + 2];
    	char[] chSessionKey = new char[64];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	int dataAddr =0;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_writeRecord_desfire(icdev, fileNo,offset,length,data,sessionKey);
    	
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] =(char) sessionKey[i];
    	
    	for( k=0;k<(int)length;k++)
    		tmpData[k] =(char) data[i];

    	cmdbuf[0]=0x3B;//cmd key
    	cmdbuf[1]=fileNo;

    	//convert int to unsigned char

    	pl_javacall.ULToMultiByte(offset, conBuffer, 3, 1);
    	
    	for( k=0;k<3;k++)
    	{
    		cmdbuf[4-k]= (byte)conBuffer[k];
    	}//
    	
    	pl_javacall.ULToMultiByte(length, conBuffer, 3, 1);
    	for(k=0;k<3;k++)
    		cmdbuf[7-k]= (byte)conBuffer[k];
    	
    	//Get file ComSet

    	flag=fw_getFileProper(icdev,fileNo,plen,proper);
    	if(flag != 0) return flag;

    	comSet= proper[1];
    	//閺嶈宓侀弬鍥︽閻ㄥ嫪绗夐崥灞撅拷鐠愵煉绱濋柌宥嗘煀鐠佹儳鐣剧憰浣稿絺闁胶娈戦弫鐗堝祦閸滃ength
    	if(1==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    	{//MAC濡�绱￠惃鍕瀮娴狅拷
    		
    		pl_javacall.getMAC(tmpData,length,mac,chSessionKey);

    		for(i=0;i<4;i++)
    			tmpData[length+i]=mac[i];

    		length+=4;

    		for(i=0; i< length; i++)
    			data[i] = (byte)tmpData[i];
    	}
    	if(3==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    	{//3(DES)濡�绱￠惃鍕瀮娴狅拷

    		pl_javacall.ComputeCrc(CRC_A,tmpData,length, chFirst, chSec);
    		tmpData[length]= chFirst[0];
    		tmpData[length+1]= chSec[0];

    		length+=2;

    		if(length%8 != 0)
    			padlen=8-(length%8);
    		else
    			padlen=0;

    		for(i=0;i<(int)padlen;i++)
    			tmpData[length+i]=0x00;

    		length+=padlen;
    		

    		pl_javacall._hex_a(srvHex,tmpData,length*2);
    		pl_javacall.tripledecry_CBC_sendMode(chSessionKey,icv,srvHex,length*2,destbuf);
    		
    		for(i=0; i< length; i++)
    			data[i] = (byte)destbuf[i];
    		
    	}
    	//data
    	if(length>52)//閼汇儴顪呴崘娆戞畱閺佺増宓佹径褌绨�2娑擃亷绱濋崚娆忓帥閸愶拷2娑擄拷
    	{
    		for(k=0;k<52;k++)
    			cmdbuf[8+k]= data[dataAddr++];
    		flag=fw_apdu_desfire(icdev, (byte)(8+52),cmdbuf,revlen,revdata);
    		if(flag != 0)return (flag);
    		if(0xAF!=revdata[0]&&0!=revdata[0])
    			return (revdata[0]);

    		length-=52;
    		
    		//闁插秷顔昪mdbuf
    		cmdbuf[0]= (byte)0xAF;
    		//婵″倹鐏夐崜鈺�稇閻ㄥ嫬鐡ч懞鍌涙殶婢堆傜艾59娑擃亷绱濋崚娆愮槨濞嗏�鍟撻崗锟�娑撹桨绔寸紒锟�    		while(length>59)
    		{
    			for(k=0;k<59;k++)
    				cmdbuf[k+1]= data[dataAddr++];
    			flag=fw_apdu_desfire(icdev,(byte)60,cmdbuf,revlen,revdata);
    			if(flag != 0) return flag;
    			if(0xAF!=revdata[0]&&0!=revdata[0])
    				return (revdata[0]);
    			length-=59;
    		}
    		if(length>0)
    		{//閸掋倖鏌囨潻妯绘箒濞屸剝婀侀崜鈺�稇鐎涙濡敍宀冨閺堝绱濋崚娆愭付閸氬簼绔村▎鈥冲晸閸忥拷
    			for(k=0;k<(int)length;k++)
    				cmdbuf[k+1]= data[dataAddr++];
    			flag=fw_apdu_desfire(icdev,(byte)(1+length),cmdbuf,revlen,revdata);
    			if(flag != 0) return flag;
    			if(0xAF!=revdata[0]&&0!=revdata[0])
    				return (revdata[0]);
    		}//閹垮秳缍旂紒鎾存将
    	}//if(length>52)//閼汇儴顪呴崘娆戞畱閺佺増宓佹径褌绨�2娑擃亷绱濋崚娆忓帥閸愶拷2娑擄拷
    	else
    	{//
    		for(k=0;k<(int)length;k++)
    			cmdbuf[k+8]=data[dataAddr++];
    		flag=fw_apdu_desfire(icdev,(byte)(8+length),cmdbuf,revlen,revdata);
    		if(flag !=0) return flag;
    		if(0xAf!=revdata[0]&&0!=revdata[0])
    			return (revdata[0]);
    		
    	}
    	//娴ｅ灝鍟撻崗銉ф晸閺侊拷
    	flag=fw_commitTransfer_desfire(icdev);
    	if(flag != 0) return flag;

    	return 0;
    }
    public short fw_readRecord_desfire(int icdev,byte fileNo,int offset,int length,byte[] revData,int[] SgRecordlen,int[] rlen,byte[] sessionKey)
    {
    	byte[] cmdbuf = new byte[128];
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[1024];
    	byte[] framest={(byte)0xAF,0};

    	//Get file ComSet
    	byte comSet;
    	byte[] plen = new byte[2];
    	byte[] proper = new byte[60];
    	char[] maxlen = new char[4];
    	char[] maxlenHex= new char[8];
    	int i,j, k;
    	short flag;
    	char[] tempbuf  =new char[7];
    	char[] conBuffer =new char[4];
    	byte cmpst;
    	char[] tmpData = new char[8 + length + 2];
    	int itmpData=0;
    	byte[] frame= {(byte)0xAF};
    	int datalen;
    	char[] icv = new char[9];
    	char[] srvHex =new char[2*(8 + length + 2) + 2];
    	char[] destbuf =new char[8 + length + 2];
    	char[] chSessionKey = new char[64];
    	char[] chFirst = new char[2];
    	char[] chSec = new char[2];
    	int dataAddr =0;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_readRecord_desfire(icdev, fileNo,offset,length,revData,SgRecordlen,rlen,sessionKey);
    	
    	for(i=0; i< 16; i++)
    		chSessionKey[i] =(char) sessionKey[i];
    	
    	flag=fw_getFileProper(icdev,fileNo,plen,proper);
    	if(flag != 0) return flag;

    	comSet=proper[1];
    	for( k=0;k<3;k++)
    	{
    		maxlen[2-k]= (char)proper[4+k];
    	}
    	
    	pl_javacall.MultiByteToUL(maxlen, 3, 1, SgRecordlen);

    	
    	//start to comm
    	cmdbuf[0]=(byte)0xBB;//cmd key
    	cmdbuf[1]=fileNo;

    	//convert int to unsigned char
    	pl_javacall.ULToMultiByte(offset, conBuffer, 3, 1);
    	
    	for( k=0;k<3;k++)
    	{
    		cmdbuf[4-k]= (byte)conBuffer[k];
    	}//
    	pl_javacall.ULToMultiByte(length, conBuffer, 3, 1);
    	for(k=0;k<3;k++)
    		cmdbuf[7-k]= (byte)conBuffer[k];
    	
    	flag=fw_apdu_desfire(icdev,(byte)8,cmdbuf,revlen,revdata);
    	if(flag != 0) return flag;
    	if(0==revlen[0]&&(revdata[0]!=0xAF))
    		return (revdata[0]);

        cmpst= revdata[0];

    	while(framest[0]==cmpst&& 0==flag)
    	{
    		for(j=0;j<revlen[0]-1;j++)
    		{
    			tmpData[itmpData]= (char)revdata[j+1];
    			itmpData++;
    		}
    		flag=fw_apdu_desfire(icdev,(byte)1,frame,revlen,revdata);
    		cmpst=revdata[0];
    	}
    	if(0!=flag)
    		return flag;
    	
    	if(KeyType == T_AES)
    		revlen[0]= (byte)(revlen[0]-8); //AES

    	for( i=0;i<revlen[0]-1;i++)
    	{
    		tmpData[itmpData]= (char)revdata[i+1];
    		itmpData++;
    	}
    	
    	datalen=itmpData;

    	if(1==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    		datalen=itmpData-4;//閸樼粯甯�娑撶嫻AC閻ㄥ嫭鏆�

    	if(3==comSet&&(0xEE!=proper[2]||0xEE!=proper[3]))
    	{//(3)DES mode file
    		//鐟欙絽鐦�

    		pl_javacall._hex_a(srvHex,tmpData,itmpData*2);
    		pl_javacall.tripledecry_CBC(chSessionKey,icv,srvHex,itmpData*2,destbuf);

    		for(j=0;j<(int)itmpData;j++)
    			tmpData[j]=destbuf[j];

    		//闁插秵鏌婄拋锛勭暬鐎圭偤妾張澶嬫櫏閻ㄥ嫭鏆熼幑顔炬畱娑擃亝鏆�
    		datalen=(itmpData-2-((itmpData-2)%(SgRecordlen[0])));//閸戝繐骞�鐎涙濡惃鍑淩C,鏉╂ɑ婀乸adding閻ㄥ嫪閲滈弫锟�
    		
    	}

    	for(i=0;i<(int)datalen;i++)
    		revData[i]= (byte)tmpData[i];

    	rlen[0] = datalen;


    	return 0;
    }
    public short fw_clearRecord_desfire(int icdev,byte fileNo)
    {
    	byte[] cmdbuf =new byte[4];
    	byte[] revlen =new byte[2];
    	byte[] revdata = new byte[20];
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_clearRecord_desfire(icdev, fileNo);

    	cmdbuf[0]= (byte)0xEB;//cmd key
    	cmdbuf[1]=fileNo;

    	flag=fw_apdu_desfire(icdev,(byte)2,cmdbuf,revlen,revdata);
    	if(flag != 0)return flag;

    	if(0!=revdata[0])
    		return (revdata[0]);

    	flag=fw_commitTransfer_desfire(icdev);
    	if(flag != 0)return flag;

    	return 0;
    }
    public short fw_commitTransfer_desfire(int lDevice)
    {
    	byte[] cmd={(byte)0xC7};//cmd key
    	byte[] revlen = new byte[2];
    	byte[] revdata =new byte[15];
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_commitTransfer_desfire(lDevice);

    	flag=fw_apdu_desfire(lDevice, (byte)1,cmd,revlen,revdata);
    	if(flag != 0)return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);
    	return 0;
    }
    public short fw_abortTransfer_desfire(int lDevice)
    {
    	byte[] cmd={(byte)0xA7};//cmd key
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[15];
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_abortTransfer_desfire(lDevice);

    	flag=fw_apdu_desfire(lDevice, (byte)1,cmd,revlen,revdata);
    	if(flag != 0)return flag;
    	if(0!=revdata[0])
    		return (revdata[0]);
    	return 0;
    }
    public short fw_formatPICC_desfire(int lDevice)
    {
    	byte[] cmd= {(byte)0xFC};//cmd key
    	byte[] revlen = new byte[2];
    	byte[] revdata = new byte[100];
    	short flag;
    	
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_formatPICC_desfire(lDevice);

    	flag=fw_apdu_desfire(lDevice, (byte)1,cmd,revlen,revdata);
    	if(flag != 0) return flag;
    	//if(0!=revdata[0])
    	//	return (revdata[0]);
    	

    	return 0;
    }
	///contact lesss cpu card 
	public short fw_pro_reset(int IDevice,char[] rlen,char[] rbuf)
	{
		
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_pro_reset(IDevice, rlen, rbuf);
    	else
    	{
    		short st = 0,i =0;
    		char _cmd_serial = 0, _cmd_usb = 0xA5;
    		char[] _userData_serial = new char[128];
    		char[] _userData_usb = new char[128];
    		char[] _cmdBuf = new char[1024];
    		char[] _rBuf =new char[1024];
    		int _userLen_serial, _userLen_usb;
    		int[] _genLen = new int[2];
    		int[] _rLen = new int[2];
    		
    		_userLen_serial = 1;
    		_userLen_usb = 1;
    		
    		_userData_usb[0] = 0x50;
    		_userData_serial[0] = 0x50;

    		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
    		
    		st =(short) mProtocol.common_transfer(IDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
    		if(0 ==st)
    		{
    			rlen[0] = (char)_rLen[0];
    			for(i=0; i< rlen[0]; i++)
    				rbuf[i] = (char)_rBuf[i];
    			
    			GL_PCB = 0x02;
    		}
    		
    		return st;
    	}
	}
	

	short _mf30w_pro_apdu_base
	(
	 int ICDev,//
	 int slen,// 
	 char[] sbuff,//
	 int[] rrlen,//
	 char[] rbuff,//
	 short tt,//0ms
	 short FG,//
	 boolean[] fNeedRedo,
	 char[] piccPCBType //pcb type
	 )
	{
	
		int		rOffset = 3;
		boolean bRedo = false;
		int rlen = 0;
		int		flag, datalen_serial,datalen_usb;
		short st = 0,i =0;
		char _cmd_serial = 0, _cmd_usb = 0xA6;
		char[] _userData_serial = new char[1024];
		char[] _userData_usb = new char[1024];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		char piccPCB = 0x00;
		char pcbType = 0x00;//0->I-Block,1->R-Block,2-S-Block
		

		_userLen_serial = slen+3;//FG+1
		_userLen_usb = slen+4;//FG+2;
		
		_userData_usb [0] = (char)tt;
		_userData_usb [1] = (char)(slen+2);//FG;
		_userData_usb [2] = 00;//
		_userData_usb [3] = GL_PCB;
		
		for(i=0; i< slen; i++)
			_userData_usb[4 + i] = sbuff[i];
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(ICDev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		if(0 !=st)
		{
			return st;
		}
		
		rOffset = 3;
		if(_rLen[0] > rOffset)rrlen[0] = _rLen[0] -rOffset;
		else rrlen[0] = 0;
		
		
		for(i=0; i< rrlen[0]; i++)
			rbuff[i] = _rBuf[rOffset + i];
		
		GL_PCB = _rBuf[rOffset-1];
		
		piccPCB = GL_PCB;
		
		if(FDEL != GL_PCB)
		{
			//if( (GL_PCB & 1) != 0)GL_PCB &= ~1;
			//else GL_PCB |= 1; //閺囧瓨宕�0xX2/0xX3
			GL_PCB ^= 1;
		}
		
		//check PCB type 
		if( (piccPCB & 0xC0) == 0xC0) pcbType = 0x02;//S-Block
		else if( (piccPCB & 0x80) == 0x80) pcbType = 0x01;//R-Block
		else pcbType = 0x00; //I-Block

		piccPCBType[0] = pcbType;//+2019.2.25
		switch(pcbType)
		{
		case 0x00://I-Block
		case 0x01://R-Block
			{
				if(FDEL == piccPCB && 0x01 == _rBuf[rOffset])
				{
					rrlen[0] = 0;
					bRedo = true;
					GL_PCB = FDEL;
				}
				else if( (piccPCB & (1<<4)) != 0 )// && (*rrlen == 2)  )
				{
					GL_PCB &= 0x0F;
					GL_PCB |= 0xA0; //閬囧埌杩斿洖鐨凱CB涓�x1X鐨勬椂鍊欙紝涓嬫蹇呴』鍙戦� 0xAY
					bRedo = true;
				}
				else
				{
					GL_PCB &= ~(0xA0);
					bRedo = false;
				}
			}
			break;
		case 0x02://S-Block
			{
				GL_PCB = (char)(piccPCB ^ 0x01);
				if( (rrlen[0])> 0) gl_WTXM_byte = _rBuf[rOffset];
				rrlen[0] = 0;//no valid data
				bRedo = true;
			}
		}
		
		fNeedRedo[0] = bRedo;
	
		return 0;
	
	}
	//2
	short _mf30w_pro_commandlink
	(
	 int ICDev,
	 int slen,
	 char[] sbuff,
	 char[] rrlen,
	 char[] rbuff,
	 short tt,
	 short FG
	 )
	{
		short seclen=24;	
		char maxCmdLen = (char)( FG-2);
		short flag;
		boolean[]		bRedo = {false};
		char[] fdele_2 = {0x01, 0x00};
		int[] _rlen = new int[2];
		char[] _rbuf = new char[1024];
		int allRLen =0;
		int i,j;
		int times;
		char[] tempbuf = new char[maxCmdLen + 2];
		char[] piccPCBtype = new char[2];
		char[] resp_S_WTX =new char[2];

		if(slen <= maxCmdLen)
		{
			flag=_mf30w_pro_apdu_base(ICDev,slen,sbuff,_rlen,rbuff,tt,FG,bRedo,piccPCBtype);
			if(flag != 0) return flag;

			allRLen += _rlen[0];
		}
		else
		{

			times=slen/maxCmdLen;
			if(slen%seclen != 0)
				times+=1;
			
			for(i=0;i<times;i++)
			{			
				if(i!=(times-1))
				{
					GL_PCB |= (1<<4); //0x1X
					
					for(j=0; j< maxCmdLen; j++)
						tempbuf[j] = sbuff[maxCmdLen*i + j];
					
					flag=_mf30w_pro_apdu_base(ICDev,maxCmdLen,tempbuf,_rlen,_rbuf,tt,FG,bRedo,piccPCBtype);
				}
				else 
				{
					GL_PCB &= ~(1<<4);//0x0X or 0xAX
					
					for(j=0; j< slen-maxCmdLen*(times-1); j++)
						tempbuf[j] = sbuff[maxCmdLen*i + j];
					
					flag=_mf30w_pro_apdu_base(ICDev,(char)(slen-maxCmdLen*(times-1)),tempbuf,_rlen,_rbuf,tt,FG,bRedo,piccPCBtype);
				}
				if(flag !=0) return flag;
				
				for(j=0; j < _rlen[0]; j++)
					rbuff[allRLen + j] = _rbuf[j];
				
				allRLen += _rlen[0];
			}

		}

		while(true == bRedo[0])
		{
			//+2019.2.22 ++
			switch(piccPCBtype[0])
			{
			case 0x00://I-Block
			case 0x01://R-Block 
			default: 
				{
					if(FDEL == GL_PCB) 
						flag=_mf30w_pro_apdu_base(ICDev,1,fdele_2,_rlen,_rbuf,tt,FG,bRedo,piccPCBtype);
					else
						flag=_mf30w_pro_apdu_base(ICDev,0, null,_rlen,_rbuf,tt,FG,bRedo,piccPCBtype);
				}
				break;
			case 0x02://S-Block
				resp_S_WTX[0] =  (char)(gl_WTXM_byte & 0x3F);
				flag = _mf30w_pro_apdu_base(ICDev, 1, resp_S_WTX, _rlen, _rbuf, tt, FG, bRedo, piccPCBtype);
				break;

			}
			if(flag != 0) return flag;
			
			for(j=0; j < _rlen[0]; j++)
				rbuff[allRLen + j] = _rbuf[j];
			
			allRLen += _rlen[0];
		}

		rrlen[0] = (char)allRLen;

		return 0;

	}
	public short fw_pro_commandlink(int IDevice,short slen,char[] sbuff,char[] rlen,char[] rbuf,short tt,short fg)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return umf_javacall.fw_pro_commandlink(IDevice, slen, sbuff, rlen, rbuf, tt, fg);
    	else
    		return _mf30w_pro_commandlink(IDevice, (int)slen, sbuff, rlen, rbuf, tt, fg);
	}
	//===============================================
	//  Mifare Plus  Commond Functions
	//===============================================
	/*
	int  fw_MFPlusL0_WritePerso(int ihic, unsigned char* key, 
								unsigned short keyAddr)
	{
		return _mf30w_MFPlusL0_WritePerso(ihic, key, keyAddr);
	}

	int  fw_MFPlusL0_CommitPerso(int ihic)
	{
		return _mf30w_MFPlusL0_CommitPerso(ihic);
	}
	int  fw_MFPlusL1_AuthenKeyL1(int ihic,unsigned char* key)
	{
		return _mf30w_MFPlusL1_AuthenKeyL1(ihic, key);
	}
	int  fw_MFPlusL1_SwitchToL2(int ihic,unsigned char* key)
	{
		return _mf30w_MFPlusL1_SwitchToL2(ihic, key);
	}
	int  fw_MFPlusL1_SwitchToL3(int ihic,unsigned char* key)
	{
		return _mf30w_MFPlusL1_SwitchToL3(ihic, key);
	}
	int  fw_MFPlusL2_SwitchToL3(int ihic,unsigned char* key)
	{
		return _mf30w_MFPlusL2_SwitchToL3(ihic, key);
	}*/
	public short  fw_MFPlusL3_AuthenL3Key(int ihic,char[] key, short keyAddr)
	{
		short				ret = Lib.Error.ErrorCode.ERR_OK;
		char[]	bufCmd =new char[1024];
		char[]	rBuf = new char[1024];
		int				cmdLen;
		char[] 	rlen = new char[4];
		char[]	bufRndB = new char[16];
		char[]	bufRndA = new char[16];
		char[]	bufRndA2 = new char[16];
		char[]	bufRnd = new char[32];
		char[]	bufIv = new char[16];
		int				i,j, itmp;
		char[]	base_K_MAC = new char[17];
		char[]	base_K_ENC = new char[17];

		/* clear K_MAC , K_ENC */
		//memset(gl_key_MAC,0,sizeof(gl_key_MAC));
		//memset(gl_key_ENC,0,sizeof(gl_key_ENC));

		/* reset W_Ctr, R_Ctr, TI */
		//memset(gl_TI,0,4);
		gl_W_count = gl_R_count = 0;

		/*   the first Authenication */
		//prepare PCD
		//----------------------
		bufCmd[0] = MFPL3_AUTHEN_PART1;
		bufCmd[1] = (char)(keyAddr & 0xff);
		bufCmd[2] = (char)( (keyAddr >> 8) & 0xff);
		bufCmd[3] = 0x00; //PCD length, here set null

		cmdLen = 4;

		//------------------------
		ret = _mf30w_pro_commandlink(ihic,cmdLen,bufCmd,rlen,rBuf,PLUS_TT,PLUS_FG);
		if(ret !=0)
			return ret;

		if(Lib.Error.ErrorCode.ERR_PLUS_CMDOK != rBuf[0] && Lib.Error.ErrorCode.ERR_PLUS_OK != rBuf[0])
			return Lib.Error.ErrorCode.ERR_TRANS;

		for(i=0; i< 16; i++)bufRndB[i] = rBuf[1 + i];
		pl_javacall.InvCipher_ECB(key, bufRndB, 16);

	    /*  cyc RandA */
		for(i=0; i< 4; i++)
		{
			itmp = (int)Math.random();

			for(j=0; j<4; j++)
				bufRndA[i*4 + j] = (char)((itmp >> j)&0xff);
		}

		for(i=0; i< 16; i++)bufRnd[i] = bufRndA[i];
		//memcpy(bufRnd,bufRndA,16);
		for(i=0; i< 15; i++)bufRnd[16 + i] = bufRndB[1+i];
		//memcpy(&bufRnd[16],&bufRndB[1],15);
		bufRnd[31] = bufRndB[0];

		pl_javacall.Cipher_CBC(bufIv,key, bufRnd,32);

		/*   the Authenication followed*/
		//prepare PCD
		//----------------------
		bufCmd[0] = MFPL3_AUTHEN_PART2;
		for(i=0; i< 32; i++) bufCmd[1+i] = bufRnd[i];
		//memcpy(&bufCmd[1], bufRnd, 32);

		cmdLen = 33;
		//------------------------
		ret = _mf30w_pro_commandlink(ihic,cmdLen,bufCmd,rlen,rBuf,PLUS_TT,PLUS_FG);
		if(ret !=0)
			return ret;

		if(Lib.Error.ErrorCode.ERR_PLUS_CMDOK != rBuf[0] && Lib.Error.ErrorCode.ERR_PLUS_OK != rBuf[0])
			return Lib.Error.ErrorCode.ERR_TRANS;

		/* verify RndA' */
		for(i=0; i< 32;i++) bufRnd[i] = rBuf[i];
		//memcpy(bufRnd, &rBuf[1], 32);
		pl_javacall.InvCipher_CBC(bufIv,key, bufRnd, 32);

		bufRndA2[0] = bufRnd[19];
		for(i=0;i<15;i++) bufRndA2[1+i] = bufRnd[4+i];
		//memcpy(&bufRndA2[1], &bufRnd[4], 15);

		for(i=0; i<16;i++) if(bufRndA[i] != bufRndA2[i])  
		//if(memcmp(bufRndA, bufRndA2, 16))
			return Lib.Error.ErrorCode.ERR_PLUS_CRC_2;

		/* save gl_TI */
		for(i=0;i<4;i++)gl_TI[i] = bufRnd[i];
		//memcpy(gl_TI, bufRnd, 4);

		/* reconfig K_MAC, K_ENC 
		 *  杩欓噷鐨刡ufRndA,bufRndB閮芥槸楂樹綅鍦ㄥ墠锛屾寜 B15-B14...B0鐨勯『搴忔帓鍒�
		*/

		/* K_MAC */
		for(i=0; i<5;i++)base_K_MAC[i] = bufRndA[7 +i];
		//memcpy(base_K_MAC, &bufRndA[7],5);//F
		for(i=0; i<5; i++)base_K_MAC[5+i] = bufRndB[7 +i];
		//memcpy(&base_K_MAC[5],&bufRndB[7],5);//G
		for(i=0; i<5;i++)
			base_K_MAC[10 +i] = (char)(bufRndA[i] ^ bufRndB[i]);//J
		base_K_MAC[15] = 0x22;

		pl_javacall.Cipher_ECB(key, base_K_MAC,16);
		//memcpy(gl_key_MAC,base_K_MAC,16);
		for(i=0; i< 16;i++)gl_key_MAC[i] = base_K_MAC[i];

		/* K_ENC */
		for(i=0; i<5;i++) base_K_ENC[i] = bufRndA[11 +i];
		//memcpy(base_K_ENC, &bufRndA[11],5);//F
		for(i=0; i<5; i++) base_K_ENC[5 +i] = bufRndB[11 +i];
		//memcpy(&base_K_ENC[5],&bufRndB[11],5);//G
		for(i=0; i<5;i++)
			base_K_ENC[10 +i] = (char)(bufRndA[4+i] ^ bufRndB[4+i]);
		base_K_ENC[15] = 0x11;

		pl_javacall.Cipher_ECB(key, base_K_ENC,16);
		for(i=0; i< 16; i++)gl_key_ENC[i] = base_K_ENC[i];
		//memcpy(gl_key_ENC,base_K_ENC,16);

		return Lib.Error.ErrorCode.ERR_OK;
	}
	/*
	int  fw_MFPlusL3_AuthenSectorKey(int ihic,unsigned char keyType,
									 unsigned int sectorNo,unsigned char* key)
	{
		return _mf30w_MFPlusL3_AuthenSectorKey(ihic, keyType, sectorNo, key);
	}
	int  fw_MFPlusL3_UpdateKey(int ihic, unsigned int keyAddr,
							   unsigned char* newKey)
	{
		return _mf30w_MFPlusL3_UpdateKey(ihic, keyAddr, newKey);
	}
	int  fw_MFPlusL3_ReadWithEncrypt(int ihic, unsigned int blkAddr,
									 unsigned char blkNum,
											unsigned char* rdata,
											unsigned char flag)
	{
		return _mf30w_MFPlusL3_ReadWithEncrypt(ihic, blkAddr, blkNum, rdata, flag);
	}
	int  fw_MFPlusL3_WriteWithEncrypt(int ihic, unsigned int blkAddr,
									  unsigned char blkNum,
										  unsigned char* wdata,
										  unsigned char flag)
	{
		return _mf30w_MFPlusL3_WriteWithEncrypt(ihic, blkAddr, blkNum, wdata, flag);
	}
	int  fw_MFPlusL3_ReadWithPlain(int ihic, unsigned int blkAddr,
								   unsigned char blkNum,
										  unsigned char* rdata)
	{
		return _mf30w_MFPlusL3_ReadWithPlain(ihic, blkAddr, blkNum, rdata);
	}
	int  fw_MFPlusL3_WriteWithPlain(int ihic, unsigned int blkAddr,unsigned char blkNum,
										  unsigned char* wdata)
	{
		return _mf30w_MFPlusL3_WriteWithPlain(ihic, blkAddr, blkNum, wdata);
	}
	int  fw_MFPlusL3_InitVal(int ihic, unsigned int blkAddr,
							 unsigned long value)
	{
		return _mf30w_MFPlusL3_InitVal(ihic, blkAddr, value);
	}
	int  fw_MFPlusL3_ReadVal(int ihic, unsigned int blkAddr,
							 unsigned long* value)
	{
		return _mf30w_MFPlusL3_ReadVal(ihic, blkAddr, value);
	}
	int  fw_MFPlusL3_Increment(int ihic, unsigned int blkAddr,
							   unsigned long value)
	{
		return _mf30w_MFPlusL3_Increment(ihic, blkAddr, value);
	}
	int  fw_MFPlusL3_Decrement(int ihic, unsigned int blkAddr,
							   unsigned long value)
	{
		return _mf30w_MFPlusL3_Decrement(ihic, blkAddr, value);
	}*/
	
	/// lcd ////
	short  _mf30w_lcd_textClear(int icdev, int line, int offset, int len)
	{
		short st = 0,i =0;
		char _cmd_serial = 0x95, _cmd_usb = 0x55;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 0x03;
		_userLen_usb = 0x03;
		
		_userData_serial[0] = _userData_usb[0] = (char)line;
		_userData_serial[1] = _userData_usb[1] = (char)offset;
		_userData_serial[2] = _userData_usb[2] = (char)len;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	public short  fw_lcd_dispclear(int icdev)
	{
		short		flag;

		flag = (short)_mf30w_lcd_textClear(icdev, 1, 0, LCD_LINE_BUFFER_SIZE);
		if(flag !=0 ) return flag;
			
		flag = (short)_mf30w_lcd_textClear(icdev, 2, 0, LCD_LINE_BUFFER_SIZE);
		if(flag != 0) return flag;
			
		return 0;

	}
	int  _mf30w_lcd_dispLine(int icdev, char[] str, int line, int offset, int len)
	{
		int rlen = 0;
		int	 datalen;
		int txtLen =0;
		
		short st = 0,i =0;
		char _cmd_serial = 0x93, _cmd_usb = 0x53;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		char[] tmpChars = new char[128];
		byte[] tmpChars_gbk;
		String strTmp_unicode, strTmp_gbk;
		byte[] temp_utf8;
		
		if( (line != 1 && line != 2))
			return mProtocol.ERR_PARA;

		txtLen = len;
		txtLen = (txtLen > LCD_LINE_BUFFER_SIZE) ? ( LCD_LINE_BUFFER_SIZE):(txtLen);
		
		//convert unicode to GBK
		for(i=0; i<txtLen; i++) tmpChars[i] = str[i];
		tmpChars[i] = 0x00;
		
		strTmp_unicode = String.valueOf(tmpChars);
		
		try{
			temp_utf8=strTmp_unicode.getBytes("utf-8");
			tmpChars_gbk =new String(temp_utf8,"utf-8").getBytes("gbk");
			txtLen = 0;
			while(tmpChars_gbk[txtLen] != 0x00) txtLen++;
		}
		catch(java.io.UnsupportedEncodingException   e){txtLen = 0; tmpChars_gbk= new byte[128];}
        
       
		datalen = txtLen+3;
		
		_userLen_serial = datalen;
		_userLen_usb = datalen;
		
		_userData_serial[0] = _userData_usb[0] = (char)line;
		_userData_serial[1] = _userData_usb[1] = (char)offset;
		_userData_serial[2] = _userData_usb[2] = (char)txtLen;
		
		for(i=0;i<txtLen;i++)
		{
			_userData_serial[3+i] = _userData_usb[3+i] = (char)(tmpChars_gbk[i]);
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev,_cmdBuf, _genLen[0], _rBuf, _rLen);

		return st;
	}

	public final int LCD_LINE_BUFFER_SIZE = 18;
	public short  fw_lcd_dispstr(int icdev,char[] digit)
	{
		char[] pNewStr = new char[256];
		char[] pLineTwoStr = new char[256];
		short st = 0;
		int newTxtLen = 0, i=0;

		while(newTxtLen < 256 && digit[newTxtLen] != 0)
		{
			pNewStr[newTxtLen] = digit[newTxtLen];
			newTxtLen++;
		}

		if(newTxtLen <= LCD_LINE_BUFFER_SIZE)
			return (short) _mf30w_lcd_dispLine(icdev, pNewStr, 1,0, newTxtLen);
		else
		{
			st = (short)_mf30w_lcd_dispLine(icdev, pNewStr, 1, 0, LCD_LINE_BUFFER_SIZE);
			if(st != 0) return st;
			
			for(i=0; i< newTxtLen-LCD_LINE_BUFFER_SIZE; i++)
				pLineTwoStr[i] = pNewStr[LCD_LINE_BUFFER_SIZE +i];
			
			st = (short)_mf30w_lcd_dispLine(icdev, pLineTwoStr, 2,0, newTxtLen-LCD_LINE_BUFFER_SIZE);
			if(st != 0) return st;
		}
		return st;
	}
	//////// Keyboard ////
	void MapInputToPass(char input,char[] pass)
	{
			switch(input){
			case 0x4F:
				pass[0] = '1';
				break;
			case 0x4E:
				pass[0] = '2';
				break;
			case 0x4D:
				pass[0] = '3';
				break;
			case 0x57:
				pass[0] = '4';
				break;
			case 0x56:
				pass[0] = '5';
				break;
			case 0x55:
				pass[0] = '6';
				break;
			case 0x5F:
				pass[0] = '7';
				break;
			case 0x5E:
				pass[0] = '8';
				break;
			case 0x5D:
				pass[0] = '9';
				break;
			case 0x54:
				pass[0] = '0';
				break;
			case 0x47:
				pass[0] = 112;
				break;
			case 0x46:
				pass[0] = 113;
				break;
			case 0x45:
				pass[0] = 114;
				break;
			case 0x4C:
				pass[0] = 0x0d;
				break;
			default:
				pass[0] = 0x00;
				break;
			}

	}

	void ConvertInputToPass(char[] input,char len_in, char[] pass, char[] len_out)
	{
		char[] tmpChar =new char[1];
		char i, count=0;

		for(i=0; i< len_in; i++)
		{
			MapInputToPass(input[i], tmpChar);
			if(tmpChar[0] != 0)
			{
				pass[count++] = tmpChar[0];
			}
		}
		len_out[0] = count;
	}

	public short  fw_PassIn(int icdev,short ctime)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xC2, _cmd_usb = 0xC2;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 0x01;
		_userLen_usb = 0x01;

		_userData_serial[i] = _userData_usb[i] = (char) ctime ;

		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	public short  fw_PassGet(int icdev,
								char[] rrlen,
	                            char[] cpass)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xC3, _cmd_usb = 0xC3;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		if(0 == st) 
		{
			ConvertInputToPass(_rBuf, (char)_rLen[0], cpass, rrlen);
		}
		
		return st;
	}
	public short  fw_PassCancel(int icdev)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xC4, _cmd_usb = 0xC4;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		return st;
	}
	public short  fw_CheckKeyValue(int icdev,
									  char[] rrlen,
									  char[] retdata)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xC5, _cmd_usb = 0xC5;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st) 
		{
			ConvertInputToPass(_rBuf, (char)_rLen[0], retdata, rrlen);
		}
		
		return st;
	}
	
	
	public short fw_rdCardStatus(int ICDev, int[] rSt)
	{
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_rdCardStatus(ICDev, rSt);
		else
			return protocol_T6.ERR_FUNC;
		
	}
	
	public short fw_read_4442(int ICDev,char Offset,char[] DataBuffer,int len)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB0, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		int maxLenOnce = 27;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_read_4442(ICDev, (short)Offset, DataBuffer, len);
		
		_userLen_serial = 0x03;
		_userLen_usb = 0x03;
		
		if(len <= maxLenOnce)
		{
			_userData_serial[0] = Offset ;
			_userData_serial[1] = 0x00 ;
			_userData_serial[2] = (char)len ;
			
			_userData_usb[0] = 0x71 ;//sub command of read 4442
			_userData_usb[1] = Offset ;
			_userData_usb[2] = (char)len ;
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(ICDev, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(0 == st) 
			{
				for(i=0; i<len; i++) DataBuffer[i] = _rBuf[i];
			}
			
			return st;
		}
		else
		{
			int j, k;
			char times;//闁硅泛锕ラ弳鐔煎箲椤旂厧鐎婚柟瀛樺姉濞堟垵鈻撻崹顐ｆ
			char[] allData = new char[len + 1];
			char[] onceData = new char[maxLenOnce + 1];

			times = (char)( len / maxLenOnce);
			if( len % maxLenOnce !=0) times += 1;

			for(j=0;j<times;j++)
			{//闁告帒妫欐竟鎺楀礃濞嗘劖娈堕柟璇″櫙缁辨繂袙韫囨柨顥ｉ柛鎰崹eclen濞戞搩浜滈悺褔鎳為敓锟�
				if(j!=(times-1))
				{
					st=fw_read_4442 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, maxLenOnce);
					if(st == 0)for(k= 0; k < maxLenOnce; k++) allData[Offset+ j*maxLenOnce + k] = onceData[k];
				}
				else 
				{
					st=fw_read_4442 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, len- j*maxLenOnce);
					if(st == 0)for(k= 0; k < len- j*maxLenOnce; k++) allData[Offset+ j*maxLenOnce + k] = onceData[k];
				}
				if(st !=0) return st;
			}
			
			for(k=0; k< len; k++) DataBuffer[k] = allData[k];
			
		}
		
		return st;
	}
	
	public short fw_write_4442(int ICDev,char Offset,char[] DataBuffer,int len)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB1, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		int maxLenOnce = 27;
		int Address_Set= Offset,Write_len=0;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_4442(ICDev, (short)Offset, DataBuffer, len);
		
		
		Write_len = len;
		
		_userLen_serial = 0x03 + Write_len;
		_userLen_usb = 0x03 + Write_len;
		
		if(len <= maxLenOnce)
		{
			_userData_serial[0] = Offset ;
			_userData_serial[1] = 0x00 ;
			_userData_serial[2] = (char)Write_len ;
			
			_userData_usb[0] = 0x72 ;//sub command of write 4442
			_userData_usb[1] = Offset ;
			_userData_usb[2] = (char)Write_len ;
			
			for(i=0; i< Write_len; i++)_userData_serial[3+i] = _userData_usb[3+i] = DataBuffer[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(ICDev, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
		else
		{
			int j, k;
			char times;//闁硅泛锕ラ弳鐔煎箲椤旂厧鐎婚柟瀛樺姉濞堟垵鈻撻崹顐ｆ
			char[] onceData = new char[maxLenOnce + 1];

			times = (char)( len / maxLenOnce);
			if( len % maxLenOnce !=0) times += 1;

			for(j=0;j<times;j++)
			{//闁告帒妫欐竟鎺楀礃濞嗘劖娈堕柟璇″櫙缁辨繂袙韫囨柨顥ｉ柛鎰崹eclen濞戞搩浜滈悺褔鎳為敓锟�
				if(j!=(times-1))
				{
					for(k= 0; k < maxLenOnce; k++) onceData[k] =  DataBuffer[Offset+ j*maxLenOnce + k] ;
					st=fw_write_4442 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, maxLenOnce);
				}
				else 
				{
					for(k= 0; k < len- j*maxLenOnce; k++) onceData[k] = DataBuffer[Offset+ j*maxLenOnce + k];
					st=fw_write_4442 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, len- j*maxLenOnce);
				}
				if(st !=0) return st;
			}
			
		}
		
		return st;
	}

	public short fw_authentikey_4442(int icdev,char _Adr,int slen, char[] key)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB2, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_authentikey_4442(icdev, (short)_Adr, slen, key);
		
		_userLen_serial = 3;
		_userLen_usb = 6;
	
		_userData_usb[0] = (char)0x75;//sub command ->verify password
		_userData_usb[1] = (char)0x00;
		_userData_usb[2] = (char)0x3;
		
		for(i=0;i<3;i++)
		{
			_userData_serial[i] = _userData_usb[3+i] = (char)key[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);

		return st;
	}

	public short fw_changkey_4442(int icdev,char _Adr,int slen,char[] key)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB4, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_changkey_4442(icdev, (short)_Adr,slen, key);
		
		_userLen_serial = 3;
		_userLen_usb = 6;
	
		_userData_usb[0] = (char)0x76; //sub command ->change password
		_userData_usb[1] = (char)0x00;
		_userData_usb[2] = (char)0x3;
		
		for(i=0;i<3;i++)
		{
			_userData_serial[i] = _userData_usb[3+i] = (char)key[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);

		return st;
	}
	public short fw_cntReadError_4442(int icdev,char[] cntReadError)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB3, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_cntReadError_4442(icdev, cntReadError);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x03;
		
		_userData_usb[0] = (char)0x77; //sub command ->read error counter
		_userData_usb[1] = (char)0x00;
		_userData_usb[2] = (char)0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st) 
		{
			cntReadError[0] = _rBuf[0];
		}
		
		return st;
	}
	
	public short fw_read_4428(int ICDev,char Offset,char[] DataBuffer,int len)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB0, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		int maxLenOnce = 27;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_read_4428(ICDev, (short)Offset, DataBuffer, len);
		
		_userLen_serial = 0x03;
		_userLen_usb = 0x03;
		
		if(len <= maxLenOnce)
		{
			_userData_serial[0] = Offset ;
			_userData_serial[1] = 0x00 ;
			_userData_serial[2] = (char)len ;
			
			_userData_usb[0] = 0x71 ;//sub command of read 4442
			_userData_usb[1] = Offset ;
			_userData_usb[2] = (char)len ;
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(ICDev, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(0 == st) 
			{
				for(i=0; i<len; i++) DataBuffer[i] = _rBuf[i];
			}
			
			return st;
		}
		else
		{
			int j, k;
			char times;//闁硅泛锕ラ弳鐔煎箲椤旂厧鐎婚柟瀛樺姉濞堟垵鈻撻崹顐ｆ
			char[] allData = new char[len + 1];
			char[] onceData = new char[maxLenOnce + 1];

			times = (char)( len / maxLenOnce);
			if( len % maxLenOnce !=0) times += 1;

			for(j=0;j<times;j++)
			{//闁告帒妫欐竟鎺楀礃濞嗘劖娈堕柟璇″櫙缁辨繂袙韫囨柨顥ｉ柛鎰崹eclen濞戞搩浜滈悺褔鎳為敓锟�
				if(j!=(times-1))
				{
					st=fw_read_4428 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, maxLenOnce);
					if(st == 0)for(k= 0; k < maxLenOnce; k++) allData[Offset+ j*maxLenOnce + k] = onceData[k];
				}
				else 
				{
					st=fw_read_4428 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, len- j*maxLenOnce);
					if(st == 0)for(k= 0; k < len- j*maxLenOnce; k++) allData[Offset+ j*maxLenOnce + k] = onceData[k];
				}
				if(st !=0) return st;
			}
			
			for(k=0; k< len; k++) DataBuffer[k] = allData[k];
			
		}
		
		return st;
	}
	
	public short fw_write_4428(int ICDev,char Offset,char[] DataBuffer,int len)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB1, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		int maxLenOnce = 27;
		int Address_Set= Offset,Write_len=0;
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_4428(ICDev, (short)Offset, DataBuffer, len);
		
		
		Write_len = len;
		
		_userLen_serial = 0x03 + Write_len;
		_userLen_usb = 0x03 + Write_len;
		
		if(len <= maxLenOnce)
		{
			_userData_serial[0] = Offset ;
			_userData_serial[1] = 0x00 ;
			_userData_serial[2] = (char)Write_len ;
			
			_userData_usb[0] = 0x72 ;//sub command of write 4442
			_userData_usb[1] = Offset ;
			_userData_usb[2] = (char)Write_len ;
			
			for(i=0; i< Write_len; i++)_userData_serial[3+i] = _userData_usb[3+i] = DataBuffer[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(ICDev, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
		else
		{
			int j, k;
			char times;//闁硅泛锕ラ弳鐔煎箲椤旂厧鐎婚柟瀛樺姉濞堟垵鈻撻崹顐ｆ
			char[] onceData = new char[maxLenOnce + 1];

			times = (char)( len / maxLenOnce);
			if( len % maxLenOnce !=0) times += 1;

			for(j=0;j<times;j++)
			{//闁告帒妫欐竟鎺楀礃濞嗘劖娈堕柟璇″櫙缁辨繂袙韫囨柨顥ｉ柛鎰崹eclen濞戞搩浜滈悺褔鎳為敓锟�
				if(j!=(times-1))
				{
					for(k= 0; k < maxLenOnce; k++) onceData[k] =  DataBuffer[Offset+ j*maxLenOnce + k] ;
					st=fw_write_4428 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, maxLenOnce);
				}
				else 
				{
					for(k= 0; k < len- j*maxLenOnce; k++) onceData[k] = DataBuffer[Offset+ j*maxLenOnce + k];
					st=fw_write_4428 (ICDev, (char)(Offset+ j*maxLenOnce), onceData, len- j*maxLenOnce);
				}
				if(st !=0) return st;
			}
			
		}
		
		return st;
	}

	public short fw_authentikey_4428(int icdev, char[] key)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB2, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_authentikey_4428(icdev, key);
		
		_userLen_serial = 3;
		_userLen_usb = 6;
	
		_userData_usb[0] = (char)0x75;//sub command ->verify password
		_userData_usb[1] = (char)0x00;
		_userData_usb[2] = (char)0x3;
		
		for(i=0;i<3;i++)
		{
			_userData_serial[i] = _userData_usb[3+i] = (char)key[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev,_cmdBuf, _genLen[0], _rBuf, _rLen);

		return st;
	}

	public short fw_changkey_4428(int icdev,char[] key)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB4, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_changkey_4428(icdev, key);
		
		_userLen_serial = 3;
		_userLen_usb = 6;
	
		_userData_usb[0] = (char)0x76; //sub command ->change password
		_userData_usb[1] = (char)0x00;
		_userData_usb[2] = (char)0x3;
		
		for(i=0;i<3;i++)
		{
			_userData_serial[i] = _userData_usb[3+i] = (char)key[i];
		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);

		return st;
	}
	public short fw_cntReadError_4428(int icdev,char[] cntReadError)
	{
		short st = 0,i =0;
		char _cmd_serial = 0xB3, _cmd_usb = 0x70;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_cntReadError_4428(icdev, cntReadError);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x03;
		
		_userData_usb[0] = (char)0x77; //sub command ->read error counter
		_userData_usb[1] = (char)0x00;
		_userData_usb[2] = (char)0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st) 
		{
			cntReadError[0] = _rBuf[0];
		}
		
		return st;
	}
	final char CPU_FADR_ATR_TA_IN_T0	= (1<<4);
	final char CPU_FADR_ATR_TB_IN_T0	=	(1<<5);
	final char CPU_FADR_ATR_TC_IN_T0	=	(1<<6);
	final char CPU_FADR_ATR_TD_IN_T0	=	(1<<7);
	public  int ApduFlag = 0;
	public  int gl_protocol_type = 0;
	
	public char fTAi;
	public char fTBi;
	public char fTCi;
	public char fTDi;
	
	public void Fill_FlagTXi(char _tdi)
	{
		if((_tdi & CPU_FADR_ATR_TA_IN_T0) != 0)fTAi = 1;else fTAi = 0;
		if((_tdi & CPU_FADR_ATR_TB_IN_T0) != 0)fTBi = 1;else fTBi = 0;
		if((_tdi & CPU_FADR_ATR_TC_IN_T0) != 0)fTCi = 1;else fTCi = 0;
		if((_tdi & CPU_FADR_ATR_TD_IN_T0) != 0)fTDi = 1;else fTDi = 0;
	}
	
	public char Get_ProtocolType_FromATR(char[] pATR,int len)
	{
		char rel = 0;
		short moreRead, i=0, newStartAddr;
		

		// TS + T0 + (TAi + TBi + TCi + TDi) + xor
		//The protocol is concent in TDi if exist
		char T0;
		if(len < 2) return rel;

		T0 = pATR[1];
		newStartAddr = 2;

		Fill_FlagTXi(T0);

		moreRead = (short)( fTAi  + fTBi  + fTCi + fTDi) ;

		newStartAddr += moreRead;
		
		while(i < moreRead)
		{
			if((i == moreRead-1) && fTDi!=0 )
			{
				Fill_FlagTXi(pATR[newStartAddr-1]);
				moreRead = (short)(fTAi  + fTBi  + fTCi + fTDi) ;
				rel = (char)(pATR[newStartAddr-1] & 0x0f);

				newStartAddr += moreRead;
				i = 0;

			}
			else i++;
		}

		return rel;
	}
	
    public   short fw_cpureset(int lDevice,byte[] rlen, byte[] revData)
    {
		short st = 0,i =0;
		char _cmd_serial = 0xB3, _cmd_usb = 0x7E;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_cpureset(lDevice, rlen, revData);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x00;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st) 
		{
			rlen[0] = (byte)(_rLen[0] -1);
			
			for(i=0; i< rlen[0]; i++)
			{
				 revData[i] = (byte)_rBuf[1+i];
				 _userData_usb[i] =_rBuf[1+i];
			}
			
			gl_protocol_type = Get_ProtocolType_FromATR(_userData_usb, rlen[0]);//閼颁焦婧��鎰繁閻⑩剠et_ProtocolType_FromATR

			ApduFlag = 0x40;
		}
		
		return st;
    	
    }
    public   short fw_setcpu(int lDevice, byte iSeat)
    {
    	
		short st = 0,i =0;
		char _cmd_serial = 0xB3, _cmd_usb = 0x83;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_setcpu(lDevice, iSeat);
		
		_userLen_serial = 0x00;
		_userLen_usb = 0x01;
		
		_userData_usb[0] = (char)iSeat;
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		
		return st;
    }
    public   short fw_cpuapdu(int lDevice, byte cmdLen, byte[] cmdBuf, byte[] rlen, byte[] revData)
    {
    	
		short st = 0,i =0;
		char _cmd_serial = 0xB3, _cmd_usb = 0x7D;
		char[] _userData_serial = new char[128];
		char[] _userData_usb = new char[128];
		char[] _cmdBuf = new char[1024];
		char[] xorBuf = new char[1024];
		char[] _rBuf =new char[1024];
		int _userLen_serial, _userLen_usb;
		int[] _genLen = new int[2];
		int[] _rLen = new int[2];
		
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_cpuapdu(lDevice, cmdLen, cmdBuf, rlen, revData);
		
		_userLen_serial = 0;
		
		if(0 == gl_protocol_type)
		{
			_userLen_usb = 1 + cmdLen;
		}
		else
		{
			_userLen_usb = 1 + cmdLen + 4 ;
		}

		
		if(0 == gl_protocol_type)
		{
			_userData_usb[0] = (char)cmdLen;
			
			for(i=0; i<cmdLen;i++)
				_userData_usb[1 + i] = (char)cmdBuf[i];
		}
		else
		{
			if(ApduFlag != 0)
				ApduFlag = 0x00;
			else
				ApduFlag = 0x40;
			_userData_usb[0] = (char)(cmdLen + 4);
			_userData_usb[1] = 0x00;
			_userData_usb[2] =(char) ApduFlag;
			_userData_usb[3] = (char)cmdLen;
			for(i=0; i< cmdLen; i++)
				_userData_usb[4 +i] = (char)cmdBuf[i];
			
			for(i=0; i< cmdLen + 3; i++)
				xorBuf[i] = _userData_usb[1 +i];
			
			_userData_usb[4+cmdLen ] = (char)(pl_javacall.genXor(xorBuf, cmdLen+3));

		}
		
		mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
		
		st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
		
		if(0 == st) 
		{
			if(0 == gl_protocol_type)
			{
				rlen[0] = (byte)(_rLen[0] -1);
				
				for(i=0; i<rlen[0]; i++)
					revData[i] = (byte)_rBuf[1 + i];
				
			}
			else 
			{
				rlen[0] = (byte)(_rLen[0] - 5);
				
				for(i=0; i<rlen[0]; i++)
					revData[i] = (byte)_rBuf[4 + i];
			}
		
		}
		
		return st;
    }
	
    public short fw_setcpupara(int lDevice,byte cputype,byte cpupro, byte cpuetu)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_setcpupara(lDevice, cputype, cpupro, cpuetu);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
    }
    
    public  short fw_config_card(int lDevice, byte flags)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_config_card(lDevice, flags);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x60, _cmd_usb = 0x87;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x01;
			_userLen_usb = 0x01;
			
			_userData_usb[0] = (char)flags;
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }

    public  short fw_inventory(int lDevice,byte flags,byte AFI, byte masklen, byte[] rlen,byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_inventory(lDevice, flags, AFI, masklen, rlen,rbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x60, _cmd_usb = 0x91;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x03;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)AFI;
			_userData_usb[2] = (char)masklen;
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				if(_rLen[0] <= 1 && _rBuf[0] <= 1)
					return Lib.Error.ErrorCode.ERR_NO_USERDATA;
				rlen[0] = (byte)(_rBuf[0] - 1);
				for(i=0; i< rlen[0]; i++)
					rbuffer[i] = (byte)(_rBuf[2 + i]);
			}
			
			return st;
		}
    }
    public  short fw_stay_quiet(int lDevice,byte flags,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_stay_quiet(lDevice, flags, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x92;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x09;
			
			_userData_usb[0] = (char)flags;
			for(i=0; i< 8; i++)
				_userData_usb[1 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_select_uid(int lDevice,byte flags,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_select_uid(lDevice, flags, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x93;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x09;
			
			_userData_usb[0] = (char)flags;
			for(i=0; i< 8; i++)
				_userData_usb[1 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_reset_to_ready(int lDevice,byte flags,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_reset_to_ready(lDevice, flags, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x94;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x09;
			
			_userData_usb[0] = (char)flags;
			for(i=0; i< 8; i++)
				_userData_usb[1 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_readblock(int lDevice,byte flags,byte startblock,byte blocknum, byte[] UID, byte[] rlen,byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_readblock(lDevice, flags, startblock,blocknum, UID, rlen, rbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x95;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0B;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)startblock;
			_userData_usb[2] = (char)blocknum;
			for(i=0; i< 8; i++)
				_userData_usb[3 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				if(_rLen[0] != 5)
					return Lib.Error.ErrorCode.ERR_NO_USERDATA;
				rlen[0] = (byte)(_rBuf[0]);
				for(i=0; i< rlen[0]; i++)
					rbuffer[i] = (byte)(_rBuf[1 + i]);
			}
			
			return st;
		}
    }
    public  short fw_writeblock(int lDevice,byte flags,byte startblock,byte blocknum, byte[] UID, byte wlen,byte[] wbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_writeblock(lDevice, flags, startblock,blocknum, UID, wlen, wbuffer);
		else
		{
			short st = 0,i =0, j=0;
			char _cmd_serial = 0x20, _cmd_usb = 0x96;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			byte[] tmpBuf = new byte[5];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			if(blocknum < 1 || blocknum >6)
				return Lib.Error.ErrorCode.ERR_NO_USERDATA;
			
			if(blocknum != 1)
			{
				for(i=0; i< blocknum; i++)
				{
					for(j=0; j< 4; j++) tmpBuf[j] = wbuffer[4*i + j];
					st = fw_writeblock(lDevice, flags, (byte)(startblock+i), (byte)1, UID, (byte)4, tmpBuf);
					if(st != 0)return st;
				}
				return st;
			}
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0F;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)startblock;
			_userData_usb[2] = (char)blocknum;
			for(i=0; i< wlen; i++)
				_userData_usb[3+i] = (char)wbuffer[i];
			for(i=0; i< 8; i++)
				_userData_usb[3 + wlen+ i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_lock_block(int lDevice,byte flags,byte block,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_lock_block(lDevice, flags, block, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x97;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			byte[] tmpBuf = new byte[5];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)block;
			for(i=0; i< 8; i++)
				_userData_usb[2 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_write_afi(int lDevice,byte flags,byte AFI,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_afi(lDevice, flags, AFI, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x98;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			byte[] tmpBuf = new byte[5];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)AFI;
			for(i=0; i< 8; i++)
				_userData_usb[2 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_lock_afi(int lDevice,byte flags,byte AFI,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_lock_afi(lDevice, flags, AFI, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x99;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			byte[] tmpBuf = new byte[5];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x0E;
			_userData_usb[1] = (char)flags;
			for(i=0; i< 8; i++)
				_userData_usb[2 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
		
			
			return st;
		}
    }
    public  short fw_write_dsfid(int lDevice,byte flags,byte DSFID,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_dsfid(lDevice, flags, DSFID, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x9A;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			byte[] tmpBuf = new byte[5];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)DSFID;
			for(i=0; i< 8; i++)
				_userData_usb[2 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_lock_dsfid(int lDevice,byte flags,byte DSFID,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_lock_dsfid(lDevice, flags, DSFID, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x9B;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			byte[] tmpBuf = new byte[5];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)DSFID;
			for(i=0; i< 8; i++)
				_userData_usb[2 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_get_systeminfo(int lDevice,byte flags,byte[] UID, byte[] rlen,byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_get_systeminfo(lDevice, flags, UID,rlen,rbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x9C;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x09;
			
			_userData_usb[0] = (char)flags;
			for(i=0; i< 8; i++)
				_userData_usb[1 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				rlen[0] = (byte)(_rLen[0]);
				for(i=0; i< rlen[0]; i++)
					rbuffer[i] = (byte)(_rBuf[i]);
			}
			
			return st;
		}
    }
    public  short fw_get_securityinfo(int lDevice,byte flags,byte startblock,byte blocknum, byte[] UID, byte[] rlen,byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_get_securityinfo(lDevice, flags, startblock,blocknum, UID,rlen,rbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x9D;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0B;
			
			_userData_usb[0] = (char)flags;
			_userData_usb[1] = (char)startblock;
			_userData_usb[2] = (char)blocknum;
			for(i=0; i< 8; i++)
				_userData_usb[3 + i] = (char)UID[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				rlen[0] = (byte)(_rLen[0]);
				for(i=0; i< rlen[0]; i++)
					rbuffer[i] = (byte)(_rBuf[i]);
			}
			
			return st;
		}
    }
    //parameter:
    //pwdType: password Identifier, 1-Read, 2-Write, 3-Privacy, 8-Destroy SL-S, 16-EAS
    //pwd: current password, (4 bytes)
    public  short fw_set_password(int lDevice,byte[] UID, byte pwdType, byte[] pwd)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_set_password(lDevice, UID, pwdType,pwd);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x9E;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0D;
			
			for(i=0; i< 8; i++)
				_userData_usb[i] = (char)UID[i];
			_userData_usb[8] = (char)pwdType;
			for(i=0; i< 4; i++)
				_userData_usb[9+i] = (char)pwd[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);

			return st;
		}
    }
    //parameter:
    //pwdType: password Identifier, 1-Read, 2-Write, 3-Privacy, 8-Destroy SL-S, 16-EAS
    //pwd: new password, (4 bytes)
    public  short fw_write_password(int lDevice,byte[] UID, byte pwdType, byte[] pwd)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_password(lDevice, UID, pwdType,pwd);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x9F;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0D;
			
			for(i=0; i< 8; i++)
				_userData_usb[i] = (char)UID[i];
			_userData_usb[8] = (char)pwdType;
			for(i=0; i< 4; i++)
				_userData_usb[9+i] = (char)pwd[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }

    public  short fw_icode_64bitProtect(int icdev,byte[] UID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_icode_64bitProtect(icdev, UID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0xA0;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x08;
			
			for(i=0; i< 8; i++)
				_userData_usb[i] = (char)UID[i];

			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(icdev, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    //parameter:
    //iPage: 0 ~ 9
    //protectStatus: 
    //32 bit mode:
    //0-public, 1-read and write protected by read password, 16-write protected by write password, 17-read by read pass, write by write pass
    //64 bit mode:
    //0-public, 1-read and write protected by read plus write password
    public  short fw_protect_page(int lDevice,byte[] UID, byte iPage, byte protectStatus)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_protect_page(lDevice, UID, iPage, protectStatus);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0xA1;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			for(i=0; i< 8; i++)
				_userData_usb[i] = (char)UID[i];
			_userData_usb[8] =(char) iPage;
			_userData_usb[9] =(char) protectStatus;

			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    //===============================================
    //  typeB at88rf020  card  Functions
    //===============================================
    public  short fw_request_b(int lDevice,byte _Mode,byte AFI, byte N,byte[] ATQB)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_request_b(lDevice, _Mode, AFI, N, ATQB);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x88;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x03;
			
			_userData_usb[0] = (char)_Mode;
			_userData_usb[1] = (char)AFI;
			_userData_usb[2] = (char)N;
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				for(i=0; i< _rLen[0]; i++)
					ATQB[i] = (byte)(_rBuf[i]);
			}
			
			return st;
		}
    }
    public  short fw_attrib(int lDevice,byte[] PUPI, byte CID)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_attrib(lDevice, PUPI, CID);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x8A;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x05;
			
			for(i=0; i< 4; i++)
				_userData_usb[i] = (char)PUPI[i];
			_userData_usb[4] = (char)CID;
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }
    public  short fw_check_at(int lDevice,byte cid,byte[] key)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_check_at(lDevice, cid, key);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x05;
			
			_userData_usb[0] = (char)0x06;
			_userData_usb[1] = (char)cid;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)key[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }
    public  short fw_read_at(int lDevice,byte Adr,byte[] key,byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_read_at(lDevice, Adr, key, rbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x04;
			_userData_usb[1] = (char)Adr;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)key[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				for(i=0; i< 8; i++)
					rbuffer[i] = (byte)(_rBuf[2 + i]);
			}
			
			return st;
		}
    }
    public  short fw_write_at(int lDevice,byte Adr,byte[] sbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_write_at(lDevice, Adr, sbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x03;
			_userData_usb[1] = (char)Adr;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)sbuffer[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }
    public  short fw_changekey_at(int lDevice,byte[] key)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_changekey_at(lDevice, key);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x03;
			_userData_usb[1] = (char)0x03;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)key[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			return st;
		}
    }
    public  short fw_lock_at(int lDevice,byte Adr,byte[] sbuffer)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_lock_at(lDevice, Adr, sbuffer);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x02;
			_userData_usb[1] = (char)Adr;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)sbuffer[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }
    public  short fw_CommandLink_at(int lDevice,byte[] sBuffer,int sLen,byte[] rBuffer,int[] rLen)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_CommandLink_at(lDevice, sBuffer, sLen, rBuffer, rLen);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[2048];
			char[] _cmdBuf = new char[2048];
			char[] _rBuf =new char[2048];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = sLen;
			
			for(i=0; i< sLen; i++)
				_userData_usb[i] = (char)sBuffer[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			if(st == 0)
			{
				rLen[0] = _rLen[0];
				
				for(i=0; i< rLen[0]%2048; i++)
					rBuffer[i] = (byte)(_rBuf[i]);
			}
			
			return st;
		}
    }
    public  short fw_halt_at(int lDevice,byte cid,byte[] key)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_halt_at(lDevice, cid, key);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x0A;
			_userData_usb[1] = (char)cid;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)key[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice,_cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }
    public  short fw_count_at(int lDevice,byte cid,byte[] key)
    {
		if(mCurPortType == PT_SERIAL)//serial
			return umf_javacall.fw_count_at(lDevice, cid, key);
		else
		{
			short st = 0,i =0;
			char _cmd_serial = 0x20, _cmd_usb = 0x89;
			char[] _userData_serial = new char[128];
			char[] _userData_usb = new char[128];
			char[] _cmdBuf = new char[1024];
			char[] _rBuf =new char[1024];
			int _userLen_serial, _userLen_usb;
			int[] _genLen = new int[2];
			int[] _rLen = new int[2];
			
			_userLen_serial = 0x00;
			_userLen_usb = 0x0A;
			
			_userData_usb[0] = (char)0x0E;
			_userData_usb[1] = (char)cid;
			for(i=0; i< 8; i++)
				_userData_usb[2+i] = (char)key[i];
			
			mProtocol.genSendBuf(_cmd_serial, _userData_serial, _userLen_serial, _cmd_usb, _userData_usb, _userLen_usb, _cmdBuf, _genLen );
			
			st =(short) mProtocol.common_transfer(lDevice, _cmdBuf, _genLen[0], _rBuf, _rLen);
			
			
			return st;
		}
    }
	
	
}

