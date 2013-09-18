/**
 * 
 */


import java.awt.Point;

/**
 * @author ateam
 *
 */
public class DummyAI extends CKPlayer {

	public DummyAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "DummyAI";
	}

	@Override
	public Point getMove(BoardModel state) {
		for(int i=0; i<state.width; ++i)
			for(int j=0; j<state.height; ++j)
				if(state.pieces[i][j] == 0)
					return new Point(i,j);
		return null;
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}

}
