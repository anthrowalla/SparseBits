package org.csac.bits;



public class BitHelper {
	long head=0;
	long shead=0;
	long data=0;
	
	int counter=0;
	int tcount = 0;
	int lcount = 0;
	
	long thisBit=-1;
	
	int bindex=-1;
	int index=0;
	int sindex=0;
	
	int binc=4096;
	int minc=64;
	
	int absindex = 0; // index to actual array of bits
	
	JxBits theBits=null;
	
	BitHelper(JxBits j) {
		theBits = j;
		init();
	}
	
	public void init() {
		head = theBits.itsHeader;
		while (head != 0 && (head & 1) != 1) {
			head >>>= 1;
			counter += binc;
		}
		index = 0;sindex = 0;
		if (head == 0) { /* throw something here */ }
		shead = theBits.itsBits[index].itsHeader;
		while (shead != 0 && (shead & 1) != 1) {
			shead >>>= 1;
			tcount += minc;
		}
		
		if (shead == 0) { /* throw something here */ }
		data = theBits.itsBits[index].itsBits[sindex];
		while (data != 0 && (data & 1) != 1) {
			data >>>= 1;
			lcount ++;
		}
		if (data == 0) { /* throw something here */ }
	}
	
	public int getBindex() {
		return bindex;
	}
	
	public long getBit() {
		return thisBit;
	}
	
	public int getxIndex() {
		return index;
	}
	
	public int getSindex() {
		return sindex;
	}
	
	boolean bumpindex = false, bumpsindex = false;
	
	public long nextBit() {
		thisBit = counter + tcount + lcount;
		if (bumpindex) {
			index++;
			bumpindex = false;
		}
		if (bumpsindex) {
			sindex++;
			bumpindex = false;
		}
		if (counter == -1) return -1;
		for (data >>>= 1,lcount++;data != 0 && (data & 1) != 1;data >>>= 1,lcount++);
		if (data == 0) {
			lcount=0;
			for (shead >>>= 1,tcount += minc;shead != 0 && (shead & 1) != 1;shead >>>= 1,tcount += minc);
			if (shead == 0) {
				sindex = 0;
				tcount = 0;
				for (head >>>= 1,counter += binc;head != 0 && (head & 1) != 1;head >>>= 1,counter += binc);
				if (head == 0) {
					counter = -1; // out of bits report next time
				} else {
					if ( theBits.itsBits.length <= index+1) {
						System.out.println("Outside: "+(index+1)+" "+(sindex));
					}
					shead = theBits.itsBits[index+1].itsHeader;
					for (;shead != 0 && (shead & 1) != 1;shead >>>= 1,tcount += minc);
					for (data = theBits.itsBits[index+1].itsBits[sindex];data != 0 && (data & 1) != 1;data >>>= 1,lcount++);
				}
				bumpindex = true;
			} else {
				bumpsindex = true;
				//System.out.println("Probe: "+index+" "+(sindex+1));
				if (theBits.itsBits[index].itsBits.length <= sindex+1) {
						System.out.println("Inside: "+index+" "+(sindex+1));
						data = 0; // cludge
					} else
				for (data = theBits.itsBits[index].itsBits[sindex+1];data != 0 && (data & 1) != 1;data >>>= 1,lcount++);
			}
		}
		bindex++;
		return thisBit;
	}
}
