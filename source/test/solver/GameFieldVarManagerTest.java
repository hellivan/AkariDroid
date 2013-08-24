package solver;

import static org.junit.Assert.*;

import org.junit.Test;

import at.ac.uibk.akari.solver.GameFieldVarManager;

public class GameFieldVarManagerTest {

	@Test
	public void testGetVar() {
		GameFieldVarManager var = new GameFieldVarManager(10, 10);
		assertEquals(var.getVar(0, 0, 0), 2);
		assertEquals(var.getVar(0, 1, 0), 3);
		assertEquals(var.getVar(0, 10, 0), var.falseVar());
		assertEquals(var.getVar(0, 9, 0), 11);
		assertEquals(var.getVar(0, 0, 1), 12);

		assertEquals(var.getVar(0, 0, 10), var.falseVar());

		assertEquals(var.getVar(0, 9, 9) + 1, var.getVar(1, 0, 0));

	}

	@Test
	public void testReverseVarBlock() {
		GameFieldVarManager var = new GameFieldVarManager(10, 10);
		
		int count=2;
		for (int i = 0; i < 10; i++) {
			for (int j2 = 0; j2 < 10; j2++) {
				for (int j = 0; j < 10; j++) {
					
//					System.out.print(var.reverseVarPoint(var.getVar(i, j, j2)).x+" ");
//					System.out.println(var.reverseVarPoint(var.getVar(i, j, j2)).y);
//					System.out.println(var.getVar(i, j, j2));
					
					assertEquals(var.getVar(i, j, j2), count);
					
					assertEquals(var.reverseVarBlock(var.getVar(i, j, j2)), i);
					assertEquals(var.reverseVarPoint(var.getVar(i, j, j2)).x, j);
					assertEquals(var.reverseVarPoint(var.getVar(i, j, j2)).y, j2);
					
					count++;
				}
			}
		}
	}
}
