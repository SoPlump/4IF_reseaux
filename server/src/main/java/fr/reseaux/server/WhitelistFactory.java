package fr.reseaux.server;

import java.io.*;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

public class WhitelistFactory {


    public boolean addUser(Set<String> whitelist, File file, String username) {
        try {
            if (whitelist.add(username)) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.append(username + '\n');
                writer.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

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
