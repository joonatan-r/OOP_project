package com.example.oop_project.ui.user_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.oop_project.InfoViewModel;
import com.example.oop_project.R;
import com.example.oop_project.User;

public class UserInfoFragment extends Fragment {
    /*
    Gets the user from InfoViewModel, and sets text views to display its info. InfoViewModel's user
    has to be set before navigating here, or this immediately navigates back.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_info, container, false);
        TextView usernameInfo = root.findViewById(R.id.usernameInfo);
        TextView emailInfo = root.findViewById(R.id.emailInfo);
        TextView phoneInfo = root.findViewById(R.id.phoneInfo);
        TextView infoInfo = root.findViewById(R.id.infoInfo);
        User user;

        if (model.getUser() != null && (user = model.getUser().getValue()) != null) {
            usernameInfo.setText(user.getUsername());
            emailInfo.setText(user.getEmail());
            phoneInfo.setText(user.getPhoneNumber());
            infoInfo.setText(user.getInfo());
        } else {
            Toast.makeText(requireContext(), "Failed to get user", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        return root;
    }
}
