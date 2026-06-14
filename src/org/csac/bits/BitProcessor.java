package org.csac.bits;

// An interface for objects that process the bits in an iterated loop
// 

public interface BitProcessor {
	
	public void initBits(Object a[]);
	
	public void initBits(Object a);

	public Object concludeBits();	
	
	public void preprocess(Object a);
	
	public void postprocess(Object x);	
	
	public void processBit(long bitno, boolean tv);
		
	public void processBit(long bitno, boolean tv, Object o);
}