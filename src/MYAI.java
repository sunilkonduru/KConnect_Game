import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class MYAI extends CKPlayer {
	
	private int aiBot=2;
	private int userPlayer=1;
	private int empty=0;
	int noOfRows;
	int noOfColumns;
	int noOfContinuousKeysForWin=4;
	final int PENALIZING_CONSTANT=30;
	int globalBoard[][];
	MyAIMove move=new MyAIMove();
	//change this playerMove if you want bot to start first.
	public boolean playerMove=false;
	int bRow,bCol;
	boolean flag=false;
	int opCount=0;
	
	ArrayList<Point> storePositions;	
	

	public ArrayList<Point> getPositions(byte [][] pieces){
		storePositions = new ArrayList<Point>();
		for(int i=noOfRows-1;i>=0;i++){
			for(int j=0;j<noOfColumns;j++){
				if(pieces[i][j]==0){
					Point p = new Point();
					p.x=i;p.y=j;
					storePositions.add(p);
				}
			}
		}
		return storePositions;
	}
	public BoardModel iterativeDeep(BoardModel state,boolean useAlpahaBetaHeuristic){
		int depth = 0;
		//boolean playerSwitch=true;

		long finishTime, totalTime, anticipatedTime;
		int holdMax = Integer.MIN_VALUE;
		long startTime = System.nanoTime(); 
		BoardModel checkFor=null, returnState=null;
		while(true){
			if(useAlpahaBetaHeuristic)
			{
			cutOpponentWins(state);
			if(flag==true)
			{
			BoardModel bm=(BoardModel)state.clone();
			Point p=new Point(bRow,bCol);
			bm.lastMove=p;
			flag=false;
			return bm;
			}
			else
			{
			checkFor=findABestMove(state);
			return checkFor;
			}
			}
			else
			{
			ArrayList<BoardModel> plausibleStates = generateNodesList(state, (byte)player);
			for(int i=0;i<plausibleStates.size();i++){ 
				 returnState = depthLimitedSearch(plausibleStates.get(i), depth);
				 if(returnState.value>holdMax){
						holdMax = returnState.value;
				 		checkFor=	returnState;
						
				}
				
			}
			}
			finishTime = System.nanoTime();
			totalTime =  finishTime - startTime;
			totalTime /= 1000000000.0;
			if(state.gravityEnabled())
			{
			 anticipatedTime = totalTime*noOfColumns;
			}
			else
			{
				anticipatedTime=totalTime*noOfRows*noOfColumns;
			}
			if(anticipatedTime>20){
				return checkFor;
			}else{
				depth++;
			}
		}
	}
	
	 public ArrayList<BoardModel> generateNodesList(BoardModel state, byte player){
		ArrayList<BoardModel> nodes = new ArrayList<BoardModel>();
        ArrayList<MyAIMove> move= findAllValidMoves(state);
        Point p=new Point();
        for(MyAIMove mov:move)
        {
        	
          p.x=mov.row;
          p.y=mov.column;
          BoardModel newState=(BoardModel)state.clone();
          newState=newState.placePiece(p,player);
         // newState.lastMove=p;
          nodes.add(newState);
          
        }	
		return nodes;
	}

	public BoardModel depthLimitedSearch(BoardModel n, int depth){
		int holdMax = Integer.MIN_VALUE, holdMin = Integer.MAX_VALUE;
		BoardModel returnState=null;
		ArrayList<BoardModel> plausibleStates=null;
		System.out.println(depth);
		if(depth == 0){
			n.value = evaluate(n);
			return n;
		}
		
		//player = 2;										
		if(isAIturn())
			player = 2;
		else
			player=1;
		plausibleStates = generateNodesList(n, player);
		for(int i=0;i<plausibleStates.size();i++){
			BoardModel node = plausibleStates.get(i);
			 
			 for (int j = 0; j< noOfRows; j++) {
				  for(int k=0;k<noOfColumns;k++)
				  {
					  globalBoard[j][k]=node.pieces[j][k];
				  }
			 }
			 selectPlayerAlternatively();    
			 BoardModel validState = depthLimitedSearch(node, depth-1);
			 if(validState!=null)
				node.value = validState.value;
			else{
				
				node.value=0;
			}
			
		}
		if(isAIturn()){
			for(int i=0; i> plausibleStates.size();i++){
				if(holdMax<plausibleStates.get(i).value){
					holdMax=plausibleStates.get(i).value;
					returnState = plausibleStates.get(i);
				}
			}
		}else{
			for(int i=0; i< plausibleStates.size();i++){
				if(holdMin>plausibleStates.get(i).value){
					holdMin=plausibleStates.get(i).value;
					returnState = plausibleStates.get(i);
				}
			}
		}
		return returnState;
	}	
	
	
	public MYAI(byte player, BoardModel state) {
		super(player, state);
		noOfRows=state.width;
		noOfColumns=state.height;
		globalBoard=new int[noOfRows][noOfColumns];
	}
	public Point getMove(BoardModel state)
	{
		globalBoard=new int[noOfRows][noOfColumns];
		int depth=4;
		Point p=new Point();
		// we are assuming that during initial it is always better to insert the element in the middle, just improving a bit flexibility
		if(isInitialState(state))
		{
			if(!state.gravity)
			{
				if(!playerMove)
				{
					p.x=noOfRows/2;
					p.y=noOfColumns/2;
				}
					
			}
			else
			{
				if(!playerMove)
				{
					p.x=0;
					p.y=noOfColumns/2;
				}
			}
			return p; 
		}

		return getMove(state,5000);
	}
	public Point getMove(BoardModel state, int deadline)
	{
		//int depth=6;
		BoardModel bm=iterativeDeep(state,true);
		//should call iterative deepeing
		return bm.lastMove;
	}
	public BoardModel findABestMove(BoardModel state)
	{
		
		Point finalMove=null;
		int depth=3;
		ArrayList<MyAIMove> aList=findAllValidMoves(state);
		int bestAlphaBetaValueTillNow=Integer.MIN_VALUE;
		int currentValue=Integer.MIN_VALUE;
		BoardModel bestState=null;
		Point p=new Point();
		
		for(MyAIMove moveTemp:aList)
		{
			BoardModel newState=(BoardModel)state.clone();
			p.x=moveTemp.row;
			p.y=moveTemp.column;
			
			newState=newState.placePiece(p,(byte)2);
			//newState.lastMove=p;
			move.row=p.x;
			move.column=p.y;
			currentValue=alphaBetaPruning(newState,depth,Integer.MIN_VALUE,Integer.MAX_VALUE);
			if(currentValue>bestAlphaBetaValueTillNow)
			{
				bestState=newState;
				bestAlphaBetaValueTillNow=currentValue;
				finalMove=p;
			}
			
		}
		
		
		return bestState;
	}
	void cutOpponentWins(BoardModel bm){
		for(int i=noOfRows-1;i>=0;i--){
		int column = 0;
		do{
			int opponent=0;
			int AIbot = 0;
		
		    int j=0;
			while(j< noOfContinuousKeysForWin)
			{	
				
			if(bm.pieces[i][column + j] == 2){
			AIbot++;
			opponent=0;
			}
			else
			if(bm.pieces[i][column + j]==0){
			AIbot=0;
			opponent=0;
			}
			else 
			{
			AIbot=0;
			opponent=opponent+1;
			if(opponent==noOfContinuousKeysForWin-1 || opponent==noOfContinuousKeysForWin-2 && noOfContinuousKeysForWin-2!=1){
			if(opponent> opCount){
			flag=false;
			if((column +j+1) < noOfColumns) {
				if( bm.pieces[i][column +j+1]==0){
				    if(!(i+1 < noOfRows && (bm.gravity && bm.pieces[i+1][column+j+1]== 0))){
					flag=true;
					bRow=i;
					bCol = column + j+1;
					opCount=opponent;
					if(opCount>=1)
					System.out.println("Opcount =1");
				}
			}
			}
			else if((column +j-(noOfContinuousKeysForWin-1) >= 0) && bm.pieces[i][column +j-(noOfContinuousKeysForWin-1)] == 0)
			{
				if(!(i+1 < noOfRows && (bm.gravity && bm.pieces[i+1][column+j-(noOfContinuousKeysForWin-1)]== 0)) )
				{
					if(opponent>=1)
					System.out.println("Opcount =1");
				}	
				opCount = opponent;
				bRow = i;
				bCol = column + j-(noOfContinuousKeysForWin-1);
				flag=true;
				}
			}
			}
			}	
			j++;
			}
			column++;
		}while(noOfColumns-column>=noOfContinuousKeysForWin);
		}
		
		
		int i=0;
		while(i<noOfColumns)
		{
		int rows = 0;
		do{
				
		int opponent=0;
		int AIbot=0;
		for(int j=0; j< noOfContinuousKeysForWin;j++){
		
		if(j==0)
			opponent=0;
		if(bm.pieces[rows + j][i]==0){
		AIbot=0;
		opponent=0;
		}
		else	
		if(bm.pieces[rows + j][i]==2){
		opponent=0;
		AIbot++;
		}else{ 
		opponent++;
		AIbot=0;
		if(opponent==noOfContinuousKeysForWin-1 || opponent==noOfContinuousKeysForWin-2 && noOfContinuousKeysForWin-2!=1){
		if(opponent> opCount){
		if((rows +j+1) < noOfRows && bm.pieces[rows +j+1][i]==0){
		flag=true;
		bCol=i;
		bRow = rows + j+1;
		opCount=opponent;
		}else if((rows +j-(noOfContinuousKeysForWin-1) >= 0) && bm.pieces[rows +j-(noOfContinuousKeysForWin-1)][i] == 0){	
		flag=true;
		bCol=i;
		bRow = rows + j-(noOfContinuousKeysForWin-1);
		opCount=opponent;
		}
		}
		}
		}
		}rows++;
		}while(noOfRows-rows>=noOfContinuousKeysForWin);
		i++;
		}
		// Diagonals lrt
		i=0;
		for(i=noOfContinuousKeysForWin-1;i<=noOfRows-1;i++){
		int column = 0;
		do{
		int opponent=0;
		int AIbot=0;
		if(i==5)
		{
			System.out.println();
		}
		int l=0;
		int m=i;
		for(int j=column; l< noOfContinuousKeysForWin;j++ ){	
		if(bm.pieces[m][j]==2){
		opponent=0;
		AIbot++;
		}else if(bm.pieces[m][j] == 0)
		    {
		opponent=0;
		AIbot = 0;
		    }else{ 
		    opponent++;
		AIbot = 0;
	    if(opponent==noOfContinuousKeysForWin-1 || opponent==noOfContinuousKeysForWin-2 && noOfContinuousKeysForWin-2!=1){
		if(opponent> opCount){
		if(m-1 >= 0 && (j+1 < noOfColumns)&&bm.pieces[m-1][j+1]==0){
			
			//-1
		if(!((m-1 >= 0) && (j+1 < column) && bm.gravity && bm.pieces[m][j+1]== 0)){
		flag=true;
		bRow=m-1;
		bCol = j+1;
		opCount=opponent;
		}
		
		}
		else if((m+2 < noOfRows) &&(j-(noOfContinuousKeysForWin-2))>=0&&bm.pieces[m+2][j-(noOfContinuousKeysForWin-2)] == 0 )
		{
		opCount = opponent;
		bRow = m+2;
		bCol = j-(noOfContinuousKeysForWin-2);
		flag=true;
		}
		}
		  
	    }
		}	
		 m--; 
		 l++;
		}
		column++;
		}while(column<=(noOfColumns-noOfContinuousKeysForWin));
		}
		//diag rtf
		i=0;
		for(i=noOfContinuousKeysForWin-1;i<=noOfRows-1;i++){
		int column = noOfColumns-1;
		do{
		int opponent=0;
		int AIbot=0;
		int l=0,m=i;
		for(int j=column; l< noOfContinuousKeysForWin;j-- ){	
		if(bm.pieces[m][j]==2){
		AIbot+=1;
		opponent=0;
		}
		else
		if(bm.pieces[m][j]==0){
		opponent=0;
		AIbot=0;
		}else {
		opponent+=1;
		AIbot=0;	
		if(opponent==noOfContinuousKeysForWin-1 || opponent==noOfContinuousKeysForWin-2 && noOfContinuousKeysForWin-2!=1){
		if(opponent> opCount){
		if(m-1 >= 0 && (j-1 >= 0) && bm.pieces[m-1][j-1]==0){
		if(!(m < noOfRows && (j-1 >=0) && bm.gravity && bm.pieces[m][j-1]== 0)){
		flag=true;
		bRow=m-1;
		bCol = j-1;
		opCount=opponent;
		}
		}else if((m+(noOfContinuousKeysForWin-2) < noOfRows) && bm.pieces[m+2][j+(noOfContinuousKeysForWin-2)] == 0 && (j+(noOfContinuousKeysForWin-2))<column)
		{
		opCount = opponent;
		bRow = m+(noOfContinuousKeysForWin-2);
		bCol = j+(noOfContinuousKeysForWin-2);
		flag=true;
		}
		}
		}
		}	
		 m--;
		 l++;
		}
		column--;
		}while(column>=noOfContinuousKeysForWin-1);
		}
		opCount=0;	
		}
	
	public int evaluate(BoardModel state)
	{
		
        int eval=(evaluateHorizontal((state.pieces))
        		*chanceOfAHorizontalWin(move.row, move.column))*
        		chanceOfAHorizontalWinContinuous(move.row, move.column)
        		  + (evaluateVertical((state.pieces))
        				  *chanceOfAVerticalWin(move.row, move.column))*
        				  chanceOfAVerticalWinContinuous(move.row, move.column)
        		  + ((evaluateDiagonalLeftToRight((state.pieces))
        				  +evaluateDiagonalRightToLeft(state.pieces))*
        				  chanceOfADiagonalWin(move.row, move.column))
        				  *chanceOfADiagonalWin(move.row, move.column);
        		  
		return eval;
	}
	public int evaluateHorizontal(byte[][] currBoardState)
	{
		int heuristic=0;
		int rows=noOfRows-1;
		
		//for(int rows=noOfRows-1;rows>=0;rows--)
		while(rows>=0)
		{
			if(rows==2)
			     System.out.println("java");	
		  int indexColumn=0;	
		  while(noOfColumns-indexColumn>noOfContinuousKeysForWin)
		  {
			  int countOfAI=0;
			  int countOfPlayer=0;
			  //changing the values of the row and seeing whether the value is higher
			  //we are penalizing a row state if it is having many continuous values
			  for(int i=0;i<noOfContinuousKeysForWin;i++)
			  {
				  if(currBoardState[rows][indexColumn+i]==0)
				  {
					  countOfAI=0;
					  countOfPlayer=0;
				  }
				  else if(currBoardState[rows][indexColumn+i]==1)
				  {
					  countOfPlayer++;
					  heuristic=heuristic-PENALIZING_CONSTANT*countOfPlayer;
					  countOfAI=0;
				  }
				  else if(currBoardState[rows][indexColumn+i]==2)
				  {
					  countOfAI++;
					  heuristic=heuristic+PENALIZING_CONSTANT*countOfAI;
					  countOfPlayer=0;
					  
				  }
				  
			  }
			  indexColumn++;
		  }
		rows--;
		}
		
		return heuristic;
	}
	
	public int evaluateVertical(byte[][] currBoardState)
	{
		int heuristic=0;
		
		for(int cols=noOfColumns-1;cols>=0;cols--)
		{
		  int indexRow=0;	
		  while(noOfRows-indexRow>noOfContinuousKeysForWin)
		  {
			  int countOfAI=0;
			  int countOfPlayer=0;
			  //changing the values of the row and seeing whether the value is higher
			  //we are penalizing a row state if it is having many continuous values
			  for(int i=0;i<noOfContinuousKeysForWin;i++)
			  {
				  if(currBoardState[indexRow+i][cols]==0)
				  {
					  countOfAI=0;
					  countOfPlayer=0;
				  }
				  else if(currBoardState[indexRow+i][cols]==1)
				  {
					  countOfPlayer++;
					  heuristic=heuristic-PENALIZING_CONSTANT*countOfPlayer;
					  countOfAI=0;
				  }
				  else if(currBoardState[indexRow+i][cols]==2)
				  {
					  countOfAI++;
					  heuristic=heuristic+PENALIZING_CONSTANT*countOfAI;
					  countOfPlayer=0;
					  
				  }
				  
			  }
			  indexRow++;
			  
		  }
		}
		
		return heuristic;
	}
	public int evaluateDiagonalLeftToRight(byte[][] currBoardState)
	{
		int heuristic=0;
		//Say we need four elements continuously for a win situation then  we start at the
		//4th row and 0th column and check. We then go and check for 4th row 1st column. similarly we proceed
		// and we go to the row above it.
		for(int k=noOfContinuousKeysForWin-1;k<=noOfRows-1;k++)
		{
			if(k==6)
			{
			   System.out.println("hello");
			}
			int indexColumn=0;
			while(indexColumn<noOfColumns-noOfContinuousKeysForWin)
			{
				int countOfAI=0;
				int countOfPlayer=0;
				int columnToStartAt=k;
				int temp=0;
				for(int i=indexColumn;;i++)
				{
					if(temp==noOfContinuousKeysForWin)
					{
						break;
					}
					if(currBoardState[columnToStartAt][i]==0)
					{
						countOfPlayer=0;
						countOfAI=0;
					}
					else if(currBoardState[columnToStartAt][i]==1)
					{
						  countOfPlayer++;
						  heuristic=heuristic-PENALIZING_CONSTANT*countOfPlayer;
						  countOfAI=0;
					}
					 else if(currBoardState[columnToStartAt][i]==2)
					  {
						  countOfAI++;
						  heuristic=heuristic+PENALIZING_CONSTANT*countOfAI;
						  countOfPlayer=0;
						  
					  }
					temp++;
					columnToStartAt--;
				}
				
				indexColumn++;
			}
		}
		
		
		return heuristic;
	}
	
	public int evaluateDiagonalRightToLeft(byte[][] currBoardState)
	{
		int heuristic=0;
		//Say we need four elements continuously for a win situation then  we start at the
		//4th row and 0th column and check. We then go and check for 4th row 1st column. similarly we proceed
		// and we go to the row above it.
		for(int k=noOfContinuousKeysForWin-1;k<=noOfRows-1;k++)
		{
			int indexColumn=noOfColumns-1;
			while(indexColumn>=noOfContinuousKeysForWin-1)
			{
				int countOfAI=0;
				int countOfPlayer=0;
				int columnToStartAt=k;
				int temp=0;
				
				for(int i=indexColumn;;i--)
				{
					if(temp==noOfContinuousKeysForWin)
					{
						break;
					}
					if(currBoardState[columnToStartAt][i]==0)
					{
						countOfPlayer=0;
						countOfAI=0;
					}
					else if(currBoardState[columnToStartAt][i]==1)
					{
						  countOfPlayer++;
						  heuristic=heuristic-PENALIZING_CONSTANT*countOfPlayer;
						  countOfAI=0;
					}
					 else if(currBoardState[columnToStartAt][i]==2)
					  {
						  countOfAI++;
						  heuristic=heuristic+PENALIZING_CONSTANT*countOfAI;
						  countOfPlayer=0;
						  
					  }
					temp++;
					columnToStartAt--;
				}
				indexColumn--;
			}
		}
		
		
		return heuristic;
	}
	//we are weighing how good the position is using this method. If we find anyposition already filled with the opponent
	//then we wont consider this position. This is done by setting the counter to 0.
	//else if there is an empty space then consider it as open possibility to win and give some weightage for that and 
	//give weightage for each element also.
	public int chanceOfAHorizontalWinContinuous(int rowPosition,int columnPosition)
	{
		int heuristic=0;
		int counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			if(columnPosition+i>=noOfColumns)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition][columnPosition+i]==0)
			{
				counter=0;
			}
			else if(globalBoard[rowPosition][columnPosition+i]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition][columnPosition-i]==0)
			{
				counter=0;
			}
			else if(globalBoard[rowPosition][columnPosition-i]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		return heuristic;
		
	}
	
	public int chanceOfAVerticalWinContinuous(int rowPosition, int columnPosition)
	{
		int heuristic=0;
		int counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			if(rowPosition+i>=noOfRows)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition+i][columnPosition]==0)
			{
				counter=0;
			}
			else if(globalBoard[rowPosition+i][columnPosition]==2)
			{
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first row position we cannot check for below
			if(rowPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition-i][columnPosition]==0)
			{
				counter=0;
			}
			else if(globalBoard[rowPosition-i][columnPosition]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		return heuristic;	
	}
	public int chanceOfADiagonalWinContinuous(int rowPosition, int columnPosition)
	{
		int heuristic=0;
		int counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			if(columnPosition+i>=noOfColumns||rowPosition+i>=noOfRows)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition+i][columnPosition+i]==0)
			{
				counter=0;
				counter=counter*33;
			}
			else if(globalBoard[rowPosition+i][columnPosition+i]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition-i<0||rowPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition-i][columnPosition-i]==0)
			{
				counter=0;
			}
			else if(globalBoard[rowPosition-i][columnPosition-i]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition-i<0||rowPosition+i>=noOfRows)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition+i][columnPosition-i]==0)
			{
				counter=0;
				
			}
			else if(globalBoard[rowPosition+i][columnPosition-i]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition+i>=noOfColumns||rowPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition-i][columnPosition+i]==0)
			{
				counter=0;
			}
			else if(globalBoard[rowPosition-i][columnPosition+i]==2)
			{
				counter++;
				counter=counter*33;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		return heuristic;
	}
	
	//clustering
	public int chanceOfAHorizontalWin(int rowPosition,int columnPosition)
	{
		int heuristic=0;
		int counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			if(columnPosition+i>=noOfColumns)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition][columnPosition+i]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition][columnPosition+i]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition][columnPosition-i]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition][columnPosition-i]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		return heuristic;
		
	}
	
	public int chanceOfAVerticalWin(int rowPosition, int columnPosition)
	{
		int heuristic=0;
		int counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			if(rowPosition+i>=noOfRows)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition+i][columnPosition]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition+i][columnPosition]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first row position we cannot check for below
			if(rowPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition-i][columnPosition]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition-i][columnPosition]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		return heuristic;	
	}
	public int chanceOfADiagonalWin(int rowPosition, int columnPosition)
	{
		int heuristic=0;
		int counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			if(columnPosition+i>=noOfColumns||rowPosition+i>=noOfRows)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition+i][columnPosition+i]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition+i][columnPosition+i]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition-i<0||rowPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition-i][columnPosition-i]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition-i][columnPosition-i]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition-i<0||rowPosition+i>=noOfRows)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition+i][columnPosition-i]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition+i][columnPosition-i]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		counter=0;
		for(int i=1;i<noOfContinuousKeysForWin;i++)
		{
			//it is the first position we cannot check for left
			if(columnPosition+i>=noOfColumns||rowPosition-i<0)
			{
				counter=0;
				break;
			}
			else if(globalBoard[rowPosition-i][columnPosition+i]==0)
			{
				counter++;
			}
			else if(globalBoard[rowPosition-i][columnPosition+i]==2)
			{
				counter++;
			}
			else
			{
				counter=0;
				break;
			}
		}
		heuristic+=counter;
		return heuristic;
	}
	
	
	
	public boolean selectPlayerAlternatively()
	{
		 
		if(playerMove==true)
			playerMove=false;
		else
			playerMove=true;
		return playerMove;
	}
	public boolean isAIturn()
	{
		
		if(playerMove==true)
			return false;
		else
		return true;
	}
	
	
	public  int alphaBetaPruning(BoardModel bState, int depth,int alpha, int beta)
	{
		
			
		//when depth is 0 or when game is over
		if(depth==0||bState.winner()==0)
			return evaluate(bState);
		
		else if(isAIturn())
		{
			BoardModel state=null;
			Point p=new Point();
			for(MyAIMove moveTemp:findAllValidMoves(bState))
			{
			  state=(BoardModel)bState.clone();
			  p.x=moveTemp.row;
			  p.y=moveTemp.column;
			  move.row=p.x;
			  move.column=p.y;
			  state=state.placePiece(p,(byte)2);
			  //state.lastMove=p;
			  for (int i = 0; i < noOfRows; i++) {
				  for(int j=0;j<noOfColumns;j++)
				  {
					  globalBoard[i][j]=state.pieces[i][j];
				  }
			        
			    }
			  selectPlayerAlternatively();
			  System.out.println(depth);
			  alpha=Math.max(alpha, alphaBetaPruning(state, depth-1, alpha, beta));
			//beta cut off
			  if(alpha>=beta)
					break;
			}
			return alpha;
		}
		else
		{
		  Point p=new Point();
		  BoardModel state=null;
		  for(MyAIMove moveTemp:findAllValidMoves(bState))
		  {
			  state=(BoardModel)bState.clone();
			  p.x=moveTemp.row;
			  p.y=moveTemp.column;
			  move.row=p.x;
			  move.column=p.y;
			 state= state.placePiece(p,(byte)1);
			  //state.lastMove=p;
			  for (int i = 0; i < noOfRows; i++) {
				  for(int j=0;j<noOfColumns;j++)
				  {
					  globalBoard[i][j]=state.pieces[i][j];
				  }
			        
			    }
			  selectPlayerAlternatively();
			  System.out.println(depth);
			  beta=Math.min(beta, alphaBetaPruning(state, depth-1, alpha, beta));
			  if(alpha>=beta)
					break;
			  
		  }
		  return beta;
		}
	}
	public int computedistance(BoardModel state,Point p)
	{
		 int i= state.width-p.x;
		 int j=state.height-p.y;
		 return Math.abs(i+j);
	}
	
	//for finding a best move we should first find all the valid moves
	public ArrayList<MyAIMove> findAllValidMoves(BoardModel state)
	{
		ArrayList<MyAIMove> aList=new ArrayList<MyAIMove>();
		HashMap<MyAIMove,Integer> hMap=new HashMap<MyAIMove,Integer>();
		ArrayList<Component> comp=new ArrayList<Component>();
		MyAIMove ai;
		for(int i=0;i<state.width;i++)
		{
			for(int j=0;j<state.height;j++)
			{
				if(state.pieces[i][j]==0)
				{
					if(!state.gravityEnabled())
					{
					 ai=new MyAIMove(i,j);
					 Point p=new Point(i,j);
					 int dist=computedistance(state,p);
					 comp.add(new Component(dist,ai));
					 
					}
					else
					{
						if(j==0||state.getSpace(i,j-1)!=0)
						{
							ai=new MyAIMove(i,j);
							aList.add(ai);
						}
					}
				}
			}
		}
		
		if(!state.gravityEnabled())
		{
	    Collections.sort(comp,new ComparatorForAI());
	    for(int i=0;i<comp.size();i++)
	    {
	    	aList.add(comp.get(i).move);
	    }
		}
		return aList;
	}
	class ComparatorForAI implements Comparator<Component>
	{
		public int compare(Component val1,Component val2)
		{
			if(val1.distance<val2.distance)
				return -1;
			else if(val1.distance>val2.distance)
				return 1;
			else
				return 0;
		}
		
	}
	 class comparatorForAIMove implements Comparator<Entry<MyAIMove,Integer>>
	{
		public int compare(Entry<MyAIMove, Integer> val1,Entry<MyAIMove, Integer> val2)
		{
			if(val1.getValue()<val2.getValue())
				return -1;
			else if(val1.getValue()>val2.getValue())
				return 1;
			else
				return 0;
		}
	}
	
	public boolean isInitialState(BoardModel bState)
	{
		for(int i=0;i<bState.width;i++)
		{
			for(int j=0;j<bState.height;j++)
			{
				if(bState.pieces[i][j]!=0)
				{
					return false;
				}
			}
		}

			return true;
	}
	public byte getOpponent(byte p)
	{
		if(p==1)
			return 2;
		else if(p==2)
			return 1;
		return 0;
	}
	class Component
	{
		int distance;
		MyAIMove move;
		Component(int i,MyAIMove move)
		{
			distance=i;
			this.move=move;
					
		}
		
	}
}
