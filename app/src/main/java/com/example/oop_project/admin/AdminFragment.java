package com.example.oop_project.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.oop_project.DataAccess;
import com.example.oop_project.Hall;
import com.example.oop_project.HallInfoContainer;
import com.example.oop_project.R;

import org.json.JSONException;

import java.io.IOException;

public class AdminFragment extends Fragment {
    private DataAccess da;
    private HallInfoContainer hic;

    /*
    Sets up buttons and initializes DataAccess and HallInfoContainer instances for accessing data.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        da = new DataAccess(requireContext());

        try {
            hic = new HallInfoContainer(requireContext());
        } catch (IOException | JSONException e) {
            Toast.makeText(requireContext(), "Failed to get hall info", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return null;
        }

        View root = inflater.inflate(R.layout.fragment_admin, container, false);
        Button addHallButton = root.findViewById(R.id.addHallButton);
        addHallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHall();
            }
        });
        Button deleteHallButton = root.findViewById(R.id.deleteHallButton);
        deleteHallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteHall();
            }
        });
        Button deleteUserButton = root.findViewById(R.id.deleteUserButton);
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
        Button deleteReservationButton = root.findViewById(R.id.deleteReservationButton);
        deleteReservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteReservation();
            }
        });
        Button deleteSportButton = root.findViewById(R.id.deleteSportButton);
        deleteSportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSport();
            }
        });

        return root;
    }

    /*
    Shows an alert dialog where properties of new hall can be inputted, then calls HallInfoContainer
    to add a new hall.
     */
    private void addHall() {
        final EditText idInput = new EditText(requireContext());
        idInput.setHint("Id");
        final EditText maxSizeInput = new EditText(requireContext());
        maxSizeInput.setHint("Max size");
        maxSizeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout inputView = new LinearLayout(requireContext());
        inputView.setOrientation(LinearLayout.VERTICAL);
        inputView.addView(idInput);
        inputView.addView(maxSizeInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Give properties of new hall")
                .setMessage("Note: restart for changes to apply")
                .setView(inputView)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = idInput.getText().toString();
                        String maxSizeText = maxSizeInput.getText().toString();
                        int maxSize;

                        try {
                            maxSize = Integer.parseInt(maxSizeText);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (id.equals("")) {
                            Toast.makeText(requireContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (hic.addHall(new Hall(id, maxSize))) {
                            Toast.makeText(requireContext(), "Added hall successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Adding hall failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
    Shows an alert dialog where hall id can be inputted, then calls HallInfoContainer to delete it.
    Also removes all reservations that were for the hall if deletion was successful.
     */
    private void deleteHall() {
        final EditText idInput = new EditText(requireContext());
        idInput.setHint("Id");

        new AlertDialog.Builder(requireContext())
                .setTitle("Give id of the hall to be deleted")
                .setMessage("Note: restart for changes to apply")
                .setView(idInput)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (hic.removeHall(idInput.getText().toString())) {
                            da.removeReservationsByField("hall", idInput.getText().toString());
                            Toast.makeText(requireContext(), "Deleted hall successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Deleting hall failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
    Shows an alert dialog where username can be inputted, then calls DataAccess to delete the user.
     */
    private void deleteUser() {
        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Username");

        new AlertDialog.Builder(requireContext())
                .setTitle("Give username of the user to be deleted")
                .setView(nameInput)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (da.removeUser(nameInput.getText().toString())) {
                            Toast.makeText(requireContext(), "Deleted user successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Deleting user failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
    Shows an alert dialog where reservation id can be inputted, then calls DataAccess to delete it.
     */
    private void deleteReservation() {
        final EditText idInput = new EditText(requireContext());
        idInput.setHint("Id");

        new AlertDialog.Builder(requireContext())
                .setTitle("Give id of the reservation to be deleted")
                .setMessage("(As admin you can see reservation id from its info page)")
                .setView(idInput)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (da.removeReservation(idInput.getText().toString())) {
                            Toast.makeText(requireContext(), "Deleted reservation successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Deleting reservation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*
    Shows an alert dialog where sport id can be inputted, then calls DataAccess to delete it.
     */
    private void deleteSport() {
        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Name");

        new AlertDialog.Builder(requireContext())
                .setTitle("Give name of the sport to be deleted")
                .setView(nameInput)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (da.removeSport(nameInput.getText().toString())) {
                            Toast.makeText(requireContext(), "Deleted sport successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Deleting sport failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
