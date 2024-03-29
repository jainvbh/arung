package model;

import java.io.Serializable;



public class Measurements implements Serializable {
    private String timestamp;
    private float temperature;
    private float dewPoint;
    private float precipitation;

    public Measurements(
            String timestamp,
            float temperature,
            float dewPoint,
            float precipitation
    ) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.dewPoint = dewPoint;
        this.precipitation = precipitation;
    }

    public Measurements() {

    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public float getTemperature() {
        return temperature;
    }
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
    public float getDewPoint() {
        return dewPoint;
    }
    public void setDewPoint(float dewPoint) {
        this.dewPoint = dewPoint;
    }
    public float getPrecipitation() {
        return precipitation;
    }
    public void setPrecipitation(float precipitation) {
        this.precipitation = precipitation;
    }

    @Override
    public String toString() {
        return "Measurements [timestamp=" + timestamp + ", temperature=" + temperature + ", dewPoint=" + dewPoint
                + ", precipitation=" + precipitation + "]";
    }
}