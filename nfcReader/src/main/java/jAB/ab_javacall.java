/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2012</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

package jAB;

public class ab_javacall
{
    static
    {
       System.loadLibrary("m2");
    }

	public native static int lc_init_ex(int portType, char[] fpath, int baud);
	public native static int  lc_exit(int icdev);
	public native static int lc_getver(int icdev,char[] version);
	//mode: 1->Read/Write, 2->Read only(Dec), 3->Read only(Hex), 4->Read only(with header)
	public native static int lc_setReaderMode(int icdev, byte mode);
	public native static int lc_beep(int icdev,int _Msec);
	public native static int lc_request(int icdev,byte _Mode,int[] TagType);
	public native static int  lc_anticoll(int icdev,byte _Bcnt,long[] _Snr);
	public native static int lc_select(int icdev,long _Snr,byte[] _Size);
	public native static int lc_card(int icdev,byte _Mode,long[] _Snr);
	public native static int lc_card_str(int lDevice,byte iMode,char[] pSnr);
	public native static int  lc_authenticate(int icdev,byte _Mode,byte blk, byte[] pKey);
	public native static int lc_mfRead(int icdev,byte blk,byte[] _Data);
	public native static int  lc_read_convenient(int icdev, byte keyMode, byte blkAddr, byte[] pKey, byte[] pData_out);
	public native static int lc_mfWrite(int icdev,byte blk,byte[] _Data);
	public native static int lc_write_convenient(int icdev,byte keyMode, byte blkAddr, byte[] pKey, byte[] pData_in);
	public native static int lc_halt(int icdev);
	public native static int lc_updateKey(int icdev, byte keyType, byte _Adr,byte[] pCurKey, byte[] pNewPData);
	public native static int lc_initval(int icdev,byte _Adr,int _Value);
	public native static int lc_increment(int icdev,byte _Adr,int _Value);
	public native static int lc_decrement(int icdev,byte _Adr,int _Value);
	public native static int lc_readval(int icdev,byte _Adr,int[] pValue);
	public native static int lc_read_NTag(int icdev,byte addr,byte[] Data_out);
	public native static int lc_write_NTag(int icdev,byte addr,byte[] Data_in);
	public native static int lc_srd_flash(int icdev,int offset,int length, byte[] rec_buffer);
	public native static int lc_swr_flash(int icdev,int offset,int length,byte[] buffer);
	public native static int lc_setANT(int icdev,byte action);
	public native static int lc_getAutoReturnedData(int icdev, byte[] rData, int[] rlen); 
    
}
