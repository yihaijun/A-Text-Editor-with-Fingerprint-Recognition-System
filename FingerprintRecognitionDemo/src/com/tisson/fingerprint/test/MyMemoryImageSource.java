package com.tisson.fingerprint.test;

import java.awt.image.MemoryImageSource;

public class MyMemoryImageSource extends MemoryImageSource{
	public int w; 
	public int h; 
	public int pix[];
	public int off;
	public int scan;
	public byte data[];
	public MyMemoryImageSource(int w, int h, int pix[], int off, int scan , byte data[]){
		super(w, h, pix, off, scan);
		this.w = w;
		this.h = h;
		this.pix = pix;
		this.off = off;
		this.scan = scan;
		this.data = data;
	}
}
