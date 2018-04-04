package com.zkteco.biometric;

public class ZKFPService {
	static
	  {
	    try {
			System.loadLibrary("libzkfp");
			System.out.println("System.loadLibrary(libzkfp) return.System.getProperty(\"java.library.path\")="+ System.getProperty("java.library.path"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	  }
	  
	  public static native int Initialize();
	  
	  public static native int Finalize();
	  
	  public static native int GetDeviceCount();
	  
	  public static native long OpenDevice(int paramInt);
	  
	  public static native int CloseDevice(long paramLong);
	  
	  public static native int GetParameter(long paramLong, int paramInt, byte[] paramArrayOfByte, int[] paramArrayOfInt);
	  
	  public static native int SetParameter(long paramLong, int paramInt1, byte[] paramArrayOfByte, int paramInt2);
	  
	  public static native int GetCapParams(long paramLong, int[] paramArrayOfInt1, int[] paramArrayOfInt2);
	  
	  public static native int AcquireTemplate(long paramLong, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int[] paramArrayOfInt);
	  
	  public static native int GenRegFPTemplate(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, int[] paramArrayOfInt);
	  
	  public static native long DBInit();
	  
	  public static native int DBFree(long paramLong);
	  
	  public static native int DBAdd(int paramInt, byte[] paramArrayOfByte);
	  
	  public static native int DBDel(int paramInt);
	  
	  public static native int DBCount();
	  
	  public static native int DBClear();
	  
	  public static native int VerifyFPByID(int paramInt, byte[] paramArrayOfByte);
	  
	  public static native int MatchFP(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
	  
	  public static native int IdentifyFP(byte[] paramArrayOfByte, int[] paramArrayOfInt1, int[] paramArrayOfInt2);
	  
	  public static native int ExtractFromImage(String paramString, int paramInt, byte[] paramArrayOfByte, int[] paramArrayOfInt);
	  
	  public static native String BlobToBase64(byte[] paramArrayOfByte, int paramInt);
	  
	  public static native int Base64ToBlob(String paramString, byte[] paramArrayOfByte, int paramInt);
	  
	  public static native int AcquireImage(long paramLong, byte[] paramArrayOfByte);
	  
	  public static native int DBSetParameter(long paramLong, int paramInt1, int paramInt2);
	  
	  public static native int DBGetParameter(long paramLong, int paramInt, int[] paramArrayOfInt);
	  
	  public static native int GetTemplateQuality(long hDBCache,  byte[] fpTemplate);
	  public static native int GetTemplateQuality();
	  public static native int Terminate();
}
