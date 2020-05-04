package com.example.oop_project.ui.reservation_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.oop_project.DataAccess;
import com.example.oop_project.MainActivity;
import com.example.oop_project.R;
import com.example.oop_project.Reservation;
import com.example.oop_project.InfoViewModel;
import com.example.oop_project.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReservationInfoFragment extends Fragment {
    private Button participateButton;
    private User owner;
    private Reservation reservation;
    private DataAccess da;
    private User currentUser;
    private String currentUserId;
    private String currentUserName;
    private boolean isParticipant = false;
    private ArrayList<String> nameList = new ArrayList<>();
    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private boolean initialSelectionDone;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        da = new DataAccess(requireContext());
        currentUserId = ((MainActivity) requireActivity()).getCurrentUserId();
        currentUser = da.getUser(currentUserId, "id");
        currentUserName = currentUser.getUsername();
        View root = inflater.inflate(R.layout.fragment_reservation_info, container, false);
        TextView hallInfo = root.findViewById(R.id.hallInfo);
        TextView startTimeInfo = root.findViewById(R.id.startTimeInfo);
        TextView endTimeInfo = root.findViewById(R.id.endTimeInfo);
        TextView maxParticipantsInfo = root.findViewById(R.id.maxParticipantsInfo);
        TextView descriptionInfo = root.findViewById(R.id.descriptionInfo);
        TextView sportInfo = root.findViewById(R.id.sportInfo);
        LinearLayout sportLayout = root.findViewById(R.id.sportLayout);
        participateButton = root.findViewById(R.id.participateButton);
        participateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleParticipation();
            }
        });
        Button ownerButton = root.findViewById(R.id.ownerButton);
        ownerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setUser(owner);
                Navigation.findNavController(v).navigate(R.id.nav_user_info);
            }
        });
        Spinner participantsSpinner = root.findViewById(R.id.participantsSpinner);
        spinnerAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        participantsSpinner.setAdapter(spinnerAdapter);
        initialSelectionDone = false;
        participantsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!initialSelectionDone) {
                    initialSelectionDone = true; // first item is selected automatically when creating view
                    return;
                }

                User user = usersList.get(position - 1); // nameList has (unclickable) "Users" as the first item
                model.setUser(user);
                Navigation.findNavController(view).navigate(R.id.nav_user_info);
                parent.setSelection(0); // "Users" will always be the first item, operating as a header
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (model.getReservation() != null && (reservation = model.getReservation().getValue()) != null) {
            hallInfo.setText(reservation.getHall());
            startTimeInfo.setText(reservation.getStartTime());
            endTimeInfo.setText(reservation.getEndTime());
            maxParticipantsInfo.setText(String.valueOf(reservation.getMaxParticipants()));
            descriptionInfo.setText(reservation.getDescription());
            owner = da.getUser(reservation.getOwner(), "id");
            ownerButton.setText(owner.getUsername());

            if (reservation.getSport() != null) {
                sportInfo.setText(reservation.getSport());
            } else {
                sportLayout.setVisibility(View.GONE);
            }

            usersList = da.getParticipants(reservation.getId());
            nameList.clear(); // if nameList isn't cleared here, items get duplicated when navigating back
            nameList.add("Users");

            for (User u : usersList) {
                nameList.add(u.getUsername());
            }

            spinnerAdapter.addAll(nameList);

            if (nameList.indexOf(currentUserName) != -1) {
                isParticipant = true;
                participateButton.setText("Cancel participation");
            }

            try {
                Calendar cal = Calendar.getInstance();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date startDate = df.parse(reservation.getStartTime());

                if (cal.getTime().after(startDate)) {
                    participateButton.setEnabled(false);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireContext(), "Failed to get reservation", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        if (currentUserId.equals(DataAccess.adminName)) {
            TextView id = root.findViewById(R.id.reservationId);
            id.setText("Id: " + reservation.getId());
            id.setVisibility(View.VISIBLE);
        }

        return root;
    }

    private void toggleParticipation() {
        if (isParticipant) {
            if (da.removeParticipant(reservation.getId(), currentUserId)) {
                participateButton.setText("Participate");
                Toast.makeText(requireContext(), "Removed participation successfully", Toast.LENGTH_SHORT).show();
                nameList.remove(currentUserName);

                int idx = -1;
                for (int i = 0; i < usersList.size(); i++) if (usersList.get(i).getId().equals(currentUserId)) idx = i;
                usersList.remove(idx);
                spinnerAdapter.clear();
                spinnerAdapter.addAll(nameList);
                isParticipant = false;
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Removing participation failed")
                        .setMessage("Unexpected error")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
            }

            return;
        } else {
            int result = da.addParticipant(reservation.getId(), currentUserId);

            if (result == 0) {
                participateButton.setText("Cancel participation");
                Toast.makeText(requireContext(), "Added to participants successfully", Toast.LENGTH_SHORT).show();
                nameList.add(currentUserName);
                usersList.add(currentUser);
                spinnerAdapter.clear();
                spinnerAdapter.addAll(nameList);
                isParticipant = true;
                return;
            } else if (result == 1) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Adding to participants failed")
                        .setMessage("Reservation is full")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                return;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Adding to participants failed")
                .setMessage("Unexpected error")
                .setPositiveButton(android.R.string.yes, null)
                .show();
    }
}
