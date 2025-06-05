package com.example.finalproject;

public class AppData {
    private static AppData instance;
    private Boolean globalVariable;

    private AppData() {
        // Приватный конструктор
        globalVariable = false;
    }

    public static AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    public Boolean getGlobalVariable() {
        return globalVariable;
    }

    public void setGlobalVariable(Boolean value) {
        this.globalVariable = value;
    }
}
