package fr.reseaux.server;

import fr.reseaux.common.ServerResponse;

import java.io.*;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

public class WhitelistFactory {

    // Adds a user to the whitelist and to the whitelist file
    public ServerResponse addUser(Set<String> whitelist, File file, String username) {
        try {
            if (whitelist.add(username)) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.append(username + '\n');
                writer.close();
                return new ServerResponse(true, "Client successfully added to group.");
            } else {
                return new ServerResponse(false, "User " + username + " already in group");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ServerResponse(false, "User " + username + " couldn't be added.");
        }
    }

    // Loads all users from the whitelist file to the whitelist List
    public Set<String> loadUsers(File file) {
        try {
            Set<String> whitelist = new ConcurrentSkipListSet<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                whitelist.add(line);
            }
            return whitelist;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
