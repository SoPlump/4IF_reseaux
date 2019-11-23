package fr.reseaux.server;

import fr.reseaux.common.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.event.CellEditorListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserFactory {
    private static final Logger LOGGER = LogManager.getLogger(UserFactory.class);

    public List<User> createUsersFromXML(File file) throws ParserConfigurationException, IOException, SAXException {
        List<User> userList = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        doc.getDocumentElement().normalize();
        NodeList users = doc.getElementsByTagName("user");

        for (int i = 0; i < users.getLength(); ++i) {
            Node user = users.item(i);
            if (user.getNodeName() == "user") {
                Element element = (Element) user;
                userList.add(new User( element.getElementsByTagName("username").item(0).getTextContent(), element.getElementsByTagName("password").item(0).getTextContent()));
            }
        }
        LOGGER.debug(userList.toString());
        return userList;
    }
}
