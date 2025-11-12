package Lib.General;

import android.util.Log;
import FWPubLib.pl_javacall;



public class Convert {
	
	public static final char ERR_GEN_PARA = 0x7D;
	
	public Convert()
	{
		
	}
	
	public int hex2asc(char[] strasc, char[] strhex, int lenASC)
	{
		pl_javacall publib = new pl_javacall();
		
		publib._hex_a(strhex, strasc, lenASC*2);
		
		return 1;
	}
	
	public int asc2hex(char[] strhex, char[] strasc, int lenASC)
	{
		pl_javacall publib = new pl_javacall();
		
		return publib._a_hex(strasc, strhex, lenASC);
	}

}