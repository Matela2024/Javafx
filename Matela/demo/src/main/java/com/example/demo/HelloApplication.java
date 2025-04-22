package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.Optional;

public class HelloApplication extends Application {

    private Connection connection;
    private String currentUserRole = ""; // Admin or Employee
    private TabPane tabPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectDatabase();
        primaryStage.setTitle("🚗 Vehicle Rental System");

        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #ffffff; -fx-padding: 0;");

        Tab vehicleTab = vehicleTab();
        Tab customerTab = customerTab();
        Tab bookingTab = bookingTab();
        Tab paymentTab = paymentTab();
        Tab loginTab = loginTab();
        Tab reportTab = reportTab();

        // Disable tabs initially, only Login is active
        vehicleTab.setDisable(true);
        customerTab.setDisable(true);
        bookingTab.setDisable(true);
        paymentTab.setDisable(true);
        reportTab.setDisable(true);

        tabPane.getTabs().addAll(vehicleTab, customerTab, bookingTab, paymentTab, loginTab, reportTab);

        VBox root = new VBox();
        root.setPadding(new Insets(20));
        // Apply gradient background with shadow effect
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#d6eaff")),
                new Stop(1, Color.web("#a3c9ff")));
        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif;");
        DropShadow shadow = new DropShadow(10, 5, 5, Color.GRAY);
        root.setEffect(shadow);
        root.getChildren().add(tabPane);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectDatabase() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:~/vehiclerental", "sa", "1234");

            if (connection != null) {
                Statement stmt = connection.createStatement();
                // Create tables if they do not exist
                stmt.execute("CREATE TABLE IF NOT EXISTS Vehicle(id INT AUTO_INCREMENT PRIMARY KEY, brand VARCHAR(255), category VARCHAR(100), price DOUBLE, available BOOLEAN)");
                stmt.execute("CREATE TABLE IF NOT EXISTS Customer(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), contact VARCHAR(100), license VARCHAR(100))");
                stmt.execute("CREATE TABLE IF NOT EXISTS Booking(id INT AUTO_INCREMENT PRIMARY KEY, customer_id INT, vehicle_id INT, start_date DATE, end_date DATE, FOREIGN KEY(customer_id) REFERENCES Customer(id), FOREIGN KEY(vehicle_id) REFERENCES Vehicle(id))");
                stmt.execute("CREATE TABLE IF NOT EXISTS Payment(id INT AUTO_INCREMENT PRIMARY KEY, booking_id INT, amount DOUBLE, method VARCHAR(100), FOREIGN KEY(booking_id) REFERENCES Booking(id))");
            } else {
                System.out.println("❌ Failed to connect to the database.");
            }
        } catch (Exception e) {
            System.out.println("❌ Database connection error: " + e.getMessage());
            connection = null;
        }
    }

    private Button createLogoutButton() {
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15; -fx-font-size: 14px; -fx-cursor: hand;");

        // Hover and click effects
        setButtonHoverEffect(logoutBtn);

        // Fade transition for click effect
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), logoutBtn);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.7);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(2);

        logoutBtn.setOnMousePressed(e -> fadeTransition.play());
        logoutBtn.setOnAction(e -> {
            currentUserRole = ""; // Reset the role
            setTabAccess(false, false, false, false, false); // Disable all tabs
            tabPane.getSelectionModel().select(tabPane.getTabs().get(4)); // Switch to login tab
        });

        return logoutBtn;
    }

    private Tab vehicleTab() {
        Tab tab = new Tab("Vehicle Management");
        tab.setStyle("-fx-background-color: #e0e0e0; -fx-background-insets: 0 1 0 1; -fx-background-radius: 5 5 0 0;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Dashboard Elements
        Label dashboardLabel = new Label("🌟 Vehicle Dashboard");
        dashboardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ListView<String> vehicleListView = new ListView<>();
        vehicleListView.setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button refreshVehicleBtn = new Button("Refresh Vehicles");
        refreshVehicleBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        // Vehicle Input Fields with Categories
        TextField brandField = new TextField();
        brandField.setPromptText("Brand & Model");
        brandField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList("Sedan", "SUV", "Hatchback", "Luxury", "Truck"));
        categoryCombo.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField priceField = new TextField();
        priceField.setPromptText("Price per Day");
        priceField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField idField = new TextField();
        idField.setPromptText("Vehicle ID (for update/delete)");
        idField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        // Action Buttons
        Button addBtn = new Button("Add Vehicle");
        addBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button updateBtn = new Button("Update Vehicle");
        updateBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button deleteBtn = new Button("Delete Vehicle");
        deleteBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button logoutBtn = createLogoutButton();

        Label vehicleMessage = new Label("");
        vehicleMessage.setStyle("-fx-font-size: 14px;");

        // Button hover effects
        setButtonHoverEffect(addBtn);
        setButtonHoverEffect(updateBtn);
        setButtonHoverEffect(deleteBtn);
        setButtonHoverEffect(refreshVehicleBtn);

        // Handler for Adding Vehicle
        addBtn.setOnAction(e -> {
            if (connection == null) {
                vehicleMessage.setText("❌ No database connection.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO Vehicle(brand, category, price, available) VALUES (?, ?, ?, true)");
                ps.setString(1, brandField.getText());
                ps.setString(2, categoryCombo.getValue());
                ps.setDouble(3, Double.parseDouble(priceField.getText()));
                ps.execute();

                refreshVehicleList(vehicleListView); // Refresh the list
                vehicleMessage.setText("✅ Vehicle Added Successfully!");
                vehicleMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                clearVehicleFields(brandField, categoryCombo, priceField); // Clear fields after adding
            } catch (Exception ex) {
                vehicleMessage.setText("❌ Error: " + ex.getMessage());
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Handler for Updating Vehicle
        updateBtn.setOnAction(e -> {
            if (connection == null) {
                vehicleMessage.setText("❌ No database connection.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            // Validate inputs
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                vehicleMessage.setText("❌ Please enter a Vehicle ID.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }
            if (brandField.getText().isEmpty() || categoryCombo.getValue() == null || priceField.getText().isEmpty()) {
                vehicleMessage.setText("❌ All fields (Brand, Category, Price) are required.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                int vehicleId = Integer.parseInt(idText);
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    vehicleMessage.setText("❌ Price must be positive.");
                    vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    return;
                }

                PreparedStatement ps = connection.prepareStatement("UPDATE Vehicle SET brand=?, category=?, price=? WHERE id=?");
                ps.setString(1, brandField.getText());
                ps.setString(2, categoryCombo.getValue());
                ps.setDouble(3, price);
                ps.setInt(4, vehicleId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    vehicleMessage.setText("✅ Vehicle Updated Successfully!");
                    vehicleMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    refreshVehicleList(vehicleListView); // Refresh the list
                    clearVehicleFields(brandField, categoryCombo, priceField);
                    idField.clear();
                } else {
                    vehicleMessage.setText("❌ No vehicle found with ID: " + vehicleId);
                    vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                vehicleMessage.setText("❌ Invalid ID or Price format.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } catch (SQLException ex) {
                vehicleMessage.setText("❌ Database error: " + ex.getMessage());
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Handler for Deleting Vehicle
        deleteBtn.setOnAction(e -> {
            if (connection == null) {
                vehicleMessage.setText("❌ No database connection.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            // Validate input
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                vehicleMessage.setText("❌ Please enter a Vehicle ID.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                int vehicleId = Integer.parseInt(idText);
                PreparedStatement ps = connection.prepareStatement("DELETE FROM Vehicle WHERE id=?");
                ps.setInt(1, vehicleId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    vehicleMessage.setText("✅ Vehicle Deleted Successfully!");
                    vehicleMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    refreshVehicleList(vehicleListView);
                    idField.clear();
                } else {
                    vehicleMessage.setText("❌ No vehicle found with ID: " + vehicleId);
                    vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                vehicleMessage.setText("❌ Please enter a valid Vehicle ID.");
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } catch (SQLException ex) {
                vehicleMessage.setText("❌ Database error: " + ex.getMessage());
                vehicleMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Refresh List Action
        refreshVehicleBtn.setOnAction(e -> refreshVehicleList(vehicleListView));

        HBox buttonBox = new HBox(10, addBtn, updateBtn, deleteBtn, logoutBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        vbox.getChildren().addAll(dashboardLabel, refreshVehicleBtn, vehicleListView,
                new Label("Add/Update/Delete Vehicle"), brandField,
                categoryCombo, priceField, idField,
                buttonBox, vehicleMessage);
        tab.setContent(vbox);
        return tab;
    }

    private void setButtonHoverEffect(Button button) {
        String originalColor = button.getText().equals("Logout") ? "#e74c3c" : "#4a90e2";
        String hoverColor = button.getText().equals("Logout") ? "#c0392b" : "#3a7bc8";

        button.setOnMouseEntered(_ -> button.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        button.setOnMouseExited(_ -> button.setStyle("-fx-background-color: " + originalColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15; -fx-cursor: hand;"));
    }

    private void refreshVehicleList(ListView<String> vehicleListView) {
        vehicleListView.getItems().clear();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, brand, category, price FROM Vehicle WHERE available = true");
            while (rs.next()) {
                vehicleListView.getItems().add("ID: " + rs.getInt("id") + ", Brand: " + rs.getString("brand") +
                        ", Category: " + rs.getString("category") + ", Price: $" + rs.getDouble("price"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void clearVehicleFields(TextField brandField, ComboBox<String> categoryCombo, TextField priceField) {
        brandField.clear();
        categoryCombo.setValue(null);
        priceField.clear();
    }

    private Tab customerTab() {
        Tab tab = new Tab("Customer Management");
        tab.setStyle("-fx-background-color: #e0e0e0; -fx-background-insets: 0 1 0 1; -fx-background-radius: 5 5 0 0;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Customer Dashboard
        Label dashboardLabel = new Label("🌟 Customer Dashboard");
        dashboardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ListView<String> customerListView = new ListView<>();
        customerListView.setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button refreshCustomerBtn = new Button("Refresh Customers");
        refreshCustomerBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        // Customer Input Fields
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setStyle("-fx-background-color: lightblue; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        contactField.setStyle("-fx-background-color: lightblue; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField licenseField = new TextField();
        licenseField.setPromptText("License Number");
        licenseField.setStyle("-fx-background-color: lightblue; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField idField = new TextField();
        idField.setPromptText("Customer ID (for update/delete)");
        idField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        // Action Buttons
        Button registerBtn = new Button("Register Customer");
        registerBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button updateBtn = new Button("Update Customer");
        updateBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button deleteBtn = new Button("Delete Customer");
        deleteBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button logoutBtn = createLogoutButton();

        Label customerMessage = new Label("");
        customerMessage.setStyle("-fx-font-size: 14px;");

        // Button hover effects
        setButtonHoverEffect(registerBtn);
        setButtonHoverEffect(updateBtn);
        setButtonHoverEffect(deleteBtn);
        setButtonHoverEffect(refreshCustomerBtn);

        // Handler for Registering Customer
        registerBtn.setOnAction(e -> {
            if (connection == null) {
                customerMessage.setText("❌ No database connection.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO Customer(name, contact, license) VALUES (?, ?, ?)");
                ps.setString(1, nameField.getText());
                ps.setString(2, contactField.getText());
                ps.setString(3, licenseField.getText());
                ps.execute();

                refreshCustomerList(customerListView);
                customerMessage.setText("✅ Customer Registered!");
                customerMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                clearCustomerFields(nameField, contactField, licenseField);
            } catch (Exception ex) {
                customerMessage.setText("❌ Error: " + ex.getMessage());
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Handler for Updating Customer
        updateBtn.setOnAction(e -> {
            if (connection == null) {
                customerMessage.setText("❌ No database connection.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            // Validate inputs
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                customerMessage.setText("❌ Please enter a Customer ID.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }
            if (nameField.getText().isEmpty() || contactField.getText().isEmpty() || licenseField.getText().isEmpty()) {
                customerMessage.setText("❌ All fields (Name, Contact, License) are required.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                int customerId = Integer.parseInt(idText);
                PreparedStatement ps = connection.prepareStatement("UPDATE Customer SET name=?, contact=?, license=? WHERE id=?");
                ps.setString(1, nameField.getText());
                ps.setString(2, contactField.getText());
                ps.setString(3, licenseField.getText());
                ps.setInt(4, customerId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    customerMessage.setText("✅ Customer Updated Successfully!");
                    customerMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    refreshCustomerList(customerListView);
                    clearCustomerFields(nameField, contactField, licenseField);
                    idField.clear();
                } else {
                    customerMessage.setText("❌ No customer found with ID: " + customerId);
                    customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                customerMessage.setText("❌ Invalid Customer ID format.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } catch (SQLException ex) {
                customerMessage.setText("❌ Database error: " + ex.getMessage());
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Handler for Deleting Customer
        deleteBtn.setOnAction(e -> {
            if (connection == null) {
                customerMessage.setText("❌ No database connection.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            // Validate input
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                customerMessage.setText("❌ Please enter a Customer ID.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                int customerId = Integer.parseInt(idText);
                PreparedStatement ps = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
                ps.setInt(1, customerId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    customerMessage.setText("✅ Customer Deleted Successfully!");
                    customerMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    refreshCustomerList(customerListView);
                    idField.clear();
                } else {
                    customerMessage.setText("❌ No customer found with ID: " + customerId);
                    customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            } catch (NumberFormatException ex) {
                customerMessage.setText("❌ Please enter a valid Customer ID.");
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } catch (SQLException ex) {
                customerMessage.setText("❌ Database error: " + ex.getMessage());
                customerMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Refresh List Action
        refreshCustomerBtn.setOnAction(e -> refreshCustomerList(customerListView));

        HBox buttonBox = new HBox(10, registerBtn, updateBtn, deleteBtn, logoutBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        vbox.getChildren().addAll(dashboardLabel, refreshCustomerBtn, customerListView,
                new Label("Register/Update/Delete Customer"), nameField,
                contactField, licenseField, idField,
                buttonBox, customerMessage);
        tab.setContent(vbox);
        return tab;
    }

    private void refreshCustomerList(ListView<String> customerListView) {
        customerListView.getItems().clear();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, contact, license FROM Customer");
            while (rs.next()) {
                customerListView.getItems().add("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") +
                        ", Contact: " + rs.getString("contact") + ", License: " + rs.getString("license"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void clearCustomerFields(TextField nameField, TextField contactField, TextField licenseField) {
        nameField.clear();
        contactField.clear();
        licenseField.clear();
    }

    private Tab bookingTab() {
        Tab tab = new Tab("Booking");
        tab.setStyle("-fx-background-color: #e0e0e0; -fx-background-insets: 0 1 0 1; -fx-background-radius: 5 5 0 0;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");
        customerIdField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField vehicleIdField = new TextField();
        vehicleIdField.setPromptText("Vehicle ID");
        vehicleIdField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField startDateField = new TextField();
        startDateField.setPromptText("Start Date (YYYY-MM-DD)");
        startDateField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField endDateField = new TextField();
        endDateField.setPromptText("End Date (YYYY-MM-DD)");
        endDateField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        Button bookBtn = new Button("Book Vehicle");
        bookBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button logoutBtn = createLogoutButton();

        Label status = new Label("");
        status.setStyle("-fx-font-size: 14px;");

        // Button hover effect
        setButtonHoverEffect(bookBtn);

        bookBtn.setOnAction(e -> {
            if (connection == null) {
                status.setText("❌ No database connection.");
                status.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                int vid = Integer.parseInt(vehicleIdField.getText());
                PreparedStatement check = connection.prepareStatement("SELECT available, price FROM Vehicle WHERE id=?");
                check.setInt(1, vid);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getBoolean("available")) {
                    int customerId = Integer.parseInt(customerIdField.getText());
                    double pricePerDay = rs.getDouble("price");
                    PreparedStatement book = connection.prepareStatement("INSERT INTO Booking(customer_id, vehicle_id, start_date, end_date) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    book.setInt(1, customerId);
                    book.setInt(2, vid);
                    book.setDate(3, Date.valueOf(startDateField.getText()));
                    book.setDate(4, Date.valueOf(endDateField.getText()));
                    book.executeUpdate();

                    // Update vehicle availability
                    PreparedStatement update = connection.prepareStatement("UPDATE Vehicle SET available=false WHERE id=?");
                    update.setInt(1, vid);
                    update.execute();

                    // Generate Payment
                    generatePayment(customerId, vid, pricePerDay, startDateField.getText(), endDateField.getText());

                    status.setText("✅ Booking Successful!");
                    status.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else {
                    status.setText("❌ Vehicle not available.");
                    status.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            } catch (Exception ex) {
                status.setText("❌ Error: " + ex.getMessage());
                status.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        HBox buttonBox = new HBox(10, bookBtn, logoutBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        vbox.getChildren().addAll(new Label("📅 Book Vehicle"), customerIdField, vehicleIdField,
                startDateField, endDateField, buttonBox, status);
        tab.setContent(vbox);
        return tab;
    }

    private void generatePayment(int customerId, int vehicleId, double pricePerDay, String startDate, String endDate) {
        try {
            // Calculate rental duration (in days)
            long duration = getDaysBetween(java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
            var ref = new Object() {
                double totalAmount = duration * pricePerDay;
            };

            // Include additional services and late fees
            double additionalServices = 0; // Assume fetching from user input as needed
            double lateFees = 0; // Assume fetching from user input as needed

            ref.totalAmount += additionalServices + lateFees;

            // Ask for payment method
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Cash", "Cash", "Credit Card", "Online");
            dialog.setTitle("Payment Method");
            dialog.setHeaderText("Choose Payment Method");
            dialog.getDialogPane().setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd;");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(method -> {
                try {
                    PreparedStatement payStmt = connection.prepareStatement("INSERT INTO Payment(booking_id, amount, method) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    payStmt.setInt(1, getLatestBookingId(customerId, vehicleId));
                    payStmt.setDouble(2, ref.totalAmount);
                    payStmt.setString(3, method);
                    payStmt.executeUpdate();

                    // Generate Invoice
                    generateInvoice(customerId, vehicleId, ref.totalAmount, method, startDate, endDate);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getDaysBetween(Date startDate, Date endDate) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private int getLatestBookingId(int customerId, int vehicleId) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) as max_id FROM Booking WHERE customer_id = " + customerId + " AND vehicle_id = " + vehicleId);
        if (rs.next()) {
            return rs.getInt("max_id");
        }
        return -1; // Error or not found
    }

    private void generateInvoice(int customerId, int vehicleId, double totalAmount, String method, String startDate, String endDate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice");
        alert.setHeaderText("Rental Invoice");
        alert.setContentText("Customer ID: " + customerId + "\n" +
                "Vehicle ID: " + vehicleId + "\n" +
                "Rental Duration: " + getDaysBetween(java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)) + " days\n" +
                "Total Amount: $" + new DecimalFormat("#.##").format(totalAmount) + "\n" +
                "Payment Method: " + method);

        alert.getDialogPane().setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd;");
        alert.showAndWait();
    }

    private Tab reportTab() {
        Tab tab = new Tab("Reports");
        tab.setStyle("-fx-background-color: #e0e0e0; -fx-background-insets: 0 1 0 1; -fx-background-radius: 5 5 0 0;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        Button revenueReportBtn = new Button("Generate Revenue Report");
        revenueReportBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button rentalHistoryReportBtn = new Button("Generate Rental History Report");
        rentalHistoryReportBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button logoutBtn = createLogoutButton();

        LineChart<String, Number> revenueChart = createRevenueChart();

        // Button hover effects
        setButtonHoverEffect(revenueReportBtn);
        setButtonHoverEffect(rentalHistoryReportBtn);

        revenueReportBtn.setOnAction(e -> {
            // Logic to generate revenue report
            try {
                Statement stmt = connection.createStatement();
                ResultSet revenueRs = stmt.executeQuery("SELECT SUM(amount) AS total_revenue FROM Payment");
                if (revenueRs.next()) {
                    double totalRevenue = revenueRs.getDouble("total_revenue");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Total Revenue: $" + totalRevenue);
                    alert.getDialogPane().setStyle("-fx-background-color: white; -fx-border-color: #dddddd;");
                    alert.showAndWait();
                }
                // Populate revenue chart with dummy data
                populateRevenueChart(revenueChart);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        rentalHistoryReportBtn.setOnAction(e -> {
            // Logic to generate rental history report
            try {
                Statement stmt = connection.createStatement();
                ResultSet historyRs = stmt.executeQuery("SELECT * FROM Booking");
                StringBuilder history = new StringBuilder("Rental History:\n");
                while (historyRs.next()) {
                    history.append("Booking ID: ").append(historyRs.getInt("id"))
                            .append(", Customer ID: ").append(historyRs.getInt("customer_id"))
                            .append(", Vehicle ID: ").append(historyRs.getInt("vehicle_id"))
                            .append(", Start Date: ").append(historyRs.getDate("start_date"))
                            .append(", End Date: ").append(historyRs.getDate("end_date")).append("\n");
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, history.toString());
                alert.getDialogPane().setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd;");
                alert.showAndWait();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(10, revenueReportBtn, rentalHistoryReportBtn, logoutBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        vbox.getChildren().addAll(buttonBox, revenueChart);
        tab.setContent(vbox);
        return tab;
    }

    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue ($)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Revenue");
        lineChart.setStyle("-fx-background-color: lightblue; -fx-border-color: #dddddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 15;");
        return lineChart;
    }

    private void populateRevenueChart(LineChart<String, Number> chart) {
        // Dummy data for chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("2023 Revenue");
        series.getData().add(new XYChart.Data<>("January", 5000));
        series.getData().add(new XYChart.Data<>("February", 7000));
        series.getData().add(new XYChart.Data<>("March", 8000));
        chart.getData().add(series);
    }

    private Tab paymentTab() {
        Tab tab = new Tab("Payment & Billing");
        tab.setStyle("-fx-background-color: #e0e0e0; -fx-background-insets: 0 1 0 1; -fx-background-radius: 5 5 0 0;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label dashboardLabel = new Label("💰 Payment & Billing Dashboard");
        dashboardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ListView<String> paymentListView = new ListView<>();
        paymentListView.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button refreshPaymentBtn = new Button("Refresh Payments");
        refreshPaymentBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        // Payment Input Fields
        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("Booking ID");
        bookingIdField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        ComboBox<String> paymentMethodCombo = new ComboBox<>(FXCollections.observableArrayList("Cash", "Credit Card", "Online"));
        paymentMethodCombo.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        Button submitPaymentBtn = new Button("Submit Payment");
        submitPaymentBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button logoutBtn = createLogoutButton();

        Label paymentMessage = new Label("");
        paymentMessage.setStyle("-fx-font-size: 14px;");

        // Button hover effects
        setButtonHoverEffect(refreshPaymentBtn);
        setButtonHoverEffect(submitPaymentBtn);

        // Handler for Submitting Payments
        submitPaymentBtn.setOnAction(e -> {
            if (connection == null) {
                paymentMessage.setText("❌ No database connection.");
                paymentMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                return;
            }

            try {
                int bookingId = Integer.parseInt(bookingIdField.getText());
                double amount = Double.parseDouble(amountField.getText());
                String paymentMethod = paymentMethodCombo.getValue();

                PreparedStatement ps = connection.prepareStatement("INSERT INTO Payment(booking_id, amount, method) VALUES (?, ?, ?)");
                ps.setInt(1, bookingId);
                ps.setDouble(2, amount);
                ps.setString(3, paymentMethod);
                ps.executeUpdate();

                paymentMessage.setText("✅ Payment Successful!");
                paymentMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                // Refresh Payment List
                refreshPaymentList(paymentListView);
            } catch (Exception ex) {
                paymentMessage.setText("❌ Error: " + ex.getMessage());
                paymentMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        // Refresh Payments Action
        refreshPaymentBtn.setOnAction(e -> refreshPaymentList(paymentListView));

        HBox buttonBox = new HBox(10, submitPaymentBtn, logoutBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        vbox.getChildren().addAll(dashboardLabel, refreshPaymentBtn, paymentListView,
                new Label("Process Payment"), bookingIdField, amountField, paymentMethodCombo,
                buttonBox, paymentMessage);
        tab.setContent(vbox);
        return tab;
    }

    private void refreshPaymentList(ListView<String> paymentListView) {
        paymentListView.getItems().clear();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, booking_id, amount, method FROM Payment");
            while (rs.next()) {
                paymentListView.getItems().add("Payment ID: " + rs.getInt("id") + ", Booking ID: " + rs.getInt("booking_id") +
                        ", Amount: $" + rs.getDouble("amount") + ", Method: " + rs.getString("method"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Tab loginTab() {
        Tab tab = new Tab("Login");
        tab.setStyle("-fx-background-color: #e0e0e0; -fx-background-insets: 0 1 0 1; -fx-background-radius: 5 5 0 0;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: blue; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");
        vbox.setAlignment(Pos.CENTER);

        // Add logo
        Label logoLabel = new Label("🚗 Rent a Car with affodable Prices");
        logoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        logoLabel.setTextFill(Color.web("#2c3e50"));
        DropShadow logoShadow = new DropShadow(8, 3, 3, Color.GRAY);
        logoLabel.setEffect(logoShadow);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15 8 15;");

        Label loginMsg = new Label("");
        loginMsg.setStyle("-fx-font-size: 14px;");

        // Button hover effect
        setButtonHoverEffect(loginBtn);
        setButtonHoverEffect(logoutBtn);

        loginBtn.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if ("admin".equals(user) && "admin123".equals(pass)) {
                currentUserRole = "admin";
                loginMsg.setText("✅ Admin Login Successful! Role: " + currentUserRole);
                loginMsg.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                setTabAccess(true, true, false, false, true); // Admin: Vehicle, Customer, Reports
                userField.clear();
                passField.clear();
            } else if ("employee".equals(user) && "emp123".equals(pass)) {
                currentUserRole = "employee";
                loginMsg.setText("✅ Employee Login Successful! Role: " + currentUserRole);
                loginMsg.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                setTabAccess(false, false, true, true, false); // Employee: Booking, Payment
                userField.clear();
                passField.clear();
            } else {
                loginMsg.setText("❌ Invalid credentials.");
                loginMsg.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        logoutBtn.setOnAction(e -> {
            currentUserRole = ""; // Reset the role
            loginMsg.setText(""); // Clear the login message
            userField.clear(); // Clear username field
            passField.clear(); // Clear password field
            setTabAccess(false, false, false, false, false); // Disable all tabs
            tabPane.getSelectionModel().select(tab); // Reset to the login tab
        });

        HBox buttonBox = new HBox(10, loginBtn, logoutBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        vbox.getChildren().addAll(logoLabel, new Label("🔐 User Login"), userField, passField, buttonBox, loginMsg);
        tab.setContent(vbox);
        return tab;
    }

    private void setTabAccess(boolean vehicle, boolean customer, boolean booking, boolean payment, boolean report) {
        tabPane.getTabs().get(0).setDisable(!vehicle);   // Vehicle Tab
        tabPane.getTabs().get(1).setDisable(!customer);  // Customer Tab
        tabPane.getTabs().get(2).setDisable(!booking);   // Booking Tab
        tabPane.getTabs().get(3).setDisable(!payment);   // Payment Tab
        tabPane.getTabs().get(5).setDisable(!report);    // Report Tab
        tabPane.getTabs().get(4).setDisable(false);      // Login Tab always enabled
    }
}