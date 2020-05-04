package com.example.oop_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.oop_project.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private String username;
    private String currentUserId;
    private ArrayList<Hall> hallsList;
    private String[] hallNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_account, R.id.nav_my_reservations, R.id.nav_search)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        username = getIntent().getStringExtra("USERNAME");

        if (username == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Fatal error")
                    .setMessage("Could not get user")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            finish();
            return;
        }

        if (username.equals(DataAccess.adminName)) {
            currentUserId = DataAccess.adminName;
            MenuItem item = navigationView.getMenu().add("Admin");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    navController.navigate(R.id.nav_admin);
                    drawer.closeDrawers();
                    return true;
                }
            });
        } else {
            DataAccess da = new DataAccess(this);
            currentUserId = da.getUser(username, "username").getId();
        }

        HallInfoContainer hic;

        try {
            hic = new HallInfoContainer(this);
        } catch (IOException | JSONException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Fatal error")
                    .setMessage("Could not get hall info")
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
            finish();
            return;
        }

        hallsList = hic.getHalls();
        hallNames = new String[hallsList.size()];

        for (int i = 0; i < hallsList.size(); i++) {
            hallNames[i] = hallsList.get(i).getId();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public ArrayList<Hall> getHallsList() {
        return hallsList;
    }

    public String[] getHallNames() {
        return hallNames;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void logout(MenuItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishLogout();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .show();
    }

    public void finishLogout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void getMyParticipations(MenuItem item) {
        Bundle bundle = new Bundle();
        bundle.putString("user", username);
        navController.navigate(R.id.nav_search, bundle);
        drawer.closeDrawers();
    }
}
