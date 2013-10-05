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
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.GateTranslator;
import org.sat4j.tools.SingleSolutionDetector;

import android.graphics.Point;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.core.Puzzle.CellState;
import at.ac.uibk.akari.solver.GameFieldVarManager.VarBlocks;

/**
 * Solver for Akari puzzles
 * 
 */
public class AkariSolverFull {

	private GameFieldModel model;

	private final int MAXVAR;
	private final int NBCLAUSES;

	private GateTranslator solver;

	private ArrayList<Integer> lampPosTrueList;

	private ArrayList<Integer> lampPosTrueAndOtherFalseList;

	private GameFieldVarManager vars;

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
	public AkariSolverFull(final GameFieldModel model) throws ContradictionException {

		this.model = model;
		this.vars = new GameFieldVarManager(model.getWidth(), model.getHeight());
		this.MAXVAR = this.vars.lastVar() + 1;
		this.NBCLAUSES = model.getWidth() * model.getHeight() * 20;

		this.solver = new GateTranslator(SolverFactory.newDefault());

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
		System.out.println(this.solver.nConstraints());

	}

	private void updateLamps() {

		this.lampPosTrueList.clear();
		this.lampPosTrueAndOtherFalseList.clear();

		for (Point point : this.model.getLamps()) {
			this.lampPosTrueList.add(this.vars.lampAt(point));
			this.lampPosTrueAndOtherFalseList.add(this.vars.lampAt(point));
		}

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {
				if (!this.lampPosTrueList.contains(this.vars.lampAt(i, j))) {
					this.lampPosTrueAndOtherFalseList.add(-this.vars.lampAt(i, j));
				}
			}
		}
	}

	void updateModel() throws ContradictionException {
		this.setModel(this.model);
	}

	private void setModel(final GameFieldModel model) throws ContradictionException {

		this.solver.clearLearntClauses();

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {
				switch (model.getPuzzleCellState(i, j)) {
				case BARRIER:
					this.solver.addClause(new VecInt(new int[] { this.vars.barrierAt(i, j) }));
					break;
				case BLANK:
					this.solver.addClause(new VecInt(new int[] { this.vars.placeableAt(i, j) }));
					break;
				case BLOCK0:
					this.solver.addClause(new VecInt(new int[] { this.vars.blockAt(0, i, j) }));
					break;
				case BLOCK1:
					this.solver.addClause(new VecInt(new int[] { this.vars.blockAt(1, i, j) }));
					break;
				case BLOCK2:
					this.solver.addClause(new VecInt(new int[] { this.vars.blockAt(2, i, j) }));
					break;
				case BLOCK3:
					this.solver.addClause(new VecInt(new int[] { this.vars.blockAt(3, i, j) }));
					break;
				case BLOCK4:
					this.solver.addClause(new VecInt(new int[] { this.vars.blockAt(4, i, j) }));
					break;

				default:
					break;
				}
			}
		}

		this.setRules();

	}

	private void exaclyOneTrue(final int[] literals) throws ContradictionException {

		this.solver.addClause(new VecInt(literals));

		for (int i = 0; i < literals.length; i++) {
			for (int j = i + 1; j < literals.length; j++) {
				this.solver.addClause(new VecInt(new int[] { -literals[i], -literals[j] }));
			}
		}
	}

	private void setRules() throws ContradictionException {

		// this.solver.clearLearntClauses();

		this.solver.addClause(new VecInt(new int[] { -this.vars.falseVar() }));

		// solver.gateTrue(vars.trueVar());

		for (int i = 0; i < this.model.getWidth(); i++) {
			for (int j = 0; j < this.model.getHeight(); j++) {

				// a field can only be one type at the same time
				this.exaclyOneTrue(new int[] { this.vars.blankAt(i, j), this.vars.lampAt(i, j), this.vars.barrierAt(i, j), this.vars.blockAt(0, i, j), this.vars.blockAt(1, i, j), this.vars.blockAt(2, i, j), this.vars.blockAt(3, i, j), this.vars.blockAt(4, i, j) });

				// placeable is lamp or blank
				GateTranslator trans = new GateTranslator(this.solver);
				trans.or(this.vars.placeableAt(i, j), new VecInt(new int[] { this.vars.blankAt(i, j), this.vars.lampAt(i, j) }));

				// light is caused by a ray
				this.solver.addClause(new VecInt(new int[] { -this.vars.lightAt(i, j), this.vars.leftRayAt(i, j), this.vars.rightRayAt(i, j), this.vars.upRayAt(i, j), this.vars.downRayAt(i, j) }));

				// a ray causes a light
				this.solver.addClause(new VecInt(new int[] { -this.vars.leftRayAt(i, j), this.vars.lightAt(i, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.rightRayAt(i, j), this.vars.lightAt(i, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.upRayAt(i, j), this.vars.lightAt(i, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.downRayAt(i, j), this.vars.lightAt(i, j) }));

				// only blanks can be lighted
				// win condition: cell lighted or a lamp
				// light equals blank (added to variable manager)
				// this.solver.addClause(new VecInt(new int[] { vars.blankAt(i,
				// j), -vars.lightAt(i, j) }));
				// this.solver.addClause(new VecInt(new int[] { -vars.blankAt(i,
				// j), vars.lightAt(i, j) }));

				// only blank can be lights (implies also the rays)
				this.solver.addClause(new VecInt(new int[] { this.vars.blankAt(i, j), -this.vars.lightAt(i, j) }));

				// lamp implies rays in all directions
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.blankAt(i + 1, j), this.vars.rightRayAt(i + 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.blankAt(i - 1, j), this.vars.leftRayAt(i - 1, j) }));

				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.blankAt(i, j + 1), this.vars.downRayAt(i, j + 1) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.blankAt(i, j - 1), this.vars.upRayAt(i, j - 1) }));

				// lamps cannot have lamps as neightbors
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.lampAt(i + 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.lampAt(i - 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.lampAt(i, j + 1) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.lampAt(i, j - 1) }));

				// lamps cannot be lighted on by rays
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.rightRayAt(i - 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.leftRayAt(i + 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.upRayAt(i, j + 1) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.downRayAt(i, j - 1) }));

				// Rays imply other rays left(i,j)->left(i-1,j)
				this.solver.addClause(new VecInt(new int[] { -this.vars.leftRayAt(i, j), -this.vars.blankAt(i - 1, j), this.vars.lampAt(i - 1, j), this.vars.leftRayAt(i - 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.rightRayAt(i, j), -this.vars.blankAt(i + 1, j), this.vars.lampAt(i + 1, j), this.vars.rightRayAt(i + 1, j) }));

				this.solver.addClause(new VecInt(new int[] { -this.vars.upRayAt(i, j), -this.vars.blankAt(i, j - 1), this.vars.lampAt(i, j - 1), this.vars.upRayAt(i, j - 1) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.downRayAt(i, j), -this.vars.blankAt(i, j + 1), this.vars.lampAt(i, j + 1), this.vars.downRayAt(i, j + 1) }));

				// rays only when caused by a lamp or an other ray from the
				// opposite site
				this.solver.addClause(new VecInt(new int[] { -this.vars.leftRayAt(i, j), this.vars.leftRayAt(i + 1, j), this.vars.lampAt(i + 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.rightRayAt(i, j), this.vars.rightRayAt(i - 1, j), this.vars.lampAt(i - 1, j) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.upRayAt(i, j), this.vars.upRayAt(i, j + 1), this.vars.lampAt(i, j + 1) }));
				this.solver.addClause(new VecInt(new int[] { -this.vars.downRayAt(i, j), this.vars.downRayAt(i, j - 1), this.vars.lampAt(i, j - 1) }));

				// lights cannot be lighted by other
				// no lamp and light
				this.solver.addClause(new VecInt(new int[] { -this.vars.lampAt(i, j), -this.vars.lightAt(i, j) }));

				this.addBlockDefinition(i, j);

			}

		}

	}

	public LinkedList<IConstr> addBlockDefinition(final int i, final int j) throws ContradictionException {
		LinkedList<IConstr> constr = new LinkedList<IConstr>();

		// case BLOCK0:

		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(0, i, j), -this.vars.lampAt(i + 1, j) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(0, i, j), -this.vars.lampAt(i, j + 1) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(0, i, j), -this.vars.lampAt(i - 1, j) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(0, i, j), -this.vars.lampAt(i, j - 1) })));

		// case BLOCK4:

		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(4, i, j), this.vars.lampAt(i + 1, j) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(4, i, j), this.vars.lampAt(i, j + 1) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(4, i, j), this.vars.lampAt(i - 1, j) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(4, i, j), this.vars.lampAt(i, j - 1) })));

		// case BLOCK1:

		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i - 1, j), this.vars.lampAt(i, j + 1), this.vars.lampAt(i, j - 1) })));

		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i - 1, j) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i, j + 1) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i, j - 1) })));

		// solver.addClause(new VecInt(new int[] { -vars.lampAt(i - 1,
		// j), -vars.lampAt(i + 1, j) }));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), -this.vars.lampAt(i - 1, j), -this.vars.lampAt(i, j + 1) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), -this.vars.lampAt(i - 1, j), -this.vars.lampAt(i, j - 1) })));

		// solver.addClause(new VecInt(new int[] { -vars.lampAt(i, j +
		// 1), -vars.lampAt(i + 1, j) }));
		// solver.addClause(new VecInt(new int[] { -vars.lampAt(i, j +
		// 1), -vars.lampAt(i - 1, j) }));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(1, i, j), -this.vars.lampAt(i, j + 1), -this.vars.lampAt(i, j - 1) })));

		// solver.addClause(new VecInt(new int[] { -vars.lampAt(i, j -
		// 1), -vars.lampAt(i + 1, j) }));
		// solver.addClause(new VecInt(new int[] { -vars.lampAt(i, j -
		// 1), -vars.lampAt(i - 1, j) }));
		// solver.addClause(new VecInt(new int[] { -vars.lampAt(i, j -
		// 1), -vars.lampAt(i, j + 1) }));

		// case BLOCK3:

		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i - 1, j), -this.vars.lampAt(i, j + 1), -this.vars.lampAt(i, j - 1) })));

		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i - 1, j) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i, j + 1) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i, j - 1) })));

		// solver.addClause(new VecInt(new int[] { vars.lampAt(i - 1, j),
		// vars.lampAt(i + 1, j) }));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), this.vars.lampAt(i - 1, j), this.vars.lampAt(i, j + 1) })));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), this.vars.lampAt(i - 1, j), this.vars.lampAt(i, j - 1) })));

		// solver.addClause(new VecInt(new int[] { vars.lampAt(i, j + 1),
		// vars.lampAt(i + 1, j) }));
		// solver.addClause(new VecInt(new int[] { vars.lampAt(i, j + 1),
		// vars.lampAt(i - 1, j) }));
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(3, i, j), this.vars.lampAt(i, j + 1), this.vars.lampAt(i, j - 1) })));

		// solver.addClause(new VecInt(new int[] { vars.lampAt(i, j - 1),
		// vars.lampAt(i + 1, j) }));
		// solver.addClause(new VecInt(new int[] { vars.lampAt(i, j - 1),
		// vars.lampAt(i - 1, j) }));
		// solver.addClause(new VecInt(new int[] { vars.lampAt(i, j - 1),
		// vars.lampAt(i, j + 1) }));

		// case BLOCK2:

		// ( a + b + c ) * ( a + b + d ) * ( a + c + d ) * ( b + c +
		// d ) *
		// ( !a + !b + !c ) *( !a + !b + !d ) * ( !a + !c + !d ) * (
		// !b + !c + !d )

		// ( a + b + c )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i - 1, j), this.vars.lampAt(i, j + 1) })));
		// ( a + b + d )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i - 1, j), this.vars.lampAt(i, j - 1) })));
		// ( a + c + d )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), this.vars.lampAt(i + 1, j), this.vars.lampAt(i, j + 1), this.vars.lampAt(i, j - 1) })));
		// ( b + c + d )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), this.vars.lampAt(i - 1, j), this.vars.lampAt(i, j + 1), this.vars.lampAt(i, j - 1) })));

		// ( !a + !b + !c )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i - 1, j), -this.vars.lampAt(i, j + 1) })));
		// ( !a + !b + !d )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i - 1, j), -this.vars.lampAt(i, j - 1) })));
		// ( !a + !c + !d )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), -this.vars.lampAt(i + 1, j), -this.vars.lampAt(i, j + 1), -this.vars.lampAt(i, j - 1) })));
		// ( !b + !c + !d )
		constr.add(this.solver.addClause(new VecInt(new int[] { -this.vars.blockAt(2, i, j), -this.vars.lampAt(i - 1, j), -this.vars.lampAt(i, j + 1), -this.vars.lampAt(i, j - 1) })));

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

		return this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueAndOtherFalseList)));

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
		return this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueList)));

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

		if (this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueList)))) {
			return null;
		} else {

			IVecInt errors = this.solver.unsatExplanation();
			System.out.println("Not satisfiable");

			if (errors == null) {
				return null;
			}

			list = new LinkedList<Point>();

			for (int i = 0; i < errors.size(); i++) {

				if (this.vars.reverseVarBlock(errors.get(i)) != VarBlocks.LAMP) {
					continue;
				}

				Point p = this.vars.reverseVarPoint(errors.get(i));

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
		while (!this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(l)))) {

			IVecInt errors = this.solver.unsatExplanation();

			if (errors == null) {
				return null;
			}

			for (int i = 0; i < errors.size(); i++) {

				l.remove((Integer) errors.get(i));
				l.add(-errors.get(i));

				if (this.vars.reverseVarBlock(errors.get(i)) != VarBlocks.LAMP) {
					continue;
				}

				Point p = this.vars.reverseVarPoint(errors.get(i));

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

		if (this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueList)))) {

			LinkedList<Point> list = new LinkedList<Point>();
			int[] model = this.solver.model();

			for (int i = 0; i < model.length; i++) {
				if (this.vars.reverseVarBlock(model[i]) != VarBlocks.LAMP) {
					continue;
				}

				Point p = this.vars.reverseVarPoint(model[i]);

				if (p != null) {
					if (!this.model.isLampAt(p)) {
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
				if (this.vars.reverseVarBlock(model[i]) != VarBlocks.LAMP) {
					continue;
				}

				Point p = this.vars.reverseVarPoint(model[i]);

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
				if (this.vars.reverseVarBlock(model[i]) != VarBlocks.LAMP) {
					continue;
				}

				Point p = this.vars.reverseVarPoint(model[i]);

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

				if ((model[i] < 0) || (this.vars.reverseVarBlock(model[i]) != VarBlocks.LAMP)) {
					continue;
				}

				Point p = this.vars.reverseVarPoint(model[i]);

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

		Puzzle puzzle = AkariSolverFull.generateRandomBlockModel(width, height);
		GameFieldModel model = new GameFieldModel(puzzle);
		Random r = new Random();

		try {
			AkariSolverFull solver = new AkariSolverFull(model);
			solver.setSolutionToModel();

			List<Point> lamps = solver.getModel().getLamps();
			for (int i = 0; i < lamps.size(); i++) {
				if (r.nextBoolean()) {

					List<Point> barriers = puzzle.getNeightbors(lamps.get(i), CellState.BARRIER);

					if (barriers.size() == 0) {
						continue;
					}

					Point barrier = barriers.get(r.nextInt(barriers.size()));

					puzzle.setCellState(barrier, CellState.getBlockByNumber(model.getLampNeightbors(barrier).size()));

					LinkedList<IConstr> constr = solver.addBlockDefinition(barrier.x, barrier.y);

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

						if (solver.vars.reverseVarBlock(intersectModel.get(i)) != VarBlocks.LAMP) {
							continue;
						}

						Point p = solver.vars.reverseVarPoint(intersectModel.get(i));

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

							LinkedList<IConstr> constr1 = solver.addBlockDefinition(barrier.x, barrier.y);

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
