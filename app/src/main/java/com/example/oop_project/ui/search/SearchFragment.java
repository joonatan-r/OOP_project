package com.example.oop_project.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oop_project.DataAccess;
import com.example.oop_project.R;
import com.example.oop_project.Reservation;
import com.example.oop_project.InfoViewModel;

import java.util.ArrayList;

class SearchReservationsAdapter extends RecyclerView.Adapter<SearchReservationsAdapter.ViewHolder> {
    private ArrayList<Reservation> reservationsList;
    private RecyclerView recyclerView;
    private InfoViewModel model;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.reservationText);
        }
    }

    SearchReservationsAdapter(RecyclerView recyclerView, ArrayList<Reservation> reservationsList, InfoViewModel model) {
        this.recyclerView = recyclerView;
        this.reservationsList = reservationsList;
        this.model = model;
    }

    /*
    Sets layout for items, and if it's a normal reservation, it's set up to navigate to reservation
    info fragment for that reservation.
     */
    @NonNull
    @Override
    public SearchReservationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_reservation, parent, false);

        if (viewType == 0) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = recyclerView.getChildAdapterPosition(v);
                    model.setReservation(reservationsList.get(position));
                    Navigation.findNavController(v).navigate(R.id.nav_reservation_info);
                }
            });
        }

        return new ViewHolder(view);
    }

    /*
    Sets items to have their reservation description as their text.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(reservationsList.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return reservationsList.size();
    }

    /*
    Item view type 0 is for normal reservation and 1 for the "No results" dummy item
     */
    @Override
    public int getItemViewType(int position) {
        if (reservationsList.get(position).getId() == null) return 1;

        return 0;
    }
}

public class SearchFragment extends Fragment {
    private EditText searchInput;
    private Switch exactSwitch;
    private final String[] spinnerItems = {"Search by...", "all", "owner", "hall", "description", "participants", "sport"};
    private String searchFrom = "all";
    private SearchReservationsAdapter adapter;
    private ArrayList<Reservation> reservationsList = new ArrayList<>();
    private DataAccess da;

    /*
    Sets up a RecyclerView with a SearchReservationsAdapter that's initially empty. Gets search
    input into a variable and sets up search button to perform the search and a spinner where the
    user can choose what reservation attributes to search from (default is "all"). The first item of
    the spinner is an unclickable "Search by...", which acts as a hint. A switch enables the user to
    control whether the search is exact or not, and initial value of the switch is false. The drawer
    item "My participations" navigates to this fragment and gives username as an argument, so if the
    argument is found, automatically perform a search for that username in participants with exact
    being true.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel model = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        da = new DataAccess(requireContext());
        RecyclerView recyclerView = root.findViewById(R.id.searchReservationsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SearchReservationsAdapter(recyclerView, reservationsList, model);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        searchInput = root.findViewById(R.id.searchInput);
        ImageButton searchButton = root.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        Spinner spinner = root.findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.addAll(spinnerItems);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // First item is automatically selected when creating view, and it needs to be ignored
                    searchFrom = spinnerItems[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        exactSwitch = root.findViewById(R.id.exactSwitch);
        exactSwitch.setChecked(false);

        if (getArguments() != null && !getArguments().isEmpty()) {
            searchInput.setText(getArguments().getString("user"));
            spinner.setSelection(spinnerAdapter.getPosition("participants"));
            searchFrom = "participants";
            exactSwitch.setChecked(true);
            search();
        }

        return root;
    }

    /*
    Performs search by calling DataAccess with the given inputs, clears SearchReservationsAdapter's
    data list of reservations and adds the returned results to it. If the returned list is empty,
    a dummy item that indicates no results were found is inserted.
     */
    private void search() {
        reservationsList.clear();
        reservationsList.addAll(da.searchReservation(searchInput.getText().toString(), searchFrom, exactSwitch.isChecked()));

        if (reservationsList.size() == 0) {
            reservationsList.add(new Reservation(null,null,null,null,null,"No results",0));
        }

        adapter.notifyDataSetChanged();
    }
}
