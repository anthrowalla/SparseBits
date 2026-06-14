//
//  Relation.java
//  Bitmap_Java
//
//  Created by mf1 on 29/06/2006.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package org.csac.bits;

public class Relation  {

	public String name="Relation";

	// Default depth 2 provides 262K × 262K capacity per dimension
	// Use higher depth for larger EntityTypes (depth 3 = 16.7M, depth 4 = 1B+)
	public R2Bitmap bmap = new R2Bitmap(2);
	EntityType rowType = null;
	EntityType colType = null;
	EntityType zType = null;

	static final int AND = 0;
	static final int OR = 1;
	static final int XOR = 2;

	// Constructor with explicit depth
	public Relation(String name, String a, String b, int depth) {
		EntityType x;
		this.name = name;
		if ((x = EntityType.getEntityType(a)) == null)
			rowType = new EntityType(a);
		else rowType = x;
		if ((x = EntityType.getEntityType(b)) == null)
			colType = new EntityType(b);
		else colType = x;
		this.bmap = new R2Bitmap(depth, rowType, colType);
	}

	public Relation(String name, String a, String b) {
		this(name, a, b, 2); // default depth 2
	}

	public Relation(String name, EntityType a, EntityType b, int depth) {
		this.name = name;
		rowType = a;
		colType = b;
		this.bmap = new R2Bitmap(depth, rowType, colType);
	}

	public Relation(String name, EntityType a, EntityType b) {
		this(name, a, b, 2); // default depth 2
	}
		
	public EntityType getRowEntity() {
		return rowType;
	}

	public EntityType getColEntity() {
		return colType;
	}
	
	public EntityType getZEntity() {
		return zType;
	}
	
	public Relation pipe(Relation refMap, Relation destBits) {
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("pipe() not yet implemented for recursive R2Bitmap");
		return null;
	}

	public Relation pipe(Relation refMap) {
		return pipe(refMap,this);
	}

	public Relation filter(Property filterBits, Relation destBits) {
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("filter() not yet implemented for recursive R2Bitmap");
		return null;
	}

	public Relation filter(Property filterBits) {
		return filter(filterBits,this);
	}

	public Relation reverse(Relation destBits) { // reverse what!! ... care here
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("reverse() not yet implemented for recursive R2Bitmap");
		return null;
	}

	public Relation reverse() { // reverse what!! ... care here
		return reverse(this);
	}
	
	public Relation reduce(Property andBits, int PRED) {
		return reduce(andBits,this, PRED);
	}

	public Relation reduce(Property andBits, Relation destBits, int PRED) {
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("reduce() not yet implemented for recursive R2Bitmap");
		return null;
	}

	public Relation maskAnd(Property andBits, Relation destBits) {
		return reduce(andBits, destBits, AND);
	}

	public Relation maskOr(Property andBits, Relation destBits) { // just the set rows, not the zero rows... may adapt later
		return reduce(andBits, destBits, OR);
	}


	public Relation maskXor(Property andBits, Relation destBits) { // just the set rows, not the zero rows... may adapt later
		return reduce(andBits, destBits, XOR);
	}

	public Relation maskAnd(Property andBits) {
		return reduce(andBits, this, AND);
	}

	public Relation maskOr(Property andBits) { // just the set rows, not the zero rows... may adapt later
		return reduce(andBits, this, OR);
	}
	
	
	public Relation maskXor(Property andBits) { // just the set rows, not the zero rows... may adapt later
		return reduce(andBits, this, XOR);
	}
	
	public Relation and(Relation andBits, Relation destBits) {
		if (destBits == null) destBits = new Relation("["+this.name+" and "+andBits.name+"]", rowType,colType);
		if (andBits.rowType == rowType && andBits.colType == colType)
			bmap.and(andBits.bmap, destBits.bmap);
		else {
			System.out.println("Returning null from Relation.and() Mismatch in types");
			return null;
		}
		return destBits;
	}
	
	public Relation and(Relation andBits) {
		return and(andBits, this);
	}
	
	public Relation or(Relation orBits, Relation destBits) {
		if (destBits == null) destBits = new Relation("["+this.name+" or "+orBits.name+"]", rowType,colType);
		if (orBits.rowType == rowType && orBits.colType == colType)
			bmap.or(orBits.bmap,destBits.bmap);
		else return null;
		return destBits;
	} // null = new 
		
	public Relation or(Relation orBits) 
	{
		return or(orBits, this);
	}
	
	public Relation xor(Relation xorBits, Relation destBits)  // null = new
	{
		if (destBits == null) destBits = new Relation("["+this.name+" xor "+xorBits.name+"]", rowType,colType);
		if (xorBits.rowType == rowType && xorBits.colType == colType)
			bmap.xor(xorBits.bmap,destBits.bmap);
		else return null;
		return destBits;
	}
	
	public Relation xor(Relation xorBits) 
	{
		return xor(xorBits, this);
	}
	
	
	public Relation copy(Relation destBits) {
		destBits.clear();
		destBits.bmap = bmap.copy();
		destBits.colType = colType;
		destBits.rowType = rowType;
		return destBits;
	}
	
	public Relation copy() {
		Relation j = new Relation("["+this.name+" copy]", rowType,colType);
		return copy(j);
	}
		
	public Relation not() { // may need to reconcile casting
		bmap.not();
		return this;
	}
	
	public Relation not(Relation destBits) { // may need to reconcile casting
		return copy(destBits).not();
	}
	
	public void clear() {
		bmap.clear();
	}
	
	public Property unionAnd() {
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("unionAnd() not yet implemented for recursive R2Bitmap");
		return new Property(name+".union", colType);
	}

	public Property unionOr() {
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("unionOr() not yet implemented for recursive R2Bitmap");
		return new Property(name+".union", colType);
	}

	public Property unionXor() {
		// TODO: Implement for recursive R2Bitmap structure
		System.out.println("unionXor() not yet implemented for recursive R2Bitmap");
		return new Property(name+".union", colType);
	}
	
	public Property readBits(String posRow) {
		// TODO: Implement for recursive R2Bitmap structure
		// This would require iterating through all column positions for the given row
		System.out.println("readBits(String) not yet implemented for recursive R2Bitmap");
		return new Property(name+".read", colType);
	}

	public Property readBits(long row) {
		// TODO: Implement for recursive R2Bitmap structure
		// This would require iterating through all column positions for the given row
		System.out.println("readBits(long) not yet implemented for recursive R2Bitmap");
		return new Property(name+".read", colType);
	}
	
	public boolean readBit(String posy,String posx) {
		long bitnoy, bitnox;
		bitnoy = rowType.findName(posy, true);
		bitnox = colType.findName(posx, true);
		if (bitnox == -1 || bitnoy == -1) {
			System.out.println("Name not set in Relation.readBit("+posy+", "+posx+").");
			return false; // throw exception!!!
		}
		return readBit(bitnoy,bitnox);
	}
	
	public boolean readBit(long row, long col) {
		return bmap.isBitSet(row,col);
	}
	
	public void setBits(long bits[]) {
		// Set multiple [row, col, col, ...] pairs
		// Format: bits[0] = row, bits[1] = col1, bits[2] = col2, etc.
		if (bits.length < 2) return;
		long row = bits[0];
		for (int i = 1; i < bits.length; i++) {
			bmap.setBit(row, bits[i], true);
		}
	}

	public void setBits(long bitno, Property p) {
		// TODO: Implement for recursive R2Bitmap structure
		// Would iterate through all set bits in p and set them in the row
		System.out.println("setBits(row, Property) not yet implemented for recursive R2Bitmap");
	}
	
	
	public void setBits(String pos, Property p) {
		// TODO: Implement for recursive R2Bitmap structure
		// Would iterate through all set bits in p and set them in the row
		System.out.println("setBits(Property) not yet implemented for recursive R2Bitmap");
	}
	
	public void setBit(long bitnoy,long bitnox) {
		bmap.setBit(bitnoy,bitnox,true);
	}

	public void clearBit(long bitnoy,long bitnox) {
		bmap.setBit(bitnoy,bitnox,false);
	}
	
	public void setBit(long bitnoy, long bitnox, boolean tv) {
		bmap.setBit(bitnoy,bitnox,tv);
	}

	public void setBit(String posy,String posx, boolean tv) {
		long bitnoy, bitnox;
		bitnoy = rowType.findName(posy, true);
		bitnox = colType.findName(posx, true);
		bmap.setBit(bitnoy,bitnox,tv);
	}

	public void setBit(String posy,String posx) {
		setBit(posy, posx, true);
	}

	public void clearBit(String posy,String posx) {
		setBit(posy, posx, false);
	}
	
	public String toxString() {
		return rowType.getTypeName()+":"+colType.getTypeName()+"\n"+bmap.toString();
	}



	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name).append("-").append(rowType.getTypeName()).append(":").append(colType.getTypeName());
		sb.append("\n").append(bmap.toString());
		return sb.toString();
	}

	class nameBits implements BitProcessor {
		StringBuffer bitStr = null;
		boolean comma=false;

		public void initBits(Object a) {
			bitStr = new StringBuffer(100);
			// Handle RBitmap case
			if (a instanceof RBitmap) {
				if (((RBitmap) a).getInverted()) bitStr.append("Relation - Inverted: ");
			}
			// R2Bitmap case handled by initBits(Object[]) method
		}

		public void initBits(Object a[]) {
			bitStr = new StringBuffer(100);
			if (a[0] instanceof R2Bitmap) {
				if (((R2Bitmap) a[0]).isInverted) bitStr.append("Relation - Inverted: ");
			}
		}

		public Object concludeBits() {
			bitStr.append("\n\n");
			return bitStr.toString();
		}

		public void processBit(long bitno, boolean tv) {
			if (tv) {
				String nm = colType.getName(bitno);
			//	if (nm == null) nm = bitno+"";
				if (comma) bitStr.append(", ");
				bitStr.append(nm);
				comma=true;
			}
		}

		public void processBit(long bitno, boolean tv, Object o) {
			String nm = rowType.getName(bitno);
		//	if (nm == null) nm = bitno+"";
			bitStr.append(nm+" -- ");
		}
		
		public void preprocess(Object a) {
			bitStr.append(a.toString()+" -- ");
		}
		public void postprocess(Object a) {
			bitStr.append(a.toString());
			comma = false;
		}
	}
	
}
