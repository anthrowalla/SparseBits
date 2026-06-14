//
//  stringBitmap.java
//  Bitmap_Java
//
//  Created by Michael Fischer on Wed Jul 14 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


package org.csac.bits;

public class stringBitmap implements BitProcessor {
	StringBuffer bitStr = null;
	
	public void initBits(Object a[]) {
		bitStr = new StringBuffer(100);
		if (((JxBitmap) a[0]).isInverted) bitStr.append("Inverted: \n");
	}
	
	public void initBits(Object a) {
		bitStr = new StringBuffer(100);
		if (((JxBitmap) a).isInverted) bitStr.append("Inverted: \n");
	}
	
	public Object concludeBits() {
		return bitStr.toString();
	}
	
	public void postprocess(Object x) {
		bitStr.append(x.toString());
	}
	
	public void preprocess(Object a) {
		
	}
	
	public void processBit(long bitno, boolean tv) {
		if (tv) bitStr.append("1");
		else bitStr.append("0");
	}
	
	public void processBit(long bitno, boolean tv, Object o) {
		
	}
}
