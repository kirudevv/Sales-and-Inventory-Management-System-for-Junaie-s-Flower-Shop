package com.gplsadgroup.ui;

import com.gplsadgroup.model.User;
import com.gplsadgroup.service.AuthService;

import javax.swing.*;
import java.util.Optional;

public class LoginDialog {

    private final AuthService authService;

    public LoginDialog(AuthService authService) {
        this.authService = authService;
    }

    public Optional<User> showDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        JFrame dummyFrame = new JFrame();
        dummyFrame.setAlwaysOnTop(true);
        dummyFrame.setLocationRelativeTo(null);

        int option = JOptionPane.showConfirmDialog(
                dummyFrame,
                message,
                "Junaie's Flower Shop - Staff Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        dummyFrame.dispose();

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            Optional<User> userOpt = authService.authenticate(username, password);

            if (userOpt.isPresent()) {
                JOptionPane.showMessageDialog(null, "Welcome, " + userOpt.get().getFullName() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                return userOpt;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return showDialog();
            }
        }

        return Optional.empty();
    }
}