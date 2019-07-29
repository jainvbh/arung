package service;


import model.Measurements;
import model.Metrics;
import model.StatsRequest;
import model.StatsResponse;

import java.util.ArrayList;

public interface MeasurementService {
    void addMeasurement(String timestamp, Metrics metrics);
    ArrayList<Measurements> getMeasurement(String timestamp);
    Metrics deleteMeasurement(String timestamp);
    int updateMeasurement(String timestamp, Metrics metrics);
    int patchMeasurement(String timestamp, Metrics metrics);
    ArrayList<StatsResponse> getMeasurementStatistics(StatsRequest statsRequest);
}