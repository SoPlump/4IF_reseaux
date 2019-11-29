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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserFactory {
    private static final Logger LOGGER = LogManager.getLogger(UserFactory.class);

    public List<User> createUsersFromXML(File file) {
        List<User> userList = new ArrayList<>();
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();
            NodeList users = doc.getElementsByTagName("user");

            for (int i = 0; i < users.getLength(); ++i) {
                Node user = users.item(i);
                if (user.getNodeName() == "user") {
                    Element element = (Element) user;
                    userList.add(
                            new User(
                                    element.getElementsByTagName("username").item(0).getTextContent().replace("\n", "").replace("\r", "").replace(" ", ""),
                                    element.getElementsByTagName("password").item(0).getTextContent().replace("\n", "").replace("\r", "").replace(" ", "")
                            ));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return userList;
    }

    public void addUserToXML(User userToAdd, File file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            Element users = doc.getDocumentElement();

            Element username = doc.createElement("username");
            Element password = doc.createElement("password");
            Element user = doc.createElement("user");

            username.appendChild(doc.createTextNode(userToAdd.getUsername()));
            password.appendChild(doc.createTextNode(userToAdd.getPassword()));
            user.appendChild(username);
            user.appendChild(password);

            users.appendChild(user);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}
