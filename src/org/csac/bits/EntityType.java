package org.csac.bits;

import java.util.*;

public class EntityType {
	static Hashtable theTypes = new Hashtable(100,10);
	Hashtable theNames; // in name order
	public Vector theIds; // in id order
	String typeName;
	int nkeys=0; // doubles as length
	
	public EntityType(String name) {
		typeName = name;
		theNames = new Hashtable(100);
		theIds = new Vector(100,10);
		theTypes.put(name,this); // return Entitytype object with name;
	}
	
	static public EntityType getEntityType(String name) {
		return (EntityType) theTypes.get(name);
	}
	
	public long addName(String name) {//  returns id
		name = name.toLowerCase();
		if (theNames.containsKey(name)) 
			return (long)((Integer) theNames.get(name)).intValue();
		else {
			theNames.put(name,new Integer(nkeys++));
			theIds.addElement(name);
			return nkeys-1;
		}
	}
	
	public long findName(String name) {//  returns id
		name = name.toLowerCase();
		if (theNames.containsKey(name)) 
			return (long) ((Integer) theNames.get(name)).intValue();
		else return -1;
	}

	public long findName(String name, boolean add) {//  returns id
		name = name.toLowerCase();
		if (theNames.containsKey(name)) 
			return (long) ((Integer) theNames.get(name)).intValue();
		else if (add) {
			return addName(name);
		} else 
			return -1;
	}
	
	public String getName(long id) {
		if (id >= theIds.size()) return id+"";
		return (String) theIds.elementAt((int) id);
	}	
	
	public int getSize() {
		return nkeys;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String listNames() {
		StringBuffer out = new StringBuffer();
		long i;
		for (i=0;i<getSize()-1;i++) {
			out.append(i+": "+getName(i)+", ");
		}
		out.append(i+": "+getName(i));
		return out.toString();
	}
	
}