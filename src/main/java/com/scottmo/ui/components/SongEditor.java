package com.scottmo.ui.components;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.glue;
import static org.httprpc.sierra.UIBuilder.row;
import static org.httprpc.sierra.UIBuilder.strut;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.scottmo.config.ConfigService;
import com.scottmo.config.Labels;
import com.scottmo.core.songs.api.song.Song;
import com.scottmo.shared.StringUtils;

public class SongEditor extends JPanel {
    private static final Dimension MIN_SIZE = new Dimension(790, 480);

    private ConfigService configService = ConfigService.get();

    private CancelListener cancelListener;
    private SaveListener saveListener;
    
    private JTextField fieldAuthor = new JTextField(null, 24);
    private JTextField fieldPublisher = new JTextField(null, 24);
    private JTextField fieldCopyright = new JTextField(null, 24);
    private JTextField fieldBook = new JTextField(null, 24);
    private JTextField fieldEntry = new JTextField(null, 12);
    private JTextArea fieldComments = new JTextArea(4, 20);
    private JTextField fieldVerseOrder = new JTextField(null, 24);
    private JTextArea fieldLyrics = new JTextArea(20, 30);

    private JButton buttonSave = new JButton(Labels.get("songs.editor.buttonSave"));
    private JButton buttonCancel = new JButton(Labels.get("songs.editor.buttonCancel"));
    private JButton buttonUpdateVerseOrder = new JButton(Labels.get("songs.editor.buttonUpdateVerseOrder"));

    public SongEditor(Song song) {
        setMinimumSize(MIN_SIZE);

        Consumer<JLabel> labelStyle = label -> label.setAlignmentX(1.0f);
        Consumer<JTextField> textFieldStyle = textField -> textField.setAlignmentX(0.0f);

        if (song != null) {
            fieldAuthor.setText(StringUtils.join(song.getAuthors()));
            fieldPublisher.setText(song.getPublisher());
            fieldCopyright.setText(song.getCopyright());
            fieldBook.setText(song.getSongBook());
            fieldEntry.setText(song.getEntry());
            fieldComments.setText(song.getComments());
            fieldVerseOrder.setText(StringUtils.join(song.getVerseOrder()));
        }

        buttonSave.addActionListener(e -> {
            Song modifiedSong = new Song(song == null ? -1 : song.getId())
                .setAuthors(StringUtils.split(fieldAuthor.getText()))
                .setPublisher(fieldPublisher.getText())
                .setCopyright(fieldCopyright.getText())
                .setSongBook(fieldBook.getText())
                .setEntry(fieldEntry.getText())
                .setComments(fieldComments.getText())
                .setVerseOrder(StringUtils.split(fieldVerseOrder.getText()))
                ;

            if (saveListener != null) saveListener.onSave(modifiedSong);
        });

        buttonCancel.addActionListener(e -> {
            if (cancelListener != null) cancelListener.onCancel();
        });

        var form = row(UI_GAP,
            glue(),
            column(UI_GAP, true,
                row(UI_GAP, true,
                    cell(new JLabel(Labels.get("songs.editor.fieldAuthor"))).with(labelStyle),
                    cell(fieldAuthor).with(textFieldStyle)
                ),
                row(UI_GAP, true,
                    cell(new JLabel(Labels.get("songs.editor.fieldPublisher"))).with(labelStyle),
                    cell(fieldPublisher).with(textFieldStyle)
                ),
                row(UI_GAP, true,
                    cell(new JLabel(Labels.get("songs.editor.fieldCopyright"))).with(labelStyle),
                    cell(fieldCopyright).with(textFieldStyle)
                ),
                row(UI_GAP, true,
                    cell(new JLabel(Labels.get("songs.editor.fieldBook"))).with(labelStyle),
                    cell(fieldBook).with(textFieldStyle)
                ),
                row(UI_GAP, true,
                    cell(new JLabel(Labels.get("songs.editor.fieldEntry"))).with(labelStyle),
                    cell(fieldEntry).with(textFieldStyle)
                ),
                row(UI_GAP, 
                    cell(new JLabel(Labels.get("songs.editor.fieldComments"))).with(labelStyle)
                        .with(label -> label.setAlignmentY(0.0f)),
                    cell(new JScrollPane(fieldComments))
                ),
                row(UI_GAP, true,
                    cell(new JLabel(Labels.get("songs.editor.fieldVerseOrder"))).with(labelStyle),
                    column(UI_GAP,
                        cell(fieldVerseOrder).with(textFieldStyle),
                        cell(buttonUpdateVerseOrder)
                    )
                )
            ),
            strut(UI_GAP),
            column(4,
                column(
                    cell(new JLabel(Labels.get("songs.editor.fieldLyrics"))).with(labelStyle),
                    cell(new JScrollPane(fieldLyrics))
                )
            ),
            glue()
        ).getComponent();

        var footer = row(UI_GAP,
            glue(), // fill left spacer to align right
            cell(buttonSave),
            cell(buttonCancel)
        ).getComponent();

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(form).weightBy(1.0),
            cell(new JSeparator()),
            cell(footer)
        ).with(view -> view.setBorder(new EmptyBorder(UI_GAP, UI_GAP, UI_GAP, UI_GAP))).getComponent());
    }

    public interface CancelListener {
        void onCancel();
    }

    public interface SaveListener {
        void onSave(Song song);
    }

    public SongEditor addCancelListener(CancelListener listener) {
        this.cancelListener = listener;
        return this;
    }

    public SongEditor addSaveListener(SaveListener listener) {
        this.saveListener = listener;
        return this;
    }
}
