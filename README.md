# SparseBits

A hierarchical bitmap library for efficient set operations on sparse data. Supports billions of entities with configurable depth while maintaining memory efficiency for sparse datasets.

## Features

- **Scalable Capacity**: From 4,000 to 281 trillion cells via depth parameter
- **Sparse Representation**: Memory scales with set bits, not total capacity
- **Efficient Operations**: AND, OR, XOR, NOT operations skip empty branches
- **Type-Safe**: EntityType system for organizing named entities
- **Relations**: 2D bitmaps for modeling relationships between entity types

## Capacity Examples

### RBitmap (1D)

| Depth | Capacity | Use Case |
|-------|----------|----------|
| 0 | 4,096 bits | Small datasets |
| 1 | 262K bits | Medium datasets |
| 2 | 16.7M bits | Large categories |
| 3 | 1.07B bits | ~1 billion entities |
| 6 | 281T bits | >2.6 billion entities |

### R2Bitmap (2D Relations)

| Depth | Dimension Capacity | Total Cells | Persons (genealogy) |
|-------|-------------------|-------------|---------------------|
| 0 | 64 | 4,096 | Small families |
| 1 | 4,096 | 16.7M | Community level |
| 2 | 262K | 68.7B | Regional studies |
| 3 | 16.7M | 281T | National census |

## Quick Start

```java
import org.csac.bits.*;

// Create an entity type
EntityType person = new EntityType("Person");
person.findName("Alice", true);   // Returns 0
person.findName("Bob", true);     // Returns 1
person.findName("Charlie", true); // Returns 2

// Create a relation (e.g., "childOf")
Relation childOf = new Relation("childOf", person, person, 2);
childOf.setBit("Alice", "Bob");     // Alice is child of Bob
childOf.setBit("Bob", "Charlie");   // Bob is child of Charlie

// Query the relation
boolean aliceIsChildOfBob = childOf.readBit("Alice", "Bob"); // true
long count = childOf.bmap.count(); // 2 relationships

// Set operations
Relation parentOf = childOf.reverse(); // Transpose the relation
```

## Building

```bash
# Compile
ant compile

# Build JAR
ant jar

# Run GUI application
ant run

# Run tests
javac -d bin -sourcepath src src/org/csac/bits/R2Bitmap.java \
    src/org/csac/bits/RBitmap.java src/org/csac/bits/EntityType.java \
    src/org/csac/bits/BitProcessor.java src/TestR2Bitmap.java && \
    java -cp bin TestR2Bitmap
```

## Architecture

### Core Classes

- **RBitmap**: Recursive 1D bitmap with configurable depth
- **R2Bitmap**: Recursive 2D bitmap for relations
- **EntityType**: Named entity vocabulary with bidirectional mapping
- **Property**: Associates a bitmap with an EntityType
- **Relation**: 2D relation between two EntityTypes

### Data Structures

```
RBitmap (depth N):
├── headerBlock[64] - tracks which child positions exist
├── children[] - sparse array of RBitmap(depth N-1)
└── depth 0: long[] bits (4,096 bits)

R2Bitmap (depth N):
├── headerBlock[64] - tracks which child positions exist
├── children[] - sparse array of R2Bitmap(depth N-1)
└── depth 0: RBitmap leaf (stores 64×64 cells)
```

## Performance Characteristics

For sparse data (1-2% density):
- **Memory**: ~0.001% of total capacity (52M bits vs 281T for 2.6B entities)
- **Operations**: O(depth) with early exit on empty branches
- **Set operations**: Skip empty branches efficiently

## Use Cases

- **Genealogy**: Family relationships across millions of persons
- **Knowledge Graphs**: Entity-relationship modeling
- **Sparse Matrices**: Efficient matrix operations
- **Set Operations**: Large-scale intersection/union operations
- **Social Networks**: Friendship/follower relationships

## Status

### ✅ Implemented

- RBitmap: setBit, isBitSet, and, or, xor, not, count
- R2Bitmap: setBit, isBitSet, and, or, xor, not, count, setBits
- Property: Basic operations with RBitmap
- Relation: Basic operations with R2Bitmap

### 🚧 TODO (See [TODO.md](TODO.md))

- Relation operations: pipe, filter, reverse, reduce
- Row operations: getBits, readBits, setBits(row, Property)
- Collapse operations: collapse, unionAnd/Or/Xor

## Legacy Code

The project includes legacy fixed-depth implementations kept for reference:
- JBits, JxBits, JBitmap, JmBitmap, JxBitmap

These serve as reference implementations for stubbed methods.

## License

Copyright 2006-2024 HRAF

## Contributing

When implementing stubbed methods, refer to:
- Legacy JxBitmap/JmBitmap implementations for algorithm reference
- TODO.md for implementation guidance
- Test files for expected behavior patterns
