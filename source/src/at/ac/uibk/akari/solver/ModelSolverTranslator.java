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

	public ModelSolverTranslator(final GameFieldModel model) {
		super(model.getWidth(), model.getHeight());

		model.addModelChangeListener(this);
		Puzzle p = model.getPuzzle();
		model.getPuzzle().addModelChangeListener(this);

		for (int i = 0; i < model.getWidth(); i++) {
			for (int j = 0; j < model.getHeight(); j++) {

				int modelVar = this.getVar(VarBlocks.fromPuzzle(p.getCellState(i, j)).getValue(), i, j);
				this.puzzleAllFixed.push(modelVar);
				this.puzzleFixedLampsChangable.push(modelVar);

				if (p.getCellState(i, j) != CellState.BLANK) {
					this.puzzleAddableNumberBlocks.push(this.getVar(VarBlocks.fromPuzzle(p.getCellState(i, j)).getValue(), i, j));
				}

				if (p.getCellState(i, j) != CellState.BARRIER) {
					this.puzzleAddableNumberBlocks.push(-this.getVar(VarBlocks.fromPuzzle(p.getCellState(i, j)).getValue(), i, j));
				}

				if (model.isLampAt(i, j)) {
					this.puzzleFixedLampsChangable.push(this.lampAt(i, j));
					this.puzzleFixedLamps.push(this.lampAt(i, j));
				} else {
					this.puzzleFixedLamps.push(-this.lampAt(i, j));
				}
			}
		}
	}

	public void removePosition(final VecInt vect, final int x, final int y) {

		int modulo = this.getBlocksize();
		for (int i = 0; i < vect.size(); i++) {
			if (((Math.abs(vect.get(i)) - this.CONSTANT_COUNT) % modulo) == (this.getVar(0, x, y) - this.CONSTANT_COUNT)) {
				vect.delete(i);
				i--;
			}
		}

	}

	@Override
	public void puzzleCellChanged(final int x, final int y, final CellState state) {
		this.removePosition(this.puzzleAllFixed, x, y);
		this.puzzleAllFixed.push(this.getVar(VarBlocks.fromPuzzle(state).getValue(), x, y));
	}

	@Override
	public void lampRemoved(final int x, final int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lampAdded(final int x, final int y) {
		// TODO Auto-generated method stub

	}

}
