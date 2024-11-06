package com.example.caracceleration;

public interface ISubscription {
    void onPhaseChange(String newPhase, int remainingTime);
    int getNumberOfCars();
    int[] getPhaseDurations();
    String[] getPhases();
    int getPassingCars();
}

