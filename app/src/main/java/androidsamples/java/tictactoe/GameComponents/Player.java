package androidsamples.java.tictactoe.GameComponents;

public class Player {
    private String playerId;

    public Player(){

    }

    public Player(String playerId){
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String ID) {
        this.playerId = ID;
    }

    public boolean equals(Player player){
        return this.playerId.equals(player.getPlayerId());
    }
}
