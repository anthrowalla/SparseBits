//
//  Parser.java
//  Bitmap_Java
//
//  Created by mf1 on 28/06/2006.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//
package org.csac.bits;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Stack;
import java.util.StringTokenizer;

/* syntax RPN

Name --> in quotes = string
--> lookup ... if found object
	else string
	X.Y implies X bits or bitmap or String or vector or matrix
	Y is a range statement that interprets X, leaving result on stack
	'|' is a pipe that connects bitmaps and/or relations
	v = and or -
	^ - or +
	~ = not
	<- set
	*/

public class Parser {

	public Hashtable objects= new Hashtable(); /* key = name , result = Object */
	public Hashtable opnames= new Hashtable(); /* key = name , result = opcode number */
	public Vector opcodes = new Vector(); // contains objects of type Opcode;
	public Stack stack=new Stack();
	
	
	public void parse(String statement) {
		char xc='.';
		StringTokenizer tokens = new StringTokenizer(statement.toLowerCase(),
							         " \t\n\r\f.()+-|&^~!,;:@#%*$={}[]'?/><§\"",true);
		while (tokens.hasMoreTokens()) {
			String x = tokens.nextToken();
			xc = x.charAt(0);
			if (x.length() == 1 && !Character.isLetterOrDigit(xc)) {
				
				switch (xc) {
					case '+': // union A B
						break;
					case '-': // complement
						break;
					case '~' :
					case '!' : // not
						break;
					case '|' : // pipe
						break;
					
				}
			} else {
				Object o;
				if (( o = objects.get(x)) != null) {
					
				}
			}
		}
	}
	
}
