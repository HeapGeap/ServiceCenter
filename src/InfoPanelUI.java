

import com.intellij.uiDesigner.core.Spacer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.github.lgooddatepicker.components.DatePicker;

import java.awt.*;
import javax.swing.*;
import java.util.Arrays;
import java.util.Currency;
import java.util.ArrayList;
import java.time.LocalDate;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

public class InfoPanelUI extends JPanel {
    private final DataManager dm;
    private final DataTableModel dtm;
    private final ArrayList<Component> components;
    public final JButton saveBtn, changeBtn, deleteBtn;

    public InfoPanelUI(JTable table, DataManager dm) throws SQLException {
        this.dm = dm;
        dtm = (DataTableModel) table.getModel();
        components = new ArrayList<>(dtm.getColumnCount());

        setLayout(new GridLayoutManager(dtm.getColumnCount() + 3, 2, new Insets(0, 5, 5, 5), -1, -1));

        //title label
        JLabel title = new JLabel(dtm.getTableName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, new GridConstraints(0, 0, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //create and add components
        for (int i = 0; i < dtm.getColumnCount(); i++) {
            addFieldNameLabel(dtm.getColumnName(i), i);
            components.add(addComponent(dtm.getColumnType(i), i));
        }

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        //action panel spacer
        actionPanel.add(new Spacer(), new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));
        //save button
        saveBtn = new JButton();
        saveBtn.setText("Сохранить");
        saveBtn.setVisible(false);
        actionPanel.add(saveBtn, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        //change button
        changeBtn = new JButton();
        changeBtn.setEnabled(true);
        changeBtn.setText("Изменить");
        actionPanel.add(changeBtn, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        //delete button
        deleteBtn = new JButton();
        deleteBtn.setText("Удалить");
        actionPanel.add(deleteBtn, new GridConstraints(0, 3, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        add(actionPanel, new GridConstraints(dtm.getColumnCount() + 1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        add(new Spacer(), new GridConstraints(dtm.getColumnCount() + 2, 0, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null));
    }

    private void addFieldNameLabel(String text, int row) {
        JLabel label = new JLabel();
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setText(text);
        add(label, new GridConstraints(row + 1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
    }

    private Component addComponent(String type, int column) throws SQLException {
        Component component;
        switch (type) {
            case "serial":
                component = new JLabel("0");
                break;
            case "varchar":
                component = new JTextField();
                break;
            case "int8":
                try {
                    component = new JFormattedTextField(new MaskFormatter("# (###) ###-##-##"));
                } catch (ParseException e) {
                    component = new JLabel("phone format error");
                    e.printStackTrace();
                }
                break;
            case "int4":
                if (dm.getTableNames().contains(dtm.getColumnName(column))) {
                    component = new JComboBox<ComboItem>();
                    break;
                }
                NumberFormatter int4Formatter = new NumberFormatter(NumberFormat.getInstance());
                int4Formatter.setValueClass(Integer.class);
                int4Formatter.setMinimum(0);
                int4Formatter.setMaximum(Integer.MAX_VALUE);
                int4Formatter.setAllowsInvalid(false);
                component = new JFormattedTextField(int4Formatter);
                break;
            case "numeric":
                NumberFormat format = DecimalFormat.getInstance();
                format.setCurrency(Currency.getInstance("RUB"));
                format.setMaximumFractionDigits(2);
                format.setMinimumFractionDigits(2);
                NumberFormatter numericFormatter = new NumberFormatter(format);
                numericFormatter.setMinimum(0.00f);
                numericFormatter.setAllowsInvalid(false);
                component = new JFormattedTextField(numericFormatter);
                break;
            case "bool":
                component = new JCheckBox();
                break;
            case "date":
                component = new DatePicker();
                break;
            default:
                component = new JLabel("error type");
                break;
        }
        add(component, new GridConstraints(column + 1, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        return component;
    }

    private String getDataFromComponent(Component component) {
        switch (component.getClass().getSimpleName()) {
            case "JFormattedTextField":
                JFormattedTextField formattedTextField = (JFormattedTextField) component;
                return formattedValue(formattedTextField).replaceAll("[-() ]", "");
            case "JComboBox" :
                JComboBox<ComboItem> comboBox = (JComboBox<ComboItem>) component;
                ComboItem comboItem = (ComboItem) comboBox.getSelectedItem();
                return comboItem.getValue();
            case "JTextField":
                JTextField textField = (JTextField) component;
                return textField.getText();
            case "JCheckBox":
                JCheckBox checkBox = (JCheckBox) component;
                return Boolean.toString(checkBox.isSelected());
            case "DatePicker":
                DatePicker datePicker = (DatePicker) component;
                return datePicker.getDate().toString();
            case "JLabel":
                JLabel label = (JLabel) component;
                return label.getText();
            default:
                return component.getClass().getSimpleName();
        }
    }

    private String formattedValue(JFormattedTextField textField) {
        try {
            Object value = textField.getFormatter().stringToValue(textField.getText());
            if (value instanceof Integer) {
                return Integer.toString((Integer) value);
            }
            if (value instanceof Float) {
                return Float.toString((Float) value);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return textField.getText();
    }

    private void setDataToComponent(Component component, String data, int column) throws SQLException {
        switch (component.getClass().getSimpleName()) {
            case "JFormattedTextField":
                JFormattedTextField formattedTextField = (JFormattedTextField) component;
                if (!(formattedTextField.getFormatter() instanceof MaskFormatter)) {
                    try {
                        formattedTextField.setValue(formattedTextField.getFormatter().stringToValue(data.replace('.', ',')));
                        break;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                formattedTextField.setText(data);
                break;
            case "JComboBox" :
                JComboBox<ComboItem> comboBox = (JComboBox<ComboItem>) component;
                comboBox.removeAllItems();
                for (String[] line : dm.getSortedData(dtm.getColumnName(column))) {
                    String value = line[0];
                    String key = Arrays.toString(line);
                    ComboItem comboItem = new ComboItem(key, value);
                    comboBox.addItem(comboItem);
                    if (data.equals(value)) {
                        comboBox.setSelectedItem(comboItem);
                    }
                }
                break;
            case "JTextField":
                JTextField textField = (JTextField) component;
                textField.setText(data);
                break;
            case "JCheckBox":
                JCheckBox checkBox = (JCheckBox) component;
                checkBox.setSelected(data.equals("t"));
                break;
            case "DatePicker":
                DatePicker datePicker = (DatePicker) component;
                datePicker.setDate(LocalDate.parse(data));
                break;
            case "JLabel":
                JLabel label = (JLabel) component;
                label.setText(data);
                break;
        }
    }

    public void showData(int row) {
        for (int i = 0; i < components.size(); i++) {
            try {
                setDataToComponent(components.get(i), String.valueOf(dtm.getValueAt(row, i)), i);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void clearAllFields() {
        for (Component component : components) {
            switch (component.getClass().getSimpleName()) {
                case "JTextField":
                    JTextField textField = (JTextField) component;
                    textField.setText("");
                    break;
                case "JLabel":
                    JLabel label = (JLabel) component;
                    label.setText("0");
                    break;
                case "JFormattedTextField":
                    JFormattedTextField formattedTextField = (JFormattedTextField) component;
                    formattedTextField.setValue(null);
                    break;
                case "JCheckBox":
                    JCheckBox checkBox = (JCheckBox) component;
                    checkBox.setSelected(false);
                    break;
                case "DatePicker":
                    DatePicker datePicker = (DatePicker) component;
                    datePicker.clear();
                    break;
            }
        }
    }

    public void insert(String tableName, DataManager dm) {
        ArrayList<String> rowNames = dm.getTableRowNames(tableName);
        if(!dm.isView(tableName)) {
            rowNames.remove(0);
        }
        ArrayList<String> info = new ArrayList<>();
        for (Component component : components) {
            info.add("'" + getDataFromComponent(component) + "'");
        }
        if(!dm.isView(tableName)) {
            info.remove(0);
        }
        String rows = rowNames.toString().replace('[', '(').replace(']', ')');
        String data = info.toString().replace('[', '(').replace(']', ')');
        dm.insertInto(tableName, rows, data);
    }

    public void update(String tableName, DataManager dm) {
        ArrayList<String> rowNames = dm.getTableRowNames(tableName);
        String id = null;
        ArrayList<String> info = new ArrayList<>();
        for (Component component : components) {
            info.add("'" + getDataFromComponent(component) + "'");
        }

        if (rowNames.contains("\"id\"")) {
            rowNames.remove(0);
            id = info.remove(0);
        }

        String rows = rowNames.toString();
        String data = info.toString();
        if (rowNames.size() != 1) {
            rows = rows.replace('[', '(').replace(']', ')');
            data = data.replace('[', '(').replace(']', ')');
        } else {
            rows = rows.substring(1, rows.length() - 1);
            data = data.substring(1, data.length() - 1);
        }
        dm.update(tableName, id, rows, data);
    }



}
