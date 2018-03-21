package businesslogic;

import exceptions.MySQLException;
import model.Worker;

import java.sql.*;
import java.time.LocalDateTime;

class Jdbc {

    private Connection connection = null;

    void loadDriver() throws MySQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            throw new MySQLException(e.getMessage());
        }
    }

    void establishConnection() throws MySQLException {
        try {
            String password = "S@tis1c";
            String username = "Ether";
            String ip = "84.114.18.45:3307";
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + "/ether" + "?user=" + username + "&password=" + password + "&useSSL=false");
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println("could not establish connection");
            throw new MySQLException("Could not establish connection");
        }
    }

    void releaseConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void insert(Worker worker) {
        if (worker != null) {
            String sql = "INSERT INTO ether(name, avg, current, valid, stale, timest) VALUES(?, ?, ?, ?, ?, ?)";

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, worker.getWorker());
                preparedStatement.setDouble(2, worker.getAverageHashrate().doubleValue());
                preparedStatement.setDouble(3, worker.getCurrentHashrate().doubleValue());
                preparedStatement.setInt(4, worker.getValidShares());
                preparedStatement.setInt(5, worker.getStaleShares());
                preparedStatement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}