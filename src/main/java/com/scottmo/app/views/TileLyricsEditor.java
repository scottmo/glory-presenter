package com.scottmo.app.views;

import com.scottmo.app.Labels;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Pair;

import java.util.List;

public class TileLyricsEditor extends GridPane {
    private final TextField titleInput;
    private final VBox lyricsContainer;
    private VerseEditor selectedVerse;

    public TileLyricsEditor(String title, List<Pair<String, String>> verses) {
        super();

        setLayoutProperties();

        addLabel(Labels.LABEL_SONG_TITLE, 0, 0);
        titleInput = new TextField();
        {
            titleInput.setPrefHeight(25);
            setColumnSpan(titleInput, 2);
            setHgrow(titleInput, Priority.ALWAYS);
            add(titleInput, 1, 0);

            titleInput.setText(title);
        }

        addLabel(Labels.LABEL_SONG_LYRICS, 0, 1);

        lyricsContainer = new VBox();
        {
            // make scrollable
            ScrollPane lyricsScrollPane = new ScrollPane();
            lyricsScrollPane.setContent(lyricsContainer);
            add(lyricsScrollPane, 1, 1);
            setVgrow(lyricsScrollPane, Priority.ALWAYS);
            setHgrow(lyricsScrollPane, Priority.ALWAYS);

            for (Pair<String, String> verse : verses) {
                createVerseInput(verse.getKey(), verse.getValue(), false);
            }
        }

        VBox actionsBox = new VBox();
        {
            actionsBox.setSpacing(5);
            add(actionsBox, 2, 1);
            setValignment(actionsBox, VPos.TOP);

            Button addButton = new Button(Labels.BTN_ADD_VERSE);
            addButton.setOnAction(this::onAddVerse);
            addButton.setPrefWidth(100);
            Button editButton = new Button(Labels.BTN_EDIT_VERSE);
            editButton.setOnAction(this::onEditVerse);
            editButton.setPrefWidth(100);
            Button deleteButton = new Button(Labels.BTN_DELETE_VERSE);
            deleteButton.setOnAction(this::onDeleteVerse);
            deleteButton.setPrefWidth(100);

            actionsBox.getChildren().addAll(addButton, editButton, deleteButton);
        }
    }

    public String getTitle() {
        return titleInput.getText();
    }

    public List<Pair<String, String>> getVerses() {
        return lyricsContainer.getChildren().stream().map(node -> {
            VerseEditor verseEditor = (VerseEditor)node;
            return new Pair<>(verseEditor.getVerseName(), verseEditor.getVerseText());
        }).toList();
    }

    private void addLabel(String text, int columnIndex, int rowIndex) {
        Label label = new Label(text);
        label.setFont(new Font(14));
        add(label, columnIndex, rowIndex);
        setValignment(label, VPos.TOP);
    }

    private void setLayoutProperties() {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(90);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setMinWidth(650);
        ColumnConstraints col3 = new ColumnConstraints();
        getColumnConstraints().addAll(col1, col2, col3);

        setPadding(new Insets(10));
        setHgap(10);
        setVgap(10);
    }

    private void createVerseInput(String verseName, String verseText, boolean isEditable) {
        VerseEditor verseEditor = new VerseEditor(verseName, verseText);
        verseEditor.setOnFocus(() -> {
            selectedVerse = verseEditor;
        });
        lyricsContainer.getChildren().add(verseEditor);
        verseEditor.setEditable(isEditable);
        verseEditor.setMaxHeight(150);
        if (isEditable) {
            selectedVerse = verseEditor;
        }
    }

    private void onAddVerse(ActionEvent event) {
        lyricsContainer.getChildren().forEach(node -> {
            ((VerseEditor)node).setEditable(false);
        });
        createVerseInput("new", "", true);
    }

    private void onEditVerse(ActionEvent event) {
        lyricsContainer.getChildren().forEach(node -> {
            ((VerseEditor)node).setEditable(false);
        });
        if (selectedVerse != null) {
            selectedVerse.setEditable(true);
        }
    }

    private void onDeleteVerse(ActionEvent event) {
        this.lyricsContainer.getChildren().remove(selectedVerse);
        selectedVerse = null;
    }
}
