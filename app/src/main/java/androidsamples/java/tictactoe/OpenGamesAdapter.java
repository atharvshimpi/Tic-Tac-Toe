package androidsamples.java.tictactoe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidsamples.java.tictactoe.Fragments.DashboardFragmentDirections;
import androidsamples.java.tictactoe.GameComponents.AvailableGames;
import androidsamples.java.tictactoe.GameComponents.RunningGames;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {

  private List<AvailableGames> mGameslist;
  private String currPlayerId;

  private Context context;
  private NavController mNavController;

  private final DatabaseReference runningGamesRef = FirebaseDatabase.getInstance().getReference("Running Games");
  private final DatabaseReference availGamesRef = FirebaseDatabase.getInstance().getReference("Available Games");

  public OpenGamesAdapter(Context context) {
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    if(mGameslist!=null){
      //Assigning values to the TextViews in the RV using the data in the Database(Available Games)
      AvailableGames current =mGameslist.get(position);

      holder.mIdView.setText(current.getPlayerOne());
      holder.mContentView.setText("Waiting...");

      //Logic for when a RV element it Clicked
      holder.itemView.setOnClickListener(view -> {

        //Making a new Running Game in the Firebase with the same GameId as that of the one clicked (RV Item)
        List<String> players = new ArrayList<>(Arrays.asList(current.getPlayerOne(),currPlayerId));
        List<String> grid = new ArrayList<>(Arrays.asList("","","","","","","","",""));
        RunningGames obj = new RunningGames(2,current.getGameId(),players,0,grid);
        runningGamesRef.child(current.getGameId()).setValue(obj);

        //Setting P2Found = true in the database : would inform the player1 that p2 is found;
        current.setPlayerTwoFound(true);
        availGamesRef.child(current.getGameId()).setValue(current);

        String gameType = context.getString(R.string.two_player);
        navigateToGame(obj.getGameId(), gameType,true);
      });
    }
  }

  private void navigateToGame(String gameId, String gameType, boolean isP2) {
    NavDirections action = DashboardFragmentDirections.actionGame().setGameId(gameId).setGameType(gameType).setIsP2(isP2);
    mNavController.navigate(action);
  }

  public void setGameslist(List<AvailableGames> mGameslist) {
    this.mGameslist = mGameslist;
    notifyDataSetChanged();
  }


  @Override
  public int getItemCount() {
    return (mGameslist == null)? 0:mGameslist.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }

  public void setCurrPlayerId(String currPlayerId) {
    this.currPlayerId = currPlayerId;
  }

  public void setNavController(NavController navController){
    this.mNavController = navController;
  }

}