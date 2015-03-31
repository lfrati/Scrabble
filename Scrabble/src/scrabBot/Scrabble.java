/* Team: Random2
 * Members: Lapo Frati 14202439, Simone Pignotti 14202498, Brennan O'Brien 14209388
 */
package scrabBot;

import java.util.ArrayList;
import java.util.Random;

import action.Action;
import action.Choice;
import action.ExchangeLetters;
import action.PlayWord;

public class Scrabble {
	
	protected Player P1;
	protected Player P2;
	protected Board board;
	protected Pool pool;
	protected Player activePlayer;
	protected int currentPlayerNumber;
	protected UI ui;
	protected boolean keepPlaying, proceed;
	protected Action playerChoice;
	protected Dictionary dict;
	protected Player[] turn;
	private Board stagingBoard;
	private int moveValue;
	private Random rand;
	private String lettersUsed;
	private int challenger;
	
	private int NUM_PLAYERS;
	
	public Scrabble(){
		pool = new Pool();
		board = new Board();
		ui = new UI(System.in);
		keepPlaying = true;
		dict = new Dictionary();
	}
	

	public void startGame(){
		//TODO: avoid challenge as first command
		if (!dict.loadDictionary()) {
			ui.printMessage("Fatal error: cannot load dictionary", true);
			return;
		}
		NUM_PLAYERS = ui.getNumberOfPlayers();
		
		turn = new Player[NUM_PLAYERS];
		for(int i = 0; i<NUM_PLAYERS; i++){
			turn[i] = new Player();
			turn[i].setPlayerName(ui.getPlayerName(i));
			turn[i].getPlayerFrame().refillFrame(pool);
		}
		
		currentPlayerNumber = rand.nextInt(NUM_PLAYERS+1);
		activePlayer = turn[currentPlayerNumber];
		
		while(keepPlaying){
			ui.gameInfo(this);
			ui.promptActivePlayer(activePlayer,board);
			proceed = false;
			while(!proceed){
				playerChoice = ui.getUserInput();
				if(activePlayer.checkIfLostChallenge()){
					activePlayer.resetLostChallenge();
					proceed = true;
				}
				else
					switch(playerChoice.getChoice()){
						case PLAYWORD:	PlayWord wordToPlace = (PlayWord) playerChoice;
										CheckResult result = board.checkPlacement(wordToPlace.getWord(), 
												wordToPlace.getRow(), 
												wordToPlace.getColumn(), 
												wordToPlace.getDirection(),
												activePlayer);
										if((result == CheckResult.OK) && dict.dictionaryCheck(wordToPlace.getWord())){
											stagingBoard = board;
											
											moveValue = calculatePlacementPoints(	wordToPlace.getWord(), 
																					wordToPlace.getRow(),
																					wordToPlace.getColumn(), 
																					wordToPlace.getDirection());
											
											lettersUsed = stagingBoard.placeWord(wordToPlace.getWord(), 
																				wordToPlace.getRow(), 
																				wordToPlace.getColumn(), 
																				wordToPlace.getDirection());
														
											activePlayer.increasePlayerScoreBy(moveValue);
											

											if((challenger = ui.checkChallenge(turn, currentPlayerNumber)) != -1){
												if (dict.dictionaryCheck(wordToPlace.getWord()))
													turn[challenger].setLostChallenge();
												else {
													activePlayer.increasePlayerScoreBy(-moveValue);
													activePlayer.getPlayerFrame().refundLetters(lettersUsed);
													stagingBoard = board;
												}
											}
											else
												if(pool.getPoolSize() != 0){
														try{
															activePlayer.getPlayerFrame().refillFrame(pool);
														} catch (EmptyPoolException e){
															ui.printMessage("Letters in the pool finished.", true);
														}
												} else {
													if(activePlayer.getPlayerFrame().getFrameSize() == 0){
														keepPlaying = false;
														endGame();
													}
												}
											
											board = stagingBoard;
											proceed = true;
										}
										else
											ui.printMessage("Invalid Placement. Error: "+result.name(), true);
										break;
						case PASSTURN: 	proceed = true;
										break;
						case GETHELP:	displayHelp();
										break;
						case EXCHANGELETTERS:	if(pool.getPoolSize() >= 7)
													proceed = exchangeLetters(((ExchangeLetters)playerChoice).getLettersToChange());
												else
													ui.printMessage("Not enough letters remaining", true);			
												break;
						case QUIT: 		quitGame();;
										proceed = true;
										ui.printMessage(activePlayer.getPlayerName() + " surrendered.", true);
										break;
						default:	break;
					}
			}
			passTurn();	
	}
}
	
	private void endGame(){
		ArrayList<Character> unusedLetters;
		Player otherPlayer;
		if(activePlayer.equals(P1))
			otherPlayer = P2;
		else
			otherPlayer = P1;
		
		unusedLetters = otherPlayer.getPlayerFrame().getLetters();
		
		for(Character letter : unusedLetters){
			activePlayer.increasePlayerScoreBy(pool.checkValue(letter));
			otherPlayer.increasePlayerScoreBy(-pool.checkValue(letter));
		}
		
		ui.gameInfo(this);
		
		if(activePlayer.getPlayerScore() > otherPlayer.getPlayerScore())
			ui.printMessage("The winner is "+activePlayer.getPlayerName(),true);
		else
			if(activePlayer.getPlayerScore() < otherPlayer.getPlayerScore())
				ui.printMessage("The winner is "+otherPlayer.getPlayerName(),true);
			else
				ui.printMessage("TIE", true);
		
	}
	
	private void passTurn(){
		currentPlayerNumber = (currentPlayerNumber + 1)%NUM_PLAYERS;
		activePlayer = turn[currentPlayerNumber];
	}
	
	private Player previousPlayer(){
		return turn[(currentPlayerNumber-1)%NUM_PLAYERS];
	}
	
	public int calculatePlacementPoints(String wordPlayed, int row, int column, Direction dir){
		int total = 0, otherWordsTotal = 0, wordMult = 1, lettersUsed = 0;
		
		for(int i = 0; i < wordPlayed.length(); i++) {
			if (board.getLetterAt(row, column) == Board.FREE_LOCATION) {
				lettersUsed++;
				wordMult *= Board.wordMultiplier[row][column];
				total += Board.letterMultiplier[row][column] * pool.checkValue(wordPlayed.charAt(i));
				if (dir == Direction.VERTICAL && (board.getLetterAt(row, column-1) != Board.FREE_LOCATION || board.getLetterAt(row, column+1) != Board.FREE_LOCATION)) {
					otherWordsTotal += calculateOtherWordsPoints(wordPlayed.charAt(i), row, column, Direction.HORIZONTAL);
				}
				else if (dir == Direction.HORIZONTAL && (board.getLetterAt(row-1, column) != Board.FREE_LOCATION || board.getLetterAt(row+1, column) != Board.FREE_LOCATION)) {
					otherWordsTotal += calculateOtherWordsPoints(wordPlayed.charAt(i), row, column, Direction.VERTICAL);
				}
			}
			else {
				total += pool.checkValue(wordPlayed.charAt(i));
			}
			if(dir == Direction.VERTICAL)
				row++;
			else
				column++;
		}
		
		total *= wordMult;
		
		total += otherWordsTotal;
		
		if (lettersUsed == 7) {
			total += 50;
		}

		return total;
	}
	
	private void quitGame(){
		keepPlaying = false;
	}
	
	private void displayHelp(){
		System.out.println("Legal options:\nQUIT\nHELP\nPASS\nEXCHANGE <letters>\n<grid ref> <across/down> <word>  e.g. A1 A HELLO");
	}
	
	
	//use the frame of the active player
	private boolean exchangeLetters( String lettersToExchange ){
		Frame frame = activePlayer.getPlayerFrame();
		boolean result;
		result = frame.containsLetters(lettersToExchange);
		if(result){
			frame.removeLetters(lettersToExchange);
			frame.refillFrame(pool);
		}
		return result;
	}
	
	private int calculateOtherWordsPoints(char startingLetter, int row, int column, Direction dir){
		int total = 0, newRow = row, newColumn = column, diff = 0;
		char newChar;
		String newWord = "";
		boolean finished = false, startingLetterUsed = false;
		
		if (dir == Direction.HORIZONTAL) {
			int tempColumn;
			while (newColumn > 0 && board.getLetterAt(row, newColumn-1) != Board.FREE_LOCATION)
				newColumn--;
			diff = column - newColumn;
			tempColumn = newColumn;
			while (tempColumn < Board.MAX_COLUMN && !finished) {
				if ((newChar = board.getLetterAt(row, tempColumn)) == Board.FREE_LOCATION) {
					if (startingLetterUsed)
						finished = true;
					else {
						startingLetterUsed = true;
						newWord += startingLetter;
					}
				}
				else
					newWord += newChar;
				tempColumn++;
			}
		}
		else {
			int tempRow;
			while (newRow > 0 && board.getLetterAt(newRow-1, column) != Board.FREE_LOCATION)
				newRow--;
			diff = row - newRow;
			tempRow = newRow;
			while (tempRow < Board.MAX_ROW && !finished) {
				if ((newChar = board.getLetterAt(tempRow, column)) == Board.FREE_LOCATION) {
					if (startingLetterUsed)
						finished = true;
					else {
						startingLetterUsed = true;
						newWord += startingLetter;
					}
				}
				else
					newWord += newChar;
				tempRow++;
			}
		}
		
		for(int i = 0; i < newWord.length(); i++) {
			if (i==diff)
				total += pool.checkValue(newWord.charAt(i)) * Board.letterMultiplier[row][column];
			else
				total += pool.checkValue(newWord.charAt(i));
			if(dir == Direction.VERTICAL)
				newRow++;
			else
				newColumn++;
		}
		
		total *= Board.wordMultiplier[row][column];

		return total;
	}
}
