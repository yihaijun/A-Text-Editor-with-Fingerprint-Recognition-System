package com.tisson.fingerprint.test;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface ZKFPServiceInf extends Library {
	ZKFPServiceInf INSTANCE = (ZKFPServiceInf)Native.loadLibrary(("libzkfp.dll"),	ZKFPServiceInf.class);

    int Initialize();
    int DBIdentify(long hDBCache,  byte[] fpTemplate, int cbTemplate,int[] FID,  int[] score);
    int GetDBCacheCount(long hDBCache, int[] fpCount);	//same as ZKFPM_GetDBCacheCount, for new version
    int DBCount(long hDBCache,int[] fpCount);
    int GetTemplateQuality(long hDBCache);
    int DBCount();
}
