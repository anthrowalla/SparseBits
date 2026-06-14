# SparseBits - TODO

This file tracks features that still need to be implemented in the recursive R2Bitmap/RBitmap structure.

## Stubbed Methods in Relation.java

The following methods are stubbed with TODO notes and need implementation for the recursive R2Bitmap structure. The legacy JxBitmap implementations may serve as reference.

### Core Operations

#### `pipe(Relation refMap, Relation destBits)`
**Purpose**: Relation composition (A×B composed with B×C = A×C)

**Example**: If `childOf(Person, Person)` and `parentOf(Person, Person)`, then `childOf.pipe(parentOf)` gives `grandchildOf`.

**Reference**: See `JxBitmap.java` pipe() implementation (~line 200-250)

**Key Points**:
- Output columns of `this` must match input rows of `refMap`
- Creates a new Relation with rowType = this.rowType, colType = refMap.colType
- For each set bit (r1, c1) in `this` and (c2, c2) in `refMap` where c1 == c2, set bit (r1, c2) in result

---

#### `filter(Property filterBits, Relation destBits)`
**Purpose**: Filter rows to only those that have the corresponding property bit set

**Example**: Filter a `childOf` relation to only include female children

**Reference**: See `JxBitmap.java` filter() implementation

**Key Points**:
- filterBits must have the same EntityType as this.rowType
- For each row r where filterBits.isBitSet(r) is true, copy that row to dest
- Sparse optimization: skip rows where filter bit is not set

---

#### `reverse(Relation destBits)`
**Purpose**: Transpose the relation (swap rows and columns)

**Example**: `childOf.reverse()` gives `hasChild` (parentOf)

**Reference**: See `JxBitmap.java` reverse() implementation

**Key Points**:
- Creates new Relation with rowType = this.colType, colType = this.rowType
- For each (r, c) set in this, set (c, r) in dest
- Used heavily in genealogy for inverse relationships

---

#### `reduce(Property andBits, Relation destBits, int PRED)`
**Purpose**: Reduce rows using bitwise operations (AND/OR/XOR)

**Parameters**:
- `PRED`: 0=AND, 1=OR, 2=XOR
- `andBits`: Property to AND with each row before reduction
- `destBits`: destination Relation

**Use Case**: Combine multiple conditions or filter rows

**Key Points**:
- maskAnd/maskOr/maskXor are convenience wrappers
- Apply operation row-wise, collapsing to single result per column

---

### Union Operations (Collapse to Property)

#### `unionAnd()`, `unionOr()`, `unionXor()`
**Purpose**: Collapse all rows of a Relation into a single Property

**Example**: From `childOf(Person, Person)`, get all people who are children (union of all columns)

**Reference**: See `JxBitmap.java` union operations

**Implementation Strategy**:
```
unionAnd: For each column, set bit only if ALL rows have it set
unionOr:  For each column, set bit if ANY row has it set
unionXor: For each column, set bit if odd number of rows have it set
```

**Key Points**:
- Returns Property with colType of this Relation
- Empty Relation → empty Property for AND, full for OR

---

## Stubbed Methods in R2Bitmap.java

#### `getBits(long row)`
**Purpose**: Return RBitmap representing all columns set in the given row

**Use Case**: Extract a single row for inspection or processing

**Implementation Strategy**:
1. Navigate to the leaf level for the given row
2. Return the RBitmap at that position (or empty RBitmap if none)
3. Handle sparse case: return empty RBitmap if row doesn't exist

---

#### `readBits(long row)` / `readBits(String posRow)`
**Purpose**: Return Property with all columns set for the given row

**Difference from getBits**: Returns Property (with EntityType) instead of raw RBitmap

**Implementation**: Similar to getBits but wraps result in Property with appropriate EntityType

---

#### `setBits(long row, Property p)` / `setBits(String pos, Property p)`
**Purpose**: Set entire row from a Property

**Use Case**: Bulk row operations, copying rows between relations

**Implementation Strategy**:
1. Clear existing bits in the row
2. Iterate through all set bits in Property p
3. Set each corresponding (row, col) position

---

## Implementation Guidelines

### Recursive Structure Considerations

1. **Always work at the correct depth level**
   - Operations must traverse the full depth tree
   - Don't assume fixed depth - use `this.depth` and `this.treeDepth`

2. **Maintain sparsity**
   - Only create children when needed
   - Remove empty children after operations
   - Use headerBlock to check for existence before navigation

3. **Handle null cases gracefully**
   - null R2Bitmap → empty (no bits set)
   - null Property → empty
   - Return empty results rather than null

4. **Inversion handling**
   - Check `isInverted` flag before operations
   - Use andInverted/orInverted for inverted operands
   - OR trick: A & B = ~(~A | ~B) for inverted AND

### Testing Strategy

For each implementation:
1. Start with depth 0 (64×64 cells)
2. Test depth 1 (4,096×4,096)
3. Test depth 2 (262K×262K)
4. Verify sparse behavior (mostly empty operations)
5. Compare with legacy JxBitmap results if available

Create test files:
- `TestPipe.java` - pipe() operation
- `TestFilter.java` - filter() operation
- `TestReverse.java` - reverse() operation
- `TestReduce.java` - reduce() operations
- `TestUnion.java` - union operations

### Performance Notes

- Sparse operations should skip empty branches early
- Header bit checks are O(1), navigation is O(depth)
- For depth 3 (16.7M×16.7M), operations remain efficient for sparse data
- Consider caching for frequently accessed rows

---

## Legacy Reference Files

The following files contain the original implementations that can serve as reference:

- `JxBitmap.java` - Original 2D bitmap with pipe/filter/reverse
- `JmBitmap.java` - Mid-level matrix operations
- `JBitmap.java` - Matrix-level operations
- `JxBits.java` - Original 1D sparse bitmap

Note: These use fixed-depth hierarchy while new implementation is recursive. Adapt the logic accordingly.

---

## Priority Order

Suggested implementation order:

1. **reverse()** - Simplest, just transpose coordinates
2. **getBits()** / **readBits()** - Single row extraction
3. **setBits(row, Property)** - Bulk row setting
4. **filter()** - Uses getBits/setBits pattern
5. **pipe()** - Core composition, builds on above
6. **reduce()** / **mask** operations - Row-wise operations
7. **union***() - Column-wise aggregation
