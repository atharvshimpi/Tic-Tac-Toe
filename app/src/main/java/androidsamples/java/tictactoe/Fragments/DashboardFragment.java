package androidsamples.java.tictactoe.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidsamples.java.tictactoe.GameComponents.AvailableGames;
import androidsamples.java.tictactoe.GameComponents.GamesHistory;
import androidsamples.java.tictactoe.GameComponents.RunningGames;
import androidsamples.java.tictactoe.MainActivity;
import androidsamples.java.tictactoe.OpenGamesAdapter;
import androidsamples.java.tictactoe.R;

public class DashboardFragment extends Fragment {

  private String currPlayerId;
  private GamesHistory currPlayerHistory;
  private OpenGamesAdapter adapter;
  private List<AvailableGames> availableGamesList;

  private TextView playerIdTxt;
  private TextView txtWin, txtLoss, txtDraw;

  private DatabaseReference gameHistoryRef;
  private DatabaseReference availGamesRef;
  private DatabaseReference runningGamesRef;

  private FirebaseUser mUser;
  private FirebaseAuth mAuth;

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */

  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);
    MainActivity.currNavController = mNavController;


    playerIdTxt = view.findViewById(R.id.txt_player_dash_name);
    txtWin = view.findViewById(R.id.txt_win);
    txtLoss = view.findViewById(R.id.txt_loss);
    txtDraw = view.findViewById(R.id.txt_draw);

    gameHistoryRef = FirebaseDatabase.getInstance().getReference("Games History");
    availGamesRef = FirebaseDatabase.getInstance().getReference("Available Games");
    runningGamesRef = FirebaseDatabase.getInstance().getReference("Running Games");

    mAuth = FirebaseAuth.getInstance();
    mUser = mAuth.getCurrentUser();

    if(mUser == null) {
      mNavController.navigate(DashboardFragmentDirections.actionNeedAuth());
    } else {
      currPlayerId = mUser.getEmail().split("@")[0];
      initRecyclerView(view);

      if (currPlayerId != null) {
        playerIdTxt.setText(String.format(Locale.ENGLISH, "%s's Dashboard", currPlayerId));

        //Loading the win_loss_draw count from the database
        loadGameHistory();

        //Setting the current player for the Recycler view for Database object creation when RV_Item is clicked
        adapter.setCurrPlayerId(currPlayerId);
      }

      // Show a dialog when the user clicks the "new game" button
      view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {
        createGameDialog();
      });

      // Adding a value listener to get the most Updated list of Available games form the firebase
      availGamesRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          availableGamesList.clear();
          for(DataSnapshot s:snapshot.getChildren()){
            AvailableGames obj = s.getValue(AvailableGames.class);
            availableGamesList.add(obj);
          }
          adapter.setGameslist(availableGamesList);
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });
    }
  }

  private void createGameDialog() {
    // A listener for the positive and negative buttons of the dialog
    DialogInterface.OnClickListener listener = (dialog, which) -> {

      String gameType = getString(R.string.no_game_type);
      if (which == DialogInterface.BUTTON_POSITIVE) {
        gameType = getString(R.string.two_player);
        AvailableGames obj = new AvailableGames(currPlayerId);
        availGamesRef.child(obj.getGameId()).setValue(obj);
        Toast.makeText(getContext(), getString(R.string.two_player_game_creation), Toast.LENGTH_SHORT).show();
        NavDirections action = DashboardFragmentDirections.actionGame().setGameId(obj.getGameId()).setGameType(gameType).setIsP2(false);
        mNavController.navigate(action);
      } else if (which == DialogInterface.BUTTON_NEGATIVE) {
        gameType = getString(R.string.one_player);
        int gameTypeInt = 1;
        List<String> players = new ArrayList<>(Arrays.asList(currPlayerId, "OS"));
        List<String> grid = new ArrayList<>(Arrays.asList("","","","","","","","",""));
        RunningGames obj = new RunningGames(gameTypeInt, UUID.randomUUID().toString(), players, 0, grid);
        runningGamesRef.child(obj.getGameId()).setValue(obj);
        Toast.makeText(getContext(), getString(R.string.one_player_game_creation), Toast.LENGTH_SHORT).show();
        NavDirections action = DashboardFragmentDirections.actionGame().setGameId(obj.getGameId()).setGameType(gameType).setIsP2(false);
        mNavController.navigate(action);
      }
    };

    // create the dialog
    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setTitle(R.string.new_game)
            .setMessage(R.string.new_game_dialog_message)
            .setPositiveButton(R.string.two_player, listener)
            .setNegativeButton(R.string.one_player, listener)
            .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
            .create();
    dialog.show();
  }

  private void initRecyclerView(View view) {
    availableGamesList = new ArrayList<>();
    RecyclerView recyclerView = view.findViewById(R.id.list);
    adapter = new OpenGamesAdapter(getContext());
    recyclerView.setAdapter(adapter);
    adapter.setNavController(mNavController);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
  }

  private void loadGameHistory() {
    gameHistoryRef.child(currPlayerId).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        GamesHistory g = snapshot.getValue(GamesHistory.class);
        if(g != null){
          currPlayerHistory = g;
          updateWinLossDrawCount();
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
  }

  private void updateWinLossDrawCount() {
    String wins = Integer.toString(currPlayerHistory.getWinCount());
    String loss = Integer.toString(currPlayerHistory.getLossCount());
    String draw = Integer.toString(currPlayerHistory.getDrawCount());
    txtWin.setText(wins);
    txtLoss.setText(loss);
    txtDraw.setText(draw);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

}