package com.example.stepappv4.ui.Game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.stepappv4.R;

public class Game_08_History extends Fragment implements View.OnClickListener{

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_08_history, container, false);

        Button btn = (Button) root.findViewById(R.id.history_home_button);
        btn.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.history_home_button) {
            Navigation.findNavController(view).navigate(R.id.action_historyFragment_to_home);
        }

    }
}
