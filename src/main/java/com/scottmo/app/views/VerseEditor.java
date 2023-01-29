package com.scottmo.app.views;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class VerseEditor extends HBox {

    private static final String INACTIVE_INPUT_STYLE = "-fx-background-color: -color-bg-subtle";
    private static final String BORDER_HIGHLIGHT_STYLE = "-fx-border-style: solid inside;"
            + "-fx-border-width: 2;"
            + "-fx-border-color: -color-accent-emphasis;";
    private Runnable onClick;

    private final TextArea verseNameInput;
    private final TextArea verseTextInput;

    public VerseEditor(String verseName, String verseText) {
        super();

        setPadding(new Insets(5, 5, 5, 5));
        setSpacing(2);

        verseNameInput = new TextArea(verseName);
        {
            verseNameInput.setMinWidth(70.0);
            verseNameInput.setMaxWidth(70.0);
            getChildren().add(verseNameInput);
        }
        verseTextInput = new TextArea(verseText);
        {
            HBox.setHgrow(verseTextInput, Priority.ALWAYS);
            verseTextInput.setMinHeight(100);
            getChildren().add(verseTextInput);
        }

        ChangeListener<Boolean> onFocusChange = (obs, oldVal, isFocused) -> {
            if (!verseNameInput.focusedProperty().getValue() && !verseTextInput.focusedProperty().getValue()) {
                setStyle(null);
            }
        };
        EventHandler<MouseEvent> onMouseClick = e -> {
            setStyle(BORDER_HIGHLIGHT_STYLE);
            onClick.run();
        };
        verseNameInput.setOnMouseClicked(onMouseClick);
        verseNameInput.focusedProperty().addListener(onFocusChange);
        verseTextInput.setOnMouseClicked(onMouseClick);
        verseTextInput.focusedProperty().addListener(onFocusChange);
    }

    public void setOnFocus(Runnable onClick) {
        this.onClick = onClick;
    }

    public String getVerseName() {
        return verseNameInput.getText().trim();
    }

    public String getVerseText() {
        return verseTextInput.getText().trim();
    }

    public void setEditable(boolean isEditable) {
        verseNameInput.setEditable(isEditable);
        verseNameInput.setStyle(isEditable ? null : INACTIVE_INPUT_STYLE);

        verseTextInput.setEditable(isEditable);
        verseTextInput.setStyle(isEditable ? null : INACTIVE_INPUT_STYLE);

        if (isEditable) {
            setStyle(BORDER_HIGHLIGHT_STYLE);
            verseTextInput.requestFocus();
        }
    }
}
