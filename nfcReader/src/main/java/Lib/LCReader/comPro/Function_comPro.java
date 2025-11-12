package Lib.LCReader.comPro;

import FWPubLib.pl_javacall;
import Lib.General.Convert;
import lc.comproCall;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;


public class Function_comPro {
	
	//private protocol mProtocol;
	public protocol mProtocol;
	public static final char PT_SERIAL = 1;
	public static final char PT_USB = 2;
	public static final char PT_BLUETOOTH = 3;
	
	private char mCurPortType = PT_SERIAL;
	
	public Function_comPro(/*Activity*/Context hostAct,Handler handler)
	{
		mProtocol = new protocol(hostAct, handler);
	}
	
	public void SetTransPara(int usbTransLenOnce,int vendorID, int productID)
	{
		mProtocol._setTransPara(usbTransLenOnce,vendorID, productID);
	}
	public int lc_init_ex(int portType, char[] fpath, int baud)
	{
		int hDev = -1;
		
		mCurPortType = (char)portType;
		
		if(mCurPortType == PT_SERIAL)//serial
		{
			hDev = comproCall.lc_init_ex(portType, fpath, baud);
		}
		else
		{
			hDev = mProtocol.protocol_open(portType, fpath, baud);
			mProtocol._setTransPara(0x20,0x0471, 0xa112);
		}
		
		return hDev;
	}
//	public int lc_init_ex(int portType, char[] fpath, int baud)
//	{
//		int hDev = -1;
//		int status ;
//		char szVer[] = new char[128];
//		
//		mCurPortType = (char)portType;
//		
//		//if(mCurPortType == PT_SERIAL)//serial
//		if(true)//test
//		{
//			hDev = comproCall.lc_init_ex(portType, fpath, baud);
//			mCurPortType = PT_SERIAL;//test
//			
//			if(hDev != -1)
//			{
//				status  = comproCall.lc_getver(hDev, szVer);
//				if(status != 0)
//				{
//					hDev = -1;
//					comproCall.lc_exit(hDev);
//				}
//			}
//		}
//		else
//		{
//			hDev = mProtocol.protocol_open(portType, fpath, baud);
//			mProtocol._setTransPara(0x20,0x0471, 0xa112);
//		}
//		
//		return hDev;
//	}
	public int  lc_exit(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_exit(icdev);
		else
			return mProtocol.protocol_close(icdev);
	}


	public int asc_hex(char[] strasc, char[] strhex, int lenASC)
	{
		Convert obj_convert = new Convert();
		return obj_convert.asc2hex( strasc,strhex, lenASC);
	}
	
	public int hex_asc(char[] strhex, char[] strasc, int lenHex)
	{
		Convert obj_convert = new Convert();
		return obj_convert.hex2asc( strhex, strasc, lenHex);
	}
	
	
	public int lc_getFlashUserMemSize(int icdev, int[] pSize)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars = new char[5];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getFlashUserMemSize(icdev, pSize);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.COMPRO_GET_MAXUSER_MEMSIZE;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		if(rlen[0] >= 4)
		{
			for(i=0; i< 4; i++) tmpChars[i] = (char)inBuf[i];
			pl_javacall.MultiByteToUL(tmpChars, 4, 0, pSize);
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_srd_flash(int icdev,int offset,int length, byte[] rec_buffer)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_srd_flash(icdev, offset, length, rec_buffer);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen =2;
		
		proData_obj.CMD = (int)protocol.COMPRO_RFLASH;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)length;
		proData_obj.pData[1] = (byte)offset;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] != length)
			return Lib.Error.ErrorCode.ERR_NO_USERDATA;
		
		for(i=0; i< length; i++)
			rec_buffer[i] = inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_getver(int icdev,char[] version)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getver(icdev, version);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.COMPRO_GETVER;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		
		for(i=0; i< rlen[0]; i++)
		{
			version[i] =(char) inBuf[i];
		}
		version[i] = 0;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_devReboot(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_devReboot(icdev);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}

	public int lc_swr_flash(int icdev,int offset,int length,byte[] buffer)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_swr_flash(icdev, offset, length, buffer);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 2 + length;
		
		proData_obj.CMD = (int)protocol.COMPRO_WFLASH;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)length;
		proData_obj.pData[1] = (byte)offset;
		
		for(i=0; i< length; i++)
			proData_obj.pData[i] = buffer[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_setAddress(int icdev, int addr)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars = new char[4];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_setAddress(icdev, addr);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 4;
		
		proData_obj.CMD = (int)protocol.COMPRO_SET_ADDR;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		pl_javacall.ULToMultiByte(addr, tmpChars, 4, 1);
		
		for(i=0; i< 4; i++)
			proData_obj.pData[1 + i] = (byte)tmpChars [i];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_getAddress(int icdev, int[] pAddr)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars = new char[128];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getAddress(icdev, pAddr);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.COMPRO_GET_ADDR;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] < 128)
		{
			for(i= 0; i< 4; i++)tmpChars[i] = (char)inBuf[i];
		}
		pl_javacall.MultiByteToUL(tmpChars, 2, 1, pAddr);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_setBaudRate(int icdev,int baud)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars = new char[4];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_setBaudRate(icdev, baud);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 4;
		
		proData_obj.CMD = (int)protocol.COMPRO_SET_ADDR;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		pl_javacall.ULToMultiByte(baud, tmpChars, 4, 1);
		
		for(i=0; i< 4; i++)
			proData_obj.pData[1 + i] = (byte)tmpChars [i];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_SetDevPassword(int icdev,byte pwdAddr, byte pwdLen, byte[] pPwd)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_SetDevPassword(icdev, pwdAddr, pwdLen, pPwd);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = pwdLen + 2;
		
		proData_obj.CMD = (int)protocol.CMD_SET_DEVPWD;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = pwdAddr;
		proData_obj.pData[1] = pwdLen;
		
		for(i=0; i< datalen;i++)
			proData_obj.pData[2+i] = pPwd[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_setReadOnlyPara(int icdev,byte[] pPara,int paraLen)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_setReadOnlyPara(icdev, pPara, paraLen);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = paraLen;
		
		proData_obj.CMD = (int)protocol.SETROPARA;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0; i< datalen;i++)
			proData_obj.pData[i] = pPara[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_getReadOnlyPara(int icdev, byte[] pPara_out, int[] paraLen_out)
	{	
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getReadOnlyPara(icdev, pPara_out, paraLen_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.GETROPARA;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		paraLen_out[0] = rlen[0];
		
		for(i=0; i< (int)(rlen[0]&0xff);i++)
			pPara_out[i] = inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_setAppendData(int icdev, byte[] para_in, byte paraLen)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_setAppendData(icdev, para_in, paraLen);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = paraLen;
		
		proData_obj.CMD = (int)protocol.SET_APPEND_DATA;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0; i< datalen;i++)
			proData_obj.pData[i] = para_in[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_getAppendData(int icdev, byte[] pPara_out, byte[] pLen_out)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getAppendData(icdev, pPara_out, pLen_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.GET_APPEND_DATA;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		pLen_out[0] = (byte)(rlen[0] & 0xff);
		
		for(i=0; i< rlen[0];i++)
			pPara_out[i] = inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_set_AutoReportDataType(int icdev, byte type,short outputPara)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[]  tmpChars = new char[4];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_set_AutoReportDataType(icdev, type, outputPara);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 3;

		proData_obj.CMD = (int)protocol.COMPRO_SET_DATATYPE_AUTOREP;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = type;
		pl_javacall.ULToMultiByte(outputPara, tmpChars, 2, 1);
		
		for(i=0; i< 2;i++)
			proData_obj.pData[1+i] = (byte)tmpChars[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_get_AutoReportDataType(int icdev, byte[] type_out, short[] pOutputPara)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars = new char[4];
		int[] tmpInt = new int[2]; 
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_get_AutoReportDataType(icdev, type_out, pOutputPara);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.COMPRO_GET_DATATYPE_AUTOREP;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		type_out[0] = (byte)(inBuf[0] & 0xff);
		
		if(rlen[0] >= 3)
		{
			for(i=0; i< 2; i++) tmpChars[i] = (char)inBuf[1 + i];
			pl_javacall.MultiByteToUL(tmpChars, 2, 1, tmpInt);
			pOutputPara[0] = (short)(tmpInt[0] & 0xffff);
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_set_identifyResponsePara(int icdev, byte[] pParaIn, byte paraLen)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[]  tmpChars = new char[4];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_set_identifyResponsePara(icdev, pParaIn, paraLen);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = paraLen;
		
		proData_obj.CMD = (int)protocol.CMD_SET_IDTOBJ_RESP;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0; i< datalen;i++)
			proData_obj.pData[i] = pParaIn[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_get_identifyResponsePara(int icdev, byte[] pParaOut, byte[] pParaLen)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_get_identifyResponsePara(icdev, pParaOut, pParaLen);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.CMD_GET_IDTOBJ_RESP;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		pParaLen[0] = (byte)(rlen[0] & 0xff);
		
		for(i=0; i< rlen[0];i++)
			pParaOut[i] = inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	//1->Read/Write, 2->Read only
	public int lc_setReaderMode(int icdev, byte mode)
	{
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_setReaderMode(icdev, mode);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.COMPRO_READER_MODE;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		proData_obj.pData[0] = mode;

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);

		return (short)flag;
	}
	public int lc_getReaderMode(int icdev, byte[] mode)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getReaderMode(icdev, mode);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;
		
		proData_obj.CMD = (int)protocol.GET_READER_MODE;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		mode[0] = (byte)(rlen[0] & 0xff);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_getAutoReturnedData(int icdev, byte[] rData, int[] rlen)
	{
		int st =0;
//		int _rlen = 0;
//		byte[] tmpData = new byte[128];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getAutoReturnedData(icdev, rData, rlen);
		
//		while(true)
//		{
//			st = mProtocol.rfab_readOneByte(icdev, tmpData);
//			if(st != 0)
//				break;
//			else
//			{
//				rData[_rlen] = tmpData[0];
//				_rlen++;
//			}
//		}
//		
//		rlen[0] = _rlen;
//		if(_rlen != 0)
//			st = 0;
		st = mProtocol.rfab_usb_onceReceive(icdev, rData, rlen);
		
		return st;
	}
	//////////////////////////////////////// device functions /////
	public int lc_beep(int icdev,int _Msec)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[]	tmpChrs = new char[4];
		
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_beep(icdev, _Msec);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 2;

		proData_obj.CMD = (int)protocol.COMPRO_BEEP;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		

		pl_javacall.ULToMultiByte(_Msec, tmpChrs, 2, 1);
		for(i=0;i<2;i++)
		{
			proData_obj.pData[i]  = (byte)(tmpChrs[i]);
		}

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);

		return (short)flag;

	}
	
	public int lc_led(int icdev, int iLED, int on_off)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_led(icdev, iLED, on_off);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_setANT(int icdev, byte status)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_setANT(icdev, status);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_rfReset(int icdev, int time_ms)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_rfReset(icdev, time_ms);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 2;
		
		proData_obj.CMD = (int)protocol.COMPRO_RF_RST;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		pl_javacall.ULToMultiByte(time_ms, tmpChars, 2, 0);
		
		for(i=0; i< 2; i++)
			proData_obj.pData[i] = (byte)tmpChars [i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_card(int icdev,byte _Mode,byte[] _Snr, byte[] snSize, int[] outTag, byte[] outSak)
	{
		byte[] size= new byte[2];
	    int[] tagtype = new int[2];
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
	    int st =0;
	    int datalen;
	    int		flag;
	    int i;
	    
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_card(icdev, _Mode,_Snr,snSize,outTag, outSak);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.COMPRO_REQUEST;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		if(_Mode == 0)
			proData_obj.pData[0] = 0x26;//_Mode;
		else
			proData_obj.pData[0] = 0x52;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] < 6)
			return Lib.Error.ErrorCode.ERR_NO_USERDATA;
		
		outTag[0] = 0;
		for(i=0; i< 2; i++)
		{
			outTag[0] += (int)(inBuf[1-i] << 8*(1-i));
		}
		outSak[0] = inBuf[2];
		snSize[0] = (byte)(rlen[0] - 3);
		for(i=0; i< snSize[0];i++)
			_Snr[i] = inBuf[3 + i];

		return st;
	}
	
	public static final byte NFC_TYPE_MFCLS_1K		=0x00;
	public static final byte NFC_TYPE_MFCLS_4K		=0x01;
	public static final byte NFC_TYPE_MFPLUS_2K		=0x02;
	public static final byte NFC_TYPE_MFPLUS_4K		=0x03;
	public static final byte NFC_TYPE_ULT			=0x04;
	public static final byte NFC_TYPE_ULTC			=0x05;
	public static final byte NFC_TYPE_NTAG203		=0x06;
	public static final byte NFC_TYPE_NTAG210		=0x07;
	public static final byte NFC_TYPE_NTAG212		=0x08;
	public static final byte NFC_TYPE_NTAG213		=0x09;
	public static final byte NFC_TYPE_NTAG215		=0x0A;
	public static final byte NFC_TYPE_NTAG216		=0x0B;
	public static final byte NFC_TYPE_ICODE_SLI		=0x0C;
	public static final byte NFC_TYPE_ICODE_SLI_S	=0x0D;
	public static final byte NFC_TYPE_ICODE_SLI_L	=0x0E;
	public static final byte NFC_TYPE_ICODE_DNA		=0x0F;
	public static final byte NFC_TYPE_MF_MINI		=0x10;
	public static final byte NFC_TYPE_MF_DESFIRE		=0x11;
	public static final byte NFC_TYPE_14443_4		=(byte)0xF0;
	public static final byte NFC_TYPE_UNKONWN		=(byte)0xFF;
	
	private int lc_compare_chars(char[] c1, char[] c2, int size)
	{
		int i;
		int compareRel = 0;
		
		for(i = 0; i< size; i++)
		{
			if(c1[i] != c2[i])
			{
				compareRel = 0xff;
				break;
			}
		}
		return compareRel ;
	}
	
	private int lc_identify_TypeAChip(int icdev, int _local_tag, byte _local_sak, byte[] chipTypeOut, byte[] pfCompliant14443_4_out)
	{
		byte _tagType = 0;
		byte _chipType = 0;
		byte _compliant14443_4= 0;
		byte[] _rData = new byte[128];
		char[] _rcData = new char[128];
		char[] _tmpChars = new char[128];
		int  flag ;
		int i;
		int fRATS;
		byte[] _sCmd ={0x60};
		byte[] _ucRelen = new byte[2];
		char[] _chRelen = new char[2];
		char[] _mifarePlus_s_ats_history={0xC1,0x05,0x2F,0x2F,0x00,0x35,0xC7};
		char[] _mifarePlus_x_ats_history={0xC1,0x05,0x2F,0x2F,0x01,0xBC,0xD6};
		char[] _mifarePlus_se_ats1_history={0xC1,0x05,0x21,0x30,0x00,0xF6,0xD1};
		char[] _mifarePlus_se_ats2_history={0xC1,0x05,0x21,0x30,0x10,0xF6,0xD1};
		
		if((_local_sak & 0x20) != 0) _compliant14443_4 = 1;//Support 14443-4

		if(_local_sak == 0x00)
		{
			//Identify Ultralight / Ultra-C / NTAG203 by Memory(Ultralight-16 pages, NTAG203-42 pages)
			//Ultralight has 16 pages
			if( 0 == lc_read_NTag(icdev, (byte)15, _rData))
			{
				_tagType = NFC_TYPE_ULT;

				//NTAG210 has 20 pages
				if( 0 == lc_read_NTag(icdev, (byte)19, _rData))
				{
					_tagType = NFC_TYPE_NTAG210;

					//NTAG212 has 41 pages
					if( 0 == lc_read_NTag(icdev,(byte) 40, _rData))
					{
						_tagType = NFC_TYPE_NTAG212;

						//NTAG203 has 42 pages
						if( 0 == lc_read_NTag(icdev, (byte)41, _rData))
						{
							_tagType = NFC_TYPE_NTAG203;

							//NTAG213 has 45 pages
							if(0 == lc_read_NTag(icdev, (byte)44, _rData))
							{
								_tagType = NFC_TYPE_NTAG213;

								//Ultralight -C has 48 pages, but only 00~43 address can be read 
								flag = lc_read_NTag(icdev, (byte)43, _rData);
								if(0 == flag)
								{//Ultralight - C
									_tagType = NFC_TYPE_ULTC;
									//NTag215(135 page) / NTAG216(231 page)
									flag = lc_rawExchange(icdev, (byte)1, _sCmd, _ucRelen, _rData);
									if(0 == flag)
									{
										switch(_rData[6])
										{
										case 0x0F://NTAG213
											_tagType = NFC_TYPE_NTAG213;
											break;
										case 0x11://NTAG215
											_tagType = NFC_TYPE_NTAG215;
											break;
										case 0x13://NTAG216
											_tagType = NFC_TYPE_NTAG216;
											break;
										default:
											_tagType = NFC_TYPE_NTAG203;
											break;
										}
									}
								}//Ultralight -C has 48 pages, but only 00~43 address can be read 
							}//NTAG213 has 45 pages
						}//NTAG203 has 42 pages
					}//NTAG212 has 41 pages
				}//NTAG210 has 20 pages
			}//Ultralight has 16 pages
		}
		else if(_local_sak == 0x04)
		{
			_tagType = NFC_TYPE_MFCLS_1K;//Any Mifare CL1 ( double SN)
		}
		else if(_local_sak == 0x08)
		{
			//Do more a card reset to identify Class or plus, if success, should be plus
			fRATS = lc_pro_reset(icdev, _chRelen, _rcData);

			if(_local_tag == 0x0400) //Mifare Class 1K with 4-byte uid or Mifare plus with 4-bytes
			{
				//Provide more ways to identify Class or Plus
				if(0 == fRATS)
					_tagType = NFC_TYPE_MFPLUS_2K;
				else
					_tagType = NFC_TYPE_MFCLS_1K;
			}
			else if(_local_tag == 0x4400) //Mifare Class 1K with 7-byte uid
			{
				//Provide more ways to identify Class or Plus
				if(0 == fRATS)
					_tagType = NFC_TYPE_MFPLUS_2K;
				else
					_tagType = NFC_TYPE_MFCLS_1K;
			}
			else if(_local_tag == 0x4200)//Mifare Class 4K with 7-byte uid or MFPLUS
			{
				//Provide more ways to identify Class or PLUS
				if(0 == fRATS)
					_tagType = NFC_TYPE_MFPLUS_2K;
				else
					_tagType = NFC_TYPE_MFCLS_4K;
			}
			else if(_local_tag == 0x0200)//Mifare Class 4K with 4-byte uid or MFPLUS
			{
				//Provide more ways to identify Class or PLUS
				if(0 == fRATS)
					_tagType = NFC_TYPE_MFPLUS_2K;
				else
					_tagType = NFC_TYPE_MFCLS_4K ;
			}
		}
		else if(_local_sak == 0x09)
		{
			//if(_local_tag == 0x04)
			//	_tagType = NFC_TYPE_MF_MINI;
			//else
				_tagType = NFC_TYPE_MF_MINI;
		}
		else if(_local_sak == 0x10)
		{
			_tagType = NFC_TYPE_MFPLUS_2K;//Mifare Plus/ Mifare Plus CL2
		}
		else if(_local_sak == 0x11)
		{
			_tagType = NFC_TYPE_MFCLS_4K;//Mifare Plus/ Mifare Plus CL2
		}
		else if(_local_sak == 0x18)
		{
			if( (_local_tag == 0x0400) || (_local_tag == 0x4400))
				_tagType = NFC_TYPE_MFPLUS_4K;
			else if( (_local_tag == 0x0200) || (_local_tag == 0x4200))
				_tagType = NFC_TYPE_MFCLS_4K;//Mifare Class 4K
		}
		else if(_local_sak == 0x20)
		{
			if(_local_tag == 0x4403)
				_tagType = NFC_TYPE_MF_DESFIRE;//Mifare Desfire CL2/ EV1 CL2
			else
			{
				//Add a RESET (RATS) to card here to get ATS, and identify chip type by the history bytes!
				fRATS = lc_pro_reset(icdev, _chRelen, _rcData);
				if(0 == fRATS)
				{
					_tagType = NFC_TYPE_14443_4;

					//Should be older version of Mifare plus, to check history byte in the ATS data
					if(_chRelen[0] >= 8)
					{
						for(i=0; i< 7; i++)_tmpChars[i] = _rcData[_chRelen[0] - 7 + i];
						
						if( (0 == lc_compare_chars(_tmpChars,_mifarePlus_s_ats_history,7) ) ||
							(0 == lc_compare_chars(_tmpChars,_mifarePlus_x_ats_history,7 ) ) ||
							(0 == lc_compare_chars(_tmpChars,_mifarePlus_se_ats1_history,7) ) ||
							(0 == lc_compare_chars(_tmpChars,_mifarePlus_se_ats2_history, 7) )
							)
						{
							if( (_local_tag & 0x0200)!=0) _tagType = NFC_TYPE_MFPLUS_4K;
							else if( (_local_tag & 0x0400)!=0) _tagType = NFC_TYPE_MFPLUS_2K;
						}
					}
						
				}
			}
		}
		else if(_local_sak == 0x24)//Desfire
		{
			_tagType = NFC_TYPE_MF_DESFIRE;
		}
		else if(_local_sak == 0x28)//M1 + CPU
		{
			_tagType = NFC_TYPE_MFCLS_1K;
		}

		chipTypeOut[0] = _tagType;
		pfCompliant14443_4_out[0] = _compliant14443_4;
		
		return 0;
	}
	
	public int lc_requestAndIdentifyTypeA(int icdev,byte _Mode,byte[] outSNBuf, byte[] outSNBufferSize, 
			byte[] pfTagTypeOut, byte[] pfCompliant14443_4_out)
	{
	    int[] tagtype = new int[2];
	    int st =0;
	    byte[] sak = new byte[2];
	    
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_requestAndIdentifyTypeA(icdev, _Mode,outSNBuf,outSNBufferSize,pfTagTypeOut,pfCompliant14443_4_out);
		
		st = lc_card(icdev, _Mode,outSNBuf, outSNBufferSize, tagtype, sak);
		if(0 == st)
		{
			st = lc_identify_TypeAChip(icdev, tagtype[0], sak[0], pfTagTypeOut, pfCompliant14443_4_out);
		}

		return st;
	}
	public int  lc_authentication(int icdev,byte _Mode,byte blk, byte[] pKey)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_authentication(icdev, _Mode, blk, pKey);
	
			
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 8;

		proData_obj.CMD = (int)protocol.COMPRO_M1AUTHENPASS;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Mode;
		proData_obj.pData[1] = blk;
		for(i=0;i<6; i++)proData_obj.pData[2+i] = pKey[i];
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;

		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_read(int icdev,byte blk,byte[] _Data)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_read(icdev, blk, _Data);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.COMPRO_M1READ;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = blk;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] != 16)
			return Lib.Error.ErrorCode.ERR_NO_USERDATA;
		
		for(i=0; i< 16; i++)
			_Data[i] = inBuf[i];

		return Lib.Error.ErrorCode.ERR_OK;

	}


	public int  lc_read_convenient(int icdev, byte keyMode, byte blkAddr, byte[] pKey, byte[] pData_out)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_read_convenient(icdev, keyMode, blkAddr,pKey,pData_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 9;

		proData_obj.CMD = (int)protocol.COMPRO_M1READ_CONV;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = keyMode;
		proData_obj.pData[1] = blkAddr;
		proData_obj.pData[2] = 1;
		
		for(i=0; i< 6; i++)
			proData_obj.pData[3+i] = pKey[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] != 16)
			return Lib.Error.ErrorCode.ERR_NO_USERDATA;
		
		for(i=0; i< 16; i++)
			pData_out[i] = inBuf[i];

		return Lib.Error.ErrorCode.ERR_OK;

	}


	public int lc_write(int icdev,byte blk,byte[] _Data)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_write(icdev, blk, _Data);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 17;

		proData_obj.CMD = (int)protocol.COMPRO_M1WRITE;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = blk;
		for(i=0; i< 16; i++)
			proData_obj.pData[1+i] = _Data[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;

	}
	
	public int lc_write_convenient(int icdev,byte keyMode, byte blkAddr, byte[] pKey, byte[] pData_in)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_write_convenient(icdev, keyMode, blkAddr, pKey, pData_in);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 25;

		proData_obj.CMD = (int)protocol.COMPRO_M1WRITE_CONV;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = keyMode;
		proData_obj.pData[1] = blkAddr;
		for(i=0;i< 6; i++)
			proData_obj.pData[2+i] = pKey[i];
		
		for(i=0; i< 16; i++)
			proData_obj.pData[8+i] = pData_in[i];
		
		proData_obj.pData[24] = 1;//Need find card
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	

	public int lc_initval(int icdev,byte _Adr,int _Value)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_initval(icdev, _Adr, _Value);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 5;
		
		proData_obj.CMD = (int)protocol.COMPRO_INITVAL;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		pl_javacall.ULToMultiByte(_Value, tmpChars, 4, 1);
		
		for(i=0; i< 4; i++)
			proData_obj.pData[1 + i] = (byte)tmpChars [i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;

	}

	public int lc_increment(int icdev,byte _Adr,int _Value)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_increment(icdev, _Adr, _Value);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 5;
		
		proData_obj.CMD = (int)protocol.COMPRO_INCREMENT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		pl_javacall.ULToMultiByte(_Value, tmpChars, 4, 1);
		
		for(i=0; i< 4; i++)
			proData_obj.pData[1 + i] = (byte)tmpChars [i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_decrement(int icdev,byte _Adr,int _Value)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_decrement(icdev, _Adr, _Value);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 5;
		
		proData_obj.CMD = (int)protocol.COMPRO_DECREMENT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		pl_javacall.ULToMultiByte(_Value, tmpChars, 4, 1);
		
		for(i=0; i< 4; i++)
			proData_obj.pData[1 + i] = (byte)tmpChars [i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_readval(int icdev,byte _Adr,int[] pValue)
	{
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		int i;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_readval(icdev, _Adr, pValue);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;
		
		proData_obj.CMD = (int)protocol.COMPRO_READVAL;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] != 4)
			return Lib.Error.ErrorCode.ERR_NO_USERDATA;
		
		for(i=0; i<4; i++)
			tmpChars[i] = (char)inBuf[i];
		
		pl_javacall.MultiByteToUL(tmpChars, 4, 1, pValue);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_updateKey(int icdev, byte sector,byte[] pNewKeyA, byte[] pNewCtrlW, byte[] pNewKeyB)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_updateKey(icdev, sector, pNewKeyA, pNewCtrlW, pNewKeyB);
		else
		{
			byte[] nData = new byte[17];
			int i;
			byte  blkKey = 0;//(byte)(sector*4 + 3);
			
			for(i=0; i< 6; i++)
			{
				nData[i] = pNewKeyA[i];
				nData[10 + i] = pNewKeyB[i];
			}
			for(i=0; i< 4; i++)
			{
				nData[6 + i] = pNewCtrlW[i];
			}
			if(sector<32) blkKey = (byte)(sector*4+3);
			else if(sector<40) blkKey = (byte)(128 + (sector - 31)*16 -1);
			else 
				return Lib.Error.ErrorCode.ERR_PARA;
			
			return lc_write(icdev, blkKey, nData);
			//return Lib.Error.ErrorCode.ERR_NO_FUNC;
		}
	}

	public  int lc_read_NTag(int icdev,byte addr,byte[] Data_out)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_read_NTag(icdev, addr, Data_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.COMPRO_READ_TAG;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = addr;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;

		for(i=0; i< rlen[0]; i++)
			Data_out[i] = inBuf[i];

		return Lib.Error.ErrorCode.ERR_OK;
	}
	public  int lc_write_NTag(int icdev,byte addr,byte[] Data_in)
	{
		int i;
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_write_NTag(icdev, addr, Data_in);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 5;

		proData_obj.CMD = (int)protocol.COMPRO_WRITE_TAG;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = addr;
		for(i=0; i< 4; i++)
			proData_obj.pData[1+i] = Data_in[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_tag_authenPwd(int icdev, byte[] pKey_in)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_tag_authenPwd(icdev, pKey_in);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_tag_setPwd(
			int icdev, 
			byte tagType,
			byte[] pPwd_in,
			byte  protectType,
			byte protectStartAddr,
			byte maxErrorVerify,
			boolean	lockConfigAfterSet)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_tag_setPwd(icdev, tagType,pPwd_in, protectType,
					protectStartAddr,maxErrorVerify, lockConfigAfterSet);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_rawExchange(int icdev,byte slen,byte[] sbuff,byte[] rrlen,byte[] rbuff)
	{
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_rawExchange(icdev, slen, sbuff, rrlen, rbuff);
		
		proData_obj = new protocol.PROTOCOL_CMM();
		datalen = slen;
		proData_obj.CMD = (int)protocol.CMD_RAW_EXC;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0; i< slen; i++)
			proData_obj.pData[i] = (byte)sbuff[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		rrlen[0] = (byte)_rlen[0];

		for(i=0; i< _rlen[0]; i++)
			rbuff[i] = (byte)inBuf[i];

		return Lib.Error.ErrorCode.ERR_OK;
	}
	///contact lesss cpu card 
	public int lc_pro_reset(int IDevice,char[] rlen,char[] rbuf)
	{
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_pro_reset(IDevice, rlen, rbuf);
		
		proData_obj = new protocol.PROTOCOL_CMM();
		datalen = 0;
		proData_obj.CMD = (int)protocol.COMPRO_CLCPU_ATR;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		flag = mProtocol.rfab_CommonTransfer((int)IDevice,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		rlen[0] = (char)(_rlen[0] & 0xff);

		for(i=0; i< _rlen[0]; i++)
			rbuf[i] = (char)(inBuf[i]&0xff);

		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_pro_commandlink(int IDevice,short slen,char[] sbuff,char[] rlen,char[] rbuf,short tt,short fg)
	{
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[1024];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_pro_commandlink(IDevice, slen, sbuff, rlen, rbuf, tt, fg);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = slen + 2;

		proData_obj.CMD = (int)protocol.COMPRO_CLCPU_APDU;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)tt;
		proData_obj.pData[1] = (byte)fg;
		
		for(i=0; i< slen; i++)
			proData_obj.pData[2+i] = (byte)sbuff[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)IDevice,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		rlen[0] = (char)(_rlen[0] &0xff);
		for(i=0; i< rlen[0]; i++)
			rbuf[i] = (char)(inBuf[i]&0xff);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_getPBOC_PAN(int icdev, byte getMode, char[] pPAN)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getPBOC_PAN(icdev, getMode, pPAN);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_readSSC(int icdev, byte slot, char[] pSSC_Info)
	{
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[256];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.CMD_GET_SSC;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)slot;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		for(i=0; i< _rlen[0]; i++)
			pSSC_Info[i] = (char)(inBuf[i]&0xff);
		pSSC_Info[i] = 0x00;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_ultralt_C_authen(int icdev, byte[] key)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_ultralt_C_authen(icdev, key);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_ultralt_C_changePwd(int icdev, byte[] keyold,byte[] keynew)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_ultralt_C_changePwd(icdev, keyold, keynew);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_ultralt_C_setSafePage(int icdev, int ipage, boolean readSec)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_ultralt_C_setSafePage(icdev, ipage, readSec);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_ultralt_C_lockPage(int icdev, int flag)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_ultralt_C_lockPage(icdev, flag);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_authen_desfire(int icdev, byte keyNo, byte[] key,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_authen_desfire(icdev, keyNo, key, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_authen_desfire_aes(int icdev, byte keyNo, byte[] key,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_authen_desfire_aes(icdev, keyNo, key, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getver_desfire(int icdev,byte[] rlen,byte[] version)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getver_desfire(icdev, rlen, version);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getAIDs_desfire(int icdev,byte[] rlen,byte[] AIDS)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getAIDs_desfire(icdev, rlen, AIDS);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_selectApp_desfire(int icdev, byte[] AID)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_selectApp_desfire(icdev, AID);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getKeySetting_desfire(int icdev, byte[] rlen,byte[] setbuf)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getKeySetting_desfire(icdev, rlen, setbuf);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getKeyver_desfire(int icdev,byte keyNo,byte[] keyVer)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getKeyver_desfire(icdev, keyNo, keyVer);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_createApp_desfire(int icdev,byte[] AID,byte KeySetting,byte NumOfKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_createApp_desfire(icdev, AID, KeySetting, NumOfKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_delAID_desfire(int icdev,byte[] AID)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_delAID_desfire(icdev, AID);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_changeKeySetting_desfire(int icdev,byte newSet,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_changeKeySetting_desfire(icdev, newSet, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_changeKey_desfire(int icdev,byte[] sessionKey,byte[] curKey,byte keyNo,byte[] newkey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_changeKey_desfire(icdev,sessionKey, curKey, keyNo, newkey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_changeKey_desfire_aes(int icdev,byte[] sessionKey,byte[] curKey,byte keyNo,byte[] newkey,byte keyVersion)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_changeKey_desfire_aes(icdev,sessionKey, curKey, keyNo, newkey, keyVersion);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public  int lc_getFileIDs_desfire(int icdev, byte[] rlen, byte[] fileIDs)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getFileIDs_desfire(icdev,rlen, fileIDs);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getFileProper(int icdev,byte fileNo,byte[] rlen,byte[] fileProper)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getFileProper(icdev, fileNo, rlen, fileProper);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_changeFileSetting(int icdev,byte fileNo,byte comSet,byte[] accessRight,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_changeFileSetting(icdev, fileNo, comSet,accessRight,sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_createDataFile_desfire(int icdev,byte fileNo,byte ComSet,byte[] AccessRight,byte[] FileSize)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_createDataFile_desfire(icdev, fileNo, ComSet, AccessRight, FileSize);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_createBackupDataFile_desfire(int icdev,byte fileNo,byte ComSet,byte[] AccessRight,byte[] FileSize)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_createBackupDataFile_desfire(icdev, fileNo, ComSet, AccessRight, FileSize);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_createValueFile_desfire(int icdev,byte fileNo,byte ComSet,byte[] AccessRight,byte[] lowerLimit,byte[] upperLimit,byte[] value,byte creditEnabled)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_createValueFile_desfire(icdev, fileNo, ComSet, AccessRight, lowerLimit,upperLimit,value,creditEnabled);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_createCsyRecord_desfire(int icdev,byte fileNo,byte comSet,byte[] AccessRight,byte[] RecordSize,byte[] MaxNum)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_createCsyRecord_desfire(icdev, fileNo, comSet,AccessRight, RecordSize, MaxNum);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_delFile_desfire(int icdev,byte fileNo)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_delFile_desfire(icdev, fileNo);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_write_desfire(int icdev,byte fileNo,int offset,int length,byte[] data,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_write_desfire(icdev, fileNo, offset, length, data, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_read_desfire(int icdev,byte fileNo,int offset,int length,byte[] revData,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_read_desfire(icdev, fileNo, offset, length, revData, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getvalue_desfire(int icdev,byte fileNo,int[] value,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_getvalue_desfire(icdev, fileNo, value, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_credit_desfire(int icdev,byte fileNo,int value,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_credit_desfire(icdev, fileNo, value, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_debit_desfire(int icdev,byte fileNo,int value,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_debit_desfire(icdev, fileNo, value, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_writeRecord_desfire(int icdev,byte fileNo,int offset,int length,byte[] data,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_writeRecord_desfire(icdev, fileNo,offset, length, data, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_readRecord_desfire(int icdev,byte fileNo,int offset,int length,byte[] revData,int[] SgRecordlen,int[] rlen,byte[] sessionKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_readRecord_desfire(icdev, fileNo,offset, length, revData, SgRecordlen, rlen, sessionKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_clearRecord_desfire(int icdev,byte fileNo)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_clearRecord_desfire(icdev, fileNo);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_commitTransfer_desfire(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_commitTransfer_desfire(icdev);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_abortTransfer_desfire(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_abortTransfer_desfire(icdev);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_formatPICC_desfire(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_formatPICC_desfire(icdev);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL0_WritePerso(int icdev, byte[] key, short keyAddr)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL0_WritePerso(icdev, key, keyAddr);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL0_CommitPerso(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL0_CommitPerso(icdev);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL1_AuthenKeyL1(int icdev,byte[] key)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL1_AuthenKeyL1(icdev, key);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL1_SwitchToL2(int icdev,byte[] key)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL1_SwitchToL2(icdev, key);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL1_SwitchToL3(int icdev,byte[] key)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL1_SwitchToL3(icdev, key);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_MFPlusL2_SwitchToL3(int icdev,byte[] key)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL2_SwitchToL3(icdev, key);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	public int lc_MFPlusL3_AuthenL3Key(int icdev,byte[] key, short keyAddr)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_AuthenL3Key(icdev, key, keyAddr);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_AuthenSectorKey(int icdev,char keyType,int sectorNo,byte[] key)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_AuthenSectorKey(icdev, keyType, sectorNo, key);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_UpdateKey(int icdev, int keyAddr,byte[] newKey)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_UpdateKey(icdev, keyAddr,newKey);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_ReadWithPlain(int icdev, int blkAddr,byte blkNum, byte[] rdata)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_ReadWithPlain(icdev, blkAddr, blkNum, rdata);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_WriteWithPlain(int icdev, int blkAddr,byte blkNum, byte[] wdata)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_WriteWithPlain(icdev, blkAddr, blkNum, wdata);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_ReadWithEncrypt(int icdev, int blkAddr,byte blkNum,byte[] rdata,byte flag)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_ReadWithEncrypt(icdev, blkAddr, blkNum, rdata,flag);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_WriteWithEncrypt(int icdev, int blkAddr,byte blkNum,byte[] wdata,byte flag)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_WriteWithEncrypt(icdev, blkAddr, blkNum, wdata,flag);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_InitVal(int icdev, int blkAddr,long value)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_InitVal(icdev, blkAddr, value);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_ReadVal(int icdev, int blkAddr,long[] value)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_ReadVal(icdev, blkAddr, value);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_Increment(int icdev, int blkAddr,long value)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_Increment(icdev, blkAddr, value);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_MFPlusL3_Decrement(int icdev, int blkAddr,long value)
	{
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_MFPlusL3_Decrement(icdev, blkAddr, value);
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_findTypeB(int icdev,byte[] response_out, byte[] resLen_out)
	{
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[1024];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_findTypeB(icdev, response_out, resLen_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;

		proData_obj.CMD = (int)protocol.COMPRO_REQUESTB;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		resLen_out[0] = (byte)(_rlen[0] &0xff);
		for(i=0; i< resLen_out[0]; i++)
			response_out[i] = (byte)(inBuf[i]&0xff);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_typeB_command
    (
    	     int icdev,//Handle of device
    	     int slen,//The length of data to send
    	     byte[] sbuff,//Data to send
    	     byte[] rlen,//The length of data received
    	     byte[] rbuff//The data received
    	     )
	{
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[1024];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_typeB_command(icdev, slen, sbuff, rlen, rbuff);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = slen;

		proData_obj.CMD = (int)protocol.COMPRO_APDU_B;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0; i< slen; i++)
			proData_obj.pData[i] = (byte)sbuff[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		rlen[0] = (byte)(_rlen[0] &0xff);
		for(i=0; i< rlen[0]; i++)
			rbuff[i] = (byte)(inBuf[i]&0xff);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	

	public int lc_NFC_FormatCard_WithURI(int icdev, int cardType, int URItype, char[] szURI, char[] szPwd, boolean fLock)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_NFC_FormatCard_WithURI(icdev, cardType, URItype, szURI, szPwd, fLock);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_NFC_FormatCard_WithText(int icdev, int cardType,char[] szIANA, char[] szText,int textFormat,char[] szPwd, boolean fLock)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_NFC_FormatCard_WithText(icdev, cardType, szIANA, szText,textFormat,szPwd, fLock);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_NFC_FormatCard_WithSmartPoster(int icdev, int cardType,char[] szIANA, char[] szText, int textFormat, int URItype, 
	    											   char[] szURI,char[] szPwd,boolean fLock)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_NFC_FormatCard_WithSmartPoster(icdev, cardType, szIANA, szText,textFormat,URItype,szURI,szPwd,fLock);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_NFC_FormatCard_WithContact(int icdev, int cardType,
	    											   char[] szName, 
	    											   char[] szTelCell,
	    											   char[] szTelWork,
	    											   char[] szTelFax,
	    											   char[] szTelHome,
	    											   char[] szOrg,
	    											   char[] szDepart,
	    											   char[] szWorkAdr,
	    											   char[] szEmail,
	    											   char[] szURL,
	    											   char[] szXQQ,
	    											   char[] szWeibo,
	    											   char[] szWeixin,
	    											   int textFormat,
	    											   char[] szPwd,
	    											   boolean fLock)
	{
		
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_NFC_FormatCard_WithContact(icdev, 
    													cardType,
    													szName,
    													szTelCell,
    													szTelWork,
    													szTelFax,
    													szTelHome,
    													szOrg,
    													szDepart,
    													szWorkAdr,
    													szEmail,
    													szURL,
    													szXQQ,
    													szWeibo,
    													szWeixin,
    													textFormat,
    													szPwd,
    													fLock);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_NFC_ReadTag(int icdev, int tagType,int textFormat, char[] szPwd, int[] pPayloadType, char[] pContent_out)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_NFC_ReadTag(icdev, tagType, textFormat, szPwd, pPayloadType, pContent_out);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	/* ISO15693 */
    /* reqMode: The mode when finding card, 0x36-Single card mode, 0x16-Multiple card mode*/
    public int lc_find15693(
    	int icdev,
    	byte reqMode, 
    	byte[] response_out, 
    	byte[] resLen_out)
    {

		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_find15693(icdev, reqMode, response_out, resLen_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.CMD_15693_REQ;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)reqMode;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		resLen_out[0] = (byte)_rlen[0];
		for(i=0; i< _rlen[0]; i++)
			response_out[i] = (byte)inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
    
    }
    public int lc_15693_command
    (
     int icdev,//Handle of device
     byte cmd, //The command to send
     byte[] pUID, //The id of card (8 bytes)
     int sParaLen,//The length of paramter 
     byte[] psPara,//Paramter 
     int[] rParaLen,//The length of paramter received
     byte[] rPara//The parater received
     )
    {
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[1024];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_command(icdev, cmd, pUID, sParaLen, psPara, rParaLen, rPara);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 9 + sParaLen;

		proData_obj.CMD = (int)protocol.CMD_15693_EXCH_COMMOM;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = cmd;
		for(i=0; i< 8; i++)
			proData_obj.pData[1+i] = (byte)pUID[i];
		
		for(i=0; i< sParaLen; i++)
			proData_obj.pData[9+i] = (byte)psPara[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		rParaLen[0] = (int)_rlen[0];
		for(i=0; i< _rlen[0]; i++)
			rPara[i] = (byte)inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
    }
    public int lc_15693_command_custom
    (
     int icdev,//Handle of device
     byte cmd, //The command to send
     byte MfgCode, //IC Mfg code
     byte[] pUID, //The id of card (8 bytes)
     int sParaLen,//The length of paramter 
     byte[] psPara,//Paramter 
     int[] rParaLen,//The length of paramter received
     byte[] rPara//The parater received
     )
    {
		int i;
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[1024];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_command_custom(icdev, cmd, MfgCode, pUID, sParaLen, psPara, rParaLen, rPara);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 10 + sParaLen;

		proData_obj.CMD = (int)protocol.CMD_15693_EXCH_CUSTOM;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = cmd;
		proData_obj.pData[1] = MfgCode;
		for(i=0; i< 8; i++)
			proData_obj.pData[2+i] = (byte)pUID[i];
		
		for(i=0; i< sParaLen; i++)
			proData_obj.pData[10+i] = (byte)psPara[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		rParaLen[0] = (int)_rlen[0];
		for(i=0; i< _rlen[0]; i++)
			rPara[i] = (byte)inBuf[i];
		
		return Lib.Error.ErrorCode.ERR_OK;
    }
    public int lc_15693_stay_quiet(
    	int icdev,
    	byte[] pUID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_stay_quiet(icdev, pUID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_STAY_QUIET;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
		
    }
    public int lc_15693_select_uid(
    	int icdev,
    	byte[] pUID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_select_uid(icdev, pUID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_SELECT;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_reset_to_ready(
    	int icdev,
    	byte[] pUID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_reset_to_ready(icdev, pUID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_RESET_TO_READY;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_readBlock(
    	int icdev,
    	byte[] pUID,
    	byte startBlock,
    	byte blockNum,
    	byte[] rlen,
    	byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_readBlock(icdev, pUID, startBlock, blockNum, rlen, rbuffer);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			if(blockNum<=0||blockNum>6)
				return Lib.Error.ErrorCode.ERR_PARA;//parameter error

			//---- Set command
			if(blockNum > 1)
				_cmd = protocol.ISO15693_READ_MULTIPLE_BLOCKS;
			else
				_cmd = protocol.ISO15693_READ_SINGLE_BLOCK;

			//---- Set Paramters
			_para[0] = startBlock;
			if(blockNum > 1)
				_para[1] = blockNum;

			//---- Set paramters length
			if(blockNum > 1)
				_paraLen = 2;
			else
				_paraLen = 1;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				rbuffer);
			if(status == 0)
			{
				rlen[0]= (byte)_rParaLen[0];
			}

			return status;
		}
    }
    public int lc_15693_writeBlock(
    	int icdev,
    	byte[] pUID,
    	byte startBlock,
    	byte blocknum, 
    	byte wlen,
    	byte[] wbuffer)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_writeBlock(icdev, pUID, startBlock, blocknum, wlen, wbuffer);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			int i;
			byte[] _para = new byte[16];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];
			byte[] _uid = new byte[16];

			//---------------- Check paramter ---
			if( (blocknum > 6) || (wlen != 4*blocknum) )
				return Lib.Error.ErrorCode.ERR_PARA;//parameter error

			//---- Set command
			if(blocknum > 1)
				_cmd = protocol.ISO15693_WRITE_MULTIPLE_BLOCKS;
			else
				_cmd = protocol.ISO15693_WRITE_SINGLE_BLOCK;


			//---- Set Paramters
			for(i=0; i< 8; i++)
				_uid[i] = (byte)(pUID[i] & 0xff);
			
			_para[0] = startBlock;
			if(blocknum == 1)
			{
				for(i=0; i< wlen; i++)
					_para[1 + i] = wbuffer[i];
			}
			else
			{
				_para[1] = blocknum;
				for(i=0; i< wlen; i++)
					_para[2 + i] = wbuffer[i];
			}

			//---- Set paramters length
			if(blocknum == 1)
				_paraLen = 1 + wlen;
			else
				_paraLen = 2 + wlen;

			status  = lc_15693_command(icdev, 
				_cmd,
				_uid,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_lock_block(
    	int icdev,
    	byte[] pUID,
    	byte block)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_lock_block(icdev, pUID, block);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_LOCK_BLOCK;

			//---- Set Paramters
			_para[0] = block;

			//---- Set paramters length
			_paraLen = 1;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_write_afi(
    	int icdev,
    	byte[] pUID,
    	byte AFI)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_write_afi(icdev, pUID, AFI);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_WRITE_AFI;

			//---- Set Paramters
			_para[0] = AFI;

			//---- Set paramters length
			_paraLen = 1;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_lock_afi(
    	int icdev,
    	byte[] pUID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_lock_afi(icdev, pUID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_LOCK_AFI;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_write_dsfid(
    	int icdev,
    	byte[] pUID,
    	byte DSFID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_write_dsfid(icdev, pUID, DSFID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_WRITE_DSFID;

			//---- Set Paramters
			_para[0] = DSFID;

			//---- Set paramters length
			_paraLen = 1;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_lock_dsfid(
    	int icdev,
    	byte[] pUID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_lock_dsfid(icdev, pUID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_LOCK_DSFID;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_get_systemInfo(
    	int icdev,
    	byte[] pUID,
    	byte[] rlen,
    	byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_get_systemInfo(icdev, pUID, rlen, rbuffer);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_GET_SYSTEM_INFO;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				rbuffer);
			if(0 == status)
			{
				rlen[0] = (byte)_rParaLen[0];
			}

			return status;
		}
    }
    public int lc_15693_get_securityInfo(
    	int icdev,
    	byte[] pUID,
    	byte startBlock,
    	byte blockNum,
    	byte[] rlen,
    	byte[] rbuffer)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_get_securityInfo(icdev, pUID, startBlock, blockNum, rlen, rbuffer);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = protocol.ISO15693_GET_MULTIPLE_BLOCK_SECURITY;

			//---- Set Paramters
			_para[0] = startBlock;
			_para[1] = blockNum;
			//---- Set paramters length
			_paraLen = 2;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				rbuffer);
			if(0 == status)
			{
				rlen[0] = (byte)_rParaLen[0];
			}

			return status;
		}
    }
/////Custom commands
//pRandomOut: 2 bytes 
    public int _compro_15693_getRandom(int icdev,byte[] pUID,  byte[] pRandomOut)
    {
		int	status;
		byte _cmd;
		int _paraLen;
		byte[] _para = new byte[3];
		int[] _rParaLen = new int[2];
		byte[] _rPara = new byte[16];

		//---------------- Check paramter ---
		//---- Set command
		_cmd = (byte)protocol.ISO15693_GET_RANDDOM;

		//---- Set Paramters
		//---- Set paramters length
		_paraLen = 0;

		status  = lc_15693_command(icdev, 
			_cmd,
			pUID,
			_paraLen,
			_para,
			_rParaLen,
			pRandomOut);
		
		return status;
    }
    /* pwdType:01h-Read, 02h-Write, 04h-Privacy,08h-Destroy SLI-L, 10h-EAS */
    public int lc_15693set_password(
    	int icdev,
    	byte[] pUID, 
    	byte pwdType, 
    	byte[] pPwd)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693set_password(icdev, pUID, pwdType, pPwd);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			int i;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];
			byte[] pRandom = new byte[5];
			byte  _MfgCode = 0x04;
			
			status = _compro_15693_getRandom(icdev, pUID, pRandom);
			if(status != 0 )
				return status;

			pPwd[0] = (byte)(pRandom[0] ^ pPwd[0]);
			pPwd[1] = (byte)(pRandom[1] ^ pPwd[1]);
			pPwd[2] = (byte)(pRandom[0] ^ pPwd[2]);
			pPwd[3] = (byte)(pRandom[1] ^ pPwd[3]);

			//---------------- Check paramter ---
			//---- Set command
			_cmd = (byte)protocol.ISO15693_SET_PASS;

			//---- Set Paramters
			_para[0] = pwdType;
			for(i=0; i< 4; i++)
				_para[1 + i] = pPwd[i];
			
			//---- Set paramters length
			_paraLen = 5;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    /* pwdType:01h-Read, 02h-Write,04h-Privacy,08h-Destroy SLI-L, 10h-EAS , and privacy is default used*/
    public int lc_15693_write_password(
    	int icdev,
    	byte[] pUID, 
    	byte pwdType, 
    	byte[] pPwd)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_write_password(icdev, pUID, pwdType, pPwd);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			int i;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = (byte)protocol.ISO15693_WRITE_PASS;

			//---- Set Paramters
			_para[0] = pwdType;
			for(i=0; i< 4; i++)
				_para[1 + i] = pPwd[i];
			
			//---- Set paramters length
			_paraLen = 5;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    public int lc_15693_icode_64bitProtect(int icdev,byte[] pUID)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_icode_64bitProtect(icdev, pUID);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			int i;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = (byte)protocol.ISO15693_PROTECT_64BIT;

			//---- Set Paramters

			//---- Set paramters length
			_paraLen = 0;

			status  = lc_15693_command(icdev, 
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
    /*protectStatus:00h-Public,01h-Read and write protect by read-pass, 10h-Write protect by wirte pass. 
    	11h-Read protect by the read pass, and Write protect by write pass*/
    public int lc_15693_protect_page(
    	int icdev,
    	byte[] pUID, 
    	byte iPage, 
    	byte protectStatus)
    {
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_15693_protect_page(icdev, pUID, iPage, protectStatus);
		else
		{
			int	status;
			byte _cmd;
			int _paraLen;
			int i;
			byte[] _para = new byte[3];
			int[] _rParaLen = new int[2];
			byte[] _rPara = new byte[16];

			//---------------- Check paramter ---
			//---- Set command
			_cmd = (byte)protocol.ISO15693_PROTECT_PAGE;

			//---- Set Paramters
			_para[0] = iPage;
			_para[1] = protectStatus;

			//---- Set paramters length
			_paraLen = 2;

			status  = lc_15693_command(icdev,
				_cmd,
				pUID,
				_paraLen,
				_para,
				_rParaLen,
				_rPara);

			return status;
		}
    }
	public int lc_halt(int icdev)
	{
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_halt(icdev);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 0;

		proData_obj.CMD = (int)protocol.COMPRO_HALT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;

	}
	
	public int lc_findTypeF(int icdev,short sysCode, byte[] IDm_out, byte[] PMm_out)
	{
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		int  i;
		
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_findTypeF(icdev,sysCode, IDm_out, PMm_out);
    	
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 2;

		proData_obj.CMD = (int)protocol.CMD_REQUEST_TYPEF;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)(sysCode>>8 & 0xff);
		proData_obj.pData[1] = (byte)(sysCode & 0xff);
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(flag == 0)
		{
			for(i = 0; i< 8; i++)
			{
				IDm_out[i]= inBuf[i];
				PMm_out[i]= inBuf[8 + i];
			}
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_typeF_command
    (
    	     int icdev,//Handle of device
    	     int slen,//The length of data to send
    	     byte[] sbuff,//Data to send
    	     byte[] rlen,//The length of data received
    	     byte[] rbuff//The data received
    	     )
	{
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[2048];
		int		flag, datalen;
		int  i;
		
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_typeF_command(icdev, slen, sbuff, rlen, rbuff);
    	
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = slen;

		proData_obj.CMD = (int)protocol.CMD_TYPEF_APDU;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i =0; i< slen; i++)
			inBuf[i] = sbuff[i];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, _rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(flag == 0)
		{
			rlen[0] = (byte)_rlen[0];
			for(i = 0; i< _rlen[0]; i++)
			{
				rbuff[i]= inBuf[i];
			}
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_setNB_serverIP(int icdev, byte[] pIP_in)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_setNB_serverIP(icdev, pIP_in);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getNB_serverIP(int icdev, byte[] pIP_out)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_getNB_serverIP(icdev, pIP_out);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_setNB_serverPort(int icdev, int port)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_setNB_serverPort(icdev, port);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getNB_serverPort(int icdev, int[] pPort)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_getNB_serverPort(icdev, pPort);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_setNB_URL(int icdev, byte[] pURL_in)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_setNB_URL(icdev, pURL_in);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	public int lc_getNB_URL(int icdev, byte[] pURL_out)
	{
    	if(mCurPortType == PT_SERIAL)//serial
    		return comproCall.lc_getNB_URL(icdev, pURL_out);
    	else
    		return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
    //----------------------------- ICC --------------
    public int  lc_iccGetCardState(int icdev, short slot, byte[] pSt)
    {
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_iccGetCardState(icdev, slot, pSt);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.CMD_ICC_GET_CARDST;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)(slot & 0xff);
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(flag == 0)
		{
			pSt[0]= (byte)inBuf[0];
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
    }
    public int  lc_iccGetATR(int icdev, short slot,byte[] response_out, byte[] resLen_out)
    {
		int[] rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		int  i;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_iccGetATR(icdev, slot, response_out, resLen_out);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol.CMD_ICC_GET_ATR;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		proData_obj.pData[0] = (byte)(slot & 0xff);
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(flag == 0)
		{
			resLen_out[0] = (byte)(rlen[0]&0xff);
			
			for(i=0; i< rlen[0]; i++)
				response_out[i]= (byte)inBuf[i];
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
    }
    public int  lc_icc_APDU
    (
     int icdev,//Handle of device
     short slot,
     int slen,//The length of data to send
     byte[] sbuff,//Data to send
     int[] rlen,//The length of data received
     byte[] rbuff//The data received
     )
    {
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		int  i;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_icc_APDU(icdev, slot, slen, sbuff, rlen, rbuff);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = slen + 1;

		proData_obj.CMD = (int)protocol.CMD_ICC_APDU;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)(slot & 0xff);
		for(i=0; i< slen; i++)
			proData_obj.pData[1 + i] = sbuff[i];

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(flag == 0)
		{
			rlen[0] = rlen[0];
			
			for(i=0; i< rlen[0]; i++)
				rbuff[i]= (byte)inBuf[i];
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
    }
    
    public int  lc_icc_ReadMem
    (
     int icdev,//Handle of device
     short slot,
     int slen,//The length of data to send
     byte[] sbuff,//Data to send
     int[] rlen,//The length of data received
     byte[] rbuff//The data received
     )
    {
		int[] _rlen =  new int[2];
		protocol.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		int  i;
		
		if(mCurPortType == PT_SERIAL)
			return comproCall.lc_icc_APDU(icdev, slot, slen, sbuff, rlen, rbuff);
		
		proData_obj = new protocol.PROTOCOL_CMM();

		datalen = slen + 1;

		proData_obj.CMD = (int)protocol.CMD_ICC_APDU;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = (byte)(slot & 0xff);
		for(i=0; i< slen; i++)
			proData_obj.pData[1 + i] = sbuff[i];

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(flag == 0)
		{
			rlen[0] = rlen[0];
			
			for(i=0; i< rlen[0]; i++)
				rbuff[i]= (byte)inBuf[i];
		}
		
		return Lib.Error.ErrorCode.ERR_OK;
    }
    

}
