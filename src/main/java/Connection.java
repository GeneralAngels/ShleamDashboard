import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Connection {

    private static final int PORT = 5800;

    // Pending command queue
    private Queue<String> commandQueue;
    private Queue<Callback> callbackQueue;

    // I/O section
    private BufferedWriter writer;
    private BufferedReader reader;
    private Socket socket;

    private boolean loop = true;
    private int teamNumber;
    private double refreshRate;

    public Connection(int teamNumber, double refreshRate) {
        this.commandQueue = new ArrayDeque<>();
        this.callbackQueue = new ArrayDeque<>();
        this.teamNumber = teamNumber;
        this.refreshRate = refreshRate;
    }

    public void connect() throws IOException {
        // Determine the address
        String address = "10." + (teamNumber / 100) + "." + (teamNumber % 100) + ".2";
        // Create a new socket
        socket = new Socket();
        // Open the socket
        socket.connect(new InetSocketAddress(address, PORT), 1000);
        // Connect I/O
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Make sure we listen to the first command
        loop = true;
        // Start the loop
        if (refreshRate > 0) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        loop();
                    } catch (IOException e) {
                        Dashboard.setStatus(false, e.toString());
                    }
                }
            }, 0, (long) (1000.0 / refreshRate));
        }
    }

    public void send(String command, Callback callback) {
        commandQueue.add(command);
        callbackQueue.add(callback);
    }

    private void loop() throws IOException {
        if (!commandQueue.isEmpty() || !loop) {
            if (loop) {
                writer.write(commandQueue.remove());
                writer.newLine();
                writer.flush();
            } else {
                // Read result
                String result = reader.readLine();
                // Split result
                String[] split = result.split(":", 2);
                // Call callback
                Callback callback = callbackQueue.remove();
                if (callback != null)
                    callback.callback(Boolean.parseBoolean(split[0]), split[1]);
            }
            // Switch handler
            loop = !loop;
        }
    }

    public interface Callback {
        void callback(boolean finished, String result);
    }
}
