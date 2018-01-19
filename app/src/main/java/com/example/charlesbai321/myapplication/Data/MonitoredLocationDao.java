package com.example.charlesbai321.myapplication.Data;

import java.util.List;

/**
 * Created by charlesbai321 on 18/01/18.
 */

public interface MonitoredLocationDao {
    List<MonitoredLocation> getListOfData();

    MonitoredLocation createNewMonitoredLocation();

    void deleteMonitoredLocationItem(MonitoredLocation place);

    void insertMonitoredLocationItem(MonitoredLocation place);
}
