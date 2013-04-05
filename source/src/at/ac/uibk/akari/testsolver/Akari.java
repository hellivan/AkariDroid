package at.ac.uibk.akari.testsolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Akari {
	
	int	width;
	int	height;
	
	enum AkariTypes {
		EMPTY, LAMB, BLOCK, BLOCK0, BLOCK1, BLOCK2, BLOCK3, BLOCK4
	}
	
	AkariTypes[][]	field;
	
	
	public Akari() throws ContradictionException, TimeoutException {
		
		String[] akari = { "--------------------", "-   1      1       -", "- 1 -  1-1 - 1---- -", "- ---  -   - -   - -", "-      ----- - 1-- -", "-1---1       1     -", "-     1       1---1-", "- -1- - --1--      -", "- -   - -   -  --1 -", "- 1---- - --1  1 - -", "-       1      -   -", "--------------------" };
		
		System.out.println("len:" + akari.length);
		
		this.width = akari[0].length();
		this.height = akari.length;
		
		field = new AkariTypes[width][height];
		int i, j = 0;
		for (i = 0; i < width; i++) {
			
			for (j = 0; j < height; j++) {
				
				switch (akari[j].charAt(i)) {
					case '-':
						field[i][j] = AkariTypes.BLOCK;
						break;
					
					case '1':
						field[i][j] = AkariTypes.BLOCK1;
						break;
					
					case ' ':
						field[i][j] = AkariTypes.EMPTY;
						break;
					
					default:
						break;
				}
			}
		}
		
		this.solve();
		
	}
	
	public int lightAt(int x, int y) {
		
		if (x >= width || y >= height || x < 0 || y < 0)
			System.out.println("Error" + x + " " + y);
		
		return lambAt(width - 1, height - 1) + x + y * width;
		
	}
	
	public int revlambtAtX(int x) {
		
		if (x > lambAt(width - 1, height - 1))
			return 0;
		
		return (x - 1) % width;
	}
	
	public int revlambAtY(int x) {
		
		if (x > lambAt(width - 1, height - 1))
			return 0;
		
		return (x - 1) / width;
	}
	
	public int lambAt(int x, int y) {
		return x + y * width + 1;
	}
	
	public void solve() throws ContradictionException, TimeoutException {
		final int MAXVAR = lightAt(width - 1, height - 1);
		final int NBCLAUSES = 500000;
		
		ISolver solver = SolverFactory.newDefault();
		
		solver.setTimeout(60);
		
		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
		
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
				
				if (field[i][j] == AkariTypes.EMPTY) {
					
					// every field lighted or a lamb
					solver.addClause(new VecInt(new int[] { lambAt(i, j), lightAt(i, j) }));
					
					// not both: lighted and a lamb
					solver.addClause(new VecInt(new int[] { -lambAt(i, j), -lightAt(i, j) }));
				} else {
					
					// blocks cannot be a lamb
					solver.addClause(new VecInt(new int[] { -lambAt(i, j) }));
					
					// blocks cannot be lighted
					// solver.addClause(new VecInt(new int[]{ -lightAt(i, j)
					// }));
				}
				
				if (field[i][j] == AkariTypes.EMPTY) {
					
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(-lightAt(i, j));
					
					int k = j + 1;
					while (field[i][k] == AkariTypes.EMPTY) {
						solver.addClause(new VecInt(new int[] { -lambAt(i, j), lightAt(i, k) }));
						
						list.add(lambAt(i, k));
						k++;
					}
					
					k = j - 1;
					while (field[i][k] == AkariTypes.EMPTY) {
						solver.addClause(new VecInt(new int[] { -lambAt(i, j), lightAt(i, k) }));
						list.add(lambAt(i, k));
						k--;
					}
					
					k = i + 1;
					while (field[k][j] == AkariTypes.EMPTY) {
						solver.addClause(new VecInt(new int[] { -lambAt(i, j), lightAt(k, j) }));
						list.add(lambAt(k, j));
						k++;
					}
					
					k = i - 1;
					while (field[k][j] == AkariTypes.EMPTY) {
						
						solver.addClause(new VecInt(new int[] { -lambAt(i, j), lightAt(k, j) }));
						list.add(lambAt(k, j));
						k--;
					}
					
					solver.addClause(new VecInt(toIntArray(list)));
					
				}
				
				if (field[i][j] == AkariTypes.BLOCK0) {
					int[] clause = { -lambAt(i + 1, j) };
					solver.addClause(new VecInt(clause));
					
					int[] clause1 = { -lambAt(i, j + 1) };
					solver.addClause(new VecInt(clause1));
					
					int[] clause2 = { -lambAt(i - 1, j) };
					solver.addClause(new VecInt(clause2));
					
					int[] clause3 = { -lambAt(i, j - 1) };
					solver.addClause(new VecInt(clause3));
				}
				
				if (field[i][j] == AkariTypes.BLOCK4) {
					int[] clause = { lambAt(i + 1, j) };
					solver.addClause(new VecInt(clause));
					
					int[] clause1 = { lambAt(i, j + 1) };
					solver.addClause(new VecInt(clause1));
					
					int[] clause2 = { lambAt(i - 1, j) };
					solver.addClause(new VecInt(clause2));
					
					int[] clause3 = { lambAt(i, j - 1) };
					solver.addClause(new VecInt(clause3));
				}
				
				if (field[i][j] == AkariTypes.BLOCK1) {
					int[] clause = { lambAt(i + 1, j), lambAt(i - 1, j), lambAt(i, j + 1), lambAt(i, j - 1) };
					solver.addClause(new VecInt(clause));
					
					solver.addClause(new VecInt(new int[] { -lambAt(i + 1, j), -lambAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i + 1, j), -lambAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i + 1, j), -lambAt(i, j - 1) }));
					
					solver.addClause(new VecInt(new int[] { -lambAt(i - 1, j), -lambAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i - 1, j), -lambAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i - 1, j), -lambAt(i, j - 1) }));
					
					solver.addClause(new VecInt(new int[] { -lambAt(i, j + 1), -lambAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i, j + 1), -lambAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i, j + 1), -lambAt(i, j - 1) }));
					
					solver.addClause(new VecInt(new int[] { -lambAt(i, j - 1), -lambAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i, j - 1), -lambAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { -lambAt(i, j - 1), -lambAt(i, j + 1) }));
				}
				
				if (field[i][j] == AkariTypes.BLOCK3) {
					
					int[] clause = { -lambAt(i + 1, j), -lambAt(i - 1, j), -lambAt(i, j + 1), -lambAt(i, j - 1) };
					solver.addClause(new VecInt(clause));
					
					solver.addClause(new VecInt(new int[] { lambAt(i + 1, j), lambAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { lambAt(i + 1, j), lambAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { lambAt(i + 1, j), lambAt(i, j - 1) }));
					
					solver.addClause(new VecInt(new int[] { lambAt(i - 1, j), lambAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { lambAt(i - 1, j), lambAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { lambAt(i - 1, j), lambAt(i, j - 1) }));
					
					solver.addClause(new VecInt(new int[] { lambAt(i, j + 1), lambAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { lambAt(i, j + 1), lambAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { lambAt(i, j + 1), lambAt(i, j - 1) }));
					
					solver.addClause(new VecInt(new int[] { lambAt(i, j - 1), lambAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { lambAt(i, j - 1), lambAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { lambAt(i, j - 1), lambAt(i, j + 1) }));
					
				}
				
				if (field[i][j] == AkariTypes.BLOCK2) {
					// int[] clause = {-lambAt(i+1, j),-lambAt(i-1,
					// j),-lambAt(i, j+1),-lambAt(i, j-1)};
					// solver.addClause(new VecInt(clause));
					//
					//
					// solver.addClause(new VecInt(new int[]{lambAt(i+1,
					// j),lambAt(i-1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(i+1,
					// j),lambAt(j+1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(i+1,
					// j),lambAt(j-1, j)}));
					//
					// solver.addClause(new VecInt(new int[]{lambAt(i-1,
					// j),lambAt(i+1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(i-1,
					// j),lambAt(j+1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(i-1,
					// j),lambAt(j-1, j)}));
					//
					// solver.addClause(new VecInt(new int[]{lambAt(j+1,
					// j),lambAt(i+1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(j+1,
					// j),lambAt(i-1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(j+1,
					// j),lambAt(j-1, j)}));
					//
					// solver.addClause(new VecInt(new int[]{lambAt(j-1,
					// j),lambAt(i+1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(j-1,
					// j),lambAt(i-1, j)}));
					// solver.addClause(new VecInt(new int[]{lambAt(j-1,
					// j),lambAt(j+1, j)}));
				}
				
			}
			
		}
		IProblem problem = solver;
		if (problem.isSatisfiable()) {
			
			int[] model = problem.model();
			System.out.println(Arrays.toString(model));
			for (int i = 0; i < model.length; i++) {
				
				if (model[i] > 0) {
					
					int x = revlambtAtX(model[i]);
					int y = revlambAtY(model[i]);
					
					System.out.println(model[i]);
					System.out.println("X:" + x + " Y:" + y);
					
					if (x != 0 && y != 0)
						field[x][y] = AkariTypes.LAMB;
					
				}
			}
			
			for (int j = 0; j < height; j++) {
				for (int i = 0; i < width; i++) {
					switch (field[i][j]) {
						case BLOCK:
							System.out.print("#");
							break;
						
						case EMPTY:
							System.out.print(" ");
							break;
						case LAMB:
							System.out.print("O");
							break;
							
						case BLOCK1:
							System.out.print("1");
							break;
							
						default:
							break;
					}
				}
				System.out.println();
			}
			
		} else {
			
			System.out.println("Scheisse");
		}
		
	}
	


	
	static int[] toIntArray(List<Integer> integerList) {
		int[] intArray = new int[integerList.size()];
		for (int i = 0; i < integerList.size(); i++) {
			intArray[i] = integerList.get(i);
		}
		return intArray;
	}
	
}
