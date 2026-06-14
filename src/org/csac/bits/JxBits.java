package org.csac.bits;



public class JxBits extends Object implements BitProcessor{

	final static int _nbits = 64; // size of block
	final static long _maxbits = _nbits * _nbits; // really this squared
	static long bah1;
	static JBits bacc1[] = new JBits[_nbits]; // accumulator for results
	
	// table of n bits per byte 0-255
	final static int bitCounts[] =   {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 
										1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
										1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
										2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
										1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
										2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
										2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
										3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
										1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
										2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
										2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
										3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
										2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
										3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
										3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
										4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8}; 


	boolean 			isInverted = false;
	long				itsHeader=0;
	JBits				itsBits[]=null; /* offset to Bits */
	
	public static int countBits(byte a) {
		return bitCounts[a & 255];
	}
	
	public static int countBits(short a) {
		return bitCounts[a & 255] + bitCounts[ (a >>> 8) & 255];
	}
	
	public static int countBits(int a) {
		return bitCounts[a & 255] + bitCounts[ (a >>>= 8) & 255]
				+ bitCounts[ (a >>>= 8) & 255] + bitCounts[ (a >>>= 8) & 255];
	}
	
	public static int countBits(long a) {
		return bitCounts[(int) (a & 255)] + bitCounts[ (int) (a >>>= 8) & 255]
				+ bitCounts[ (int) (a >>>= 8) & 255] + bitCounts[ (int)  (a >>>= 8) & 255]
				+ bitCounts[ (int) (a >>>= 8) & 255] + bitCounts[ (int)  (a >>>= 8) & 255]
				+ bitCounts[ (int) (a >>>= 8) & 255] + bitCounts[ (int)  (a >>>= 8) & 255];
	}

	public void clear() {
		itsBits = null;
		itsHeader = 0L;
		isInverted = false;
	}

	public void set(JBits[] a, long h, int l) {
		if (itsBits == null || itsBits.length < l) itsBits = new JBits [l];
		System.arraycopy(a,0,itsBits,0,l); // bacc1 is global to all of these
		itsHeader = h;
	}
	

	public JxBits and(JxBits andBits, JxBits destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int b1;
		int d1,d2, d3; // indexes into bits and accumulator

		if (destBits == null) destBits = new JxBits(); // maybe revise this.
		
		if ((itsHeader & andBits.itsHeader) != 0) { // they share some blocks in common
			h1 = itsHeader;
			h2 = andBits.itsHeader;
			JBits ax=null;
			// results stuff
			bah1 = 0L; // result header
			b1 = 0; // pointer into what
			// pointers into the various itsBits and accumulator
			d1 = 0;
			d2 = 0;
			d3 = 0;
			// cycle through the headers doing common blocks
			for(;(h1 & h2) != 0; h1>>>=1, h2>>>=1, theBit <<= 1) {
				if ((h1 & h2 & 1L) != 0) {
					if ((ax = itsBits[d1++].and(andBits.itsBits[d2++],null)).itsHeader != 0) {
						bah1 |= theBit; // result header
						bacc1[d3++] = ax;
						ax = null;
						b1++; // nblocks in accum header
					} 
				} else if ((h1 & 1L) != 0) d1++;
				else if ((h2 & 1L) != 0) d2++;
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JxBits();
				destBits.set(bacc1,bah1,b1);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JxBits and(JxBits andBits) 
	{
		return and(andBits, this);
	}

	public JxBits or(JxBits orBits, JxBits destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int d1,d2, d3; // indexes into bits and accumulator
		int b1;

		if (destBits == null) destBits = new JxBits();
				
		if ((itsHeader | orBits.itsHeader) != 0) { // some blocks has bits
			h1 = itsHeader;
			h2 = orBits.itsHeader;
			// results stuff
			bah1 = 0L; // result header
			b1 = 0; // pointer into what
			// pointers into the various itsBits and accumulator
			d1 = 0;
			d2 = 0;
			d3 = 0;
			JBits ax = null;
			// cycle through the headers doing common blocks
			for(;(h1 | h2) != 0; h1>>>=1, h2>>>=1, theBit <<= 1) {
				if ((h1 & h2 & 1L) != 0) {
					if ((ax = itsBits[d1++].or(orBits.itsBits[d2++],null)).itsHeader != 0) {
						bah1 |= theBit; // result header
						bacc1[d3++] = ax;
						ax = null;
						b1++; // nblocks in accum header
					} 
				} else if ((h1 & 1L) != 0) {
					bacc1[d3++] = itsBits[d1++].copy();
					b1++;
					bah1 |= theBit;
				}
				else if ((h2 & 1L) != 0) {
					bacc1[d3++] = orBits.itsBits[d2++].copy();
					b1++;
					bah1 |= theBit;
				}
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JxBits();
				if (b1 > itsBits.length) itsBits = null;
				destBits.set(bacc1,bah1,b1);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}
		
	public JxBits or(JxBits andBits) 
	{
		return or(andBits, this);
	}

	
	 public JxBits not() {
		long h1=itsHeader;
		int b1=0;
		
		isInverted = !getInverted();
		if (itsHeader != 0) { // some blocks have bits
			for(;(h1) != 0; h1>>>=1) {
				if ((h1 & 1L) != 0) {
					itsBits[b1].not(); // check was x.isInverted = isInverted
					b1++;
				}
			}
		}
		return this;
	}

	
	public boolean getInverted() {
		long h1=itsHeader;
		int b1=0;
		
		if (itsHeader != 0) { // some blocks have bits
			for(;(h1) != 0; h1>>>=1) {
				if ((h1 & 1L) != 0) {
					isInverted = itsBits[b1].isInverted;
					b1++;
				}
			}
		}
		return isInverted;
	}


	public void setInverted(boolean tv) {
		if (tv != getInverted() ) not();
	}
	

	public JxBits xor(JxBits xorBits, JxBits destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int d1,d2, d3; // indexes into bits and accumulator
		int b1;
		
		if (destBits == null) destBits = new JxBits();
		
		if ((itsHeader | xorBits.itsHeader) != 0) { // some blocks has bits
			h1 = itsHeader;
			h2 = xorBits.itsHeader;
			// results stuff
			bah1 = 0L; // result header
			b1 = 0; // counter for array depth of bits
			// pointers into the various itsBits and accumulator
			d1 = d2 = d3 = 0;
			JBits ax = null;
			// cycle through the headers doing all non-null blocks
			for(;(h1 | h2) != 0; h1>>>= 1, h2>>>= 1, theBit <<= 1) {
				if ((h1 & (h2 & 1L)) != 0) {
					if ((ax = itsBits[d1++].xor(xorBits.itsBits[d2++],null)).itsHeader != 0) {
						bah1 |= theBit; // result header
						bacc1[d3++] = ax;ax = null;b1++; // nblocks in accum header
					} 
				} else if ((h1 & 1L) != 0) {
					bacc1[d3++] = itsBits[d1++].copy();
					b1++; bah1 |= theBit;
				}
				else if ((h2 & 1L) != 0) {
					bacc1[d3++] = xorBits.itsBits[d2++].copy();
					b1++; bah1 |= theBit;
				}
			}
			if (d3 != 0) { // move following code to a method
				if (destBits == null) destBits = new JxBits();
				if (d3 > itsBits.length) itsBits = null;
				destBits.set(bacc1,bah1,d3);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JxBits xor(JxBits xorBits) 
	{
		return xor(xorBits, this);
	}

	public long count() 
	{
		long h1;
		int b1;
		long howmany=0;
		
		if (itsHeader != 0) { // some blocks has bits
			h1 = itsHeader;
			b1 = 0;
			for(;(h1) != 0; h1>>>=1) {
				if ((h1 & 1L) != 0) {
					howmany += itsBits[b1++].count();
				}
			}
		}
		return howmany;
	}
	
	public void bitIterator(BitProcessor handler) {
		long h1 = itsHeader;
		long bitpos = 0;
		int apos = 0;
		for (;h1 != 0;h1 >>>= 1,bitpos += _maxbits) { // what about isInverted
			if ((h1 & 1) != 0) {
				itsBits[apos++].bitIterator(handler,bitpos);
			} else {
				for(int i = 0 ; i < _maxbits; i++) handler.processBit(bitpos+i, false != getInverted());
			}
		}
	}
	
	public void bitIterator(BitProcessor handler, boolean tv) { // just pos bits if true
		if (tv == false) bitIterator(handler); // do all bits
		else { // do only positive bits
			long h1 = itsHeader;
			long bitpos = 0;
			int apos = 0;
			for (;h1 != 0;h1 >>>= 1,bitpos += _maxbits) {
				if ((h1 & 1) != 0) {
					long subbit=0;
					itsBits[apos++].bitIterator(handler,bitpos,true);
				} 
			}
		}
	}

	public void clearBit(long bitno) {
		setBit(bitno,false);
	}

	public void setBits(long bits[]) {
		for(int i=0;i<bits.length;i++) setBit(bits[i]);
	}
	
	public boolean isBitSetx(long bitno) { // remove 
		BitHelper b = new BitHelper(this);
		long lastBit = 0;
		while ((lastBit = b.nextBit()) != -1 && b.getBit() < bitno);
		return lastBit == bitno;
	}
	
	public boolean getBit(long bitno) {
		return isBitSet(bitno);
	}

	public boolean isBitSet(long bitno) {
		long bitpos = bitno % _maxbits;
		long blockno = bitno / _maxbits;
		long bhead = 1L << blockno;
		
		if ((bhead & itsHeader) == 0) return false == isInverted;
		else {
			int c = JxBits.countBits(itsHeader & (bhead - 1));
			return itsBits[c].isBitSet(bitpos);
		}
	}
	
	public void setBit(long bitno) {
		setBit(bitno,true);
	}

	public void clearBits(long bits[]) {
		for(int i=0;i<bits.length;i++) clearBit(bits[i]);
	}
	
	public void setBit(long bitno, boolean tv) {
		long bitpos = bitno % _maxbits;
		long blockno = bitno / _maxbits;
		long bhead = 1L << blockno;
		// long bpos = 1L << bitpos;
		
		if (getInverted()) tv = !tv;
		
		if (bitno > _nbits*_maxbits) return; // max for 64x64  Throw exception here later;

		if (itsHeader == 0) { // dont set if false
			if (tv) {
				itsHeader = bhead;
				itsBits = new JBits[1];
				itsBits[0] = new JBits();
				itsBits[0].setBit(bitpos,tv);
			}
		} else if ((itsHeader & bhead) != 0) {
			long head = itsHeader;
			int bp = 0;
			for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
				if ((head & 1L) != 0) bp++;
			}
			itsBits[bp].setBit(bitpos,tv);
			if (!tv) {
				if (itsBits[bp].itsHeader == 0) {
					for(int i=bp+1;i<itsBits.length;i++) itsBits[i-1] = itsBits[i];
					itsHeader &= ~(1L << blockno);
				}
			}
		} else if (tv) {
			int a = countBits(itsHeader);
			if (itsBits.length > a) {
				itsBits[a] = new JBits();
				if (bhead < 0) {
					itsBits[a].setBit(bitpos,tv);
				} else if (itsHeader > 0 && bhead > itsHeader) {
					itsBits[a].setBit(bitpos,tv);
				} else {
					long head = itsHeader;
					int bp = 0;
					int cnt=0;
					for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
						if ((head & 1L) != 0) {
							bp++;
							cnt++;
						}
					}
					System.arraycopy(itsBits,cnt,itsBits,cnt+1,a-cnt);
					itsBits[bp] = new JBits();
					itsBits[bp].setBit(bitpos,tv);
				}
			} else {
				JBits[] newBits = new JBits[a+1 + (a / 4)]; // add configure increment as bits proceed
				long head = itsHeader;
				int bp = 0;
				if (bhead < 0) {
					System.arraycopy(itsBits,0,newBits,0,a);
					newBits[a] = new JBits();
					newBits[a].setBit(bitpos,tv);
				}  else if (itsHeader > 0 && bhead > itsHeader) {
					System.arraycopy(itsBits,0,newBits,0,a);
					newBits[a] = new JBits();
					newBits[a].setBit(bitpos,tv);
				} else {
					for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
						if ((head & 1L) != 0) {
							newBits[bp] = itsBits[bp];
							bp++;
						}
					}
					newBits[bp] = new JBits();
					newBits[bp].setBit(bitpos,tv);
					for(;head != 0;head >>>= 1) {
						if ((head & 1L) != 0) {
							newBits[bp+1] = itsBits[bp];
							bp++;
						}
					}
				}
				itsBits = newBits;
			}
			itsHeader |= (1L << blockno);
		}	
	}

	public JxBits copy() {
		JxBits j = new JxBits();
		int a = countBits(itsHeader);
		j.itsBits = new JBits[a];
		for(int i=0;i<a;i++) j.itsBits[i] = itsBits[i].copy();
		j.itsHeader = itsHeader;
		return j;
	}
	
	public String toString() {
		printxBits s = new printxBits();
		s.initBits(this);
		bitIterator(s,true);
		return (String) s.concludeBits();
	}
	
	public String toBitString() {
		stringxBits s = new stringxBits();
		s.initBits(this);
		bitIterator(s,false);
		return (String) s.concludeBits();
	}
		
	public void initBits(Object a) {
		if (((JxBits) a).isInverted) System.out.println("Inverted:");

	}
			
	public void initBits(Object a[]) {
		if (((JxBits) a[0]).isInverted) System.out.println("Inverted:");

	}

	public Object concludeBits() {
		return null;
	}

	public void processBit(long bitno, boolean tv) {
		if (tv) System.out.println(bitno+"");
	}
	
	public void processBit(long bitno, boolean tv, Object o) {
	}
	
	public void preprocess(Object a) {
		
	}
	public void postprocess(Object a) {
		
	}


class printxBits implements BitProcessor {
	StringBuffer bitStr = null;

	public void initBits(Object a) {
		bitStr = new StringBuffer(100);
		if (((JxBits) a).isInverted) bitStr.append("JxBits - Inverted: \n");
	}

	public void initBits(Object a[]) {
		bitStr = new StringBuffer(100);
		if (((JxBits) a[0]).isInverted) bitStr.append("JxBits - Inverted: \n");
	}
	
	public Object concludeBits() {
		return bitStr.toString()+"\n";
	}

	public void processBit(long bitno, boolean tv) {
		if (tv) bitStr.append(bitno+" ");
	}
	
	public void processBit(long bitno, boolean tv, Object o) {
		
	}
	
	public void preprocess(Object a) {
		
	}
	public void postprocess(Object a) {
		
	}
}

class stringxBits implements BitProcessor {
	StringBuffer bitStr = null;
	
	public void initBits(Object a) {
		bitStr = new StringBuffer(100);
		if (((JxBits) a).isInverted) bitStr.append("Inverted: \n");
	}
	
	public void initBits(Object a[]) {
		bitStr = new StringBuffer(100);
		if (((JxBits) a[0]).isInverted) bitStr.append("Inverted: \n");
	}

	public Object concludeBits() {
		return bitStr.toString();
	}

	public void processBit(long bitno, boolean tv) {
		if (tv) bitStr.append("1");
		else bitStr.append("0");
	}
	
	public void processBit(long bitno, boolean tv, Object o) {
		
	}
	
	public void preprocess(Object a) {
		
	}
	public void postprocess(Object a) {
		
	}
}
	
	
}