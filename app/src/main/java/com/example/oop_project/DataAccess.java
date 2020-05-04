package com.example.oop_project;

import android.content.Context;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DataAccess {
    private Context context;
    private File usersFile;
    private File reservationsFile;
    private File sportsFile;
    private final String usersFileName = "users.xml";
    private final String reservationsFileName = "reservations.xml";
    private final String sportsFileName = "sports.xml";
    private final String adminPassword = "admin";
    public static final String adminName = "admin";

    public DataAccess(Context context) {
        String filesDirPath = context.getFilesDir().getPath();
        this.context = context;
        this.usersFile = new File(filesDirPath, usersFileName);
        this.reservationsFile = new File(filesDirPath, reservationsFileName);
        this.sportsFile = new File(filesDirPath, sportsFileName);

        if (!this.sportsFile.exists()) { // first run, copy default one from assets
            try {
                InputStream in = context.getResources().getAssets().open(sportsFileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String s;

                while ((s = br.readLine()) != null) sb.append(s);

                br.close();
                in.close();
                OutputStreamWriter ows = new OutputStreamWriter(context.openFileOutput(sportsFileName, Context.MODE_PRIVATE));
                ows.write(sb.toString());
                ows.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean validateLogin(String username, String password) {
        if (username.equals(adminName)) return password.equals(adminPassword);

        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(usersFile);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            Element user = getUserElement(root, username, "username");

            if (user != null && user.getElementsByTagName("password").item(0).getTextContent().equals(password)) return true;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void writeChanges(Document doc, String fileName) throws TransformerException, IOException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(context.openFileOutput(fileName, Context.MODE_PRIVATE));
        transformer.transform(source, result);
    }

    private int hallTaken(Element root, String hall, String startTime, String endTime) {
        return hallTaken(root, hall, startTime, endTime, ""); // id "" never matches
    }

    // hallTaken makes special case for ignoring id (useful when editing reservation)

    private int hallTaken(Element root, String hall, String startTime, String endTime, String id) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        NodeList nList = root.getElementsByTagName("reservation");

        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element) node;

            if (el.getElementsByTagName("hall").item(0).getTextContent().equals(hall)
                && !el.getElementsByTagName("id").item(0).getTextContent().equals(id)) {
                try {
                    Date existingStart = df.parse(el.getElementsByTagName("startTime").item(0).getTextContent());
                    Date existingEnd = df.parse(el.getElementsByTagName("endTime").item(0).getTextContent());
                    Date newStart = df.parse(startTime);
                    Date newEnd = df.parse(endTime);

                    if (newStart == null || newEnd == null) return -1;

                    if ((newStart.after(existingStart) && newStart.before(existingEnd))
                            || (newEnd.after(existingStart) && newEnd.before(existingEnd))
                            || (newStart.before(existingStart) && newEnd.after(existingEnd))
                            || newStart.equals(existingStart) || newStart.equals(existingEnd)
                            || newEnd.equals(existingStart) || newEnd.equals(existingEnd)) {
                        return 1;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        return 0;
    }

    public ArrayList<Sport> getSports() {
        ArrayList<Sport> sportsList = new ArrayList<>();

        if (sportsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(sportsFile);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getDocumentElement().getElementsByTagName("sport");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;
                    String name = el.getElementsByTagName("name").item(0).getTextContent();
                    String maxParticipants = el.getElementsByTagName("maxParticipants").item(0).getTextContent();
                    String description = el.getElementsByTagName("description").item(0).getTextContent();
                    Sport sport = new Sport(name, description, Integer.parseInt(maxParticipants));
                    sportsList.add(sport);
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }

        return sportsList;
    }

    private Element getSportElement(Element root, String name) {
        NodeList nList = root.getElementsByTagName("sport");

        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element) node;
            String elName = el.getElementsByTagName("name").item(0).getTextContent();

            if (elName.equals(name)) return el;
        }

        return null;
    }

    public int addSport(Sport sport) {
        if (sportsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(sportsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();

                if (getSportElement(root, sport.getName()) != null) return 1;

                Element newSport = doc.createElement("sport");
                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(sport.getName()));
                newSport.appendChild(name);
                Element maxParticipants = doc.createElement("maxParticipants");
                maxParticipants.appendChild(doc.createTextNode(String.valueOf(sport.getMaxParticipants())));
                newSport.appendChild(maxParticipants);
                Element description = doc.createElement("description");
                description.appendChild(doc.createTextNode(sport.getDescription()));
                newSport.appendChild(description);
                root.appendChild(newSport);
                writeChanges(doc, sportsFileName);
                return 0;
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public boolean removeSport(String name) {
        if (sportsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(sportsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                Element sportElement = getSportElement(root, name);
                root.removeChild(sportElement);
                writeChanges(doc, sportsFileName);
                return true;
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private Element getUserElement(Element root, String value, String field) {
        NodeList nList = root.getElementsByTagName("user");

        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element) node;
            String userId = el.getElementsByTagName("id").item(0).getTextContent();
            String username = el.getElementsByTagName("username").item(0).getTextContent();

            if ((field.equals("id") && userId.equals(value))
                    || (field.equals("username") && username.equals(value))) return el;
        }

        return null;
    }

    public int addUser(User user) {
        if (user.getUsername().equals(adminName)) return 1;

        if (usersFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(usersFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();

                if (getUserElement(root, user.getUsername(), "username") != null) return 1;

                Element newUser = doc.createElement("user");
                Element id = doc.createElement("id");
                id.appendChild(doc.createTextNode(user.getId()));
                newUser.appendChild(id);
                Element name = doc.createElement("username");
                name.appendChild(doc.createTextNode(user.getUsername()));
                newUser.appendChild(name);
                Element password = doc.createElement("password");
                password.appendChild(doc.createTextNode(user.getPassword()));
                newUser.appendChild(password);
                Element email = doc.createElement("email");
                email.appendChild(doc.createTextNode(user.getEmail()));
                newUser.appendChild(email);
                Element phoneNumber = doc.createElement("phoneNumber");
                phoneNumber.appendChild(doc.createTextNode(user.getPhoneNumber()));
                newUser.appendChild(phoneNumber);
                Element info = doc.createElement("info");
                info.appendChild(doc.createTextNode(user.getInfo()));
                newUser.appendChild(info);
                root.appendChild(newUser);
                writeChanges(doc, usersFileName);
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(usersFileName, Context.MODE_PRIVATE));
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(osw);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "users");
                serializer.startTag("","user");
                serializer.startTag("", "id");
                serializer.text(user.getId());
                serializer.endTag("", "id");
                serializer.startTag("", "username");
                serializer.text(user.getUsername());
                serializer.endTag("", "username");
                serializer.startTag("", "password");
                serializer.text(user.getPassword());
                serializer.endTag("", "password");
                serializer.startTag("", "email");
                serializer.text(user.getEmail());
                serializer.endTag("", "email");
                serializer.startTag("", "phoneNumber");
                serializer.text(user.getPhoneNumber());
                serializer.endTag("", "phoneNumber");
                serializer.startTag("", "info");
                serializer.text(user.getInfo());
                serializer.endTag("", "info");
                serializer.endTag("", "user");
                serializer.endTag("", "users");
                serializer.endDocument();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        return 0;
    }

    public int editUser(String id, User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();
        String phoneNumber = user.getPhoneNumber();
        String info = user.getInfo();

        if (username != null && username.equals(adminName)) return 1;

        if (usersFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(usersFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();

                // checks whether the new name is taken

                if (getUserElement(root, username, "username") != null) return 1;

                Element userElement = getUserElement(root, id, "id");

                if (userElement == null) return -1;

                if (username != null) userElement.getElementsByTagName("username").item(0).setTextContent(username);

                userElement.getElementsByTagName("password").item(0).setTextContent(password);
                userElement.getElementsByTagName("email").item(0).setTextContent(email);
                userElement.getElementsByTagName("phoneNumber").item(0).setTextContent(phoneNumber);
                userElement.getElementsByTagName("info").item(0).setTextContent(info);
                writeChanges(doc, usersFileName);
                return 0;
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public User getUser(String value, String field) {
        User user = null;

        if (usersFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(usersFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                Element userElement = getUserElement(root, value, field);

                if (userElement == null) return null;

                String id = userElement.getElementsByTagName("id").item(0).getTextContent();
                String username = userElement.getElementsByTagName("username").item(0).getTextContent();
                String password = userElement.getElementsByTagName("password").item(0).getTextContent();
                String email = userElement.getElementsByTagName("email").item(0).getTextContent();
                String phoneNumber = userElement.getElementsByTagName("phoneNumber").item(0).getTextContent();
                String info = userElement.getElementsByTagName("info").item(0).getTextContent();
                user = new User(id, username, password, email, phoneNumber, info);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }

        return user;
    }

    public boolean removeUser(String username) {
        // first remove all user's reservations and participations

        User user = getUser(username, "username");
        String id = user.getId();
        removeReservationsByField("owner", id);
        removeParticipant(null, id);

        if (usersFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(usersFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                Element userElement = getUserElement(root, username, "username");
                root.removeChild(userElement);
                writeChanges(doc, usersFileName);
                return true;
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public int addReservation(Reservation reservation) {
        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();

                if (hallTaken(root, reservation.getHall(), reservation.getStartTime(), reservation.getEndTime()) != 0) return 1;

                Element newReservation = doc.createElement("reservation");
                Element id = doc.createElement("id");
                id.appendChild(doc.createTextNode(reservation.getId()));
                newReservation.appendChild(id);
                Element hall = doc.createElement("hall");
                hall.appendChild(doc.createTextNode(reservation.getHall()));
                newReservation.appendChild(hall);
                Element owner = doc.createElement("owner");
                owner.appendChild(doc.createTextNode(reservation.getOwner()));
                newReservation.appendChild(owner);
                Element startTime = doc.createElement("startTime");
                startTime.appendChild(doc.createTextNode(reservation.getStartTime()));
                newReservation.appendChild(startTime);
                Element endTime = doc.createElement("endTime");
                endTime.appendChild(doc.createTextNode(reservation.getEndTime()));
                newReservation.appendChild(endTime);
                Element description = doc.createElement("description");
                description.appendChild(doc.createTextNode(reservation.getDescription()));
                newReservation.appendChild(description);
                Element maxParticipants = doc.createElement("maxParticipants");
                maxParticipants.appendChild(doc.createTextNode(String.valueOf(reservation.getMaxParticipants())));
                newReservation.appendChild(maxParticipants);

                if (reservation.getSport() != null) {
                    Element sport = doc.createElement("sport");
                    sport.appendChild(doc.createTextNode(reservation.getSport()));
                    newReservation.appendChild(sport);
                }

                root.appendChild(newReservation);
                writeChanges(doc, reservationsFileName);
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(reservationsFileName, Context.MODE_PRIVATE));
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(osw);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "reservations");
                serializer.startTag("", "reservation");
                serializer.startTag("", "id");
                serializer.text(reservation.getId());
                serializer.endTag("", "id");
                serializer.startTag("", "hall");
                serializer.text(reservation.getHall());
                serializer.endTag("", "hall");
                serializer.startTag("", "owner");
                serializer.text(reservation.getOwner());
                serializer.endTag("", "owner");
                serializer.startTag("", "startTime");
                serializer.text(reservation.getStartTime());
                serializer.endTag("", "startTime");
                serializer.startTag("", "endTime");
                serializer.text(reservation.getEndTime());
                serializer.endTag("", "endTime");
                serializer.startTag("", "description");
                serializer.text(reservation.getDescription());
                serializer.endTag("", "description");
                serializer.startTag("", "maxParticipants");
                serializer.text(String.valueOf(reservation.getMaxParticipants()));
                serializer.endTag("", "maxParticipants");

                if (reservation.getSport() != null) {
                    serializer.startTag("", "sport");
                    serializer.text(reservation.getSport());
                    serializer.endTag("", "sport");
                }

                serializer.endTag("", "reservation");
                serializer.endTag("", "reservations");
                serializer.endDocument();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        return 0;
    }

    public int editReservation(String id, Reservation reservation) {
        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getElementsByTagName("reservation");

                if (hallTaken(root, reservation.getHall(), reservation.getStartTime(), reservation.getEndTime(), reservation.getId()) != 0) return 1;

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;

                    if (el.getElementsByTagName("id").item(0).getTextContent().equals(id)) {
                        el.getElementsByTagName("hall").item(0).setTextContent(reservation.getHall());
                        el.getElementsByTagName("startTime").item(0).setTextContent(reservation.getStartTime());
                        el.getElementsByTagName("endTime").item(0).setTextContent(reservation.getEndTime());
                        el.getElementsByTagName("description").item(0).setTextContent(reservation.getDescription());
                        el.getElementsByTagName("maxParticipants").item(0).setTextContent(String.valueOf(reservation.getMaxParticipants()));

                        if (reservation.getSport() != null) {
                            if (el.getElementsByTagName("sport").getLength() != 0) {
                                el.getElementsByTagName("sport").item(0).setTextContent(reservation.getSport());
                            } else {
                                Element sport = doc.createElement("sport");
                                sport.appendChild(doc.createTextNode(reservation.getSport()));
                                el.appendChild(sport);
                            }
                        } else if (el.getElementsByTagName("sport").getLength() != 0) {
                            el.removeChild(el.getElementsByTagName("sport").item(0));
                        }

                        writeChanges(doc, reservationsFileName);
                        return 0;
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    // note: doesn't support using maxParticipants as field

    public boolean removeReservationsByField(String field, String value) {
        ArrayList<Node> toBeDeleted = new ArrayList<>();

        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getElementsByTagName("reservation");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;

                    if (el.getElementsByTagName(field).item(0).getTextContent().equals(value)) {
                        toBeDeleted.add(node);

                        if (field.equals("id")) break; // ids are unique
                    }
                }

                if (field.equals("id") && toBeDeleted.size() == 0) return false; // failure if didn't find particular reservation

                for (Node n : toBeDeleted) root.removeChild(n);

                writeChanges(doc, reservationsFileName);
                return true;
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public boolean removeReservation(String id) {
        return removeReservationsByField("id", id);
    }

    public ArrayList<Reservation> searchReservation(String text, String where, boolean exact) { // search from all halls
        return searchReservation(text, where, exact, null);
    }

    // note: doesn't support using maxParticipants as where

    public ArrayList<Reservation> searchReservation(String text, String where, boolean exact, String fromHall) {
        ArrayList<Reservation> list = new ArrayList<>();

        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getElementsByTagName("reservation");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    boolean addThis = false;

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;

                    if (fromHall != null && !el.getElementsByTagName("hall").item(0).getTextContent().equals(fromHall)) continue;

                    if (!exact) {
                        if (where.equals("all") && (el.getElementsByTagName("owner").item(0).getTextContent().toLowerCase().contains(text.toLowerCase())
                                || el.getElementsByTagName("hall").item(0).getTextContent().toLowerCase().contains(text.toLowerCase())
                                || el.getElementsByTagName("description").item(0).getTextContent().toLowerCase().contains(text.toLowerCase()))) {
                            addThis = true;
                        } else if (where.equals("participants") || where.equals("all")) {
                            if (el.getElementsByTagName("participants").getLength() == 0) continue;

                            Element participantsElement = (Element) el.getElementsByTagName("participants").item(0);
                            NodeList participantsList = participantsElement.getElementsByTagName("participant");

                            for (int j = 0; j < participantsList.getLength(); j++) {
                                Node participant = participantsList.item(j);

                                if (participant.getNodeType() != Node.ELEMENT_NODE) continue;

                                // participants are searched by username, not id

                                String participantName = getUser(participant.getTextContent(), "id").getUsername();

                                if (participantName.toLowerCase().contains(text.toLowerCase())) {
                                    addThis = true;
                                    break;
                                }
                            }
                        } else if (where.equals("sport")) {
                            if (el.getElementsByTagName("sport").getLength() != 0
                                    && el.getElementsByTagName(where).item(0).getTextContent().toLowerCase().contains(text.toLowerCase())) {
                                addThis = true;
                            }
                        } else if (el.getElementsByTagName(where).item(0).getTextContent().toLowerCase().contains(text.toLowerCase())) {
                            addThis = true;
                        }
                    } else {
                        if (where.equals("all") && (el.getElementsByTagName("owner").item(0).getTextContent().equals(text)
                                || el.getElementsByTagName("hall").item(0).getTextContent().equals(text)
                                || el.getElementsByTagName("description").item(0).getTextContent().equals(text))) {
                            addThis = true;
                        } else if (where.equals("participants") || where.equals("all")) {
                            if (el.getElementsByTagName("participants").getLength() == 0) continue;

                            Element participantsElement = (Element) el.getElementsByTagName("participants").item(0);
                            NodeList participantsList = participantsElement.getElementsByTagName("participant");

                            for (int j = 0; j < participantsList.getLength(); j++) {
                                Node participant = participantsList.item(j);

                                if (participant.getNodeType() != Node.ELEMENT_NODE) continue;

                                // participants are searched by username, not id

                                String participantName = getUser(participant.getTextContent(), "id").getUsername();

                                if (participantName.equals(text)) {
                                    addThis = true;
                                    break;
                                }
                            }
                        } else if (where.equals("sport")) {
                            if (el.getElementsByTagName("sport").getLength() != 0
                                    && el.getElementsByTagName(where).item(0).getTextContent().equals(text)) {
                                addThis = true;
                            }
                        } else if (el.getElementsByTagName(where).item(0).getTextContent().equals(text)) {
                            addThis = true;
                        }
                    }

                    if (addThis) {
                        String id = el.getElementsByTagName("id").item(0).getTextContent();
                        String startTime = el.getElementsByTagName("startTime").item(0).getTextContent();
                        String endTime = el.getElementsByTagName("endTime").item(0).getTextContent();
                        String hall = el.getElementsByTagName("hall").item(0).getTextContent();
                        String owner = el.getElementsByTagName("owner").item(0).getTextContent();
                        String description = el.getElementsByTagName("description").item(0).getTextContent();
                        String maxParticipantsText = el.getElementsByTagName("maxParticipants").item(0).getTextContent();
                        int maxParticipants = Integer.parseInt(maxParticipantsText);

                        if (el.getElementsByTagName("sport").getLength() != 0) {
                            String sport = el.getElementsByTagName("sport").item(0).getTextContent();
                            list.add(new Reservation(id, startTime, endTime, hall, owner, description, maxParticipants, sport));
                        } else {
                            list.add(new Reservation(id, startTime, endTime, hall, owner, description, maxParticipants));
                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public int addParticipant(String reservationId, String userId) {
        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getElementsByTagName("reservation");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;

                    if (el.getElementsByTagName("id").item(0).getTextContent().equals(reservationId)) {
                        String maxParticipantsText = el.getElementsByTagName("maxParticipants").item(0).getTextContent();
                        int maxParticipants;

                        try {
                            maxParticipants = Integer.parseInt(maxParticipantsText);
                        } catch (Exception e) {
                            return -1;
                        }

                        int numberOfParticipantsElements = el.getElementsByTagName("participants").getLength();
                        int numberOfParticipants = 0;

                        if (numberOfParticipantsElements != 0) {
                            Element participantsElement = (Element) el.getElementsByTagName("participants").item(0);
                            numberOfParticipants = participantsElement.getElementsByTagName("participant").getLength();
                        }

                        if (numberOfParticipants == maxParticipants) {
                            return 1;
                        }

                        Element participant = doc.createElement("participant");
                        participant.appendChild(doc.createTextNode(userId));

                        if (numberOfParticipantsElements == 0) {
                            Element participants = doc.createElement("participants");
                            participants.appendChild(participant);
                            el.appendChild(participants);
                        } else {
                            el.getElementsByTagName("participants").item(0).appendChild(participant);
                        }

                        writeChanges(doc, reservationsFileName);
                        return 0;
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public boolean removeParticipant(String reservationId, String userId) {
        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getElementsByTagName("reservation");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;

                    // if id is null, delete this participant everywhere

                    if ((reservationId == null || el.getElementsByTagName("id").item(0).getTextContent().equals(reservationId))
                            && el.getElementsByTagName("participants").getLength() != 0) {
                        Element participantsElement = (Element) el.getElementsByTagName("participants").item(0);
                        NodeList participantsList = participantsElement.getElementsByTagName("participant");

                        for (int j = 0; j < participantsList.getLength(); j++) {
                            Node participant = participantsList.item(j);

                            if (participant.getNodeType() == Node.ELEMENT_NODE && participant.getTextContent().equals(userId)) {
                                participantsElement.removeChild(participant);
                                break;
                            }
                        }
                    }
                }

                writeChanges(doc, reservationsFileName);
                return true;
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public ArrayList<User> getParticipants(String reservationId) {
        ArrayList<User> usersList = new ArrayList<>();

        if (reservationsFile.exists()) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(reservationsFile);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getElementsByTagName("reservation");

                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element el = (Element) node;

                    if (el.getElementsByTagName("id").item(0).getTextContent().equals(reservationId)) {
                        if (el.getElementsByTagName("participants").getLength() != 0) {
                            Element participantsElement = (Element) el.getElementsByTagName("participants").item(0);
                            NodeList participantsList = participantsElement.getElementsByTagName("participant");

                            for (int j = 0; j < participantsList.getLength(); j++) {
                                Node participant = participantsList.item(j);

                                if (participant.getNodeType() != Node.ELEMENT_NODE) continue;

                                String userId = participant.getTextContent();
                                usersList.add(getUser(userId, "id"));
                            }
                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }

        return usersList;
    }
}
