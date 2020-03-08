package com.ga2230.dashboard.graphics;

import com.ga2230.dashboard.communications.Connection;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class JScriptButton extends JButton implements ActionListener, Connection.Callback {

    private Connection connection;

    private int teamNumber;
    private JFrame frame;

    public JScriptButton(Connection connection, int teamNumber, JFrame frame) {
        this.connection = connection;
        this.teamNumber = teamNumber;
        this.frame = frame;
        // Set button text
        setText("Upload new script");
        // Add action listener
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // Choose a file
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Shleam Script", "shleam", String.valueOf(teamNumber)));
        chooser.setDialogType(JFileChooser.FILES_ONLY);
        chooser.showDialog(frame, "Upload");
        // Make sure chosen file isn't null
        File file = chooser.getSelectedFile();
        if (file != null) {
            // Try reading the file
            try {
                // Read the file
                List<String> strings = Files.readAllLines(file.toPath());
                // Rebuild the file
                StringBuilder builder = new StringBuilder();
                for (String s : strings) {
                    if (builder.length() > 0)
                        builder.append("\n");
                    builder.append(s);
                }
                // Encode the file
                String command = "runtime load " + builder.toString();
                String base64 = new String(Base64.getEncoder().encode(command.getBytes()));
                // Upload the file
                connection.send(new Connection.Command("base64:" + base64, this));
            } catch (IOException e) {
                updateStatus(false, e.toString());
            }
        } else {
            updateStatus(false, "You must choose a file");
        }
    }

    @Override
    public void callback(boolean finished, String result) {
        updateStatus(finished, new String(Base64.getDecoder().decode(result.getBytes())));
        // Make sure we do not execute the command again
        if (connection.getConnectionType() == Connection.ConnectionType.PeriodicExecution)
            connection.clear();
    }

    private void updateStatus(boolean finished, String result) {
        JOptionPane.showMessageDialog(frame, result, "Status update", finished ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

}
