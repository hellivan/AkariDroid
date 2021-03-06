//package at.ac.uibk.akari.solver;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Random;
//
//import org.sat4j.core.VecInt;
//import org.sat4j.maxsat.WeightedMaxSatDecorator;
//import org.sat4j.specs.ContradictionException;
//import org.sat4j.specs.IConstr;
//import org.sat4j.specs.IVecInt;
//import org.sat4j.specs.TimeoutException;
//
//import android.graphics.Point;
//import at.ac.uibk.akari.core.GameFieldModel;
//import at.ac.uibk.akari.core.Puzzle;
//import at.ac.uibk.akari.core.Puzzle.CellState;
//import at.ac.uibk.akari.solver.GameFieldVarManager.VarBlocks;
//
///**
// * Solver for Akari puzzles
// * 
// */
//public class AkariSolverFull {
//
//	private GameFieldModel model;
//
//	private final int MAXVAR;
//	private final int NBCLAUSES;
//
//	private WeightedMaxSatDecorator solver;
//
//	private ArrayList<Integer> lampPosTrueList;
//
//	private ArrayList<Integer> lampPosTrueAndOtherFalseList;
//
//	private VecInt modelList;
//
//	private GameFieldVarManager vars;
//
//	/**
//	 * Initializes a new Instance of a Akari solver
//	 * 
//	 * @param model
//	 *            the model of the game to be solved
//	 * @param timeout
//	 *            timeout of the solve process
//	 * @throws ContradictionException
//	 *             Is thrown when a gamefield is not solvable even if no lamps
//	 *             are placed
//	 */
//	public AkariSolverFull(final GameFieldModel model) throws ContradictionException {
//
//		this.model = model;
//		vars = new GameFieldVarManager(model.getWidth(), model.getHeight());
//		this.MAXVAR = vars.lastVar() + 1;
//		this.NBCLAUSES = model.getWidth() * model.getHeight() * 20;
//
//		this.solver = new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newDefault());
//
//		// this.solver.setTimeout(100000);
//
//		this.solver.newVar(MAXVAR);
//		this.solver.setExpectedNumberOfClauses(this.NBCLAUSES);
//		this.solver.setDBSimplificationAllowed(true);
//		this.solver.setKeepSolverHot(true);
//		this.solver.setVerbose(true);
//
//		this.lampPosTrueList = new ArrayList<Integer>();
//		this.lampPosTrueAndOtherFalseList = new ArrayList<Integer>();
//		
//		this.updateLamps();
//
//		this.setModel(model);
//		System.out.println(solver.nConstraints());
//
//	}
//
//	private void updateLamps() {
//
//		this.lampPosTrueList.clear();
//		this.lampPosTrueAndOtherFalseList.clear();
//
//		for (Point point : this.model.getLamps()) {
//			this.lampPosTrueList.add(vars.lampAt(point));
//			this.lampPosTrueAndOtherFalseList.add(vars.lampAt(point));
//		}
//
//		for (int i = 0; i < this.model.getWidth(); i++) {
//			for (int j = 0; j < this.model.getHeight(); j++) {
//				if (!this.lampPosTrueList.contains(vars.lampAt(i, j))) {
//					this.lampPosTrueAndOtherFalseList.add(-vars.lampAt(i, j));
//				}
//			}
//		}
//	}
//
//	void updateModel() throws ContradictionException {
//		this.setModel(this.model);
//	}
//
//	private void setModel(final GameFieldModel model) throws ContradictionException {
//
//		solver.clearLearntClauses();
//
//		setRules();
//
//		modelList = new VecInt(this.model.getWidth() * this.model.getHeight());
//
//		for (int i = 0; i < this.model.getWidth(); i++) {
//			for (int j = 0; j < this.model.getHeight(); j++) {
//				
//				
//				
//				switch (model.getPuzzleCellState(i, j)) {
//				case BARRIER:
//					this.modelList.push(vars.barrierAt(i, j));
//					break;
//				case BLANK:
//					this.modelList.push(vars.placeableAt(i, j));
//					break;
//				case BLOCK0:
//					this.modelList.push(vars.blockAt(0, i, j));
//					break;
//				case BLOCK1:
//					this.modelList.push(vars.blockAt(1, i, j));
//					break;
//				case BLOCK2:
//					this.modelList.push(vars.blockAt(2, i, j));
//					break;
//				case BLOCK3:
//					this.modelList.push(vars.blockAt(3, i, j));
//					break;
//				case BLOCK4:
//					this.modelList.push(vars.blockAt(4, i, j));
//					break;
//
//				default:
//					break;
//				}
//			}
//		}
//
//	}
//
//	private void exaclyOneTrue(int[] literals) throws ContradictionException {
//
//		solver.addHardClause(new VecInt(literals));
//
//		for (int i = 0; i < literals.length; i++) {
//			for (int j = i + 1; j < literals.length; j++) {
//				solver.addHardClause(new VecInt(new int[] { -literals[i], -literals[j] }));
//			}
//		}
//	}
//
//	private void setRules() throws ContradictionException {
//
//		// this.solver.clearLearntClauses();
//
//		solver.addHardClause(new VecInt(new int[] { -vars.falseVar() }));
//
//		// solver.gateTrue(vars.trueVar());
//
//		for (int i = 0; i < this.model.getWidth(); i++) {
//			for (int j = 0; j < this.model.getHeight(); j++) {
//
//				// a field can only be one type at the same time
//				exaclyOneTrue(new int[] { vars.blankAt(i, j), vars.lampAt(i, j), vars.barrierAt(i, j), vars.blockAt(0, i, j), vars.blockAt(1, i, j), vars.blockAt(2, i, j), vars.blockAt(3, i, j), vars.blockAt(4, i, j) });
//
//				// placeable is lamp or blank
//				this.solver.addHardClause(new VecInt(new int[] { -vars.placeableAt(i, i), vars.blankAt(i, j), vars.lampAt(i, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { vars.placeableAt(i, i), -vars.blankAt(i, j), -vars.lampAt(i, j) }));
//
//				// light is caused by a ray
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lightAt(i, j), vars.leftRayAt(i, j), vars.rightRayAt(i, j), vars.upRayAt(i, j), vars.downRayAt(i, j) }));
//
//				// a ray causes a light
//				this.solver.addHardClause(new VecInt(new int[] { -vars.leftRayAt(i, j), vars.lightAt(i, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.rightRayAt(i, j), vars.lightAt(i, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.upRayAt(i, j), vars.lightAt(i, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.downRayAt(i, j), vars.lightAt(i, j) }));
//
//				// only blanks can be lighted
//				// win condition: cell lighted or a lamp
//				// light equals blank (added to variable manager)
//				// this.solver.addHardClause(new VecInt(new int[] {
//				// vars.blankAt(i,
//				// j), -vars.lightAt(i, j) }));
//				// this.solver.addHardClause(new VecInt(new int[] {
//				// -vars.blankAt(i,
//				// j), vars.lightAt(i, j) }));
//
//				// only blank can be lights (implies also the rays)
//				solver.addHardClause(new VecInt(new int[] { vars.blankAt(i, j), -vars.lightAt(i, j) }));
//
//				// lamp implies rays in all directions
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.blankAt(i + 1, j), vars.rightRayAt(i + 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.blankAt(i - 1, j), vars.leftRayAt(i - 1, j) }));
//
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.blankAt(i, j + 1), vars.downRayAt(i, j + 1) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.blankAt(i, j - 1), vars.upRayAt(i, j - 1) }));
//
//				// lamps cannot have lamps as neightbors
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.lampAt(i + 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.lampAt(i - 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.lampAt(i, j + 1) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.lampAt(i, j - 1) }));
//
//				// lamps cannot be lighted on by rays
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.rightRayAt(i - 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.leftRayAt(i + 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.upRayAt(i, j + 1) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.downRayAt(i, j - 1) }));
//
//				// Rays imply other rays left(i,j)->left(i-1,j)
//				this.solver.addHardClause(new VecInt(new int[] { -vars.leftRayAt(i, j), -vars.blankAt(i - 1, j), vars.lampAt(i - 1, j), vars.leftRayAt(i - 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.rightRayAt(i, j), -vars.blankAt(i + 1, j), vars.lampAt(i + 1, j), vars.rightRayAt(i + 1, j) }));
//
//				this.solver.addHardClause(new VecInt(new int[] { -vars.upRayAt(i, j), -vars.blankAt(i, j - 1), vars.lampAt(i, j - 1), vars.upRayAt(i, j - 1) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.downRayAt(i, j), -vars.blankAt(i, j + 1), vars.lampAt(i, j + 1), vars.downRayAt(i, j + 1) }));
//
//				// rays only when caused by a lamp or an other ray from the
//				// opposite site
//				this.solver.addHardClause(new VecInt(new int[] { -vars.leftRayAt(i, j), vars.leftRayAt(i + 1, j), vars.lampAt(i + 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.rightRayAt(i, j), vars.rightRayAt(i - 1, j), vars.lampAt(i - 1, j) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.upRayAt(i, j), vars.upRayAt(i, j + 1), vars.lampAt(i, j + 1) }));
//				this.solver.addHardClause(new VecInt(new int[] { -vars.downRayAt(i, j), vars.downRayAt(i, j - 1), vars.lampAt(i, j - 1) }));
//
//				// lights cannot be lighted by other
//				// no lamp and light
//				this.solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j), -vars.lightAt(i, j) }));
//
//				this.addBlockDefinition(i, j);
//
//			}
//
//		}
//
//	}
//
//	public LinkedList<IConstr> addBlockDefinition(final int i, final int j) throws ContradictionException {
//		LinkedList<IConstr> constr = new LinkedList<IConstr>();
//
//		// case BLOCK0:
//
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(0, i, j), -vars.lampAt(i + 1, j) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(0, i, j), -vars.lampAt(i, j + 1) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(0, i, j), -vars.lampAt(i - 1, j) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(0, i, j), -vars.lampAt(i, j - 1) })));
//
//		// case BLOCK4:
//
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(4, i, j), vars.lampAt(i + 1, j) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(4, i, j), vars.lampAt(i, j + 1) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(4, i, j), vars.lampAt(i - 1, j) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(4, i, j), vars.lampAt(i, j - 1) })));
//
//		// case BLOCK1:
//
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), vars.lampAt(i + 1, j), vars.lampAt(i - 1, j), vars.lampAt(i, j + 1), vars.lampAt(i, j - 1) })));
//
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i - 1, j) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i, j + 1) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i, j - 1) })));
//
//		// solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i - 1,
//		// j), -vars.lampAt(i + 1, j) }));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), -vars.lampAt(i - 1, j), -vars.lampAt(i, j + 1) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), -vars.lampAt(i - 1, j), -vars.lampAt(i, j - 1) })));
//
//		// solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j +
//		// 1), -vars.lampAt(i + 1, j) }));
//		// solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j +
//		// 1), -vars.lampAt(i - 1, j) }));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(1, i, j), -vars.lampAt(i, j + 1), -vars.lampAt(i, j - 1) })));
//
//		// solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j -
//		// 1), -vars.lampAt(i + 1, j) }));
//		// solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j -
//		// 1), -vars.lampAt(i - 1, j) }));
//		// solver.addHardClause(new VecInt(new int[] { -vars.lampAt(i, j -
//		// 1), -vars.lampAt(i, j + 1) }));
//
//		// case BLOCK3:
//
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i - 1, j), -vars.lampAt(i, j + 1), -vars.lampAt(i, j - 1) })));
//
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), vars.lampAt(i + 1, j), vars.lampAt(i - 1, j) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), vars.lampAt(i + 1, j), vars.lampAt(i, j + 1) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), vars.lampAt(i + 1, j), vars.lampAt(i, j - 1) })));
//
//		// solver.addHardClause(new VecInt(new int[] { vars.lampAt(i - 1, j),
//		// vars.lampAt(i + 1, j) }));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), vars.lampAt(i - 1, j), vars.lampAt(i, j + 1) })));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), vars.lampAt(i - 1, j), vars.lampAt(i, j - 1) })));
//
//		// solver.addHardClause(new VecInt(new int[] { vars.lampAt(i, j + 1),
//		// vars.lampAt(i + 1, j) }));
//		// solver.addHardClause(new VecInt(new int[] { vars.lampAt(i, j + 1),
//		// vars.lampAt(i - 1, j) }));
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(3, i, j), vars.lampAt(i, j + 1), vars.lampAt(i, j - 1) })));
//
//		// solver.addHardClause(new VecInt(new int[] { vars.lampAt(i, j - 1),
//		// vars.lampAt(i + 1, j) }));
//		// solver.addHardClause(new VecInt(new int[] { vars.lampAt(i, j - 1),
//		// vars.lampAt(i - 1, j) }));
//		// solver.addHardClause(new VecInt(new int[] { vars.lampAt(i, j - 1),
//		// vars.lampAt(i, j + 1) }));
//
//		// case BLOCK2:
//
//		// ( a + b + c ) * ( a + b + d ) * ( a + c + d ) * ( b + c +
//		// d ) *
//		// ( !a + !b + !c ) *( !a + !b + !d ) * ( !a + !c + !d ) * (
//		// !b + !c + !d )
//
//		// ( a + b + c )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), vars.lampAt(i + 1, j), vars.lampAt(i - 1, j), vars.lampAt(i, j + 1) })));
//		// ( a + b + d )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), vars.lampAt(i + 1, j), vars.lampAt(i - 1, j), vars.lampAt(i, j - 1) })));
//		// ( a + c + d )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), vars.lampAt(i + 1, j), vars.lampAt(i, j + 1), vars.lampAt(i, j - 1) })));
//		// ( b + c + d )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), vars.lampAt(i - 1, j), vars.lampAt(i, j + 1), vars.lampAt(i, j - 1) })));
//
//		// ( !a + !b + !c )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i - 1, j), -vars.lampAt(i, j + 1) })));
//		// ( !a + !b + !d )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i - 1, j), -vars.lampAt(i, j - 1) })));
//		// ( !a + !c + !d )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), -vars.lampAt(i + 1, j), -vars.lampAt(i, j + 1), -vars.lampAt(i, j - 1) })));
//		// ( !b + !c + !d )
//		constr.add(this.solver.addHardClause(new VecInt(new int[] { -vars.blockAt(2, i, j), -vars.lampAt(i - 1, j), -vars.lampAt(i, j + 1), -vars.lampAt(i, j - 1) })));
//
//		return constr;
//
//	}
//
//	public void removeDefinition(final LinkedList<IConstr> constr) {
//
//		for (IConstr c : constr) {
//			this.solver.removeConstr(c);
//		}
//	}
//
//	/**
//	 * Returns true when the Akari game is solved with the lamps currently
//	 * placed on the gamefield.
//	 * 
//	 * @return true when the Akari game is solved otherwise false;
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public boolean isSolved() throws TimeoutException {
//
//		this.updateLamps();
//
//		return this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueAndOtherFalseList)));
//
//	}
//
//	/**
//	 * Returns true when the Akari puzzle is solvable (without the currently
//	 * placed lamps)
//	 * 
//	 * @return true when the Akari game is solvable otherwise false;
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public boolean isSatisfiable() throws TimeoutException {
//
//		return this.solver.isSatisfiable(modelList);
//
//	}
//
//	/**
//	 * Returns true when the Akari puzzle is solvable (with the currently placed
//	 * lamps)
//	 * 
//	 * @return true when the Akari game is solvable otherwise false;
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public boolean isSatisfiableWithCurrentLamps() throws TimeoutException {
//
//		this.updateLamps();
//		return this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueList)));
//
//	}
//
//	/**
//	 * Returns the lamps which lead to an unsolvable puzzle
//	 * 
//	 * @return true the lamps which lead to an unsolvable puzzle
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public LinkedList<Point> getWrongPlacedLamp() throws TimeoutException {
//
//		this.updateLamps();
//
//		LinkedList<Point> list = null;
//
//		if (this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueList)))) {
//			return null;
//		} else {
//
//			IVecInt errors = this.solver.unsatExplanation();
//			System.out.println("Not satisfiable");
//
//			if (errors == null) {
//				return null;
//			}
//
//			list = new LinkedList<Point>();
//
//			for (int i = 0; i < errors.size(); i++) {
//
//				if (vars.reverseVarBlock(errors.get(i)) != VarBlocks.LAMP)
//					continue;
//
//				Point p = vars.reverseVarPoint(errors.get(i));
//
//				if (p != null) {
//					list.add(p);
//				}
//			}
//		}
//
//		return list;
//
//	}
//
//	/**
//	 * Returns all the lamps which lead to an unsolvable puzzle
//	 * 
//	 * @return true the lamps which lead to an unsolvable puzzle
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public LinkedList<Point> getAllWrongPlacedLamps() throws TimeoutException {
//
//		this.updateLamps();
//
//		LinkedList<Point> list = null;
//
//		ArrayList<Integer> l = new ArrayList<Integer>(this.lampPosTrueList);
//
//		list = new LinkedList<Point>();
//		while (!this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(l)))) {
//
//			IVecInt errors = this.solver.unsatExplanation();
//
//			if (errors == null) {
//				return null;
//			}
//
//			for (int i = 0; i < errors.size(); i++) {
//
//				l.remove((Integer) errors.get(i));
//				l.add(-errors.get(i));
//
//				if (vars.reverseVarBlock(errors.get(i)) != VarBlocks.LAMP)
//					continue;
//
//				Point p = vars.reverseVarPoint(errors.get(i));
//
//				if (p != null) {
//					list.add(p);
//				}
//
//			}
//		}
//
//		return list;
//
//	}
//
//	public boolean hasWrongPlacedLamps() throws TimeoutException {
//
//		List<Point> list = this.getWrongPlacedLamp();
//
//		return list != null && list.size() > 0;
//	}
//
//	/**
//	 * Returns the lamps which are needed to complete the current game
//	 * 
//	 * @return the lamps which are needed to complete the current game
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public LinkedList<Point> getHints() throws TimeoutException {
//
//		this.updateLamps();
//
//		if (this.solver.isSatisfiable(new VecInt(AkariSolverFull.toIntArray(this.lampPosTrueList)))) {
//
//			LinkedList<Point> list = new LinkedList<Point>();
//			int[] model = this.solver.model();
//
//			for (int i = 0; i < model.length; i++) {
//				if (vars.reverseVarBlock(model[i]) != VarBlocks.LAMP)
//					continue;
//
//				Point p = vars.reverseVarPoint(model[i]);
//
//				if (p != null) {
//					if (this.model.isLampAt(p)) {
//						list.add(p);
//					}
//				}
//			}
//
//			return list;
//		} else {
//			return null;
//		}
//
//	}
//
//	/**
//	 * Returns an arbitrary combination of lamps which are needed to complete
//	 * the complete puzzle
//	 * 
//	 * @return an arbitrary combination of lamps which are needed to complete
//	 *         the complete puzzle
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public LinkedList<Point> getSolution() throws TimeoutException {
//
//		if (this.solver.isSatisfiable(modelList)) {
//
//			LinkedList<Point> list = new LinkedList<Point>();
//			int[] model = this.solver.model();
//
//			for (int i = 0; i < model.length; i++) {
//				if (vars.reverseVarBlock(model[i]) != VarBlocks.LAMP)
//					continue;
//
//				Point p = vars.reverseVarPoint(model[i]);
//
//				if (p != null) {
//					list.add(p);
//				}
//			}
//
//			return list;
//		} else {
//			return null;
//		}
//
//	}
//
//	/**
//	 * Returns an arbitrary model which is needed to complete the complete
//	 * puzzle
//	 * 
//	 * @return an arbitrary model which is needed to complete the complete
//	 *         puzzle
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public GameFieldModel getSolutionModel() throws TimeoutException {
//
//		GameFieldModel clone = (GameFieldModel) this.model.clone();
//
//		if (this.solver.isSatisfiable(modelList)) {
//
//			int[] model = this.solver.model();
//
//			for (int i = 0; i < model.length; i++) {
//				if (vars.reverseVarBlock(model[i]) != VarBlocks.LAMP)
//					continue;
//
//				Point p = vars.reverseVarPoint(model[i]);
//
//				if (p != null) {
//					clone.setLampAt(p);
//				}
//			}
//
//			return clone;
//		} else {
//			return null;
//		}
//
//	}
//
//	/**
//	 * Inserts the solution of the puzzle in the current model.
//	 * 
//	 * @throws TimeoutException
//	 *             Timeout expired
//	 */
//	public void setSolutionToModel() throws TimeoutException {
//
//		if (this.solver.isSatisfiable(modelList)) {
//
//			int[] model = this.solver.model();
//
//			for (int i = 0; i < model.length; i++) {
//
//				if (model[i] > 0 && vars.reverseVarBlock(model[i]) == VarBlocks.BLOCK4 || (vars.reverseVarBlock(model[i]) == VarBlocks.BLOCK3 || vars.reverseVarBlock(model[i]) == VarBlocks.BLOCK2 || vars.reverseVarBlock(model[i]) == VarBlocks.BLOCK1 || vars.reverseVarBlock(model[i]) == VarBlocks.BLOCK0 || vars.reverseVarBlock(model[i]) == VarBlocks.LAMP || vars.reverseVarBlock(model[i]) == VarBlocks.BARRIER)) {
//					System.out.println(vars.reverseVarPoint(model[i]) + " " + vars.reverseVarBlock(model[i]));
//				}
//
//				if (model[i] < 0 || vars.reverseVarBlock(model[i]) != VarBlocks.LAMP)
//					continue;
//
//				Point p = vars.reverseVarPoint(model[i]);
//				if (p != null) {
//					this.model.setLampAt(p);
//				}
//			}
//
//		}
//
//	}
//
//	private static int[] toIntArray(final List<Integer> integerList) {
//		int[] intArray = new int[integerList.size()];
//		for (int i = 0; i < integerList.size(); i++) {
//			intArray[i] = integerList.get(i);
//		}
//		return intArray;
//	}
//
//	private static Puzzle generateRandomBlockModel(final int width, final int height) {
//
//		Random r = new Random();
//		Puzzle model = new Puzzle(width, height);
//
//		int count = (width + height) + (r.nextInt((width * height - (width + height)) / 3));
//
//		for (int i = 0; i < count; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) == CellState.BARRIER) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BARRIER);
//			}
//		}
//
//		return model;
//	}
//
//	private Puzzle generateSatisfiablePuzzle(Puzzle p) throws TimeoutException {
//
//		VecInt literals = new VecInt();
//		VecInt weights = new VecInt();
//		for (int i = 0; i < this.model.getWidth(); i++) {
//			for (int j = 0; j < this.model.getHeight(); j++) {
//				
//
//				switch (p.getCellState(i, j)) {
//				case BARRIER:
//					literals.push(vars.barrierAt(i, j));
//					weights.push(-5);
//					break;
//				case BLANK:
//					literals.push(vars.placeableAt(i, j));
//					weights.push(-5);
//					break;
//				case BLOCK0:
//					literals.push(vars.blockAt(0, i, j));
//					weights.push(-5);
//					break;
//				case BLOCK1:
//					literals.push(vars.blockAt(1, i, j));
//					weights.push(-5);
//					break;
//				case BLOCK2:
//					literals.push(vars.blockAt(2, i, j));
//					weights.push(-5);
//					break;
//				case BLOCK3:
//					literals.push(vars.blockAt(3, i, j));
//					weights.push(-5);
//					break;
//				case BLOCK4:
//					literals.push(vars.blockAt(4, i, j));
//					weights.push(-5);
//					break;
//
//				default:
//					break;
//				}
//			}
//		}
//
//		this.solver.addWeightedLiteralsToMinimize(literals, weights);
//
//		Puzzle res = new Puzzle(p.getWidth(), p.getHeight());
//		res.clear();
//		if (this.solver.isSatisfiable()) {
//			int[] model = solver.model();
//
//			for (int i = 0; i < model.length; i++) {
//
//				if (model[i] < 0)
//					continue;
//
//				VarBlocks block = vars.reverseVarBlock(model[i]);
//
//				if (block == null || block.toPuzzle() == null)
//					continue;
//
//				Point point = vars.reverseVarPoint(model[i]);
//
//				System.out.println(block + " " + point);
//
//				if (point != null) {
//					res.setCellState(point, block.toPuzzle());
//				}
//
//			}
//		}
//
//		return res;
//
//	}
//
//	private static Puzzle generateRandomModel(final int width, final int height, int num0Block, int num1Block, int num2Block, int num3Block, int num4Block) {
//
//		Random r = new Random();
//		Puzzle model = new Puzzle(width, height);
//
//		int count = 5;
//
//		for (int i = 0; i < count; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) != CellState.BLANK) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BARRIER);
//			}
//		}
//
//		for (int i = 0; i < num0Block; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) != CellState.BLANK) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BLOCK0);
//			}
//		}
//
//		for (int i = 0; i < num1Block; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) != CellState.BLANK) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BLOCK1);
//			}
//		}
//
//		for (int i = 0; i < num2Block; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) != CellState.BLANK) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BLOCK2);
//			}
//		}
//
//		for (int i = 0; i < num3Block; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) != CellState.BLANK) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BLOCK3);
//			}
//		}
//
//		for (int i = 0; i < num4Block; i++) {
//			int x = r.nextInt(width);
//			int y = r.nextInt(height);
//
//			if (model.getCellState(x, y) != CellState.BLANK) {
//				i--;
//			} else {
//				model.setCellState(x, y, CellState.BLOCK4);
//			}
//		}
//
//		return model;
//
//	}
//
//	public static Puzzle generatePuzzle(final int width, final int height) {
//
//		Puzzle puzzle = AkariSolverFull.generateRandomModel(width, height, 5, 5, 5, 5, 5);
//
//		AkariSolverFull solver;
//		try {
//			solver = new AkariSolverFull(new GameFieldModel(puzzle));
//
//			return solver.generateSatisfiablePuzzle(puzzle);
//		} catch (ContradictionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (TimeoutException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//
//		// GameFieldModel model = new GameFieldModel(puzzle);
//		// Random r = new Random();
//
//		// try {
//		//
//		// AkariSolverFull solver = new AkariSolverFull(model);
//		//
//		// SingleSolutionDetector ssd = new
//		// SingleSolutionDetector(solver.solver);
//		//
//		// if (solver.isSatisfiable()) {
//		//
//		// while (!ssd.hasASingleSolution()) {
//		//
//		// System.out.println("satisfiable");
//		// solver.updateModel();
//		//
//		// int[] model1 = solver.solver.model();
//		//
//		// VecInt a = new VecInt();
//		// for (int i = 0; i < model1.length; i++) {
//		// a.push(-model1[i]);
//		// }
//		//
//		// IConstr added = solver.solver.addHardClause(a);
//		//
//		// int[] model2 = solver.solver.model();
//		//
//		// solver.solver.removeConstr(added);
//		//
//		// LinkedList<Integer> intersectModel = new LinkedList<Integer>();
//		// Collections.shuffle(intersectModel);
//		//
//		// for (int i = 0; i < 1; i++) {
//		// if (model1[i] > 0 && !Arrays.asList(model2).contains(model1[i])) {
//		// intersectModel.add(model1[i]);
//		// }
//		// }
//		//
//		// System.out.println(intersectModel.size());
//		//
//		// for (int i = 0; i < intersectModel.size(); i++) {
//		//
//		// if (solver.vars.reverseVarBlock(intersectModel.get(i)) !=
//		// VarBlocks.LAMP)
//		// continue;
//		//
//		// Point p = solver.vars.reverseVarPoint(intersectModel.get(i));
//		//
//		// if (p != null) {
//		// System.out.println("Fixing:" + p);
//		//
//		// List<Point> points = puzzle.getNeightbors(p, CellState.BARRIER);
//		//
//		// if (points.size() == 0) {
//		// points = puzzle.getNeightbors(p, CellState.BLANK);
//		// }
//		//
//		// if (points.size() == 0) {
//		// continue;
//		// }
//		//
//		// Point barrier = points.get(r.nextInt(points.size()));
//		//
//		// puzzle.setCellState(barrier,
//		// CellState.getBlockByNumber(model.getLampNeightbors(barrier).size()));
//		//
//		// LinkedList<IConstr> constr1 = solver.addBlockDefinition(barrier.x,
//		// barrier.y);
//		//
//		// if (!solver.isSatisfiable()) {
//		// System.out.println("!satisfiable after first steps....");
//		// solver.removeDefinition(constr1);
//		// puzzle.setCellState(barrier, CellState.BARRIER);
//		//
//		// }
//		// }
//		//
//		// }
//		//
//		// solver.updateModel();
//		//
//		// }
//		// }
//		//
//		// } catch (ContradictionException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (TimeoutException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//		//
//		// return model;
//
//	}
//
//	public GameFieldModel getModel() {
//		return this.model;
//	}
//
// }
