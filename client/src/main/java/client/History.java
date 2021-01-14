package client;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class History {
    private Path path;
    private FileWriter writer;

    public void init(String login) throws IOException {
        this.path = Paths.get("history_" + login + ".txt");
        this.writer = new FileWriter(this.path.toString(), true);
    }

    public void writeMessage(String message) throws IOException {
        writer.write(message);
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }

    public List<String> getLastMessages(int n) throws IOException {
        List<String> lastN = new LinkedList<>();
        List<String> allLines = Files.readAllLines(path);

        for (int i = 0; i < Math.min(allLines.size(), n); i++) {
            lastN.add(0, allLines.get(allLines.size() - i - 1));
        }

        return lastN;

    }

}
