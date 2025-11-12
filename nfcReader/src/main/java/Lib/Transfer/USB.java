package Lib.Transfer;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;//+2018.12.14
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import android.app.*;

public class USB {
//public class USB  extends Service{//+2018.12.14

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
	private static final boolean fDebug = false;//false;//
	public static final int MAX_USB_DEV_SUPPORT = 128;

	private  int m_vendorID = 1137;// s8/s3: 0x471(1137), E7:0x0483(1155)
	private  int m_productID = 41234;// s8/s3: 0xa112(41234),
										// E7:0x5750(22352)

	//private Activity hostAct;//-2018.12.14
	private Context hostAct;//+2018.12.14
	//private Context mContext1, mContext2;//+2018.12.14
	//public USBDeviceInfo[] usbDevices_mount = new USBDeviceInfo[MAX_USB_DEV_SUPPORT + 1];
	public USBDeviceInfo[] usbDevices_linked = new USBDeviceInfo[MAX_USB_DEV_SUPPORT + 1];
	private int USBDevicesMountedNum = 0;//The device number mount to this host
	private int USBDevicesLinkedNum = 0; //The device number linked by app

	private UsbDevice usbDevice =null;
	private UsbInterface usbInterface;
	private UsbDeviceConnection connection = null;
	private UsbEndpoint inEndpoint = null;
	private UsbEndpoint outEndpoint = null;
	
	//public SerialPort mSerialPort;
	private OutputStream m_OutputStream;
	private InputStream m_InputStream;
	//private SerialPort m_serial;
	private char m_portType;
	int size;
	int save_log = 0;
	
	private UsbManager usbManager;
	USBDeviceInfo[] usbDevices_mount = null;

	/*// -2018.12.14
	public USB(Activity hostAct)
	{
		super();
		this.hostAct = hostAct;
	}*/
	
	public USB(Context hostAct)//+2018.12.14
	{
		super();
		int i;
		
		this.hostAct = hostAct;
		
		for(i=0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			//usbDevices_mount[i] = new USBDeviceInfo();
			usbDevices_linked[i] = new USBDeviceInfo();
			usbDevices_linked[i].m_fCustomSoftwareLink = false;
			usbDevices_linked[i].usbHandle = -1;
		}
		
		
		usbDevices_mount = new USBDeviceInfo[MAX_USB_DEV_SUPPORT + 1];//+2020.6.29
		
		for(i=0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			usbDevices_mount[i] = new USBDeviceInfo();
		}
		ClearUSBDeviceInfo(usbDevices_mount);
		
	}
	
	public void ClearUSBDeviceInfo(USBDeviceInfo[] pInfo)
	{
		int i;
		
		for(i= 0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			//usbDevices_mount[i].usbHandle = -1;
			pInfo[i].usbHandle = -1;
		}
		
		USBDevicesMountedNum = 0;
	}
	
	public void setDevicePara(int vendorID, int productID)
	{
		m_vendorID = vendorID;
		m_productID = productID;
	}
	
	private int patchUSBDevice(USBDeviceInfo usbInfo)
	{
		int rel = -1;
		int i;
		
		for(i=0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			//if(usbDevices_linked[i].usbHandle != -1)
			{
				if( (usbInfo.usbDevPath.equals(usbDevices_linked[i].usbDevPath)) && usbInfo.usbDevPath != "")
				{
					rel = i;
					break;
				}
			}
		}
		
		return rel;
	}
	//Return: The handle if insert success, otherwise -1
	private int fillUSBDevice(USBDeviceInfo usbInfo)
	{
		int rel = -1;
		int i;
		
		for(i=0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			//if(usbDevices_linked[i].usbHandle == -1)
			if(usbDevices_linked[i].usbDevPath.equals(""))
			{
				//usbDevices_linked[i] = usbInfo;
				usbDevices_linked[i].usbDevPath = usbInfo.usbDevPath;
				usbDevices_linked[i].usbHandle = i+1;
				usbDevices_linked[i].usbDevice = usbInfo.usbDevice;
				usbDevices_linked[i].usbInterface = usbInfo.usbInterface;
				usbDevices_linked[i].connection = usbInfo.connection;
				usbDevices_linked[i].inEndpoint = usbInfo.inEndpoint;
				usbDevices_linked[i].outEndpoint = usbInfo.outEndpoint;
				usbDevices_linked[i].m_OutputStream = usbInfo.m_OutputStream;
				usbDevices_linked[i].m_InputStream = usbInfo.m_InputStream;
				
				rel = i + 1;
				break;
			}
		}
		return rel;
	}
	
	private int deleteUSBDevice(int handle)
	{
		int rel = 0;
		int i;
		
		if(USBDevicesMountedNum >0)USBDevicesMountedNum--;
	
		for(i=0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			if(usbDevices_linked[i].usbHandle == handle)
			{
				if (usbDevices_linked[i].connection != null) {
					usbDevices_linked[i].connection.close();
					
					if(true == fDebug)
						Log.i("readerlog", "device closed: "+handle);
				}
				else
				{
					if(true == fDebug)
						Log.i("readerlog", "Close a offline handle!!!");
				}
				usbDevices_linked[i].usbDevice = null;
				usbDevices_linked[i].usbInterface = null;
				usbDevices_linked[i].connection = null;
				usbDevices_linked[i].outEndpoint = null;
				usbDevices_linked[i].inEndpoint = null;
				usbDevices_linked[i].usbHandle = -1;
				usbDevices_linked[i].usbDevPath= "";
				usbDevices_linked[i].m_fCustomSoftwareLink = false;
				
				USBDevicesLinkedNum--;
				
				break;
			}
		}
		return rel;
	}
	//Find a not linked usb device mounted
	//Return: The handle of usb device, -1 if no found
	private int findUnusedUSBDevice()
	{
		int rel = -1;
		int i;
		
		for(i=0; i< MAX_USB_DEV_SUPPORT; i++)
		{
			if( (usbDevices_linked[i].m_fCustomSoftwareLink == false) && (usbDevices_linked[i].usbDevPath.equals("") == false) )
			{
				usbDevices_linked[i].m_fCustomSoftwareLink = true;
				rel = usbDevices_linked[i].usbHandle;
				break;
			}
		}
		return rel;
	}
	private int ResetDeviceOperating(int usbHandle)
	{
		int rel = -1;
		int i;
		int iHandle;
		
		if( (usbHandle == -1) || (usbHandle >MAX_USB_DEV_SUPPORT) )
			return rel;
		
		rel = usbHandle;
		iHandle = usbHandle -1;
		
		usbDevice = usbDevices_linked[iHandle].usbDevice;
		usbInterface = usbDevices_linked[iHandle].usbInterface;
		connection = usbDevices_linked[iHandle].connection;
		inEndpoint = usbDevices_linked[iHandle].inEndpoint;
		outEndpoint = usbDevices_linked[iHandle].outEndpoint;
		
		//public SerialPort mSerialPort;
		m_OutputStream = usbDevices_linked[iHandle].m_OutputStream;
		m_InputStream = usbDevices_linked[iHandle].m_InputStream;
		
		return rel;	
	}
	/**
	 * 打开usb
	 */
	public int usbOpen() {
		
		PendingIntent pendingIntent;
		IntentFilter filter;
		int RelHandle = -1;
		int i;
		int totalUSBDeviceNum = 0;
		int addrHandle = 0;
		
		if(hostAct == null)
			return -1;
		
		usbManager = (UsbManager) hostAct.getSystemService(Context.USB_SERVICE);
		
		if(usbManager == null)
			return -1;

		try{
			pendingIntent = PendingIntent.getBroadcast(hostAct, 0,
					new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
			filter = new IntentFilter(ACTION_USB_PERMISSION);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		
		try{
			hostAct.registerReceiver(mUsbReceiver, filter);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		
		try{
			hostAct.unregisterReceiver(mUsbReceiver);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	

		try {
			HashMap<String, UsbDevice> map = usbManager.getDeviceList();
			for (UsbDevice device : map.values()) {
				
				if(true == fDebug)
				{
					Log.i("readerlog",
						"vid:" + device.getVendorId() + "   pid:"
								+ device.getProductId() + "   "
								+ device.getDeviceName());
				}
				if (m_vendorID == device.getVendorId()
						&& m_productID == device.getProductId()) {
					
					if(device.getInterfaceCount() > 0)
					{
						if(totalUSBDeviceNum < MAX_USB_DEV_SUPPORT)
						{
							if (usbManager.hasPermission(device) == false) {
								usbManager.requestPermission(device, pendingIntent);
							}							
							
							usbDevices_mount[totalUSBDeviceNum].usbDevPath = device.getDeviceName();
							usbDevices_mount[totalUSBDeviceNum].usbDevice = device;
							usbDevices_mount[totalUSBDeviceNum].usbInterface = device.getInterface(0);
							usbDevices_mount[totalUSBDeviceNum].outEndpoint = device.getInterface(0).getEndpoint(1); // write by in endpoint
							usbDevices_mount[totalUSBDeviceNum].inEndpoint = device.getInterface(0).getEndpoint(0); // read from out endpoint
							
							addrHandle = patchUSBDevice(usbDevices_mount[totalUSBDeviceNum]);
							if(-1 == addrHandle)
							{//Not in the current linked array, it's a new node
															
								usbDevices_mount[totalUSBDeviceNum].connection = usbManager.openDevice(device);
								
								if(usbDevices_mount[totalUSBDeviceNum].connection == null)
								{
									if(true == fDebug)
										Log.i("readerlog","openDevice failed");
									break;
								}
								
								RelHandle = fillUSBDevice(usbDevices_mount[totalUSBDeviceNum]);
								usbDevices_mount[totalUSBDeviceNum].usbHandle = RelHandle;//+2020.6.29
								if(true == fDebug)
									Log.i("readerlog","Insert usb node,valid nodes=" + (totalUSBDeviceNum+1));
								
								
								totalUSBDeviceNum++;//test
								USBDevicesMountedNum++;
							
							}
							else
							{
								RelHandle = -1;
							}
							
							
							if(true == fDebug)
								Log.i("readerlog","match usb ok,valid nodes="+ USBDevicesMountedNum);
						}
						else
						{
							if(true == fDebug)
								Log.i("readerlog",">>>>>>>>>>>>>>> reader number achieve max! <<<<<<<<<<<<");
							break;
						}
					}
					else if(true == fDebug)
						Log.i("readerlog",">>>>>>>>>>>>>>> reader interface is 0 <<<<<<<<<<<<");
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		RelHandle = findUnusedUSBDevice();
		
		if((RelHandle != -1) && (USBDevicesLinkedNum < MAX_USB_DEV_SUPPORT))
			USBDevicesLinkedNum++;
		
		return RelHandle;//1;
	}

	
	public int usb_onceSend(int hdev, char[] bufData, int slen) {
		int i;
		int transferedLen;
		
		//if(connection1 == null || usbInterface1== null || outEndpoint1 == null)
		if(-1 == ResetDeviceOperating(hdev))
			return -1;
		
		if (connection != null) {
			
			try
			{
				if (connection.claimInterface(usbInterface, true)) {
					byte[] byte2 = new byte[slen ];
	
					for (i = 0; i < slen; i++)
						byte2[i] = (byte) bufData[i];
	
					transferedLen = connection.bulkTransfer(outEndpoint, byte2, slen, 3000);
					if(transferedLen < 0)
						return -1;
					if(true == fDebug)
					{
						for (Byte byte1 : byte2) {
							Log.d("readerlog", "send: " + Integer.toHexString(byte1 & 0xff));
						}
					}

	
				} else
				{
					if(true == fDebug)
					{
						Log.d("readerlog",
								"usb_onceSend err, claimInterface failed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return -1;
			}
		}
		else
		{
			if(true == fDebug)
			{
				Log.e("readerlog",
						"usb_onceSend err, connection is nullxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			}
		}


		return 0;
	}

	
	public int usb_onceReceive(int hdev, char[] bufData, int lenToRead) {

		byte[] bt = new byte[lenToRead+ 1];
		int i;
		
		if(-1 == ResetDeviceOperating(hdev))
			return -1;

		if (connection != null) {
			
			try
			{
				if (connection.claimInterface(usbInterface, true)) {
					int ret = connection.bulkTransfer(inEndpoint, bt, lenToRead,
							1000);//3000);
	
					if (ret != -1) {
						for (i = 0; i < ret; i++)
							bufData[i] = (char) (bt[i] & 0xff);
	
						if(true == fDebug)
						{
							for (Byte byte1 : bt) {
								Log.d("readerlog", "rev:" + Integer.toHexString(byte1 & 0xff));
							}
						}

					}
					else
					{
						return -1;//+2021.8.25
					}
				} else {
					if(true == fDebug)
					{
						Log.d("readerlog",
								"usb_onceReceive err, claimInterface failed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					}

					return -1;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return -1;
			}
		}
		else
		{
			if(true == fDebug)
			{
				Log.e("readerlog",
						"usb_onceReceive err, connection is nullxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			}
		}

		return 0;
	}
	
	public void usbClose(int handle)
	{
		int iHandle;
		
		if(handle > MAX_USB_DEV_SUPPORT)
			return;
		
		if(handle < 1)
			return;
		
		if(true == fDebug)
		{
			Log.d("readerlog",
					"usbClose:" +handle );
		}
		deleteUSBDevice(handle);
	}
	
	public class USBDeviceInfo {
		public int  usbHandle;
		public String usbDevPath;
		public UsbDevice usbDevice;
		public UsbInterface usbInterface;
		public UsbDeviceConnection connection;
		public UsbEndpoint inEndpoint;
		public UsbEndpoint outEndpoint;
		public OutputStream m_OutputStream;
		public InputStream m_InputStream;
		//public SerialPort m_serial;
		public boolean	m_fCustomSoftwareLink;
		
		public USBDeviceInfo()
		{
			usbHandle = -1;
			usbDevPath = "";
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//Log.e("action", action);

			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					usbDevice = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						
						//Toast.makeText(hostAct, "permission granded for device by user", Toast.LENGTH_SHORT).show();
						
						if (usbDevice != null) {
							//Granted success!! do something
							//Log.d("Test-Demo", "permission granded for device by user "+ usbDevice);
						}
					} else {
						//Toast.makeText(hostAct, "permission denied for device by user", Toast.LENGTH_SHORT).show();
						//Log.d("Test-Demo", "permission denied for device by user " + usbDevice);
					}
				}
			}
		}
	};
	

}
