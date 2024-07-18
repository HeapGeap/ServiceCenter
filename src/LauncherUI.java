

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class LauncherUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LauncherUI() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icons/icon.png")));
        setMinimumSize(new Dimension(450, 225));
        getRootPane().setDefaultButton(buttonOK);
        errorLabel.setForeground(Color.RED);
        setTitle("Service Center Database");
        setLocationRelativeTo(null);
        setContentPane(contentPane);
        setVisible(true);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ServiceCenter",
                    usernameField.getText(), String.valueOf(passwordField.getPassword()));
            if (connection != null) {
                new DatabaseUI(connection);
                dispose();
            } else {
                errorLabel.setText("Ошибка");
            }
        } catch (Exception e) {
            errorLabel.setText("Неверное имя пользователя или пароль");
            e.printStackTrace();
        }
    }

    private void onCancel() {
        dispose();
    }

}