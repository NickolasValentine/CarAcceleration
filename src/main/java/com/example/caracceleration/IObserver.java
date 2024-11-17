package com.example.caracceleration;

public interface IObserver {
    void update(int passingCars, int numberOfCars);
    short getSubStatus();
}
