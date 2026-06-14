package org.csac.bits;

// R2Bitmap - Recursive 2D bitmap for Relations
// Each level creates a 64×64 grid of child R2Bitmaps
// At depth 0 (leaf): stores a single 64×64 RBitmap
// At depth N (internal): 64×64 grid of R2Bitmap(depth-1) nodes
//
// Capacity: (64^(depth+1))^2 cells
// Depth 0: 4,096 cells (64×64)
// Depth 1: 16.7M cells (4,096×4,096)
// Depth 2: 68.7B cells (262,144×262,144)
// Depth 3: 281 trillion cells (16.7M×16.7M) - enough for 2.6B entities
//
// Sparse representation using a 1D array of 64×64 child positions
// with a header indicating which positions are non-null

public class R2Bitmap {
	// Configuration - same for all instances in a tree
	public static final int BRANCH_SHIFT = 6; // log2(64) = 6 bits per level
	public static final int BRANCH = 64; // branching factor

	// Sparse storage: header is a 4096-bit (64×64) indicator stored in 64 longs
	// But for efficiency, we use a single long with 64 bits for now
	// and store children in a flat array indexed by (rowBlock * 64 + colBlock)
	long[] headerBlock; // 64 longs, each indicating 64 child positions (4,096 total)
	R2Bitmap[] children; // flat array of non-null children
	boolean isInverted = false;

	// At depth 0 (leaf), we store a single RBitmap for the 64×64 cell
	RBitmap leafBitmap;

	// Row and column EntityTypes (optional, for semantic tracking)
	EntityType rowType = null;
	EntityType colType = null;

	int depth; // 0 = leaf (stores RBitmap), 1+ = internal (stores children)
	int treeDepth; // total depth of tree

	// Operation constants
	static final int AND = 0;
	static final int OR = 1;
	static final int XOR = 2;

	// Root constructor - creates empty 2D bitmap
	public R2Bitmap(int depth) {
		this(depth, depth);
	}

	// Root constructor - creates empty 2D bitmap with explicit depth
	public R2Bitmap(int depth, int treeDepth) {
		this.depth = depth;
		this.treeDepth = treeDepth;
		clear();
	}

	// Constructor with EntityTypes
	public R2Bitmap(int depth, EntityType rowType, EntityType colType) {
		this(depth, depth, rowType, colType);
	}

	// Constructor with EntityTypes and explicit tree depth
	public R2Bitmap(int depth, int treeDepth, EntityType rowType, EntityType colType) {
		this.depth = depth;
		this.treeDepth = treeDepth;
		this.rowType = rowType;
		this.colType = colType;
		clear();
	}

	// Total capacity (number of cells)
	public long capacity() {
		long cells = BRANCH * BRANCH; // leaf cell capacity = 4,096
		for (int i = 0; i < treeDepth; i++) {
			cells *= BRANCH * BRANCH;
		}
		return cells;
	}

	// Row/column capacity (same for both - square relation)
	public long getDimensionCapacity() {
		long dim = BRANCH; // base dimension
		for (int i = 0; i < treeDepth; i++) {
			dim *= BRANCH;
		}
		return dim;
	}

	// Get row EntityType
	public EntityType getRowType() {
		return rowType;
	}

	// Set row EntityType
	public void setRowType(EntityType rowType) {
		this.rowType = rowType;
	}

	// Get column EntityType
	public EntityType getColType() {
		return colType;
	}

	// Set column EntityType
	public void setColType(EntityType colType) {
		this.colType = colType;
	}

	// Clear all data
	public void clear() {
		headerBlock = null;
		children = null;
		leafBitmap = null;
		isInverted = false;
	}

	// Calculate child index from row and column block positions
	private int getChildIndex(int rowBlock, int colBlock) {
		return rowBlock * BRANCH + colBlock;
	}

	// Check if a bit is set
	public boolean isBitSet(long row, long col) {
		long dimCap = getDimensionCapacity();
		if (row < 0 || row >= dimCap || col < 0 || col >= dimCap) {
			return false;
		}

		if (isInverted) {
			// If inverted, return opposite of actual bit
			return !isBitSetInternal(row, col);
		}
		return isBitSetInternal(row, col);
	}

	private boolean isBitSetInternal(long row, long col) {
		if (depth == 0) {
			// Leaf level - check in the RBitmap
			if (leafBitmap == null) {
				return false;
			}
			return leafBitmap.isBitSet(row * BRANCH + col);
		}

		// Calculate block size for this level
		// At depth 0: blockSize = 1 (each cell is 1×1)
		// At depth 1: blockSize = 64 (each child covers 64×64 cells)
		// At depth 2: blockSize = 4,096 (each child covers 4,096×4,096 cells)
		long blockSize = 1L;
		for (int i = 0; i < depth; i++) {
			blockSize *= BRANCH;
		}

		int rowBlock = (int) (row / blockSize);
		int colBlock = (int) (col / blockSize);

		// Check if child exists
		if (headerBlock == null) {
			return false;
		}

		int blockIndex = rowBlock * BRANCH + colBlock;
		int longIndex = blockIndex / 64;
		int bitIndex = blockIndex % 64;

		if ((headerBlock[longIndex] & (1L << bitIndex)) == 0) {
			return false;
		}

		// Find the child in the sparse array
		int childPos = countHeaderBitsBefore(blockIndex);
		R2Bitmap child = children[childPos];

		// Recurse with remaining coordinates
		long remainingRow = row % blockSize;
		long remainingCol = col % blockSize;
		return child.isBitSetInternal(remainingRow, remainingCol);
	}

	// Count total set bits in header
	private int countHeaderBits() {
		if (headerBlock == null) return 0;
		int count = 0;
		for (int i = 0; i < 64; i++) {
			count += Long.bitCount(headerBlock[i]);
		}
		return count;
	}

	// Count set bits in header before a given position
	private int countHeaderBitsBefore(int position) {
		if (headerBlock == null) return 0;
		int count = 0;
		for (int i = 0; i < position; i++) {
			int longIdx = i / 64;
			int bitIdx = i % 64;
			if ((headerBlock[longIdx] & (1L << bitIdx)) != 0) {
				count++;
			}
		}
		return count;
	}

	// Set or clear a bit
	public void setBit(long row, long col, boolean value) {
		long dimCap = getDimensionCapacity();
		if (row < 0 || row >= dimCap || col < 0 || col >= dimCap) {
			return;
		}

		if (isInverted) value = !value;

		if (depth == 0) {
			setBitLeaf(row, col, value);
		} else {
			setBitInternal(row, col, value);
		}
	}

	public void setBit(long row, long col) {
		setBit(row, col, true);
	}

	public void setBits(long bits[]) {
		// Set multiple [row, col, col, ...] pairs
		// Format: bits[0] = row, bits[1] = col1, bits[2] = col2, etc.
		if (bits.length < 2) return;
		long row = bits[0];
		for (int i = 1; i < bits.length; i++) {
			setBit(row, bits[i], true);
		}
	}

	public RBitmap getBits(long row) {
		// Return an RBitmap representing all columns set in the given row
		// TODO: Implement for recursive R2Bitmap structure
		// For now, return an empty RBitmap
		System.out.println("getBits(row) not yet implemented for recursive R2Bitmap");
		return new RBitmap(0);
	}

	private void setBitLeaf(long row, long col, boolean value) {
		long bitIndex = row * BRANCH + col;

		if (leafBitmap == null) {
			if (value) {
				leafBitmap = new RBitmap(0);
				leafBitmap.setBit(bitIndex, true);
			}
			return;
		}

		leafBitmap.setBit(bitIndex, value);

		// Remove leaf if it became empty
		if (!value && leafBitmap.itsHeader == 0) {
			leafBitmap = null;
		}
	}

	private void setBitInternal(long row, long col, boolean value) {
		// Calculate block size for this level
		long blockSize = 1L;
		for (int i = 0; i < depth; i++) {
			blockSize *= BRANCH;
		}

		int rowBlock = (int) (row / blockSize);
		int colBlock = (int) (col / blockSize);
		int blockIndex = rowBlock * BRANCH + colBlock;

		// Initialize header if needed
		if (headerBlock == null) {
			if (value) {
				headerBlock = new long[64]; // 64 longs for 4,096 positions
				children = new R2Bitmap[1];
				children[0] = new R2Bitmap(depth - 1, treeDepth, rowType, colType);
				children[0].setBit(row % blockSize, col % blockSize, true);

				int longIndex = blockIndex / 64;
				int bitIndex = blockIndex % 64;
				headerBlock[longIndex] |= (1L << bitIndex);
			}
			return;
		}

		int longIndex = blockIndex / 64;
		int bitIndex = blockIndex % 64;
		long bitMask = 1L << bitIndex;

		if ((headerBlock[longIndex] & bitMask) != 0) {
			// Child exists
			int childPos = countHeaderBitsBefore(blockIndex);
			R2Bitmap child = children[childPos];
			child.setBit(row % blockSize, col % blockSize, value);

			// Remove empty child
			if (!value && child.isEmpty()) {
				int newLen = children.length - 1;
				if (newLen == 0) {
					children = null;
					headerBlock = null;
				} else {
					R2Bitmap[] newChildren = new R2Bitmap[newLen];
					System.arraycopy(children, 0, newChildren, 0, childPos);
					System.arraycopy(children, childPos + 1, newChildren, childPos, newLen - childPos);
					children = newChildren;
					headerBlock[longIndex] &= ~bitMask;

					// Check if entire headerBlock is empty
					boolean hasBits = false;
					for (int i = 0; i < 64; i++) {
						if (headerBlock[i] != 0) {
							hasBits = true;
							break;
						}
					}
					if (!hasBits) {
						headerBlock = null;
					}
				}
			}
		} else if (value) {
			// Need to add new child
			int pos = countHeaderBits();
			R2Bitmap[] newChildren = new R2Bitmap[pos + 1];
			int insertAt = countHeaderBitsBefore(blockIndex);
			if (children != null) {
				System.arraycopy(children, 0, newChildren, 0, insertAt);
				System.arraycopy(children, insertAt, newChildren, insertAt + 1, pos - insertAt);
			}
			R2Bitmap newChild = new R2Bitmap(depth - 1, treeDepth, rowType, colType);
			newChild.setBit(row % blockSize, col % blockSize, true);
			newChildren[insertAt] = newChild;
			children = newChildren;
			headerBlock[longIndex] |= bitMask;
		}
	}

	// Check if this node is empty
	public boolean isEmpty() {
		if (depth == 0) {
			return leafBitmap == null || leafBitmap.itsHeader == 0;
		}
		return headerBlock == null;
	}

	// NOT operation
	public R2Bitmap not() {
		isInverted = !isInverted;
		return this;
	}

	// Copy this bitmap
	public R2Bitmap copy() {
		R2Bitmap result = new R2Bitmap(depth, treeDepth, rowType, colType);
		result.isInverted = isInverted;

		if (depth == 0 && leafBitmap != null) {
			result.leafBitmap = leafBitmap.copy();
		} else if (headerBlock != null) {
			result.headerBlock = new long[64];
			System.arraycopy(headerBlock, 0, result.headerBlock, 0, 64);
			result.children = new R2Bitmap[children.length];
			for (int i = 0; i < children.length; i++) {
				result.children[i] = children[i].copy();
			}
		}

		return result;
	}

	// Get inverted state
	public boolean getInverted() {
		return isInverted;
	}

	// Count total set bits
	public long count() {
		if (isInverted) {
			return capacity() - countInternal();
		}
		return countInternal();
	}

	private long countInternal() {
		if (depth == 0) {
			if (leafBitmap == null) return 0;
			return leafBitmap.count();
		}

		long total = 0;
		if (headerBlock != null && children != null) {
			for (int i = 0; i < children.length; i++) {
				total += children[i].count();
			}
		}
		return total;
	}

	// AND operation
	public R2Bitmap and(R2Bitmap other, R2Bitmap dest) {
		if (dest == null) dest = new R2Bitmap(depth, treeDepth, rowType, colType);
		if (dest != this && dest != other) dest.clear();

		// Handle inversion
		if (isInverted || other.isInverted) {
			return andInverted(other, dest);
		}

		if (depth == 0) {
			andLeaf(other, dest);
		} else {
			andInternal(other, dest);
		}

		return dest;
	}

	private void andLeaf(R2Bitmap other, R2Bitmap dest) {
		if (this.leafBitmap == null && other.leafBitmap == null) {
			return;
		}
		if (this.leafBitmap == null) {
			return;
		}
		if (other.leafBitmap == null) {
			return;
		}

		dest.leafBitmap = this.leafBitmap.and(other.leafBitmap, null);
		if (dest.leafBitmap.itsHeader == 0) {
			dest.leafBitmap = null;
		}
	}

	private void andInternal(R2Bitmap other, R2Bitmap dest) {
		if (this.headerBlock == null || other.headerBlock == null) {
			return;
		}

		// Process all 4,096 possible child positions
		for (int blockIndex = 0; blockIndex < BRANCH * BRANCH; blockIndex++) {
			int longIdx = blockIndex / 64;
			int bitIdx = blockIndex % 64;
			long bitMask = 1L << bitIdx;

			boolean inThis = (this.headerBlock[longIdx] & bitMask) != 0;
			boolean inOther = (other.headerBlock[longIdx] & bitMask) != 0;

			if (inThis && inOther) {
				int thisPos = this.countHeaderBitsBefore(blockIndex);
				int otherPos = other.countHeaderBitsBefore(blockIndex);

				R2Bitmap resultChild = this.children[thisPos].and(other.children[otherPos], null);
				if (resultChild != null && !resultChild.isEmpty()) {
					dest.addChild(blockIndex, resultChild);
				}
			}
		}
	}

	private void addChild(int blockIndex, R2Bitmap child) {
		// Count current children BEFORE adding
		int pos = this.countHeaderBits();

		if (this.headerBlock == null) {
			this.headerBlock = new long[64];
		}

		int longIdx = blockIndex / 64;
		int bitIdx = blockIndex % 64;
		this.headerBlock[longIdx] |= (1L << bitIdx);

		if (this.children == null) {
			this.children = new R2Bitmap[1];
		} else {
			R2Bitmap[] newChildren = new R2Bitmap[pos + 1];
			System.arraycopy(this.children, 0, newChildren, 0, pos);
			this.children = newChildren;
		}
		this.children[pos] = child;
	}

	private R2Bitmap andInverted(R2Bitmap other, R2Bitmap dest) {
		// Simplified inversion handling
		R2Bitmap temp1 = this.copy();
		R2Bitmap temp2 = other.copy();
		temp1.isInverted = false;
		temp2.isInverted = false;

		if (isInverted && other.isInverted) {
			// !A & !B = !(A | B)
			R2Bitmap result = temp1.or(temp2, dest);
			result.not();
			return result;
		} else if (isInverted) {
			// !A & B = B - A
			return subtractHelper(temp2, temp1, dest);
		} else {
			// A & !B = A - B
			return subtractHelper(temp1, temp2, dest);
		}
	}

	private R2Bitmap subtractHelper(R2Bitmap a, R2Bitmap b, R2Bitmap dest) {
		// A - B = A & !B
		// This is complex for 2D, use: A - B = A ^ (A & B)
		// Actually simpler: (A | B) ^ B for set difference
		R2Bitmap aOrB = a.or(b, null);
		return aOrB.xor(b, dest);
	}

	public R2Bitmap and(R2Bitmap other) {
		return and(other, this);
	}

	// OR operation
	public R2Bitmap or(R2Bitmap other, R2Bitmap dest) {
		if (dest == null) dest = new R2Bitmap(depth, treeDepth, rowType, colType);
		if (dest != this && dest != other) dest.clear();

		if (isInverted || other.isInverted) {
			return orInverted(other, dest);
		}

		if (depth == 0) {
			orLeaf(other, dest);
		} else {
			orInternal(other, dest);
		}

		return dest;
	}

	private void orLeaf(R2Bitmap other, R2Bitmap dest) {
		if (this.leafBitmap == null && other.leafBitmap == null) {
			return;
		}
		if (this.leafBitmap == null) {
			dest.leafBitmap = other.leafBitmap.copy();
			return;
		}
		if (other.leafBitmap == null) {
			dest.leafBitmap = this.leafBitmap.copy();
			return;
		}

		dest.leafBitmap = this.leafBitmap.or(other.leafBitmap, null);
	}

	private void orInternal(R2Bitmap other, R2Bitmap dest) {
		// Process all 4,096 possible child positions
		for (int blockIndex = 0; blockIndex < BRANCH * BRANCH; blockIndex++) {
			int longIdx = blockIndex / 64;
			int bitIdx = blockIndex % 64;
			long bitMask = 1L << bitIdx;

			boolean inThis = this.headerBlock != null && (this.headerBlock[longIdx] & bitMask) != 0;
			boolean inOther = other.headerBlock != null && (other.headerBlock[longIdx] & bitMask) != 0;

			if (inThis && inOther) {
				int thisPos = this.countHeaderBitsBefore(blockIndex);
				int otherPos = other.countHeaderBitsBefore(blockIndex);

				R2Bitmap resultChild = this.children[thisPos].or(other.children[otherPos], null);
				if (resultChild != null && !resultChild.isEmpty()) {
					dest.addChild(blockIndex, resultChild);
				}
			} else if (inThis) {
				int thisPos = this.countHeaderBitsBefore(blockIndex);
				dest.addChild(blockIndex, this.children[thisPos].copy());
			} else if (inOther) {
				int otherPos = other.countHeaderBitsBefore(blockIndex);
				dest.addChild(blockIndex, other.children[otherPos].copy());
			}
		}
	}

	private R2Bitmap orInverted(R2Bitmap other, R2Bitmap dest) {
		R2Bitmap temp1 = this.copy();
		R2Bitmap temp2 = other.copy();
		temp1.isInverted = false;
		temp2.isInverted = false;

		if (isInverted && other.isInverted) {
			// !A | !B = !(A & B)
			R2Bitmap result = temp1.and(temp2, dest);
			result.not();
			return result;
		} else if (isInverted) {
			// !A | B = !(A & !B)
			R2Bitmap result = temp1.and(temp2, dest);
			result.not();
			return result;
		} else {
			// A | !B = !(!A & B)
			R2Bitmap result = temp1.and(temp2, dest);
			result.not();
			return result;
		}
	}

	public R2Bitmap or(R2Bitmap other) {
		return or(other, this);
	}

	// XOR operation
	public R2Bitmap xor(R2Bitmap other, R2Bitmap dest) {
		if (dest == null) dest = new R2Bitmap(depth, treeDepth, rowType, colType);
		if (dest != this && dest != other) dest.clear();

		if (isInverted || other.isInverted) {
			return xorInverted(other, dest);
		}

		if (depth == 0) {
			xorLeaf(other, dest);
		} else {
			xorInternal(other, dest);
		}

		return dest;
	}

	private void xorLeaf(R2Bitmap other, R2Bitmap dest) {
		if (this.leafBitmap == null && other.leafBitmap == null) {
			return;
		}
		if (this.leafBitmap == null) {
			dest.leafBitmap = other.leafBitmap.copy();
			return;
		}
		if (other.leafBitmap == null) {
			dest.leafBitmap = this.leafBitmap.copy();
			return;
		}

		dest.leafBitmap = this.leafBitmap.xor(other.leafBitmap, null);
		if (dest.leafBitmap.itsHeader == 0) {
			dest.leafBitmap = null;
		}
	}

	private void xorInternal(R2Bitmap other, R2Bitmap dest) {
		// Similar to OR but use XOR for children in both positions
		for (int blockIndex = 0; blockIndex < BRANCH * BRANCH; blockIndex++) {
			int longIdx = blockIndex / 64;
			int bitIdx = blockIndex % 64;
			long bitMask = 1L << bitIdx;

			boolean inThis = this.headerBlock != null && (this.headerBlock[longIdx] & bitMask) != 0;
			boolean inOther = other.headerBlock != null && (other.headerBlock[longIdx] & bitMask) != 0;

			if (inThis && inOther) {
				int thisPos = this.countHeaderBitsBefore(blockIndex);
				int otherPos = other.countHeaderBitsBefore(blockIndex);

				R2Bitmap resultChild = this.children[thisPos].xor(other.children[otherPos], null);
				if (resultChild != null && !resultChild.isEmpty()) {
					dest.addChild(blockIndex, resultChild);
				}
			} else if (inThis) {
				int thisPos = this.countHeaderBitsBefore(blockIndex);
				dest.addChild(blockIndex, this.children[thisPos].copy());
			} else if (inOther) {
				int otherPos = other.countHeaderBitsBefore(blockIndex);
				dest.addChild(blockIndex, other.children[otherPos].copy());
			}
		}
	}

	private R2Bitmap xorInverted(R2Bitmap other, R2Bitmap dest) {
		R2Bitmap temp1 = this.copy();
		R2Bitmap temp2 = other.copy();
		temp1.isInverted = false;
		temp2.isInverted = false;

		if (isInverted && other.isInverted) {
			// !A ^ !B = A ^ B
			return temp1.xor(temp2, dest);
		} else if (isInverted) {
			// !A ^ B = !(A ^ B)
			R2Bitmap result = temp1.xor(temp2, dest);
			result.not();
			return result;
		} else {
			// A ^ !B = !(A ^ B)
			R2Bitmap result = temp1.xor(temp2, dest);
			result.not();
			return result;
		}
	}

	public R2Bitmap xor(R2Bitmap other) {
		return xor(other, this);
	}

	// String representation
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("R2Bitmap[depth=").append(depth);
		sb.append(", capacity=").append(capacity());
		sb.append(", bits=").append(count());
		if (rowType != null && colType != null) {
			sb.append(", ").append(rowType.getTypeName()).append(":").append(colType.getTypeName());
		}
		if (isInverted) sb.append(", inverted");
		sb.append("]");
		return sb.toString();
	}
}
