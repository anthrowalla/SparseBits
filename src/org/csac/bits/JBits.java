package org.csac.bits;


// import org.csac.bits.Allocater;
//import org.csac.bits.BitProcessor;

public class JBits extends Object implements BitProcessor{

	final static int _nbits = 64; // size of block
	final static long _maxbits = _nbits * _nbits - 1;
	static long bah1, bacc1[] = new long[_nbits];
	
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


	
	boolean isInverted=false;

	long				itsHeader=0;
	long				itsBits[]=null; /* offset to Bits */
	
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

	public void set(long[] a, long h, int l) {
		if (itsBits == null) itsBits = new long [l];
		System.arraycopy(a,0,itsBits,0,l); // bacc1 is global to all of these
		itsHeader = h;
	}
	
	public JBits iand(JBits a, JBits ret) {
		if (isInverted || a.isInverted) {
			if (isInverted && a.isInverted) {
				isInverted = false;
				a.isInverted = false;
				or(a,ret);
				isInverted = true;
				a.isInverted = true;
				ret.isInverted = true;
			} else if (isInverted) {
				isInverted = false;
				and(a,ret);
				ret.xor(a);
				if (ret != this) isInverted = true;
			} else {
				a.isInverted = false;
				and(a,ret);
				ret.xor(this);
				if (a != ret) a.isInverted = true;
			}
		} else return and(a, ret);
		return ret;
	}
	
	
	public JBits ior(JBits a, JBits ret) {
		if (isInverted || a.isInverted){
			if (isInverted && a.isInverted) {
				isInverted = false;
				a.isInverted = false;
				and(a,ret);
				isInverted = true;
				a.isInverted = true;
				ret.isInverted = true;
			} else if (isInverted) {
				isInverted = false;
				if (ret == this) {
					JBits q = copy();
					and(a,ret);
					ret.xor(q);
				} else {
					and(a,ret);
					ret.xor(this);
				}
				isInverted = true;
				ret.isInverted = true;
			} else {
				a.isInverted = false;
				if (ret == a) {
					JBits q = a.copy();
					and(a,ret);
					ret.xor(q);
				} else {
					and(a,ret);
					ret.xor(a);
				}
				a.isInverted = true;
				ret.isInverted = true;
			}
		} else return or(a, ret);
		return ret;
	}
	
	
	public JBits ixor(JBits a, JBits ret) {
		if (isInverted || a.isInverted){
			if (isInverted && a.isInverted) {
				isInverted = false;
				a.isInverted = false;
				xor(a,ret);
				if (ret != this) isInverted = true;
				if (ret != a) a.isInverted = true;
			} else if (isInverted) {
				isInverted = false;
				JBits r = null;
				JBits p = null;
				r = and(a,null); // store in r
				p = xor(r,null);
				r.xor(a,ret);
				ret.or(p);
				isInverted = true;
				ret.isInverted = true;
			} else {
				a.isInverted = false;
				JBits r = null;
				JBits p = null;
				r = and(a,null); // store in r
				p = xor(r,null);
				r.xor(a,ret);
				ret.or(p);
				a.isInverted = true;
				ret.isInverted = true;
			}
		} else return xor(a, ret);
		return ret;
	}
	

	public JBits and(JBits andBits, JBits destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int b1;
		int d1,d2, d3; // indexes into bits and accumulator

		if (destBits == null) destBits = new JBits(); // maybe revise this.
		
		if (isInverted || andBits.isInverted) return iand(andBits,destBits);
		
		if ((itsHeader & andBits.itsHeader) != 0) { // they share some blocks in common
			h1 = itsHeader;
			h2 = andBits.itsHeader;
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
					if ((bacc1[d3] = itsBits[d1++] & andBits.itsBits[d2++]) != 0) {
						bah1 |= theBit; // result header
						d3++; // increment result .. already in accumulator
						b1++; // nblocks in accum header
					} 
					//d1++;
					//d2++;
				} else if ((h1 & 1L) != 0) d1++;
				else if ((h2 & 1L) != 0) d2++;
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JBits();
				destBits.set(bacc1,bah1,b1);
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JBits and(JBits andBits) 
	{
		return and(andBits, this);
	}

	public JBits or(JBits orBits, JBits destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int d1,d2, d3; // indexes into bits and accumulator
		int b1;

		if (destBits == null) destBits = new JBits();
				
		if (isInverted) return ior(orBits, destBits);
		
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
			// cycle through the headers doing common blocks
			for(;(h1 | h2) != 0; h1>>>=1, h2>>>=1, theBit <<= 1) {
				if ((h1 & h2 & 1L) != 0) {
					if ((bacc1[d3] = (itsBits[d1++] | orBits.itsBits[d2++])) != 0) {
						bah1 |= theBit; // result header
						d3++; // increment result .. already in accumulator
						b1++; // nblocks in accum header
					} 
				} else if ((h1 & 1L) != 0) {
					bacc1[d3++] = itsBits[d1++];
					b1++;
					bah1 |= theBit;
				}
				else if ((h2 & 1L) != 0) {
					bacc1[d3++] = orBits.itsBits[d2++];
					b1++;
					bah1 |= theBit;
				}
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JBits();
				if (b1 > itsBits.length) itsBits = null;
				destBits.set(bacc1,bah1,b1);
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JBits not() {
		isInverted = !isInverted;
		return this;
	}

	public JBits or(JBits andBits) 
	{
		return or(andBits, this);
	}

	public JBits xor(JBits xorBits, JBits destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int d1,d2, d3; // indexes into bits and accumulator
		int b1;
		
		if (destBits == null) destBits = new JBits();
		
		if (isInverted) return ixor(xorBits, destBits);
		
		if ((itsHeader | xorBits.itsHeader) != 0) { // some blocks has bits
			h1 = itsHeader;
			h2 = xorBits.itsHeader;
			// results stuff
			bah1 = 0L; // result header
			b1 = 0; // pointer into what
			// pointers into the various itsBits and accumulator
			d1 = 0;
			d2 = 0;
			d3 = 0;
			// cycle through the headers doing common blocks
			for(;(h1 | h2) != 0; h1>>>=1, h2>>>=1, theBit <<= 1) {
				if ((h1 & h2 & 1L) != 0) {
					if ((bacc1[d3] = itsBits[d1++] ^ xorBits.itsBits[d2++]) != 0) {
						bah1 |= theBit; // result header
						d3++; // increment result .. already in accumulator
						b1++; // nblocks in accum header
					} 
					//d1++;
					//d2++;
				} else if ((h1 & 1L) != 0) {
					bacc1[d3++] = itsBits[d1++];
					b1++;
					bah1 |= theBit;
				}
				else if ((h2 & 1L) != 0) {
					bacc1[d3++] = xorBits.itsBits[d2++];
					b1++;
					bah1 |= theBit;
				}
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JBits();
				if (b1 > itsBits.length) itsBits = null;
				destBits.set(bacc1,bah1,b1);
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JBits xor(JBits xorBits) 
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
					howmany += countBits(itsBits[b1++]);
				}
			}
		}
		return howmany;
	}
	
	public void bitIterator(BitProcessor handler, long offset) {
		long h1 = itsHeader;
		long bitpos = 0;
		int apos = 0;
		for (;h1 != 0;h1 >>>= 1,bitpos+=_nbits) { // what about isInverted
			if ((h1 & 1) != 0) {
				long data = itsBits[apos++];
				for(int i=0;i<_nbits;data >>>= 1,i++) {
					handler.processBit(bitpos+i,((data & 1) != 0) != isInverted); // better way to handle inverted printout
				}
			} else {
				for(int i = 0 ; i < _nbits; i++) handler.processBit(offset+bitpos+i,false != isInverted);
			}
		}
	}
	
	public void bitIterator(BitProcessor handler, long offset, boolean tv) { // just pos bits if true
		if (tv == false) bitIterator(handler,offset); // do all bits
		else { // do only positive bits
			long h1 = itsHeader;
			long bitpos = 0;
			int apos = 0;
			for (;h1 != 0;h1 >>>= 1,bitpos+=_nbits) {
				if ((h1 & 1) != 0) {
					long subbit=0;
					long data = itsBits[apos++];
					for(;data != 0;data >>>= 1, subbit++) {
						if ((data & 1) != 0) handler.processBit(offset+bitpos+subbit,true);
					}
				} 
			}
		}
	}

	public void setBits(long bits[]) {
		for(int i=0;i<bits.length;i++) setBit(bits[i]);
	}
	
	public void setBit(long bitno) {
		setBit(bitno,true);
	}

	public void clearBit(long bitno) {
		setBit(bitno,false);
	}

	public boolean isBitSet(long bitno) {
		long bitpos = bitno % _nbits;
		long blockno = bitno / _nbits;
		long bhead = 1L << blockno;
		
		if ((bhead & itsHeader) == 0) return false == isInverted;
		else {
			int c = JxBits.countBits(itsHeader & (bhead - 1));
			return ((itsBits[c] & bitpos) != 0) == isInverted;
		}
	}

	public void setBit(long bitno, boolean tv) {
		long bitpos = bitno % _nbits;
		long blockno = bitno / _nbits;
		long bhead = 1L << blockno;
		long bpos = 1L << bitpos;
		
		if (isInverted) tv = !tv;
		
		if (bitno > _maxbits) return; // max for 64x64  Throw exception here later;
		if (itsHeader == 0) {
			if (tv) {
				itsHeader = bhead;
				itsBits = new long[1];
				itsBits[0] = bpos;
			}
		} else if ((itsHeader & bhead) != 0) {
			long head = itsHeader;
			int bp = 0;
			for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
				if ((head & 1L) != 0) bp++;
			}
			if (tv) itsBits[bp] |= bpos;
			else {
				itsBits[bp] &= ~bpos;
				if (itsBits[bp] == 0) {
					for(int i=bp+1;i<itsBits.length;i++) itsBits[i-1] = itsBits[i];
					itsHeader &= ~(1L << blockno);
				}
			}
		} else if (tv) {
			int a = countBits(itsHeader);
			if (itsBits.length > a) {
				if (bhead < 0) {
					itsBits[a] = bpos;
				} else if (itsHeader > 0 && bhead > itsHeader)
					itsBits[a] = bpos;
				else {
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
					itsBits[bp] = bpos;
				}
			} else {
				long[] newBits = new long[a+1 + (a / 4)]; // add configure increment as bits proceed
				long head = itsHeader;
				int bp = 0;
				if (bhead < 0) {
					System.arraycopy(itsBits,0,newBits,0,a);
					newBits[a] = bpos;
				}  else if (itsHeader > 0 && bhead > itsHeader) {
					System.arraycopy(itsBits,0,newBits,0,a);
					newBits[a] = bpos;
				} else {
					for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
						if ((head & 1L) != 0) {
							newBits[bp] = itsBits[bp];
							bp++;
						}
					}
					newBits[bp] = bpos;
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

	public JBits copy() {
		JBits j = new JBits();
		int a = countBits(itsHeader);
		j.itsBits = new long[a];
		System.arraycopy(itsBits,0,j.itsBits,0,a);
		j.itsHeader = itsHeader;
		return j;
	}
	
	public String toString() {
		printBits s = new printBits();
		s.initBits(this);
		bitIterator(s,0,true);
		return (String) s.concludeBits();
	}
	
	public String toBitString() {
		stringBits s = new stringBits();
		s.initBits(this);
		bitIterator(s, 0, false);
		return (String) s.concludeBits();
	}
		
	public void initBits(Object a) {
		if (((JBits) a).isInverted) System.out.println("Inverted:");

	}
		
	public void initBits(Object a[]) {
		if (((JBits) a[0]).isInverted) System.out.println("Inverted:");

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
}

class printBits implements BitProcessor {
	StringBuffer bitStr = null;

	public void initBits(Object a) {
		bitStr = new StringBuffer(100);
		if (((JBits) a).isInverted) bitStr.append("JBits:Inverted: \n");
	}
	
	public void initBits(Object a[]) {
		bitStr = new StringBuffer(100);
		if (((JBits) a[0]).isInverted) bitStr.append("JBits:Inverted: \n");
	}

	public Object concludeBits() {
		return bitStr.toString();
	}

	public void processBit(long bitno, boolean tv) {
		if (tv) bitStr.append(bitno+"\n");
	}
	
	public void processBit(long bitno, boolean tv, Object o) {
		
	}
	
	public void preprocess(Object a) {
		if (((JBits) a).isInverted) bitStr.append("JBits:Inverted: \n");
	}
	
	public void postprocess(Object a) {
		
	}
}

class stringBits implements BitProcessor {
	StringBuffer bitStr = null;
	
	public void initBits(Object a) {
		bitStr = new StringBuffer(100);
		if (((JBits) a).isInverted) bitStr.append("Inverted: \n");
	}
	
	public void initBits(Object a[]) {
		bitStr = new StringBuffer(100);
		if (((JBits) a[0]).isInverted) bitStr.append("Inverted: \n");
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