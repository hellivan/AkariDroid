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
	 *             Is thrown when a gamefield is not solvable even if no lamps
	 *             are placed
	 */
	public AkariSolver(final GameFieldModel model, final int timeout) throws ContradictionException {

		this.model = model;
		this.MAXVAR = this.lightAt(model.getWidth() - 1, model.getHeight() - 1) + 1;
		this.NBCLAUSES = 50000;

		this.solver = SolverFactory.newDefault();

		this.solver.setTimeout(timeout);

		this.solver.newVar(this.MAXVAR);
		this.solver.setExpectedNumberOfClauses(this.NBCLAUSES);
		this.solver.setDBSimplificationAllowed(true);
		this.solver.setKeepSolverHot(true);
		this.solver.setVerbose(true);

		this.lampPosTrueList = new ArrayList<Integer>();

		this.updateLamps();

		this.createModel();
	}

	private void updateLamps() {
		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {
				if (this.model.getCellState(i, j) == CellState.LAMP) {
					this.lampPosTrueList.add(this.lampAt(i, j));
				}
			}
		}

		this.lampPosTrueAndOtherFalseList = new ArrayList<Integer>();

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {
				if (this.model.getCellState(i, j) == CellState.LAMP) {
					this.lampPosTrueAndOtherFalseList.add(this.lampAt(i, j));
				} else {
					this.lampPosTrueAndOtherFalseList.add(-this.lampAt(i, j));
				}
			}
		}
	}

	private Point reverseLampAt(final int pos) {

		if (pos > this.lampAt(this.model.getWidth() - 1, this.model.getHeight() - 1) || pos < 1) {
			return null;
		}

		Point res = new Point((pos - 1) % this.model.getWidth(), (pos - 1) / this.model.getWidth());

		return res;

	}

	private int lampAt(final int x, final int y) {
		return x + y * this.model.getWidth() + 1;
	}

	private int lightAt(final int x, final int y) {

		if (x >= this.model.getWidth() || y >= this.model.getWidth() || x < 0 || y < 0) {
			System.out.println("Error" + x + " " + y);
		}

		return this.lampAt(this.model.getWidth() - 1, this.model.getHeight() - 1) + x + y * this.model.getWidth() + 1000;

	}

	private void createModel() throws ContradictionException {
		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {

				switch (this.model.getCellState(i, j)) {

				case LAMP:
					// solver.addClause(new VecInt(new int[] { lampAt(i, j) }));
				case BLANK:
					// win condition: every model.getCellState( lighted or a
					// lamp
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j), this.lightAt(i, j) }));

					// win condition: not both: lighted and a lamp
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), -this.lightAt(i, j) }));

					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(-this.lightAt(i, j));

					int k = j + 1;
					while (k >= 0 && k < this.model.getHeight() && (this.model.getCellState(i, k) == CellState.BLANK || this.model.getCellState(i, k) == CellState.LAMP)) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(i, k) }));

						list.add(this.lampAt(i, k));
						k++;
					}

					k = j - 1;
					while (k >= 0 && k < this.model.getHeight() && (this.model.getCellState(i, k) == CellState.BLANK || this.model.getCellState(i, k) == CellState.LAMP)) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(i, k) }));
						list.add(this.lampAt(i, k));
						k--;
					}

					k = i + 1;
					while (k >= 0 && k < this.model.getWidth() && (this.model.getCellState(k, j) == CellState.BLANK || this.model.getCellState(k, j) == CellState.LAMP)) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(k, j) }));
						list.add(this.lampAt(k, j));
						k++;
					}

					k = i - 1;
					while (k >= 0 && k < this.model.getWidth() && (this.model.getCellState(k, j) == CellState.BLANK || this.model.getCellState(k, j) == CellState.LAMP)) {

						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(k, j) }));
						list.add(this.lampAt(k, j));
						k--;
					}

					this.solver.addClause(new VecInt(AkariSolver.toIntArray(list)));

					break;

				case BLOCK0:
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j - 1) }));
					break;

				case BLOCK4:
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j - 1) }));
					break;

				case BLOCK1:
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j), this.lampAt(i, j + 1), this.lampAt(i, j - 1) }));

					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { -lampAt(i - 1,
					// j), -lampAt(i + 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j), -this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j), -this.lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { -lampAt(i, j +
					// 1), -lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { -lampAt(i, j +
					// 1), -lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { -lampAt(i, j -
					// 1), -lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { -lampAt(i, j -
					// 1), -lampAt(i - 1, j) }));
					// solver.addClause(new VecInt(new int[] { -lampAt(i, j -
					// 1), -lampAt(i, j + 1) }));

					break;

				case BLOCK3:
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j), -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) }));

					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { lampAt(i - 1, j),
					// lampAt(i + 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j), this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j), this.lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { lampAt(i, j + 1),
					// lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { lampAt(i, j + 1),
					// lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j + 1), this.lampAt(i, j - 1) }));

					// solver.addClause(new VecInt(new int[] { lampAt(i, j - 1),
					// lampAt(i + 1, j) }));
					// solver.addClause(new VecInt(new int[] { lampAt(i, j - 1),
					// lampAt(i - 1, j) }));
					// solver.addClause(new VecInt(new int[] { lampAt(i, j - 1),
					// lampAt(i, j + 1) }));

					break;

				case BLOCK2:
					// ( a + b + c ) * ( a + b + d ) * ( a + c + d ) * ( b + c +
					// d ) *
					// ( !a + !b + !c ) *( !a + !b + !d ) * ( !a + !c + !d ) * (
					// !b + !c + !d )

					// ( a + b + c )
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j), this.lampAt(i, j + 1) }));
					// ( a + b + d )
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j), this.lampAt(i, j - 1) }));
					// ( a + c + d )
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i, j + 1), this.lampAt(i, j - 1) }));
					// ( b + c + d )
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j), this.lampAt(i, j + 1), this.lampAt(i, j - 1) }));

					// ( !a + !b + !c )
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j), -this.lampAt(i, j + 1) }));
					// ( !a + !b + !d )
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j), -this.lampAt(i, j - 1) }));
					// ( !a + !c + !d )
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) }));
					// ( !b + !c + !d )
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j), -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) }));

					break;

				default:
					break;
				}

				if (this.model.getCellState(i, j) != CellState.BLANK && this.model.getCellState(i, j) != CellState.LAMP) {
					// blocks cannot be lighted
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j) }));
					// blocks cannot be lighted
					this.solver.addClause(new VecInt(new int[] { -this.lightAt(i, j) }));
				}

			}

		}

	}

	/**
	 * Returns true when the Akari game is solved with the lamps currently
	 * placed on the gamefield.
	 * 
	 * @return true when the Akari game is solved otherwise false;
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public boolean isSolved() throws TimeoutException {

		this.updateLamps();

		return this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(this.lampPosTrueAndOtherFalseList)));

	}

	/**
	 * Returns true when the Akari puzzle is solvable (without the currently
	 * placed lamps)
	 * 
	 * @return true when the Akari game is solvable otherwise false;
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public boolean isSatisfiable() throws TimeoutException {

		return this.solver.isSatisfiable();

	}

	/**
	 * Returns true when the Akari puzzle is solvable (with the currently placed
	 * lamps)
	 * 
	 * @return true when the Akari game is solvable otherwise false;
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public boolean isSatisfiableWithCurrentLamps() throws TimeoutException {

		this.updateLamps();
		return this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(this.lampPosTrueAndOtherFalseList)));

	}

	/**
	 * Returns the lamps which lead to an unsolvable puzzle
	 * 
	 * @return true the lamps which lead to an unsolvable puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getWrongPlacedLamps() throws TimeoutException {

		this.updateLamps();

		LinkedList<Point> list = null;

		if (this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(this.lampPosTrueList)))) {
			return null;
		} else {
			IVecInt errors = this.solver.unsatExplanation();

			if (errors == null) {
				return null;
			}

			list = new LinkedList<Point>();

			for (int i = 0; i < errors.size(); i++) {
				Point p = this.reverseLampAt(errors.get(i));

				if (p != null) {
					list.add(p);
				}
			}
		}

		return list;

	}

	public boolean hasWrongPlacedLamps() throws TimeoutException {

		List<Point> list = this.getWrongPlacedLamps();

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

		this.updateLamps();

		if (this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(this.lampPosTrueList)))) {

			LinkedList<Point> list = new LinkedList<Point>();
			int[] model = this.solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = this.reverseLampAt(model[i]);

				if (p != null) {
					if (this.model.getCellState(p.x, p.y) != CellState.LAMP) {
						list.add(p);
					}
				}
			}

			return list;
		} else {
			return null;
		}

	}

	/**
	 * Returns an arbitrary combination of lamps which are needed to complete
	 * the complete puzzle
	 * 
	 * @return an arbitrary combination of lamps which are needed to complete
	 *         the complete puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getSolution() throws TimeoutException {

		if (this.solver.isSatisfiable()) {

			LinkedList<Point> list = new LinkedList<Point>();
			int[] model = this.solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = this.reverseLampAt(model[i]);

				if (p != null) {
					list.add(p);
				}
			}

			return list;
		} else {
			return null;
		}

	}

	/**
	 * Returns an arbitrary model which is needed to complete the complete
	 * puzzle
	 * 
	 * @return an arbitrary model which is needed to complete the complete
	 *         puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public GameFieldModel getSolutionModel() throws TimeoutException {

		GameFieldModel clone = (GameFieldModel) this.model.clone();

		if (this.solver.isSatisfiable(new VecInt())) {

			int[] model = this.solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = this.reverseLampAt(model[i]);

				if (p != null) {
					clone.setCellState(p.x, p.y, CellState.LAMP);
				}
			}

			return clone;
		} else {
			return null;
		}

	}

	private static int[] toIntArray(final List<Integer> integerList) {
		int[] intArray = new int[integerList.size()];
		for (int i = 0; i < integerList.size(); i++) {
			intArray[i] = integerList.get(i);
		}
		return intArray;
	}

}
