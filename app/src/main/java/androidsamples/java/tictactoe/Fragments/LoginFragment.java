package androidsamples.java.tictactoe.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidsamples.java.tictactoe.GameComponents.GamesHistory;
import androidsamples.java.tictactoe.GameComponents.Player;
import androidsamples.java.tictactoe.MainActivity;
import androidsamples.java.tictactoe.R;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private EditText mEmail, mPassword;
    private String email, password;
    private FirebaseAuth mAuth;

    private List<String> allPlayerDetails;
    private Button loginBtn;

    private DatabaseReference playerDetailsRef;
    private ProgressDialog progressDialog;

    NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        MainActivity.currNavController = navController;

        mEmail = view.findViewById(R.id.edit_email);
        mPassword = view.findViewById(R.id.edit_password);
        loginBtn = view.findViewById(R.id.btn_log_in);

        allPlayerDetails = new ArrayList<>();
        playerDetailsRef = FirebaseDatabase.getInstance().getReference("Player Details");

        progressDialog = new ProgressDialog(getContext());
        mAuth = FirebaseAuth.getInstance();

        //If User is already Logged in then navigate to Dashboard
        if(mAuth.getCurrentUser()!=null){
            NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
            navController.navigate(action);
            Toast.makeText(getContext(), "User Authenticated", Toast.LENGTH_SHORT).show();
        }

        playerDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Storing all the players currently registered in the Database
                allPlayerDetails.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    allPlayerDetails.add(s.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loginBtn.setOnClickListener(v -> {

             email = mEmail.getText().toString();
             password = mPassword.getText().toString();

            //Removing all the spaces
            email = email.replaceAll(" ", "");
            password = password.replaceAll(" ", "");

            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter Email", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter Password", Toast.LENGTH_SHORT).show();
            } else {
                Player curr = new Player(email);
                // check if email already exists in the database
                mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            // Signup
                            if (isNewUser) {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Sign in success, redirect user to dashboard
                                            Log.d(TAG, "createUserWithEmail:success");

                                            //Making a new Entry in the list of all Players registered
                                            playerDetailsRef.push().setValue(curr.getPlayerId());

                                            //Making a new Games History Entry that contains the Win/Loss/Draw count of that user
                                            FirebaseDatabase.getInstance().getReference("Games History").child(curr.getPlayerId().split("@")[0]).setValue(new GamesHistory(curr.getPlayerId(), 0, 0, 0));

                                            NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                            navController.navigate(action);
                                        } else {
                                            // If sign up fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task1.getException());
                                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                            // Signin
                            else {
                                mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Log.d(TAG, "signInWithEmailAndPassword:success");
                                            NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                            navController.navigate(action);
                                        } else {
                                            // If sign up fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmailAndPassword:failure", task2.getException());
                                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                        }
                    });
            }
        });
    }
}