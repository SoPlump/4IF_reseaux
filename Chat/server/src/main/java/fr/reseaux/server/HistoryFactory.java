package fr.reseaux.server;

import fr.reseaux.common.Message;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryFactory {

    public void addMessageToStory(Message message, File file) {
        try {
            if (message.getContent().startsWith("/")) return;
            System.out.println(message);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.append(message.toString() + '\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vector<Message> loadStory(File file) {
        try {
            Vector<Message> messageList = new Vector<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            Pattern messagePattern = Pattern.compile("([0-9a-zA-Z]+?) : (.*)");
            Matcher messageMatcher;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                messageMatcher = messagePattern.matcher(line);
                if (messageMatcher.matches()) {
                    System.out.println(line);
                    Message message = new Message(messageMatcher.group(2), messageMatcher.group(1));
                    messageList.add(message);
                }
            }
            System.out.println(messageList);
            return messageList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
