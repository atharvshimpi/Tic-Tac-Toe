package androidsamples.java.tictactoe.GameComponents;

public class GamesHistory {

    private String playerId;
    private int winCount, lossCount, drawCount;

    public GamesHistory() {

    }

    public GamesHistory(String playerId, int winCount, int lossCount, int drawCount){
        setPlayerId(playerId);
        setWinCount(winCount);
        setLossCount(lossCount);
        setDrawCount(drawCount);
    }

    public int getWinCount() { return winCount; }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public void setLossCount(int lossCount) {
        this.lossCount = lossCount;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
