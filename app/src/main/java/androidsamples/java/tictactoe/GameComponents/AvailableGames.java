package androidsamples.java.tictactoe.GameComponents;

import java.util.UUID;

public class AvailableGames {
    private String gameId;
    private String playerOne;
    private boolean playerTwoFound;

    public AvailableGames() {

    }

    public AvailableGames(String playerOne){
        setGameId(UUID.randomUUID().toString());
        setPlayerOne(playerOne);
        setPlayerTwoFound(false);
    }

    public String getGameId(){
        return gameId;
    }

    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getPlayerOne() {
        return playerOne;
    }

    public void setPlayerOne(String playerOne) {
        this.playerOne = playerOne;
    }

    public boolean isPlayerTwoFound() {
        return playerTwoFound;
    }

    public void setPlayerTwoFound(boolean playerTwoFound) {
        this.playerTwoFound = playerTwoFound;
    }
}