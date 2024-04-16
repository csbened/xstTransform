package com.roomex.xmltransform;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.apache.xml.utils.DOMHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XsltFunctions {
    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }
    public static String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
    public static String getBase64(String source) {
        return Base64.getEncoder().encodeToString(source.getBytes());
    }

    /** getPassword
     * Password is Base64(Sha1($Nonce + $Created + Sha1($ClearPassword))
     * Nonce is Base64($MessageID)
     */
    //

    public static String getPassword(String messageId, String created, String password) {
        String composed = getBase64(messageId)+created+Sha1(password);
        return getBase64(Sha1(composed));
    }

    private static String Sha1(String password) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "EXCEPTION "+e.getMessage();
        }
    }
    public static String upperCase(String source) {
        return source.toUpperCase();
    }
    private static int getUnderScoreIndex(String composed) {
        int underscore= composed.indexOf("_");
        if (underscore<0) {
            underscore=0;
        }
        return underscore;
    }
    public static String getChainCode(String composed) {
        return composed.substring(0,getUnderScoreIndex(composed));
    }
    public static String getHotelCode(String composed) {
        return composed.substring(getUnderScoreIndex(composed)+1);
    }
    public static String getNumberOfNights(String start, String end) {
        ZonedDateTime startDate = ZonedDateTime.parse(start);
        ZonedDateTime endDate = ZonedDateTime.parse(end);
        return Long.toString(ChronoUnit.DAYS.between(startDate, endDate));
    }
    public static String getStartDate(String start) {
        ZonedDateTime startDate = ZonedDateTime.parse(start);
        return startDate.toLocalDate().toString();
    }
    private static Node getFirstElement(NodeList nodelist) {
        for(int i=0;i< nodelist.getLength();i++) {
            if (nodelist.item(i).getNodeType()==Node.ELEMENT_NODE) {
                return nodelist.item(i);
            }
        }
        return null;
    }
    public static Map<String, Object> getRoomGroups(Object o) {
        Map<String, Object> retVal = new HashMap<>();
        Map<Integer, Integer> rooms= new HashMap<>();
        retVal.put("children",0);
        retVal.put("rooms", rooms);
        DTMNodeIterator iter = (DTMNodeIterator)o;
        Node roomStayCandidate = iter.nextNode();
        while(roomStayCandidate!=null) {
            String quantity = roomStayCandidate.getAttributes().getNamedItem("Quantity").getNodeValue();
            int roomsCount = Integer.valueOf(quantity);
            NodeList nodelist = getFirstElement(roomStayCandidate.getChildNodes()).getChildNodes();
            for(int i=0;i<nodelist.getLength();i++){
                Node guestCountNode = nodelist.item(i);
                if (guestCountNode.getNodeType()==Node.ELEMENT_NODE) {
                    String ageQualifyingCode = guestCountNode.getAttributes().getNamedItem("AgeQualifyingCode").getNodeValue();
                    String count = guestCountNode.getAttributes().getNamedItem("Count").getNodeValue();
                    int age = Integer.parseInt(ageQualifyingCode);
                    int questCount = Integer.parseInt(count);
                    if (age==10) {
                        Integer adultCount=rooms.get(questCount);
                        if (adultCount==null) {
                            adultCount=0;
                        }
                        rooms.put(questCount, adultCount+roomsCount);
                    } else if (age==8) {
                        retVal.put("children",(int)retVal.get("children")+questCount);
                    }
                }

            }
            roomStayCandidate=iter.nextNode();
        }
        return retVal;
    }
    public static String getNumberOfChildren(Map<String, Object> map) {
        return String.valueOf(map.get("children"));
    }

    public static String getSize(Map<String, Object> map) {
        Map<Integer, Integer> rooms = (Map<Integer, Integer>) map.get("rooms");
        return String.valueOf(rooms.size());
    }

    public static NodeList getList(Map<String, Object> map) {
        Document document = DOMHelper.createDocument();
        Map<Integer, Integer> rooms = (Map<Integer, Integer>) map.get("rooms");
        List<Map.Entry<Integer, Integer>> listRooms = rooms.entrySet().stream()
                .sorted((e1, e2)-> e1.getKey()-e2.getKey()).toList();
        return new NodeList() {
            @Override
            public Node item(int index) {
                Map.Entry<Integer, Integer> entry = listRooms.get(index);
                Node current = document.createElement("room");
                Node numberOfRooms =  document.createElement("numberOfRooms");
                numberOfRooms.setTextContent(String.valueOf(entry.getValue()));
                Node numberOfAdults = document.createElement("numberOfAdults");
                numberOfAdults.setTextContent(String.valueOf(entry.getKey()));
                current.appendChild(numberOfRooms);
                current.appendChild(numberOfAdults);
                return current;
            }

            @Override
            public int getLength() {
                return rooms.size();
            }
        };
    }

    public String toString() {
        return "XsltFunctions";
    }
}
