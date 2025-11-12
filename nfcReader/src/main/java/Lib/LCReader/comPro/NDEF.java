package Lib.LCReader.comPro;

public class  NDEF{
	
	
//	int _readNFCData(int icdev, int tagType, int textFormat, char[] szPwd, int[] pPayloadType, char[] pContent)
//	{
//		unsigned char* ndef_data = NULL;
//		unsigned long ndef_len;
//		Ndef_message ndef_msg ;
//		int st;
//		int recordNum = 0;
//		int i;
//		
//
//		switch(tagType)
//		{
//		case NFC_TYPE_ULT:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_ULT);
//			break;
//		case NFC_TYPE_ULTC:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_ULTC);
//			break;
//		case NFC_TYPE_MFCLS_1K:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_MFCLS_1K);
//			break;
//		case NFC_TYPE_MFCLS_4K:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_MFCLS_4K);
//			break;
//		case NFC_TYPE_NTAG203:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_NTAG203);
//			break;
//		case NFC_TYPE_NTAG210:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_NTAG210);
//			break;
//		case NFC_TYPE_NTAG212:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_NTAG212);
//			break;
//		case NFC_TYPE_NTAG213:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_NTAG213);
//			break;
//		case NFC_TYPE_NTAG215:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_NTAG215);
//			break;
//		case NFC_TYPE_NTAG216:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_NTAG216);
//			break;
//		case NFC_TYPE_ICODE_SLI:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_ICODE_SLI);
//			break;
//		case NFC_TYPE_ICODE_SLI_S:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_ICODE_SLI_S);
//			break;
//		case NFC_TYPE_ICODE_SLI_L:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_ICODE_SLI_L);
//			break;
//		case NFC_TYPE_ICODE_DNA:
//			ndef_data = (unsigned char*)malloc(sizeof(unsigned char)* NFC_MAX_USER_MEM_ICODE_DNA);
//			break;
//		}
//
//		if(ndef_data == NULL)
//			return FW_EMPTY;
//
//
//		st = _ReadCardAsNFCMode(icdev, tagType, szPwd, ndef_data, &ndef_len);
//		if(st)
//			goto _READNFCDATA_FREE_NDEF; 
//
//		#if defined(LIB_DEBUG) && defined(__ANDROID__)
//		LOGD("_ReadCardAsNFCMode ok \n");
//		#endif
//
//		//get the number of records in this buffer
//		recordNum = _getRecordNumFromNDEFData(ndef_data, ndef_len);
//
//		#if defined(LIB_DEBUG) && defined(__ANDROID__)
//		LOGD("_getRecordNumFromNDEFData return:%d, malloc:%d\n",recordNum, sizeof(Ndef_record) * recordNum);
//		#endif
//
//		ndef_msg.pRec = (Ndef_record*)malloc(sizeof(Ndef_record) * recordNum);
//		if(ndef_msg.pRec == NULL)
//		{
//			st = FW_EMPTY;
//			goto _READNFCDATA_FREE_NDEF;
//		}
//
//		#if defined(LIB_DEBUG) && defined(__ANDROID__)
//		LOGD("ndef_msg.pRec malloc ok\n");
//		#endif
//
//		ndef_msg.arrNum = recordNum;
//
//		//-2021.6.1 ++
//		for(i =0; i< recordNum; i++)
//		{
//			//ndef_msg.pRec[i].bufIdentifier = NULL;
//			ndef_msg.pRec[i].bufPayLoad = NULL;
//		}
//		//-2021.6.1 ---
//
//		////+2021.6.3 ++
//		//for(i = 0; i< recordNum; i++)
//		//{
//
//		//}
//		////+2021.6.3 --
//
//		//ndef_data start with Byte0
//		_disassembleNDEFData(ndef_data, ndef_len, &ndef_msg);
//
//		#if defined(LIB_DEBUG) && defined(__ANDROID__)
//		LOGD("_disassembleNDEFData ok\n");
//		#endif
//
//		//get the content in the ndef
//		_generUserMessage(&ndef_msg, pPayloadType, pContent, textFormat);//There are bugs in this funciton!!!!
//
//		#if defined(LIB_DEBUG) && defined(__ANDROID__)
//		LOGD("_generUserMessage ok\n");
//		#endif
//
//		free(ndef_msg.pRec);
//
//	_READNFCDATA_FREE_NDEF:
//		free(ndef_data);
//
//		return st;
//
//	}

}

