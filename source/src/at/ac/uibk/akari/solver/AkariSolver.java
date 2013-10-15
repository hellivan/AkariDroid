package at.ac.uibk.akari.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SingleSolutionDetector;

import android.graphics.Point;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.core.Puzzle.CellState;

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
	public AkariSolver(final GameFieldModel model) throws ContradictionException {

		this.model = model;
		this.MAXVAR = this.falseVar() + 1;
		this.NBCLAUSES = 50000;

		this.solver = SolverFactory.newDefault();

		// this.solver.setTimeout(100000);

		this.solver.newVar(this.MAXVAR);
		this.solver.setExpectedNumberOfClauses(this.NBCLAUSES);
		this.solver.setDBSimplificationAllowed(true);
		this.solver.setKeepSolverHot(true);
		this.solver.setVerbose(true);

		this.lampPosTrueList = new ArrayList<Integer>();
		this.lampPosTrueAndOtherFalseList = new ArrayList<Integer>();

		this.updateLamps();

		this.setModel(model);
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

		if ((pos > this.lampAt(this.model.getWidth() - 1, this.model.getHeight() - 1)) || (pos < 1)) {
			return null;
		}

		Point res = new Point((pos - 1) % this.model.getWidth(), (pos - 1) / this.model.getWidth());

		return res;

	}

	private int lampAt(final Point location) {
		return this.lampAt(location.x, location.y);
	}

	private int lampAt(final int x, final int y) {
		if ((x >= this.model.getWidth()) || (y >= this.model.getWidth()) || (x < 0) || (y < 0)) {
			return this.falseVar();
		}

		return x + (y * this.model.getWidth()) + 1;
	}

	private int falseVar() {
		return this.lightAt(this.model.getWidth() - 1, this.model.getHeight() - 1) + 1;
	}

	private int lightAt(final int x, final int y) {

		if ((x >= this.model.getWidth()) || (y >= this.model.getWidth()) || (x < 0) || (y < 0)) {
			return this.falseVar();
		}

		return this.lampAt(this.model.getWidth() - 1, this.model.getHeight() - 1) + x + (y * this.model.getWidth()) + 1000;

	}

	private void updateModel() throws ContradictionException {
		this.setModel(this.model);
	}

	private void setModel(final GameFieldModel model) throws ContradictionException {

		this.model = model;
		this.solver.clearLearntClauses();

		this.solver.addClause(new VecInt(new int[] { -this.falseVar() }));

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {

				if (this.model.getPuzzleCellState(i, j) != CellState.BLANK) {
					// blocks cannot be lighted
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j) }));
					// blocks cannot be lighted
					this.solver.addClause(new VecInt(new int[] { -this.lightAt(i, j) }));
				}

				switch (this.model.getPuzzleCellState(i, j)) {

				case BLANK:
					// win condition: every model.getCellState( lighted or a
					// lamp
					this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j), this.lightAt(i, j) }));

					// win condition: not both: lighted and a lamp
					this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), -this.lightAt(i, j) }));

					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(-this.lightAt(i, j));

					int k = j + 1;
					while ((k >= 0) && (k < this.model.getHeight()) && (this.model.getPuzzleCellState(i, k) == CellState.BLANK)) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(i, k) }));

						list.add(this.lampAt(i, k));
						k++;
					}

					k = j - 1;
					while ((k >= 0) && (k < this.model.getHeight()) && (this.model.getPuzzleCellState(i, k) == CellState.BLANK)) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(i, k) }));
						list.add(this.lampAt(i, k));
						k--;
					}

					k = i + 1;
					while ((k >= 0) && (k < this.model.getWidth()) && (this.model.getPuzzleCellState(k, j) == CellState.BLANK)) {
						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(k, j) }));
						list.add(this.lampAt(k, j));
						k++;
					}

					k = i - 1;
					while ((k >= 0) && (k < this.model.getWidth()) && (this.model.getPuzzleCellState(k, j) == CellState.BLANK)) {

						this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j), this.lightAt(k, j) }));
						list.add(this.lampAt(k, j));
						k--;
					}

					this.solver.addClause(new VecInt(AkariSolver.toIntArray(list)));

					break;

				case BLOCK0:
				case BLOCK1:
				case BLOCK2:
				case BLOCK3:
				case BLOCK4:
					this.addBlockDefinition(i, j, this.model.getPuzzleCellState(i, j));

				default:
					break;
				}

			}

		}

	}

	public LinkedList<IConstr> addBlockDefinition(final int i, final int j, final CellState state) throws ContradictionException {
		LinkedList<IConstr> constr = new LinkedList<IConstr>();
		switch (state) {
		case BLOCK0:
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j + 1) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j - 1) })));
			break;

		case BLOCK4:
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j + 1) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j - 1) })));
			break;

		case BLOCK1:
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j), this.lampAt(i, j + 1), this.lampAt(i, j - 1) })));

			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i, j + 1) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i, j - 1) })));

			// solver.addClause(new VecInt(new int[] { -lampAt(i - 1,
			// j), -lampAt(i + 1, j) }));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j), -this.lampAt(i, j + 1) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j), -this.lampAt(i, j - 1) })));

			// solver.addClause(new VecInt(new int[] { -lampAt(i, j +
			// 1), -lampAt(i + 1, j) }));
			// solver.addClause(new VecInt(new int[] { -lampAt(i, j +
			// 1), -lampAt(i - 1, j) }));
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) })));

			// solver.addClause(new VecInt(new int[] { -lampAt(i, j -
			// 1), -lampAt(i + 1, j) }));
			// solver.addClause(new VecInt(new int[] { -lampAt(i, j -
			// 1), -lampAt(i - 1, j) }));
			// solver.addClause(new VecInt(new int[] { -lampAt(i, j -
			// 1), -lampAt(i, j + 1) }));

			break;

		case BLOCK3:
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j), -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) })));

			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i, j + 1) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i, j - 1) })));

			// solver.addClause(new VecInt(new int[] { lampAt(i - 1, j),
			// lampAt(i + 1, j) }));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j), this.lampAt(i, j + 1) })));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j), this.lampAt(i, j - 1) })));

			// solver.addClause(new VecInt(new int[] { lampAt(i, j + 1),
			// lampAt(i + 1, j) }));
			// solver.addClause(new VecInt(new int[] { lampAt(i, j + 1),
			// lampAt(i - 1, j) }));
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i, j + 1), this.lampAt(i, j - 1) })));

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
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j), this.lampAt(i, j + 1) })));
			// ( a + b + d )
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i - 1, j), this.lampAt(i, j - 1) })));
			// ( a + c + d )
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i + 1, j), this.lampAt(i, j + 1), this.lampAt(i, j - 1) })));
			// ( b + c + d )
			constr.add(this.solver.addClause(new VecInt(new int[] { this.lampAt(i - 1, j), this.lampAt(i, j + 1), this.lampAt(i, j - 1) })));

			// ( !a + !b + !c )
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j), -this.lampAt(i, j + 1) })));
			// ( !a + !b + !d )
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i - 1, j), -this.lampAt(i, j - 1) })));
			// ( !a + !c + !d )
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i + 1, j), -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) })));
			// ( !b + !c + !d )
			constr.add(this.solver.addClause(new VecInt(new int[] { -this.lampAt(i - 1, j), -this.lampAt(i, j + 1), -this.lampAt(i, j - 1) })));

			break;

		default:
			break;
		}

		return constr;

	}

	public void removeDefinition(final LinkedList<IConstr> constr) {

		for (IConstr c : constr) {
			this.solver.removeConstr(c);
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

		ArrayList<Integer> solvableList = new ArrayList<Integer>(this.lampPosTrueList);

		LinkedList<Point> list = new LinkedList<Point>();
		while (!this.solver.isSatisfiable(new VecInt(AkariSolver.toIntArray(solvableList)))) {

			IVecInt errors = this.solver.unsatExplanation();

			if (errors == null) {
				return null;
			}

			for (int i = 0; i < errors.size(); i++) {

				solvableList.remove((Integer) errors.get(i));
				solvableList.add(-errors.get(i));

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

		return (list != null) && (list.size() > 0);
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
					if (!this.model.isLampAt(p)) {
						list.add(p);
					}
				}
			}

			return list;
		} else {
			IVecInt unsat=this.solver.unsatExplanation();
			
			for (int i = 0; i < unsat.size(); i++) {				
				
			}
		}
		return null;

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

		if (this.solver.isSatisfiable()) {

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

	public static Puzzle generateRandomBlockModel(final int width, final int height) {

		Random r = new Random();
		Puzzle model = new Puzzle(width, height);

		int count = (width + height) + (r.nextInt(((width * height) - (width + height)) / 3));

		for (int i = 0; i < count; i++) {
			int x = r.nextInt(width);
			int y = r.nextInt(height);

			if (model.getCellState(x, y) == CellState.BARRIER) {
				i--;
			} else {
				model.setCellState(x, y, CellState.BARRIER);
			}
		}

		return model;

	}

	public static GameFieldModel generatePuzzle(final int width, final int height) {

		Puzzle puzzle = AkariSolver.generateRandomBlockModel(width, height);
		GameFieldModel model = new GameFieldModel(puzzle);
		Random r = new Random();

		try {
			AkariSolver solver = new AkariSolver(model);
			solver.setSolutionToModel();

			for (Point lamp : solver.getModel().getLamps()) {
				if (r.nextBoolean()) {

					List<Point> barriers = puzzle.getNeightbors(lamp, CellState.BARRIER);

					if (barriers.size() == 0) {
						continue;
					}

					Point barrier = barriers.get(r.nextInt(barriers.size()));

					puzzle.setCellState(barrier, CellState.getBlockByNumber(model.getLampNeightbors(barrier).size()));

					LinkedList<IConstr> constr = solver.addBlockDefinition(barrier.x, barrier.y, puzzle.getCellState(barrier));

					if (!solver.isSatisfiable()) {
						System.out.println("!satisfiable after first steps....");
						solver.removeDefinition(constr);
						puzzle.setCellState(barrier, CellState.BARRIER);
					}

				}
			}

			SingleSolutionDetector ssd = new SingleSolutionDetector(solver.solver);

			if (solver.isSatisfiable()) {

				while (!ssd.hasASingleSolution()) {

					System.out.println("satisfiable");
					solver.updateModel();

					int[] model1 = solver.solver.model();

					VecInt a = new VecInt();
					for (int i = 0; i < model1.length; i++) {
						a.push(-model1[i]);
					}

					IConstr added = solver.solver.addClause(a);

					int[] model2 = solver.solver.model();

					solver.solver.removeConstr(added);

					LinkedList<Integer> intersectModel = new LinkedList<Integer>();
					Collections.shuffle(intersectModel);

					for (int i = 0; i < 1; i++) {
						if ((model1[i] > 0) && !Arrays.asList(model2).contains(model1[i])) {
							intersectModel.add(model1[i]);
						}
					}

					System.out.println(intersectModel.size());

					for (int i = 0; i < intersectModel.size(); i++) {

						Point p = solver.reverseLampAt(intersectModel.get(i));

						if (p != null) {
							System.out.println("Fixing:" + p);

							List<Point> points = puzzle.getNeightbors(p, CellState.BARRIER);

							if (points.size() == 0) {
								points = puzzle.getNeightbors(p, CellState.BLANK);
							}

							if (points.size() == 0) {
								continue;
							}

							Point barrier = points.get(r.nextInt(points.size()));

							puzzle.setCellState(barrier, CellState.getBlockByNumber(model.getLampNeightbors(barrier).size()));

							LinkedList<IConstr> constr1 = solver.addBlockDefinition(barrier.x, barrier.y, puzzle.getCellState(barrier));

							if (!solver.isSatisfiable()) {
								System.out.println("!satisfiable after first steps....");
								solver.removeDefinition(constr1);
								puzzle.setCellState(barrier, CellState.BARRIER);

							}
						}

					}

					solver.updateModel();

				}
			}

		} catch (ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return model;

	}

	public GameFieldModel getModel() {
		return this.model;
	}

}
