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
		this.MAXVAR = this.falseVar() + 1;
		this.NBCLAUSES = 50000;

		this.solver = SolverFactory.newDefault();

		this.solver.setTimeout(timeout);

		this.solver.newVar(this.MAXVAR);
		this.solver.setExpectedNumberOfClauses(this.NBCLAUSES);
		this.solver.setDBSimplificationAllowed(true);
		this.solver.setKeepSolverHot(true);
		this.solver.setVerbose(true);

		this.lampPosTrueList = new ArrayList<Integer>();
		this.lampPosTrueAndOtherFalseList = new ArrayList<Integer>();

		this.updateLamps();

		this.createModel();
	}

	private void updateLamps() {

		this.lampPosTrueList.clear();
		this.lampPosTrueAndOtherFalseList.clear();

		for (Point point : this.model.getLamps()) {
			this.lampPosTrueList.add(this.lampAt(point));
			this.lampPosTrueAndOtherFalseList.add(this.lampAt(point));
		}

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {
				if (!this.lampPosTrueList.contains(this.lampAt(i, j))) {
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

	private int lampAt(final Point location) {
		return this.lampAt(location.x, location.y);
	}

	private int lampAt(final int x, final int y) {
		if (x >= this.model.getWidth() || y >= this.model.getWidth() || x < 0 || y < 0) {
			return this.falseVar();
		}

		return x + y * this.model.getWidth() + 1;
	}

	private int falseVar() {
		return this.lightAt(this.model.getWidth() - 1, this.model.getHeight() - 1) + 1;
	}

	private int lightAt(final int x, final int y) {

		if (x >= this.model.getWidth() || y >= this.model.getWidth() || x < 0 || y < 0) {
			return this.falseVar();
		}

		return this.lampAt(this.model.getWidth() - 1, this.model.getHeight() - 1) + x + y * this.model.getWidth() + 1000;

	}

	private void createModel() throws ContradictionException {

		this.solver.addClause(new VecInt(new int[] { -this.falseVar() }));

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {

				if (!this.model.isCellEmpty(i, j, true)) {
					// blocks cannot be lighted
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j) }));
					// blocks cannot be lighted
					this.solver.addClause(new VecInt(new int[] { -this.lightAt(i, j) }));
				}

				// Current position is empty or a lamp
				if (this.model.isCellEmpty(i, j, true)) {
					// win condition: every model.getCellState( lighted or a
					// lamp
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j), this.lightAt(i, j) }));

					// win condition: not both: lighted and a lamp
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), -this.lightAt(i, j) }));

					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(-this.lightAt(i, j));

					int k = j + 1;
					while (k >= 0 && k < this.model.getHeight() && (this.model.isCellEmpty(i, k, true))) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(i, k) }));

						list.add(this.lampAt(i, k));
						k++;
					}

					k = j - 1;
					while (k >= 0 && k < this.model.getHeight() && (this.model.isCellEmpty(i, k, true))) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(i, k) }));
						list.add(this.lampAt(i, k));
						k--;
					}

					k = i + 1;
					while (k >= 0 && k < this.model.getWidth() && (this.model.isCellEmpty(k, j, true))) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(k, j) }));
						list.add(this.lampAt(k, j));
						k++;
					}

					k = i - 1;
					while (k >= 0 && k < this.model.getWidth() && (this.model.isCellEmpty(k, j, true))) {

						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(k, j) }));
						list.add(this.lampAt(k, j));
						k--;
					}

					this.solver.addClause(new VecInt(AkariSolver.toIntArray(list)));

				}

				// Current position is a 0 block
				else if (this.model.isBlock0At(i, j)) {
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j - 1) }));
				}

				// Current position is a 4 block
				else if (this.model.isBlock4At(i, j)) {
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j + 1) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j) }));
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j - 1) }));
				}

				// Current position is a 1 block
				else if (this.model.isBlock1At(i, j)) {
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
				}

				// Current position is a 3 block
				else if (this.model.isBlock3At(i, j)) {
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

				}

				// Current position is a 2 block
				else if (this.model.isBlock2At(i, j)) {
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
		return this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(this.lampPosTrueList)));

	}

	/**
	 * Returns the lamps which lead to an unsolvable puzzle
	 * 
	 * @return true the lamps which lead to an unsolvable puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getWrongPlacedLamp() throws TimeoutException {

		this.updateLamps();

		LinkedList<Point> list = null;

		if (this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(this.lampPosTrueList)))) {
			return null;
		} else {

			IVecInt errors = this.solver.unsatExplanation();
			System.out.println("Not satisfiable");

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

	/**
	 * Returns all the lamps which lead to an unsolvable puzzle
	 * 
	 * @return true the lamps which lead to an unsolvable puzzle
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public LinkedList<Point> getAllWrongPlacedLamps() throws TimeoutException {

		this.updateLamps();

		LinkedList<Point> list = null;

		ArrayList<Integer> l = new ArrayList<Integer>(this.lampPosTrueList);

		list = new LinkedList<Point>();
		while (!this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(l)))) {

			IVecInt errors = this.solver.unsatExplanation();

			if (errors == null) {
				return null;
			}

			for (int i = 0; i < errors.size(); i++) {

				l.remove((Integer) errors.get(i));
				l.add(-errors.get(i));

				Point p = this.reverseLampAt(errors.get(i));

				if (p != null) {
					list.add(p);
				}

			}
		}

		return list;

	}

	public boolean hasWrongPlacedLamps() throws TimeoutException {

		List<Point> list = this.getWrongPlacedLamp();

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
					if (this.model.isLampAt(p)) {
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
					clone.setLampAt(p);
				}
			}

			return clone;
		} else {
			return null;
		}

	}

	/**
	 * Inserts the solution of the puzzle in the current model.
	 * 
	 * @throws TimeoutException
	 *             Timeout expired
	 */
	public void setSolutionToModel() throws TimeoutException {

		if (this.solver.isSatisfiable(new VecInt())) {

			int[] model = this.solver.model();

			for (int i = 0; i < model.length; i++) {
				Point p = this.reverseLampAt(model[i]);

				if (p != null) {
					this.model.setLampAt(p);
				}
			}

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
