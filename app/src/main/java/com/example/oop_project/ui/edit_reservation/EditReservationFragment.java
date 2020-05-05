package com.example.oop_project.ui.edit_reservation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.oop_project.DataAccess;
import com.example.oop_project.Hall;
import com.example.oop_project.MainActivity;
import com.example.oop_project.R;
import com.example.oop_project.Reservation;
import com.example.oop_project.InfoViewModel;
import com.example.oop_project.Sport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EditReservationFragment extends Fragment {
    private EditText startTimeInput;
    private EditText endTimeInput;
    private EditText dateInput;
    private EditText maxParticipantsInput;
    private EditText descriptionInput;
    private Spinner hallSpinner;
    private Spinner sportSpinner;
    private Reservation reservation;
    private ArrayList<Sport> sportsList;
    private DataAccess da;

    /*
    The same as new reservation fragment's onCreateView, except the reservation being edited is
    gotten from InfoViewModel and the inputs are initialized with values got from it, and an info
    button is set up to navigate to this reservation's reservation info fragment. InfoViewModel's
    reservation has to be set before navigating here, or this immediately navigates back.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        da = new DataAccess(requireContext());
        final InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_edit_reservation, container, false);
        startTimeInput = root.findViewById(R.id.startTimeInput);
        endTimeInput = root.findViewById(R.id.endTimeInput);
        dateInput = root.findViewById(R.id.dateInput);
        descriptionInput = root.findViewById(R.id.descriptionInput);
        maxParticipantsInput = root.findViewById(R.id.maxParticipantsInput);
        Button addSportButton = root.findViewById(R.id.addSportButton);
        addSportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText nameInput = new EditText(requireContext());
                nameInput.setHint("Name");
                final EditText maxParticipantsInput = new EditText(requireContext());
                maxParticipantsInput.setHint("Max participants");
                maxParticipantsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                final EditText sportDescriptionInput = new EditText(requireContext());
                sportDescriptionInput.setHint("Description");
                LinearLayout inputView = new LinearLayout(requireContext());
                inputView.setOrientation(LinearLayout.VERTICAL);
                inputView.addView(nameInput);
                inputView.addView(maxParticipantsInput);
                inputView.addView(sportDescriptionInput);

                new AlertDialog.Builder(requireContext())
                        .setTitle("Add a new sport to the system")
                        .setMessage("Give the new sport's name and maximum number of participants")
                        .setView(inputView)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = nameInput.getText().toString();
                                String description = sportDescriptionInput.getText().toString();
                                int maxParticipants;

                                try {
                                    maxParticipants = Integer.parseInt(maxParticipantsInput.getText().toString());
                                } catch (Exception e) {
                                    Toast.makeText(requireContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (name.length() == 0 || maxParticipants <= 0) {
                                    Toast.makeText(requireContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (description.length() < 10) {
                                    Toast.makeText(requireContext(), "Minimum length for description is 10 characters", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int result = da.addSport(new Sport(name, description, maxParticipants));

                                if (result > 0) {
                                    Toast.makeText(requireContext(), "That sport already exists!", Toast.LENGTH_SHORT).show();
                                } else if (result < 0) {
                                    Toast.makeText(requireContext(), "Adding sport failed unexpectedly", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Added sport successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        Button sportInfoButton = root.findViewById(R.id.sportInfoButton);
        sportInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sportsList != null && sportSpinner.getSelectedItemPosition() != 0) {
                    model.setSport(sportsList.get(sportSpinner.getSelectedItemPosition() - 1));
                    Navigation.findNavController(v).navigate(R.id.nav_sport_info);
                }
            }
        });
        Button cancelButton = root.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
        Button deleteButton = root.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete reservation")
                        .setMessage("Are you sure you want to delete this reservation?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteReservation();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
        Button confirmButton = root.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReservation();
            }
        });
        Button infoButton = root.findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Model already has the right reservation
                Navigation.findNavController(v).navigate(R.id.nav_reservation_info);
            }
        });
        hallSpinner = root.findViewById(R.id.hallSpinner);
        ArrayAdapter<String> hallSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        hallSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hallSpinnerAdapter.addAll(((MainActivity) requireActivity()).getHallNames());
        hallSpinner.setAdapter(hallSpinnerAdapter);
        sportSpinner = root.findViewById(R.id.sportSpinner);
        ArrayAdapter<String> sportSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        sportSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sportsList = da.getSports();
        ArrayList<String> sportNames = new ArrayList<>();

        for (Sport s : sportsList) sportNames.add(s.getName());

        sportSpinnerAdapter.add("Not defined");
        sportSpinnerAdapter.addAll(sportNames);
        sportSpinner.setAdapter(sportSpinnerAdapter);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        final DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                dateInput.setText(year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day));
            }
        }, year, month, day);
        final TimePickerDialog startTimeDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startTimeInput.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
            }
        }, hour, minute, DateFormat.is24HourFormat(getActivity()));
        final TimePickerDialog endTimeDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endTimeInput.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
            }
        }, hour, minute, DateFormat.is24HourFormat(getActivity()));

        Button dateButton = root.findViewById(R.id.dateButton);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });
        Button startTimeButton = root.findViewById(R.id.startTimeButton);
        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeDialog.show();
            }
        });
        Button endTimeButton = root.findViewById(R.id.endTimeButton);
        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimeDialog.show();
            }
        });

        if (model.getReservation() != null && (reservation = model.getReservation().getValue()) != null) {
            hallSpinner.setSelection(hallSpinnerAdapter.getPosition(reservation.getHall()));

            String date = reservation.getStartTime().split(" ")[0];
            String startTime = reservation.getStartTime().split(" ")[1];
            String endTime = reservation.getEndTime().split(" ")[1];

            dateInput.setText(date);
            startTimeInput.setText(startTime);
            endTimeInput.setText(endTime);
            maxParticipantsInput.setText(String.valueOf(reservation.getMaxParticipants()));
            descriptionInput.setText(reservation.getDescription());

            if (reservation.getSport() != null) {
                int position = sportSpinnerAdapter.getPosition(reservation.getSport());

                if (position >= 0) sportSpinner.setSelection(position);
            }
        } else {
            Toast.makeText(requireContext(), "Failed to get reservation", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        return root;
    }

    /*
    The same as new reservation fragment's createReservation, except id is kept the same and
    DataAccess is called to edit reservation instead of adding.
     */
    private void editReservation() {
        String id = reservation.getId();
        String owner = reservation.getOwner();
        String hall = hallSpinner.getSelectedItem().toString();
        String startTime = dateInput.getText().toString() + " " + startTimeInput.getText().toString();
        String endTime = dateInput.getText().toString() + " " + endTimeInput.getText().toString();
        String description = descriptionInput.getText().toString();
        String sportName = sportSpinner.getSelectedItem().toString();
        boolean sportSelected = sportSpinner.getSelectedItemPosition() != 0; // "Not defined" item is at 0
        ArrayList<Hall> hallsList = ((MainActivity) requireActivity()).getHallsList();

        if (hallsList == null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Editing reservation failed")
                    .setMessage("Fatal error, restart app")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        int hallMax = hallsList.get(hallSpinner.getSelectedItemPosition()).getMaxSize();
        int sportMax = sportSelected ? sportsList.get(sportSpinner.getSelectedItemPosition() - 1).getMaxParticipants() : 0;
        String maxParticipantsText = maxParticipantsInput.getText().toString();
        int maxParticipants;

        try {
            maxParticipants = Integer.parseInt(maxParticipantsText);

            if (maxParticipants <= 0) {
                Toast.makeText(requireContext(), "Invalid number for max participants", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid number for max participants", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((sportSelected && maxParticipants > sportMax) || maxParticipants > hallMax) {
            Toast.makeText(requireContext(), "Max participants can't exceed maximum of hall or sport", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        java.text.DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startTimeDate, endTimeDate;

        try {
            startTimeDate = df.parse(startTime);
            endTimeDate = df.parse(endTime);

            if (startTimeDate == null || endTimeDate == null) throw new ParseException("", 0);

            long differenceInMinutes = (endTimeDate.getTime() - startTimeDate.getTime()) / 60000;

            if (cal.getTime().after(startTimeDate)) {
                Toast.makeText(requireContext(), "Past times can't be reserved!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (differenceInMinutes < 30) {
                Toast.makeText(requireContext(), "Minimum length for reservation is 30 minutes", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Editing reservation failed")
                    .setMessage("Unexpected error")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        if (description.length() < 10) {
            Toast.makeText(requireContext(), "Minimum length for description is 10 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        Reservation editedReservation;

        if (sportSelected) {
            editedReservation = new Reservation(id, startTime, endTime, hall, owner, description, maxParticipants);
        } else {
            editedReservation = new Reservation(id, startTime, endTime, hall, owner, description, maxParticipants, sportName);
        }

        int result = da.editReservation(id, editedReservation);

        if (result > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Editing reservation failed")
                    .setMessage("Hall is taken at that time")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        } else if (result < 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Editing reservation failed")
                    .setMessage("Unexpected error")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        Toast.makeText(requireContext(), "Edited reservation", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
    }

    /*
    Calls DataAccess to remove the reservation with the id of the reservation being edited.
    Automatically navigates back if successful.
     */
    private void deleteReservation() {
        DataAccess da = new DataAccess(requireContext());

        if (da.removeReservation(reservation.getId())) {
            Toast.makeText(requireContext(), "Deleted reservation", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        } else {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Deleting reservation failed")
                    .setMessage("Unexpected error")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
        }
    }
}
