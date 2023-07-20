package androidsamples.java.tictactoe.GameComponents;

import java.util.List;

public class RunningGames {

    private String gameId;
    private int gameType;
    private List <String> players;
    private int currPlayer;
    private List <String> currGrid;

    public RunningGames(){

    }

    public RunningGames(int gameType, String gameId, List<String> players, int currPlayer, List<String> currGrid){
        setPlayers(players);
        setCurrPlayer(currPlayer);
        setCurrGrid(currGrid);
        setGameId(gameId);
        setGameType(gameType);
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List <String> players) { this.players = players; }

    public List<String> getCurrGrid() {
        return currGrid;
    }

    public void setCurrGrid(List <String> currGrid) {
        this.currGrid = currGrid;
    }

    public int getCurrPlayer() { return currPlayer; }

    public void setCurrPlayer(int currPlayer) {
        this.currPlayer = currPlayer;
    }
}
