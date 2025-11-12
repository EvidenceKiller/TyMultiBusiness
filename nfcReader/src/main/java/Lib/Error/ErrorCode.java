package Lib.Error;


public class ErrorCode {
	
	//library error code list: zone 0x0100 ~ 0x0FFF
	public static final int ERR_OK = 0x0000;
	
	public static final int ERR_NOLINK = 0x0101;
	public static final int ERR_TRANS = 0x0102;
	public static final int ERR_NODATA_FOR_GET= 0x0103;
	public static final int ERR_TIMEOUT = 0x0104;
	public static final int ERR_NO_FUNC = 0x0105;
	
	public static final int ERR_NO_USERDATA = 0x0201;
	public static final int ERR_PARA = 0x0202;
	
	//app errors
	public static final int ERR_SHAKE_AUTHEN = 0x0301;
	
	//mifare plus error
	public static final int ERR_PLUS_GRANTED			=0x04ff;	//transfer cannot be granted
	public static final int ERR_PLUS_CRC_1				=0x0405; //wrong CRC value in 1st exchanged package
	public static final int ERR_PLUS_CRC_2				=0x0401; //wrong CRC value in 2nd exchanged package
	public static final int ERR_PLUS_CONDITION_SATISFY	=0x0404; //condition of use not satisfied
	public static final int ERR_PLUS_AUTHEN				=0x0406; //authentication error
	public static final int ERR_PLUS_CMD_OVERFLOW		=0x0407; //command overflow
	public static final int ERR_PLUS_INVALID_MAC		=0x0408; //invalid mac
	public static final int ERR_PLUS_INVALID_BLKNUM		=0x0409; //invalid block number
	public static final int ERR_PLUS_BLKNUM_NO			=0x040A; //not existing block number
	public static final int ERR_PLUS_INVALID_CMD		=0x040B; //the current command is not allowed on the requested block
	public static final int ERR_PLUS_LENGTH				=0x040C; //length error
	public static final int ERR_PLUS_MANIPULATION		=0x040F; //general manipulation error. failure in the operation of the picc

	public static final int ERR_PLUS_CMDOK				=0x0A; //Command success
	public static final int ERR_PLUS_OK					=0x90; //OK

}
