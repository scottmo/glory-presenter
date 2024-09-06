package com.scottmo.ui.components;

import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.glue;
import static org.httprpc.sierra.UIBuilder.row;
import static org.httprpc.sierra.UIBuilder.strut;
import static com.scottmo.config.Config.UI_GAP;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.scottmo.config.ConfigService;
import com.scottmo.core.songs.api.song.Song;

public class SongEditor extends JPanel {
    private ConfigService configService = ConfigService.get();

    private ActionListener listener;

    private JButton buttonSave = new JButton(configService.getLabel("songs.editor.buttonSave"));
    private JButton buttonCancel = new JButton(configService.getLabel("songs.editor.buttonCancel"));

    public SongEditor() {
        Consumer<JLabel> labelStyle = label -> label.setAlignmentX(1.0f);
        Consumer<JTextField> textFieldStyle = textField -> textField.setAlignmentX(0.0f);

        var form = new JScrollPane(column(4, true,
            row(true,
                cell(new JLabel("First Name")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Last Name")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Street Address")).with(labelStyle),
                cell(new JTextField(null, 24)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("City")).with(labelStyle),
                cell(new JTextField(null, 16)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("State")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Postal Code")).with(labelStyle),
                cell(new JTextField(null, 8)).with(textFieldStyle)
            ),

            cell(new JSeparator()),

            row(true,
                cell(new JLabel("Email Address")).with(labelStyle),
                cell(new JTextField(null, 16)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Home Phone")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Mobile Phone")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Fax")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),

            cell(new JSeparator()),

            row(true,
                cell(new JLabel("Field 1")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Field 2")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Field 3")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),
            row(true,
                cell(new JLabel("Field 4")).with(labelStyle),
                cell(new JTextField(null, 12)).with(textFieldStyle)
            ),

            cell(new JSeparator()),

            row(
                cell(new JLabel("Notes")).with(labelStyle).with(label -> label.setAlignmentY(0.0f)),
                cell(new JScrollPane(new JTextArea(4, 20)))
            )
        ).with(viewportView -> viewportView.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
        form.setBorder(null);

        var footer = row(UI_GAP,
            glue(), // fill left spacer to align right
            cell(buttonSave),
            cell(buttonCancel), strut(UI_GAP)
        ).getComponent();

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(form).weightBy(1.0),
            cell(new JSeparator()),
            cell(footer),
            strut(UI_GAP)
        ).getComponent());
    }

    public interface ActionListener {
        void onSave(Song song);
        void onCancel();
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }
}
