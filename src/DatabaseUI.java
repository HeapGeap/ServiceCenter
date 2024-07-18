import com.github.lgooddatepicker.components.DateTimePicker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.awt.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DatabaseUI extends JFrame {
    private JPanel contentPane;
    private JSplitPane splitPane;
    private JTabbedPane tabbedPane;
    private JScrollPane infoScrollPane;
    private JLabel usernameLabel;
    private JButton exitBtn;
    private JButton backBtn;
    private JButton uploadBtn;
    private JButton downloadBtn;
    private DateTimePicker dtPicker;
    private int lastModified = 0;

    ImageIcon icon1 = new ImageIcon("C:\\Users\\Илья\\Desktop\\NewDbProject\\src\\icons\\1.png");
    ImageIcon icon2 = new ImageIcon("C:\\Users\\Илья\\Desktop\\NewDbProject\\src\\icons\\2.png");
    ImageIcon icon3 = new ImageIcon("C:\\Users\\Илья\\Desktop\\NewDbProject\\src\\icons\\3.png");
    ImageIcon icon4 = new ImageIcon("C:\\Users\\Илья\\Desktop\\NewDbProject\\src\\icons\\4.png");

    public DatabaseUI(Connection connection) throws SQLException {
        super("Service Center Database");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icons/icon.png")));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setContentPane(contentPane);
        usernameLabel.setText(usernameLabel.getText() + connection.getMetaData().getUserName());
        downloadBtn.setIcon(icon1);
        exitBtn.setIcon(icon2);
        backBtn.setIcon(icon3);
        uploadBtn.setIcon(icon4);


        DataManager dm = new DataManager(connection);
        ArrayList<TablePanelUI> tablePanels = new ArrayList<>();
        ArrayList<InfoPanelUI> infoPanels = new ArrayList<>();

        try {
            Path path = Paths.get("C:\\Users\\Илья\\Desktop\\backup\\prev_states\\");
            if (Files.notExists(path))
                Files.createDirectory(path);
            File file = DataManager.getLastModified(path.toString());
            if (file != null) {
                String cleanName = file.getName().substring(0, file.getName().lastIndexOf("."));
                lastModified = Integer.parseInt(cleanName);
            }
        } catch (IOException | NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }

        boolean admin = connection.getMetaData().getUserName().equals("postgres");
        if (!admin) {
            backBtn.setEnabled(false);
            uploadBtn.setEnabled(false);
            downloadBtn.setEnabled(false);
        } else if (lastModified == 0)
            backBtn.setEnabled(false);

        for (String tableName : dm.getTableNames()) {
            TablePanelUI tablePanel = new TablePanelUI();
            tablePanel.dataTable.setModel(new DataTableModel(dm, tableName));
            tablePanel.dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tablePanel.dataTable.setAutoCreateRowSorter(true);
            tablePanel.dataTable.getRowSorter().toggleSortOrder(0);

            tabbedPane.add(tableName, tablePanel.contentPane);
            InfoPanelUI infoPanelUI = new InfoPanelUI(tablePanel.dataTable, dm);
            infoPanels.add(infoPanelUI);
            tablePanels.add(tablePanel);

            if (!dm.hasPrivilege(tableName, "INSERT")) {
                tablePanel.addBtn.setEnabled(false);
            }
            if (!dm.hasPrivilege(tableName, "UPDATE")) {
                infoPanelUI.changeBtn.setEnabled(false);
            }
            if (!dm.hasPrivilege(tableName, "DELETE")) {
                infoPanelUI.deleteBtn.setEnabled(false);
            }


            tablePanel.dataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent event) {
                    if (!event.getValueIsAdjusting() && tablePanel.dataTable.getSelectedRow() != -1) {
                        InfoPanelUI infoPanelUI = infoPanels.get(tabbedPane.getSelectedIndex());
                        infoScrollPane.setViewportView(infoPanelUI);
                        infoPanelUI.clearAllFields();
                        infoPanelUI.showData(tablePanel.dataTable.convertRowIndexToModel(tablePanel.dataTable.getSelectedRow()));
                        infoPanelUI.changeBtn.setVisible(true);
                        infoPanelUI.deleteBtn.setVisible(true);
                        infoPanelUI.saveBtn.setVisible(false);
                    }
                }
            });

            tablePanel.addBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (TablePanelUI tablePanel : tablePanels) {
                        tablePanel.dataTable.clearSelection();
                        infoPanelUI.showData(0);
                    }
                    InfoPanelUI infoPanelUI = infoPanels.get(tabbedPane.getSelectedIndex());
                    infoPanelUI.clearAllFields();
                    infoScrollPane.setViewportView(infoPanelUI);
                    infoPanelUI.changeBtn.setVisible(false);
                    infoPanelUI.deleteBtn.setVisible(false);
                    infoPanelUI.saveBtn.setVisible(true);
                }
            });

            tablePanel.findBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String search = tablePanels.get(tabbedPane.getSelectedIndex()).searchField.getText();
                    DataTableModel dtm = (DataTableModel) tablePanels.get(tabbedPane.getSelectedIndex()).dataTable.getModel();
                    dtm.refresh();
                    ArrayList<String[]> data = (ArrayList<String[]>) dtm.getData().clone();
                    ArrayList<String[]> dtmData = dtm.getData();
                    for (String[] dataRow : dtmData) {
                        String row = Arrays.toString(dataRow);
                        if (!row.contains(search)) {
                            data.remove(dataRow);
                        }
                    }
                    dtm.setData(data);
                }
            });

            infoPanelUI.saveBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InfoPanelUI infoPanelUI = (InfoPanelUI) infoScrollPane.getViewport().getView();
                    tabbedPane.setSelectedIndex(infoPanels.indexOf(infoPanelUI));
                    DataTableModel dtm = (DataTableModel) tablePanels.get(tabbedPane.getSelectedIndex()).dataTable.getModel();
                    DataManager.exportDatabase(String.valueOf(++lastModified), true);
                    if (admin)
                        backBtn.setEnabled(true);
                    infoPanelUI.insert(dtm.getTableName(), dm);
                    dtm.refresh();
                    for (TablePanelUI tablePanel : tablePanels) {
                        DataTableModel tableModel = (DataTableModel) tablePanel.dataTable.getModel();
                        tableModel.refresh();
                    }
                }
            });

            infoPanelUI.changeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InfoPanelUI infoPanelUI = (InfoPanelUI) infoScrollPane.getViewport().getView();
                    tabbedPane.setSelectedIndex(infoPanels.indexOf(infoPanelUI));
                    JTable table = tablePanels.get(tabbedPane.getSelectedIndex()).dataTable;
                    DataTableModel dtm = (DataTableModel) table.getModel();
                    DataManager.exportDatabase(String.valueOf(++lastModified), true);
                    if (admin)
                        backBtn.setEnabled(true);

                    infoPanelUI.update(dtm.getTableName(), dm);
                    int selectedRow = table.getSelectedRow();
                    dtm.refresh();
                    for (TablePanelUI tablePanel : tablePanels) {
                        DataTableModel tableModel = (DataTableModel) tablePanel.dataTable.getModel();
                        tableModel.refresh();
                    }

                    table.setRowSelectionInterval(selectedRow, selectedRow);
                }
            });

            infoPanelUI.deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InfoPanelUI infoPanelUI = (InfoPanelUI) infoScrollPane.getViewport().getView();
                    tabbedPane.setSelectedIndex(infoPanels.indexOf(infoPanelUI));
                    JTable table = tablePanels.get(tabbedPane.getSelectedIndex()).dataTable;
                    DataTableModel dtm = (DataTableModel) table.getModel();
                    String id = String.valueOf(dtm.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0));
                    DataManager.exportDatabase(String.valueOf(++lastModified), true);
                    if (admin)
                        backBtn.setEnabled(true);
                    dm.deleteFrom(dtm.getTableName(), id);
                    infoScrollPane.setViewportView(null);
                    table.clearSelection();
                    dtm.refresh();
                    for (TablePanelUI tablePanel : tablePanels) {
                        DataTableModel tableModel = (DataTableModel) tablePanel.dataTable.getModel();
                        tableModel.refresh();
                    }
                }
            });

            tabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    tablePanels.get(tabbedPane.getSelectedIndex()).dataTable.clearSelection();
                }
            });
        }

        uploadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
                String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
                if (DataManager.exportDatabase(timestamp, false))
                    JOptionPane.showMessageDialog(getFrame(), "База данных была успешно экспортированна в файл: " + timestamp + ".sql",
                            "Сообщение", JOptionPane.INFORMATION_MESSAGE);
                else
                    JOptionPane.showMessageDialog(getFrame(), "Произошла ошибка", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        downloadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("C:\\Users\\Илья\\Desktop\\backup");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("backup", "sql");
                fileChooser.setFileFilter(filter);
                if (fileChooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (DataManager.importDatabase(file.getName(), false)) {
                        JOptionPane.showMessageDialog(getFrame(), "База данных была успешно импортирована из файла: " + file.getName(),
                                "Сообщение", JOptionPane.INFORMATION_MESSAGE);
                        setVisible(false);
                        try {
                            new DatabaseUI(connection);
                            dispose();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    } else
                        JOptionPane.showMessageDialog(getFrame(), "Произошла ошибка", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String last = String.valueOf(lastModified);
                File file = new File("C:\\Users\\Илья\\backup\\prev_states\\" + last + ".sql");
                if (dtPicker.datePicker.isTextFieldValid() && dtPicker.timePicker.isTextFieldValid() && dtPicker.getDateTimeStrict() != null)
                {
                    try {
                        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        LocalDateTime ldt = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
                        while (dtPicker.getDateTimeStrict().isBefore(ldt))
                        {
                            DataManager.importDatabase(last, true);
                            file.delete();
                            lastModified--;
                            if (lastModified == 0)
                            {
                                backBtn.setEnabled(false);
                                break;
                            }
                            last = String.valueOf(lastModified);
                            file = new File("C:\\Users\\Илья\\Desktop\\backup\\prev_states\\" + last + ".sql");
                            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                            ldt = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    DataManager.importDatabase(last, true);
                    file.delete();
                    lastModified--;
                    if (lastModified == 0)
                        backBtn.setEnabled(false);
                }
                for (TablePanelUI tablePanel : tablePanels) {
                    DataTableModel tableModel = (DataTableModel) tablePanel.dataTable.getModel();
                    tableModel.refresh();
                }
                infoScrollPane.setViewportView(null);
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new LauncherUI();
                dispose();
            }
        });

        splitPane.setDividerLocation(0.5f);
        setVisible(true);
    }

    private JFrame getFrame() {
        return this;
    }

}