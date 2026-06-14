// Test integration of RBitmap and R2Bitmap with Property and Relation
import org.csac.bits.*;

public class TestIntegration {
	public static void main(String args[]) {
		System.out.println("Testing RBitmap/R2Bitmap integration with Property/Relation...\n");

		// Test Property with RBitmap
		System.out.println("=== Testing Property with RBitmap ===");
		EntityType personType = new EntityType("Person");
		Property isRed = new Property("IsRed", personType);
		isRed.setBit(0);
		isRed.setBit(10);
		isRed.setBit(100);

		System.out.println("Set bits 0, 10, 100 in IsRed");
		System.out.println("Count: " + isRed.bits.count());
		System.out.println("isBitSet(10): " + isRed.readBit(10));
		System.out.println("isBitSet(50): " + isRed.readBit(50));

		// Test NOT on Property
		isRed.not();
		System.out.println("\nAfter NOT:");
		System.out.println("Count: " + isRed.bits.count());
		System.out.println("isBitSet(10): " + isRed.readBit(10));

		// Test Property AND/OR
		Property isTall = new Property("IsTall", personType);
		isTall.setBit(10);
		isTall.setBit(20);
		isTall.setBit(30);

		Property result = isRed.and(isTall, null);
		System.out.println("\nIsRed AND IsTall:");
		System.out.println("Count: " + result.bits.count());
		System.out.println("isBitSet(10): " + result.readBit(10));

		// Test Relation with R2Bitmap
		System.out.println("\n=== Testing Relation with R2Bitmap ===");
		EntityType colorType = new EntityType("Color");
		Relation likesColor = new Relation("LikesColor", personType, colorType);

		likesColor.setBit(0, 1);  // Person 0 likes color 1
		likesColor.setBit(0, 5);  // Person 0 likes color 5
		likesColor.setBit(10, 2); // Person 10 likes color 2

		System.out.println("Set: (0,1), (0,5), (10,2)");
		System.out.println("isBitSet(0,1): " + likesColor.readBit(0, 1));
		System.out.println("isBitSet(0,5): " + likesColor.readBit(0, 5));
		System.out.println("isBitSet(10,2): " + likesColor.readBit(10, 2));
		System.out.println("isBitSet(5,1): " + likesColor.readBit(5, 1));

		// Test NOT on Relation
		likesColor.not();
		System.out.println("\nAfter NOT:");
		System.out.println("isBitSet(0,1): " + likesColor.readBit(0, 1));
		System.out.println("isBitSet(5,1): " + likesColor.readBit(5, 1));

		// Test Relation AND/OR
		Relation likesColor2 = new Relation("LikesColor2", personType, colorType);
		likesColor2.setBit(0, 1);
		likesColor2.setBit(10, 2);
		likesColor2.setBit(20, 3);

		Relation relResult = likesColor.and(likesColor2, null);
		System.out.println("\nLikesColor AND LikesColor2:");
		System.out.println("isBitSet(0,1): " + relResult.readBit(0, 1));
		System.out.println("isBitSet(10,2): " + relResult.readBit(10, 2));
		System.out.println("isBitSet(20,3): " + relResult.readBit(20, 3));

		// Test getBits
		System.out.println("\n=== Testing getBits ===");
		RBitmap rowBits = likesColor2.bmap.getBits(0);
		System.out.println("Row 0 of LikesColor2: " + rowBits);

		System.out.println("\nAll integration tests passed!");
	}
}
