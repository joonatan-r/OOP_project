package com.example.oop_project.ui.my_reservations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oop_project.DataAccess;
import com.example.oop_project.MainActivity;
import com.example.oop_project.R;
import com.example.oop_project.Reservation;
import com.example.oop_project.InfoViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

class MyReservationsAdapter extends RecyclerView.Adapter<MyReservationsAdapter.ViewHolder> {
    private ArrayList<Reservation> reservationsList;
    private RecyclerView recyclerView;
    private InfoViewModel model;
    private Calendar cal;
    private DateFormat df;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView reservationText;
        ImageView editIcon;

        ViewHolder(View v) {
            super(v);
            reservationText = v.findViewById(R.id.reservationText);
            editIcon = v.findViewById(R.id.editIcon);
        }
    }

    MyReservationsAdapter(RecyclerView recyclerView, ArrayList<Reservation> reservationsList, InfoViewModel model) {
        this.recyclerView = recyclerView;
        this.reservationsList = reservationsList;
        // insert null, because the first recycler item is a button for creating new reservation
        this.reservationsList.add(0,null);
        this.model = model;

        cal = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    @NonNull
    @Override
    public MyReservationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_reservation, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = recyclerView.getChildAdapterPosition(v);
                    Reservation reservation = reservationsList.get(position);
                    model.setReservation(reservation);

                    try {
                        Date startDate = df.parse(reservation.getStartTime());

                        if (cal.getTime().after(startDate)) {
                            Navigation.findNavController(v).navigate(R.id.nav_reservation_info);
                        } else {
                            Navigation.findNavController(v).navigate(R.id.nav_edit_reservation);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (viewType == 1) { // first item, which is an add new button
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_reservation, parent, false);
            view.findViewById(R.id.newReservationButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(v).navigate(R.id.nav_new_reservation);
                }
            });
        } else { // "You don't have reservations yet" item
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_reservation, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position != 0) {
            holder.reservationText.setText(reservationsList.get(position).getDescription());

            if (reservationsList.get(position).getId() == null) { // "You don't have reservations yet" item
                holder.editIcon.setVisibility(View.INVISIBLE);
                return;
            }

            try {
                Date startDate = df.parse(reservationsList.get(position).getStartTime());

                if (cal.getTime().after(startDate)) {
                    holder.editIcon.setVisibility(View.INVISIBLE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return reservationsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;

        if (reservationsList.get(position).getId() == null) return 2; // "You don't have reservations yet" item

        return 0;
    }
}

public class MyReservationsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_my_reservations, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.myReservationsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        String owner = ((MainActivity) requireActivity()).getCurrentUserId();
        DataAccess da = new DataAccess(requireContext());
        ArrayList<Reservation> reservationsList = da.searchReservation(owner, "owner", true);

        if (reservationsList.size() == 0) {
            reservationsList.add(new Reservation(null,null,null,null,null,"You don't have any reservations yet",0));
        }

        MyReservationsAdapter adapter = new MyReservationsAdapter(recyclerView, reservationsList, model);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        return root;
    }
}
