package com.example.stepappv4.ui.Game;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.stepappv4.ui.Home.HomeViewModel;
import com.example.stepappv4.R;
import com.example.stepappv4.ui.Security.LoginActivity;

public class GameFragment extends Fragment implements View.OnClickListener{

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.activity_00_game, container, false);

        Button btn = (Button) root.findViewById(R.id.game_init);
        btn.setOnClickListener(this);

        Button historyBtn = (Button) root.findViewById(R.id.history_button);
        historyBtn.setOnClickListener(this);

        Button rematchBtn = (Button) root.findViewById(R.id.rematch_init);
        rematchBtn.setOnClickListener(this);

        Button loginBtn = (Button) root.findViewById(R.id.login_button);
        loginBtn.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.game_init) {
            Intent intent = new Intent(requireActivity(), Game_01_Matching.class);
            startActivity(intent);
            //Navigation.findNavController(view).navigate(R.id.action_gameFragment_to_matching);
        } else if (view.getId() == R.id.history_button) {
            Navigation.findNavController(view).navigate(R.id.action_gameFragment_to_history);
        } else if (view.getId() == R.id.rematch_init) {
            Navigation.findNavController(view).navigate(R.id.action_gameFragment_to_rematching);
        } else if (view.getId() == R.id.login_button) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            //startActivity(new Intent(getActivity(), LoginActivity.class));
            //((Activity) getActivity()).overridePendingTransition(0, 0);
            //Navigation.findNavController(view).navigate(R.id.action_gameFragment_to_login);
        }

    }

}
