package Lib.Reader.MT;


import Lib.General.Convert;
import Lib.Reader.MT.protocol_rf_ab.PROTOCOL_CMM;
import jAB.ab_javacall;
import android.app.Activity;
import android.os.Handler;


public class FunctionV2 {
	
	//private protocol_rf_ab mProtocol;
	public protocol_rf_abV2 mProtocol;
	public static final char PT_SERIAL = 1;
	public static final char PT_USB = 2;
	public static final char PT_BLUETOOTH = 3;
	
	private char mCurPortType = PT_SERIAL;
	
	public FunctionV2(Activity hostAct,Handler handler)
	{
		mProtocol = new protocol_rf_abV2(hostAct, handler);		
	}
	
	public int lc_init_ex(int portType, char[] fpath, int baud)
	{
		int st = 0;
		
		mCurPortType = (char)portType;
		
		if(mCurPortType == PT_SERIAL)//serial
			st = ab_javacall.lc_init_ex(portType, fpath, baud);
		else
			st = mProtocol.protocol_open(portType, fpath, baud);
		
		return st;
	}
	public int  lc_exit(int icdev)
	{
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_exit(icdev);
		else
			return mProtocol.protocol_close(icdev);
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
	
	/*  convert commond char to Hex
	 * parameter:
		len:the length of hex buffer
	 * return null
	 */
	public int lc_getver(int icdev,char[] version)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_getver(icdev, version);

		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 0;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_GETVER;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if( flag ==0 )
		{
			if(rlen[0] != 0)
				for(i=0; i< rlen[0]; i++)version[i] =(char) inBuf[i];
			else
				flag = Lib.Error.ErrorCode.ERR_NO_USERDATA;
		}		

		return (short)flag;
	}

	//1->Read/Write, 2->Read only(Dec), 3->Read only(Hex), 4->Read only(with header)
	public int lc_setReaderMode(int icdev, byte mode)
	{
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_setReaderMode(icdev, mode);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_READER_MODE;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		proData_obj.pData[0] = mode;

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);

		return (short)flag;
	}
	
	//////////////////////////////////////// device functions /////
	public int lc_beep(int icdev,int _Msec)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_beep(icdev, _Msec);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 2;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_BEEP;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		for(i=0;i<2;i++)
		{
			proData_obj.pData[i]  = (byte)(_Msec >> 8* i);
		}

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);

		return (short)flag;

	}
	
	public int lc_request(int icdev,byte _Mode,int[] TagType)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_request(icdev, _Mode, TagType);

		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 1;
		if(0 == _Mode)_Mode = 0x26;
		else _Mode = 0x52;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_REQUEST;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];

		proData_obj.pData[0]  = (byte)(_Mode);

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		TagType[0] = 0;
		for(i = 0; i<2; i++)
		{
			TagType[0]  += (inBuf[1-i] << 8* (1-i));
		}

		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_card(int icdev,byte _Mode,long[] _Snr)
	{
		byte[] size= new byte[2];
	    int[] tagtype = new int[2];
	    int st =0;

		st = lc_request(icdev,_Mode,tagtype);

		return st;
	}
	
	public int lc_card_str(int lDevice,byte iMode,char[] pSnr)
	{
		int st, i;
		long[] pSN = new long[2];
		char[] arrSN = new char[8];
		
		st = lc_card(lDevice, iMode, pSN);
		if(0 ==st)
		{
			for(i=0; i< 4; i++) arrSN[i] = (char)( pSN[0] >> 8*i  & 0xff);
			hex_a(pSnr,arrSN, 8);
		}
		return st;
	}
	public int  lc_authenticate(int icdev,byte _Mode,byte blk, byte[] pKey)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_authenticate(icdev, _Mode, blk, pKey);
	
			
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 2;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_M1AUTHENPASS;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Mode;
		proData_obj.pData[1] = blk;

		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;

		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_mfRead(int icdev,byte blk,byte[] _Data)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_mfRead(icdev, blk, _Data);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 1;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_M1READ;
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
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_read_convenient(icdev, keyMode, blkAddr,pKey,pData_out);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 8;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_M1READ_CONV;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = keyMode;
		proData_obj.pData[1] = blkAddr;
		for(i=0; i< 6; i++)
			proData_obj.pData[2+i] = pKey[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		if(rlen[0] != 16)
			return Lib.Error.ErrorCode.ERR_NO_USERDATA;
		
		for(i=0; i< 16; i++)
			pData_out[i] = inBuf[i];

		return Lib.Error.ErrorCode.ERR_OK;

	}


	public int lc_mfWrite(int icdev,byte blk,byte[] _Data)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_mfWrite(icdev, blk, _Data);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 17;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_M1WRITE;
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
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_write_convenient(icdev, keyMode, blkAddr, pKey, pData_in);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 24;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_M1WRITE_CONV;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = keyMode;
		proData_obj.pData[1] = blkAddr;
		for(i=0;i< 6; i++)
			proData_obj.pData[2+i] = pKey[i];
		
		for(i=0; i< 16; i++)
			proData_obj.pData[8+i] = pData_in[i];
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;
	}

	public int lc_halt(int icdev)
	{
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_halt(icdev);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 0;

		proData_obj.CMD = (int)protocol_rf_ab.RFAB_HALT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
		
		return Lib.Error.ErrorCode.ERR_OK;

	}

	public int lc_updateKey(int icdev, byte keyType, byte _Adr,byte[] pCurKey, byte[] pNewPData)
	{
		byte sector;
		byte keyBlkAddr;
		
		if(_Adr < 128)
		{
			sector = (byte)(_Adr / 4);
			keyBlkAddr =(byte)( 4*sector + 3);
		}
		else
		{
			sector = (byte)((_Adr - 128)/16 + 32);
			keyBlkAddr = (byte)(128 + 16*(sector -32) + 15);
		}
		return lc_write_convenient(icdev, keyType, (byte)keyBlkAddr, pCurKey, pNewPData);
	}

	public int lc_initval(int icdev,byte _Adr,int _Value)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_initval(icdev, _Adr, _Value);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 5;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_INITVAL;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		FWPubLib.pl_javacall.ULToMultiByte(_Value, tmpChars, 4, 0);
		
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
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_increment(icdev, _Adr, _Value);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 5;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_INCREMENT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		FWPubLib.pl_javacall.ULToMultiByte(_Value, tmpChars, 4, 0);
		
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
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_decrement(icdev, _Adr, _Value);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 5;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_DECREMENT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = _Adr;
		
		FWPubLib.pl_javacall.ULToMultiByte(_Value, tmpChars, 4, 0);
		
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
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		char[] tmpChars =new char[8];
		int i;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_readval(icdev, _Adr, pValue);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 1;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_READVAL;
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
		
		FWPubLib.pl_javacall.MultiByteToUL(tmpChars, 4, 0, pValue);
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	public int lc_srd_flash(int icdev,int offset,int length, byte[] rec_buffer)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_srd_flash(icdev, offset, length, rec_buffer);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen =2;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_RFLASH;
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

	public int lc_swr_flash(int icdev,int offset,int length,byte[] buffer)
	{
		int i;
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_swr_flash(icdev, offset, length, buffer);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 2 + length;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_WFLASH;
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
	public int lc_setANT(int icdev,byte action)
	{
		int[] rlen =  new int[2];
		protocol_rf_abV2.PROTOCOL_CMM proData_obj;
		byte[] inBuf = new byte[255];
		int		flag, datalen;
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_setANT(icdev, action);
		
		proData_obj = new protocol_rf_abV2.PROTOCOL_CMM();

		datalen = 1;
		
		proData_obj.CMD = (int)protocol_rf_ab.RFAB_ANT;
		proData_obj.DATALEN = datalen;
		proData_obj.pData = new byte[proData_obj.DATALEN];
		
		proData_obj.pData[0] = action;
		
		flag = mProtocol.rfab_CommonTransfer((int)icdev,proData_obj, inBuf, rlen);
		if(flag != Lib.Error.ErrorCode.ERR_OK)
			return flag;
	
		
		return Lib.Error.ErrorCode.ERR_OK;
	}
	
	public int lc_getAutoReturnedData(int icdev, byte[] rData, int[] rlen)
	{
		int st =0;
		int _rlen = 0;
		byte[] tmpData = new byte[2];
		
		if(mCurPortType == PT_SERIAL)
			return ab_javacall.lc_getAutoReturnedData(icdev, rData, rlen);
		
		while(true)
		{
			st = mProtocol.rfab_readOneByte(icdev, tmpData);
			if(st != 0)
				break;
			else
			{
				rData[_rlen] = tmpData[0];
				_rlen++;
			}
		}
		
		rlen[0] = _rlen;
		if(_rlen != 0)
			st = 0;
		
		return st;
	}

}
