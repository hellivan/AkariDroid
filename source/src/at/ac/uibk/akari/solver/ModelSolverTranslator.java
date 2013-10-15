package at.ac.uibk.akari.solver;

import org.sat4j.core.VecInt;

import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.ModelChangeListener;
import at.ac.uibk.akari.core.Puzzle;
import at.ac.uibk.akari.core.Puzzle.CellState;

public class ModelSolverTranslator extends GameFieldVarManager implements ModelChangeListener {

	private VecInt puzzleAllFixed = new VecInt();
	private VecInt puzzleAddableNumberBlocks = new VecInt();

	private VecInt puzzleFixedLampsChangable = new VecInt();
	private VecInt puzzleFixedLamps = new VecInt();

	public ModelSolverTranslator(GameFieldModel model) {
		super(model.getWidth(), model.getHeight());

		model.setModelChangeListener(this);
		Puzzle p = model.getPuzzle();
		model.getPuzzle().setModelChangeListener(this);

		for (int i = 0; i < model.getWidth(); i++) {
			for (int j = 0; j < model.getHeight(); j++) {

				int modelVar = getVar(VarBlocks.fromPuzzle(p.getCellState(i, j)).getValue(), i, j);
				puzzleAllFixed.push(modelVar);
				puzzleFixedLampsChangable.push(modelVar);

				if (p.getCellState(i, j) != CellState.BLANK)
					puzzleAddableNumberBlocks.push(getVar(VarBlocks.fromPuzzle(p.getCellState(i, j)).getValue(), i, j));

				if (p.getCellState(i, j) != CellState.BARRIER)
					puzzleAddableNumberBlocks.push(-getVar(VarBlocks.fromPuzzle(p.getCellState(i, j)).getValue(), i, j));

				if (model.isLampAt(i, j)) {
					puzzleFixedLampsChangable.push(lampAt(i, j));
					puzzleFixedLamps.push(lampAt(i, j));
				} else {
					puzzleFixedLamps.push(-lampAt(i, j));
				}
			}
		}
	}

	public void removePosition(VecInt vect, int x, int y) {

		int modulo = this.getBlocksize();
		for (int i = 0; i < vect.size(); i++) {
			if (((Math.abs(vect.get(i)) - this.CONSTANT_COUNT) % modulo) == (getVar(0, x, y) - this.CONSTANT_COUNT)) {
				vect.delete(i);
				i--;
			}
		}

	}

	@Override
	public void puzzleCellChanged(int x, int y, CellState state) {
		removePosition(this.puzzleAllFixed, x, y);
		puzzleAllFixed.push(getVar(VarBlocks.fromPuzzle(state).getValue(), x, y));
	}

	@Override
	public void lampRemoved(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lampAdded(int x, int y) {
		// TODO Auto-generated method stub

	}

}
