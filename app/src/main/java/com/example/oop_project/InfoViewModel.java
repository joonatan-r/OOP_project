package com.example.oop_project;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {
    private MutableLiveData<Reservation> reservation = new MutableLiveData<>();
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<Sport> sport = new MutableLiveData<>();

    public InfoViewModel() {}

    public LiveData<Reservation> getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation.setValue(reservation);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public LiveData<Sport> getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport.setValue(sport);
    }
}
