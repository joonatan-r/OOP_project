package com.example.oop_project.login;

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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.oop_project.DataAccess;
import com.example.oop_project.R;

import java.util.Random;

public class LoginFragment extends Fragment {
    private EditText usernameInput;
    private EditText passwordInput;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        Button loginButton = root.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        usernameInput = root.findViewById(R.id.usernameInput);
        passwordInput = root.findViewById(R.id.passwordInput);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar toolbar = ((LoginActivity) requireActivity()).getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle("Log in");
            toolbar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void login() {
        DataAccess da = new DataAccess(requireContext());
        final String username = usernameInput.getText().toString();
        final String password = passwordInput.getText().toString();

        if (da.validateLogin(username, password)) {
            Random r = new Random();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                sb.append(r.nextInt(10));
            }

            final String passcode = sb.toString();
            final EditText codeInput = new EditText(requireContext());
            codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Give passcode")
                    .setView(codeInput)
                    .setMessage(passcode)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (codeInput.getText().toString().equals(passcode)) {
                                ((LoginActivity) requireActivity()).finishLogin(username);
                            } else {
                                Toast.makeText(requireContext(), "Incorrect passcode", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Failed to log in")
                    .setMessage("Could not log in with those inputs")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
        }
    }
}
