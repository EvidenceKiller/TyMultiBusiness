package Lib.HNCOS;

import Lib.LCReader.C1.function_C1;
import FWPubLib.pl_javacall;
import android.R.string;
import android.util.Log;


public class HNCOS {
	
	public static final int ERR_APDU  					=	0x0000ffff;		//锟斤拷锟筋发锟斤拷失锟杰凤拷锟斤拷锟斤拷
	public static final int ERR_PARA					=	0x0000000f;
	public static final int CMD_OK						=	0x00000000;				//锟斤拷锟斤拷锟斤拷确执锟叫凤拷锟斤拷锟斤拷
	public static final char MF_PERMISSION				=	0x0f;			//MF锟斤拷锟斤拷锟侥硷拷锟斤拷删锟斤拷MF锟斤拷权锟斤拷
	public static final char MF_DELCONDITION			=	0x02;			//MF锟斤拷锟斤拷锟缴撅拷锟組F锟斤拷锏斤拷锟阶刺拷锟缴撅拷锟組F前锟斤拷证锟斤拷锟斤拷员锟斤拷钥锟斤拷
	public static final char DF_PERMISSION				=	0x0f;			//DF锟斤拷锟斤拷锟侥硷拷权锟斤拷
	public static final char KEYEF_ADD_PERMISSION_ALL	=	0x0f;			//锟斤拷钥锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷钥权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟接ｏ拷
	public static final char KEYEF_ADD_PERMISSION_USER	=	0x1f;			//锟斤拷钥锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷钥权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟接ｏ拷
	public static final char KEYEF_ADD_PERMISSION_ADMIN	=	0x2f;			//锟斤拷钥锟侥硷拷锟斤拷锟斤拷锟斤拷锟斤拷钥权锟睫ｏ拷锟斤拷锟斤拷员锟斤拷锟斤拷锟接ｏ拷
	public static final char EF_READ_PERMISSION_ALL		=	0x0f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴讹拷锟斤拷
	public static final char EF_READ_PERMISSION_USER	=	0x1f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴讹拷锟斤拷
	public static final char EF_READ_PERMISSION_ADMIN	=	0x2f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷员锟缴讹拷锟斤拷
	public static final char EF_UPDATE_PERMISSION_ALL	=	0x0f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴革拷锟铰ｏ拷
	public static final char EF_UPDATE_PERMISSION_USER	=	0x1f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷锟矫伙拷锟缴革拷锟铰ｏ拷
	public static final char EF_UPDATE_PERMISSION_ADMIN	=	0x2f;			//锟斤拷锟斤拷EF锟侥硷拷锟斤拷锟斤拷权锟睫ｏ拷锟斤拷锟斤拷员锟缴革拷锟铰ｏ拷
	public static final char KEY_ID						=	0x01;			//锟斤拷钥锟斤拷识锟斤拷
	public static final char KEY_VERSION				=	0x01;			//锟斤拷钥锟芥本锟斤拷
	public static final char KEY_ALGORITHM				=	0x00;			//锟斤拷钥锟姐法锟斤拷识锟斤拷默锟斤拷Triple DES锟斤拷
	public static final char KEY_USEPERMISION			=	0x0f;			//锟斤拷钥使锟斤拷权锟斤拷
	public static final char KEY_SUCCEEDING_STATE_ADMIN	=	0x02;			//锟斤拷钥锟斤拷证锟斤拷锟斤拷状态锟斤拷锟解部锟斤拷证锟斤拷
	public static final char KEY_SUCCEEDING_STATE_PIN	=	0x01;			//锟斤拷钥锟斤拷证锟斤拷锟斤拷状态锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷证锟斤拷
	public static final char KEY_UPDATE_PERMISSION_ADMIN=	0x2f;			//锟斤拷钥锟睫革拷权锟睫ｏ拷锟斤拷锟斤拷员锟斤拷钥锟斤拷
	public static final char KEY_UPDATE_PERMISSION_PIN	=	0x1f;			//锟斤拷钥锟睫革拷权锟睫ｏ拷锟斤拷锟斤拷锟斤拷钥锟斤拷
	public static final int KEY_ERROR_COUNT				=	0xff;	
	
	public static final int DEVTYPE_S8 					= 	0x01;
	public static final int DEVTYPE_S3					=	0x02;
	
	
	boolean IscreatendMF = false;//全锟街憋拷锟斤拷: MF锟角否激伙拷
	char cardstatus = 0;//全锟街憋拷锟斤拷:锟斤拷片状态
	int gl_curDevType = 0;
	private function_C1 call_contactFuncs;
	private pl_javacall publib = new  pl_javacall();

	public HNCOS(int devType, function_C1 obj_contactFuncs)
	{
		gl_curDevType = devType;
		call_contactFuncs  = obj_contactFuncs;
	}
	
	public int CPUAPDU_FUNC(int lDevice, int  cmdBufLen, char[] cmdBuf, int[] revBufLen, char[] revBuf )
	{
		if(gl_curDevType == DEVTYPE_S3)
			return call_contactFuncs.IC_CpuApdu(lDevice, cmdBufLen, cmdBuf, revBufLen,  revBuf);
		else 
			return ERR_PARA;
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//删锟斤拷MF锟斤拷锟斤拷式锟斤拷锟斤拷片锟斤拷(锟斤拷MF锟窖硷拷锟斤拷锟斤拷锟斤拷证锟斤拷锟斤拷员锟斤拷钥)
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosDeleteMF(int lDevint)
	{
		int status = -1;
		char sbuff[] = new char[] {0x80,0xe0,0x00,0x03,0x02,0x3f,0x00};
		
		//int[] rlen = new int[1];
		int[]			rlen = new int[1];
		char rbuff[] = new char[128];
		int st;
		
		if ((cardstatus & 0xf0) == 0x60 || IscreatendMF)  //MF锟窖硷拷锟斤拷锟斤拷锟斤拷锟街わ拷锟斤拷锟皆憋拷锟斤拷耄拷锟饺拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷删锟斤拷MF
		{
			char senddata[] = new char[] {0x80,0xca,0x00,0x03,0x01};
			int[] revlen = new int[1];
			char rdata[] = new char[128];
			status = CPUAPDU_FUNC(lDevint,5,senddata,revlen,rdata);
			if (status != 0)
			{
				return(status);
			} 
			else if (rdata[0] < MF_DELCONDITION)
			{
				return (0x00006982); //锟斤拷全状态锟斤拷锟斤拷锟斤拷
			}
		}
		
		status = CPUAPDU_FUNC(lDevint,7,sbuff,rlen,rbuff);
		
		if (status != 0)
		{
			if(0xCF == status)
			return CMD_OK;
			
			if(0xF2 == status)
			return CMD_OK;
			
			return(ERR_APDU);
		}
		
		if (rbuff[rlen[0] -2] != 0x90 && rbuff[rlen[0] -2] != 0x61)
		{
			st = (rbuff[0] << 8) + rbuff[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Create End MF锟斤拷锟斤拷锟斤拷MF锟斤拷使锟斤拷全锟斤拷锟斤拷锟斤拷效锟斤拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosCreateEndMF(int lDevint)
	{
		int status = -1;
		char sbuff[] = new char[]  {0x80,0xe0,0x00,0x01,0x02,0x3f,0x00};
		//	int[] rlen = new int[1];
		int[] rlen = new int[1];
		char rbuff[] = new char[128];
		int st;
		
		
		status = CPUAPDU_FUNC(lDevint,7,sbuff,rlen,rbuff);
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuff[rlen[0]-2] != 0x90 && rbuff[rlen[0]-2] != 0x61)
		{
			st = (rbuff[0] << 8) + rbuff[1];
			return(st);
		}
		
		IscreatendMF = true;
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Create End DF锟斤拷锟斤拷锟斤拷DF锟斤拷使锟斤拷全锟斤拷锟斤拷锟斤拷效锟斤拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	  锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//DFID:       锟斤拷锟斤拷锟斤拷锟紻F锟侥硷拷锟斤拷识锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//锟斤拷锟界： char  DFID[4] = {'2','f','0','1'};
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosCreateEndDF(int lDevint,char[]  DFID)
	{
		int status = -1;
		char sbuff[] = new char[128];
		int st;
		char Data[] = new char[128];
		int i = 0;
		int[] rlen = new int[1];
		char rbuff[] =new char[128];
		
		sbuff[0] = 0x80;
		sbuff[1] = 0xe0;
		sbuff[2] = 0x01;
		sbuff[3] = 0x01;
		sbuff[4] = 0x02;
		
		pl_javacall._a_hex(Data, DFID, 2);
		for ( i = 0; i < 2; i++)
		{
			sbuff[5+i] = Data[i];
		}
		
		
		status = CPUAPDU_FUNC(lDevint,7,sbuff,rlen,rbuff);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuff[rlen[0] -2] != 0x90 && rbuff[rlen[0] -2] != 0x61)
		{
			st = (rbuff[0] << 8) + rbuff[1];
			return(st);
		}
		
		return(CMD_OK);
	}							   

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//锟斤拷锟斤拷MF
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//MFName:    锟斤拷锟斤拷锟斤拷锟組F锟侥硷拷锟斤拷疲锟绞拷锟斤拷锟斤拷锟街凤拷锟斤拷锟斤拷为10~32锟斤拷锟街节ｏ拷偶锟斤拷
	//MFNameLen锟斤拷锟斤拷锟斤拷锟斤拷锟組F锟斤拷瞥锟斤拷锟�
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosCreateMF(int lDevint,char[] MFName,int MFNameLen)
	{
		char cmdbuf[] =new char[256];
		char rbuf[] = new char[256];
		int cmdlen = MFNameLen/2 + 15;
		int status = -1;
		int i, j = 0;
		int[] rlen = new int[1];
		int st;
		char MFNameData[] = new char[256];
		
		pl_javacall._a_hex(MFNameData, MFName, MFNameLen/2);
		
		cmdbuf[0] = 0x80;
		cmdbuf[1] = 0xe0;
		cmdbuf[2] = 0x00;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = (char)(MFNameLen/2+10);
		for ( j = 0; j < 8; j++)
		{
			cmdbuf[5+j] = 0xff;
		}
		
		cmdbuf[13] = MF_PERMISSION;
		cmdbuf[14] = 0x01;
		for ( i = 0; i < MFNameLen/2; i++)
		{
			cmdbuf[15+i] = MFNameData[i];
		}
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//锟斤拷锟斤拷DF
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//DFID:       锟斤拷锟斤拷锟斤拷锟紻F锟侥硷拷锟斤拷识锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//DFName:     锟斤拷锟斤拷锟斤拷锟紻F锟斤拷锟斤拷疲锟绞拷锟斤拷锟斤拷锟街凤拷锟斤拷锟斤拷为10~32锟斤拷锟街节ｏ拷偶锟斤拷
	//DFNameLen锟斤拷 锟斤拷锟斤拷锟斤拷锟紻F锟斤拷瞥锟斤拷锟�
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosCreateDF(int lDevint,
	char[] DFID,
	char[] DFName,
	int DFNameLen)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = DFNameLen/2 + 9;
		int status = -1;
		int i;
		int[] rlen = new int[1];
		int st;
		char DFIDData[] = new char[256];
		char DFNameData[] = new char[256];
		
		pl_javacall._a_hex(DFIDData, DFID, 2);
		pl_javacall._a_hex(DFNameData, DFName, DFNameLen/2);
		
		cmdbuf[0] = 0x80;
		cmdbuf[1] = 0xe0;
		cmdbuf[2] = 0x01;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = (char)(DFNameLen/2 + 4);
		cmdbuf[5] = DFIDData[0];
		cmdbuf[6] = DFIDData[1];
		cmdbuf[7] = DF_PERMISSION;
		cmdbuf[8] = 0x00;
		for ( i = 0; i < DFNameLen/2; i++)
		{
			cmdbuf[9+i] = DFNameData[i];
		}
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//锟斤拷锟斤拷EF
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	      锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//EFID:             锟斤拷锟斤拷锟斤拷锟紼F锟侥硷拷锟斤拷识锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//EFType:           锟斤拷锟斤拷锟斤拷锟紼F锟侥硷拷锟斤拷锟酵ｏ拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//EFLen:            锟斤拷锟斤拷锟斤拷锟紼F锟侥硷拷锟斤拷锟饺ｏ拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//ReadPermission:   锟斤拷锟斤拷锟斤拷锟斤拷权锟睫ｏ拷=0锟斤拷锟斤拷锟斤拷锟矫伙拷锟缴讹拷锟斤拷=1锟斤拷锟斤拷锟斤拷锟矫伙拷锟缴讹拷锟斤拷=2锟斤拷锟斤拷锟斤拷员锟缴讹拷
	//UpdatePermission: 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷权锟睫ｏ拷=0锟斤拷锟斤拷锟斤拷锟矫伙拷锟缴革拷锟铰ｏ拷=1锟斤拷锟斤拷锟斤拷锟矫伙拷锟缴革拷锟铰ｏ拷=2锟斤拷锟斤拷锟斤拷员锟缴革拷锟斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosCreateEF(int lDevint,
	char[] EFID,
	char[] EFType,
	char[] EFLen,
	int ReadPermission,
	int UpdatePermission)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = 12;
		int status = -1;
		//int[] rlen = new int[1];
		int[] rlen = new int[1];
		int st;
		char EFIDHex[] = new char[256];;
		char EFTypeHex[] = new char[256];;
		char EFLenHex[] = new char[256];;
		
		
		pl_javacall._a_hex(EFIDHex, EFID, 2);
		pl_javacall._a_hex(EFTypeHex,EFType,1);
		pl_javacall._a_hex(EFLenHex,EFLen, 2);
		
		cmdbuf[0] = 0x80;
		cmdbuf[1] = 0xe0;
		cmdbuf[2] = 0x02;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = 0x07;
		cmdbuf[5] = EFIDHex[0];
		cmdbuf[6] = EFIDHex[1];
		cmdbuf[7] = EFTypeHex[0];
		cmdbuf[8] = 0x0f;
		cmdbuf[9] = 0x0f;
		cmdbuf[10] = EFLenHex[0];
		cmdbuf[11] = EFLenHex[1];
		if ((cmdbuf[7] & 0x0f) == 0x05)  //锟斤拷钥锟侥硷拷
		{
			switch(UpdatePermission)
			{
			case 0: cmdbuf[8] = KEYEF_ADD_PERMISSION_ALL;break;
			case 1: cmdbuf[8] = KEYEF_ADD_PERMISSION_USER;break;
			case 2: cmdbuf[8] = KEYEF_ADD_PERMISSION_ADMIN;break;
			}
			cmdbuf[9] = 0x00;
		}
		else
		{
			switch(ReadPermission)
			{
			case 0: cmdbuf[8] = EF_READ_PERMISSION_ALL;break;
			case 1: cmdbuf[8] = EF_READ_PERMISSION_USER;break;
			case 2: cmdbuf[8] = EF_READ_PERMISSION_ADMIN;break;
			}
			switch(UpdatePermission)
			{
			case 0: cmdbuf[9] = EF_UPDATE_PERMISSION_ALL;break;
			case 1: cmdbuf[9] = EF_UPDATE_PERMISSION_USER;break;
			case 2: cmdbuf[9] = EF_UPDATE_PERMISSION_ADMIN;break;
			}
		}
		
		if ((cmdbuf[7] & 0x0f) == 0x06 || (cmdbuf[7] & 0x0f) == 0x07)  //钱锟斤拷锟斤拷锟斤拷募锟�
		{
			cmdbuf[8] = 0x00;
			cmdbuf[9] = 0x00;
			cmdbuf[10] = 0x00;
			cmdbuf[11] = 0x00;
		}
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//选锟斤拷锟侥硷拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//FileType:   锟斤拷锟斤拷锟斤拷锟窖★拷锟斤拷募锟斤拷锟斤拷停锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//=00锟斤拷选锟斤拷MF锟斤拷DF锟斤拷=02锟斤拷选锟斤拷EF
	//FileID:     锟斤拷锟斤拷锟斤拷锟窖★拷锟斤拷募锟斤拷锟绞讹拷锟绞拷锟斤拷锟斤拷锟街凤拷4锟街斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosSelectFile(int lDevint,
	char[] FileType,
	char[] FileID)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = 7;
		int status = -1;
		int[] rlen = new int[1];
		//char rlen = 0;
		int st;
		char FileTypeHex[] = new char[256];;
		char FileIDHex[] = new char[256];;
		
		
		status = pl_javacall._a_hex(FileTypeHex, FileType,1);
		status = pl_javacall._a_hex(FileIDHex, FileID, 2);
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0xa4;
		cmdbuf[2] = FileTypeHex[0];
		cmdbuf[3] = 0x00;
		cmdbuf[4] = 0x02;
		cmdbuf[5] = FileIDHex[0];
		cmdbuf[6] = FileIDHex[1];
		
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

//////////////////////////////////////////////////////////////////////////////////////
////锟斤拷锟斤拷锟杰ｏ拷
////Append Record 追锟接硷拷录
////锟斤拷锟斤拷锟斤拷锟�
////lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
////RecordSID:      锟斤拷锟斤拷锟斤拷锟斤拷录锟侥硷拷锟教憋拷识锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
////RecordDATA:     锟斤拷锟斤拷锟斤拷锟斤拷录锟斤拷荩锟绞拷锟斤拷锟斤拷锟街凤拷锟斤拷锟斤拷为偶锟斤拷
////RecordDATALen:  锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷RecordDATA锟侥筹拷锟斤拷
////Mode:           锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷写锟斤拷模式锟斤拷0-锟斤拷锟侥ｏ拷1-锟斤拷锟斤拷+MAC
////keybuf:         锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷写锟斤拷模式锟剿诧拷锟斤拷锟斤拷效锟斤拷32锟街斤拷
////锟斤拷锟截ｏ拷
////锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
///////////////////////////////////////////////////////////////////////////////////////
//public int FWCosAppendRecord(int lDevint,
//char[] RecordSID,
//char[] RecordDATA,
//int  RecordDATALen,
//int  Mode,
//char[] keybuf)
//{
//char cmdbuf[] = new char[256];;
//char rbuf[] = new char[256];
//int cmdlen = RecordDATALen/2 + 5;
//int status = -1;
//int i = 0;
//int[] rlen = new int[1];
//int st;
//char RecordSIDHex[] = new char[256];;
//char RecordDATAHex[] = new char[256];;
//
//
//publib._a_hex(RecordSIDHex, RecordSID, 1);
//publib._a_hex(RecordDATAHex, RecordDATA, RecordDATALen/2);
//
//cmdbuf[0] = 0x00;
//cmdbuf[1] = 0xe2;
//cmdbuf[2] = 0x00;
//cmdbuf[3] = (RecordSIDHex[0] << 3);
//cmdbuf[4] = RecordDATALen/2;
//for ( i = 0; i < RecordDATALen/2; i++)
//{
//cmdbuf[5+i] = RecordDATAHex[i];
//}
//
//if (Mode == 1)  //锟斤拷锟斤拷+MAC模式
//{
//char data[] = new char[256];;
//char ptrdate[] = new char[256];;
//char key[] =new char[50];;
//char challenge[] = new char[9];;
// int challengelen;
//int ptrdatelen ;
//char MAC[] = new char[9];;
//
//cmdbuf[0] = 0x04;          //锟斤拷锟斤拷锟斤拷式
//
//
//data[0] = RecordDATALen/2;
//for (i = 0; i < RecordDATALen/2; i++)
//{
//data[1+i] = cmdbuf[5+i];
//}
//
//publib._a_hex(key, keybuf, 16);
//publib.ic_encrypt_3DES_ECB((char[])key,data,( short)(RecordDATALen/2 + 1),ptrdate);   //锟斤拷锟斤拷锟斤拷锟斤拷
//
//
//FWCosGetChallenge(lDevint,04,challenge,&challengelen);   //取锟斤拷锟斤拷锟�
//ptrdatelen = strlen(ptrdate);
//cmdbuf[4] = ptrdatelen + 4;
//for (i = 0; i < ptrdatelen; i++)
//{
//cmdbuf[5+i] = ptrdate[i];
//}
//
//publib.ic_encrypt_3DES_ECB_MAC((char[])key,(char[])cmdbuf,( short)(ptrdatelen+5),MAC,challenge);  //锟斤拷锟斤拷MAC
//for (i = 0; i < 4; i++)
//{
//cmdbuf[5+ptrdatelen+i] = MAC[i];                                    //锟斤拷锟斤拷+MAC写锟斤拷锟斤拷锟斤拷锟斤拷锟�
//}
//cmdlen = ptrdatelen + 9;
//}
//
//
//status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
//
//
//if (status != 0)
//{
//return(ERR_APDU);
//}
//
//if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
//{
//st = (rbuf[0] << 8) + rbuf[1];
//return(st);
//}
//
//return(CMD_OK);
//}
//
//////////////////////////////////////////////////////////////////////////////////////
////锟斤拷锟斤拷锟杰ｏ拷
////Read Record 锟斤拷锟斤拷录
////锟斤拷锟斤拷锟斤拷锟�
////lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
////RecordNum:      锟斤拷锟斤拷锟斤拷锟揭拷锟斤拷锟铰硷拷牛锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
////RecordSID:      锟斤拷锟斤拷锟斤拷锟斤拷录锟侥硷拷锟教憋拷识锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
////RecordLen:      锟斤拷锟斤拷锟斤拷锟揭拷锟斤拷锟铰硷拷某锟斤拷龋锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
////pvStr:		    锟斤拷锟斤拷锟斤拷锟斤拷欧锟斤拷锟斤拷锟较拷幕锟斤拷锟斤拷锟�
////锟斤拷锟截ｏ拷
////锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
///////////////////////////////////////////////////////////////////////////////////////
//public int FWCosReadRecord(int  lDevint,
//char[]  RecordNum,
//char[]  RecordSID,
//char[]  RecordLen,
//char[]  pvStr)
//{
//char cmdbuf[] = new char[256];;
//char rbuf[] = new char[256];
//int cmdlen = 5;
//int status = -1;
//int[] rlen = new int[1];
////char rlen = 0;
//int st;
//char RecordNumHex[] = new char[256];;
//char RecordSIDHex[] = new char[256];;
//char RecordLenHex[] = new char[256];;
//
//
//publib._a_hex(RecordNumHex, RecordNum, 1);
//publib._a_hex(RecordSIDHex, RecordSID, 1);
//publib._a_hex(RecordLenHex, RecordLen, 1);
//
//cmdbuf[0] = 0x00;
//cmdbuf[1] = 0xb2;
//cmdbuf[2] = RecordNumHex[0];
//cmdbuf[3] = (RecordSIDHex[0] << 3) + 0x04;
//cmdbuf[4] = RecordLenHex[0];
//
//
//status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
//
//
//if (status != 0)
//{
//return(ERR_APDU);
//}
//
//if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
//{
//st = (rbuf[0] << 8) + rbuf[1];
//return(st);
//}
//
//publib._hex_a(pvStr, rbuf, 2*(rlen-2));
//return(CMD_OK);
//}

//////////////////////////////////////////////////////////////////////////////////////
////锟斤拷锟斤拷锟杰ｏ拷
////Update Record 锟睫改硷拷录
////锟斤拷锟斤拷锟斤拷锟�
////lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
////RecordNum:      锟斤拷锟斤拷锟斤拷锟揭拷薷募锟铰硷拷牛锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
////RecordSID:      锟斤拷锟斤拷锟斤拷锟斤拷录锟侥硷拷锟教憋拷识锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
////RecordDATA:     锟斤拷锟斤拷锟斤拷锟斤拷录锟铰硷拷锟捷ｏ拷十锟斤拷锟斤拷锟斤拷址锟斤拷锟斤拷锟轿硷拷锟�
////RecordLen:      锟斤拷锟斤拷锟斤拷锟斤拷录锟铰硷拷某锟斤拷锟�
////Mode:           锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷写锟斤拷模式锟斤拷0-锟斤拷锟侥ｏ拷1-锟斤拷锟斤拷+MAC
////keybuf:         锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷写锟斤拷模式锟剿诧拷锟斤拷锟斤拷效锟斤拷32锟街斤拷
////锟斤拷锟截ｏ拷
////锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
///////////////////////////////////////////////////////////////////////////////////////
//public int FWCosUpdateRecord(int  lDevint,
//char[]  RecordNum,
//char[]  RecordSID,
//char[]  RecordDATA,
//int  RecordLen,
//int  Mode,
//char[]  keybuf)
//{
//char cmdbuf[] = new char[256];;
//char rbuf[] = new char[256];
//int cmdlen = RecordLen/2 + 5;
//int status = -1;
//int[] rlen = new int[1];
////char rlen = 0;
//int st ,i ;
//char RecordNumHex[] = new char[256];;
//char RecordSIDHex[] = new char[256];;
//char RecordDATAHex[] = new char[256];;
//
//publib._a_hex(RecordNumHex, RecordNum, 1);
//publib._a_hex(RecordSIDHex, RecordSID, 1);
//publib._a_hex(RecordDATAHex,RecordDATA,RecordLen/2);
//
//
//
//cmdbuf[0] = 0x00;
//cmdbuf[1] = 0xdc;
//cmdbuf[2] = RecordNumHex[0];
//cmdbuf[3] = (RecordSIDHex[0] << 3) + 0x04;
//cmdbuf[4] = RecordLen/2;
//for ( i = 0; i < RecordLen/2; i++)
//{
//cmdbuf[5+i] = RecordDATAHex[i];
//}
//
//if (Mode == 1)  //锟斤拷锟斤拷+MAC模式
//{
//char data[] = new char[256];;
//char ptrdate[] = new char[256];;
//char key[] =new char[50];;
//char challenge[] = new char[9];;
// int challengelen;
//int ptrdatelen;
//char MAC[] = new char[9];;
//
//cmdbuf[0] = 0x04;          //锟斤拷锟斤拷锟斤拷式
//data[0] = RecordLen/2;
//for (i = 0; i < RecordLen/2; i++)
//{
//data[1+i] = cmdbuf[5+i];
//}
//publib._a_hex((char[])key,keybuf,16);
//publib.ic_encrypt_3DES_ECB((char[])key,data,( short)(RecordLen/2 +1),ptrdate);   //锟斤拷锟斤拷锟斤拷锟斤拷
//
//
//FWCosGetChallenge(lDevint,04,challenge,&challengelen);   //取锟斤拷锟斤拷锟�
//ptrdatelen = strlen(ptrdate);
//cmdbuf[4] = ptrdatelen + 4;
//for (i = 0; i < ptrdatelen; i++)
//{
//cmdbuf[5+i] = ptrdate[i];
//}
//
//publib.ic_encrypt_3DES_ECB_MAC((char[])key,(char[])cmdbuf,( short)(ptrdatelen+5),MAC,challenge);  //锟斤拷锟斤拷MAC
//for (i = 0; i < 4; i++)
//{
//cmdbuf[5+ptrdatelen+i] = MAC[i];                                    //锟斤拷锟斤拷+MAC写锟斤拷锟斤拷锟斤拷锟斤拷锟�
//}
//cmdlen = ptrdatelen + 9;
//}
//
//status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
//
//if (status != 0)
//{
//return(ERR_APDU);
//}
//
//if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
//{
//st = (rbuf[0] << 8) + rbuf[1];
//return(st);
//}
//
//return(CMD_OK);
//}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Write KEY  锟斤拷装锟斤拷钥
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//KEYType:        锟斤拷锟斤拷锟斤拷锟斤拷锟皆匡拷锟斤拷停锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//KEYDATA:        锟斤拷锟斤拷锟斤拷锟斤拷锟皆匡拷锟捷ｏ拷十锟斤拷锟斤拷锟斤拷址锟�~32锟街节ｏ拷偶锟斤拷
	//KEYDATALen:     锟斤拷锟斤拷锟斤拷锟斤拷锟皆匡拷锟捷筹拷锟斤拷
	//Mode:           锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷写锟斤拷模式锟斤拷0-锟斤拷锟侥ｏ拷1-锟斤拷锟斤拷+MAC
	//keybuf:         锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷写锟斤拷模式锟剿诧拷锟斤拷锟斤拷效锟斤拷32锟街斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosWriteKEY(int  lDevint,
	char[]  KEYType,
	char[]  KEYDATA,
	int  KEYDATALen,
	int  Mode,
	char[] keybuf)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = KEYDATALen/2 + 13;
		int status = -1;
		int i = 0;
		int[] rlen = new int[1];
		int st;
		char KEYTypeHex[]= new  char[5];;
		char KEYDATAHex[] =new char[50];;
		
		
		pl_javacall._a_hex(KEYTypeHex,KEYType,1);
		pl_javacall._a_hex(KEYDATAHex,KEYDATA,KEYDATALen/2);
		
		cmdbuf[0] = 0x80;
		cmdbuf[1] = 0xd4;
		cmdbuf[2] = 0x00;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = (char)( KEYDATALen/2 + 8);
		cmdbuf[5] = KEY_ID;
		cmdbuf[6] = KEY_VERSION;
		cmdbuf[7] = KEY_ALGORITHM;
		cmdbuf[8] = KEYTypeHex[0];
		cmdbuf[9] = KEY_USEPERMISION;
		cmdbuf[10] = 0x00;
		cmdbuf[11] = 0x00;
		cmdbuf[12] = KEY_ERROR_COUNT;
		if ((cmdbuf[8] & 0x0f) == 0x08)         //锟斤拷锟矫癸拷锟斤拷员锟斤拷钥锟侥猴拷锟斤拷状态锟斤拷锟睫革拷权锟斤拷
		{
			cmdbuf[10] = KEY_SUCCEEDING_STATE_ADMIN;
			cmdbuf[11] = KEY_UPDATE_PERMISSION_ADMIN;
		}
		if ((cmdbuf[8] & 0x0f) == 0x0b)         //锟斤拷锟矫革拷锟斤拷锟斤拷锟斤拷暮锟斤拷锟阶刺拷锟斤拷薷锟饺拷锟�
		{
			cmdbuf[10] = KEY_SUCCEEDING_STATE_PIN;
			cmdbuf[11] = KEY_UPDATE_PERMISSION_PIN;
		}
		for ( i = 0; i < KEYDATALen/2; i++)
		{
			cmdbuf[13+i] = KEYDATAHex[i];
		}
		
		if (Mode == 1)        //锟斤拷锟斤拷+MAC模式
		{
			char data[] = new char[256];;
			char ptrdate[] = new char[256];;
			char key[] =new char[50];;
			char challenge[] = new char[9];;
			int challengelen[] = new int[1];
			int ptrdatelen;
			char MAC[] = new char[9];;
			
			cmdbuf[0] = 0x84;          //锟斤拷锟斤拷锟斤拷式
			data[0] = (char)( KEYDATALen/2 +8);
			for (i = 0; i < KEYDATALen/2 + 8; i++)
			{
				data[1+i] = cmdbuf[5+i];
			}
			
			pl_javacall._a_hex((char[])key,keybuf,16);
			pl_javacall.ic_encrypt_3DES_ECB(key,data,( short)(KEYDATALen/2 + 9),ptrdate);   //锟斤拷锟斤拷锟斤拷锟斤拷
			
			
			FWCosGetChallenge(lDevint,(char)04,challenge,challengelen);   //取锟斤拷锟斤拷锟�
			ptrdatelen = String.valueOf(ptrdate).length();
			cmdbuf[4] =(char)( ptrdatelen + 4);
			for (i = 0; i < ptrdatelen; i++)
			{
				cmdbuf[5+i] = ptrdate[i];
			}
			
			pl_javacall.ic_encrypt_3DES_ECB_MAC(key,cmdbuf,( short)(ptrdatelen+5),MAC,challenge);  //锟斤拷锟斤拷MAC
			for (i = 0; i < 4; i++)
			{
				cmdbuf[5+ptrdatelen+i] = MAC[i];                               //锟斤拷锟斤拷+MAC写锟斤拷锟斤拷锟斤拷锟斤拷锟�
			}
			cmdlen = ptrdatelen + 9;
		}
		
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Update KEY  锟睫革拷锟斤拷钥
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//KEYType:        锟斤拷锟斤拷锟斤拷锟揭拷薷锟斤拷锟皆匡拷锟斤拷停锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//KEYDATA:        锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟皆匡拷锟捷ｏ拷十锟斤拷锟斤拷锟斤拷址锟�~32锟街节ｏ拷偶锟斤拷
	//KEYDATALen:     锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟皆匡拷锟捷的筹拷锟斤拷
	//Mode:           锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷写锟斤拷模式锟斤拷0-锟斤拷锟侥ｏ拷1-锟斤拷锟斤拷+MAC
	//keybuf:         锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷写锟斤拷模式锟剿诧拷锟斤拷锟斤拷效锟斤拷32锟街斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosUpdateKEY(int  lDevint,
	char[]  KEYType,
	char[]  KEYDATA,
	int  KEYDATALen,
	int  Mode,
	char[] keybuf)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = KEYDATALen/2 + 13;
		int status = -1;
		int i = 0;
		int[] rlen = new int[1];
		int st;
		char KEYTypeHex[]= new  char[5];;
		char KEYDATAHex[] =new char[50];;
		
		
		pl_javacall._a_hex(KEYTypeHex,KEYType,1);
		pl_javacall._a_hex(KEYDATAHex,KEYDATA,KEYDATALen/2);
		
		cmdbuf[0] = 0x80;
		cmdbuf[1] = 0xd4;
		cmdbuf[2] = 0x01;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = (char)(KEYDATALen/2 + 8);
		cmdbuf[5] = KEY_ID;
		cmdbuf[6] = KEY_VERSION;
		cmdbuf[7] = KEY_ALGORITHM;
		cmdbuf[8] = KEYTypeHex[0];
		cmdbuf[9] = KEY_USEPERMISION;
		cmdbuf[10] = 0x00;
		cmdbuf[11] = 0x00;
		cmdbuf[12] = KEY_ERROR_COUNT;
		if ((cmdbuf[8] & 0x0f) == 0x08)         //锟斤拷锟矫癸拷锟斤拷员锟斤拷钥锟侥猴拷锟斤拷状态锟斤拷锟睫革拷权锟斤拷
		{
			cmdbuf[10] = KEY_SUCCEEDING_STATE_ADMIN;
			cmdbuf[11] = KEY_UPDATE_PERMISSION_ADMIN;
		}
		if ((cmdbuf[8] & 0x0f) == 0x0b)         //锟斤拷锟矫革拷锟斤拷锟斤拷锟斤拷暮锟斤拷锟阶刺拷锟斤拷薷锟饺拷锟�
		{
			cmdbuf[10] = KEY_SUCCEEDING_STATE_PIN;
			cmdbuf[11] = KEY_UPDATE_PERMISSION_PIN;
		}
		for ( i = 0; i < KEYDATALen/2; i++)
		{
			cmdbuf[13+i] = KEYDATAHex[i];
		}
		
		if (Mode == 1)        //锟斤拷锟斤拷+MAC模式
		{
			char data[] = new char[256];;
			char ptrdate[] = new char[256];;
			char key[] =new char[50];;
			char challenge[] = new char[9];;
			int challengelen[] = new int[1];
			int ptrdatelen;
			char MAC[] = new char[9];;
			
			cmdbuf[0] = 0x84;          //锟斤拷锟斤拷锟斤拷式
			cmdbuf[2] = cmdbuf[8];
			cmdbuf[3] = KEY_ID;
			
			data[0] =(char)( KEYDATALen/2 +8);
			for (i = 0; i < KEYDATALen/2 + 8; i++)
			{
				data[1+i] = cmdbuf[5+i];
			}
			
			pl_javacall._a_hex((char[])key, keybuf, 16);
			pl_javacall.ic_encrypt_3DES_ECB((char[])key,data,( short)(KEYDATALen/2 + 9),ptrdate);   //锟斤拷锟斤拷锟斤拷锟斤拷
			
			
			FWCosGetChallenge(lDevint,(char)04,challenge,challengelen);   //取锟斤拷锟斤拷锟�
			ptrdatelen = String.valueOf(ptrdate).length();
			cmdbuf[4] = (char)( ptrdatelen + 4);
			for (i = 0; i < ptrdatelen; i++)
			{
				cmdbuf[5+i] = ptrdate[i];
			}
			
			pl_javacall.ic_encrypt_3DES_ECB_MAC((char[])key,(char[])cmdbuf,( short)(ptrdatelen+5),MAC,challenge);  //锟斤拷锟斤拷MAC
			for (i = 0; i < 4; i++)
			{
				cmdbuf[5+ptrdatelen+i] = MAC[i];                                    //锟斤拷锟斤拷+MAC写锟斤拷锟斤拷锟斤拷锟斤拷锟�
			}
			cmdlen = ptrdatelen + 9;
		}
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Get Challenge 锟斤拷锟斤拷锟斤拷锟斤拷锟�
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	        锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//Le:                 锟斤拷锟斤拷锟斤拷锟絃e=04锟斤拷取4位锟斤拷锟斤拷锟絃e=08锟斤拷取8位锟斤拷锟斤拷锟�
	//Challenge:		    锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷十锟斤拷锟斤拷锟斤拷址锟�
	//ChallengeLen:	    锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosGetChallenge(int  lDevint,char Le,char[]  Challenge,int[]  ChallengeLen)
	{
		char cmdbuf[] = new char[256];
		char rbuf[] = new char[256];
		int cmdlen = 5;
		int status = -1;
		int[] rlen = new int[1];
		//char rlen = 0;
		int st, i;
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0x84;
		cmdbuf[2] = 0x00;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = Le;
		
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		ChallengeLen[0] = rlen[0] - 2;
		
		for(i=0; i<ChallengeLen[0]; i++)Challenge[i] = rbuf[i];

		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//External Authentication 锟解部锟斤拷证
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//KEYbuf:         锟斤拷锟斤拷锟斤拷锟斤拷锟皆恐碉拷锟绞拷锟斤拷锟斤拷锟街凤拷32锟街斤拷
	//KEYbuflen:      锟斤拷锟斤拷锟斤拷锟斤拷锟皆匡拷锟斤拷锟�
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosExternalAuthentication(int  lDevint,
	char[]  KEYbuf,
	int  KEYbuflen)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = 13;
		int status = -1;
		int i = 0;
		int[] rlen = new int[1];
		char Challenge[] = new char[9];;
		int Challengelen[] = new int[1];
		char EncryptChallenge[] = new char[9];;
		int st;
		char KEYbufHex[] =new char[50];;
		
		
		pl_javacall._a_hex(KEYbufHex,  KEYbuf, KEYbuflen/2);
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0x82;
		cmdbuf[2] = 0x00;
		cmdbuf[3] = 0x00;
		cmdbuf[4] = 0x08;
		
		FWCosGetChallenge(lDevint,(char)0x08,Challenge,Challengelen);
		
		if (KEYbuflen/2 == 8)
		{
			pl_javacall.ic_encrypt((char[])KEYbufHex,Challenge,(short)8,EncryptChallenge);
		}
		if (KEYbuflen/2 == 16)
		{
			pl_javacall.ic_encrypt_3DES_ECB((char[])KEYbufHex,Challenge,(short)8,EncryptChallenge);
		}
		
		for ( i = 0; i < 8; i++)
		{
			cmdbuf[5+i] = EncryptChallenge[i];
		}
		
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Verify 校锟斤拷锟斤拷锟斤拷锟斤拷锟�
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//PINbuf:         锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟诫，十锟斤拷锟斤拷锟斤拷址锟�~12锟街节ｏ拷偶锟斤拷
	//PINlen:         锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟诫长锟斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosVerify(int  lDevint,
	char[]  PINbuf,
	int  PINlen)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = PINlen/2 + 5;
		int status = -1;
		int i = 0;
		int[] rlen = new int[1];
		int st;
		char PINbufHex[] =new char[50];;
		
		
		pl_javacall._a_hex(PINbufHex, PINbuf, PINlen/2);
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0x20;
		cmdbuf[2] = 0x00;
		cmdbuf[3] = 0x00;
		
		cmdbuf[4] = (char)(PINlen/2);
		
		for ( i = 0; i < PINlen/2; i++)
		{
		cmdbuf[5+i] = PINbufHex[i];
		}
		
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Get Data 取锟斤拷片锟斤拷锟�
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//DataType:       锟斤拷锟斤拷锟斤拷锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//00-取PIN锟斤拷锟皆硷拷锟斤拷锟斤拷锟斤拷01-取芯片锟斤拷锟叫号ｏ拷02-取锟斤拷片剩锟斤拷
	//锟秸间；03-取锟斤拷片状态值锟斤拷04-取锟斤拷片锟节诧拷锟斤拷锟斤拷姹撅拷锟�
	//Data:		    锟斤拷锟斤拷锟斤拷锟绞拷锟斤拷锟斤拷锟街凤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosGetData(int  lDevint,
	char[]  DataType,
	char[]  Data)
	{
		char cmdbuf[] = new char[256];;
		char rbuf[] = new char[256];
		int cmdlen = 5;
		int status = -1;
		int[] rlen = new int[1];
		//char rlen = 0;
		int st;
		char DataTypeHex[]= new  char[5];;
		
		pl_javacall._a_hex(DataTypeHex, DataType, 1);
		
		cmdbuf[0] = 0x80;
		cmdbuf[1] = 0xca;
		cmdbuf[2] = 0x00;
		cmdbuf[3] = DataTypeHex[0];
		
		switch(DataTypeHex[0])
		{
		case 0x01 : cmdbuf[4] = 0x08; break;
		case 0x02 : cmdbuf[4] = 0x02; break;
		case 0x00 : 
		case 0x03 : 
		case 0x04 : cmdbuf[4] = 0x01; break; 
		}
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
		st = (rbuf[0] << 8) + rbuf[1];
		return(st);
		}
		
		pl_javacall._hex_a(Data, rbuf, 2*(rlen[0]-2));
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Update Binary 锟睫改讹拷锟斤拷锟斤拷锟侥硷拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	   锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//BinarySID:     锟斤拷锟斤拷锟斤拷锟揭拷薷亩锟斤拷锟斤拷锟斤拷募锟斤拷瘫锟绞讹拷锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//Offset:        锟斤拷锟斤拷锟斤拷锟揭拷薷亩锟斤拷锟斤拷锟斤拷募锟狡拷频锟街凤拷锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//BinaryDATA:    锟斤拷锟斤拷锟斤拷锟斤拷碌亩锟斤拷锟斤拷锟斤拷锟捷ｏ拷十锟斤拷锟斤拷锟斤拷址锟斤拷锟斤拷锟轿硷拷锟�
	//BinaryDATALen: 锟斤拷锟斤拷锟斤拷锟斤拷碌亩锟斤拷锟斤拷锟斤拷锟捷筹拷锟斤拷
	//Mode:          锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷写锟斤拷模式锟斤拷0-锟斤拷锟侥ｏ拷1-锟斤拷锟斤拷+MAC
	//keybuf:        锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷写锟斤拷模式锟剿诧拷锟斤拷锟斤拷效锟斤拷32锟街斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosUpdateBinary(int  lDevint,
	char[]  BinarySID,
	char[]  Offset,
	char[]  BinaryDATA,
	int  BinaryDATALen,
	int  Mode,
	char[]  keybuf)
	{
		char cmdbuf[] =new char[1024];
		char rbuf[] =new char[1024];
		int cmdlen = BinaryDATALen/2 + 5;
		int status = -1;
		int i = 0;
		int[] rlen = new int[1];
		int st;
		char BinarySIDHex[]= new  char[5];
		char OffsetHex[]= new  char[5];
		char BinaryDATAHex[] =new char[1024];
		
		
		pl_javacall._a_hex(BinarySIDHex, BinarySID, 1);
		pl_javacall._a_hex(OffsetHex, Offset,1);
		pl_javacall._a_hex(BinaryDATAHex, BinaryDATA, BinaryDATALen/2);
		
		
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0xd6;
		cmdbuf[2] = (char)(BinarySIDHex[0] + 0x80);
		cmdbuf[3] = OffsetHex[0];
		cmdbuf[4] = (char)(BinaryDATALen/2);
		
		for ( i = 0; i < BinaryDATALen/2; i++)
		{
			cmdbuf[5+i] = BinaryDATAHex[i];
		}
		
		if (Mode == 0) //锟斤拷锟斤拷模式
		{
		
			status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		}
		
		if (Mode == 1)  //锟斤拷锟斤拷+MAC模式
		{
			char data[] =new char[1024];;
			char ptrdate[] =new char[1024];;
			char key[] =new char[50];;
			char challenge[] = new char[9];;
			 int challengelen[] = new int[1];
			int ptrdatelen;
			char MAC[] = new char[9];;
			
			cmdbuf[0] = 0x04;          //锟斤拷锟斤拷锟斤拷式
			
			data[0] =(char)( BinaryDATALen/2);
			for (i = 0; i < BinaryDATALen/2; i++)
			{
				data[1+i] = cmdbuf[5+i];
			}
			
			pl_javacall._a_hex((char[])key, keybuf, 16);
			pl_javacall.ic_encrypt_3DES_ECB(key,data,( short)(BinaryDATALen/2 + 1),ptrdate);   //锟斤拷锟斤拷锟斤拷锟斤拷
			
			
			FWCosGetChallenge(lDevint,(char)04,challenge,challengelen);   //取锟斤拷锟斤拷锟�
			ptrdatelen = String.valueOf(ptrdate).length();
			cmdbuf[4] = (char)(ptrdatelen + 4);
			for (i = 0; i < ptrdatelen; i++)
			{
				cmdbuf[5+i] = ptrdate[i];
			}
			
			pl_javacall.ic_encrypt_3DES_ECB_MAC(key,cmdbuf,( short)(ptrdatelen+5),MAC,challenge);  //锟斤拷锟斤拷MAC
			for (i = 0; i < 4; i++)
			{
				cmdbuf[5+ptrdatelen+i] = MAC[i];                                    //锟斤拷锟斤拷+MAC写锟斤拷锟斤拷锟斤拷锟斤拷锟�
			}
			
			
			status = CPUAPDU_FUNC(lDevint,(char)(ptrdatelen+9),cmdbuf,rlen,rbuf);
		
		}
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}
	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Update Binary 锟睫改讹拷锟斤拷锟斤拷锟侥硷拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	   锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//Offset:        锟斤拷锟斤拷锟斤拷锟揭拷薷亩锟斤拷锟斤拷锟斤拷募锟狡拷频锟街凤拷锟绞拷锟斤拷锟斤拷锟街凤拷4锟街斤拷
	//BinaryDATA:    锟斤拷锟斤拷锟斤拷锟斤拷碌亩锟斤拷锟斤拷锟斤拷锟捷ｏ拷十锟斤拷锟斤拷锟斤拷址锟斤拷锟斤拷锟轿硷拷锟�
	//BinaryDATALen: 锟斤拷锟斤拷锟斤拷锟斤拷碌亩锟斤拷锟斤拷锟斤拷锟捷筹拷锟斤拷
	//Mode:          锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷写锟斤拷模式锟斤拷0-锟斤拷锟侥ｏ拷1-锟斤拷锟斤拷+MAC
	//keybuf:        锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷钥锟斤拷锟斤拷锟斤拷写锟斤拷模式锟剿诧拷锟斤拷锟斤拷效锟斤拷32锟街斤拷
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosUpdateBinaryEx(int  lDevint,
	char[]  Offset,
	char[]  BinaryDATA,
	int  BinaryDATALen,
	int  Mode,
	char[]  keybuf)
	{
		char cmdbuf[] =new char[2048];;
		char rbuf[] =new char[2048];;
		int cmdlen = BinaryDATALen/2 + 5;
		int status = -1;
		int i = 0;
		int[] rlen = new int[1];
		int st;
		char OffsetHex[] =new char[128];;
		char BinaryDATAHex[] =new char[2048];;
		
		
		pl_javacall._a_hex(OffsetHex, Offset,2);
		pl_javacall._a_hex(BinaryDATAHex, BinaryDATA, BinaryDATALen/2);
		
		
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0xd6;
		cmdbuf[2] = OffsetHex[0];
		cmdbuf[3] = OffsetHex[1];
		cmdbuf[4] =(char)( BinaryDATALen/2);
		
		for ( i = 0; i < BinaryDATALen/2; i++)
		{
			cmdbuf[5+i] = BinaryDATAHex[i];
		}
		
		if (Mode == 0) //锟斤拷锟斤拷模式
		{
		
			status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		}
		
		if (Mode == 1)  //锟斤拷锟斤拷+MAC模式
		{
			char data[] =new char[2048];
			char ptrdate[] =new char[2048];
			char key[] =new char[50];
			char challenge[] = new char[128];
			 int challengelen[] =new int[1];
			int ptrdatelen;
			char MAC[] = new char[128];
			
			cmdbuf[0] = 0x04;          //锟斤拷锟斤拷锟斤拷式
			
			data[0] =(char)( BinaryDATALen/2);
			for (i = 0; i < BinaryDATALen/2; i++)
			{
				data[1+i] = cmdbuf[5+i];
			}
			
			pl_javacall._a_hex((char[])key, keybuf, 16);
			pl_javacall.ic_encrypt_3DES_ECB(key,data,( short)(BinaryDATALen/2 + 1),ptrdate);   //锟斤拷锟斤拷锟斤拷锟斤拷
			
			
			FWCosGetChallenge(lDevint,(char)04,challenge,challengelen);   //取锟斤拷锟斤拷锟�
			
			ptrdatelen = 8;// String.valueOf(ptrdate).length();
			cmdbuf[4] = (char)(ptrdatelen + 4);
			for (i = 0; i < ptrdatelen; i++)
			{
				cmdbuf[5+i] = ptrdate[i];
			}
			
			pl_javacall.ic_encrypt_3DES_ECB_MAC((char[])key,(char[])cmdbuf,( short)(ptrdatelen+5),MAC,challenge);  //锟斤拷锟斤拷MAC
			for (i = 0; i < 4; i++)
			{
				cmdbuf[5+ptrdatelen+i] = MAC[i];                                    //锟斤拷锟斤拷+MAC写锟斤拷锟斤拷锟斤拷锟斤拷锟�
			}
			
			
			status = CPUAPDU_FUNC(lDevint,(char)(ptrdatelen+9),cmdbuf,rlen,rbuf);
		
		}
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		return(CMD_OK);
	}
	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Read Binary 锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//BinarySID:      锟斤拷锟斤拷锟斤拷锟揭拷锟饺★拷锟斤拷锟斤拷锟斤拷募锟斤拷瘫锟绞讹拷锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//Offset:         锟斤拷锟斤拷锟斤拷锟绞硷拷锟饺★拷锟捷碉拷偏锟狡碉拷址锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//ReadLen:        锟斤拷锟斤拷锟斤拷锟揭拷锟饺★拷锟斤拷纸锟斤拷锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//pvStr:		    锟斤拷锟斤拷锟斤拷锟斤拷欧锟斤拷锟斤拷锟较拷幕锟斤拷锟斤拷锟�
	//pulStrLen:	    锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷莩锟斤拷锟�
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosReadBinary(int  lDevint,
	char[]  BinarySID,
	char[]  Offset,
	char[]  ReadLen,
	char[]  pvStr)
	{
		char cmdbuf[] =new char[2048];;
		char rbuf[] =new char[2048];;
		int cmdlen = 5;
		int status = -1;
		int[] rlen = new int[1];
		//char rlen = 0;
		int st;
		char BinarySIDHex[]= new  char[5];;
		char OffsetHex[]= new  char[5];;
		char ReadLenHex[]= new  char[5];;
		
		
		pl_javacall._a_hex(BinarySIDHex, BinarySID,1);
		pl_javacall._a_hex(OffsetHex, Offset, 1);
		pl_javacall._a_hex(ReadLenHex, ReadLen, 1);
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0xb0;
		cmdbuf[2] = (char)(BinarySIDHex[0] + 0x80);
		cmdbuf[3] = OffsetHex[0];
		cmdbuf[4] = ReadLenHex[0];
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		if (status != 0)
		{
		return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
		st = (rbuf[0] << 8) + rbuf[1];
		return(st);
		}
		
		pl_javacall._hex_a(pvStr, rbuf, 2*(rlen[0]-2));
		return(CMD_OK);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//锟斤拷锟斤拷锟杰ｏ拷
	//Read Binary 锟斤拷锟斤拷锟斤拷锟斤拷锟侥硷拷
	//锟斤拷锟斤拷锟斤拷锟�
	//lDevint:	    锟斤拷锟斤拷锟斤拷锟斤拷璞革拷锟斤拷
	//Offset:         锟斤拷锟斤拷锟斤拷锟绞硷拷锟饺★拷锟捷碉拷偏锟狡碉拷址锟斤拷十锟斤拷锟斤拷锟斤拷址锟�锟街斤拷
	//ReadLen:        锟斤拷锟斤拷锟斤拷锟揭拷锟饺★拷锟斤拷纸锟斤拷锟绞拷锟斤拷锟斤拷锟街凤拷2锟街斤拷
	//pvStr:		    锟斤拷锟斤拷锟斤拷锟斤拷欧锟斤拷锟斤拷锟较拷幕锟斤拷锟斤拷锟�
	//锟斤拷锟截ｏ拷
	//锟缴癸拷锟斤拷锟斤拷0锟斤拷失锟杰凤拷锟截达拷锟斤拷锟�
	/////////////////////////////////////////////////////////////////////////////////////
	public int FWCosReadBinaryEx(int  lDevint,
	char[]  Offset,
	char[]  ReadLen,
	char[]  pvStr)
	{
		char cmdbuf[] =new char[2048];;
		char rbuf[] =new char[2048];;
		int cmdlen = 5;
		int status = -1;
		int[] rlen = new int[1];
		//char rlen = 0;
		int st;
		char OffsetHex[] =new char[128];;
		char ReadLenHex[]= new  char[5];;
		
		
		pl_javacall._a_hex(OffsetHex, Offset, 2);
		pl_javacall._a_hex(ReadLenHex, ReadLen, 1);
		
		cmdbuf[0] = 0x00;
		cmdbuf[1] = 0xb0;
		cmdbuf[2] = OffsetHex[0];
		cmdbuf[3] = OffsetHex[1];
		cmdbuf[4] = ReadLenHex[0];
		
		
		status = CPUAPDU_FUNC(lDevint,(char)cmdlen,cmdbuf,rlen,rbuf);
		
		
		if (status != 0)
		{
			return(ERR_APDU);
		}
		
		if (rbuf[rlen[0]-2] != 0x90 && rbuf[rlen[0]-2] != 0x61)
		{
			st = (rbuf[0] << 8) + rbuf[1];
			return(st);
		}
		
		pl_javacall._hex_a(pvStr, rbuf, 2*(rlen[0]-2));
		return(CMD_OK);
	}

}