package com.example.oop_project.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oop_project.DataAccess;
import com.example.oop_project.InfoViewModel;
import com.example.oop_project.MainActivity;
import com.example.oop_project.R;
import com.example.oop_project.Reservation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ViewHolder> {
    private ArrayList<Reservation> reservationsList;
    private RecyclerView recyclerView;
    private Spinner hallSpinner;
    private InfoViewModel model;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView reservationText;
        TextView reservationTime;

        ViewHolder(View v) {
            super(v);
            reservationText = v.findViewById(R.id.reservationText);
            reservationTime = v.findViewById(R.id.reservationTime);
        }
    }

    ReservationsAdapter(RecyclerView recyclerView, ArrayList<Reservation> reservationsList, Spinner hallSpinner, InfoViewModel model) {
        this.recyclerView = recyclerView;
        this.reservationsList = reservationsList;
        this.hallSpinner = hallSpinner;
        this.model = model;
    }

    @NonNull
    @Override
    public ReservationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_reservation, parent, false);
        if (viewType == 0) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = recyclerView.getChildAdapterPosition(v);
                    model.setReservation(reservationsList.get(position));
                    Navigation.findNavController(v).navigate(R.id.nav_reservation_info);
                }
            });
        } else if (viewType == 1) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = recyclerView.getChildAdapterPosition(v);
                    String startTime = reservationsList.get(position).getStartTime();
                    Bundle bundle = new Bundle();
                    bundle.putString("hall", hallSpinner.getSelectedItem().toString());
                    bundle.putString("startTime", startTime);
                    Navigation.findNavController(v).navigate(R.id.nav_new_reservation, bundle);
                }
            });
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.reservationText.setText(reservationsList.get(position).getDescription());
        String startTime = reservationsList.get(position).getStartTime().split(" ")[1];
        String endTime = reservationsList.get(position).getEndTime().split(" ")[1];
        holder.reservationTime.setText(startTime + " - " + endTime);
    }

    @Override
    public int getItemCount() {
        return reservationsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (reservationsList.get(position).getId() == null) { // "Free time" item
            return 1;
        }

        return 0;
    }
}

public class HomeFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private ArrayList<Reservation> reservationsList = new ArrayList<>();
    private Button dateButton;
    private Spinner hallSpinner;
    private ReservationsAdapter adapter;
    private DataAccess da;
    private String dateString = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        da = new DataAccess(requireContext());
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        dateButton = root.findViewById(R.id.dateButton);
        hallSpinner = root.findViewById(R.id.hallSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.addAll(((MainActivity) requireActivity()).getHallNames());
        hallSpinner.setAdapter(spinnerAdapter);
        hallSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getReservations(dateString, parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        RecyclerView recyclerView = root.findViewById(R.id.reservationsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservationsAdapter(recyclerView, reservationsList, hallSpinner, model);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String currentDateString = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);

        if (dateString == null) dateString = currentDateString;

        final DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), this, year, month, day);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });

        return root;
    }

    @Override
    public void onResume() { // default search, today when opening first time, restores previous when user returns here
        super.onResume();
        reservationsList.clear();
        dateButton.setText(dateString);
        getReservations(dateString, hallSpinner.getSelectedItem().toString());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        dateString = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
        dateButton.setText(dateString);
        getReservations(dateString, hallSpinner.getSelectedItem().toString());
    }

    private void getReservations(String date, String hall) {
        ArrayList<Reservation> searchList = da.searchReservation(date, "startTime", false, hall);
        ArrayList<Reservation> resultList = new ArrayList<>(); // the shown list, a combination of free times and reservations
        final DateFormat df = new SimpleDateFormat("HH:mm");
        Date prevEndDate = null; // end time of the previous item
        Reservation reservation;
        long differenceInMinutes = 0;

        // reservations must be put to chronological order

        Collections.sort(searchList, new Comparator<Reservation>() {
            @Override
            public int compare(Reservation o1, Reservation o2) {
                try {
                    String startO1 = o1.getStartTime().split(" ")[1];
                    String startO2 = o2.getStartTime().split(" ")[1];
                    Date startDateO1 = df.parse(startO1);
                    Date startDateO2 = df.parse(startO2);

                    if (startDateO1 == null || startDateO2 == null) return 0;

                    if (startDateO1.before(startDateO2)) {
                        return -1;
                    } else {
                        return 1;
                    }
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        for (Reservation r : searchList) {
            String startTime = r.getStartTime().split(" ")[1];
            String endTime = r.getEndTime().split(" ")[1];

            try {
                Date startTimeDate = df.parse(startTime);
                Date midnight = df.parse("00:00");

                if (startTimeDate == null || midnight == null) continue;

                // the last minute before the reservation starts
                String lastFreeTime = df.format(new Date((startTimeDate.getTime() - 60000)));

                if (prevEndDate == null) { // first item
                    differenceInMinutes = (startTimeDate.getTime() - midnight.getTime()) / 60000;
                    // insert placeholder "reservation" that simply tells this time is free
                    reservation = new Reservation(null, date + " " + "00:00", date + " " + lastFreeTime, null, null, "Free time", 0);
                } else {
                    // the first minute after the previous reservation
                    String firstFreeTime = df.format(new Date((prevEndDate.getTime() + 60000)));
                    differenceInMinutes = (startTimeDate.getTime() - prevEndDate.getTime()) / 60000;
                    reservation = new Reservation(null, date + " " + firstFreeTime, date + " " + lastFreeTime, null, null, "Free time", 0);
                }

                if (differenceInMinutes >= 32) { // 30 min reservation minimum, +2 needed because reservations can't be in same minutes with others
                    resultList.add(reservation);
                }

                prevEndDate = df.parse(endTime);
                resultList.add(r);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (prevEndDate == null) { // no reservations
            reservation = new Reservation(null, date + " " + "00:00", date + " " + "23:59", null, null, "Free time", 0);
            resultList.add(reservation);
        } else {
            try {
                Date lastMinute = df.parse("23:59");

                if (lastMinute != null) differenceInMinutes = (lastMinute.getTime() - prevEndDate.getTime()) / 60000;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (differenceInMinutes >= 31) {
                String firstFreeTime = df.format(new Date((prevEndDate.getTime() + 60000)));
                reservation = new Reservation(null, date + " " + firstFreeTime, date + " " + "23:59", null, null, "Free time", 0);
                resultList.add(reservation);
            }
        }

        reservationsList.clear();
        reservationsList.addAll(resultList);
        adapter.notifyDataSetChanged();
    }
}
