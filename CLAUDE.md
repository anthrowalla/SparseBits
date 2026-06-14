# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**SparseBits** - Hierarchical bitmap library for efficient set operations on sparse data, supporting billions of entities with configurable depth.

Repository: https://github.com/anthrowalla/SparseBits

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

# Run R2Bitmap tests
javac -d bin -sourcepath src src/org/csac/bits/R2Bitmap.java src/org/csac/bits/RBitmap.java src/org/csac/bits/EntityType.java src/org/csac/bits/BitProcessor.java src/TestR2Bitmap.java && java -cp bin TestR2Bitmap
```

The project uses Apache Ant for building (build.xml). The main class is `Bitmap_Java` which launches the `Frame2` GUI application. The final JAR is created at `dist/Bitnets-030.jar`.

## Architecture

This is a hierarchical bitmap manipulation library for efficient set operations on sparse data. All bitmap classes are in the `org.csac.bits` package.

### Recursive Bitmap Structure (Current)

#### RBitmap - 1D Recursive Bitmap

**File**: `src/org/csac/bits/RBitmap.java`

```java
// Capacity examples (each level multiplies by 64):
new RBitmap(0)  // 4,096 bits       (leaf: 64 blocks × 64 bits)
new RBitmap(1)  // 262,144 bits     (64 × 4,096)
new RBitmap(2)  // 16.7M bits       (64 × 262,144)
new RBitmap(3)  // 1.07B bits       (supports ~1 billion entities)
new RBitmap(6)  // 281T bits        (supports >2.6 billion entities)
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

#### R2Bitmap - 2D Recursive Bitmap (Relations)

**File**: `src/org/csac/bits/R2Bitmap.java`

```java
// Capacity examples for 2D relations (cells = dimension²):
new R2Bitmap(0)  // 64×64 = 4,096 cells       (64 persons)
new R2Bitmap(1)  // 4,096×4,096 = 16.7M cells (4,096 persons)
new R2Bitmap(2)  // 262K×262K = 68.7B cells  (262,144 persons)
new R2Bitmap(3)  // 16.7M×16.7M = 281T cells (16.7M persons)
```

**Operations:**
- `setBit(row, col)`, `clearBit(row, col)`, `isBitSet(row, col)` - Cell manipulation
- `and(other)`, `or(other)`, `xor(other)`, `not()` - Set operations
- `count()` - Count set cells
- `capacity()` - Total cell capacity
- `getDimensionCapacity()` - Entities per dimension

**Structure:**
- Depth 0 (leaf): Single RBitmap stores 64×64 cells
- Depth N (internal): 64×64 grid of R2Bitmap(depth-1) nodes
- Sparse: `headerBlock[64]` tracks which 4,096 positions are non-null
- Inverted: `boolean isInverted` for lazy NOT

**Implemented:**
- Basic operations (setBit, isBitSet, and, or, xor, not, count)
- setBits(long[]) - bulk set multiple columns for a row

**TODO (see TODO.md):**
- getBits(row) - extract row as RBitmap
- readBits(row) - extract row as Property
- setBits(row, Property) - bulk set row from Property
- Advanced Relation operations (pipe, filter, reverse, reduce, collapse, union)

#### Legacy Fixed Hierarchy (Reference Only)

The following classes use fixed-depth hierarchy and are kept for reference:
- **JBits** - Lowest level (64×64 = 4,096 bits)
- **JxBits** - Mid-level (64×4,096 = 262,144 bits)
- **JBitmap** - Matrix level (64 rows of JxBits)
- **JmBitmap** - Higher matrix level with collapse()
- **JxBitmap** - Top level for relations with pipe/filter/reverse

These serve as reference implementations for stubbed methods in R2Bitmap.

### Domain Objects

- **EntityType** - Defines a vocabulary of named entities with bidirectional name-to-ID mapping. Entities are stored in a static registry. Use for organizing person names, concepts, or any entity type.

- **Property** - Associates an RBitmap with an EntityType. Represents a property that a set of entities may have (e.g., "isRed", "isFemale"). Supports operations like and/or/xor between Properties of the same EntityType. Default depth is 3 (~262K capacity).

- **Relation** - A 2D R2Bitmap connecting two EntityTypes (rowType and colType). Represents relationships between entities (e.g., "daughterOf", "livesIn"). Default depth is 2 (262K×262K capacity). Supports:
  - `and/or/xor/not` - Set operations between Relations of same types
  - `setBit/readBit` - Individual cell access
  - `setBits(long[])` - Bulk set multiple columns for a row
  - `copy/not` - Copy and negate operations

**TODO (see TODO.md):**
- `pipe()` - Relation composition (A→B composed with B→C = A→C)
- `filter()` - Filter rows by a Property
- `reverse()` - Transpose the relation (swap rows/columns)
- `reduce()` / `maskAnd/maskOr/maskXor()` - Row-wise operations
- `collapse()` / `unionAnd/Or/Xor()` - Collapse rows to Property

### Key Interfaces

- **BitProcessor** - Interface for iterating over bits with callbacks. Implement `initBits()`, `processBit()`, `preprocess()`, `postprocess()`, `concludeBits()`.
- **Opcode** - Interface for parser operations (incomplete parser implementation)

### Utilities

- **BitHelper** - Iterator for traversing set bits in JxBits (legacy)
- **BitVector** - Vector subclass for storing JxBits collections (legacy)
- **Allocater** - Simple allocator (placeholder)

### Sparse Representation Pattern

All recursive bitmap classes use:
- Branching factor of 64 (based on long word size)
- Header bits indicate which child positions are non-null
- Only allocate children for non-empty branches
- `boolean isInverted` for lazy NOT (no actual bit flipping)

**RBitmap**: `long itsHeader` (64 bits → 64 child positions)
**R2Bitmap**: `long headerBlock[64]` (4,096 bits → 4,096 child positions)

### Naming Conventions

- Internal state fields prefixed with `its` (e.g., `itsHeader`, `itsBits`)
- Recursive classes: `RBitmap` (1D), `R2Bitmap` (2D)
- Legacy classes: `JBits`, `JxBits`, `JBitmap`, `JmBitmap`, `JxBitmap`
- Constants: `BRANCH = 64` (branching factor)

## GUI

The `Frame2` class is an AWT-based GUI for testing bitmap operations. It creates demo EntityTypes and allows testing Property and Relation operations through mouse clicks.

## Sparse Efficiency

For sparse data (1-2% density at 2.6B scale):
- Only ~52 million bits stored vs 281 trillion capacity (0.00002% storage)
- Memory usage scales with set bits, not total capacity
- Empty branches are single null checks
- Set operations (and/or/xor) skip empty branches efficiently

## Implementation Notes

### Critical Bug Fix (RBitmap AND operation)

The AND operation in RBitmap had a bug where `resultHeader |= (1L << resultCount)` was using `resultCount` instead of the original `bitPos`. Fixed to use `(1L << bitPos)` to correctly set header bits at their original positions.

### Recursive Design Principles

1. **Depth-based capacity**: Each level multiplies capacity by 64
2. **Sparse navigation**: Use headers to skip empty branches
3. **Lazy inversion**: `isInverted` flag avoids actual bit flipping
4. **Branching factor**: Fixed at 64 (based on long word size)

## TODO

See `TODO.md` for detailed implementation roadmap for stubbed methods:
- Core Relation operations: pipe, filter, reverse, reduce
- Row operations: getBits, readBits, setBits
- Collapse operations: collapse, unionAnd/Or/Xor
