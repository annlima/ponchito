import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Main extends JFrame {
    static Connection conn;

    public Main() {
        conn = getConnection();

        setTitle("PONCHITO: Travel Agency");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        JLabel titleLabel = new JLabel("PONCHITO: Travel Agency");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(Color.DARK_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(Color.WHITE);
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordLabel.setForeground(Color.WHITE);
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.PLAIN, 18));
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // TODO: Change the logic of the if to successfully validate the user @Diego
            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                //JOptionPane.showMessageDialog(this, "Logged in successfully!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                //ClientOptionsWindow clientOptionsWindow = new ClientOptionsWindow();
                //clientOptionsWindow.setVisible(true);
                //this.dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Logged in successfully!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                ClientOptionsWindow clientOptionsWindow = new ClientOptionsWindow();
                clientOptionsWindow.setVisible(true);
                //JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }//end if-else
        });
        loginPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JButton guestButton = new JButton("Continue as Guest");
        guestButton.setFont(new Font("Arial", Font.PLAIN, 18));
        guestButton.addActionListener(e -> {
            GuestWindow guestWindow = new GuestWindow();
            guestWindow.setVisible(true);
            this.dispose();
        });
        loginPanel.add(guestButton, gbc);

        mainPanel.add(loginPanel, BorderLayout.CENTER);

        add(mainPanel);
    }//end Main

    public boolean authenticateUser(String username, String password) {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                System.err.println("Failed to establish a connection to the database.");
                return false;
            }//end if
            String query = "SELECT password_hash, salt FROM ClientAuth WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("password_hash");
                String storedSalt = resultSet.getString("salt");

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String saltedPassword = password + storedSalt;
                byte[] hashedBytes = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
                String hashedPassword = Base64.getEncoder().encodeToString(hashedBytes);

                // Debugging messages
                System.out.println("Stored password hash: " + storedPasswordHash);
                System.out.println("Generated password hash: " + hashedPassword);
                System.out.println("Stored salt: " + storedSalt);

                return storedPasswordHash.equals(hashedPassword);
            } else {
                System.out.println("No matching username found in the database."); // Debugging message
                return false;
            }//end if-else
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }//end try-catch
        return false;
    }//end authenticateUser

    public void executeQuery(String query) {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                System.err.println("Failed to establish a connection to the database.");
                return;
            }//end if

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    System.out.println(columnName + ": " + resultSet.getString(columnName));
                }//end for
                System.out.println("-----------------------");
            }//end while
        } catch (SQLException e) {
            e.printStackTrace();
        }//end try-catch
    }//end executeQuery

    public static Connection getConnection() {
        Connection connection;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/ponchitostravelagency";
            String username = "root";
            String password = "qwe.HJK_02";
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }//end try-catch
        return connection;
    }//end getConnection

    public static void main(String[] arg) {
        Main main = new Main();
        main.setVisible(true);
    }//end main
    class GuestWindow extends JFrame {
        public GuestWindow() {
            setTitle("Guest Options");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Guest Options");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            buttonPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(5, 5, 5, 5);
            JButton simulationButton = new JButton("Simulate Trip");
            simulationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            simulationButton.addActionListener(e -> {
                CreateSimulation_nameRequest createSimulation_nameRequest = new CreateSimulation_nameRequest();
                createSimulation_nameRequest.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(simulationButton, gbc);

            gbc.gridy = 1;
            JButton reservationButton = new JButton("Make a Reservation");
            reservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            reservationButton.addActionListener(e -> {
                // Add logic for making a reservation here
                Reservation_idRequest Reservation_idRequest = new Reservation_idRequest();
                Reservation_idRequest.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(reservationButton, gbc);

            mainPanel.add(buttonPanel, BorderLayout.CENTER);

            add(mainPanel);
        }//end GuestWindow
    }//end class GuestWindow
    class Reservation_idRequest extends JFrame {
        private final JTextField idField;
        public boolean checkSimulationIdExists(String idSimulation) {
            String query = "SELECT * FROM Simulation WHERE simulation_id = '" + idSimulation + "'";
            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                    return false;
                }

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                if (resultSet.next()) {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
        public Reservation_idRequest() {

            setTitle("Reservation");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Make simulation to reservation");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel idPanel = new JPanel();
            idPanel.setLayout(new FlowLayout());
            idPanel.setBackground(Color.DARK_GRAY);

            JLabel idLabel = new JLabel("Introduce your simulation id: ");
            idLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            idLabel.setForeground(Color.WHITE);
            idPanel.add(idLabel);

            idField = new JTextField(15);
            idField.setFont(new Font("Arial", Font.PLAIN, 18));
            idPanel.add(idField);

            mainPanel.add(idPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            // Check Simulation ID by Name Button
            JButton checkIdButton = new JButton("Check Simulation ID by Name");
            checkIdButton.setFont(new Font("Arial", Font.PLAIN, 18));
            checkIdButton.addActionListener(e -> {
                //
            });
            buttonPanel.add(checkIdButton);

            // Next Button
            JButton reservationButton = new JButton("Next");
            reservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            reservationButton.addActionListener(e -> {
                String idSimulation = idField.getText().trim();
                if (!idSimulation.isEmpty()) {
                    // Add code to check if the simulation_id exists in the database
                    boolean simulationIdExists = checkSimulationIdExists(idSimulation);

                    // If simulation_id exists, then proceed to the next window
                    if (simulationIdExists) {
                        // TODO: Check if the client is already registered skip to validation
                        Client_generalDataRequest  Client_generalDataRequest  = new Client_generalDataRequest();
                        Client_generalDataRequest .setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(Reservation_idRequest.this, "Simulation ID does not exist, try to generate a new one", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(Reservation_idRequest.this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(reservationButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end Reservation_idRequest
    }//end class Reservation_idRequest
    static class Client_generalDataRequest extends JFrame {
        public Client_generalDataRequest() {
            setTitle("Client General Data");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(800, 550);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Register to complete Reservation");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridLayout(8, 1, 10, 10));
            formPanel.setBackground(Color.DARK_GRAY);

            JLabel nameLabel = new JLabel("Name:");
            nameLabel.setForeground(Color.WHITE);
            formPanel.add(nameLabel);

            JTextField nameField = new JTextField();
            formPanel.add(nameField);

            JLabel userLabel = new JLabel("User:");
            userLabel.setForeground(Color.WHITE);
            formPanel.add(userLabel);

            JTextField userField = new JTextField();
            formPanel.add(userField);

            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setForeground(Color.WHITE);
            formPanel.add(passwordLabel);

            JTextField passwordField = new JTextField();
            formPanel.add(passwordField);

            JLabel addressLabel = new JLabel("Address:");
            addressLabel.setForeground(Color.WHITE);
            formPanel.add(addressLabel);

            JTextField addressField = new JTextField();
            formPanel.add(addressField);

            JLabel clientTypeLabel = new JLabel("Client Type:");
            clientTypeLabel.setForeground(Color.WHITE);
            formPanel.add(clientTypeLabel);

            JComboBox<String> clientTypeComboBox = new JComboBox<>(new String[]{"Company", "Group", "Individual"});
            formPanel.add(clientTypeComboBox);

            JLabel paymentMethodLabel = new JLabel("Payment Method:");
            paymentMethodLabel.setForeground(Color.WHITE);
            formPanel.add(paymentMethodLabel);

            JComboBox<String> paymentMethodComboBox = new JComboBox<>(new String[]{"Apple Pay", "PayPal", "Credit Card", "Debit Card"});
            formPanel.add(paymentMethodComboBox);

            JLabel fromAgencyLabel = new JLabel("Are you a worker of the agency? :");
            fromAgencyLabel.setForeground(Color.WHITE);
            formPanel.add(fromAgencyLabel);

            JPanel radioButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            radioButtonsPanel.setBackground(Color.DARK_GRAY);

            ButtonGroup fromAgencyGroup = new ButtonGroup();
            JRadioButton yesRadioButton = new JRadioButton("Yes");
            yesRadioButton.setForeground(Color.WHITE);
            yesRadioButton.setBackground(Color.DARK_GRAY);
            fromAgencyGroup.add(yesRadioButton);
            radioButtonsPanel.add(yesRadioButton);

            JRadioButton noRadioButton = new JRadioButton("No");
            noRadioButton.setForeground(Color.WHITE);
            noRadioButton.setBackground(Color.DARK_GRAY);
            fromAgencyGroup.add(noRadioButton);
            radioButtonsPanel.add(noRadioButton);

            formPanel.add(radioButtonsPanel);

            mainPanel.add(formPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(e -> {
                boolean validateSimulationToReservation = true;//TODO: Function that validates
                if (validateSimulationToReservation){
                    //Add Client to DB
                    String clientName = nameField.getText().trim();
                    String address = addressField.getText().trim();
                    String clientType = (String) clientTypeComboBox.getSelectedItem();
                    String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
                    boolean agency = yesRadioButton.isSelected();
                    String registrationYear = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
                    //Data for ClientAuth create an account of the user
                    String username = userField.getText().trim();
                    String password = passwordField.getText().trim();
                    int clientID = 0; // falta un query busquedamax
                    String salt = "cidnsoidf";//falta funcion que genere salt
                    // Create a SQL query to insert the collected data into the Client table
                    String query = "INSERT INTO Client (client_type, registration_year, agency, address, payment_method, client_name) " +
                            "VALUES ('" + clientType + "', '" + registrationYear + "', " + agency + ", '" + address + "', '" + paymentMethod + "', '" + clientName + "')";


                    // TODO: ADD AND CREATE THE CLIENT AUTH TUPLE is not null is required in database

                    String queryAuth = "INSERT INTO ClientAuth (client_id,username,password_hash,salt)" + "VALUES ('" + clientID + "','" + username + "','" + password + "','" + salt + "')";
                    // Execute the SQL query
                    try (Connection connection = getConnection()) {
                        if (connection == null) {
                            System.err.println("Failed to establish a connection to the database.");
                            return;
                        }

                        // Generate Client
                        Statement statement = connection.createStatement();
                        int rowsAffected = statement.executeUpdate(query);

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(Client_generalDataRequest.this, "Client data successfully added.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            // TODO: Give the user their reservation ID or say you can see your reservation with your account
                        } else {
                            JOptionPane.showMessageDialog(Client_generalDataRequest.this, "Failed to add client data.", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        // Generate Client Authorization
                        Statement statementAuth = connection.createStatement();
                        int rowsAffectedAuth = statementAuth.executeUpdate(queryAuth);

                        if (rowsAffectedAuth > 0) {
                            JOptionPane.showMessageDialog(Client_generalDataRequest.this, "Client account successfully created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            // TODO: Give the user their reservation ID or say you can see your reservation with your account
                        } else {
                            JOptionPane.showMessageDialog(Client_generalDataRequest.this, "Failed to create client account.", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        //TODO: Function that creates the reservation (copy paste values of simulation an gives the simulation id)

                    } catch (SQLException el) {
                        el.printStackTrace();
                        JOptionPane.showMessageDialog(Client_generalDataRequest.this, "Failed to add client data.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                }else{
                    JOptionPane.showMessageDialog(Client_generalDataRequest.this, "Failed to validate reservation. There must be errors with the avaialbility of the circuits, hotels or the established dates.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(submitButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end Client_generalDataRequest
    }// end class Client_generalDataRequest
    class CreateSimulation_nameRequest extends JFrame {
        private final JTextField nameField;
        public CreateSimulation_nameRequest() {

            setTitle("Let's Start your Journey....");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Create your Simulation");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel namePanel = new JPanel();
            namePanel.setLayout(new FlowLayout());
            namePanel.setBackground(Color.DARK_GRAY);

            JLabel nameLabel = new JLabel("Insert Name:");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            nameLabel.setForeground(Color.WHITE);
            namePanel.add(nameLabel);

            nameField = new JTextField(15);
            nameField.setFont(new Font("Arial", Font.PLAIN, 18));
            namePanel.add(nameField);

            mainPanel.add(namePanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton reservationButton = new JButton("Next");
            reservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            reservationButton.addActionListener(e -> {
                String userName = nameField.getText().trim();
                if (!userName.isEmpty()) {
                    // Add code for going to choose a country
                    CreateSimulation_selectCountry CreateSimulation_selectCountry = new CreateSimulation_selectCountry();
                    CreateSimulation_selectCountry.setVisible(true);
                    this.dispose();
                    System.out.println("User name: " + userName);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(reservationButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end CreateSimulation_nameRequest
    }//end class CreateSimulation_nameRequest

    class CreateSimulation_selectCountry extends JFrame {
        private static JComboBox<String> countryComboBox;

        public CreateSimulation_selectCountry() {
            setTitle("Select Country to visit:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Select country to go:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            //Query to dynamically have the list of countries based on a query
            ArrayList<String> countriesList = new ArrayList<>();
            try {
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT DISTINCT country FROM City");
                while (resultSet.next()) {
                    String country = resultSet.getString("country");
                    countriesList.add(country);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no country
            }
            String[] countries = countriesList.toArray(new String[0]);

            // Drop down selector with countries
            countryComboBox = new JComboBox<>(countries);
            countryComboBox.setSelectedIndex(-1);
            countryComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
            countryComboBox.setForeground(Color.WHITE);
            countryComboBox.setBackground(Color.DARK_GRAY);
            countryComboBox.addActionListener(e -> {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                System.out.println("Selected Country: " + comboBox.getSelectedItem());
            });
            mainPanel.add(countryComboBox, BorderLayout.CENTER);

            // Add a "Next" button to move to another window
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton nextButton = new JButton("Next");
            nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
            nextButton.addActionListener(e -> {
                if (countryComboBox.getSelectedIndex() != -1) {
                    String selectedCountry = (String) countryComboBox.getSelectedItem();
                    System.out.println("Selected Country: " + selectedCountry);

                    // Add code for going to general data of simulation
                    CreateSimulation_generalData CreateSimulation_generalData = new CreateSimulation_generalData();
                    CreateSimulation_generalData.setVisible(true);
                    this.dispose();

                } else {
                    JOptionPane.showMessageDialog(this, "Please select a country", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(nextButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end CreateSimulation_selectCountry
    }//end class CreateSimulation_selectCountry

    // class to use for the checkboxing of the cities
    class NumberedCheckBox extends JCheckBox {
        private int number;

        public NumberedCheckBox(String text) {
            super(text);
            number = 0;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    class CreateSimulation_generalData extends JFrame {
        private final JFormattedTextField arrivalDateField;
        private final JFormattedTextField departureDateField;
        private final JSpinner travelersSpinner;

        public CreateSimulation_generalData() {
            setTitle("General Data:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(600, 400);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("General Data:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            // Form panel with arrival date, departure date, and number of travelers
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new GridBagLayout());
            formPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(5, 5, 5, 5);
            JLabel arrivalDateLabel = new JLabel("Arrival Date:");
            arrivalDateLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            arrivalDateLabel.setForeground(Color.WHITE);
            formPanel.add(arrivalDateLabel, gbc);

            gbc.gridx = 1;
            arrivalDateField = new JFormattedTextField(new SimpleDateFormat("MM/dd/yyyy"));
            arrivalDateField.setValue(new Date(System.currentTimeMillis()));
            arrivalDateField.setFont(new Font("Arial", Font.PLAIN, 18));
            arrivalDateField.setColumns(10);
            formPanel.add(arrivalDateField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            JLabel departureDateLabel = new JLabel("Departure Date:");
            departureDateLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            departureDateLabel.setForeground(Color.WHITE);
            formPanel.add(departureDateLabel, gbc);

            gbc.gridx = 1;
            departureDateField = new JFormattedTextField(new SimpleDateFormat("MM/dd/yyyy"));
            departureDateField.setValue(new Date(System.currentTimeMillis()));
            departureDateField.setFont(new Font("Arial", Font.PLAIN, 18));
            departureDateField.setColumns(10);
            formPanel.add(departureDateField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            JLabel travelersLabel = new JLabel("Number of Travelers:");
            travelersLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            travelersLabel.setForeground(Color.WHITE);
            formPanel.add(travelersLabel, gbc);

            gbc.gridx = 1;
            SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 50, 1);
            travelersSpinner = new JSpinner(spinnerModel);
            travelersSpinner.setFont(new Font("Arial", Font.PLAIN, 18));
            formPanel.add(travelersSpinner, gbc);

            mainPanel.add(formPanel, BorderLayout.CENTER);

            // Panel with a list of cities
            JPanel citiesPanel = new JPanel();
            citiesPanel.setLayout(new BorderLayout());
            citiesPanel.setBackground(Color.DARK_GRAY);

            // Get the list of city names based on the selected country
            List<String> cityNames = new ArrayList<>();
            String selectedCountry = Objects.requireNonNull(CreateSimulation_selectCountry.countryComboBox.getSelectedItem()).toString();
            try {
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT city_name FROM City WHERE country = '" + selectedCountry + "'");
                while (resultSet.next()) {
                    String city = resultSet.getString("city_name");
                    cityNames.add(city);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JLabel citiesLabel = new JLabel("Select Cities:");
            citiesLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            citiesLabel.setForeground(Color.WHITE);
            citiesPanel.add(citiesLabel, BorderLayout.NORTH);

            JPanel checkBoxPanel = new JPanel();
            checkBoxPanel.setLayout(new GridLayout(cityNames.size(), 1));
            checkBoxPanel.setBackground(Color.DARK_GRAY);

            List<NumberedCheckBox> cityCheckBoxes = new ArrayList<>();
            AtomicInteger currentNumber = new AtomicInteger(1);

            for (String cityName : cityNames) {
                NumberedCheckBox checkBox = new NumberedCheckBox(cityName);
                checkBox.setFont(new Font("Arial", Font.PLAIN, 18));
                checkBox.setForeground(Color.WHITE);
                checkBox.setBackground(Color.DARK_GRAY);

                checkBox.addActionListener(e -> {
                    NumberedCheckBox source = (NumberedCheckBox) e.getSource();
                    if (source.isSelected()) {
                        source.setNumber(currentNumber.getAndIncrement());
                        source.setText(source.getNumber() + " - " + cityName);
                    } else {
                        int removedNumber = source.getNumber();
                        source.setNumber(0);
                        source.setText(cityName);
                        currentNumber.decrementAndGet();

                        // Reorder the remaining selected checkboxes
                        cityCheckBoxes.stream()
                                .filter(NumberedCheckBox::isSelected)
                                .filter(cb -> cb.getNumber() > removedNumber)
                                .forEach(cb -> {
                                    cb.setNumber(cb.getNumber() - 1);
                                    cb.setText(cb.getNumber() + " - " + cb.getText().substring(cb.getText().indexOf(" - ") + 3));
                                });
                    }
                });

                cityCheckBoxes.add(checkBox);
                checkBoxPanel.add(checkBox);
            }

            JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
            citiesPanel.add(scrollPane, BorderLayout.CENTER);

            // Panel with "Back" and "Next" buttons
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(citiesPanel, BorderLayout.NORTH);
            JPanel buttonPanel;
            buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                dispose();
                CreateSimulation_selectCountry selectCountryFrame = new CreateSimulation_selectCountry();
                selectCountryFrame.setVisible(true);
            });
            buttonPanel.add(backButton);

            JButton finishButton = new JButton("Next");
            finishButton.setFont(new Font("Arial", Font.PLAIN, 18));
            finishButton.addActionListener(e -> {
                // Get the values from the form and the selected cities
                Date arrivalDate = (Date) arrivalDateField.getValue();
                Date departureDate = (Date) departureDateField.getValue();
                int numTravelers = (int) travelersSpinner.getValue();
                List<NumberedCheckBox> sortedSelectedCheckBoxes = cityCheckBoxes.stream()
                        .filter(NumberedCheckBox::isSelected)
                        .sorted(Comparator.comparingInt(NumberedCheckBox::getNumber))
                        .toList();

                List<String> selectedCitiesList = new ArrayList<>();
                for (NumberedCheckBox checkBox : sortedSelectedCheckBoxes) {
                    selectedCitiesList.add(checkBox.getText().substring(checkBox.getText().indexOf(" - ") + 3));
                }

                String[] selectedCities = selectedCitiesList.toArray(new String[0]);

                // Print out the values (you can replace this with saving to a database, etc.)
                System.out.println("Arrival Date: " + arrivalDate);
                System.out.println("Departure Date: " + departureDate);
                System.out.println("Number of Travelers: " + numTravelers);
                System.out.println("Selected Cities:");
                for (String city : selectedCities) {
                    System.out.println("- " + city);
                }

                // Move to Available circuits
                CreateSimulation_selectCircuits CreateSimulation_selectCircuits = new CreateSimulation_selectCircuits(selectedCities, arrivalDate, departureDate);
                CreateSimulation_selectCircuits.setVisible(true);
                this.dispose();


            });
            buttonPanel.add(finishButton);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

            mainPanel.add(formPanel, BorderLayout.WEST);
            mainPanel.add(citiesPanel, BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            add(mainPanel);
        }//end CreateSimulation_generalData
    }// end class CreateSimulation_generalData
    class CreateSimulation_selectHotels extends JFrame {
        public CreateSimulation_selectHotels(String[] selectedCities){

        }//end class CreateSimulation_selectHotels
    }//end class CreateSimulation_selectHotels


    class CreateSimulation_selectCircuits extends JFrame{
        private final List<JCheckBox> checkBoxes;
        private final List<String> selectedCircuitIds;
        public CreateSimulation_selectCircuits(String[] selectedCities, Date arrivalDate, Date departureDate) {
            // Initialize lists
            checkBoxes = new ArrayList<>();
            selectedCircuitIds = new ArrayList<>();

            setTitle("Select Circuits:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(600, 400);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Select Circuits:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel formPanel = new JPanel();
            formPanel.setLayout(new GridBagLayout());
            formPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            for (int i = 0; i < selectedCities.length - 1; i++) {
                String departingCity = selectedCities[i];
                String arrivalCity = selectedCities[i + 1];

                gbc.gridy = i;
                String circuitText = departingCity + " - " + arrivalCity + ":";
                JLabel cityPairLabel = new JLabel(circuitText);
                cityPairLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                cityPairLabel.setForeground(Color.WHITE);
                gbc.gridx = 0;
                formPanel.add(cityPairLabel, gbc);

                JPanel circuitPanel = new JPanel();
                circuitPanel.setLayout(new BoxLayout(circuitPanel, BoxLayout.Y_AXIS));
                gbc.gridx = 0;
                formPanel.add(circuitPanel, gbc);

                ButtonGroup buttonGroup = new ButtonGroup();

                try {
                    final Date[] departureDateCircuit = {departureDate};
                    ResultSet resultSet = conn.createStatement().executeQuery(
                            "SELECT Circuit.circuit_id,description,departure_date,trip_duration,cost FROM Circuit, DateCircuit  WHERE (Circuit.circuit_id = DateCircuit.circuit_id AND departing_city = '+departingCity' AND arrival_city = '+departingCity' AND departure_date = '"+departureDate+"')");

                    /*

                    * SELECT Circuit.circuit_id, description,departure_date, trip_duration, cost
                            FROM Circuit, DateCircuit
                            WHERE (Circuit.circuit_id = DateCircuit.circuit_id
                            AND departing_city = 'New York'
                            AND arrival_city = 'Los Angeles'
                            AND departure_date = '2023-06-01')*/

                    int daysUpdate = 0;
                    AtomicBoolean notNext = new AtomicBoolean(false);
                    while (resultSet.next()) {
                        while (notNext.get()) {
                            //TODO ??????
                        } //end while
                        notNext.set(true);
                        String circuitId = resultSet.getString("circuit_id");
                        String description = resultSet.getString("description");
                        Date date = resultSet.getDate("departure_date");
                        int tripDuration = resultSet.getInt("trip_duration");
                        double cost = resultSet.getDouble("cost");

                        String checkBoxText = description + ", " + date + ", " + tripDuration + " days, $" + cost;
                        JCheckBox checkBox = new JCheckBox(checkBoxText);
                        checkBox.setFont(new Font("Arial", Font.PLAIN, 18));
                        checkBox.setForeground(Color.WHITE);
                        checkBox.setBackground(Color.DARK_GRAY);
                        checkBoxes.add(checkBox);
                        buttonGroup.add(checkBox);

                        int currentIndex = i;
                        checkBox.addActionListener(e -> {
                            if (checkBox.isSelected()) {


                                selectedCircuitIds.set(currentIndex, circuitId);
                            }
                        });

                        circuitPanel.add(checkBox);

                        // Panel with "Back" and "Confirm" buttons
                        JPanel buttonPanel = new JPanel();
                        buttonPanel.setBackground(Color.DARK_GRAY);
                        // "Next" button
                        JButton nextButton = new JButton("Next");
                        nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
                        nextButton.addActionListener(e -> {


                            for (JCheckBox ignored : checkBoxes) {
                                if (checkBox.isSelected()) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(departureDateCircuit[0]);
                                    cal.add(Calendar.DAY_OF_MONTH, tripDuration);
                                    departureDateCircuit[0] = (Date) cal.getTime();
                                }
                            }

                            notNext.set(false);
                            this.dispose();
                        });
                        buttonPanel.add(nextButton);
                        circuitPanel.add(buttonPanel);
                    }

                    //revisar si con una tupla que la fecha de salida sea la fecha que se acaba el circuito actual si sale como opcion
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            mainPanel.add(formPanel, BorderLayout.CENTER);

            // Panel with "Back" and "Confirm" buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            // "Back" button
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                CreateSimulation_generalData CreateSimulation_generalData = new CreateSimulation_generalData();
                CreateSimulation_generalData.setVisible(true);
                this.dispose();

            });
            buttonPanel.add(backButton);

            // "Confirm" button
            JButton confirmButton = new JButton("Confirm");
            confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
            confirmButton.addActionListener(e -> {
                // Hotel Window
                CreateSimulation_selectHotels CreateSimulation_selectHotels = new CreateSimulation_selectHotels(selectedCities);
                CreateSimulation_selectHotels.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(confirmButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(mainPanel);
        }// end CreateSimulation_selectCircuits
    }// end class CreateSimulation_selectCircuits

    class SelectCountryToViewPamphlet extends JFrame {
        private static JComboBox<String> countryComboBox;

        public SelectCountryToViewPamphlet() {
            setTitle("Select Country to view pamphlet:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Select country to view pamphlet:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            //Query to dynamically have the list of countries based on a query
            ArrayList<String> countriesList = new ArrayList<>();
            try {
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT DISTINCT country FROM City");
                while (resultSet.next()) {
                    String country = resultSet.getString("country");
                    countriesList.add(country);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no country
            }
            String[] countries = countriesList.toArray(new String[0]);

            // Drop down selector with countries
            countryComboBox = new JComboBox<>(countries);
            countryComboBox.setSelectedIndex(-1);
            countryComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
            countryComboBox.setForeground(Color.WHITE);
            countryComboBox.setBackground(Color.DARK_GRAY);
            countryComboBox.addActionListener(e -> {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                System.out.println("Selected Country: " + comboBox.getSelectedItem());
            });
            mainPanel.add(countryComboBox, BorderLayout.CENTER);

            // Add a "Back" button to move back to the main menu
            //JPanel buttonPanel = new JPanel();
            //buttonPanel.setBackground(Color.DARK_GRAY);
            //JButton backButton = new JButton("Back");
            //backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            //backButton.addActionListener(e -> {
            // Show the ClientOptionsWindow again
            //    ClientOptionsWindow clientOptionsWindow = new ClientOptionsWindow();
            //    clientOptionsWindow.setVisible(true);
            //    this.dispose();
            //});

            JButton nextButton = new JButton("Next");
            nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
            nextButton.addActionListener(e -> {
                if (countryComboBox.getSelectedIndex() != -1) {
                    // Show the SelectCityToViewPamphlet
                    SelectCityToViewPamphlet selectCityToViewPamphlet = new SelectCityToViewPamphlet(Objects.requireNonNull(countryComboBox.getSelectedItem()).toString());
                    selectCityToViewPamphlet.setVisible(true);
                    this.dispose();
                }
            });

            //buttonPanel.add(backButton);
            //mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            nextButton.add(nextButton);
            mainPanel.add(nextButton, BorderLayout.SOUTH);

            add(mainPanel);
        }//end SelectCountryToViewPamphlet
    }//end class SelectCountryToViewPamphlet

    class SelectCityToViewPamphlet extends JFrame {
        private final JComboBox<String> cityComboBox;

        public SelectCityToViewPamphlet(String selectedCountry) {
            setTitle("Select City to view pamphlet:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Select city to view pamphlet:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            // Query to dynamically have the list of cities based on the selected country
            ArrayList<String> citiesList = new ArrayList<>();
            try {
                PreparedStatement pstmt = conn.prepareStatement("SELECT city_name FROM City WHERE country = ?");
                pstmt.setString(1, selectedCountry);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    String city = resultSet.getString("name");
                    citiesList.add(city);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no city
            }
            String[] cities = citiesList.toArray(new String[0]);

            // Drop down selector with cities
            cityComboBox = new JComboBox<>(cities);
            cityComboBox.setSelectedIndex(-1);
            cityComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
            cityComboBox.setForeground(Color.WHITE);
            cityComboBox.setBackground(Color.DARK_GRAY);
            cityComboBox.addActionListener(e -> {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                System.out.println("Selected City: " + comboBox.getSelectedItem());
            });
            mainPanel.add(cityComboBox, BorderLayout.CENTER);

            // Add a "Back" and "Next" button to move back to the main menu or forward to SelectCircuitAndHotel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Show the SelectCountryToViewPamphlet again
                SelectCountryToViewPamphlet selectCountryToViewPamphlet = new SelectCountryToViewPamphlet();
                selectCountryToViewPamphlet.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(backButton);

            JButton nextButton = new JButton("Next");
            nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
            nextButton.addActionListener(e -> {
                if (cityComboBox.getSelectedIndex() != -1) {
                    // Show the SelectCircuitAndHotel
                    SelectCircuitAndHotel selectCircuitAndHotel = new SelectCircuitAndHotel(Objects.requireNonNull(cityComboBox.getSelectedItem()).toString(), selectedCountry);
                    selectCircuitAndHotel.setVisible(true);
                    this.dispose();
                }
            });
            buttonPanel.add(nextButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end SelectCityToViewPamphlet
    }//end class SelectCityToViewPamphlet

    class SelectCircuitAndHotel extends JFrame {

        public SelectCircuitAndHotel(String selectedCity, String selectedCountry) {
            setTitle("Select Circuit and Hotel");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(800, 500);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Select Circuit and Hotel");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            contentPanel.setBackground(Color.DARK_GRAY);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Query and display circuits based on the selected city and country
            ArrayList<String> circuits = new ArrayList<>();
            try {
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Circuit WHERE departing_city = ? AND departing_country = ?");
                pstmt.setString(1, selectedCity);
                pstmt.setString(2, selectedCountry);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    String circuit = resultSet.getString("description");
                    circuits.add(circuit);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no circuit
            }
            JList<String> circuitList = new JList<>(circuits.toArray(new String[0]));
            circuitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contentPanel.add(new JScrollPane(circuitList));

            // Query and display hotels based on the selected city and country
            ArrayList<String> hotels = new ArrayList<>();
            try {
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Hotel WHERE city_name = ? AND country = ?");
                pstmt.setString(1, selectedCity);
                pstmt.setString(2, selectedCountry);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    String hotel = resultSet.getString("hotel_name");
                    hotels.add(hotel);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no hotel
            }
            JList<String> hotelList = new JList<>(hotels.toArray(new String[0]));
            hotelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contentPanel.add(new JScrollPane(hotelList));

            mainPanel.add(contentPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Show the SelectCityToViewPamphlet again
                SelectCityToViewPamphlet selectCityToViewPamphlet = new SelectCityToViewPamphlet(selectedCountry);
                selectCityToViewPamphlet.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(backButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end SelectCircuitAndHotel
    }//end class SelectCircuitAndHotel



    class ClientOptionsWindow extends JFrame {
        public ClientOptionsWindow() {
            setTitle("Client Options");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 300);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Client Options");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(4, 1, 5, 5));
            buttonPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            JButton seePamphletButton = new JButton("See pamphlet");
            seePamphletButton.setFont(new Font("Arial", Font.PLAIN, 18));
            seePamphletButton.addActionListener(e -> {
                // Add logic for "See pamphlet" here
                SelectCountryToViewPamphlet selectCountryToViewPamphlet = new SelectCountryToViewPamphlet();
                selectCountryToViewPamphlet.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(seePamphletButton, gbc);

            JButton doSimulationButton = new JButton("Do a simulation");
            doSimulationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            doSimulationButton.addActionListener(e -> {
                // Add logic for "Do a simulation" here
            });
            buttonPanel.add(doSimulationButton, gbc);

            JButton doReservationButton = new JButton("Do a reservation");
            doReservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            doReservationButton.addActionListener(e -> {
                // Add logic for "Do a reservation" here
            });
            buttonPanel.add(doReservationButton, gbc);

            JButton checkReservationButton = new JButton("Check a reservation");
            checkReservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            checkReservationButton.addActionListener(e -> {
                // Add logic for "Check a reservation" here
            });
            buttonPanel.add(checkReservationButton, gbc);

            mainPanel.add(buttonPanel, BorderLayout.CENTER);

            add(mainPanel);
        }//end ClientOptionsWindow
    }//end class ClientOptionsWindow


}//end class Main
