package com.ga2230.dashboard.graphics;

import com.ga2230.dashboard.communications.BroadcastConnection;
import com.ga2230.dashboard.communications.Connection;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class JIndicator extends JLabel {

    private long lastTime = 0;

    public JIndicator(BroadcastConnection connection) {
        setHorizontalAlignment(JLabel.CENTER);
        setHorizontalTextPosition(JLabel.CENTER);
        setFont(getFont().deriveFont(Font.BOLD, 20f));

        connection.register(new Connection.Callback() {
            @Override
            public void callback(boolean finished, String result) {
                lastTime = System.currentTimeMillis();
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ((System.currentTimeMillis() - lastTime) > 5 * 1000) {
                    setStatus(false);
                } else {
                    setStatus(true);
                }
            }
        }, 1000, 1000);
    }

    private void setStatus(boolean connected) {
        setText(connected ? "Connected" : "Disconnected");
        setBackground(connected ? Color.GREEN : Color.RED);
    }

}
