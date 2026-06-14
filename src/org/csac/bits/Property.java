package org.csac.bits;

import java.util.Vector;

public class Property {

	public String name="Property";

	public RBitmap bits = new RBitmap(3); // depth 3 for ~262K capacity

	public EntityType entityType = null;
	
	public Property(String name) {
		this.name = name;
		entityType = null;
	}
	
	public Property(String name, String a) {
		this.name = name;
		EntityType x;
		if ((x = EntityType.getEntityType(a)) == null)
			entityType = new EntityType(a);
		else entityType = x;
	}

	public Property(String name, EntityType a) {
		this.name = name;
		entityType = a;
	}
		
	public EntityType getEntity() {
		return entityType;
	}
	
	public Property and(Property andBits) {
		return and(andBits, this);
	}
	
	public Property or(Property andBits) {
		return or(andBits, this);
	}
	
	public Property xor(Property andBits) {
		return xor(andBits, this);
	}
	
	public Property and(Property andBits, Property destBits) {
		if (destBits == null) destBits = new Property("["+this.name+" and "+andBits.name+"]", entityType);
		if (andBits.entityType == entityType)
			bits.and(andBits.bits, destBits.bits);
		else return null;
		return destBits;
	}
	
	public Property xor(Property xorBits, Property destBits) {
		if (destBits == null) destBits = new Property("["+this.name+" xor "+xorBits.name+"]", entityType);
		if (xorBits.entityType == entityType)
			bits.xor(xorBits.bits, destBits.bits);
		else return null;
		return destBits;
	}
	
	public Property or(Property orBits, Property destBits) {
		if (destBits == null) destBits = new Property("["+this.name+" or "+orBits.name+"]", entityType);
		if (orBits.entityType == entityType)
			bits.or(orBits.bits, destBits.bits);
		else return null;
		return destBits;
	}
	
	public Property not() {
		bits.not();
		return this;
	}
	
	public Property not(Property destBits) { // may need to reconcile casting
		return copy(destBits).not();
	}
	
	public Property copy() { // may need to reconcile casting
		Property j = new Property("["+this.name+" copy]", entityType);
		return copy(j);
	}

	public Property copy(Property destBits) { // may need to reconcile casting
		destBits.clear();
		destBits.bits = bits.copy();
		destBits.entityType = entityType;
		
		return destBits;
	}
	
	public void clear() {
		bits.clear();
	}
	
	public void clearBit(long bitno) {
		bits.setBit(bitno,false);
	}
	
	public void setBits(long bitsx[]) {
		bits.setBits(bitsx);
	}

	public void setBits(String [] names) {
		long bitsx [] = new long[names.length];
		for(int i = 0; i< names.length;i++) {
			bitsx[i] = entityType.findName((String) names[i],true);
		}
		bits.setBits(bitsx);
	}
	
	public void setBits(Vector names) {
		long bitsx [] = new long[names.size()];
		for(int i = 0; i< names.size();i++) {
			bitsx[i] = entityType.findName((String) names.elementAt(i),true);
		}
		bits.setBits(bitsx);
	}
	
	public boolean readBit(long bitno) {
		return bits.isBitSet(bitno);
	}

	public boolean readBit(String xname) {
		long bitno = entityType.findName(xname);
		if (bitno == -1) {
			System.out.println("Name not set in Property.getBit("+name+").");
			return false; // throw exception!!!
		}
		return bits.isBitSet(bitno);
	}
	
	
	public void setBit(long bitno) {
		bits.setBit(bitno,true);
	}
	
	public void setBit(long bitno, boolean tf) {
		if (tf) setBit(bitno);
		else clearBit(bitno);
	}
	
	public void setBit(String xname, boolean tf) {
		long bitno = entityType.findName(xname,true);
		if (tf) setBit(bitno);
		else clearBit(bitno);
	}

	
	public void setBit(String bitno) {
		setBit(bitno,true);
	}
	
	public void clearBit(String bitno) {
		setBit(bitno,false);
	}
	
	public void clearBits(long bitsx[]) {
		this.bits.clearBits(bitsx);
	}
	
	public String toString() {
		nameBits n = new nameBits();
		n.initBits(bits);
		bits.bitIterator(n,true);
		return name+"-"+entityType.getTypeName()+": "+((String) n.concludeBits());
	}
	
	class nameBits implements BitProcessor {
		StringBuffer bitStr = null;
		boolean comma=false;

		public void initBits(Object a) {
			bitStr = new StringBuffer(100);
			if (((RBitmap) a).getInverted()) bitStr.append("Property - Inverted: \n");
		}

		public void initBits(Object a[]) {
			bitStr = new StringBuffer(100);
			if (((RBitmap) a[0]).getInverted()) bitStr.append("Property - Inverted: \n");
		}
		
		public Object concludeBits() {
			return bitStr.toString();
		}
		
		public void processBit(long bitno, boolean tv) {
			if (tv) {
				String nm = entityType.getName(bitno);
				if (comma) bitStr.append(", ");
				bitStr.append(nm);
				comma=true;
			}
		}
		
		public void processBit(long bitno, boolean tv, Object o) {
			
		}
		
		public void preprocess(Object a) {
			
		}
		public void postprocess(Object a) {
			
		}
	}
	
}

