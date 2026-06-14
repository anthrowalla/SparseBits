// Test the recursive bitmap structure
import org.csac.bits.*;

public class TestRBitmap {
	public static void main(String args[]) {
		System.out.println("Testing RBitmap...");

		// First test: simple set/clear at leaf level
		System.out.println("\n=== Testing leaf level (depth 0) ===");
		RBitmap leaf = new RBitmap(0);
		leaf.setBit(36);
		System.out.println("Set bit 36 in leaf, count: " + leaf.count());
		System.out.println("isBitSet(36): " + leaf.isBitSet(36));
		leaf.clearBit(36);
		System.out.println("After clear, count: " + leaf.count());
		System.out.println("isBitSet(36): " + leaf.isBitSet(36));

		// Test depth 3
		System.out.println("\n=== Testing depth 3 ===");
		RBitmap bm3 = new RBitmap(3);
		System.out.println("Depth 3 capacity: " + bm3.capacity());
		testBasicOps(bm3, 262143L);

		System.out.println("\nAll tests passed!");
	}

	static void testBasicOps(RBitmap bm, long maxBit) {
		// Set some bits
		bm.setBit(0);
		bm.setBit(100);
		bm.setBit(10000);
		bm.setBit(maxBit);

		System.out.println("Set bits: " + bm.count());

		// Check bits
		if (!bm.isBitSet(0)) System.out.println("ERROR: bit 0 should be set");
		if (!bm.isBitSet(100)) System.out.println("ERROR: bit 100 should be set");
		if (!bm.isBitSet(10000)) System.out.println("ERROR: bit 10000 should be set");
		if (!bm.isBitSet(maxBit)) System.out.println("ERROR: bit " + maxBit + " should be set");
		if (bm.isBitSet(50)) System.out.println("ERROR: bit 50 should not be set");

		// Clear a bit
		System.out.println("\nClearing bit 100...");
		bm.clearBit(100);
		System.out.println("After clear: " + bm.count());
		boolean isSet = bm.isBitSet(100);
		System.out.println("isBitSet(100): " + isSet);
		if (isSet) System.out.println("ERROR: bit 100 should be cleared");

		// Test AND
		System.out.println("\n=== Testing AND ===");
		RBitmap bm2 = new RBitmap(3);
		bm2.setBit(0);
		bm2.setBit(5000);

		System.out.println("bm bits: " + bm.count());
		System.out.println("bm2 bits: " + bm2.count());

		RBitmap result = bm.and(bm2, null);
		System.out.println("AND result count: " + result.count());
		if (!result.isBitSet(0)) System.out.println("ERROR: AND should have bit 0");
		if (result.isBitSet(100)) System.out.println("ERROR: AND should not have bit 100");

		// Test OR
		System.out.println("\n=== Testing OR ===");
		result = bm.or(bm2, null);
		System.out.println("OR result count: " + result.count());
		if (!result.isBitSet(5000)) System.out.println("ERROR: OR should have bit 5000");
	}
}
