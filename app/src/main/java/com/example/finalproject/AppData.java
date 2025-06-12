package com.example.finalproject;

public class AppData {  // класс сохранение состояния активированности ключа к якартам
    private static AppData instance;
    private Boolean globalVariable;

    private AppData() {
        // Приватный конструктор
        globalVariable = false;
    }

    /**
     *
     * @return
     */
    public static AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    /**
     *
     * @return
     */
    public Boolean getGlobalVariable() {
        return globalVariable;
    }

    /**
     *
     * @param value
     */
    public void setGlobalVariable(Boolean value) {
        this.globalVariable = value;
    }
}
