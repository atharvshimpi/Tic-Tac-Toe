package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;

public class ForfeitDialogFragment extends DialogFragment {
    private NavController navController;
    public ForfeitDialogFragment(){

    }

    public ForfeitDialogFragment(NavController navController){
        this.navController = navController;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("OOPS!!")
                .setMessage("It Looks Like The Opponent Left")
                .setPositiveButton("Go to Dashboard", (dialog, which) -> {
                    navController.popBackStack();
                })
                .create();
    }
}
