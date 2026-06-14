import org.csac.bits.*;

public class TestNot {
	public static void main(String args[]) {
		System.out.println("Testing NOT operation...");

		// Test at leaf level
		RBitmap leaf = new RBitmap(0);
		leaf.setBit(10);
		leaf.setBit(20);
		leaf.setBit(30);
		System.out.println("Leaf: Set bits 10,20,30");
		System.out.println("Count: " + leaf.count());
		System.out.println("isBitSet(10): " + leaf.isBitSet(10));
		System.out.println("isBitSet(40): " + leaf.isBitSet(40));

		// NOT the leaf
		leaf.not();
		System.out.println("\nAfter NOT:");
		System.out.println("Count: " + leaf.count());
		System.out.println("isBitSet(10): " + leaf.isBitSet(10));
		System.out.println("isBitSet(40): " + leaf.isBitSet(40));

		// NOT again to get back
		leaf.not();
		System.out.println("\nAfter second NOT:");
		System.out.println("Count: " + leaf.count());
		System.out.println("isBitSet(10): " + leaf.isBitSet(10));
		System.out.println("isBitSet(40): " + leaf.isBitSet(40));

		// Test at depth 3
		System.out.println("\n=== Testing NOT at depth 3 ===");
		RBitmap bm = new RBitmap(3);
		bm.setBit(0);
		bm.setBit(100);
		bm.setBit(10000);
		System.out.println("Set bits: 0, 100, 10000");
		System.out.println("Count: " + bm.count());
		System.out.println("isBitSet(0): " + bm.isBitSet(0));
		System.out.println("isBitSet(100): " + bm.isBitSet(100));
		System.out.println("isBitSet(50000): " + bm.isBitSet(50000));

		bm.not();
		System.out.println("\nAfter NOT:");
		System.out.println("Count: " + bm.count());
		System.out.println("isBitSet(0): " + bm.isBitSet(0));
		System.out.println("isBitSet(100): " + bm.isBitSet(100));
		System.out.println("isBitSet(50000): " + bm.isBitSet(50000));

		// Test NOT with AND
		System.out.println("\n=== Testing NOT with AND ===");
		RBitmap bm1 = new RBitmap(3);
		bm1.setBit(10);
		bm1.setBit(20);
		bm1.setBit(30);
		
		RBitmap bm2 = new RBitmap(3);
		bm2.setBit(20);
		bm2.setBit(30);
		bm2.setBit(40);
		
		System.out.println("bm1: 10,20,30");
		System.out.println("bm2: 20,30,40");
		
		RBitmap andResult = bm1.and(bm2, null);
		System.out.println("AND result (20,30): count = " + andResult.count());
		
		RBitmap notAnd = bm1.not().and(bm2, null);
		System.out.println("NOT(bm1).AND(bm2): count = " + notAnd.count());
		System.out.println("Expected: ~{10,20,30} AND {20,30,40} = {40}");
		System.out.println("isBitSet(40): " + notAnd.isBitSet(40));
		
		if (notAnd.count() == 1 && notAnd.isBitSet(40)) {
			System.out.println("✓ NOT with AND works correctly");
		} else {
			System.out.println("✗ NOT with AND failed!");
		}
	}
}
