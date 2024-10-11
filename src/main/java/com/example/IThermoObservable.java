package com.example;

public interface IThermoObservable {
    public void attachObserver(ThermoView observer);
    public void detachObserver(ThermoView observer);
    public void notifyObserver();
}