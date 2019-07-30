package hello;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import model.Measurements;
import model.Metrics;
import model.StatsRequest;
import model.StatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.MeasurementService;
import util.WeatherTrackerUtil;

import javax.servlet.http.HttpServletRequest;

@RestController
public class MeasurementController {

    private static final ResponseEntity NOT_IMPLEMENTED = new ResponseEntity(HttpStatus.NOT_IMPLEMENTED);

    @Autowired
    private MeasurementService measurementService;

    //Create Measurement
    @RequestMapping(method = RequestMethod.POST, value = "/measurements", consumes = "application/json")
    public ResponseEntity createMeasurement(@RequestBody JsonNode measurement,
                                            HttpServletRequest httpServletRequest) {

        try {
            LocalDateTime timeStamp = WeatherTrackerUtil.convertStringToLocalDate(measurement.get("timestamp").asText());

            boolean isValidRequest = true;
            if(measurement.get("temperature") != null) {
                isValidRequest = isValidRequest && isFloatCheck(measurement.get("temperature").asText());
            }
            if(measurement.get("dewPoint") != null) {
                isValidRequest = isValidRequest && isFloatCheck(measurement.get("dewPoint").asText());
            }
            if(measurement.get("precipitation") != null) {
                isValidRequest = isValidRequest && isFloatCheck(measurement.get("precipitation").asText());
            }

            if(!isValidRequest) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }

            Metrics metric = new Metrics(
                    convertNullToFloatAdd(measurement.get("temperature")),
                    convertNullToFloatAdd(measurement.get("dewPoint")),
                    convertNullToFloatAdd(measurement.get("precipitation"))
            );

            measurementService.addMeasurement(measurement.get("timestamp").asText(), metric);

        }
        catch (Exception e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

    // features/01-measurements/02-get-measurement.feature
    //GET by timestamp
    @RequestMapping(value = "/measurements/{timestamp}", method = RequestMethod.GET)
    public ResponseEntity getMeasurement(@PathVariable("timestamp") String timestamp) {

        ArrayList<Measurements> measurementList = measurementService.getMeasurement(timestamp);
        System.out.println("Here is the list" + measurementList);

        /* Example 1:
        timestamp := "2015-09-01T16:20:00.000Z"
        return {
            "timestamp": "2015-09-01T16:00:00.000Z",
            "temperature": 27.1,
            "dewPoint": 16.7,
            "precipitation": 0
        }
        *//*

         *//* Example 2:
        timestamp := "2015-09-01"
        return [
            {
                "timestamp": "2015-09-01T16:00:00.000Z",
                "temperature": 27.1,
                "dewPoint": 16.7,
                "precipitation": 0
            },
            {
                "timestamp": "2015-09-01T16:01:00.000Z",
                "temperature": 27.3,
                "dewPoint": 16.9,
                "precipitation": 0
            }
        ]
        */

        ResponseEntity resp = null;
        if(measurementList.size() != 0) {
            resp = new ResponseEntity<Iterable<Measurements>>(measurementList, HttpStatus.OK);
        } else {
            resp = new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return resp;
    }

    // features/01-measurements/03-update-measurement.feature
    //@PUT @Path("/measurements/{timestamp}")
    //REPLACE by time stamp
    @RequestMapping(value = "/measurements/{timestamp}", method = RequestMethod.PUT)
    public ResponseEntity replaceMeasurement(@PathVariable("timestamp") String timestamp, @RequestBody JsonNode measurement) {
        /* Example:
        timestamp := "2015-09-01T16:20:00.000Z"
        measurement := {
            "timestamp": "2015-09-01T16:00:00.000Z",
            "temperature": 27.1,
            "dewPoint": 16.7,
            "precipitation": 0
        }
        */

        int httpStatusCode = 0;
        ResponseEntity resp = null;


        if(isRequestValid(measurement)) {
            String newtimestamp = measurement.get("timestamp").asText();
            if(newtimestamp.equals(timestamp)) {
                Metrics metric = new Metrics(
                        measurement.get("temperature").floatValue(),
                        measurement.get("dewPoint").floatValue(),
                        measurement.get("precipitation").floatValue()
                );

                httpStatusCode = measurementService.updateMeasurement(timestamp, metric);
            } else {
                httpStatusCode = 409;
            }
        } else {
            httpStatusCode = 400;
        }

        HttpStatus httpStatus = getHttpStatus(httpStatusCode);

        return new ResponseEntity(httpStatus);
    }


    //Update API
    @RequestMapping(value = "/measurements/{timestamp}", method = RequestMethod.PATCH)
    public ResponseEntity updateMeasurement(@PathVariable("timestamp") String timestamp, @RequestBody JsonNode measurement) {
        /* Example:
        timestamp := "2015-09-01T16:20:00.000Z"
        measurement := {
            "timestamp": "2015-09-01T16:00:00.000Z",
            "precipitation": 15.2
        }
        */

        int httpStatusCode;
        boolean isValidRequest = true;
        if(measurement.get("temperature") != null) {
            isValidRequest = isValidRequest && isFloatCheck(measurement.get("temperature").asText());
        }
        if(measurement.get("dewPoint") != null) {
            isValidRequest = isValidRequest && isFloatCheck(measurement.get("dewPoint").asText());
        }
        if(measurement.get("precipitation") != null) {
            isValidRequest = isValidRequest && isFloatCheck(measurement.get("precipitation").asText());
        }

        if(isValidRequest) {
            String newtimestamp = measurement.get("timestamp").asText();
            if(newtimestamp.equals(timestamp)) {
                Metrics metric = new Metrics(
                        convertNullToFloat(measurement.get("temperature")),
                        convertNullToFloat(measurement.get("dewPoint")),
                        convertNullToFloat(measurement.get("precipitation"))
                );

                httpStatusCode = measurementService.patchMeasurement(timestamp, metric);
            } else {
                httpStatusCode = 409;
            }
        } else {
            httpStatusCode = 400;
        }

        HttpStatus httpStatus = getHttpStatus(httpStatusCode);

        return new ResponseEntity(httpStatus);
    }

    // features/01-measurements/04-delete-measurement.feature
    //@DELETE @Path("/measurements/{timestamp}")
    @RequestMapping(value = "/measurements/{timestamp}", method = RequestMethod.DELETE)
    public ResponseEntity deleteMeasurement(@PathVariable("timestamp") String timestamp) {
        /* Example:
        timestamp := "2015-09-01T16:20:00.000Z"
        */
        Metrics result = measurementService.deleteMeasurement(timestamp);
        int httpStatusCode = 0;
        if(result != null) {
            httpStatusCode = 204;
        } else {
            httpStatusCode = 404;
        }

        HttpStatus httpStatus = getHttpStatus(httpStatusCode);


        return new ResponseEntity(httpStatus);
    }

    //@GET @Path("/stats")
    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    public ResponseEntity getStats(@RequestParam("metric") List<String> metrics,
                                   @RequestParam("stat") List<String> stats,
                                   @RequestParam("fromDateTime") String fromDateTime,
                                   @RequestParam("toDateTime") String toDateTime
    ) {
        /* Example:
        metrics := [
            "temperature",
            "dewPoint"
        ]
        stats := [
            "min",
            "max"
        ]
        return [
            {
                "metric": "temperature",
                "stat": "min"
                "value": 27.1
            },
            {
                "metric": "temperature",
                "stat": "max"
                "value": 27.5
            },
            {
                "metric": "dewPoint",
                "stat": "min"
                "value": 16.9
            },
            {
                "metric": "dewPoint",
                "stat": "max"
                "value": 17.3
            }
        ]
        */

        System.out.println("To Date Time " + toDateTime);
        LocalDateTime fromDateLt = WeatherTrackerUtil.convertStringToLocalDate(fromDateTime);
        LocalDateTime toDateLt = WeatherTrackerUtil.convertStringToLocalDate(toDateTime);
        StatsRequest request = new StatsRequest(new ArrayList<>(metrics), new ArrayList<>(stats), fromDateLt, toDateLt);
        ArrayList<StatsResponse> response = measurementService.getMeasurementStatistics(request);

        return new ResponseEntity(response, HttpStatus.OK);
    }


    private boolean isFloatCheck(String str) {
        try {
            Float.parseFloat(str);
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    private float convertNullToFloat(JsonNode measurement) {
        if(measurement == null) {
            return Float.MIN_VALUE;
        } else {
            return measurement.floatValue();
        }
    }

    private float convertNullToFloatAdd(JsonNode measurement) {
        if(measurement == null) {
            return 0.0f;
        } else {
            return measurement.floatValue();
        }
    }

    private boolean isRequestValid(JsonNode measurement) {
        return isFloatCheck(measurement.get("temperature").asText()) &&
                isFloatCheck(measurement.get("dewPoint").asText()) &&
                isFloatCheck(measurement.get("precipitation").asText());
    }

    private HttpStatus getHttpStatus(int httpStatusCode) {
        HttpStatus httpStatus = null;
        if (httpStatusCode == 204) {
            httpStatus = HttpStatus.NO_CONTENT;
        } else if (httpStatusCode == 404) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (httpStatusCode == 409) {
            httpStatus = HttpStatus.CONFLICT;
        } else if (httpStatusCode == 400) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return httpStatus;
    }

}
