package Lib.COS;

import Lib.LCReader.T6.function_T6;
import Lib.LCReader.comPro.Function_comPro;
import FWPubLib.pl_javacall;
import android.R.string;
import android.util.Log;


public class FM1208 {
	
//	public static final int ERR_APDU  					=	0x0000ffff;		//锟斤拷锟筋发锟斤拷失锟杰凤拷锟斤拷锟斤拷
//	public static final int ERR_PARA					=	0x0000000f;
//	public static final int CMD_OK						=	0x00000000;				//锟斤拷锟斤拷锟斤拷确执锟叫凤拷锟斤拷锟斤拷
//	public static final char MF_PERMISSION				=	0x0f;			//MF锟斤拷锟斤拷锟侥硷拷锟斤拷删锟斤拷MF锟斤拷权锟斤拷
//	public static final char MF_DELCONDITION			=	0x02;			//MF锟斤拷锟斤拷锟缴撅拷锟組F锟斤拷锏斤拷锟阶刺拷锟缴撅拷锟組F前锟斤拷证锟斤拷锟斤拷员锟斤拷钥锟斤拷
//	public static final char DF_PERMISSION				=	0x0f;			//DF锟斤拷锟斤拷锟侥硷拷权锟斤拷
//	public static final char KEYEF_ADD_PERMISSION_ALL	=	0x0f;			//锟斤拷钥锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷钥权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟接ｏ拷
//	public static final char KEYEF_ADD_PERMISSION_USER	=	0x1f;			//锟斤拷钥锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷钥权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟接ｏ拷
//	public static final char KEYEF_ADD_PERMISSION_ADMIN	=	0x2f;			//锟斤拷钥锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷钥权锟睫ｏ拷锟斤拷锟斤拷员锟斤拷锟斤拷锟接ｏ拷
//	public static final char EF_READ_PERMISSION_ALL		=	0x0f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴讹拷锟斤拷
//	public static final char EF_READ_PERMISSION_USER	=	0x1f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴讹拷锟斤拷
//	public static final char EF_READ_PERMISSION_ADMIN	=	0x2f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷员锟缴讹拷锟斤拷
//	public static final char EF_UPDATE_PERMISSION_ALL	=	0x0f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴革拷锟铰ｏ拷
//	public static final char EF_UPDATE_PERMISSION_USER	=	0x1f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴革拷锟铰ｏ拷
//	public static final char EF_UPDATE_PERMISSION_ADMIN	=	0x2f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷员锟缴革拷锟铰ｏ拷
//	public static final char KEY_ID						=	0x01;			//锟斤拷钥锟斤拷识锟斤拷
//	public static final char KEY_VERSION				=	0x01;			//锟斤拷钥锟芥本锟斤拷
//	public static final char KEY_ALGORITHM				=	0x00;			//锟斤拷钥锟姐法锟斤拷识锟斤拷默锟斤拷Triple DES锟斤拷
//	public static final char KEY_USEPERMISION			=	0x0f;			//锟斤拷钥使锟斤拷权锟斤拷
//	public static final char KEY_SUCCEEDING_STATE_ADMIN	=	0x02;			//锟斤拷钥锟斤拷证锟斤拷锟斤拷状态锟斤拷锟解部锟斤拷证锟斤拷
//	public static final char KEY_SUCCEEDING_STATE_PIN	=	0x01;			//锟斤拷钥锟斤拷证锟斤拷锟斤拷状态锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷证锟斤拷
//	public static final char KEY_UPDATE_PERMISSION_ADMIN=	0x2f;			//锟斤拷钥锟睫革拷权锟睫ｏ拷锟斤拷锟斤拷员锟斤拷钥锟斤拷
//	public static final char KEY_UPDATE_PERMISSION_PIN	=	0x1f;			//锟斤拷钥锟睫革拷权锟睫ｏ拷锟斤拷锟斤拷锟斤拷钥锟斤拷
//	public static final int KEY_ERROR_COUNT				=	0xff;	
//	
//	public static final int DEVTYPE_S8 					= 	0x01;
//	public static final int DEVTYPE_S3					=	0x02;
	 
	public static final int  FW_KEYTYPE_FILEPROTECT       =(0x01);//文件线路保护密钥
	public static final int  FW_KEYTYPE_EXVERIFY          =(0x02);//外部认证密钥
	public static final int  FW_KEYTYPE_PIN               =(0x03);//口令密钥
	public static final int  FW_KEYTYPE_UNLOCK            =(0x04);//解锁口令密钥
	public static final int  FW_KEYTYPE_CREDIT			=(0x05);//圈存密钥
	public static final int  FW_KEYTYPE_DEBIT				=(0x06);//圈提密钥
	public static final int  FW_KEYTYPE_PURCHASE			=(0x07);//消费密钥
	public static final int  FW_KEYTYPE_OVERDRAWLIMIT     =(0x08);//修改透支限额
	public static final int  FW_KEYTYPE_INTERNAL			=(0x09);//内部密钥
	public static final int  FW_KEYTYPE_RELOADPIN			=(0x0A);//重装口令密钥
	
	public static final long FWCOS_SEC_ALW				=((long)0x00000000);	// 永远满足
	public static final long FWCOS_SEC_USER				=((long)0x00000001);	// 用户口令权限
	public static final long FWCOS_SEC_SO				=((long)0x0000000A); // 管理员权限
	public static final long FWCOS_SEC_NEVER			=((long)0x000000EF); // 永远不满足
	public static final boolean  fMaskSec = true;//FALSE;//是否将权限掩码，默认掩码，客户要求不掩码的设为非
	public static final int MAX_SIZE_FILE	=240;
	
	boolean IscreatendMF = false;//全锟街憋拷锟斤拷: MF锟角否激伙拷
	char cardstatus = 0;//全锟街憋拷锟斤拷:锟斤拷片状态
	int gl_curDevType = 0;
	private function_T6 call_contactLess = null;
	private Function_comPro call_comPro = null;
	private pl_javacall publib = new  pl_javacall();

	public FM1208(int devType, function_T6 obj_cls, Function_comPro obj_comPro )
	{
		gl_curDevType = devType;
		call_contactLess  = obj_cls;
		call_comPro = obj_comPro;
	}
	
	public int CPUAPDU_FUNC(long lDevice, int  cmdBufLen, char[] cmdBuf, char[] revBufLen, char[] revBuf )
	{
		short _waitTime = 9;//wait when transfer
		short _fragmentSize = 52;
		
		if(call_contactLess != null)
			return (int)call_contactLess.fw_pro_commandlink((int)lDevice, (short)cmdBufLen, cmdBuf, revBufLen, revBuf, _waitTime, _fragmentSize);
		else if(call_comPro != null)
			return call_comPro.lc_pro_commandlink((int)lDevice, (short)cmdBufLen, cmdBuf, revBufLen, revBuf, _waitTime, _fragmentSize);
	
		else
			return Lib.Error.ErrorCode.ERR_NO_FUNC;
	}
	
	void FM1208_GetTokenErrorType(char[] buffer, char bufLen,long[] type)
	{
	    char i=0,j= 0;
		char  codes[] = {0xff,0xff,0xff};

		if(bufLen>=2)i= (char)(bufLen -2);
		if(bufLen>=1)j= (char)(bufLen -1);

		codes[0] = (char)(buffer[i] & 0xff);
		codes[1] = (char)(buffer[j] & 0xff);

		if((codes[0] == 0x90) && (codes[1] ==0x00))
			type[0] = 0;//成功
		else if((codes[0] == 0x61))
			type[0] = 0;//0x80616100  ;// 		正常结束，仍有XX个有效数据可取
		else if((codes[0] == 0x63) && (codes[1] >= 0xC0) && (codes[1] <= 0xCF))
			type[0] = (0x80616300 | codes[1]);//剩余尝试次数 
		else if((codes[0] == 0x65) && (codes[1] ==0x81))
			type[0] = 0x80616581 ; // 		写EEPROM失败  
		else if((codes[0] == 0x67) && (codes[1] ==0x00))
			type[0] = 0x80616700   ; // 		写EEPROM失败  
		else if((codes[0] == 0x69) && (codes[1] ==0x01))
			type[0] = 0x80616901   ; // 		无效的状态
		else if((codes[0] == 0x69) && (codes[1] ==0x81))
			type[0] = 0x80616981 ; // 		文件类型不匹配  
		else if((codes[0] == 0x69) && (codes[1] ==0x82))
			type[0] = 0x80616982 ; // 		安全状态不满足  
		else if((codes[0] == 0x69) && (codes[1] ==0x83))
			type[0] = 0x80616983 ; // 		密钥已经被锁住   
		else if((codes[0] == 0x69) && (codes[1] ==0x85))
			type[0] = 0x80616985 ; // 		使用条件不满足 
		else if((codes[0] == 0x69) && (codes[1] ==0x88))
			type[0] = 0x80616988  ;// 		安全报文数据项不正确 
		else if((codes[0] == 0x6A) && (codes[1] ==0x80))
			type[0] = 0x80616A80;  // 		数据域参数不正确  
		else if((codes[0] == 0x6A) && (codes[1] ==0x81))
			type[0] = 0x80616A81;  // 		功能不支持    
		else if((codes[0] == 0x6A) && (codes[1] ==0x82))
			type[0] = 0x80616A82;  // 		没有找到文件    
		else if((codes[0] == 0x6A) && (codes[1] ==0x83))
			type[0] = 0x80616A83;  // 		没有找到记录   
		else if((codes[0] == 0x6A) && (codes[1] ==0x84))
			type[0] = 0x80616A84;  // 		没有足够的空间     
		else if((codes[0] == 0x6A) && (codes[1] ==0x86))
			type[0] = 0x80616A86;  // 		P1，P2参数不正确   
		else if((codes[0] == 0x6B) && (codes[1] ==0x00))
			type[0] = 0x80616B00;  // 	参数错误（偏移地址超出了EF文件长度）
		else if((codes[0] == 0x6D) && (codes[1] ==0x00))
			type[0] = 0x80616D00;  // 	不正确的INS 
		else if((codes[0] == 0x6E) && (codes[1] ==0x00))
			type[0] = 0x80616E00;  // 	不正确的CLA  
		else if((codes[0] == 0x6F) && (codes[1] ==0x00))
			type[0] = 0x80616F00;  // 	没有精确的诊断   
		else if((codes[0] == 0x93) && (codes[1] ==0x02))
			type[0] = 0x80619302;  // 	MAC错误
		else if((codes[0] == 0x93) && (codes[1] ==0x03))
			type[0] = 0x80619303;  // 	应用被锁定
		else if((codes[0] == 0x94) && (codes[1] ==0x03))
			type[0] = 0x80619403;  // 	密钥未找到
		else type[0] = 0x8061FFFF;//未知错误
	}
	
	public long  FWTokenGetRnd(
			 long			lDevHandle,
			 long	ulRndLen,
			 char[]			pValue)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int lenCmd;
		char reLen[] = new char[2];
		char CLA = 0x00;
		char INS = 0x84;
		char P1  = 0x00;//建立EF
		char P2  = 0x00;//正在建立，01 --建立结束
		char Le;
		char i;
		
		//////////////////////////////////////////数据域
		///////////////////////////////////////////////////////////
		if(ulRndLen<4)
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误

		Le  = (char)(ulRndLen);//文件信息的长度,注：仅当长度不超过255的时候才可以

		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *
		cmdbuf[4] = Le; //Lc    文件信息的长度
		
		lenCmd = 5;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];

		for(i=0; i< Le; i++)
			pValue[i] = retbuf[i];

		return ret[0];
	}
	
	public long FWCosCreateKeyFile(long lDevHandle,
			short	usDirFileID,
			short	usFileID,
			short  uFileLen,
			long	ulGenSec)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		char CLA = 0x80;
		char INS = 0xE0;

		char P1  = (char)(((usFileID>>8) & 0x00FF));//0x00;//0x02;//建立EF
		char P2  = (char)((usFileID & 0x00FF));//0x00;//正在建立，01 --建立结束

		//////////////////////////////////////////数据域
		char DATA[] = new char[512];

		int  lenData = 7;
		char Lc;

		DATA[0] = 0x3F;//(0x3f | (1<<7));

		DATA[1] = (char)(((uFileLen>>8) & 0x00FF));//0x00;//文件大小
		DATA[2] = (char)((uFileLen & 0x00FF));//0x70;

		DATA[3] = (char)((usDirFileID & 0x1F));//低5位为短文件标识符
		if( usDirFileID == 0x0095)// 100 1 0101 ADF
			DATA[3] = 0x95;

		if(true == fMaskSec)
			DATA[4] = (char)(0xF0 | ulGenSec);//0xf0;  //增加密钥的权限
		else
			DATA[4] = (char)( ulGenSec);//0xf0;  //增加密钥的权限


		DATA[5] = 0xff;
		DATA[6] = 0xff;

		///////////////////////////////////////////////////////////

		Lc  = (char)(lenData);//文件信息的长度,注：仅当长度不超过255的时候才可以


		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	
	public long FWCosAddKey(long lDevHandle,
			  char bKeyType,
			  char[] pbKeyValue,
			  long ulKeyValueLen)
	{
		long ret[] = new long[2];
		char cmdbuf[] = new char[256];
		int lenCmd =0;
		int i=0;
		long ftoken;
		char retbuf[]= new char[256];
		char reLen[] = new char[2];
		char CLA = 0x80;
		char INS = 0xD4;
		char P1  = 0x01;//01 表示要安装密钥
		char P2  = 0x00;//0x00或密钥标识符
		//////////////////////////////////////////数据域
		char DATA[] = new char[512];
		char Lc;

		if((ulKeyValueLen!=16)&&(ulKeyValueLen!=8) && (bKeyType != FW_KEYTYPE_PIN))
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误

		if((FW_KEYTYPE_PIN == bKeyType) && (ulKeyValueLen >8))
			return Lib.Error.ErrorCode.ERR_PARA;

		switch(bKeyType){
		case FW_KEYTYPE_FILEPROTECT://文件线路保护密钥
		{
			DATA[0] = (0x36 | (3<<6));
			DATA[1] = 0xF0;//使用权限
			DATA[2] = 0xF0;//更改权限
			DATA[3] = 0xFF;
			DATA[4] = 0xFF;//0x33;
		}
		break;
		case FW_KEYTYPE_EXVERIFY://外部认证密钥
		{
			DATA[0] = (0x39 | (3<<6));
			DATA[1] = 0xF0;//使用权限
			DATA[2] = 0xF0;//更新权限
			DATA[3] = 0xAA;//后续状态
			DATA[4] = 0xFF;//0x66;
		}
		break;
		case FW_KEYTYPE_PIN://口令密钥 
		{
			DATA[0] = 0x3A ; //-2019.8.20
			//DATA[0] = 0xFA;//(0x3A | (3<<6)); //+2019.8.20 verify with crypt
			DATA[1] = 0xF0;//使用权限
			DATA[2] = 0xEF;
			DATA[3] = 0x01;//后续状态
			DATA[4] = 0xFF;//0x33;
		}
		break;
		case FW_KEYTYPE_UNLOCK://解锁口令密钥
			//80D4010015 3BF002FF33 FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
		{
			DATA[0] = 0xF7;//0x37;//0x3B;
			DATA[1] = 0xF0;
			DATA[2] = 0xAA;//0x02;
			DATA[3] = 0xFF;
			DATA[4] = 0x33;//0xFF;//0x33;
		}
		break;
		case FW_KEYTYPE_CREDIT://圈存密钥
		{
			DATA[0] = (0x3F|(3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = 0xF0;//更改权
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x00;//算法标识

		}
		break;
		case FW_KEYTYPE_PURCHASE://消费密钥
		{
			DATA[0] = (0x3E|(3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = 0xF0;//更改权
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x00;//算法标识
		}
		break;
		case FW_KEYTYPE_DEBIT://圈提密钥
		{
			DATA[0] = 0x3D;
			DATA[1] = 0xF0;//使用权
			DATA[2] = 0xF0;//更改权
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x01;//算法标识
		}
		break;
		case FW_KEYTYPE_RELOADPIN://重装口令密钥
		{
			DATA[0] = 0xF8;
			DATA[1] = 0xF0;//使用权限
			DATA[2] = 0xAA;//更改权限
			DATA[3] = 0xFF;
			DATA[4] = 0x33;
		}
		break;
		case FW_KEYTYPE_OVERDRAWLIMIT://修改透支限额密钥
		{
			DATA[0] = 0x3C;
			DATA[1] = 0xF0;//使用权
			DATA[2] = 0xF0;//更改权
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x01;//算法标识
		}
		break;

		default:
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误;
		}

		for(i=0; i< ulKeyValueLen; i++)DATA[5 + i] = pbKeyValue[i];

		///////////////////////////////////////////////////////////

		Lc  = (char)(ulKeyValueLen + 5);//文件信息的长度,注：仅当长度不超过255的时候才可以


		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];

	}
	
	public  long FWCosAddKeyEx(long lDevHandle,
		    char bKeyType,
		    char ucKeyID,
            char[] pbKeyValue,
		    long ulKeyValueLen,
		    char ucSecUpdate,
		    char ucNextSt,
		    char	ucMaxCntError,
		    char cryptyType)
	{
		long ret[] = new long[2];
		char cmdbuf[] = new char[256];
		int lenCmd =0;
		int i=0;
		long ftoken;
		char retbuf[] = new char[256];
		char reLen[] = new char[2];
		char maxCountErr;
		char CLA = 0x80;
		char INS = 0xD4;
		char P1  = 0x01;//01 表示要安装密钥
		char P2  = ucKeyID;//0x00;//0x00或密钥标识符
		//////////////////////////////////////////数据域
		char DATA[] =new char[512];
		char Lc;


		if((ulKeyValueLen!=16)&&(ulKeyValueLen!=8) && (bKeyType != FW_KEYTYPE_PIN))
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误

		if((FW_KEYTYPE_PIN == bKeyType) && (ulKeyValueLen >8))
			return Lib.Error.ErrorCode.ERR_PARA;

		if(cryptyType >2 )
			return Lib.Error.ErrorCode.ERR_PARA;

		if(ucMaxCntError<1 || ucMaxCntError>15)
			return Lib.Error.ErrorCode.ERR_PARA;//+add by cdt.2011.6.17

		maxCountErr = (char)(((ucMaxCntError<<4) & 0xf0) | (ucMaxCntError & 0x0f));

		switch(bKeyType){
		case FW_KEYTYPE_FILEPROTECT://文件线路保护密钥
			//80D401000D 36F0F0FF33 FFFFFFFFFFFFFFFF (0x0D)
		{
			DATA[0] = (0x36 | (3<<6));
			DATA[1] = 0xF0;//使用权限
			if(true== fMaskSec)
				DATA[2] = (char)(0xF0 | ucSecUpdate);//更改权限
			else
				DATA[2] = ucSecUpdate;//更改权限

			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = 0xFF;
			DATA[4] = maxCountErr;//0x33;
		}
		break;
		case FW_KEYTYPE_EXVERIFY://外部认证密钥
			//80D4010015 39F0F0AA33 FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
		{
			DATA[0] = (0x39 | (3<<6));
			DATA[1] = 0xF0;//使用权限
			if(true == fMaskSec)
				DATA[2] = (char)((0xF0 |ucSecUpdate));//更改权限
			else
				DATA[2] = (ucSecUpdate);//更改权限

			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = ucNextSt;//后续状态
			DATA[4] = maxCountErr;//0x66;
		}
		break;
		case FW_KEYTYPE_PIN://口令密钥
			// 80D401000D 3AF0EF0133 888888FFFFFFFFFF  
		{
			DATA[0] = 0x3A;//(0x3A | (3<<6)); //-2019.8.20
			//DATA[0] = 0xFA;//(0x3A | (3<<6)); //+2019.8.20 verify with crypt
			DATA[1] = 0xF0;//使用权限
			DATA[2] = 0xEF;
			DATA[3] = ucNextSt;//后续状态
			DATA[4] = maxCountErr;//0x33;
		}
		break;
		case FW_KEYTYPE_UNLOCK://解锁口令密钥
			//80D4010015 3BF002FF33 FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
		{
			DATA[0] = (0x37 | (3<<6));//0x3B;
			DATA[1] = 0xF0;
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = 0xFF;
			DATA[4] = maxCountErr;//0x33;
		}
		break;
		case FW_KEYTYPE_CREDIT://圈存密钥
		{
			DATA[0] = (0x3F | (3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] =(char) FWCOS_SEC_NEVER;
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x01;//算法标识
		}
		break;
		case FW_KEYTYPE_PURCHASE://消费密钥
		{
			DATA[0] = (0x3E | (3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x01;//算法标识
		}
		break;
		case FW_KEYTYPE_DEBIT://圈提密钥
		{
			DATA[0] =( 0x3D|(3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x01;//算法标识
		}
		break;
		case FW_KEYTYPE_OVERDRAWLIMIT://修改透支限额密钥
		{
			DATA[0] =(0x3C | (3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = 0x01;//密钥版本号
			DATA[4] = 0x01;//算法标识
		}
		break;
		case FW_KEYTYPE_INTERNAL://内部密钥
		{
			DATA[0] = (0x34|(3<<6));
			DATA[1] = 0xF0;//使用权
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] = (char)FWCOS_SEC_NEVER;
			DATA[3] = 0x03;//密钥版本号
			DATA[4] = 0x00;//算法标识
		}
		break;
		case FW_KEYTYPE_RELOADPIN://重装口令密钥
		{
			DATA[0] = (0x38|(3<<6));
			DATA[1] = 0xF0;//使用权限
			DATA[2] = ucSecUpdate;//更改权限
			if( ucSecUpdate == FWCOS_SEC_NEVER)
				DATA[2] =(char) FWCOS_SEC_NEVER;

			DATA[3] = 0xFF;
			DATA[4] = maxCountErr;//0x33;
		}
		break;
		default:
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误;
		}

		for(i=0; i< ulKeyValueLen; i++)DATA[5 + i] = pbKeyValue[i];

		///////////////////////////////////////////////////////////

		Lc  = (char)(ulKeyValueLen + 5);//文件信息的长度,注：仅当长度不超过255的时候才可以


		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];

	}
    public long  FWCosUpdateKey(long lDevHandle,
		     char[] pbOldKey,
		     long ulOldKeyLen,
		     char[] pbNewKey,
		     long ulNewKeyLen,
		     long bKeyType)
   {
	   	long ret[] = new long[2];
	   	long ftoken;
	   	char cmdbuf[] = new char[256];
	   	char retbuf[]= new char[256];
	   	int i;
	   	int lenCmd;
	   	char reLen[] = new char[2];
	   	char CLA = 0x80;
	   	char INS = 0x0E;
	   	char P1  = 0x00;//建立MF
	   	char P2  = 0x00;//正在建立，01 --建立结束
	   	//////////////////////////////////////////数据域
	   	char DATA[] = new char[256];
	   	int  lenData = 0x00;//0x08;
	   	char[] maintainKey = new char[17];
	   	char[] plainData = new char[256];
	   	int cCiphertype;
	   	int cipherLen;
		char[] rndBuf = new char[5];
		char[] InitDATA = new char[9];
		char[] MACCipher= new char[256];
		char[] MAC = new char[5];
		char[] cipherData = new char[256];
	
	   	///////////////////////////////////////////////////////////
	   	char Lc  = 0;//文件信息的长度,注：仅当长度不超过255的时候才可以
	   	
	   	if((ulNewKeyLen!=16)&&(ulNewKeyLen!=8))
	   		return Lib.Error.ErrorCode.ERR_PARA;;//参数错误
	
	   	if((ulOldKeyLen!=16)&&(ulOldKeyLen!=8))
	   		return Lib.Error.ErrorCode.ERR_PARA;//参数错误
	   	
	   	switch((char)bKeyType){
	   	case FW_KEYTYPE_FILEPROTECT://文件线路保护密钥
	   		//80D401000D 36F0F0FF33 FFFFFFFFFFFFFFFF (0x0D)
	   		{
	   			CLA     = 0x84;
	   			P1      = 0x36;
	   			P2      = 0x00;
	
	   			DATA[0] = (0x36 | (3<<6));
	   			DATA[1] = 0xF0;//使用权限
	   			DATA[2] = 0xF0;//更改权限
	   			DATA[3] = 0xFF;
	   			DATA[4] = 0xFF;//0x33;//可以改为 66
	   		}
	   		break;
	   	case FW_KEYTYPE_EXVERIFY://外部认证密钥
	   		//80D4010015 39F0F0AA33 FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
	   		{
	   			CLA     = 0x84;
	   			P1      = 0x39;
	   			P2      = 0x00;
	
	   			DATA[0] = (0x39 | (3<<6));
	   			DATA[1] = 0xF0;//使用权限
	   			DATA[2] = 0xF0;//更新权限
	   			DATA[3] = 0xAA;//后续状态
	   			DATA[4] = 0xFF;//0x33;
	   		}
	   		break;
	   	case FW_KEYTYPE_PIN://口令密钥
	   		// 80D401000D 3AF0EF0133 888888FFFFFFFFFF  
	   		{
	   			CLA     = 0x84;
	   			P1      = 0x3A ;//0x3A;
	   			P2      = 0x00;
	
	   			DATA[0] = (0x3A | (3<<6));
	   			DATA[1] = 0xF0;//使用权限
	   			DATA[2] = 0xEF;
	   			DATA[3] = 0x01;//后续状态
	   			DATA[4] = 0xFF;//0x33;
	   		}
	   		break;
	   	case FW_KEYTYPE_UNLOCK://解锁口令密钥
	   		//80D4010015 3BF002FF33 FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
	   		{
	   			CLA     = 0x84;
	   			P1      = 0x3B;
	   			P2      = 0x00;
	
	   			DATA[0] = (0x3B | (3<<6));
	   			DATA[1] = 0xF0;
	   			DATA[2] = 0x02;
	   			DATA[3] = 0xFF;
	   			DATA[4] = 0xFF;//0x33;
	   		}
	   		break;
	   	case FW_KEYTYPE_CREDIT:
	   		{
	   			CLA     = 0x84;
	   			P1      = 0x3F;
	   			P2      = 0x00;//密钥标识
	
	   			DATA[0] = (0x3F|(3<<6));
	   			DATA[1] = 0xF0;//使用权
	   			DATA[2] = 0xF0;//更改权限
	   			DATA[3] = 0x01;//密钥版本号
	   			DATA[4] = 0x00;//算法标识
	
	   		}
	   		break;
	   	case FW_KEYTYPE_PURCHASE:
	   		{
	   			CLA     = 0x84;
	   			P1      = 0x3E;
	   			P2      = 0x00;//密钥标识
	
	   			DATA[0] = (0x3E | (3<<6));
	   			DATA[1] = 0xF0;//使用权
	   			DATA[2] = 0xF0;//更改权限
	   			DATA[3] = 0x01;//密钥版本号
	   			DATA[4] = 0x00;//算法标识
	   		}
	   		break;
	   	default:
	   		return Lib.Error.ErrorCode.ERR_PARA;//参数错误;
	   	}
	
	   	for(i=0; i< ulNewKeyLen; i++)
	   		DATA[5 + i] = pbNewKey[i];
	
	   	//////////////////////////////////////////数据域
	   	lenData = 0x00;//0x08;
	
	   	///////////////////////////////////////////////////////////
	   	Lc  = (char)(ulNewKeyLen + 5);;//文件信息的长度,注：仅当长度不超过255的时候才可以
	
	
	   	cmdbuf[0] = CLA;//CLA    *
	   	cmdbuf[1] = INS;//INS    * 命令头 *
	   	cmdbuf[2] = P1;//P1     *
	   	cmdbuf[3] = P2;//P2     *
	
	   	cmdbuf[4] = Lc; //Lc    文件信息的长度
	   	
	   	for(i = 0; i< Lc ; i++)
	   		cmdbuf[5 +i] = DATA[i];//数据域
	
	   	lenCmd = Lc + 5;
	   	
	   	for(i = 0; i< ulOldKeyLen;i++)
	   		maintainKey[i] = pbOldKey[i];
		
		//设置加密方式
		if(ulOldKeyLen == 8)
			cCiphertype = 1;
		else cCiphertype =3;
		
		plainData[0] = Lc;
		for(i=0; i< Lc; i++)
			plainData[1 + i] = DATA[i];//明文内容
		

		if(1 == cCiphertype){
			//DES加密，生成密文
			ret[0] =(long)pl_javacall.ic_encrypt_ECB(maintainKey,plainData,(short)(Lc +1),cipherData,InitDATA);
			if(ret[0] != 0) return ret[0];
		}else{
			//3DES加密，生成密文
			ret[0] =pl_javacall.ic_encrypt_3DES_ECB(maintainKey,plainData,(short)(Lc +1),cipherData);
			if(ret[0] != 0) return ret[0];
		}
		cipherLen = pl_javacall.GetCipherReLen(Lc + 1);
		
		//从卡片取4字节随机数,并设置初始值
		FWTokenGetRnd(lDevHandle,4,rndBuf);
		
		for(i=0;i<4;i++){InitDATA[i] = rndBuf[i];InitDATA[4+i]=0x00;}
		//计算MAC
		DATA[0] = CLA;
		DATA[1] = INS;
		DATA[2] = P1;
		DATA[3] = P2;
		
		DATA[4] = (char)(cipherLen + 4);
		
		for(i=0;i<(int)cipherLen;i++)DATA[5 + i] = cipherData[i];
		
		if(1== cCiphertype){
			ret[0] = pl_javacall.ic_encrypt_ECB(maintainKey,DATA,(short)(cipherLen + 4 +1) ,MACCipher,InitDATA);
			if(ret[0] != 0) return ret[0];
			
			for(i=0;i<4;i++)MAC[i] = MACCipher[pl_javacall.GetCipherReLen(cipherLen + 4 +1)-8 +i];
		}else{
			//3DES加密，生成密文
			ret[0] = pl_javacall.ic_encrypt_3DES_ECB_MAC(maintainKey,DATA,(short)(cipherLen + 4 +1), MACCipher,InitDATA);
			if(ret[0] != 0) return ret[0];
			
			for(i=0;i<4;i++)MAC[i] = MACCipher[i];
		}
		//
		// 填充数据域
		//
		
		for(i =0; i<(int)cipherLen; i++) DATA[i] = cipherData[i];
		for(i =0; i<4;i++) DATA[cipherLen +i ]= MAC[i];
		
		Lc = (char)(cipherLen + 4);
		
		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];

		return ret[0];
   }
	public long  FWCosCreateDirectory(long lDevHandle,
			 short usDirSize,
			 short usDirFileID,
			 long ulCreateSec,
			 long ulDeleteSec)
	{
		long ret[] = new long[2];
		int ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		//80E0000107 280010 F0F0 FF80
		char CLA = 0x80;
		char INS = 0xE0;
		char P1  = (char)((usDirFileID >> 8) & (0x00FF)); //文件标识
		char P2  = (char)(usDirFileID & (0x00FF));

		//////////////////////////////////////////数据域
		char DATA[] = new char[1024];

		int  lenData = 7;
		char Lc;

		DATA[0] = 0x38; //建立二进制文件，明文形式
		DATA[1] = (char)((usDirSize>>8) & (0x00FF)); //文件大小
		DATA[2] = (char)((usDirSize) & (0x00FF));

		if(true == fMaskSec)
			DATA[3] = (char)(0xF0 | ulCreateSec); //Section of Create
		else
			DATA[3] = (char)(ulCreateSec); //Section of Create

		if(FWCOS_SEC_NEVER == ulCreateSec)
			DATA[3] = (char)(FWCOS_SEC_NEVER & 0xFF);

		if(true == fMaskSec)
			DATA[4] = (char)(0xF0 | ulDeleteSec); //写权限
		else
			DATA[4] = (char)(ulDeleteSec); //写权限

		if(FWCOS_SEC_NEVER == ulDeleteSec)
			DATA[4] = (char)(FWCOS_SEC_NEVER & 0xFF);

		DATA[5] = 0x01;
		DATA[6] = 0xFF;
		DATA[7] = 0xFF;
		DATA[8] =  0xA0;//应用文件名称
		DATA[9]=  0x00;
		DATA[10]=  0x00;
		DATA[11]=  0x00;
		DATA[12]=  0x03;
		DATA[13]=  0x86;
		DATA[14]=  0x98;
		DATA[15]=  0x07;
		DATA[16]=  0x01;

		///////////////////////////////////////////////////////////
		Lc  = (char)(lenData);//文件信息的长度,注：仅当长度不超过255的时候才可以

		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;
		
		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	public long  FWCosActiveDirectory(long lDevHandle,
			 short usDirFileID)
	{
		long ret[] = new long[2];
		int ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		//80E0000107 280010 F0F0 FF80
		char CLA = 0x80;
		char INS = 0xE0;
		char P1  = 0x01; //文件标识
		char P2  = 0x01; //Create done

		//////////////////////////////////////////数据域
		char DATA[] = new char[1024];

		int  lenData = 7;
		char Lc;
		
		if(usDirFileID != 0){
			DATA[0] = (char)((usDirFileID >> 8) & (0x00FF)); //目录文件标识
			DATA[1] = (char) (usDirFileID & (0x00FF));
		}
		else
		{
			DATA[0] = 0x3f;
			DATA[1] = 0x00;
		}

		///////////////////////////////////////////////////////////
		Lc  = (char)(lenData);//文件信息的长度,注：仅当长度不超过255的时候才可以

		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;
		
		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	
	public long  FWCosCreateBinaryFile(long lDevHandle,
			short usDirFileID,
		    short usFileID,
		    short usFileLen,
		    char cryptyType,
		    long ulReadSec,
		    long ulUpdateSec,
		    long ulDeleteSec)
	{
		long ret[] = new long[2];
		int ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		//80E0000107 280010 F0F0 FF80
		char CLA = 0x80;
		char INS = 0xE0;
		char P1  = (char)((usFileID >> 8) & (0x00FF)); //文件标识
		char P2  = (char)(usFileID & (0x00FF));

		//////////////////////////////////////////数据域
		char DATA[] = new char[1024];

		int  lenData = 7;
		char Lc;

		if(cryptyType > 2)
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误


		if(usDirFileID != 0){//先选择到特定的目录下
			ret[0] = FWCosSelectDirectory(lDevHandle,usDirFileID);
			if(ret[0] != 0) return ret[0];
		}

		DATA[0] = 0x28; //建立二进制文件，明文形式
		DATA[1] = (char)((usFileLen>>8) & (0x00FF)); //文件大小
		DATA[2] = (char)((usFileLen) & (0x00FF));

		if(true == fMaskSec)
			DATA[3] = (char)(0xF0 | ulReadSec); //读权限
		else
			DATA[3] = (char)(ulReadSec); //读权限

		if(FWCOS_SEC_NEVER == ulReadSec)
			DATA[3] = (char)(FWCOS_SEC_NEVER & 0xFF);

		if(true == fMaskSec)
			DATA[4] = (char)(0xF0 | ulUpdateSec); //写权限
		else
			DATA[4] = (char)(ulUpdateSec); //写权限

		if(FWCOS_SEC_NEVER == ulUpdateSec)
			DATA[4] = (char)(FWCOS_SEC_NEVER & 0xFF);

		DATA[5] = 0xFF;
		DATA[6] = 0x80;

		if(1 == cryptyType){
			DATA[0] = 0xA8;//0x68;//0xA8;明文+MAC     0x68: 密文形式
			DATA[6] = 0xFF; // 0 111 11 11
		}else if(2 == cryptyType){
			DATA[0] = 0xE8;//0x68;//0xA8;明文+MAC     0x68: 密文形式
			DATA[6] = 0xFF; // 0 111 11 11
		}

		///////////////////////////////////////////////////////////
		Lc  = (char)(lenData);//文件信息的长度,注：仅当长度不超过255的时候才可以

		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;
		
		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	public long  FWCosCreateRecordFile(
			 long			lDevHandle,
			 short	usDirFileID,
			 char  ucFRecord,
			 short	usFileID,
			 char	bRecordNum,
			 short	bRecordLen,
			 char           cryptyType,
			 long	ulReadSec,
			 long	ulUpdateSec,
			 long	ulDeleteSec)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[]= new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		//80E0000107 280010 F0F0 FF80
		char CLA = 0x80;
		char INS = 0xE0;
		char P1  = (char)((usFileID >> 8) & (0x00FF)); //文件标识
		char P2  = (char)(usFileID & (0x00FF));

		//////////////////////////////////////////数据域
		char DATA[] = new char[512];

		int  lenData = 7;
		char Lc;


		if(cryptyType >2 )
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误


		if(usDirFileID != 0){//先选择到特定的目录下
			ret[0] = FWCosSelectDirectory(lDevHandle,usDirFileID);
			if(ret[0] != 0) return ret[0];
		}

		switch(ucFRecord)
		{
		case 0://
			DATA[0] = 0x2A; //建立定长记录文件，明文形式
			break;
		case 1:
			DATA[0] = 0x2E; //建立循环记录文件，明文形式
			break;
		case 2:
			DATA[0] = 0x2C; //建立变长记录文件，明文形式
			break;
		}


		DATA[1] = bRecordNum; //文件大小
		DATA[2] = (char)((bRecordLen) & (0x00FF));

		DATA[3] = (char)(0xF0 | ulReadSec); //读权限
		if( FWCOS_SEC_NEVER == ulReadSec)
			DATA[3] = (char)(FWCOS_SEC_NEVER & 0xff);

		DATA[4] = (char)(0xF0 | ulUpdateSec); //写权限
		if(FWCOS_SEC_NEVER == ulUpdateSec)
			DATA[4] = (char)(FWCOS_SEC_NEVER & 0xff);

		DATA[5] = 0xFF;
		DATA[6] = 0xFF;//0x80;

		if(2 == cryptyType){
			switch(ucFRecord){
			case 0:
				DATA[0] = 0xEA;//密文+MAC 定长记录
				break;
			case 1:
				DATA[0] = 0xEE;//密文+MAC 循环记录
				break;
			case 2:
				DATA[0] = 0xEC;//密文+MAC 变长记录
				break;
			}
			DATA[6] = 0xFF; // 1 111 11 11
		}

		if(1 == cryptyType){
			switch(ucFRecord){
			case 0:
				DATA[0] = 0xAA;//明文+MAC 定长记录
				break;
			case 1:
				DATA[0] = 0xAE;//明文+MAC 循环记录
				break;
			case 2:
				DATA[0] = 0xAC;//明文+MAC 变长记录
				break;
			}
			DATA[6] = 0xFF; // 1 111 11 11
		}


		///////////////////////////////////////////////////////////

		Lc  = (char)(lenData);//文件信息的长度,注：仅当长度不超过255的时候才可以


		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	public long  FWCosSelecteApp(long lDevHandle)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];
		char CLA = 0x00;
		char INS = 0xA4;
		char P1  ;//= 0x04;//0x00-按文件标识符选择MF,DF
		//0x02- 选择EF
		//0x04-按文件名选择应用
		char P2  ;//= 0x02;//0x00-第一个或仅有一个
		//0x02下一个
		//////////////////////////////////////////数据域
		char DATA[] = new char[1024];

		///////////////////////////////////////////////////////////
		char Lc  ;//= (char)lenData;//文件信息的长度,注：仅当长度不超过255的时候才可以

		//usFileID = 0 && usDirFileID != 0:	
		P1 = 0x00;//0x00-按文件标识符选择MF,DF
		P2 = 0x00;//0x00-第一个或仅有一个
		Lc = 0x02;
		DATA[0] = 0x3F;
		DATA[1] = 0x00;

		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域
		
		cmdbuf[5 +i] = 0x00;//le

		lenCmd = Lc + 6;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	public long  FWCosSelectDirectory(long lDevHandle,
			   short usDirID)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];
		char CLA = 0x00;
		char INS = 0xA4;
		char P1  ;//= 0x04;//0x00-按文件标识符选择MF,DF
		//0x02- 选择EF
		//0x04-按文件名选择应用
		char P2  ;//= 0x02;//0x00-第一个或仅有一个
		//0x02下一个
		//////////////////////////////////////////数据域
		char DATA[] = new char[1024];

		///////////////////////////////////////////////////////////
		char Lc  ;//= (char)lenData;//文件信息的长度,注：仅当长度不超过255的时候才可以

		//usFileID = 0 && usDirFileID != 0:	
		P1 = 0x00;//0x00-按文件标识符选择MF,DF
		P2 = 0x00;//0x00-第一个或仅有一个
		Lc = 0x02;
		DATA[0] = (char)((usDirID >> 8)&(0x00FF));
		DATA[1] = (char)(usDirID & (0x00FF));

		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	
	public long  FWCosSelectFile(
         long            lDevHandle,
         short  usFileID)
	{
		long ret[] = new long[2];
		long  ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[]= new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		char CLA = 0x00;
		char INS = 0xA4;
		char P1  ;//= 0x04;//0x00-按文件标识符选择MF,DF
		//0x02- 选择EF
		//0x04-按文件名选择应用
		char P2  ;//= 0x02;//0x00-第一个或仅有一个
		//0x02下一个

		//////////////////////////////////////////数据域
		char DATA[] = new char[256];

		///////////////////////////////////////////////////////////

		char Lc  ;//= (char)lenData;//文件信息的长度,注：仅当长度不超过255的时候才可以

		P1 = 0x00;//0x00- 按文件标识符选择
		P2 = 0x00;//0x00-第一个或仅有一个
		Lc = 0x02;
		DATA[0] =(char)( (usFileID >> 8)&(0x00FF));
		DATA[1] =(char)( usFileID & (0x00FF));


		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	
	public long  FM1208_exVerify(long			lTokenHandle,
			char   bKeyID,
			char[]     keyValue,
			long   keyLen)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[]= new char[256] ;
		char gndbuf[] = new char[9];
		int i;
		int lenCmd;
		char reLen[] = new char[2];

		char CLA = 0x00;
		char INS = 0x82;
		char P1  = 0x00;//
		char P2  = bKeyID;//密钥标识符

		//////////////////////////////////////////数据域
		char DATA[] = new char[512];
		int  lenData;
		char Lc;

		if(bKeyID == 0x00)
		{//如果是验证主控密钥

			char initVal[]={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

			lenData = 4;
			ret[0] = FWTokenGetRnd(lTokenHandle,lenData,gndbuf);
			if(ret[0] != 0) return ret[0];


			//对4字节随机数后面补4个0x00
			for(i=0; i< 4; i++)
				gndbuf[4+i] = 0x00;

			if(8 == keyLen){
				//对8字节数据进行DES加密，密文存入DATA中
				ftoken = pl_javacall.ic_encrypt_ECB(keyValue,gndbuf,(short)8,DATA,initVal);
				if(ftoken != 0) return ftoken;
			}else{
				//对8字节数据进行3DES加密，密文存入DATA中
				ftoken = pl_javacall.ic_encrypt_3DES_ECB(keyValue,gndbuf,(short)8,DATA);
				if(ftoken != 0) return ftoken;
			}

			lenData = 8;//后面要用

		}else{

			if(keyLen != 16)
				return Lib.Error.ErrorCode.ERR_PARA;//参数错误

			lenData = 8;//取8字节的随机数
			ret[0] = FWTokenGetRnd(lTokenHandle,lenData,gndbuf);
			if(ret[0] != 0) return ret[0];

			//对8字节随机数进行3DES加密，密文存入DATA中
			ftoken = pl_javacall.ic_encrypt_3DES_ECB(keyValue,gndbuf,(short)8,DATA);
			if(ftoken != 0) return ftoken;
		}

		///////////////////////////////////////////////////////////


		Lc  = (char)(lenData);//文件信息的长度,注：仅当长度不超过255的时候才可以

		cmdbuf[0] = CLA;//CLA    *	00
		cmdbuf[1] = INS;//INS    * 命令头 *	82
		cmdbuf[2] = P1;//P1     *	00
		cmdbuf[3] = P2;//P2     *	00

		cmdbuf[4] = Lc; //Lc    文件信息的长度 08
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;

		ftoken = CPUAPDU_FUNC(lTokenHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	
	public long FWCosVerifyKey(long lDevHandle,
		     char[] pbKey,
		     long ulKeyLen,
		     long ulKeyType)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[]= new char[256];
		int i;
		int lenCmd;
		char reLen[] = new char[2];
		char CLA = 0x00;
		char INS = 0x20;
		char P1  = 0x00;//
		char P2  = 0x00;//密钥标识符 这个版本的都设置为0x00
		//////////////////////////////////////////数据域
		char DATA[] = new char[512];
		char Lc;

		switch((int)ulKeyType){
		case FW_KEYTYPE_FILEPROTECT:
			break;
		case FW_KEYTYPE_EXVERIFY:
		{
			P2  = 0x00;

			ret[0] = FM1208_exVerify(lDevHandle,(char)0x00,pbKey,ulKeyLen);

			return ret[0];
		}

		case FW_KEYTYPE_PIN:
		{
			if(ulKeyLen<2 || ulKeyLen>8)
				return Lib.Error.ErrorCode.ERR_PARA;
		}
		break;
		case FW_KEYTYPE_UNLOCK:
			break;
		default:
			return Lib.Error.ErrorCode.ERR_PARA;
		}

		for(i=0; i<ulKeyLen; i++)DATA[i] = pbKey[i];

		///////////////////////////////////////////////////////////

		Lc  = (char)(ulKeyLen);//文件信息的长度,注：仅当长度不超过255的时候才可以


		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *

		cmdbuf[4] = Lc; //Lc    文件信息的长度
		for(i = 0; i< Lc ; i++)
			cmdbuf[5 +i] = DATA[i];//数据域

		lenCmd = Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		return ret[0];
	}
	
	public long FWCosReadBinaryFile(long lDevHandle,
			  short usFileID,
			  char[] pbBinData,
			  short usOffset,
			  short usReadLen,
			  char cryptyType,
			  char[] pProKey,
			  long ulKeyLen)
	{
		long ret[] = new long[2];
		long ftoken;
		char cmdbuf[] = new char[256];
		char retbuf[] = new char[256];
		int lenCmd;
		char reLen[] = new char[2];
		char CLA = 0x00;//0x00;
		char INS = 0xB0;
		char P1	 = (char)((usOffset >>8) & (0x007F));//+2017/05/27 for long size read
		char P2  = (char)(usOffset & 0x00FF);//
		char Lc  = 0x00;//MAC码长度
		char Le;
		short i;

		if( cryptyType > 2)
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误

		if(1 == cryptyType){
			CLA = 0x00;
			Lc  = (char)(usReadLen & 0x00FF);//0x04;
		}

		if(2 == cryptyType){
			CLA = 0x00;//0x04;
			Lc  = 0x0C;//12个字节（8字节密文 + 4字节MAC)
		}


		//////////////////////////////////////////数据域
		//char DATA[1024];

		///////////////////////////////////////////////////////////

		if(usReadLen > MAX_SIZE_FILE)
			return Lib.Error.ErrorCode.ERR_PARA;//参数错误

		Le = (char)(usReadLen);
		cmdbuf[0] = CLA;//CLA    *
		cmdbuf[1] = INS;//INS    * 命令头 *
		cmdbuf[2] = P1;//P1     *
		cmdbuf[3] = P2;//P2     *
		cmdbuf[4] = Lc;//Le; //

		if( 0 == cryptyType)
			cmdbuf[4] = Le;

		lenCmd =  5;

		if(cryptyType != 0){


			int cCiphertype;
			int plainLen =0;
			char plainData[] = new char[256];
			char cipherData[] = new char[1024];
			char maintainKey[] = new char[17];
			int cipherLen = 0;
			char rndBuf[] = new char[5];
			char InitDATA[]= new char[16];
			char MACCipher[] = new char[256];
			char MAC[] = new char[5];
			char DATA[] = new char[256];

			for(i=0;i<ulKeyLen; i++)maintainKey[i] = pProKey[i];
			//设置加密方式
			if(ulKeyLen == 8)
				cCiphertype = 1;
			else cCiphertype =3;

			if(1 == cryptyType){
				cipherLen = 0;
			}

			if(2 == cryptyType){

				/*
				 * 用线路保护密钥生成密文
				 */

				plainData[0] = (char)usReadLen;

				if(1 == cCiphertype){
					//DES加密，生成密文
					ret[0] = pl_javacall.ic_encrypt_ECB(maintainKey,plainData,(short)1,cipherData,InitDATA);
					if(ret[0] != 0) return ret[0];
				}else{
					//3DES加密，生成密文
					ret[0] = pl_javacall.ic_encrypt_3DES_ECB(maintainKey,plainData,(short)1,cipherData);
					if(ret[0] != 0) return ret[0];
				}
				cipherLen = pl_javacall.GetCipherReLen(1);
			}


			/*
			 * 生成MAC
			 */


			if(8 == ulKeyLen)
				cCiphertype = 1;
			else
				cCiphertype = 3;


			//从卡片取4字节随机数,并设置初始值
			FWTokenGetRnd(lDevHandle,4,rndBuf);

			for(i=0;i<4;i++){InitDATA[i] = rndBuf[i];InitDATA[4+i]=0x00;}

			//计算MAC
			DATA[0] = CLA;
			DATA[1] = INS;
			DATA[2] = P1;
			DATA[3] = P2;


			if( 1 == cryptyType)
				DATA[4] = (char)usReadLen;
			if( 2 == cryptyType)
				DATA[4] = (char)(cipherLen + 4);


			for(i=0;i<(int)cipherLen;i++)DATA[5 + i] = cipherData[i];

			if(1== cCiphertype){
				ret[0] =pl_javacall.ic_encrypt_ECB(maintainKey,DATA,(short)(cipherLen + 4 +1) ,MACCipher,InitDATA);
				if(ret[0] !=0 ) return ret[0];

				for(i=0;i<4;i++)MAC[i] = MACCipher[pl_javacall.GetCipherReLen(cipherLen + 4 +1)-8 +i];
			}else{
				//3DES加密，生成密文
				ret[0] =pl_javacall.ic_encrypt_3DES_ECB_MAC(maintainKey,DATA,( short)(cipherLen + 4 +1),MACCipher,InitDATA);
				if(ret[0] != 0) return ret[0];

				for(i=0;i<4;i++)MAC[i] = MACCipher[i];
			}

			Lc = (char) (cipherLen + 4);

			if(1 == cryptyType)
				Lc = (char)usReadLen;

			if(2 == cryptyType)
				Lc = (char)usReadLen;

			/*
			 * 填充数据域
			 */

			for(i =0; i<(int)cipherLen; i++) DATA[i] = cipherData[i];
			for(i =0; i<4;i++) DATA[cipherLen +i ]= MAC[i];

			cmdbuf[0] = CLA;//CLA    *
			cmdbuf[1] = INS;//INS    * 命令头 *
			cmdbuf[2] = P1;//P1     *
			cmdbuf[3] = P2;//P2     *
			cmdbuf[4] = Lc; //
			for(i = 0; i< Lc; i++) cmdbuf[5 + i] = DATA[i];


			lenCmd =  Lc + 5;
		}


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];


		//返回读到的数据
		for(i=0; i< usReadLen; i++)pbBinData[i] = retbuf[i];

		return ret[0];
	}
    public long FWCosUpdateBinaryFile(long lDevHandle,
		    short  usFileID,
		    short usOffset,
		    char[] pbBinData,
		    short usUpdateLen,
		    char cryptyType,
		    char[] pProKey,
		    long ulKeyLen)
    {
    	long ret[] = new long[2];
    	long ftoken;
    	char cmdbuf[] = new char[256];
    	char retbuf[]= new char[256];
    	int i;
    	int lenCmd;
    	char reLen[] = new char[2];

    	char CLA = 0x00;//0x04;//密文+MAC方式

    	char INS = 0xD6;
    	char P1	 = (char)((usOffset >>8) & (0x00FF));//
    	char P2  = (char)(usOffset & 0x00FF);//

    	//////////////////////////////////////////数据域
    	char DATA[] = new char[1024];
    	int cCiphertype;
    	char cipherData[] = new char[4096];
    	char plainData[]= new char[256];
    	int plainLen =0;

    	char maintainKey[] = new char[17];//={0x00,0x00,0x00,0x00,0x01,0x02,0x03,0x04,0x04,0x03,0x02,0x01,0x00,0x00,0x00,0x00,0x00};
    	int cipherLen = 0;
    	char rndBuf[] = new char[5];
    	char InitDATA[]=new char[16];
    	char MACCipher[]= new char[256];
    	char MAC[] = new char[5];

    	char Lc;

    	if(cryptyType != 0){
    		//加密文件的情况
    		CLA = 0x04;
    	}

    	if(usUpdateLen > MAX_SIZE_FILE)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	if( cryptyType > 2)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	if(cryptyType != 0){
    		for(i=0; i< ulKeyLen;i++)maintainKey[i] = pProKey[i];

    		//设置加密方式
    		if(ulKeyLen == 8)
    			cCiphertype = 1;
    		else cCiphertype =3;


    		if(1 == cryptyType){
    			for(i=0; i< usUpdateLen;i++)cipherData[i] = pbBinData[i];
    			cipherLen = usUpdateLen;
    		}

    		if(2 == cryptyType){

    			/*
    			 * 用线路保护密钥生成密文
    			 */

    			plainData[0] = (char)usUpdateLen;
    			for(i=0; i< usUpdateLen;i++)plainData[1 +i] = pbBinData[i];

    			if(1 == cCiphertype){
    				//DES加密，生成密文
    				ret[0] =(long)pl_javacall.ic_encrypt_ECB(maintainKey,plainData,(short)(usUpdateLen +1),cipherData,InitDATA);
    				if(ret[0] != 0) return ret[0];
    			}else{
    				//3DES加密，生成密文
    				ret[0] =pl_javacall.ic_encrypt_3DES_ECB(maintainKey,plainData,(short)(usUpdateLen +1),cipherData);
    				if(ret[0] != 0) return ret[0];
    			}
    			cipherLen = pl_javacall.GetCipherReLen(usUpdateLen + 1);
    		}

    		/*
    		 * 用密文生成MAC
    		 */


    		//从卡片取4字节随机数,并设置初始值
    		FWTokenGetRnd(lDevHandle,4,rndBuf);

    		for(i=0;i<4;i++){InitDATA[i] = rndBuf[i];InitDATA[4+i]=0x00;}

    		//计算MAC
    		DATA[0] = CLA;
    		DATA[1] = INS;
    		DATA[2] = P1;
    		DATA[3] = P2;
    		DATA[4] = (char)(cipherLen + 4);

    		for(i=0;i<(int)cipherLen;i++)DATA[5 + i] = cipherData[i];

    		if(1== cCiphertype){
    			ret[0] =pl_javacall.ic_encrypt_ECB(maintainKey,DATA,(short)(cipherLen + 4 +1) ,MACCipher,InitDATA);
    			if(ret[0] != 0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[pl_javacall.GetCipherReLen(cipherLen + 4 +1)-8 +i];
    		}else{
    			//3DES加密，生成密文
    			ret[0] =pl_javacall.ic_encrypt_3DES_ECB_MAC(maintainKey,DATA,(short)(cipherLen + 4 +1),MACCipher,InitDATA);
    			if(ret[0] != 0) return ret[0];
    			for(i=0;i<4;i++)MAC[i] = MACCipher[i];
    		}

    		/*
    		 * 填充数据域
    		 */

    		for(i =0; i<(int)cipherLen; i++) DATA[i] = cipherData[i];
    		for(i =0; i<4;i++) DATA[cipherLen +i ]= MAC[i];

    		///////////////////////////////////////////////////////////
    		Lc = (char) (cipherLen + 4);//= (char)lenData;//文件信息的长度,注：仅当长度不超过110的时候才可以
    	}//if(cryptyType)
    	else{
    		//明文类型
    		for(i=0; i< usUpdateLen;i++)DATA[i] = pbBinData[i];
    		Lc = (char)usUpdateLen;
    	}
    	cmdbuf[0] = CLA;//CLA    *
    	cmdbuf[1] = INS;//INS    * 命令头 *
    	cmdbuf[2] = P1;//P1     *
    	cmdbuf[3] = P2;//P2     *
    	cmdbuf[4] = Lc; //
    	for(i = 0; i< Lc; i++) cmdbuf[5 + i] = DATA[i];

    	lenCmd =  Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];
		
		return ret[0];
    }
	
    public long  FWCosReadRecord(
			 long			lDevHandle,
			 char  ucFRecord,
			 char  bRecordFileID,
			 char	bRecordNum,
			 char[]			pbRecData,
			 short	bRecordLen)
    {
    	long ret[] = new long[2];
    	long ftoken;
    	char cmdbuf[] = new char[256];
    	char retbuf[] = new char[256];
    	char			cryptyType = 0;
    	char			pProKey[] = new char[17];
    	long	ulKeyLen =0;
    	int lenCmd;
    	char reLen[] = new char[2];
    	int plainLen =0;
    	char InitDATA[]={0};
    	char CLA = 0x00;//0x00;
    	char INS = 0xB2;
    	char P1	 = bRecordNum;
    	char P2  = (char)(((bRecordFileID & 0x1F) << 3) | 0x04);// 00000 100 当前文件，使用P1中的记录
    	char Lc  = 0x04;//MAC码长度
    	char Le;
    	int cCiphertype;
    	char maintainKey[] = new char[17];
    	int cipherLen;
    	char rndBuf[] = new char[5];
    	char MACCipher[] = new char[256];
    	char MAC[] = new char[5];
    	char DATA[] = new char[256];
    	int i;


    	if( (ucFRecord != 0)&& (ucFRecord != 1) && (ucFRecord != 2))
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	if(cryptyType != 0){
    		CLA = 0x04;
    	}
    	//////////////////////////////////////////数据域
    	//char DATA[1024];

    	///////////////////////////////////////////////////////////

    	if(bRecordLen > MAX_SIZE_FILE)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	Le = (char)(bRecordLen);


    	if( 2 == ucFRecord)//变长记录文件的情况，多读2个字节
    		Le  =(char)( 2 + (char)(bRecordLen));


    	cmdbuf[0] = CLA;//CLA    *
    	cmdbuf[1] = INS;//INS    * 命令头 *
    	cmdbuf[2] = P1;//P1     *
    	cmdbuf[3] = P2;//P2     *

    	cmdbuf[4] = Le; //
    	lenCmd =  5;

    	if(cryptyType != 0){
    		cmdbuf[4] = Lc;

    		/*
    		 * 生成MAC
    		 */
    		if(8 == ulKeyLen)
    			cCiphertype = 1;
    		else
    			cCiphertype = 3;


    		//从卡片取4字节随机数,并设置初始值
    		FWTokenGetRnd(lDevHandle,4,rndBuf);

    		for(i=0;i<4;i++){InitDATA[i] = rndBuf[i];InitDATA[4+i]=0x00;}

    		//计算MAC
    		DATA[0] = CLA;
    		DATA[1] = INS;
    		DATA[2] = P1;
    		DATA[3] = P2;

    		cipherLen = 0;

    		DATA[4] = (char)(cipherLen + 4);


    		for(i=0; i< ulKeyLen; i++)maintainKey[i] = pProKey[i];

    		if(1== cCiphertype){
    			ret[0] = pl_javacall.ic_encrypt_ECB(maintainKey,DATA,( short)(cipherLen + 4 +1) ,MACCipher,InitDATA);
    			if(ret[0] !=0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[pl_javacall.GetCipherReLen(cipherLen + 4 +1)-8 +i];
    		}
    		else
    		{
    			//3DES加密，生成密文
    			ret[0] =pl_javacall.ic_encrypt_3DES_ECB_MAC(maintainKey,DATA,(short)(cipherLen + 4 +1),MACCipher,InitDATA);
    			if(ret[0] != 0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[i];
    		}


    		/*
    		 * 填充数据域
    		 */

    		for(i =0; i<4;i++) cmdbuf[5 +i ]= MAC[i];

    		lenCmd = Lc + 5;
    	}


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];


		//返回读到的数据
		if( 2 == ucFRecord)
			for(i=0; i< bRecordLen; i++)pbRecData[i] = retbuf[2 + i];
		else
			for(i=0; i<bRecordLen; i++ )pbRecData[i] = retbuf[i];

		return ret[0];
    }
    
    public long  FWCosUpdateRecord(
			 long			lDevHandle,
			 char  ucRecord,
			 char  bRecordFileID,
			 char	bRecordNum,
			 char[]		pbRecData,
			 short	bUpdateLen,
			 char            cryptyType,
			 char[]           pProKey,
			 long   ulKeyLen)
    {
    	long ret[] = new long[2];
    	long ftoken;
    	char cmdbuf[] = new char[256];
    	char retbuf[]= new char[256];
    	int i;
    	int lenCmd;
    	char reLen[] = new char[2];
    	char CLA = 0x00;//0x04;//密文+MAC方式
    	char INS = 0xDC;
    	char P1	 = bRecordNum;// 1,2,3...
    	char P2  = (char)(((bRecordFileID & 0x1F) << 3) | 0x04);// 00000 100 当前文件，使用P1中的记录
    	//////////////////////////////////////////数据域
    	char DATA[] = new char[512];
    	int cCiphertype;
    	char plainData[] = new char[256];
    	int plainLen =0;
    	char cipherData[] = new char[512];
    	char maintainKey[] = new char[17];//={0x00,0x00,0x00,0x00,0x01,0x02,0x03,0x04,0x04,0x03,0x02,0x01,0x00,0x00,0x00,0x00,0x00};
    	int cipherLen = 0;
    	char rndBuf[] = new char[5];
    	char InitDATA[]={0};
    	char MACCipher[] = new char[256];
    	char MAC[] = new char[5];
    	char Lc = 0;
    
    	if((ucRecord != 0) && (ucRecord != 1))
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	if(cryptyType >2)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	if(cryptyType != 0){
    		//加密文件的情况
    		CLA = 0x04;
    	}

    	if(bUpdateLen > MAX_SIZE_FILE)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	for(i=0; i< bUpdateLen;i++)DATA[i] = pbRecData[i];


    	//这个版本的COS，在写之前，除了刚刚建好文件之后，
    	//好像只有在读过之后，才可以写入
    	//所以这里在写之前，先读一下
    	if(cryptyType != 0){
    		for(i=0; i< ulKeyLen; i++)maintainKey[i] = pProKey[i];

    		//设置加密方式
    		if(ulKeyLen == 8)
    			cCiphertype = 1;
    		else cCiphertype =3;

    		if(1 == cryptyType){
    			if(0 == ucRecord){
    				//定长记录文件
    				for(i=0;i<bUpdateLen;i++)cipherData[i] = pbRecData[i];
    				cipherLen = bUpdateLen ;
    			}else if(1 == ucRecord){
    				//变长记录文件
    				cipherData[0] = 0xAA;//Tag这里定死看行不行
    				cipherData[1] = (char)bUpdateLen;
    				for(i=0; i< bUpdateLen; i++)cipherData[2 +i] = pbRecData[i];
    				cipherLen = bUpdateLen + 2;
    			}

    		}

    		if(2 == cryptyType){

    			/*
    			 * 用线路保护密钥生成密文
    			 */

    			if(0 == ucRecord){
    				//定长记录文件
    				plainData[0] =(char) bUpdateLen;
    				for(i=0; i< bUpdateLen;i++)plainData[1 + i] = pbRecData[i];

    				if(1 == cCiphertype){
    					//DES加密，生成密文
    					ret[0] = pl_javacall.ic_encrypt_ECB(maintainKey,plainData,(short)(bUpdateLen +1),cipherData,InitDATA);
    					if(ret[0] != 0) return ret[0];
    				}else{
    					//3DES加密，生成密文
    					ret[0] = pl_javacall.ic_encrypt_3DES_ECB(maintainKey,plainData,(short)(bUpdateLen +1),cipherData);
    					if(ret[0] != 0) return ret[0];
    				}
    				cipherLen = pl_javacall.GetCipherReLen(bUpdateLen + 1);

    			}else if(1 == ucRecord){
    				//变长记录文件
    				plainData[0] = (char)( bUpdateLen +2);
    				plainData[1] = 0xAA;//Tag这里定死看行不行
    				plainData[2] = (char)bUpdateLen;
    				for(i=0; i< bUpdateLen; i++)plainData[3 + i] = pbRecData[i];

    				if(1 == cCiphertype){
    					//DES加密，生成密文
    					ret[0] =pl_javacall.ic_encrypt_ECB(maintainKey,plainData,( short)(bUpdateLen +3),cipherData,InitDATA);
    					if(ret[0] != 0) return ret[0];
    				}else{
    					//3DES加密，生成密文
    					ret[0] = pl_javacall.ic_encrypt_3DES_ECB(maintainKey,plainData,(short)(bUpdateLen +3),cipherData);
    					if(ret[0] != 0) return ret[0];
    				}
    				cipherLen = pl_javacall.GetCipherReLen(bUpdateLen + 3);
    			}
    		}

    		/*
    		 * 用密文生成MAC
    		 */


    		//从卡片取4字节随机数,并设置初始值
    		FWTokenGetRnd(lDevHandle,4,rndBuf);

    		for(i=0;i<4;i++){InitDATA[i] = rndBuf[i];InitDATA[4+i]=0x00;}

    		//计算MAC
    		DATA[0] = CLA;
    		DATA[1] = INS;
    		DATA[2] = P1;
    		DATA[3] = P2;
    		DATA[4] = (char)(cipherLen + 4);

    		for(i=0;i<(int)cipherLen;i++)DATA[5 + i] = cipherData[i];

    		if(1== cCiphertype){
    			ret[0] =pl_javacall.ic_encrypt_ECB(maintainKey,DATA,( short)(cipherLen + 4 +1) ,MACCipher,InitDATA);
    			if(ret[0] != 0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[pl_javacall.GetCipherReLen(cipherLen + 4 +1)-8 +i];
    		}else{
    			//3DES加密，生成密文
    			ret[0] =pl_javacall.ic_encrypt_3DES_ECB_MAC(maintainKey,DATA,( short)(cipherLen + 4 +1),MACCipher,InitDATA);
    			if(ret[0] !=0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[i];
    		}

    		/*
    		 * 填充数据域
    		 */

    		for(i =0; i<(int)cipherLen; i++) DATA[i] = cipherData[i];
    		for(i =0; i<4;i++) DATA[cipherLen +i ]= MAC[i];


    		///////////////////////////////////////////////////////////
    		Lc = (char) (cipherLen + 4);//= (char)lenData;//文件信息的长度,注：仅当长度不超过110的时候才可以


    	}//if(cryptyType)
    	else{
    		//明文类型

    		if(0 == ucRecord)
    			Lc = (char)bUpdateLen;
    		else if( 1 == ucRecord){
    			Lc = (char)(bUpdateLen + 2);

    			plainData[0] = 0xBB;//0xAA;//Tag
    			plainData[1] = (char)bUpdateLen;
    			for(i= 0; i<bUpdateLen; i++)
    				plainData[2+i] = DATA[i];

    			for(i=0; i<Lc; i++)
    				DATA[i] = plainData[i];
    		}
    	}
    	cmdbuf[0] = CLA;//CLA    *
    	cmdbuf[1] = INS;//INS    * 命令头 *
    	cmdbuf[2] = P1;//P1     *
    	cmdbuf[3] = P2;//P2     *
    	cmdbuf[4] = Lc; //

    	for(i = 0; i< Lc; i++) cmdbuf[5 + i] = DATA[i];

    	lenCmd =  Lc + 5;

		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];

		return ret[0];
    }
    
    public long  FWCosAppendRecord(
			 long			lDevHandle,
			 char  bRecordFileID,
			 char[]		pbRecData,
			 short	bAppendLen,
			 char            cryptyType,
			 char[]           pProKey,
			 long   ulKeyLen)
    {
    	long ret[] = new long[2];
    	long ftoken;
    	char cmdbuf[] = new char[256];
    	char retbuf[] = new char[256];
    	int i;
    	int lenCmd;
    	char reLen[] = new char[2];
    	char CLA = 0x00;//0x04;//密文+MAC方式
    	char INS = 0xE2;
    	char P1	 = 0x00;// 1,2,3...
    	char P2  = (char)( ((bRecordFileID & 0x1F) << 3) | 0x04);// 00000 100 当前文件
    	//////////////////////////////////////////数据域
    	char DATA[] = new char[512];
    	int cCiphertype;
    	char plainData[]= new char[256];
    	char cipherData[]= new char[256];
    	char maintainKey[] = new char[17];//={0x00,0x00,0x00,0x00,0x01,0x02,0x03,0x04,0x04,0x03,0x02,0x01,0x00,0x00,0x00,0x00,0x00};
    	int cipherLen = 0;
    	char rndBuf[] = new char[5];
    	char InitDATA[]={0};
    	char MACCipher[]= new char[256];
    	char MAC[] = new char[5];
    	char Lc = 0;

    	if(cryptyType != 0){
    		//加密文件的情况
    		CLA = 0x04;
    	}

    	if(bAppendLen > MAX_SIZE_FILE)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	if(cryptyType > 2)
    		return Lib.Error.ErrorCode.ERR_PARA;//参数错误

    	for(i=0; i< bAppendLen; i++)
    		DATA[i] = pbRecData[i];

    	if(cryptyType != 0){
    		for(i= 0; i< ulKeyLen; i++)maintainKey[i] = pProKey[i];

    		//设置加密方式
    		if(ulKeyLen == 8)
    			cCiphertype = 1;
    		else cCiphertype =3;

    		/*
    		 * 用线路保护密钥生成密文
    		 */
    		if( 1 == cryptyType){
    			for(i=0; i< bAppendLen; i++)cipherData[i] = pbRecData[i];
    			cipherLen = bAppendLen ;
    		}

    		if( 2 == cryptyType){

    			plainData[0] = (char)bAppendLen;
    			for(i=0; i<bAppendLen; i++)plainData[1 + i] = pbRecData[i];

    			if(1 == cCiphertype){
    				//DES加密，生成密文
    				ret[0] = pl_javacall.ic_encrypt_ECB(maintainKey,plainData,(short)(bAppendLen +1),cipherData,InitDATA);
    				if(ret[0] != 0) return ret[0];
    			}else{
    				//3DES加密，生成密文
    				ret[0] =pl_javacall.ic_encrypt_3DES_ECB(maintainKey,plainData,(short)(bAppendLen +1),cipherData);
    				if(ret[0] != 0) return ret[0];
    			}
    			cipherLen = pl_javacall.GetCipherReLen(bAppendLen + 1);
    		}

    		/*
    		 * 用密文生成MAC
    		 */


    		//从卡片取4字节随机数,并设置初始值
    		FWTokenGetRnd(lDevHandle,4,rndBuf);

    		for(i=0;i<4;i++){InitDATA[i] = rndBuf[i];InitDATA[4+i]=0x00;}

    		//计算MAC
    		DATA[0] = CLA;
    		DATA[1] = INS;
    		DATA[2] = P1;
    		DATA[3] = P2;

    		DATA[4] = (char)(cipherLen + 4);

    		for(i=0;i<(int)cipherLen;i++)DATA[5 + i] = cipherData[i];

    		if(1== cCiphertype){
    			ret[0] =pl_javacall.ic_encrypt_ECB(maintainKey,DATA,(short)(cipherLen + 4 +1) ,MACCipher,InitDATA);
    			if(ret[0] != 0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[pl_javacall.GetCipherReLen(cipherLen + 4 +1)-8 +i];
    		}else{
    			//3DES加密，生成密文
    			ret[0] =pl_javacall.ic_encrypt_3DES_ECB_MAC(maintainKey,DATA,(short)(cipherLen + 4 +1),MACCipher,InitDATA);
    			if(ret[0] != 0) return ret[0];

    			for(i=0;i<4;i++)MAC[i] = MACCipher[i];
    		}

    		/*
    		 * 填充数据域
    		 */

    		for(i =0; i<(int)cipherLen; i++) DATA[i] = cipherData[i];
    		for(i =0; i<4;i++) DATA[cipherLen +i ]= MAC[i];


    		///////////////////////////////////////////////////////////
    		Lc = (char) (cipherLen + 4);//= (char)lenData;//文件信息的长度,注：仅当长度不超过110的时候才可以
    	}//if(cryptyType)
    	else{
    		//明文类型
    		Lc = (char)bAppendLen;
    	}

    	cmdbuf[0] = CLA;//CLA    *
    	cmdbuf[1] = INS;//INS    * 命令头 *
    	cmdbuf[2] = P1;//P1     *
    	cmdbuf[3] = P2;//P2     *
    	cmdbuf[4] = Lc; //
    	for(i = 0; i< Lc; i++) cmdbuf[5 + i] = DATA[i];
    	lenCmd =  Lc + 5;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];

		return ret[0];
    }

    public long  FWCosDeleteFileSys(
            long           lDevHandle)
    {
    	long ret[] = new long[2];
    	long ftoken;
    	char cmdbuf[] = new char[256];
    	char retbuf[]= new char[256];
    	int i;
    	int lenCmd;
    	char reLen[] = new char[2];


    	char CLA = 0x80;
    	char INS = 0x0E;
    	char P1  = 0x00;//建立MF
    	char P2  = 0x00;//正在建立，01 --建立结束

    	//////////////////////////////////////////数据域
    	char DATA[] = new char[256];

    	int  lenData = 0x00;//0x08;


    	///////////////////////////////////////////////////////////

    	char Lc  = (char)lenData;//文件信息的长度,注：仅当长度不超过255的时候才可以


    	cmdbuf[0] = CLA;//CLA    *
    	cmdbuf[1] = INS;//INS    * 命令头 *
    	cmdbuf[2] = P1;//P1     *
    	cmdbuf[3] = P2;//P2     *

    	cmdbuf[4] = Lc; //Lc    文件信息的长度
    	
    	for(i = 0; i< Lc ; i++)
    		cmdbuf[5 +i] = DATA[i];//数据域

    	lenCmd = Lc + 5;


		ftoken = CPUAPDU_FUNC(lDevHandle,lenCmd,cmdbuf,reLen, retbuf);
		if(ftoken != 0)return ftoken;
		FM1208_GetTokenErrorType(retbuf,reLen[0],ret);
		if(ret[0] != 0)return ret[0];

		return ret[0];
    }
	
	
}