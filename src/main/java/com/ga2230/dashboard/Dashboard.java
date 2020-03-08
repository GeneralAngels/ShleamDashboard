package com.ga2230.dashboard;

import com.ga2230.dashboard.communications.Communicator;
import com.ga2230.dashboard.communications.Connection;
import com.ga2230.dashboard.graphics.JScriptButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Dashboard {

    private static JFrame frame;
    private static JPanel panel;
    private static JTextPane label;
    private static JScriptButton upload;
    private static JButton on, off;

    private static Connection connection;

    public static void main(String[] arguments) {
        try {
            // Prompt for team number
            int number = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter your team number:", "Team", JOptionPane.INFORMATION_MESSAGE));
            // Make sure the number is valid
            if (number >= 10000)
                return;
            // Configure team number
            Communicator.setTeamNumber(number);
            // Initialize frame
            frame = new JFrame("Shleam Dashboard");
            // Configure frame
            Communicator.setFrame(frame);

            connection = Communicator.openConnection(5, Connection.ConnectionType.QueuedExecution);

            // Initialize panel and buttons
            panel = new JPanel();
            label = new JTextPane();
            upload = new JScriptButton(connection, number, frame);
            on = new JButton("Drive robot");
            off = new JButton("Stop robot");

            label.setEditable(false);

            panel.setLayout(new GridLayout(4, 1));
            panel.add(label);
            panel.add(upload);
            panel.add(on);
            panel.add(off);

            frame.setContentPane(panel);

            on.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    connection.send(new Connection.Command("drive power 0.5:0.5", new Connection.Callback() {
                        @Override
                        public void callback(boolean finished, String result) {
                            setStatus(finished, result);
                        }
                    }));
                }
            });

            off.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    connection.send(new Connection.Command("drive power 0:0", new Connection.Callback() {
                        @Override
                        public void callback(boolean finished, String result) {
                            setStatus(finished, result);
                        }
                    }));
                }
            });

            // Show frame
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(250, 300);
            frame.setResizable(false);
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "An error occurred: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static void setStatus(boolean success, String result) {
        label.setForeground(success ? Color.GREEN : Color.RED);
        label.setText(result);
    }

}
