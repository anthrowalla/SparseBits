# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile source code
ant compile

# Build jar file
ant jar

# Run the application
ant run

# Clean build artifacts
ant clean
```

The project uses Apache Ant for building (build.xml). The main class is `Bitmap_Java` which launches the `Frame2` GUI application. The final JAR is created at `dist/Bitnets-030.jar`.

## Architecture

This is a hierarchical bitmap manipulation library for efficient set operations on sparse data. All bitmap classes are in the `org.csac.bits` package.

### Bitmap Hierarchy

#### New Recursive Structure (Recommended)

**RBitmap** (src/org/csac/bits/RBitmap.java) - New recursive bitmap with configurable depth

```java
// Capacity examples (each level multiplies by 64):
new RBitmap(0)  // 4,096 bits       (leaf: 64 blocks × 64 bits)
new RBitmap(1)  // 262,144 bits     (64 × 4,096)
new RBitmap(2)  // 16,777,216 bits   (64 × 262,144)
new RBitmap(3)  // 1,073,741,824 bits
new RBitmap(4)  // 68,719,476,736 bits
new RBitmap(5)  // 4,398,046,511,104 bits
new RBitmap(6)  // 281,474,976,710,656 bits (supports >2.6 billion entities)
```

**Operations:**
- `setBit(bitno)`, `clearBit(bitno)`, `isBitSet(bitno)` - Individual bit manipulation
- `and(other)`, `or(other)`, `xor(other)`, `not()` - Set operations
- `count()` - Count set bits
- `capacity()` - Total bit capacity

**Structure:**
- Depth 0 (leaf): Stores 64 blocks of 64 bits each = 4,096 bits in `long[]`
- Depth N (internal): Array of up to 64 depth-(N-1) nodes
- Sparse: `long itsHeader` indicates which 64 child positions are non-null
- Inverted: `boolean isInverted` for lazy NOT

#### Legacy Fixed Hierarchy (Original)

1. **JBits** - Lowest level (64×64 = 4,096 bits max)
   - Stores bits in a `long[]` array
   - Uses a header (`long`) to indicate which 64-bit blocks are non-zero
   - Supports and, or, xor, not operations

2. **JxBits** - Mid-level (64×4,096 = 262,144 bits max)
   - Array of JBits (up to 64 elements)
   - Header indicates which JBits blocks are non-zero
   - Implements `BitProcessor` for iteration

3. **JBitmap** - Matrix level (64 rows of JxBits)
   - Each row is a JxBits
   - Used for building matrices

4. **JmBitmap** - Higher matrix level (64 rows of JBitmap)
   - Array of JBitmap
   - Supports `collapse()` for reducing dimensions

5. **JxBitmap** - Top level for relations
   - Array of JmBitmap (up to 64)
   - Supports `pipe()` (composition), `filter()`, and `reverse()` operations

All levels support sparse representation through headers and lazy allocation.

### Domain Objects

- **EntityType** - Defines a vocabulary of named entities with bidirectional name-to-ID mapping. Entities are stored in a static registry.

- **Property** - Associates a JxBits with an EntityType. Represents a property that a set of entities may have (e.g., "isRed"). Supports operations like and/or/xor between Properties of the same EntityType.

- **Relation** - A 2D bitmap (JxBitmap) connecting two EntityTypes (rowType and colType). Represents relationships between entities. Supports:
  - `and/or/xor` between Relations of same types
  - `pipe()` - relation composition (A→B composed with B→C = A→C)
  - `filter()` - filter rows by a Property
  - `reverse()` - transpose the relation
  - `maskAnd/maskOr/maskXor()` - reduce using a Property
  - `unionAnd/unionOr/unionXor()` - collapse to a Property

### Key Interfaces

- **BitProcessor** - Interface for iterating over bits with callbacks. Implement `initBits()`, `processBit()`, `preprocess()`, `postprocess()`, `concludeBits()`.
- **Opcode** - Interface for parser operations (incomplete parser implementation)

### Utilities

- **BitHelper** - Iterator for traversing set bits in a JxBits
- **BitVector** - Vector subclass for storing JxBits collections
- **Allocater** - Simple allocator (placeholder)

### Sparse Representation Pattern

All bitmap classes use:
- `long itsHeader` - bitset indicating which blocks are non-null
- Array of child objects (only for non-zero blocks)
- `boolean isInverted` - lazy NOT operation support
- Static accumulator arrays (`bacc1[]`, `bah1`) for operation results

### Naming Conventions

- Internal state fields prefixed with `its` (e.g., `itsHeader`, `itsBits`)
- Bitmap class names: `JBits`, `JxBits`, `JBitmap`, `JmBitmap`, `JxBitmap`
- Constants in UPPER_SNAKE_CASE or prefixed with underscore (e.g., `_nbits`, `_maxbits`)

## GUI

The `Frame2` class is an AWT-based GUI for testing bitmap operations. It creates demo EntityTypes and allows testing Property and Relation operations through mouse clicks.

## Sparse Efficiency

For sparse data (1-2% density at 2.6B scale):
- Only ~26 million actual bits stored vs 2.6B capacity (0.001% storage)
- Memory usage scales with set bits, not total capacity
- Empty branches are single null checks
- Set operations (and/or/xor) skip empty branches efficiently

## Migration to RBitmap

To migrate from the legacy hierarchy to RBitmap:

1. **Replace JxBits:**
   ```java
   // Old:
   JxBits bits = new JxBits();

   // New (similar capacity):
   RBitmap bits = new RBitmap(3);  // ~262K capacity
   RBitmap bits = new RBitmap(6);  // >2.6B capacity
   ```

2. **API Compatibility:** Most operations have the same names and signatures

3. **Performance:** RBitmap uses the same sparse representation principles, so performance characteristics are similar

4. **Testing:** Run `javac -d bin -sourcepath src src/org/csac/bits/RBitmap.java src/TestRBitmap.java && java -cp bin TestRBitmap`
