/* Team: Random2
 * Members: Lapo Frati 14202439, Simone Pignotti 14202498, Brennan O'Brien ?
*/
package scrabBot;

public class Player {
	private int playerScore;
	private String playerName;
	private Frame frame;
	private static int numberOfPlayers = 0;
	
	public Player()
	{
		this.playerScore = 0;
		Player.numberOfPlayers = numberOfPlayers + 1; // Default player name will be PlayerN where N is the number of player created so far
		this.playerName = "Player".concat(Integer.toString(numberOfPlayers));
		this.frame = new Frame();
	}
	
	public Player(Character[] letters)
	{
		this.playerScore = 0;
		Player.numberOfPlayers = numberOfPlayers + 1; // Default player name will be PlayerN where N is the number of player created so far
		this.playerName = "Player".concat(Integer.toString(numberOfPlayers));
		this.frame = new Frame(letters);
	}
	
	public Player(String name){
		this.playerScore = 0;
		Player.numberOfPlayers = numberOfPlayers + 1; // Default player name will be PlayerN where N is the number of player created so far
		this.playerName = name;
		this.frame = new Frame();
	}
	
	public void resetPlayerData() {
		this.playerScore = 0;
		this.frame = new Frame();
	}
	
	public void setPlayerName(String playerName) throws NullNameException {
		if(playerName != null)
			this.playerName = playerName;
		else
			throw new NullNameException("Name can't be null.");
	}
	
	public String getPlayerName() {
			return this.playerName;
	}
	
	/* If the value is positive it increments the score by that amount, otherwise it throws an exception */
	public void increasePlayerScoreBy(int value) throws NegativeScoreException
	{
		if(value >= 0)
			this.playerScore = playerScore + value;
		else
			throw new NegativeScoreException("Score can't be negative.");
	}
	
	public int getPlayerScore() {
			return this.playerScore;
	}
	
	public Frame getPlayerFrame(){
		return frame;
	}
}
