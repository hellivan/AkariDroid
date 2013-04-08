package at.ac.uibk.akari.solver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import android.graphics.Point;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.GameFieldModel.CellState;

/**
 * Solver for Akari puzzles
 * 
 */
public class AkariSolver {

	private GameFieldModel model;

	private final int MAXVAR;
	private final int NBCLAUSES;

	private ISolver solver;

	private ArrayList<Integer> lampPosTrueList;

	private ArrayList<Integer> lampPosTrueAndOtherFalseList;

	/**
	 * Initializes a new Instance of a Akari solver
	 * 
	 * @param model
	 *            the model of the game to be solved
	 * @param timeout
	 *            timeout of the solve process
	 * @throws ContradictionException
	 *             Is thrown when a gamefield is not solvable even if no lamps are placed
	 */
	public AkariSolver(GameFieldModel model, int timeout) throws ContradictionException {

		this.model = model;
		MAXVAR = lightAt(model.getWidth() - 1, model.getHeight() - 1) + 1;
		NBCLAUSES = 50000;

		solver = SolverFactory.newDefault();

		solver.setTimeout(timeout);

		solver.newVar(MAXVAR);
		solver.setExpectedNumberOfClauses(NBCLAUSES);
		solver.setDBSimplificationAllowed(true);
		solver.setKeepSolverHot(true);
		solver.setVerbose(true);

		lampPosTrueList = new ArrayList<Integer>();

		updateLamps();

		createModel();
	}

	private void updateLamps() {
		for (int i = 0; i < model.getHeight(); i++) {
			for (int j = 0; j < model.getWidth(); j++) {
				if (model.getCellState(i, j) == CellState.LAMP)
					lampPosTrueList.add(lampAt(i, j));
			}
		}

		lampPosTrueAndOtherFalseList = new ArrayList<Integer>();

		for (int i = 0; i < model.getHeight(); i++) {
			for (int j = 0; j < model.getWidth(); j++) {
				if (model.getCellState(i, j) == CellState.LAMP)
					lampPosTrueAndOtherFalseList.add(lampAt(i, j));
				else
					lampPosTrueAndOtherFalseList.add(-lampAt(i, j));
			}
		}
	}

	private Point reverseLampAt(int pos) {

		if (pos > lampAt(model.getWidth() - 1, model.getHeight() - 1))
			return null;

		Point res = new Point((pos - 1) % model.getWidth(), (pos - 1) / model.getWidth());

		return res;

	}

	private int lampAt(int x, int y) {
		return x + y * this.model.getWidth() + 1;
	}

	private int lightAt(int x, int y) {

		if (x >= this.model.getWidth() || y >= this.model.getWidth() || x < 0 || y < 0)
			System.out.println("Error" + x + " " + y);

		return lampAt(this.model.getWidth() - 1, model.getHeight() - 1) + x + y * this.model.getWidth() + 1000;

	}

	private void createModel() throws ContradictionException {
		for (int i = 0; i < model.getHeight(); i++) {
			for (int j = 0; j < model.getWidth(); j++) {

				switch (model.getCellState(i, j)) {

				case LAMP:
					// solver.addClause(new VecInt(new int[] { lampAt(i, j) }));
				case BLANK:
					// win condition: every model.getCellState( lighted or a lamp
					solver.addClause(new VecInt(new int[] { lampAt(i, j), lightAt(i, j) }));

					// win condition: not both: lighted and a lamp
					solver.addClause(new VecInt(new int[] { -lampAt(i, j), -lightAt(i, j) }));

					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(-lightAt(i, j));

					int k = j + 1;
					while (k >= 0 && k < model.getHeight() && (model.getCellState(i, k) == CellState.BLANK || model.getCellState(i, k) == CellState.LAMP)) {
						solver.addClause(new VecInt(new int[] { -lampAt(i, j), lightAt(i, k) }));

						list.add(lampAt(i, k));
						k++;
					}

					k = j - 1;
					while (k >= 0 && k < model.getHeight() && (model.getCellState(i, k) == CellState.BLANK || model.getCellState(i, k) == CellState.LAMP)) {
						solver.addClause(new VecInt(new int[] { -lampAt(i, j), lightAt(i, k) }));
						list.add(lampAt(i, k));
						k--;
					}

					k = i + 1;
					while (k >= 0 && k < model.getWidth() && (model.getCellState(k, j) == CellState.BLANK || model.getCellState(k, j) == CellState.LAMP)) {
						solver.addClause(new VecInt(new int[] { -lampAt(i, j), lightAt(k, j) }));
						list.add(lampAt(k, j));
						k++;
					}

					k = i - 1;
					while (k >= 0 && k < model.getWidth() && (model.getCellState(k, j) == CellState.BLANK || model.getCellState(k, j) == CellState.LAMP)) {

						solver.addClause(new VecInt(new int[] { -lampAt(i, j), lightAt(k, j) }));
						list.add(lampAt(k, j));
						k--;
					}

					solver.addClause(new VecInt(toIntArray(list)));

					break;

				case BLOCK0:
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i, j - 1) }));
					break;

				case BLOCK4:
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { lampAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { lampAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { lampAt(i, j - 1) }));
					break;

				case BLOCK1:
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i - 1, j), lampAt(i, j + 1), lampAt(i, j - 1) }));

					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { -lampAt(i - 1, j), -lampAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i - 1, j), -lampAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i - 1, j), -lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { -lampAt(i, j + 1), -lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { -lampAt(i, j + 1), -lampAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { -lampAt(i, j + 1), -lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { -lampAt(i, j - 1), -lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { -lampAt(i, j - 1), -lampAt(i - 1, j) }));
					// solver.addClause(new VecInt(new int[] { -lampAt(i, j - 1), -lampAt(i, j + 1) }));

					break;

				case BLOCK3:
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i - 1, j), -lampAt(i, j + 1), -lampAt(i, j - 1) }));

					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { lampAt(i - 1, j), lampAt(i + 1, j) }));
					solver.addClause(new VecInt(new int[] { lampAt(i - 1, j), lampAt(i, j + 1) }));
					solver.addClause(new VecInt(new int[] { lampAt(i - 1, j), lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { lampAt(i, j + 1), lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { lampAt(i, j + 1), lampAt(i - 1, j) }));
					solver.addClause(new VecInt(new int[] { lampAt(i, j + 1), lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { lampAt(i, j - 1), lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { lampAt(i, j - 1), lampAt(i - 1, j) }));
					// solver.addClause(new VecInt(new int[] { lampAt(i, j - 1), lampAt(i, j + 1) }));

					break;

				case BLOCK2:
					// ( a + b + c ) * ( a + b + d ) * ( a + c + d ) * ( b + c + d ) *
					// ( !a + !b + !c ) *( !a + !b + !d ) * ( !a + !c + !d ) * ( !b + !c + !d )

					// ( a + b + c )
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i - 1, j), lampAt(i, j + 1) }));
					// ( a + b + d )
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i - 1, j), lampAt(i, j - 1) }));
					// ( a + c + d )
					solver.addClause(new VecInt(new int[] { lampAt(i + 1, j), lampAt(i, j + 1), lampAt(i, j - 1) }));
					// ( b + c + d )
					solver.addClause(new VecInt(new int[] { lampAt(i - 1, j), lampAt(i, j + 1), lampAt(i, j - 1) }));

					// ( !a + !b + !c )
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i - 1, j), -lampAt(i, j + 1) }));
					// ( !a + !b + !d )
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i - 1, j), -lampAt(i, j - 1) }));
					// ( !a + !c + !d )
					solver.addClause(new VecInt(new int[] { -lampAt(i + 1, j), -lampAt(i, j + 1), -lampAt(i, j - 1) }));
					// ( !b + !c + !d )
					solver.addClause(new VecInt(new int[] { -lampAt(i - 1, j), -lampAt(i, j + 1), -lampAt(i, j - 1) }));

					break;

				default:
					break;
				}

				if (model.getCellState(i, j) != CellState.BLANK && model.getCellState(i, j) != CellState.LAMP) {
					// blocks cannot be lighted
					solver.addClause(new VecInt(new int[] { -lampAt(i, j) }));
					// blocks cannot be lighted
					solver.addClause(new VecInt(new int[] { -lightAt(i, j) }));
				}

			}

		}

	}

	/**
	 * Returns true when the Akari game is solved with the lamps currently placed on the gamefield.
	 * 
	 * @return true when the Akari game is solved otherwise false;
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public boolean isSolved() throws TimeoutException {

		updateLamps();

		return solver.isSatisfiable(new VecInt(toIntArray(this.lampPosTrueAndOtherFalseList)));

	}

	/**
	 * Returns true when the Akari puzzle is solvable (without the currently placed lamps)
	 * 
	 * @return true when the Akari game is solvable otherwise false;
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public boolean isSatisfiable() throws TimeoutException {

		return solver.isSatisfiable();

	}

	/**
	 * Returns true when the Akari puzzle is solvable (with the currently placed lamps)
	 * 
	 * @return true when the Akari game is solvable otherwise false;
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public boolean isSatisfiableWithCurrentLamps() throws TimeoutException {

		updateLamps();
		return solver.isSatisfiable(new VecInt(toIntArray(lampPosTrueAndOtherFalseList)));

	}

	/**
	 * Returns the lamps which lead to an unsolvable puzzle
	 * 
	 * @return true the lamps which lead to an unsolvable puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getWrongPlacedLamps() throws TimeoutException {

		updateLamps();

		LinkedList<Point> list = null;

		if (solver.isSatisfiable(new VecInt(toIntArray(lampPosTrueList))))
			return null;
		else {
			IVecInt errors = solver.unsatExplanation();

			if (errors == null)
				return null;

			list = new LinkedList<Point>();

			for (int i = 0; i < errors.size(); i++) {
				Point p = reverseLampAt(errors.get(i));

				if (p != null)
					list.add(p);
			}
		}

		return list;

	}

	public boolean hasWrongPlacedLamps() throws TimeoutException {

		List<Point> list = getWrongPlacedLamps();

		return list != null && list.size() > 0;
	}

	/**
	 * Returns the lamps which are needed to complete the current game
	 * 
	 * @return the lamps which are needed to complete the current game
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getHints() throws TimeoutException {

		updateLamps();

		if (solver.isSatisfiable(new VecInt(toIntArray(lampPosTrueList)))) {

			LinkedList<Point> list = new LinkedList<Point>();
			int[] model = solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = reverseLampAt(model[i]);

				if (p != null) {
					if (this.model.getCellState(p.x, p.y) != CellState.LAMP)
						list.add(p);
				}
			}

			return list;
		} else
			return null;

	}
	
	/**
	 * Returns an arbitrary combination of lamps which are needed to complete the complete puzzle
	 * 
	 * @return an arbitrary combination of lamps which are needed to complete the complete puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getSolution() throws TimeoutException {


		if (solver.isSatisfiable()) {

			LinkedList<Point> list = new LinkedList<Point>();
			int[] model = solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = reverseLampAt(model[i]);

				if (p != null) {
						list.add(p);
				}
			}

			return list;
		} else
			return null;

	}
	
	/**
	 * Returns an arbitrary model which is needed to complete the complete puzzle
	 * 
	 * @return an arbitrary model which is needed to complete the complete puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public GameFieldModel getSolutionModel() throws TimeoutException {
		
		GameFieldModel clone = (GameFieldModel) model.clone();

		if (solver.isSatisfiable(new VecInt())) {

			int[] model = solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = reverseLampAt(model[i]);

				if (p != null) {
						clone.setCellState(p.x, p.y, CellState.LAMP);
				}
			}

			return clone;
		} else
			return null;

	}

	private static int[] toIntArray(List<Integer> integerList) {
		int[] intArray = new int[integerList.size()];
		for (int i = 0; i < integerList.size(); i++) {
			intArray[i] = integerList.get(i);
		}
		return intArray;
	}

}
