package com.ga2230.dashboard.communications;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Copyright (c) 2019 General Angels
 * https://github.com/GeneralAngels/RIO20
 */

public abstract class Communicator {

    private static final ArrayList<Connection> connections = new ArrayList<>();

    private static int teamNumber;
    private static JFrame frame;

    private static boolean popupLock = false;

    private static void lockPopup() {
        Communicator.popupLock = true;
    }

    private static void unlockPopup() {
        Communicator.popupLock = false;
    }

    public static void setTeamNumber(int teamNumber) {
        Communicator.teamNumber = teamNumber;
    }

    public static void setFrame(JFrame frame) {
        Communicator.frame = frame;
    }

    public static void reconnect() {
        new Thread(() -> {
            Iterator<Connection> connectionIterator = connections.iterator();
            while (connectionIterator.hasNext()) {
                Connection connection = connectionIterator.next();
                if (!connection.isConnected())
                    connection.open();
            }
            unlockPopup();
        }).start();
    }

    public static void disconnected() {
        if (!Communicator.popupLock) {
            lockPopup();
            int result = JOptionPane.showConfirmDialog(frame, "There has been a connection error, do you want to reconnect?", "Connection error", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                Communicator.reconnect();
            } else {
                unlockPopup();
            }
        }
    }

    public static void registerConnection(Connection connection) {
        connections.add(connection);
    }

    public static Connection openConnection(double refreshRate, Connection.ConnectionType connectionType) {
        Connection connection = new Connection(teamNumber, refreshRate, connectionType);
        connection.open();
        return connection;
    }

    public static BroadcastConnection openBroadcastConnection(double refreshRate) {
        BroadcastConnection broadcastConnection = new BroadcastConnection(teamNumber, refreshRate);
        broadcastConnection.open();
        return broadcastConnection;
    }

}