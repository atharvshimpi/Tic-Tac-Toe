package androidsamples.java.tictactoe.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidsamples.java.tictactoe.ForfeitDialogFragment;
import androidsamples.java.tictactoe.GameComponents.AvailableGames;
import androidsamples.java.tictactoe.GameComponents.GamesHistory;
import androidsamples.java.tictactoe.GameComponents.RunningGames;
import androidsamples.java.tictactoe.MainActivity;
import androidsamples.java.tictactoe.R;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  private String gameType;
  private int gameTypeInt;
  private String gameId;
  private String playerInThisDevice;

  private boolean isPlayer2 = false;
  private boolean isGameConcluded = false;
  private boolean cancelWaiting = true;

  private final Button[] mButtons = new Button[GRID_SIZE];

  private TextView playerIdTxt;
  private TextView gameTypeTxt;

  private DatabaseReference runningGamesRef;
  private DatabaseReference availGamesRef;
  private DatabaseReference gameHistoryRef;

  private ValueEventListener listener;
  private ChildEventListener deleteListener;

  private RunningGames currRunGame;
  private List<List<Integer>> winCombinations;

  private ProgressDialog progressDialog;
  private NavController mNavController;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());

    gameType = args.getGameType();
    gameId = args.getGameId();
    isPlayer2 = args.getIsP2();

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Leaving would result in a Loss ?");
        builder.setTitle("Are you sure ?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
          runningGamesRef.child(gameId).removeEventListener(deleteListener);
          runningGamesRef.child(gameId).removeValue();
          String opponent = currRunGame.getPlayers().get(0).equals(playerInThisDevice) ? currRunGame.getPlayers().get(1) : currRunGame.getPlayers().get(0);
          changeWinLossCount(opponent, playerInThisDevice, false, true);
          mNavController.popBackStack();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
          dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);
    MainActivity.currNavController = mNavController;

    playerIdTxt = view.findViewById(R.id.player_id_game_text);
    gameTypeTxt = view.findViewById(R.id.gameTypeText_game);

    playerInThisDevice = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];

    runningGamesRef = FirebaseDatabase.getInstance().getReference("Running Games");
    availGamesRef = FirebaseDatabase.getInstance().getReference("Available Games");
    gameHistoryRef = FirebaseDatabase.getInstance().getReference("Games History");

    // Initialize the grid buttons & their click listeners
    initBtns(view);
    initBtnsClickListener();
    initWinCombinations();

    // Error Checks
    if (!gameId.equals("NULL") && gameTypeInt != -1) {
      gameTypeTxt.setText(gameType);
      gameTypeInt = (gameType.equals(getString(R.string.one_player)) ? 1 : 2);

      // If one player game, start it without any delay
      if (gameTypeInt == 1) {
        loadRunningGameData();

        // If two player game, then wait till someone joins it
      } else if (gameTypeInt == 2) {
        if (!isPlayer2) {
          waitDialog();
          listenForPlayerFound();

          // If someone joins it, then start the game
        } else {
          loadRunningGameData();
        }
      }

      listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          RunningGames g = snapshot.getValue(RunningGames.class);
          if (g != null) {
            currRunGame = g;
            playerIdTxt.setText(currRunGame.getPlayers().get(currRunGame.getCurrPlayer()) + "'s Turn");
            updateUI();
            checkGameStatus();
          }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
          Log.d(TAG, "Database Error!");
        }
      };

      deleteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
          if (!isGameConcluded && gameTypeInt == 2)
            new ForfeitDialogFragment(mNavController).show(getChildFragmentManager(),"Forfeit Dialog");
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      };

    }
  }

  private void initBtns(View view) {
    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);
  }

  private void initBtnsClickListener() {
    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        onClickGridElement(finalI);
      });
    }
  }

  public void onClickGridElement(int index) {
    List<String> currGrid = currRunGame.getCurrGrid();

    // Click only if index is valid and the respective grid is empty
    if (index != -1 && !currGrid.get(index).equals("X") && !currGrid.get(index).equals("O")) {
      if (currRunGame.getPlayers().get(currRunGame.getCurrPlayer()).equals(playerInThisDevice)) {
        int currPlayer = currRunGame.getCurrPlayer();
        String input = (currPlayer == 0) ? "X" : "O";
        currRunGame.getCurrGrid().set(index, input);
        currRunGame.setCurrPlayer((currPlayer == 0) ? 1 : 0);
        runningGamesRef.child(currRunGame.getGameId()).setValue(currRunGame);
      }
    }
  }

  private void initWinCombinations() {
    winCombinations = new ArrayList<>();
    winCombinations.clear();
    winCombinations.add(Arrays.asList(0, 1, 2));
    winCombinations.add(Arrays.asList(3, 4, 5));
    winCombinations.add(Arrays.asList(6, 7, 8));
    winCombinations.add(Arrays.asList(0, 3, 6));
    winCombinations.add(Arrays.asList(1, 4, 7));
    winCombinations.add(Arrays.asList(2, 5, 8));
    winCombinations.add(Arrays.asList(0, 4, 8));
    winCombinations.add(Arrays.asList(2, 4, 6));

  }

  private void waitDialog() {
    progressDialog = new ProgressDialog(getContext());
    progressDialog.setTitle("");
    progressDialog.setMessage("Waiting For Player 2");
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.show();
    progressDialog.setOnCancelListener(dialogInterface -> {
      if(cancelWaiting) mNavController.popBackStack();
    });
  }

  private void listenForPlayerFound() {
    availGamesRef.child(gameId).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        AvailableGames g = snapshot.getValue(AvailableGames.class);
        if (g != null) {
          if (g.isPlayerTwoFound()) {
            progressDialog.dismiss();
            loadRunningGameData();
            availGamesRef.child(gameId).removeValue();
            cancelWaiting = false;
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }

    });
  }

  private void loadRunningGameData() {
    runningGamesRef.child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        RunningGames g = snapshot.getValue(RunningGames.class);
        if (g != null) {
          currRunGame = g;
          playerIdTxt.setText(currRunGame.getPlayers().get(currRunGame.getCurrPlayer()) + "'s Turn");
          runningGamesRef.child(gameId).addValueEventListener(listener);
          runningGamesRef.child(gameId).addChildEventListener(deleteListener);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
  }

  private void updateUI() {
    String playerTurnTxt = (currRunGame.getPlayers().get(currRunGame.getCurrPlayer()).equals(playerInThisDevice)) ? "Your Turn" : "Opponent's Turn";
    playerIdTxt.setText(playerTurnTxt);
    for (int i = 0; i < mButtons.length; i++)
      mButtons[i].setText(currRunGame.getCurrGrid().get(i));
  }

  private void checkGameStatus() {
    List<String> currGrid = currRunGame.getCurrGrid();

    List<Integer> pos_X = new ArrayList<>();
    List<Integer> pos_O = new ArrayList<>();
    int emptySpots = 0;

    for (int i = 0; i < 9; i++) {
      if (currGrid.get(i).equals("X")) pos_X.add(i);
      else if (currGrid.get(i).equals("O")) pos_O.add(i);
      else emptySpots++;
    }

    if (isWinningCombination(pos_X)) {
      //Player 1 Wins
      gameConcluded(false, 0, 1);
    } else if (isWinningCombination(pos_O)) {
      //Player 2 Wins
      gameConcluded(false, 1, 0);
    } else if (emptySpots == 0) {
      //Draw
      gameConcluded(true, 0, 1);
    } else {
      if (currRunGame.getCurrPlayer() == 1 && currRunGame.getGameType() == 1) {
        waitForCpu();
      }
    }
  }

  private boolean isWinningCombination(List<Integer> list) {
    for (int i = 0; i < 8; i++) {
      List<Integer> temp = winCombinations.get(i);
      int count = 0;
      for (int j = 0; j < 3; j++) {
        if (list.contains(temp.get(j)))
          count++;
      }

      if (count == 3)
        return true;
    }
    return false;
  }

  private void gameConcluded(boolean isDraw, int win, int loss) {
    isGameConcluded = true;
    String title = "";
    String message = "";

    String winPlayer = currRunGame.getPlayers().get(win);
    String lossPlayer = currRunGame.getPlayers().get(loss);

    if (isDraw) {
      title = getString(R.string.draw_title);
      message = getString(R.string.draw_message);
      changeWinLossCount(winPlayer, lossPlayer, true, false);
    } else {
      if (winPlayer.equals(playerInThisDevice)) {
        title = getString(R.string.win_title);
        message = getString(R.string.win_message);
      } else if (lossPlayer.equals(playerInThisDevice)) {
        title = getString(R.string.loss_title);
        message = getString(R.string.loss_message);
      }
      changeWinLossCount(winPlayer, lossPlayer, false, false);
    }

    androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).setMessage(message).setTitle(title);
    alertDialog.setCancelable(false);
    alertDialog.setPositiveButton(getString(R.string.back_to_dashboard_message), (dialog, which) -> {
      runningGamesRef.child(gameId).removeValue();
      mNavController.popBackStack();
    });
    alertDialog.show();
  }

  private void changeWinLossCount(String winPlayer, String lossPlayer, boolean isDraw, boolean changeBoth) {
    if (playerInThisDevice.equals(winPlayer) || changeBoth) {
      gameHistoryRef.child(winPlayer).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          GamesHistory g = snapshot.getValue(GamesHistory.class);
          if (g != null) {
            if (isDraw) g.setDrawCount(g.getDrawCount() + 1);
            else g.setWinCount(g.getWinCount() + 1);
            gameHistoryRef.child(winPlayer).setValue(g);
          }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });
    }

    if (playerInThisDevice.equals(lossPlayer) || changeBoth) {
      gameHistoryRef.child(lossPlayer).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          GamesHistory g = snapshot.getValue(GamesHistory.class);
          if (g != null) {
            if (isDraw) g.setDrawCount(g.getDrawCount() + 1);
            else g.setLossCount(g.getLossCount() + 1);
            gameHistoryRef.child(lossPlayer).setValue(g);
          }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });
    }
  }

  private void waitForCpu() {
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        List <Integer> availableTiles = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
          // Check for not of ('X' or 'O') in tile --> tile not available
          if (!(currRunGame.getCurrGrid().get(i).equals("X") || currRunGame.getCurrGrid().get(i).equals("O")))
            availableTiles.add(i);
        }

        // Shuffling the list, and choosing the 1st index of this new list
        Collections.shuffle(availableTiles);

        currRunGame.getCurrGrid().set(availableTiles.get(0), "O");
        // Set the current player as the opponent
        currRunGame.setCurrPlayer((currRunGame.getCurrPlayer() == 0) ? 1 : 0);
        // Save the new data in database
        runningGamesRef.child(currRunGame.getGameId()).setValue(currRunGame);
      }
    }, 500);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy");
    if(!isGameConcluded) {
      availGamesRef.child(gameId).removeValue();
      runningGamesRef.child(gameId).removeValue();
    }
  }
}