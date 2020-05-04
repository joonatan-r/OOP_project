package com.example.oop_project.ui.my_account;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.oop_project.MainActivity;
import com.example.oop_project.R;
import com.example.oop_project.User;

public class MyAccountFragment extends Fragment {
    private EditText usernameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText infoInput;
    private EditText passwordInput;
    private EditText confirmPassWordInput;
    private EditText givePassword;
    private DataAccess da;
    private String id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String info;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        da = new DataAccess(requireContext());
        id = ((MainActivity) requireActivity()).getCurrentUserId();

        if (id.equals(DataAccess.adminName)) {
            requireActivity().onBackPressed();
            Toast.makeText(requireContext(), "Admin doesn't have normal account page", Toast.LENGTH_SHORT).show();
            return null;
        }

        User user = da.getUser(id, "id");
        username = user.getUsername();
        password = user.getPassword();
        email = user.getEmail();
        phoneNumber = user.getPhoneNumber();
        info = user.getInfo();

        View root = inflater.inflate(R.layout.fragment_my_account, container, false);
        usernameInput = root.findViewById(R.id.usernameInput);
        usernameInput.setText(username);
        emailInput = root.findViewById(R.id.emailInput);
        emailInput.setText(email);
        phoneInput = root.findViewById(R.id.phoneInput);
        phoneInput.setText(phoneNumber);
        infoInput  = root.findViewById(R.id.infoInput);
        infoInput.setText(info);
        passwordInput = root.findViewById(R.id.passwordInput);
        confirmPassWordInput = root.findViewById(R.id.confirmPasswordInput);
        Button confirmButton = root.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        Button cancelButton = root.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
        return root;
    }

    private void confirm() {
        if (!passwordInput.getText().toString().equals(confirmPassWordInput.getText().toString())) {
            Toast.makeText(requireContext(), "Please give the same password twice", Toast.LENGTH_SHORT).show();
            return;
        }

        // empty input means field won't be changed

        if (!infoInput.getText().toString().equals("") && infoInput.getText().toString().length() < 10) {
            Toast.makeText(requireContext(), "Minimum length for info is 10 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasDigit = false;
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;
        boolean hasSpecialChar = false;

        for (char c : passwordInput.getText().toString().toCharArray()) {
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

        if (!passwordInput.getText().toString().equals("")
                && (passwordInput.getText().toString().length() < 12 || !(hasDigit && hasLowerCase && hasUpperCase && hasSpecialChar))) {
            Toast.makeText(requireContext(),
                    "Password must be at least 12 characters long and have at least one of each: lower case letter, upper case letter, number and special character",
                    Toast.LENGTH_LONG).show();
            return;
        }

        givePassword = new EditText(requireContext());
        givePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm changes")
                .setMessage("Give password to confirm")
                .setView(givePassword)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validateConfirm();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void validateConfirm() {
        if (da.validateLogin(username, givePassword.getText().toString())) {
            String newName = usernameInput.getText().toString();
            String newEmail = emailInput.getText().toString();
            String newPhoneNumber = phoneInput.getText().toString();
            String newInfo = infoInput.getText().toString();
            String newPassword = passwordInput.getText().toString();

            if (newEmail.equals("")) newEmail = email;
            if (newPhoneNumber.equals("")) newPhoneNumber = phoneNumber;
            if (newInfo.equals("")) newInfo = info;
            if (newPassword.equals("")) newPassword = password;

            // if username isn't being changed, have it as null, because editUser checks
            // if the name is already taken and this way it knows not to check it

            if (newName.equals("") || newName.equals(username)) newName = null;

            User user = new User(id, newName, newPassword, newEmail, newPhoneNumber, newInfo);
            int result = da.editUser(id, user);

            if (result > 0) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Making changes failed")
                        .setMessage("Username is already taken")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                return;
            } else if (result < 0) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Making changes failed")
                        .setMessage("Unexpected error")
                        .setPositiveButton(android.R.string.yes, null)
                        .show();
                return;
            }

            if (newName != null) {
                ((MainActivity) requireActivity()).setUsername(newName);
            }

            Toast.makeText(requireContext(), "Changes made successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        } else {
            Toast.makeText(requireContext(), "Password incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}
