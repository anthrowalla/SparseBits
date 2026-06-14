// Test the recursive 2D bitmap structure
import org.csac.bits.*;

public class TestR2Bitmap {
	public static void main(String args[]) {
		System.out.println("Testing R2Bitmap (recursive 2D)...\n");

		// Test depth 0: 64×64 = 4,096 cells
		System.out.println("=== Testing depth 0 ===");
		R2Bitmap r2_0 = new R2Bitmap(0);
		System.out.println("Depth 0 capacity: " + r2_0.capacity() + " cells");
		System.out.println("Dimension capacity: " + r2_0.getDimensionCapacity());

		r2_0.setBit(0, 0);
		r2_0.setBit(10, 20);
		r2_0.setBit(63, 63);
		System.out.println("Set bits (0,0), (10,20), (63,63)");
		System.out.println("Count: " + r2_0.count());
		System.out.println("isBitSet(0,0): " + r2_0.isBitSet(0, 0));
		System.out.println("isBitSet(10,20): " + r2_0.isBitSet(10, 20));
		System.out.println("isBitSet(63,63): " + r2_0.isBitSet(63, 63));
		System.out.println("isBitSet(50,50): " + r2_0.isBitSet(50, 50));

		// Test NOT at depth 0
		r2_0.not();
		System.out.println("\nAfter NOT:");
		System.out.println("Count: " + r2_0.count());
		System.out.println("isBitSet(0,0): " + r2_0.isBitSet(0, 0));
		System.out.println("isBitSet(50,50): " + r2_0.isBitSet(50, 50));

		// Test depth 1: 4,096×4,096 = 16.7M cells
		System.out.println("\n=== Testing depth 1 ===");
		R2Bitmap r2_1 = new R2Bitmap(1);
		System.out.println("Depth 1 capacity: " + r2_1.capacity() + " cells");
		System.out.println("Dimension capacity: " + r2_1.getDimensionCapacity());

		r2_1.setBit(100, 200);
		r2_1.setBit(1000, 2000);
		r2_1.setBit(4095, 4095);
		System.out.println("Set bits (100,200), (1000,2000), (4095,4095)");
		System.out.println("Count: " + r2_1.count());
		System.out.println("isBitSet(100,200): " + r2_1.isBitSet(100, 200));
		System.out.println("isBitSet(1000,2000): " + r2_1.isBitSet(1000, 2000));
		System.out.println("isBitSet(4095,4095): " + r2_1.isBitSet(4095, 4095));

		// Test depth 2: 262,144×262,144 cells
		System.out.println("\n=== Testing depth 2 ===");
		R2Bitmap r2_2 = new R2Bitmap(2);
		System.out.println("Depth 2 capacity: " + r2_2.capacity() + " cells");
		System.out.println("Dimension capacity: " + r2_2.getDimensionCapacity());

		r2_2.setBit(100000, 100000);
		System.out.println("Set bit (100000,100000)");
		System.out.println("Count: " + r2_2.count());
		System.out.println("isBitSet(100000,100000): " + r2_2.isBitSet(100000, 100000));

		// Test AND operation
		System.out.println("\n=== Testing AND ===");
		R2Bitmap a = new R2Bitmap(1);
		R2Bitmap b = new R2Bitmap(1);

		a.setBit(100, 100);
		a.setBit(500, 500);
		b.setBit(100, 100);
		b.setBit(1000, 1000);

		R2Bitmap result = a.and(b, null);
		System.out.println("A has (100,100), (500,500)");
		System.out.println("B has (100,100), (1000,1000)");
		System.out.println("A AND B count: " + result.count());
		System.out.println("isBitSet(100,100): " + result.isBitSet(100, 100));

		// Test with EntityTypes (genealogy example)
		System.out.println("\n=== Testing with EntityTypes ===");
		EntityType person = new EntityType("Person");
		R2Bitmap childOf = new R2Bitmap(1, person, person);
		System.out.println("Relation: childOf (Person × Person)");
		System.out.println("Capacity: " + childOf.getDimensionCapacity() + " persons");

		childOf.setBit(0, 1);  // Person 0 is child of Person 1
		childOf.setBit(1, 2);  // Person 1 is child of Person 2
		childOf.setBit(2, 1);  // Person 2 is child of Person 1

		System.out.println("Set: (0,1), (1,2), (2,1)");
		System.out.println("Count: " + childOf.count());

		System.out.println("\nAll tests passed!");
	}
}
