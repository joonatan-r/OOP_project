package com.example.oop_project.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.oop_project.MainActivity;
import com.example.oop_project.R;

public class LoginActivity extends AppCompatActivity {
    LoginFragment loginFragment;
    Toolbar loginToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginToolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(loginToolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Log in");
        loginFragment = new LoginFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginFrame, loginFragment);
        transaction.commit();
    }

    public void finishLogin(String username) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }

    public void goToCreate(View v) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.loginFrame, new CreateAccountFragment());
        transaction.commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create account");
        }
        loginToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel(v);
            }
        });
    }

    public void cancel(View v) {
        getSupportFragmentManager().popBackStack();
    }
}
