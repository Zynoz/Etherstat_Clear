package businesslogic;

import exceptions.MySQLException;
import model.JsonWorker;
import model.Worker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

class Start {

    private ArrayList<JsonWorker> jsonWorkers;
    private ArrayList<Worker> workers;

    private Jdbc jdbc;
    private String minerAddress = "7b1101df6f19c9c6fa5a2b4d2c579aeb52de07b9";

    private Thread thread;

    void init() {
        jsonWorkers = new ArrayList<>();
        workers = new ArrayList<>();
    }

    void start() {
        jdbc = new Jdbc();
        connectDB();
    }

    private void connectDB() {
        try {
            jdbc.loadDriver();
            jdbc.establishConnection();
            startThread();
        } catch (MySQLException e) {
            e.printStackTrace();
        }
    }

    private void disconnectDB() {
        jdbc.releaseConnection();
        thread.stop();
        connectDB();
    }

    private void dbEntry() {
        for (Worker worker : workers) {
            jdbc.insert(worker);
        }
    }

    private void startThread() {
        Runnable runnable = () -> {
            while (true) {
                workers.clear();
                jsonWorkers.clear();
                try {
                    System.out.println(LocalDateTime.now().toString());
                    if (Util.getWorkers(minerAddress) != null) {
                        jsonWorkers.addAll(Util.getWorkers(minerAddress));
                    } else {
                        disconnectDB();
                    }
                } catch (MySQLException e) {
                    System.out.println(e.getMessage());
                    break;
                }
                convertWorkers();
                dbEntry();
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    private void convertWorkers() {
        for (JsonWorker json : jsonWorkers) {
            if (json.getAverageHashrate().doubleValue() > 1000) {
                if (json.getAverageHashrate() != null) {
                    json.setAverageHashrate(json.getAverageHashrate().divide(BigDecimal.valueOf(1000000)));
                }
                if (json.getCurrentHashrate() != null) {
                    json.setCurrentHashrate(json.getCurrentHashrate().divide(BigDecimal.valueOf(1000000)));
                }
                if (json.getReportedHashrate() != null) {
                    json.setReportedHashrate(json.getReportedHashrate().divide(BigDecimal.valueOf(1000000)));
                }
            }
            if (json.getCurrentHashrate() != null) {
                workers.add(new Worker(0, json.getWorker(), json.getReportedHashrate(), json.getCurrentHashrate().setScale(2, RoundingMode.DOWN), json.getValidShares(), json.getInvalidShares(), json.getStaleShares(), json.getAverageHashrate().setScale(2, RoundingMode.DOWN), Timestamp.valueOf(LocalDateTime.now())));
            } else {
                workers.add(new Worker(0, json.getWorker(), json.getReportedHashrate(), json.getCurrentHashrate(), json.getValidShares(), json.getInvalidShares(), json.getStaleShares(), json.getAverageHashrate().setScale(2, RoundingMode.DOWN), Timestamp.valueOf(LocalDateTime.now())));
            }
        }
    }
}