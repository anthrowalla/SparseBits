package org.csac.bits;

// Recursive bitmap - handles any depth
// At depth 0: leaf node storing long[]
// At depth > 0: internal node storing array of child RBitmaps

public class RBitmap {
	// Configuration - same for all instances in a tree
	public static final int BRANCH_SHIFT = 6; // log2(64) = 6 bits per level
	public static final int BRANCH = 64; // branching factor (based on word size)
	public static final int LEAF_BITS = BRANCH * BRANCH; // 4096 bits at leaf

	// Table of n bits per byte 0-255 - shared for counting
	public static final int bitCounts[] = {
		0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4,
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
		4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8
	};

	// Instance state
	long itsHeader; // bitset indicating which children are non-null
	boolean isInverted;

	// At leaf (depth=0): itsBits holds long[] of actual bit data
	// At internal (depth>0): children holds RBitmap[]
	long[] itsBits; // leaf data
	RBitmap[] children; // child nodes
	int depth; // 0 = leaf, 1+ = internal
	int treeDepth; // total depth of tree (for capacity calculations)

	// Root constructor - creates empty bitmap
	public RBitmap(int depth) {
		this.depth = depth;
		this.treeDepth = depth;
		clear();
	}

	// Internal constructor - creates child node
	private RBitmap(int depth, int treeDepth) {
		this.depth = depth;
		this.treeDepth = treeDepth;
		clear();
	}

	// Create a new bitmap with same depth
	private RBitmap createNode() {
		return new RBitmap(depth - 1, treeDepth);
	}

	// Total capacity in bits
	// Depth 0: 64*64 = 4096 bits (leaf stores 64 blocks of 64 bits)
	// Depth 1: 64*4096 = 262K bits
	// Depth 2: 64*262K = 16M bits
	// Depth 3: 64*16M = 1B bits
	// etc.
	public long capacity() {
		long cap = BRANCH * BRANCH; // leaf capacity
		for (int i = 0; i < treeDepth - 1; i++) {
			cap *= BRANCH;
		}
		return cap;
	}

	// Clear all data
	public void clear() {
		itsHeader = 0;
		isInverted = false;
		itsBits = null;
		children = null;
	}

	// Count total set bits
	public long count() {
		if (isInverted) {
			// When inverted, count = capacity - actual bits
			long actualCount = 0;
			if (depth == 0) {
				// Leaf level - count bits in long[]
				long h = itsHeader;
				int idx = 0;
				while (h != 0) {
					if ((h & 1) != 0) {
						actualCount += countBits(itsBits[idx++]);
					} else {
						// Missing block: all 64 bits in this block are "set" when inverted
						actualCount += BRANCH;
					}
					h >>>= 1;
				}
			} else {
				// Internal level - recurse and count missing children too
				// Each child (at depth-1) has capacity: BRANCH * BRANCH * BRANCH^(depth-2)
				long childCapacity = BRANCH * BRANCH;
				for (int i = 0; i < depth - 1; i++) {
					childCapacity *= BRANCH;
				}
				long h = itsHeader;
				int idx = 0;
				while (h != 0) {
					if ((h & 1) != 0) {
						actualCount += children[idx++].count();
					} else {
						// Missing child: all of its capacity is "set" when inverted
						actualCount += childCapacity;
					}
					h >>>= 1;
				}
			}
			return capacity() - actualCount;
		} else {
			// Not inverted - count actual bits
			if (depth == 0) {
				// Leaf level - count bits in long[]
				long h = itsHeader;
				int idx = 0;
				long total = 0;
				while (h != 0) {
					if ((h & 1) != 0) {
						total += countBits(itsBits[idx++]);
					}
					h >>>= 1;
				}
				return total;
			} else {
				// Internal level - recurse
				long h = itsHeader;
				int idx = 0;
				long total = 0;
				while (h != 0) {
					if ((h & 1) != 0) {
						total += children[idx++].count();
					}
					h >>>= 1;
				}
				return total;
			}
		}
	}

	// Static countBits utilities
	public static int countBits(byte a) {
		return bitCounts[a & 255];
	}

	public static int countBits(short a) {
		return bitCounts[a & 255] + bitCounts[(a >>> 8) & 255];
	}

	public static int countBits(int a) {
		return bitCounts[a & 255] + bitCounts[(a >>>= 8) & 255]
				+ bitCounts[(a >>>= 8) & 255] + bitCounts[(a >>>= 8) & 255];
	}

	public static int countBits(long a) {
		return bitCounts[(int) (a & 255)] + bitCounts[(int) ((a >>>= 8) & 255)]
				+ bitCounts[(int) ((a >>>= 8) & 255)] + bitCounts[(int) ((a >>>= 8) & 255)]
				+ bitCounts[(int) ((a >>>= 8) & 255)] + bitCounts[(int) ((a >>>= 8) & 255)]
				+ bitCounts[(int) ((a >>>= 8) & 255)] + bitCounts[(int) ((a >>>= 8) & 255)];
	}

	// Check if a bit is set
	public boolean isBitSet(long bitno) {
		if (bitno < 0 || bitno >= capacity()) {
			return false; // or throw exception
		}

		if (depth == 0) {
			// Leaf level
			long bitpos = bitno % BRANCH;
			long blockno = bitno / BRANCH;
			long bhead = 1L << blockno;

			if ((itsHeader & bhead) == 0) {
				return isInverted;
			}
			int c = countBits(itsHeader & (bhead - 1));
			return ((itsBits[c] & (1L << bitpos)) != 0) != isInverted;
		} else {
			// Navigate to child
			long childIdx = bitno >> ((depth) * BRANCH_SHIFT);
			long remainingBit = bitno & ((1L << (depth * BRANCH_SHIFT)) - 1);

			long bhead = 1L << childIdx;
			if ((itsHeader & bhead) == 0) {
				return isInverted;
			}

			int c = countBits(itsHeader & (bhead - 1));
			// Get child's bit value and apply parent's inversion
			boolean childBit = children[c].isBitSet(remainingBit);
			return childBit != isInverted;
		}
	}

	// Set or clear a bit
	public void setBit(long bitno, boolean value) {
		if (bitno < 0 || bitno >= capacity()) {
			return; // or throw exception
		}

		if (isInverted) value = !value;

		if (depth == 0) {
			setBitLeaf(bitno, value);
		} else {
			setBitInternal(bitno, value);
		}
	}

	public void setBit(long bitno) {
		setBit(bitno, true);
	}

	public void clearBit(long bitno) {
		setBit(bitno, false);
	}

	private void setBitLeaf(long bitno, boolean value) {
		long bitpos = bitno % BRANCH;
		long blockno = bitno / BRANCH;
		long bhead = 1L << blockno;
		long bpos = 1L << bitpos;

		if (itsHeader == 0) {
			if (value) {
				itsHeader = bhead;
				itsBits = new long[1];
				itsBits[0] = bpos;
			}
			return;
		}

		if ((itsHeader & bhead) != 0) {
			// Block exists
			int bp = countBits(itsHeader & (bhead - 1));
			if (value) {
				itsBits[bp] |= bpos;
			} else {
				itsBits[bp] &= ~bpos;
				if (itsBits[bp] == 0) {
					// Remove empty block
					int newLen = itsBits.length - 1;
					if (newLen == 0) {
						itsBits = null;
						itsHeader = 0;
					} else {
						long[] newBits = new long[newLen];
						System.arraycopy(itsBits, 0, newBits, 0, bp);
						System.arraycopy(itsBits, bp + 1, newBits, bp, newLen - bp);
						itsBits = newBits;
						itsHeader &= ~(1L << blockno);
					}
				}
			}
		} else if (value) {
			// Need to add new block
			int pos = countBits(itsHeader);
			long[] newBits = new long[pos + 1];
			int insertAt = countBits(itsHeader & (bhead - 1));
			System.arraycopy(itsBits, 0, newBits, 0, insertAt);
			newBits[insertAt] = bpos;
			System.arraycopy(itsBits, insertAt, newBits, insertAt + 1, pos - insertAt);
			itsBits = newBits;
			itsHeader |= bhead;
		}
	}

	private void setBitInternal(long bitno, boolean value) {
		long childIdx = bitno >> (depth * BRANCH_SHIFT);
		long remainingBit = bitno & ((1L << (depth * BRANCH_SHIFT)) - 1);
		long bhead = 1L << childIdx;

		if ((itsHeader & bhead) != 0) {
			// Child exists
			int bp = countBits(itsHeader & (bhead - 1));
			RBitmap child = children[bp];
			child.setBit(remainingBit, value);

			// Check if child became empty
			if (!value && child.itsHeader == 0) {
				int newLen = children.length - 1;
				if (newLen == 0) {
					children = null;
					itsHeader = 0;
				} else {
					RBitmap[] newChildren = new RBitmap[newLen];
					System.arraycopy(children, 0, newChildren, 0, bp);
					System.arraycopy(children, bp + 1, newChildren, bp, newLen - bp);
					children = newChildren;
					itsHeader &= ~(bhead);
				}
			}
		} else if (value) {
			// Need to add new child
			int pos = countBits(itsHeader);
			RBitmap[] newChildren = new RBitmap[pos + 1];
			int insertAt = countBits(itsHeader & (bhead - 1));
			if (children != null) {
				System.arraycopy(children, 0, newChildren, 0, insertAt);
				System.arraycopy(children, insertAt, newChildren, insertAt + 1, pos - insertAt);
			}
			RBitmap newChild = createNode();
			newChild.setBit(remainingBit, true);
			newChildren[insertAt] = newChild;
			children = newChildren;
			itsHeader |= bhead;
		}
	}

	// Set multiple bits from array
	public void setBits(long[] bits) {
		for (int i = 0; i < bits.length; i++) {
			setBit(bits[i]);
		}
	}

	// NOT operation - inverts all bits (lazy: sets flag, doesn't modify data)
	// Note: inversion is local to each level and does NOT propagate to children
	// When a node is inverted, its children are treated as normal (non-inverted)
	public RBitmap not() {
		isInverted = !isInverted;
		return this;
	}

	// AND operation
	public RBitmap and(RBitmap other, RBitmap dest) {
		if (dest == null) dest = new RBitmap(depth, treeDepth);
		if (dest != this && dest != other) dest.clear();

		// Handle inversion
		if (isInverted || other.isInverted) {
			return andInverted(other, dest);
		}

		long h1 = itsHeader;
		long h2 = other.itsHeader;
		long resultHeader = 0;
		int idx1 = 0, idx2 = 0, resultCount = 0;

		// Use temporary arrays for result
		long[] resultBits = null;
		RBitmap[] resultChildren = null;

		// Process through all bit positions
		for (int bitPos = 0; (h1 | h2) != 0; bitPos++, h1 >>>= 1, h2 >>>= 1) {
			boolean in1 = (h1 & 1L) != 0;
			boolean in2 = (h2 & 1L) != 0;

			if (in1 && in2) {
				if (depth == 0) {
					long val = itsBits[idx1] & other.itsBits[idx2];
					if (val != 0) {
						if (resultBits == null) resultBits = new long[BRANCH];
						resultBits[resultCount] = val;
						resultHeader |= (1L << bitPos);
						resultCount++;
					}
				} else {
					RBitmap r = children[idx1].and(other.children[idx2], null);
					if (r != null && r.itsHeader != 0) {
						if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
						resultChildren[resultCount] = r;
						resultHeader |= (1L << bitPos);
						resultCount++;
					}
				}
				idx1++;
				idx2++;
			} else if (in1) {
				idx1++;
			} else if (in2) {
				idx2++;
			}
		}

		if (resultCount == 0) {
			dest.clear();
		} else if (depth == 0) {
			long[] finalBits = new long[resultCount];
			System.arraycopy(resultBits, 0, finalBits, 0, resultCount);
			dest.itsBits = finalBits;
			dest.itsHeader = resultHeader;
			dest.children = null;
		} else {
			RBitmap[] finalChildren = new RBitmap[resultCount];
			System.arraycopy(resultChildren, 0, finalChildren, 0, resultCount);
			dest.children = finalChildren;
			dest.itsHeader = resultHeader;
			dest.itsBits = null;
		}

		return dest;
	}

	private RBitmap andInverted(RBitmap other, RBitmap dest) {
		// Handle AND with inversion using proper boolean algebra
		if (isInverted && other.isInverted) {
			// !A & !B = !(A | B)
			RBitmap temp1 = this.copy();
			RBitmap temp2 = other.copy();
			temp1.isInverted = false;
			temp2.isInverted = false;
			RBitmap result = temp1.or(temp2, dest);
			result.not();
			return result;
		} else if (isInverted) {
			// !A & B = B - A (bits in B that are NOT in A)
			RBitmap temp1 = this.copy();
			temp1.isInverted = false;
			// We need B & !A = B - A
			// Since we don't have a subtract operation, use: B & !A = B & (everything ^ A)
			// But that's expensive. Instead, use: B - A = B & !A
			// Compute by: (B | A) ^ A
			RBitmap temp2 = other.copy();
			RBitmap bOrA = temp2.or(temp1, null);
			RBitmap result = bOrA.xor(temp1, dest);
			return result;
		} else {
			// A & !B = A - B (bits in A that are NOT in B)
			RBitmap temp2 = other.copy();
			temp2.isInverted = false;
			RBitmap temp1 = this.copy();
			RBitmap aOrB = temp1.or(temp2, null);
			RBitmap result = aOrB.xor(temp2, dest);
			return result;
		}
	}

	public RBitmap and(RBitmap other) {
		return and(other, this);
	}

	// OR operation
	public RBitmap or(RBitmap other, RBitmap dest) {
		if (dest == null) dest = new RBitmap(depth, treeDepth);
		if (dest != this && dest != other) dest.clear();

		if (isInverted || other.isInverted) {
			return orInverted(other, dest);
		}

		long h1 = itsHeader;
		long h2 = other.itsHeader;
		long resultHeader = 0;
		int idx1 = 0, idx2 = 0, resultCount = 0;

		long[] resultBits = null;
		RBitmap[] resultChildren = null;

		// Process through all bit positions
		for (int bitPos = 0; (h1 | h2) != 0; bitPos++, h1 >>>= 1, h2 >>>= 1) {
			boolean in1 = (h1 & 1L) != 0;
			boolean in2 = (h2 & 1L) != 0;

			if (in1 && in2) {
				if (depth == 0) {
					long val = itsBits[idx1] | other.itsBits[idx2];
					if (val != 0) {
						if (resultBits == null) resultBits = new long[BRANCH];
						resultBits[resultCount] = val;
						resultHeader |= (1L << resultCount);
						resultCount++;
					}
				} else {
					RBitmap r = children[idx1].or(other.children[idx2], null);
					if (r != null && r.itsHeader != 0) {
						if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
						resultChildren[resultCount] = r;
						resultHeader |= (1L << resultCount);
						resultCount++;
					}
				}
				idx1++;
				idx2++;
			} else if (in1) {
				if (depth == 0) {
					if (resultBits == null) resultBits = new long[BRANCH];
					resultBits[resultCount] = itsBits[idx1];
				} else {
					if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
					resultChildren[resultCount] = children[idx1].copy();
				}
				resultHeader |= (1L << resultCount);
				resultCount++;
				idx1++;
			} else if (in2) {
				if (depth == 0) {
					if (resultBits == null) resultBits = new long[BRANCH];
					resultBits[resultCount] = other.itsBits[idx2];
				} else {
					if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
					resultChildren[resultCount] = other.children[idx2].copy();
				}
				resultHeader |= (1L << resultCount);
				resultCount++;
				idx2++;
			}
		}

		if (resultCount == 0) {
			dest.clear();
		} else if (depth == 0) {
			long[] finalBits = new long[resultCount];
			System.arraycopy(resultBits, 0, finalBits, 0, resultCount);
			dest.itsBits = finalBits;
			dest.itsHeader = resultHeader;
			dest.children = null;
		} else {
			RBitmap[] finalChildren = new RBitmap[resultCount];
			System.arraycopy(resultChildren, 0, finalChildren, 0, resultCount);
			dest.children = finalChildren;
			dest.itsHeader = resultHeader;
			dest.itsBits = null;
		}

		return dest;
	}

	private RBitmap orInverted(RBitmap other, RBitmap dest) {
		RBitmap temp1 = this.copy();
		RBitmap temp2 = other.copy();
		temp1.isInverted = false;
		temp2.isInverted = false;

		if (isInverted && other.isInverted) {
			// !A | !B = !(A & B)
			RBitmap result = temp1.and(temp2, dest);
			result.not();
			return result;
		} else if (isInverted) {
			// !A | B = !(A & !B)
			RBitmap result = temp1.and(temp2, dest);
			result.not();
			return result;
		} else {
			// A | !B = !(!A & B)
			RBitmap result = temp1.and(temp2, dest);
			result.not();
			return result;
		}
	}

	public RBitmap or(RBitmap other) {
		return or(other, this);
	}

	// XOR operation
	public RBitmap xor(RBitmap other, RBitmap dest) {
		if (dest == null) dest = new RBitmap(depth, treeDepth);
		if (dest != this && dest != other) dest.clear();

		if (isInverted || other.isInverted) {
			return xorInverted(other, dest);
		}

		long h1 = itsHeader;
		long h2 = other.itsHeader;
		long resultHeader = 0;
		int idx1 = 0, idx2 = 0, resultCount = 0;

		long[] resultBits = null;
		RBitmap[] resultChildren = null;

		// Process through all bit positions
		for (int bitPos = 0; (h1 | h2) != 0; bitPos++, h1 >>>= 1, h2 >>>= 1) {
			boolean in1 = (h1 & 1L) != 0;
			boolean in2 = (h2 & 1L) != 0;

			if (in1 && in2) {
				if (depth == 0) {
					long val = itsBits[idx1] ^ other.itsBits[idx2];
					if (val != 0) {
						if (resultBits == null) resultBits = new long[BRANCH];
						resultBits[resultCount] = val;
						resultHeader |= (1L << resultCount);
						resultCount++;
					}
				} else {
					RBitmap r = children[idx1].xor(other.children[idx2], null);
					if (r != null && r.itsHeader != 0) {
						if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
						resultChildren[resultCount] = r;
						resultHeader |= (1L << resultCount);
						resultCount++;
					}
				}
				idx1++;
				idx2++;
			} else if (in1) {
				if (depth == 0) {
					if (resultBits == null) resultBits = new long[BRANCH];
					resultBits[resultCount] = itsBits[idx1];
				} else {
					if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
					resultChildren[resultCount] = children[idx1].copy();
				}
				resultHeader |= (1L << resultCount);
				resultCount++;
				idx1++;
			} else if (in2) {
				if (depth == 0) {
					if (resultBits == null) resultBits = new long[BRANCH];
					resultBits[resultCount] = other.itsBits[idx2];
				} else {
					if (resultChildren == null) resultChildren = new RBitmap[BRANCH];
					resultChildren[resultCount] = other.children[idx2].copy();
				}
				resultHeader |= (1L << resultCount);
				resultCount++;
				idx2++;
			}
		}

		if (resultCount == 0) {
			dest.clear();
		} else if (depth == 0) {
			long[] finalBits = new long[resultCount];
			System.arraycopy(resultBits, 0, finalBits, 0, resultCount);
			dest.itsBits = finalBits;
			dest.itsHeader = resultHeader;
			dest.children = null;
		} else {
			RBitmap[] finalChildren = new RBitmap[resultCount];
			System.arraycopy(resultChildren, 0, finalChildren, 0, resultCount);
			dest.children = finalChildren;
			dest.itsHeader = resultHeader;
			dest.itsBits = null;
		}

		return dest;
	}

	private RBitmap xorInverted(RBitmap other, RBitmap dest) {
		RBitmap temp1 = this.copy();
		RBitmap temp2 = other.copy();
		temp1.isInverted = false;
		temp2.isInverted = false;

		if (isInverted && other.isInverted) {
			// !A ^ !B = A ^ B
			return temp1.xor(temp2, dest);
		} else if (isInverted) {
			// !A ^ B = !(A ^ B)
			RBitmap result = temp1.xor(temp2, dest);
			result.not();
			return result;
		} else {
			// A ^ !B = !(A ^ B)
			RBitmap result = temp1.xor(temp2, dest);
			result.not();
			return result;
		}
	}

	public RBitmap xor(RBitmap other) {
		return xor(other, this);
	}

	// Copy this bitmap
	public RBitmap copy() {
		RBitmap result = new RBitmap(depth, treeDepth);
		result.itsHeader = itsHeader;
		result.isInverted = isInverted;

		if (depth == 0 && itsBits != null) {
			result.itsBits = new long[itsBits.length];
			System.arraycopy(itsBits, 0, result.itsBits, 0, itsBits.length);
		} else if (children != null) {
			result.children = new RBitmap[children.length];
			for (int i = 0; i < children.length; i++) {
				result.children[i] = children[i].copy();
			}
		}

		return result;
	}

	// Get inverted state
	public boolean getInverted() {
		if (depth == 0 || children == null) {
			return isInverted;
		}
		return children[0].isInverted;
	}

	// String representation
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RBitmap[depth=").append(depth);
		sb.append(", capacity=").append(capacity());
		sb.append(", bits=").append(count());
		sb.append("]");
		return sb.toString();
	}

	// Simple iterator that prints set bits - for testing
	public void printSetBits() {
		printSetBits(0);
	}

	private void printSetBits(long baseOffset) {
		if (depth == 0) {
			long h = itsHeader;
			int idx = 0;
			long bitpos = baseOffset;
			for (int i = 0; i < BRANCH; i++, h >>>= 1, bitpos += BRANCH) {
				if ((h & 1) != 0) {
					long data = itsBits[idx++];
					for (int j = 0; j < BRANCH; j++, data >>>= 1) {
						if ((data & 1) != 0) System.out.println(bitpos + j);
					}
				}
			}
		} else {
			long h = itsHeader;
			int idx = 0;
			long childOffset = baseOffset;
			long childSize = 1L << (depth * BRANCH_SHIFT);
			for (int i = 0; i < BRANCH; i++, h >>>= 1, childOffset += childSize) {
				if ((h & 1) != 0) {
					children[idx++].printSetBits(childOffset);
				}
			}
		}
	}

	// Clear multiple bits from array
	public void clearBits(long[] bits) {
		for (int i = 0; i < bits.length; i++) {
			clearBit(bits[i]);
		}
	}

	// BitProcessor iteration - iterates over bits and calls processor
	public void bitIterator(BitProcessor handler, boolean tv) {
		handler.initBits(this);
		bitIteratorInternal(handler, tv, 0);
		handler.concludeBits();
	}

	private void bitIteratorInternal(BitProcessor handler, boolean tv, long baseOffset) {
		if (depth == 0) {
			long h = itsHeader;
			int idx = 0;
			long bitpos = baseOffset;

			if (tv) {
				// Only iterate set bits
				for (int i = 0; i < BRANCH; i++, h >>>= 1, bitpos += BRANCH) {
					if ((h & 1) != 0) {
						long data = itsBits[idx++];
						handler.preprocess(new Long(bitpos));
						for (int j = 0; j < BRANCH; j++, data >>>= 1) {
							boolean bitValue = (data & 1) != 0;
							if (bitValue) handler.processBit(bitpos + j, bitValue != isInverted);
						}
						handler.postprocess(new Long(bitpos));
					}
				}
			} else {
				// Iterate all bits (including unset)
				for (int i = 0; i < BRANCH; i++, h >>>= 1, bitpos += BRANCH) {
					handler.preprocess(new Long(bitpos));
					if ((h & 1) != 0) {
						long data = itsBits[idx++];
						for (int j = 0; j < BRANCH; j++, data >>>= 1) {
							boolean bitValue = (data & 1) != 0;
							handler.processBit(bitpos + j, bitValue != isInverted);
						}
					} else {
						// All bits in this block are unset (or inverted)
						for (int j = 0; j < BRANCH; j++) {
							handler.processBit(bitpos + j, isInverted);
						}
					}
					handler.postprocess(new Long(bitpos));
				}
			}
		} else {
			long h = itsHeader;
			int idx = 0;
			long childOffset = baseOffset;
			long childSize = 1L << (depth * BRANCH_SHIFT);

			for (int i = 0; i < BRANCH; i++, h >>>= 1, childOffset += childSize) {
				handler.preprocess(new Long(childOffset));
				if ((h & 1) != 0) {
					children[idx++].bitIteratorInternal(handler, tv, childOffset);
				} else if (!tv) {
					// For unset children, process all bits as inverted value
					for (long j = 0; j < childSize; j++) {
						handler.processBit(childOffset + j, isInverted);
					}
				}
				handler.postprocess(new Long(childOffset));
			}
		}
	}
}
