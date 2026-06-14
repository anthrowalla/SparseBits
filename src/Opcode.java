//
//  Opcode.java
//  Bitmap_Java
//
//  Created by mf1 on 28/06/2006.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package org.csac.bits;

import java.util.Stack;

public interface Opcode {
	public void action(Stack st, Object info); // all changes made in stack
}
