import com.github.lgooddatepicker.components.DateTimePicker;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DataManager {
    protected final Connection connection;

    DataManager(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<String> getTableNames() throws SQLException {
        ArrayList<String> arrayList = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT table_name FROM information_schema.table_privileges WHERE table_schema = 'public' AND privilege_type = 'SELECT'");
        while (rs.next()) {
            arrayList.add(rs.getString(1));
        }
        arrayList = removeDuplicates(arrayList);
        return arrayList;
    }

    public boolean hasPrivilege(String tableName, String privilege) throws SQLException {
        ArrayList<String> arrayList = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT table_name FROM information_schema.table_privileges " +
                "WHERE table_schema = 'public' AND privilege_type = '" + privilege + "'");
        while (rs.next()) {
            arrayList.add(rs.getString(1));
        }
        arrayList = removeDuplicates(arrayList);
        return arrayList.contains(tableName);
    }

    private static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    public int getColumnCount(String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM \"" + tableName + "\"");
        ResultSetMetaData metadata = rs.getMetaData();
        return metadata.getColumnCount();
    }

    public ArrayList<String> getTableRowNames(String tableName) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM \"" + tableName + "\"");
            ResultSetMetaData metadata = rs.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                String name = metadata.getColumnName(i);
                arrayList.add("\"" + name + "\"");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return arrayList;
    }

    public ArrayList<String> getTableRowTypes(String tableName) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM \"" + tableName + "\"");
            ResultSetMetaData metadata = rs.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                String name = metadata.getColumnTypeName(i);
                arrayList.add(name);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return arrayList;
    }

    public void insertInto(String tableName, String rowNames, String data) {
        String query = "INSERT INTO \"" + tableName + "\"" + rowNames + " VALUES " + data;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean isView(String tableName) {
        try {
            ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[]{"VIEW"});
            while (resultSet.next()) {
                if (tableName.equals(resultSet.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public void update(String tableName, String id, String rowNames, String data) {
        String query = "UPDATE \"" + tableName + "\"" + " SET " + rowNames + " = " + data;
        if (!isView(tableName))
            query += " WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void deleteFrom(String tableName, String id) {
        String query = "DELETE FROM \"" + tableName + "\"" + " WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public ArrayList<String[]> getData(String tableName) throws SQLException {
        ArrayList<String[]> data = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM \"" + tableName + "\"");
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getString(i + 1);
            }
            data.add(row);
        }
        return data;
    }

    public ArrayList<String[]> getSortedData(String tableName) throws SQLException {
        ArrayList<String[]> data = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM \"" + tableName + "\"");
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getString(i + 1);
            }
            data.add(row);
        }
        data.sort(Comparator.comparingInt(s -> Integer.parseInt(s[0])));
        return data;
    }

    public static boolean exportDatabase(String fileName, boolean dataOnly) {
        String dir = "C:\\Users\\Илья\\Desktop\\backup\\" + fileName + ".sql";
        if (dataOnly)
            dir = "C:\\Users\\Илья\\Desktop\\backup\\prev_states\\" + fileName + ".sql";
        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Program Files\\PostgreSQL\\14\\bin\\pg_dump.exe",
                "--host", "localhost",
                "--port", "5432",
                "--username", "postgres",
                "--no-password",
                "--format", "c",
                "--file", dir, "ServiceCenter");
        try {
            final Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", "Dat@b@s$P@s$384293498!1232@");
            Process p = pb.start();
            final BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getErrorStream()));
            String line = r.readLine();
            while (line != null) {
                System.err.println(line);
                line = r.readLine();
            }
            r.close();
            p.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean importDatabase(String fileName, boolean dataOnly) {
        String dir = "C:\\Users\\Илья\\Desktop\\backup\\" + fileName;
        if (dataOnly)
            dir = "C:\\Users\\Илья\\Desktop\\backup\\prev_states\\" + fileName + ".sql";
        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Program Files\\PostgreSQL\\14\\bin\\pg_restore.exe",
                "--host", "localhost",
                "--port", "5432",
                "--username", "postgres",
                "--dbname", "ServiceCenter",
                "--no-password",
                "--clean",
                dir);
        try {
            final Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", "Dat@b@s$P@s$384293498!1232@");
            Process p = pb.start();
            final BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getErrorStream(), "Windows-1251"));
            String line = r.readLine();
            while (line != null) {
                System.err.println(line);
                line = r.readLine();
            }
            r.close();
            p.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static File getLastModified(String directoryFilePath) {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() > lastModifiedTime) {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }

}
