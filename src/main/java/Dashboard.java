import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

public class Dashboard {

    private static JFrame frame;
    private static JPanel panel;
    private static JTextPane label;
    private static JButton upload, on, off;

    private static Connection connection;

    public static void main(String[] arguments) {
        // Prompt for team number
        int number = Integer.parseInt(JOptionPane.showInputDialog("Enter your team number:"));
        // Make sure the number is valid
        if (number >= 10000)
            return;
        // Initialize frame
        frame = new JFrame("Shleam Dashboard");

        // Initialize panel and buttons
        panel = new JPanel();
        label = new JTextPane();
        upload = new JButton("Upload autonomous script");
        on = new JButton("Drive robot");
        off = new JButton("Stop robot");

        label.setEditable(false);

        panel.setLayout(new GridLayout(4, 1));
        panel.add(label);
        panel.add(upload);
        panel.add(on);
        panel.add(off);

        frame.setContentPane(panel);

        // Initialize connection
        connection = new Connection(number, 10);

        // Connect
        try {
            connection.connect();
        } catch (IOException e) {
            setStatus(false, e.toString());
        }

        // Create onclicks
        upload.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Choose a file
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Shleam Script", "shleam"));
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
                        connection.send("base64:" + base64, new Connection.Callback() {
                            @Override
                            public void callback(boolean finished, String result) {
                                setStatus(finished, new String(Base64.getDecoder().decode(result.getBytes())));
                            }
                        });
                    } catch (IOException e) {
                        setStatus(false, e.toString());
                    }
                }else{
                    setStatus(false, "You must choose a file");
                }
            }
        });

        on.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                connection.send("drive power 0.5:0.5", new Connection.Callback() {
                    @Override
                    public void callback(boolean finished, String result) {
                        setStatus(finished, result);
                    }
                });
            }
        });

        off.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                connection.send("drive power 0:0", new Connection.Callback() {
                    @Override
                    public void callback(boolean finished, String result) {
                        setStatus(finished, result);
                    }
                });
            }
        });

        // Show frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 300);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void setStatus(boolean success, String result) {
        label.setForeground(success ? Color.GREEN : Color.RED);
        label.setText(result);
    }

}
