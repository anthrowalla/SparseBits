package org.csac.bits;


public class JBitmap implements BitProcessor {
	
	static JxBits bacc1[] = new JxBits[64];
	static long bah1 = 0;
	
	long itsHeader = 0;
	JxBits[] itsBits=null; //n x 64
	boolean isInverted=false;
	
	public void clear() {
		itsBits = null;
		itsHeader = 0;
		isInverted = false;
	}

	public void set(JxBits[] a, long h, int l) {
		if (itsBits == null || itsBits.length < l) itsBits = new JxBits [l];
		
		System.arraycopy(a,0,itsBits,0,l); // bacc1 is global to all of these
		itsHeader = h;
	}
	
	static final int AND = 0;
	static final int OR = 1;
	static final int XOR = 2;
	

	public JxBits collapse(int PRED) {
		long h1;
		int d1; // indexes into bits and accumulator
		JxBits ax=null;	
		if ((h1 = itsHeader) != 0) { // some blocks  
			
			// results stuff
			// pointers into the various itsBits and accumulator
			d1 = 0;
			// cycle through the headers doing common blocks
			for(;h1 != 0; h1>>>=1) {
				if ((h1 & 1L) != 0) {
					if (ax == null) {
						 ax = itsBits[d1++];
						 if (PRED == AND && ax.itsHeader == 0) break;
					} else switch (PRED) {
						case AND : ax.and(itsBits[d1++]);
									if (ax.itsHeader == 0) break; // no possible result
									break;
						case OR : ax.or(itsBits[d1++]);
									break;
						case XOR : ax.xor(itsBits[d1++]);
									break;
					}
				}
			}
		}
		return ax;
	}

	public JxBits getBits(long bitno) {
		long hbit = 1 << bitno;
		if ((itsHeader & hbit) != 0) {
			int c = JxBits.countBits(itsHeader & (hbit - 1));
			return itsBits[c];
		} else return new JxBits();
	}

	public JBitmap reduce(JxBits andBits, JBitmap destBits, int PRED) {
		long h1;
		long theBit=1L;
		int b1;
		int d1,d3; // indexes into bits and accumulator
		long bh = 0;
		
		if (destBits == null) destBits = new JBitmap(); // maybe revise this.
		
		if ((h1 = itsHeader) != 0) { // some blocks  
			JxBits ax=null;
			// results stuff
			bah1 = 0L; // result header
			b1 = 0; // pointer into what
			// pointers into the various itsBits and accumulator
			d1 = 0;
			d3 = 0;
			// cycle through the headers doing common blocks
			for(;h1 != 0; h1>>>=1, theBit <<= 1) {
				if ((h1 & 1L) != 0) {
					switch (PRED) {
						case AND : ax = itsBits[d1++].and(andBits,null);
									break;
						case OR : ax = itsBits[d1++].or(andBits,null);
									break;
						case XOR : ax = itsBits[d1++].xor(andBits,null);
									break;
					}
					if (ax.itsHeader != 0) {
						bah1 |= theBit; // result header
						bacc1[d3++] = ax;
						ax = null;
						b1++; // nblocks in accum header
					} 
				}
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JBitmap();
				destBits.set(bacc1,bah1,b1);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	
	public JBitmap and(JxBits andBits, JBitmap destBits) {
		return reduce(andBits, destBits, AND);
	}
	
	public JBitmap or(JxBits andBits, JBitmap destBits) { // just the set rows, not the zero rows... may adapt later
		return reduce(andBits, destBits, OR);
	}

	
	public JBitmap xor(JxBits andBits, JBitmap destBits) { // just the set rows, not the zero rows... may adapt later
		return reduce(andBits, destBits, XOR);
	}

	
	public JBitmap and(JBitmap andBits, JBitmap destBits) {
		long h1, h2;
		long theBit=1L;
		int b1;
		int d1,d2, d3; // indexes into bits and accumulator
		long bh = 0;
		
		if (destBits == null) destBits = new JBitmap(); // maybe revise this.
		
		if ((bh = itsHeader & andBits.itsHeader) != 0) { // they share some blocks in common
			h1 = bh;
			h2 = andBits.itsHeader;
			JxBits ax=null;
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
				if (destBits == null) destBits = new JBitmap();
				destBits.set(bacc1,bah1,b1);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JBitmap and(JBitmap andBits) 
	{
		return and(andBits, this);
	}

	
	public JBitmap or(JBitmap orBits, JBitmap destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int d1,d2, d3; // indexes into bits and accumulator
		int b1;

		if (destBits == null) destBits = new JBitmap();
				
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
			JxBits ax = null;
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
				if (destBits == null) destBits = new JBitmap();
				destBits.set(bacc1,bah1,b1);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}
		
	public JBitmap or(JBitmap orBits) 
	{
		return or(orBits, this);
	}


	public JBitmap xor(JBitmap xorBits, JBitmap destBits)  // null = new
	{
		long h1, h2,  theBit=1L;
		int d1,d2, d3; // indexes into bits and accumulator
		int b1;
		
		if (destBits == null) destBits = new JBitmap();
		
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
			JxBits ax = null;
			// cycle through the headers doing common blocks
			for(;(h1 | h2) != 0; h1>>>=1, h2>>>=1, theBit <<= 1) {
				if ((h1 & h2 & 1L) != 0) {
					if ((ax = itsBits[d1++].xor(xorBits.itsBits[d2++],null)).itsHeader != 0) {
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
					bacc1[d3++] = xorBits.itsBits[d2++].copy();
					b1++;
					bah1 |= theBit;
				}
			}
			if (b1 != 0) { // move following code to a method
				if (destBits == null) destBits = new JBitmap();
				destBits.set(bacc1,bah1,b1);
				destBits.isInverted = destBits.itsBits[0].isInverted;
			} else if (destBits != null) destBits.clear();
		} else if (destBits != null) destBits.clear();
		return destBits;
	}

	public JBitmap xor(JBitmap xorBits) 
	{
		return xor(xorBits, this);
	}


	public JBitmap copy() {
		JBitmap j = new JBitmap();
		int a = JxBits.countBits(itsHeader);
		j.itsBits = new JxBits[a];
		for(int i=0;i<a;i++) j.itsBits[i] = itsBits[i].copy();
		j.itsHeader = itsHeader;
		return j;
	}

	public JBitmap not() {
		long h1=itsHeader;
		int b1=0;
		
		isInverted = !getInverted();
		if (itsHeader != 0) { // some blocks have bits
			for(;(h1) != 0; h1>>>=1) {
				if ((h1 & 1L) != 0) {
				// was	itsBits[b1].setInverted(isInverted);
					itsBits[b1].not();
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
			isInverted = itsBits[0].getInverted();
		} else isInverted = false;
		
		return isInverted;
	}


	public void setInverted(boolean tv) {
		if (tv != getInverted() ) not();
	}

	public void setBits(long bits[]) {
		for(int i=1;i<bits.length;i++) setBit(bits[0],bits[i]);
	}
	
	
	public void setBits(long bitno, JxBits j) {
		long hbit = 1 << bitno;
		if ((itsHeader & hbit) != 0) {
			int c = JxBits.countBits(itsHeader & (hbit - 1));
			itsBits[c] = j;
		} else {
			setBit(bitno,1); // sets up a row for this one
			setBits(bitno,j);
		}
	}

	public void setBit(long bitnoy,long bitnox) {
		setBit(bitnoy,bitnox,true);
	}


	public void setBit(long bitnoy, long bitnox, boolean tv) {
		long bitpos = bitnoy; //  % JxBits._maxbits;
		long blockno = bitnoy; // just 64 of they
		long bhead = 1L << blockno; 
		// long bpos = 1L << bitpos;
		
		if (getInverted()) tv = !tv;
		
		if (bitnox > JxBits._nbits*JxBits._maxbits) return; // max for 64x4096  Throw exception here later;
		if (bitnoy > JxBits._nbits) return; // too big
		if (itsHeader == 0) { // dont set if false
			if (tv) {
				itsHeader = bhead;
				itsBits = new JxBits[1];
				itsBits[0] = new JxBits();
				itsBits[0].setBit(bitnox,tv);
			}
		} else if ((itsHeader & bhead) != 0) {
			long head = itsHeader;
			int bp = 0;
			for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
				if ((head & 1L) != 0) bp++;
			}
			itsBits[bp].setBit(bitnox,tv);
			if (!tv) {
				if (itsBits[bp].itsHeader == 0) {
					for(int i=bp+1;i<itsBits.length;i++) itsBits[i-1] = itsBits[i];
					itsHeader &= ~(1L << blockno);
				}
			}
		} else if (tv) {
			int a = JxBits.countBits(itsHeader);
			if (itsBits.length > a) {
				itsBits[a] = new JxBits();
				if (bhead < 0) {
					itsBits[a].setBit(bitnox,tv);
				} else if (itsHeader > 0 && bhead > itsHeader) {
					itsBits[a].setBit(bitnox,tv);
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
					itsBits[bp] = new JxBits();
					itsBits[bp].setBit(bitnox,tv);
				}
			} else {
				JxBits[] newBits = new JxBits[a+1 + (a / 4)]; // add configure increment as bits proceed
				long head = itsHeader;
				int bp = 0;
				if (bhead < 0) {
					System.arraycopy(itsBits,0,newBits,0,a);
					newBits[a] = new JxBits();
					newBits[a].setBit(bitnox,tv);
				}  else if (itsHeader > 0 && bhead > itsHeader) {
					System.arraycopy(itsBits,0,newBits,0,a);
					newBits[a] = new JxBits();
					newBits[a].setBit(bitnox,tv);
				} else {
					for(;(bhead & 1L) == 0;bhead >>>= 1, head >>>= 1) {
						if ((head & 1L) != 0) {
							newBits[bp] = itsBits[bp];
							bp++;
						}
					}
					newBits[bp] = new JxBits();
					newBits[bp].setBit(bitnox,tv);
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
		
	public void bitIterator(BitProcessor handler,long bitpos) {
		long h1 = itsHeader;
		int apos = 0;
		
		for (;h1 != 0;h1 >>>= 1,bitpos += 1) {
			if ((h1 & 1) != 0) {
				handler.preprocess(new Long(bitpos));
				itsBits[apos++].bitIterator(handler);
				handler.postprocess("\n");
			} 
		}
	}
	
	public void bitIterator(BitProcessor handler, long bitpos, boolean tv) { // just pos bits if true
		if (tv == false) bitIterator(handler, bitpos); // do all bits
		else { // do only positive bits
			long h1 = itsHeader;
			int apos = 0;
			for (;h1 != 0;h1 >>>= 1,bitpos += 1) {
				if ((h1 & 1) != 0) {
					handler.preprocess(new Long(bitpos));
					itsBits[apos++].bitIterator(handler,true);
					handler.postprocess("\n");
				} 
			}
		}
	}

	
	public String toString(long offset) {
		printBitmap s = new printBitmap();
		s.initBits(this);
		bitIterator(s,offset,true);
		return (String) s.concludeBits();
	}
	
	public String toBitString() {
		stringBitmap s = new stringBitmap();
		s.initBits(this);
		bitIterator(s,0,false);
		return (String) s.concludeBits();
	}
		
	public void initBits(Object a) {
		if (((JBitmap) a).isInverted) System.out.println("Inverted:");

	}
	
	public void initBits(Object a[]) {
		if (((JBitmap) a[0]).isInverted) System.out.println("Inverted:");

	}

	public Object concludeBits() {
		return null;
	}

	public void postprocess(Object x) {
		
	}

	public void preprocess(Object a) {
		
	}

	public void processBit(long bitno, boolean tv) {
		if (tv) System.out.println(bitno+"");
	}
	
	public void processBit(long bitno, boolean tv, Object o) {
	}


	class printBitmap implements BitProcessor {
		StringBuffer bitStr = null;
	
		public void initBits(Object a) {
			bitStr = new StringBuffer(100);
			if (((JBitmap) a).isInverted) bitStr.append("JBitmap - Inverted: \n");
		}
	
		public void initBits(Object a[]) {
			bitStr = new StringBuffer(100);
			if (((JBitmap) a[0]).isInverted) bitStr.append("JBitmap - Inverted: \n");
		}
		
		public Object concludeBits() {
			bitStr.append("\n\n");
			return bitStr.toString();
		}
		
		public void postprocess(Object x) {
			bitStr.append(x.toString());
		}
	
		public void preprocess(Object a) {
			bitStr.append(a.toString()+" -- ");
		}
		
		public void processBit(long bitno, boolean tv) {
			if (tv) bitStr.append(bitno+", ");
		}
		
		public void processBit(long bitno, boolean tv, Object o) {
			bitStr.append(bitno+" -- ");
		}
}

	class stringBitmap implements BitProcessor {
		StringBuffer bitStr = null;
		
		public void initBits(Object a) {
			bitStr = new StringBuffer(100);
			if (((JBitmap) a).isInverted) bitStr.append("Inverted: \n");
		}
		
		public void initBits(Object a[]) {
			bitStr = new StringBuffer(100);
			if (((JBitmap) a[0]).isInverted) bitStr.append("Inverted: \n");
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
}