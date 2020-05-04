package com.example.oop_project.ui.sport_info;

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
import com.example.oop_project.Sport;

public class SportInfoFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_sport_info, container, false);
        TextView nameInfo = root.findViewById(R.id.nameInfo);
        TextView maxParticipantsInfo = root.findViewById(R.id.maxParticipantsInfo);
        TextView descriptionInfo = root.findViewById(R.id.descriptionInfo);
        Sport sport;

        if (model.getSport() != null && (sport = model.getSport().getValue()) != null) {
            nameInfo.setText(sport.getName());
            maxParticipantsInfo.setText(String.valueOf(sport.getMaxParticipants()));
            descriptionInfo.setText(sport.getDescription());
        } else {
            Toast.makeText(requireContext(), "Failed to get sport", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        return root;
    }
}
