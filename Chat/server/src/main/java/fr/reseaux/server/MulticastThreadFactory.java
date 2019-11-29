package fr.reseaux.server;

import fr.reseaux.common.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastThreadFactory {

    public Vector<MulticastThread> createGroupsFromXML(File file, int port) throws ParserConfigurationException, IOException, SAXException {
        Vector<MulticastThread> groupList = new Vector<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        doc.getDocumentElement().normalize();
        NodeList groups = doc.getElementsByTagName("group");

        for (int i = 0; i < groups.getLength(); ++i) {
            Node user = groups.item(i);
            if (user.getNodeName().equals("group")) {
                Element element = (Element) user;
                String groupName = element.getElementsByTagName("name").item(0).getTextContent().replace("\n", "").replace("\r", "");

                groupName = groupName.trim();
                groupList.add(
                        new MulticastThread(
                                port,
                                (Inet4Address) Inet4Address.getByName(element.getElementsByTagName("ip").item(0).getTextContent().replace("\n", "").replace("\r", "").replace(" ", "")),
                                groupName
                        )
                );
            }
        }
        return groupList;
    }

    public boolean addGroup(File file, String groupName, String ipAddress, int port, Vector<MulticastThread> groupList) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            Node root = doc.getFirstChild();

            Element newGroup = doc.createElement("group");

            Element mName = doc.createElement("name");
            mName.appendChild(doc.createTextNode(groupName));
            newGroup.appendChild(mName);

            Element mAddress = doc.createElement("ip");
            mAddress.appendChild(doc.createTextNode(ipAddress));
            newGroup.appendChild(mAddress);

            root.appendChild(newGroup);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(file);
            transformer.transform(domSource, streamResult);

            groupList.add(
                    new MulticastThread(port, (Inet4Address)Inet4Address.getByName(ipAddress), groupName)
            );
            return true;

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
