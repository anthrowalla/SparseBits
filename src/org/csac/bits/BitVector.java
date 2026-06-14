package org.csac.bits;

import java.util.Vector;

public class BitVector extends Vector implements Cloneable, java.io.Serializable {
	
	BitVector() {
		super();
	}
	
	BitVector(int size) {
		super(size);
	}
	
	BitVector(int size,int inc) {
		super(size,inc);
	}

	public JxBits getBits(int n) {
		return (JxBits) elementAt(n);
	}
}
	
	