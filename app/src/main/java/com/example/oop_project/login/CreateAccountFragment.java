package com.example.oop_project.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.oop_project.DataAccess;
import com.example.oop_project.R;
import com.example.oop_project.User;

public class CreateAccountFragment extends Fragment {
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPassWordInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText infoInput;

    /*
    Sets up create button and gets input views to variables.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_account, container, false);
        usernameInput = root.findViewById(R.id.usernameInput);
        passwordInput = root.findViewById(R.id.passwordInput);
        confirmPassWordInput = root.findViewById(R.id.confirmPasswordInput);
        emailInput = root.findViewById(R.id.emailInput);
        phoneInput = root.findViewById(R.id.phoneInput);
        infoInput  = root.findViewById(R.id.infoInput);
        Button createButton = root.findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
        return root;
    }

    /*
    Gets input values for username, password, email, phone number and user info and checks that none
    of them are empty, info is at least 10 characters long and that the same password is inputted
    twice. The password needs to be at least 12 characters long and contain at least one number,
    upper case letter, lower case letter and special character. Also generates an id for the user
    with System.currentTimeMillis, which is a sufficient unique id for this project. Even though
    usernames are unique, user has an id that is used in reservations so that when changing username
    only the name recorded for this user in the users file needs to be updated. If all inputs meet
    the required conditions, calls DataAccess to add a new user. If successful, automatically
    restores login fragment.
     */
    private void createUser() {
        String id = String.valueOf(System.currentTimeMillis());
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPassWordInput.getText().toString();
        String email = emailInput.getText().toString();
        String phoneNumber = phoneInput.getText().toString();
        String info = infoInput.getText().toString();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Please give the same password twice", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals("") || password.equals("") || email.equals("") || phoneNumber.equals("") || info.equals("")) {
            Toast.makeText(requireContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (info.length() < 10) {
            Toast.makeText(requireContext(), "Minimum length for info is 10 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasDigit = false;
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else {
                hasSpecialChar = true;
            }
        }

        if (password.length() < 12 || !(hasDigit && hasLowerCase && hasUpperCase && hasSpecialChar)) {
            Toast.makeText(requireContext(),
                    "Password must be at least 12 characters long and have at least one of each: lower case letter, upper case letter, number and special character",
                    Toast.LENGTH_LONG).show();
            return;
        }

        User user = new User(id, username, password, email, phoneNumber, info);
        DataAccess da = new DataAccess(requireContext());
        int result = da.addUser(user);

        if (result > 0) {
            new AlertDialog.Builder(requireContext())
                .setTitle("Creating account failed")
                .setMessage("Username is already taken")
                .setPositiveButton(android.R.string.yes, null)
                .show();
            return;
        } else if (result < 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Creating account failed")
                    .setMessage("Unexpected error")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            return;
        }

        Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
    }
}
