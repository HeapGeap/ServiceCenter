

import java.util.ArrayList;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;

public class DataTableModel extends AbstractTableModel {
    private final ArrayList<String> columnNames;
    private final ArrayList<String> columnTypes;
    private final int columnCount;
    private final String tableName;
    private final DataManager dm;
    private ArrayList<String[]> data;

    public DataTableModel(DataManager dm, String tableName) throws SQLException {
        columnCount = dm.getColumnCount(tableName);
        columnNames = dm.getTableRowNames(tableName);
        for (int i = 0; i < columnNames.size(); i++)
            columnNames.set(i, columnNames.get(i).replace("\"", ""));
        columnTypes = dm.getTableRowTypes(tableName);
        data = dm.getData(tableName);
        this.tableName = tableName;
        this.dm = dm;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnTypes.get(columnIndex)) {
            case "serial":
            case "int4":
                return Integer.parseInt(data.get(rowIndex)[columnIndex]);
            case "int8":
                return Long.parseLong(data.get(rowIndex)[columnIndex]);
            case "numeric":
                return Float.parseFloat(data.get(rowIndex)[columnIndex]);
            default:
                return data.get(rowIndex)[columnIndex];
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnTypes.get(columnIndex)) {
            case "serial":
            case "int4":
                return Integer.class;
            case "int8":
                return Long.class;
            case "numeric":
                return Float.class;
            case "varchar":
                return String.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    public String getColumnType(int column) {
        return columnTypes.get(column);
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<String[]> getData() {
        return data;
    }

    public void setData(ArrayList<String[]> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public void refresh() {
        this.data.clear();
        try {
            this.data = dm.getData(tableName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        fireTableDataChanged();
    }

}