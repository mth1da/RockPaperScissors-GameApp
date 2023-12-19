package com.example.stepappv4.ui.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.stepappv4.MainActivity;
import com.example.stepappv4.R;
import com.example.stepappv4.databinding.FragmentSlideshowBinding;
import com.example.stepappv4.ui.Game.Game_01_Matching;

public class ProfileFragment extends Fragment {
    private Button home;

    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        home = root.findViewById(R.id.profile_home_button);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Too lazy to define its own navigation controller...
                Navigation.findNavController(view).navigate(R.id.action_historyFragment_to_home);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}