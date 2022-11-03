package com.scottmo.services.openLyrics;

import org.apache.logging.log4j.util.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

class OpenLyricsSerializer {
    String serialize(OpenLyrics song) {
        try {
            Document doc = createDocument(song);
            return stringifyDocument(doc);
        } catch (ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String stringifyDocument(Document doc) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private Document createDocument(OpenLyrics song) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element songElement = doc.createElement("song");
        songElement.setAttribute("xmlns", "http://openlyrics.info/namespace/2009/song");
        songElement.setAttribute("createdIn", "GloryPresenter");
        songElement.setAttribute("modifiedDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date()));

        songElement.appendChild(getProperties(doc, song));
        songElement.appendChild(getLyrics(doc, song));

        doc.appendChild(songElement);
        return doc;
    }

    private Element getProperties(Document doc, OpenLyrics song) {
        Element properties = doc.createElement("properties");
        properties.appendChild(getTitles(doc, song));
        properties.appendChild(getAuthors(doc, song));
        properties.appendChild(getCopyright(doc, song));
        properties.appendChild(getComments(doc, song));
        properties.appendChild(getVerseOrder(doc, song));
        properties.appendChild(getPublisher(doc, song));

        return properties;
    }

    private Element getPublisher(Document doc, OpenLyrics song) {
        Element publisherElement = doc.createElement("publisher");
        String publisher = song.getProperties().getPublisher();
        if (Strings.isNotEmpty(publisher)) {
            publisherElement.appendChild(doc.createTextNode(publisher));
        }
        return publisherElement;
    }

    private Element getVerseOrder(Document doc, OpenLyrics song) {
        List<String> verseOrderList = song.getProperties().getVerseOrder();
        String verseOrderText = (verseOrderList != null && !verseOrderList.isEmpty())
                ? String.join(" ", verseOrderList)
                : String.join(" ", song.getVerseNames());

        Element verseOrderElement = doc.createElement("verseOrder");
        verseOrderElement.appendChild(doc.createTextNode(verseOrderText.trim()));
        return verseOrderElement;
    }

    private Element getComments(Document doc, OpenLyrics song) {
        Element commentsBlockElement = doc.createElement("comments");
        List<String> comments = song.getProperties().getComments();
        if (comments != null && !comments.isEmpty()) {
            for (String comment : comments) {
                Element commentElement = doc.createElement("comment");
                commentElement.appendChild(doc.createTextNode(comment));
                commentsBlockElement.appendChild(commentElement);
            }
        }
        return commentsBlockElement;
    }

    private Element getCopyright(Document doc, OpenLyrics song) {
        Element copyrightElement = doc.createElement("copyright");
        String copyright = song.getProperties().getCopyright();
        copyright = Strings.isNotEmpty(copyright) ? copyright : "Unknown";
        copyrightElement.appendChild(doc.createTextNode(copyright));
        return copyrightElement;
    }

    private Element getAuthors(Document doc, OpenLyrics song) {
        Element authorsElement = doc.createElement("authors");
        List<String> authors = song.getProperties().getAuthors();
        if (authors != null && !authors.isEmpty()) {
            for (String author : authors) {
                Element authorElement = doc.createElement("author");
                authorElement.appendChild(doc.createTextNode(author));
                authorsElement.appendChild(authorElement);
            }
        }
        return authorsElement;
    }

    private Element getTitles(Document doc, OpenLyrics song) {
        Element titlesElement = doc.createElement("titles");
        List<Locale> titleLocales = song.getProperties().getTitleLocales();
        for (Locale titleLocale : titleLocales) {
            Element titleElement = doc.createElement("title");
            titleElement.setAttribute("lang", titleLocale.getLanguage());
            titleElement.appendChild(doc.createTextNode(song.getProperties().getTitle(titleLocale)));
            titlesElement.appendChild(titleElement);
        }

        return titlesElement;
    }

    private Element getLyrics(Document doc, OpenLyrics song) {
        Element lyrics = doc.createElement("lyrics");

        for (Verse verse : song.getVerses()) {
            Element verseElement = doc.createElement("verse");
            if (verse.getName() != null) {
                verseElement.setAttribute("name", verse.getName());
            }

            Element linesElement = doc.createElement("lines");
            verseElement.appendChild(linesElement);

            for (int i = 0; i < verse.getLines().size(); i++) {
                String line = verse.getLines().get(i);
                linesElement.appendChild(doc.createTextNode(line));

                // Do not <br/> to the last line in the verse
                if (i < verse.getLines().size() - 1) {
                    linesElement.appendChild(doc.createElement("br"));
                }
            }

            lyrics.appendChild(verseElement);
        }
        return lyrics;
    }
}
