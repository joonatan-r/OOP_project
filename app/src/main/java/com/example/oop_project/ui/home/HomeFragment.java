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

    /*
    Sets layout for items and if it's a normal reservation, it's set up to navigate to reservation
    info fragment for that reservation. If it's a "Free time" item, it's set up to navigate to new
    reservation fragment with the selected hall and the item's start time as arguments, which sets
    the new reservations hall and date according to them.
     */
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

    /*
    Sets items to have their description as their text and their start and end times as their time
    text.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.reservationText.setText(reservationsList.get(position).getDescription());
        // Reservation start and end times are in format yyyy.MM.dd HH:mm
        String startTime = reservationsList.get(position).getStartTime().split(" ")[1];
        String endTime = reservationsList.get(position).getEndTime().split(" ")[1];
        holder.reservationTime.setText(startTime + " - " + endTime);
    }

    @Override
    public int getItemCount() {
        return reservationsList.size();
    }

    /*
    Item view type 0 is for normal reservations and 1 is the "Free time" item
     */
    @Override
    public int getItemViewType(int position) {
        if (reservationsList.get(position).getId() == null) {
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

    /*
    Sets up a spinner where the user can select the hall, date button that opens a date picker that
    is used to update dateString, and a RecyclerView with a ReservationsAdapter. When a hall is
    selected or a date is picked, a search is performed with the current value of dateString and the
    currently selected hall by calling getReservations, which updates the shown reservations by
    updating the ReservationsAdapter's data list. Initial value for dateString is the current date.
     */
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

        if (dateString == null) dateString = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);

        final DatePickerDialog dateDialog = new DatePickerDialog(requireContext(), this, year, month, day);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });

        return root;
    }

    /*
    Performs default search by clearing ReservationsAdapter data list, calling getReservations with
    default date and hall and updating date button's text to the date's value. The default date is
    the current date when opening fragment first time and previously used date when navigating back
    here. The default hall is whatever hall is selected in hall spinner.
     */
    @Override
    public void onResume() {
        super.onResume();
        reservationsList.clear();
        dateButton.setText(dateString);
        getReservations(dateString, hallSpinner.getSelectedItem().toString());
        adapter.notifyDataSetChanged();
    }

    /*
    This fragment is also a listener for the date picker. When date is set, update dateString and
    update shown reservations by calling getReservations with it and the currently selected hall.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        dateString = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
        dateButton.setText(dateString);
        getReservations(dateString, hallSpinner.getSelectedItem().toString());
    }

    /*
    Takes date and hall Strings as parameters and performs a search by calling DataAccess with them.
    The search is made from startTime with "exact" as false, because reservation's start times
    contain the date and time, and the goal is to get all reservations in that hall at that date.
    The Reservation objects in the ArrayList returned from the search are then sorted to
    chronological order, which is necessary for making the shown list properly. The shown list is a
    combination of the found reservations and "Free time" dummy reservations inserted between them.
    The ArrayList is then iterated through, and all reservations are added to resultList, but before
    the first one is inserted a "Free time" from 00:00 to the last minute before the first
    reservation starts, and between each reservation is inserted a "Free time" from first to last
    minute between the reservations. After the last reservation is inserted a "Free time" from the
    first free minute to 23:59. If the search found no reservations, a "Free time" from 00:00 to
    23:59 is inserted and will be the only item. Note: if any of the "Free time" items would be less
    than 30 minutes in length, they are not inserted, as that is the minimum length for a
    reservation. After resultList is ready, ReservationsAdapter's data list is cleared and
    resultList is added.
     */
    private void getReservations(String date, String hall) {
        ArrayList<Reservation> searchList = da.searchReservation(date, "startTime", false, hall);
        ArrayList<Reservation> resultList = new ArrayList<>(); // The shown list
        final DateFormat df = new SimpleDateFormat("HH:mm");
        Date prevEndDate = null; // End time of the previous item
        Reservation reservation;
        long differenceInMinutes = 0;

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
            // Reservation start and end times are in format yyyy.MM.dd HH:mm
            String startTime = r.getStartTime().split(" ")[1];
            String endTime = r.getEndTime().split(" ")[1];

            try {
                Date startTimeDate = df.parse(startTime);
                Date midnight = df.parse("00:00");

                if (startTimeDate == null || midnight == null) continue;

                // The last minute before the reservation starts
                String lastFreeTime = df.format(new Date((startTimeDate.getTime() - 60000)));

                if (prevEndDate == null) { // True if this is the first item
                    differenceInMinutes = (startTimeDate.getTime() - midnight.getTime()) / 60000;
                    // Insert placeholder "reservation" that simply tells this time is free
                    reservation = new Reservation(null, date + " " + "00:00", date + " " + lastFreeTime, null, null, "Free time", 0);
                } else {
                    // The first minute after the previous reservation
                    String firstFreeTime = df.format(new Date((prevEndDate.getTime() + 60000)));
                    differenceInMinutes = (startTimeDate.getTime() - prevEndDate.getTime()) / 60000;
                    reservation = new Reservation(null, date + " " + firstFreeTime, date + " " + lastFreeTime, null, null, "Free time", 0);
                }

                if (differenceInMinutes >= 32) { // 30 min reservation minimum, + 2 needed because reservations can't be in same minutes with others
                    resultList.add(reservation);
                }

                prevEndDate = df.parse(endTime);
                resultList.add(r);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (prevEndDate == null) { // No reservations
            reservation = new Reservation(null, date + " " + "00:00", date + " " + "23:59", null, null, "Free time", 0);
            resultList.add(reservation);
        } else {
            try {
                Date lastMinute = df.parse("23:59");

                if (lastMinute != null) {
                    differenceInMinutes = (lastMinute.getTime() - prevEndDate.getTime()) / 60000;

                    if (differenceInMinutes >= 31) {
                        String firstFreeTime = df.format(new Date((prevEndDate.getTime() + 60000)));
                        reservation = new Reservation(null, date + " " + firstFreeTime, date + " " + "23:59", null, null, "Free time", 0);
                        resultList.add(reservation);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        reservationsList.clear();
        reservationsList.addAll(resultList);
        adapter.notifyDataSetChanged();
    }
}
