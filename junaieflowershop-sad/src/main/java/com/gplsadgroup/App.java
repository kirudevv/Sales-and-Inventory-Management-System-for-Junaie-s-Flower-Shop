package com.gplsadgroup;

import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.CategoryRepository;
import com.gplsadgroup.repository.OrderItemRepository;
import com.gplsadgroup.repository.OrderRepository;
import com.gplsadgroup.repository.ProductRepository;
import com.gplsadgroup.repository.SupplierRepository;
import com.gplsadgroup.service.AuthService;
import com.gplsadgroup.ui.LoginDialog;
import com.gplsadgroup.ui.MainFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        new SpringApplicationBuilder(App.class)
                .headless(false)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        authService.initDefaultUser();

        EventQueue.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(authService);
            Optional<User> userOpt = loginDialog.showDialog();

            if (userOpt.isPresent()) {
                MainFrame mainFrame = new MainFrame(
                        categoryRepository,
                        productRepository,
                        orderRepository,
                        orderItemRepository,
                        supplierRepository,
                        authService,
                        userOpt.get()
                );
                mainFrame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}