package com.example.caracceleration;

public interface ISubscription {
    void register(IObserver observer);
    void unregister(IObserver observer);
    void notifyObserver();

}

