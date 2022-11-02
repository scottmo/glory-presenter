package com.scottmo.services.openLyrics;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;

class OpenLyricsDeserializer {
    OpenLyrics deserialize(String source)
            throws ParserConfigurationException,
            SAXException,
            IOException {
        OpenLyrics song = new OpenLyrics();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));
        parseProperties(doc, song);
        parseLyrics(doc, song);
        return song;
    }

    private void parseProperties(Document doc, OpenLyrics song) {
        NodeList prop = doc.getElementsByTagName("properties");
        if (prop != null && prop.getLength() > 0) {
            Element properties = (Element) prop.item(0);
            getTitlesProp(properties, song);
            getAuthorsProp(properties, song);
            getCopyrightProp(properties, song);
            getPublisherProp(properties, song);
            getVerseOrderProp(properties, song);
            getCommentsProp(properties, song);
        }
    }

    private void getCommentsProp(Element properties, OpenLyrics song) {
        NodeList commentsNodeList = properties.getElementsByTagName("comments");
        for (int i = 0; i < commentsNodeList.getLength(); i++) {
            song.getProperties().addComment(commentsNodeList.item(i)
                    .getChildNodes().item(0).getTextContent());
        }
    }

    private void getVerseOrderProp(Element properties, OpenLyrics song) {
        NodeList verseOrderNodeList = properties.getElementsByTagName("verseOrder");
        if (verseOrderNodeList.getLength() > 0) {
            song.getProperties().setVerseOrder(Arrays.asList(verseOrderNodeList.item(0)
                    .getChildNodes().item(0).getTextContent().split("\\s+")));
        }
    }

    private void getPublisherProp(Element properties, OpenLyrics song) {
        NodeList publisherNodeList = properties.getElementsByTagName("publisher");
        if (publisherNodeList.getLength() > 0) {
            song.getProperties().setPublisher(publisherNodeList.item(0)
                    .getChildNodes().item(0).getTextContent());
        }
    }

    private void getCopyrightProp(Element properties, OpenLyrics song) {
        NodeList copyrightNodeList = properties.getElementsByTagName("copyright");
        if (copyrightNodeList.getLength() > 0) {
            song.getProperties().setCopyright(copyrightNodeList.item(0)
                    .getChildNodes().item(0).getTextContent());
        }
    }

    private void getAuthorsProp(Element properties, OpenLyrics song) {
        NodeList authors = properties.getElementsByTagName("authors");
        if (authors.item(0) != null) {
            NodeList authorsList = ((Element) authors.item(0)).getElementsByTagName("author");
            for (int i = 0; i < authorsList.getLength(); i++) {
                if((authorsList.item(i)).getChildNodes().item(0)!=null) {
                    song.getProperties().addAuthor(authorsList.item(i)
                            .getChildNodes().item(0).getTextContent());
                }
            }
        } else {
            song.getProperties().addAuthor("Unknown Author");
        }
    }

    private void getTitlesProp(Element properties, OpenLyrics song) {
        NodeList titles = properties.getElementsByTagName("titles");
        if (titles.item(0) != null) {
            NodeList titleList = ((Element) titles.item(0)).getElementsByTagName("title");
            for (int i = 0; i < titleList.getLength(); i++) {
                Element titleNode = (Element) titleList.item(i);
                String lang = titleNode.getAttribute("lang");
                Locale locale;
                if (lang.contains("-")) {
                    locale = new Locale(lang.split("-")[0], lang.split("-")[1]);
                } else if (!lang.equals("")) {
                    locale = new Locale(lang);
                } else {
                    locale = Locale.getDefault();
                }

                song.getProperties().addTitle(locale, titleNode.getChildNodes().item(0).getTextContent());
            }
        } else {
            song.getProperties().addTitle(Locale.getDefault(), "Unknown Title");
        }
    }

    private void parseLyrics(Document doc, OpenLyrics song) {
        NodeList lyricsNodes = doc.getElementsByTagName("lyrics");
        if (lyricsNodes != null && lyricsNodes.getLength() > 0) {
            NodeList verseNodes = lyricsNodes.item(0).getChildNodes();
            for (int i = 0; i < verseNodes.getLength(); i++) {
                if (verseNodes.item(i) instanceof Element) {
                    parseVerse((Element) verseNodes.item(i), song);
                }
            }
        }
    }

    private void parseVerse(Element verseElement, OpenLyrics song) {
        Locale locale = !verseElement.getAttribute("lang").equals("")
                ? new Locale(verseElement.getAttribute("lang"))
                : Locale.getDefault();

        Verse verse = new Verse();
        verse.setName(verseElement.getAttribute("name"));
        Element linesNode = (Element) verseElement.getElementsByTagName("lines").item(0);
        if (linesNode == null) {
            throw new IllegalArgumentException("No lines in the verse.");
        }
        NodeList textNodes = linesNode.getChildNodes();

        String textLine = "";
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textElement = textNodes.item(i);
            if (textElement.getNodeName().equalsIgnoreCase("br")) {
                if (!textLine.isEmpty()) {
                    verse.addLine(textLine);
                }
                textLine = "";
            } else if (textElement.getNodeType() == Node.TEXT_NODE) {
                textLine += textElement.getTextContent();
            }
        }

        if (!textLine.isEmpty()) {
            verse.addLine(textLine);
        }

        song.addVerse(locale, verse);
    }
}
