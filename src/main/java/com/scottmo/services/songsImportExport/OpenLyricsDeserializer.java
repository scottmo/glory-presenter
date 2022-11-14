package com.scottmo.services.songsImportExport;

import com.scottmo.data.song.Song;
import com.scottmo.data.song.Verse;
import org.apache.logging.log4j.util.Strings;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class OpenLyricsDeserializer {
    Song deserialize(String source)
            throws ParserConfigurationException,
            SAXException,
            IOException {
        Song song = new Song();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(source)));
        parseProperties(doc, song);
        parseLyrics(doc, song);
        return song;
    }

    private void parseProperties(Document doc, Song song) {
        NodeList prop = doc.getElementsByTagName("properties");
        if (prop != null && prop.getLength() > 0) {
            Element properties = (Element) prop.item(0);
            getTitlesProp(properties, song);
            getAuthorsProp(properties, song);
            getCopyrightProp(properties, song);
            getPublisherProp(properties, song);
            getVerseOrderProp(properties, song);
            getCommentsProp(properties, song);
            getSongBookProp(properties, song);
        }
    }

    private String getTextContent(Node node) {
        Node topNode = node.getChildNodes().item(0);
        if (topNode != null && topNode.getTextContent() != null) {
            return topNode.getTextContent();
        }
        return "";
    }

    private void getSongBookProp(Element properties, Song song) {
        NodeList songbooks = properties.getElementsByTagName("songbooks");
        if (songbooks.item(0) != null) {
            NodeList songbookList = ((Element) songbooks.item(0)).getElementsByTagName("songbook");
            for (int i = 0; i < songbookList.getLength(); i++) {
                Element songbookNode = (Element) songbookList.item(i);
                String songbook = songbookNode.getAttribute("name");
                String entry = songbookNode.getAttribute("entry");
                if (Strings.isNotEmpty(songbook) && Strings.isNotEmpty(entry)) {
                    song.setSongBook(songbook);
                    song.setEntry(entry);
                    break; // only handle one songbook for now
                }
            }
        }
    }

    private void getCommentsProp(Element properties, Song song) {
        NodeList commentsNodeList = properties.getElementsByTagName("comments");
        List<String> comments = new ArrayList<>();
        for (int i = 0; i < commentsNodeList.getLength(); i++) {
            comments.add(getTextContent(commentsNodeList.item(i)));
        }
        song.setComments(String.join("\n", comments));
    }

    private void getVerseOrderProp(Element properties, Song song) {
        NodeList verseOrderNodeList = properties.getElementsByTagName("verseOrder");
        if (verseOrderNodeList.getLength() > 0) {
            song.setVerseOrder(Arrays.asList(getTextContent(verseOrderNodeList.item(0)).split("\\s+")));
        }
    }

    private void getPublisherProp(Element properties, Song song) {
        NodeList publisherNodeList = properties.getElementsByTagName("publisher");
        if (publisherNodeList.getLength() > 0) {
            song.setPublisher(getTextContent(publisherNodeList.item(0)));
        }
    }

    private void getCopyrightProp(Element properties, Song song) {
        NodeList copyrightNodeList = properties.getElementsByTagName("copyright");
        if (copyrightNodeList.getLength() > 0) {
            song.setCopyright(getTextContent(copyrightNodeList.item(0)));
        }
    }

    private void getAuthorsProp(Element properties, Song song) {
        NodeList authors = properties.getElementsByTagName("authors");
        if (authors.item(0) != null) {
            NodeList authorsList = ((Element) authors.item(0)).getElementsByTagName("author");
            for (int i = 0; i < authorsList.getLength(); i++) {
                String author = getTextContent(authorsList.item(i));
                if (!Strings.isEmpty(author)) {
                    song.addAuthor(author);
                }
            }
        }
    }

    private void getTitlesProp(Element properties, Song song) {
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

                song.setTitle(locale, getTextContent(titleNode));
            }
        } else {
            song.setTitle(Locale.getDefault(), "Unknown Title");
        }
    }

    private void parseLyrics(Document doc, Song song) {
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

    private void parseVerse(Element verseElement, Song song) {
        Locale locale = Strings.isNotEmpty(verseElement.getAttribute("lang"))
                ? new Locale(verseElement.getAttribute("lang"))
                : Locale.getDefault();

        Element linesNode = (Element) verseElement.getElementsByTagName("lines").item(0);
        if (linesNode == null) {
            throw new IllegalArgumentException("No lines in the verse.");
        }
        NodeList textNodes = linesNode.getChildNodes();

        List<String> verseLines = new ArrayList<>();
        String textLine = "";
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textElement = textNodes.item(i);
            if (textElement.getNodeName().equalsIgnoreCase("br")) {
                if (!textLine.isEmpty()) {
                    verseLines.add(textLine.trim());
                }
                textLine = "";
            } else if (textElement.getNodeType() == Node.TEXT_NODE) {
                textLine += textElement.getTextContent();
            }
        }

        if (!textLine.isEmpty()) {
            verseLines.add(textLine.trim());
        }

        Verse verse = new Verse();
        verse.setName(verseElement.getAttribute("name"));
        verse.setText(String.join("\n", verseLines));
        song.addVerse(locale, verse);
    }
}
