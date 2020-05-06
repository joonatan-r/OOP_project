package com.example.oop_project.ui.new_reservation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.oop_project.DataAccess;
import com.example.oop_project.Hall;
import com.example.oop_project.InfoViewModel;
import com.example.oop_project.MainActivity;
import com.example.oop_project.R;
import com.example.oop_project.Reservation;
import com.example.oop_project.Sport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NewReservationFragment extends Fragment {
    private EditText startTimeInput;
    private EditText endTimeInput;
    private EditText dateInput;
    private EditText maxParticipantsInput;
    private EditText descriptionInput;
    private Spinner hallSpinner;
    private Spinner sportSpinner;
    private String owner;
    private ArrayList<Hall> hallsList = null;
    private ArrayList<Sport> sportsList = null;
    private DataAccess da;

    /*
    Sets up inputs for giving the new reservation's attributes. Sets cancel button to simply
    navigate back, confirm button to call createReservation and sport info button to navigate to
    sport info fragment for the currently selected sport. Add sport button is set to open an alert
    dialog where the user can input the attributes of a new sport to be added in the system. It is
    checked that name isn't empty, max participants is a valid positive number and description is at
    least 10 characters long. If all requirements are met, DataAccess is called to add a new sport.
    The reservation's date and start and end times are inputted by clicking a button that is set to
    open a date or time picker. The pickers set the corresponding inputs to have the selected date
    or times as formatted strings. Hall and sport are set to be selected from their own spinners.
    Sports are gotten from DataAccess and halls from main activity. Sport can also be "Not defined",
    which is the first item in the spinner. Home fragments "Free time" items navigate here giving
    the hall and start time as string arguments, so if those arguments are found, hall and date
    input's initial values are set accordingly.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        da = new DataAccess(requireContext());
        owner = ((MainActivity) requireActivity()).getCurrentUserId();
        final InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        hallsList = ((MainActivity) requireActivity()).getHallsList();

        if (hallsList == null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Getting hall info failed")
                    .setMessage("Fatal error, restart app")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return null;
        }

        View root = inflater.inflate(R.layout.fragment_new_reservation, container, false);
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
        Button confirmButton = root.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReservation();
            }
        });
        hallSpinner = root.findViewById(R.id.hallSpinner);
        ArrayAdapter<String> hallSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        hallSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hallSpinnerAdapter.addAll(((MainActivity) requireActivity()).getHallNames());
        hallSpinner.setAdapter(hallSpinnerAdapter);
        final TextView hallMaxInfo = root.findViewById(R.id.hallMaxInfo);
        hallSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int hallMax = hallsList.get(position).getMaxSize();

                hallMaxInfo.setText(String.valueOf(hallMax));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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

        if (getArguments() != null) {
            String hall = getArguments().getString("hall");
            String startTime = getArguments().getString("startTime");
            int position = hallSpinnerAdapter.getPosition(hall);

            if (position >= 0) hallSpinner.setSelection(position);

            // Reservation start times are in format yyyy.MM.dd HH:mm

            if (startTime != null) dateInput.setText(startTime.split(" ")[0]);
        }

        return root;
    }

    /*
    Checks all the inputs for the new reservation. Max participants has to be a valid positive
    number, and it is checked that it isn't more than the selected hall's max size or the selected
    sports max participants, if a sport is selected. Checks that end time is at least 30 minutes
    after the start time, which is the minimum, and that start time isn't already gone. The
    description has to be at least 10 characters long. Also generates an id for the reservation
    with System.currentTimeMillis, which is a sufficient unique id for this project. If all
    requirements are met, a Reservation object is created and DataAccess is called to add a
    reservation with it. Automatically navigates back if the new reservation was added successfully.
     */
    private void createReservation() {
        String id = String.valueOf(System.currentTimeMillis()); // Sufficient id for this project
        String hall = hallSpinner.getSelectedItem().toString();
        String startTime = dateInput.getText().toString() + " " + startTimeInput.getText().toString();
        String endTime = dateInput.getText().toString() + " " + endTimeInput.getText().toString();
        String description = descriptionInput.getText().toString();
        String sportName = sportSpinner.getSelectedItem().toString();
        boolean sportSelected = sportSpinner.getSelectedItemPosition() != 0; // "Not defined" item is at 0
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
                    .setTitle("Creating reservation failed")
                    .setMessage("Unexpected error")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        if (description.length() < 10) {
            Toast.makeText(requireContext(), "Minimum length for description is 10 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        Reservation reservation;

        if (sportSelected) {
            reservation = new Reservation(id, startTime, endTime, hall, owner, description, maxParticipants);
        } else {
            reservation = new Reservation(id, startTime, endTime, hall, owner, description, maxParticipants, sportName);
        }

        int result = da.addReservation(reservation);

        if (result > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Creating reservation failed")
                    .setMessage("Hall is taken at that time")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        } else if (result < 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Creating reservation failed")
                    .setMessage("Unexpected error")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        Toast.makeText(requireContext(), "Reservation created successfully", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
    }
}
