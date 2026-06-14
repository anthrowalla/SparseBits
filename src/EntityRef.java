//
//  EntityRef.java
//  Bitmap_Java
//
//  Created by mf1 on 29/06/2006.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package org.csac.bits;
import java.util.Stack;

public class EntityRef implements Opcode {
	
	Object ref = null;
	
	public void action(Stack st, Object info) {
		if (ref == null) {
			st.push(null); // replace with an error object
		} else if (ref instanceof String) {
			
		}
		
		
	} // all changes made in stack

}
