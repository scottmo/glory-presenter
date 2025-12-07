package com.scottmo.ui.components;

import static com.scottmo.config.Config.UI_GAP;
import static org.httprpc.sierra.UIBuilder.cell;
import static org.httprpc.sierra.UIBuilder.column;
import static org.httprpc.sierra.UIBuilder.row;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.scottmo.config.Labels;
import com.scottmo.ui.utils.Dialog;

public class JsonEditor extends JPanel {
    private static final int INDENT_WIDTH = 20;
    
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path filePath;
    private final JPanel fieldsPanel;
    private final JLabel statusLabel;
    private final Map<String, FieldEntry> fieldMap = new LinkedHashMap<>();
    private Consumer<String> onSave;

    public JsonEditor(Path filePath, String title) {
        this.filePath = filePath;
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize() + 6f));
        
        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        
        statusLabel = new JLabel(" ");
        
        JButton buttonReload = new JButton(Labels.get("jsonEditor.buttonReload"));
        JButton buttonSave = new JButton(Labels.get("jsonEditor.buttonSave"));

        buttonReload.addActionListener(evt -> reloadJson());
        buttonSave.addActionListener(evt -> saveJson());

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        setLayout(new BorderLayout());
        add(column(UI_GAP,
            cell(titleLabel),
            cell(scrollPane).weightBy(1.0),
            cell(statusLabel),
            row(UI_GAP,
                cell(buttonReload),
                cell(buttonSave)
            )
        ).getComponent(), BorderLayout.CENTER);

        reloadJson();
    }

    public void setOnSave(Consumer<String> onSave) {
        this.onSave = onSave;
    }

    private void reloadJson() {
        try {
            String content = Files.readString(filePath);
            Map<String, Object> json = objectMapper.readValue(content, new TypeReference<LinkedHashMap<String, Object>>() {});
            buildFields(json);
            setStatus("Loaded from " + filePath.getFileName(), false);
        } catch (IOException e) {
            setStatus("Failed to load: " + e.getMessage(), true);
        }
    }

    private void buildFields(Map<String, Object> json) {
        fieldsPanel.removeAll();
        fieldMap.clear();
        
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            JPanel fieldPanel = createFieldPanel(entry.getKey(), entry.getValue(), 0, entry.getKey());
            fieldsPanel.add(fieldPanel);
            fieldsPanel.add(Box.createVerticalStrut(UI_GAP));
        }
        
        fieldsPanel.revalidate();
        fieldsPanel.repaint();
    }

    @SuppressWarnings("unchecked")
    private JPanel createFieldPanel(String key, Object value, int indentLevel, String path) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        
        if (indentLevel > 0) {
            panel.setBorder(BorderFactory.createEmptyBorder(0, INDENT_WIDTH, 0, 0));
        }

        if (value instanceof Map) {
            // Nested object - create a titled border group
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            JPanel groupPanel = new JPanel();
            groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
            groupPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), key,
                TitledBorder.LEFT, TitledBorder.TOP
            ));
            groupPanel.setAlignmentX(LEFT_ALIGNMENT);
            
            for (Map.Entry<String, Object> nested : nestedMap.entrySet()) {
                JPanel nestedPanel = createFieldPanel(nested.getKey(), nested.getValue(), indentLevel + 1,
                        path + "." + nested.getKey());
                groupPanel.add(nestedPanel);
            }
            
            panel.add(groupPanel);
        } else if (value instanceof List) {
            // Array - comma separated input
            List<?> list = (List<?>) value;
            String csvValue = String.join(", ", list.stream().map(Object::toString).toList());
            panel.add(createLabeledField(key, csvValue, FieldType.ARRAY, path));
        } else {
            // Simple value (string or number)
            FieldType type = (value instanceof Number) ? FieldType.NUMBER : FieldType.STRING;
            panel.add(createLabeledField(key, String.valueOf(value), type, path));
        }
        
        return panel;
    }

    private JPanel createLabeledField(String key, String value, FieldType type, String path) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(formatLabel(key, type));
        label.setPreferredSize(new java.awt.Dimension(180, 25));
        label.setMinimumSize(new java.awt.Dimension(180, 25));
        label.setMaximumSize(new java.awt.Dimension(180, 25));
        
        JTextField textField = new JTextField(value);
        textField.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 25));
        
        fieldMap.put(path, new FieldEntry(textField, type));
        
        rowPanel.add(label);
        rowPanel.add(Box.createHorizontalStrut(UI_GAP));
        rowPanel.add(textField);
        
        return rowPanel;
    }

    private String formatLabel(String key, FieldType type) {
        return switch (type) {
            case ARRAY -> key + " (comma-sep)";
            default -> key;
        };
    }

    private void saveJson() {
        try {
            // Read original to preserve structure
            String originalContent = Files.readString(filePath);
            Map<String, Object> json = objectMapper.readValue(originalContent, new TypeReference<LinkedHashMap<String, Object>>() {});
            
            // Update values from fields
            updateJsonFromFields(json, "");
            
            // Write back
            String newContent = objectMapper.writeValueAsString(json);
            Files.writeString(filePath, newContent);
            
            setStatus("Saved to " + filePath.getFileName(), false);
            if (onSave != null) {
                onSave.accept(newContent);
            }
            Dialog.info("Configuration saved. Some changes may require app restart.");
        } catch (IOException e) {
            setStatus("Failed to save: " + e.getMessage(), true);
            Dialog.error("Failed to save: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateJsonFromFields(Map<String, Object> json, String parentPath) {
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            String currentPath = parentPath.isEmpty() ? key : parentPath + "." + key;

            if (value instanceof Map) {
                updateJsonFromFields((Map<String, Object>) value, currentPath);
            } else if (fieldMap.containsKey(currentPath)) {
                FieldEntry fieldEntry = fieldMap.get(currentPath);
                String textValue = fieldEntry.field.getText().trim();
                
                json.put(key, switch (fieldEntry.type) {
                    case ARRAY -> parseArray(textValue);
                    case NUMBER -> parseNumber(textValue);
                    case STRING -> textValue;
                });
            }
        }
    }

    private List<String> parseArray(String csv) {
        List<String> result = new ArrayList<>();
        for (String item : csv.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private Object parseNumber(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return value; // Fall back to string if not a valid number
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError 
            ? Color.RED 
            : javax.swing.UIManager.getColor("Label.foreground"));
    }

    private enum FieldType {
        STRING, NUMBER, ARRAY
    }

    private record FieldEntry(JTextField field, FieldType type) {}
}
