import java.awt.*;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
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
import java.util.concurrent.atomic.AtomicInteger;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.sql.Types.NULL;

//TODO Circuit and Hotel selection improvements (next)
//TODO CONFIRM YOUR SIMULATION EMPTY WINDOW


// TODO Agregar dato de termino de viaje (independiente de termino de circuito)
//  cambia simulacion a esReservacion true, decrementa disponibilidad de hotel y circuito notifica y da id de reservacion)
//  En simulacionCircuito mostrar lugares a visitar de ese circuito

// TODO AGREGAR FORMA DE PONER TEXTO EN SEARCH SIMULATION ID BY NAME

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
        passwordField.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Logged in successfully!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                ClientOptionsWindow ClientOptionsWindow = new ClientOptionsWindow(username);
                ClientOptionsWindow.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }//end if-else
        });
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

            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Logged in successfully!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                ClientOptionsWindow ClientOptionsWindow = new ClientOptionsWindow(username);
                ClientOptionsWindow.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
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
                //System.out.println("Stored password hash: " + storedPasswordHash);
                //System.out.println("Generated password hash: " + hashedPassword);
                //System.out.println("Stored salt: " + storedSalt);

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
    public static Connection getConnection() {
        Connection connection;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/ponchitostravelagency?serverTimezone=America/Mexico_City&useSSL=false";
            String username = "root";
            String password = "3011";
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
            setSize(450, 250);
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
                Reservation_idRequest Reservation_idRequest = new Reservation_idRequest(true);
                Reservation_idRequest.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(reservationButton, gbc);
            // Back Button
            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                Main Main = new Main();
                Main.setVisible(true);
                this.dispose();
            });

            backButtonPanel.add(backButton);
            mainPanel.add(buttonPanel, BorderLayout.CENTER);
            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);

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

        public void simulationToReservation (String idSimulation) {
            String insertReservationQuery = "INSERT INTO reservation (simulation_id, client_id, reservation_created_at) SELECT simulation_id, client_id, NOW() FROM simulation WHERE simulation_id = ?";
            String insertReservationCircuitQuery = "INSERT INTO reservationcircuit (reservation_id, circuit_id) SELECT ?, circuit_id FROM simulationcircuit WHERE simulation_id = ?";
            String insertReservationHotelQuery = "INSERT INTO reservationhotel (reservation_id, hotel_name, departure_date, arrival_date) SELECT ?, hotel_name, departure_date, arrival_date FROM simulationhotel WHERE simulation_id = ?";
            String updateSimulationQuery = "UPDATE simulation SET is_reservation = 1 WHERE simulation_id = '" + idSimulation + "'";

            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                }

                // Start a transaction
                connection.setAutoCommit(false);

                try (PreparedStatement insertReservationStmt = connection.prepareStatement(insertReservationQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertReservationStmt.setInt(1, Integer.parseInt(idSimulation));
                    insertReservationStmt.executeUpdate();

                    try (ResultSet generatedKeys = insertReservationStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int reservationId = generatedKeys.getInt(1);

                            try (PreparedStatement insertReservationCircuitStmt = connection.prepareStatement(insertReservationCircuitQuery)) {
                                insertReservationCircuitStmt.setInt(1, reservationId);
                                insertReservationCircuitStmt.setInt(2, Integer.parseInt(idSimulation));
                                insertReservationCircuitStmt.executeUpdate();
                            }

                            try (PreparedStatement insertReservationHotelStmt = connection.prepareStatement(insertReservationHotelQuery)) {
                                insertReservationHotelStmt.setInt(1, reservationId);
                                insertReservationHotelStmt.setInt(2, Integer.parseInt(idSimulation));
                                insertReservationHotelStmt.executeUpdate();
                            }
                        }
                    }

                    try (PreparedStatement updateSimulationStmt = connection.prepareStatement(updateSimulationQuery)) {
                        updateSimulationStmt.setInt(1, Integer.parseInt(idSimulation));
                        updateSimulationStmt.executeUpdate();
                    }

                    // Commit the transaction
                    connection.commit();
                } catch (SQLException ex) {
                    // Roll back the transaction if there was a problem
                    connection.rollback();
                    ex.printStackTrace();
                }

                // Restore auto commit
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public boolean comprobationOfAvilability (String idSimulation) {    
            boolean available = true;
            try {
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT simulation.num_people, datecircuit.num_people  FROM simulationcircuit, datecircuit, simulation WHERE circuit_id = '"+idSimulation+"' AND datecircuit.circuit_id=simulationCircuit.circuit_id AND simulation.simulation_id=simulationCircuit.simulation_id" );
                while (resultSet.next()) {
                    int circuitAvailability = resultSet.getInt("simulation.num_people");
                    int travelers =  resultSet.getInt("circuit.num_people");
                    if (circuitAvailability<travelers) {
                        return false;
                    } //end if
                    else {
                        int updatedNumPeople = circuitAvailability - travelers;
                        try {
                            // Actualizar el valor de datecircuit.num_people en la base de datos
                            String updateQuery = "UPDATE datecircuit SET num_people = " + updatedNumPeople + " WHERE circuit_id = '" + idSimulation + "'";
                            conn.createStatement().executeUpdate(updateQuery);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //AVAILABILITY OF ROOMS
            try {
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT num_rooms, num_people, hotel_name FROM Hotel, simulationhotel, simulation WHERE  simulation_id= '"+ idSimulation + "' AND hotel.hotel_name = simulationhotel.hotel_name AND simulation.simulation_id=simulationhotel.simulation_id");

                while (resultSet.next()) {
                    int hotelAvailability = resultSet.getInt("num_rooms");
                    int travelers = resultSet.getInt("num_people");
                    String hotelName = resultSet.getString("hotel_name");

                    if (hotelAvailability < travelers) {
                        return false;
                    } else {
                        // Update the num_rooms in the Hotel table
                        String updateQuery = "UPDATE Hotel SET num_rooms = num_rooms - ? WHERE hotel_name = ?";
                        try (PreparedStatement updateStatement = conn.prepareStatement(updateQuery)) {
                            updateStatement.setInt(1, travelers);
                            updateStatement.setString(2, hotelName);
                            updateStatement.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return available;
        }
        
        public Reservation_idRequest(boolean isGuest) {

            setTitle("Reservation");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(500, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Make simulation to reservation");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);

            // Create a new JPanel for titleLabel with a centered FlowLayout
            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            titlePanel.setBackground(Color.DARK_GRAY);
            titlePanel.add(titleLabel);

            mainPanel.add(titlePanel, BorderLayout.NORTH);


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


            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout((FlowLayout.CENTER))); // Layout changed to FlowLayout
            buttonPanel.setBackground(Color.DARK_GRAY);

            // Check Simulation ID by Name Button
            JButton checkIdButton = new JButton("Check Simulation ID by Name");
            checkIdButton.setFont(new Font("Arial", Font.PLAIN, 18));
            checkIdButton.addActionListener(e -> {
                SimulationSearch_requestName SimulationSearch_requestName = new SimulationSearch_requestName();
                SimulationSearch_requestName.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(checkIdButton);

            // Next Button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setLayout(new FlowLayout((FlowLayout.CENTER))); // Layout changed to FlowLayout
            backButtonPanel.setBackground(Color.DARK_GRAY);

            JButton reservationButton = new JButton("Next");
            reservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            reservationButton.addActionListener(e -> {
                String idSimulation = idField.getText().trim();
                if (!idSimulation.isEmpty()) {
                    // Add code to check if the simulation_id exists in the database
                    boolean simulationIdExists = checkSimulationIdExists(idSimulation);

                    // If simulation_id exists, then proceed to the next window
                    if (simulationIdExists) {
                        // If the simulation exist then proceed
                        if (isGuest){
                            Client_generalDataRequest Client_generalDataRequest = new Client_generalDataRequest();
                            Client_generalDataRequest.setVisible(true);
                            this.dispose();
                        } else {
                            if (comprobationOfAvilability(idSimulation)){
                                simulationToReservation(idSimulation);
                                JOptionPane.showMessageDialog(Reservation_idRequest.this, "Your journey awaits you... your reservation has been made", "Reservation has been made", JOptionPane.INFORMATION_MESSAGE);
                            }else{
                                JOptionPane.showMessageDialog(Reservation_idRequest.this, "There were errors with the availability of your simulation. Try making another simulation", "Error no availability", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(Reservation_idRequest.this, "Simulation ID does not exist, try to generate a new one", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(Reservation_idRequest.this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            backButtonPanel.add(reservationButton);

            // Back button
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                if (isGuest) {
                    GuestWindow GuestWindow = new GuestWindow();
                    GuestWindow.setVisible(true);
                }else {
                    Main Main = new Main();
                    Main.setVisible(true);
                }
                this.dispose();
            });

            backButtonPanel.add(backButton);

            mainPanel.add(idPanel);
            mainPanel.add(Box.createVerticalStrut(10)); // Add some space between panels
            mainPanel.add(buttonPanel);
            mainPanel.add(Box.createVerticalStrut(10)); // Add some space between panels
            mainPanel.add(backButtonPanel);

            add(mainPanel);
        }//end Reservation_idRequest
    }//end class Reservation_idRequest

    class SimulationSearch_requestName extends JFrame {
        private final JTextField nameField;

        public SimulationSearch_requestName() {
            setTitle("Search Simulation ID by name");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Search simulation ID by name");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel namePanel = new JPanel();
            namePanel.setLayout(new FlowLayout());
            namePanel.setBackground(Color.DARK_GRAY);

            JLabel nameLabel = new JLabel("Insert your Name:");
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            nameLabel.setForeground(Color.WHITE);
            namePanel.add(nameLabel);

            nameField = new JTextField(15);
            nameField.setFont(new Font("Arial", Font.PLAIN, 18));
            namePanel.add(nameField);

            nameField.addActionListener(e -> {
                String searchedName = nameField.getText().trim();
                if (!searchedName.isEmpty()) {
                    // Goes to summary of Simulations that are associated with a name
                    SimulationsFromName SimulationsFromName = new SimulationsFromName(searchedName);
                    SimulationsFromName.setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a name to Search", "Error", JOptionPane.ERROR_MESSAGE);
                }//end if-else
            });
            namePanel.add(nameField);

            mainPanel.add(namePanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton reservationButton = new JButton("Next");
            reservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            reservationButton.addActionListener(e -> {
                String searchedName = nameField.getText().trim();
                if (!searchedName.isEmpty()) {
                    // Goes to summary of Simulations that are associated with a name
                    SimulationsFromName SimulationsFromName = new SimulationsFromName(searchedName);
                    SimulationsFromName.setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a name to Search", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(reservationButton);
            mainPanel.add(buttonPanel, BorderLayout.CENTER);

            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                Reservation_idRequest Reservation_idRequest = new Reservation_idRequest(true);
                Reservation_idRequest.setVisible(true);
                this.dispose();
            });
            backButtonPanel.add(backButton);

            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }// end SimulationSearch_requestName
    }// end class SimulationSearch_requestName

    class SimulationsFromName extends JFrame {
        public List<String> searchSimulations(String searchedName) {
            List<String> simulationIds = new ArrayList<>();

            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                    return simulationIds;
                }

                String query = "SELECT simulation_id FROM Simulation WHERE client_name LIKE '" + searchedName + "'";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String simulationId = resultSet.getString("simulation_id");
                    simulationIds.add(simulationId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return simulationIds;
        }

        public SimulationsFromName(String searchedName) {
            setTitle("Simulations for " + searchedName);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Simulations for " + searchedName);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            List<String> simulationIds = searchSimulations(searchedName);

            if (simulationIds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "There are no simulations with the searched name: " + searchedName,
                        "No Simulations Found",
                        JOptionPane.WARNING_MESSAGE);
                // Go back to previous window
                SimulationSearch_requestName SimulationSearch_requestName = new SimulationSearch_requestName();
                SimulationSearch_requestName.setVisible(true);
                this.dispose();

            } else {
                JPanel simulationPanel = new JPanel();
                simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));
                simulationPanel.setBackground(Color.DARK_GRAY);

                for (String simulationId : simulationIds) {
                    JPanel singleSimulationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    singleSimulationPanel.setBackground(Color.DARK_GRAY);

                    JLabel simulationLabel = new JLabel("Simulation ID: " + simulationId);
                    simulationLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                    simulationLabel.setForeground(Color.WHITE);
                    singleSimulationPanel.add(simulationLabel);

                    JButton moreInfoButton = new JButton("More Info");
                    moreInfoButton.setFont(new Font("Arial", Font.PLAIN, 18));
                    moreInfoButton.addActionListener(e -> {
                        // Create and open a new window to show the simulation details
                        SimulationDetailsWindow simulationDetailsWindow = new SimulationDetailsWindow(simulationId);
                        simulationDetailsWindow.setVisible(true);
                    });
                    singleSimulationPanel.add(moreInfoButton);

                    simulationPanel.add(singleSimulationPanel);
                }

                JScrollPane scrollPane = new JScrollPane(simulationPanel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                mainPanel.add(scrollPane, BorderLayout.CENTER);
            }

            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                SimulationSearch_requestName SimulationSearch_requestName = new SimulationSearch_requestName();
                SimulationSearch_requestName.setVisible(true);
                this.dispose();
            });
            backButtonPanel.add(backButton);

            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }// end SimulationsFromName
    }// end class SimulationsFromName

    class SimulationDetailsWindow extends JFrame {
        public SimulationDetailsWindow(String simulationId) {
            // Create and set up the window to display the simulation details
            setTitle("Simulation Details: " + simulationId);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(600, 400);
            setResizable(false);
            setLocationRelativeTo(null);
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Simulation Details: " + simulationId);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new GridBagLayout());
            detailsPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            /**
             * The SQL query is built that selects the columns "client_name", "client_id", "departure_date", "arrival_date",
             * "num_people", "cost" and "simulation_created_at" from the table "Simulation" where the value of "simulation_id"
             * matches with the specified Simulation ID.
             */
            String query = "SELECT client_name, client_id, departure_date, arrival_date, num_people, cost, simulation_created_at FROM Simulation WHERE simulation_id = " + simulationId;
            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                    return;
                }

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                if (resultSet.next()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnValue = resultSet.getString(columnName);

                        gbc.gridx = 0;
                        gbc.gridy = i - 1;
                        JLabel columnLabel = new JLabel(columnName + ":");
                        columnLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                        columnLabel.setForeground(Color.WHITE);
                        detailsPanel.add(columnLabel, gbc);

                        gbc.gridx = 1;
                        JLabel valueLabel = new JLabel(columnValue);
                        valueLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                        valueLabel.setForeground(Color.WHITE);
                        detailsPanel.add(valueLabel, gbc);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            mainPanel.add(detailsPanel, BorderLayout.CENTER);
            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                SimulationsFromName SimulationsFromName = new SimulationsFromName(simulationId);
                SimulationsFromName.setVisible(true);
                this.dispose();
            });
            backButtonPanel.add(backButton);

            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);
            add(mainPanel);
        }// end SimulationDetailsWindow
    }// end class SimulationDetailsWindow

    class Client_generalDataRequest extends JFrame {
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
                String clientType = (String) clientTypeComboBox.getSelectedItem();
                String registrationYear = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
                boolean agency = yesRadioButton.isSelected();
                String address = addressField.getText().trim();
                String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
                String clientName = nameField.getText().trim();
                String username = userField.getText().trim();
                String password = passwordField.getText().trim();

                try (Connection connection = getConnection()) {
                    if (connection == null) {
                        System.err.println("Failed to establish a connection to the database.");
                        return;
                    }//end if

                    // Insert client information into the Client table
                    String clientQuery = "INSERT INTO Client (client_type, registration_year, agency, address, payment_method, client_name) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement clientPreparedStatement = connection.prepareStatement(clientQuery, Statement.RETURN_GENERATED_KEYS);
                    clientPreparedStatement.setString(1, clientType);
                    clientPreparedStatement.setString(2, registrationYear);
                    clientPreparedStatement.setBoolean(3, agency);
                    clientPreparedStatement.setString(4, address);
                    clientPreparedStatement.setString(5, paymentMethod);
                    clientPreparedStatement.setString(6, clientName);
                    int rowsAffected = clientPreparedStatement.executeUpdate();

                    if (rowsAffected == 0) {
                        JOptionPane.showMessageDialog(this, "Failed to add client.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }//end if

                    // Get the generated client_id
                    ResultSet generatedKeys = clientPreparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int clientId = generatedKeys.getInt(1);

                        // Generate salt and hashed password
                        SecureRandom random = new SecureRandom();
                        byte[] saltBytes = new byte[16];
                        random.nextBytes(saltBytes);
                        String salt = Base64.getEncoder().encodeToString(saltBytes);

                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        String saltedPassword = password + salt;
                        byte[] hashedBytes = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
                        String hashedPassword = Base64.getEncoder().encodeToString(hashedBytes);

                        // Insert the client's authentication information into the ClientAuth table
                        String authQuery = "INSERT INTO ClientAuth (client_id, username, password_hash, salt) VALUES (?, ?, ?, ?)";
                        PreparedStatement authPreparedStatement = connection.prepareStatement(authQuery);
                        authPreparedStatement.setInt(1, clientId);
                        authPreparedStatement.setString(2, username);
                        authPreparedStatement.setString(3, hashedPassword);
                        authPreparedStatement.setString(4, salt);
                        authPreparedStatement.executeUpdate();

                        JOptionPane.showMessageDialog(this, "Client added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add client.", "Error", JOptionPane.ERROR_MESSAGE);
                    }//end if-else

                } catch (SQLException | NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to add client. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }//end of try catch
            });
            buttonPanel.add(submitButton);

            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                Reservation_idRequest reservation_idRequest = new Reservation_idRequest(true);
                reservation_idRequest.setVisible(true);
                this.dispose();
            });
            backButtonPanel.add(backButton);

            // Create a new panel to hold both buttonPanel and backButtonPanel
            JPanel southPanel = new JPanel();
            southPanel.setLayout(new BorderLayout());

            // Add both panels to the new panel
            southPanel.add(buttonPanel, BorderLayout.CENTER);
            southPanel.add(backButtonPanel, BorderLayout.SOUTH);

            // Add the new panel to the main panel
            mainPanel.add(southPanel, BorderLayout.SOUTH);

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
            nameField.addActionListener(e -> {
                String userName = nameField.getText().trim();
                if (!userName.isEmpty()) {
                    // Add code for going to choose a country
                    CreateSimulation_selectCountry CreateSimulation_selectCountry = new CreateSimulation_selectCountry(userName,true);
                    CreateSimulation_selectCountry.setVisible(true);
                    this.dispose();
                    System.out.println("User name: " + userName);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                }//end if-else
            });
            namePanel.add(nameField);

            mainPanel.add(namePanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton reservationButton = new JButton("Next");
            reservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            reservationButton.addActionListener(e -> {
                String userName = nameField.getText().trim();
                if (!userName.isEmpty()) {
                    // Add code for going to choose a country
                    CreateSimulation_selectCountry CreateSimulation_selectCountry = new CreateSimulation_selectCountry(userName,true);
                    CreateSimulation_selectCountry.setVisible(true);
                    this.dispose();
                    System.out.println("User name: " + userName);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
                }//end if-else
            });
            buttonPanel.add(reservationButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                GuestWindow GuestWindow = new GuestWindow();
                GuestWindow.setVisible(true);
                this.dispose();
            });

            buttonPanel.add(backButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end CreateSimulation_nameRequest
    }//end class CreateSimulation_nameRequest

    class CreateSimulation_selectCountry extends JFrame {
        private static JComboBox<String> countryComboBox;

        public CreateSimulation_selectCountry(String userName, boolean isGuest) {
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
            countryComboBox.setPreferredSize(new Dimension(250, 30)); // Set preferred size
            countryComboBox.addActionListener(e -> {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                System.out.println("Current Country: " + comboBox.getSelectedItem());
            });

            // Create a new JPanel with FlowLayout for the dropdown selector
            JPanel dropdownPanel = new JPanel(new FlowLayout());
            dropdownPanel.setBackground(Color.DARK_GRAY);
            dropdownPanel.add(countryComboBox);

            mainPanel.add(dropdownPanel, BorderLayout.CENTER); // Add dropdownPanel instead of countryComboBox

            // Add a "Next" button to move to another window
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton nextButton = new JButton("Next");
            nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
            nextButton.addActionListener(e -> {
                if (countryComboBox.getSelectedIndex() != -1) {
                    String selectedCountry = (String) countryComboBox.getSelectedItem();
                    System.out.println("Selected Country: " + selectedCountry);

                    // Add code for going to general data of simulation
                    CreateSimulation_generalData CreateSimulation_generalData = new CreateSimulation_generalData(userName, isGuest);
                    CreateSimulation_generalData.setVisible(true);
                    this.dispose();

                } else {
                    JOptionPane.showMessageDialog(this, "Please select a country", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window for client or guest
                if (isGuest){
                    CreateSimulation_nameRequest CreateSimulation_nameRequest = new CreateSimulation_nameRequest();
                    CreateSimulation_nameRequest.setVisible(true);
                    this.dispose();
                } else {
                    ClientOptionsWindow ClientOptionsWindow = new ClientOptionsWindow(userName);
                    ClientOptionsWindow.setVisible(true);
                    this.dispose();
                }//end if-else
            });

            buttonPanel.add(nextButton);
            buttonPanel.add(backButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(mainPanel);
        }//end CreateSimulation_selectCountry
    }//end class CreateSimulation_selectCountry

    static class NumberedCheckBox extends JCheckBox {
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
        private final DatePicker returnDatePicker;
        private final DatePicker departureDatePicker;

        private final JSpinner travelersSpinner;
        public CreateSimulation_generalData(String userName, boolean isGuest) {
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

            gbc.gridy = 0;
            gbc.insets = new Insets(5, 5, 5, 5);

            DatePickerSettings departureDatePickerSettings = new DatePickerSettings();
            departureDatePickerSettings.setFontInvalidDate(new Font("Arial", Font.PLAIN, 18));
            departureDatePicker = new DatePicker(departureDatePickerSettings);
            departureDatePicker.setDateToToday();
            gbc.gridx = 1;
            formPanel.add(departureDatePicker, gbc);

            gbc.gridx = 0;
            JLabel departureDateLabel = new JLabel("Departure Date:");
            departureDateLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            departureDateLabel.setForeground(Color.WHITE);
            formPanel.add(departureDateLabel, gbc);


            gbc.gridy = 1;
            JLabel arrivalDateLabel = new JLabel("Return Date:");
            arrivalDateLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            arrivalDateLabel.setForeground(Color.WHITE);
            formPanel.add(arrivalDateLabel, gbc);

            DatePickerSettings returnDatePickerSettings = new DatePickerSettings();
            returnDatePickerSettings.setFontInvalidDate(new Font("Arial", Font.PLAIN, 18));
            returnDatePicker = new DatePicker(returnDatePickerSettings);
            returnDatePicker.setDateToToday();
            gbc.gridx = 1;
            formPanel.add(returnDatePicker, gbc);

            // Disable the date pickers by default
            departureDatePicker.setEnabled(false);
            returnDatePicker.setEnabled(false);

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

                    // Update valid dates for departure and return date pickers
                    List<NumberedCheckBox> sortedSelectedCheckBoxes = cityCheckBoxes.stream()
                            .filter(NumberedCheckBox::isSelected)
                            .sorted(Comparator.comparingInt(NumberedCheckBox::getNumber))
                            .toList();

                    if (!sortedSelectedCheckBoxes.isEmpty()) {
                        // Enable the date pickers
                        departureDatePicker.setEnabled(true);
                        returnDatePicker.setEnabled(true);

                        Set<LocalDate> validDepartureDates = getValidDepartureDates(sortedSelectedCheckBoxes.get(0).getText().substring(sortedSelectedCheckBoxes.get(0).getText().indexOf(" - ") + 3));
                        departureDatePickerSettings.setVetoPolicy(validDepartureDates::contains);

                        Set<LocalDate> validReturnDates = getValidReturnDates(sortedSelectedCheckBoxes.get(sortedSelectedCheckBoxes.size() - 1).getText().substring(sortedSelectedCheckBoxes.get(sortedSelectedCheckBoxes.size() - 1).getText().indexOf(" - ") + 3), departureDatePicker.getDate());
                        returnDatePickerSettings.setVetoPolicy(date -> validReturnDates.contains(date) && !date.isBefore(departureDatePicker.getDate()));
                    } else {
                        // Disable the date pickers
                        departureDatePicker.setEnabled(false);
                        returnDatePicker.setEnabled(false);

                        departureDatePickerSettings.setVetoPolicy(null);
                        returnDatePickerSettings.setVetoPolicy(null);
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
                this.dispose();
                CreateSimulation_selectCountry selectCountryFrame = new CreateSimulation_selectCountry(userName, isGuest);
                selectCountryFrame.setVisible(true);
            });
            buttonPanel.add(backButton);

            JButton finishButton = new JButton("Finish");
            finishButton.setFont(new Font("Arial", Font.PLAIN, 18));
            finishButton.addActionListener(e -> {
                // Get the values from the form and the selected cities

                java.sql.Date arrivalDate = java.sql.Date.valueOf(returnDatePicker.getDate().atStartOfDay(ZoneId.systemDefault()).toLocalDate());
                java.sql.Date departureDate = java.sql.Date.valueOf(departureDatePicker.getDate().atStartOfDay(ZoneId.systemDefault()).toLocalDate());

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

                String[] selectedCircuitsIdsArray = new String[selectedCities.length];
                // Move to Available circuits
                int[] daysPerCity = new int[selectedCities.length];
                CreateSimulation_selectCircuits CreateSimulation_selectCircuits = new CreateSimulation_selectCircuits(isGuest, selectedCities, arrivalDate, departureDate, 0, selectedCircuitsIdsArray, true, userName, selectedCountry, numTravelers, departureDate, daysPerCity);
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

        private Set<LocalDate> getValidDepartureDates(String departureCity) {
            Set<LocalDate> validDates = new HashSet<>();
            try {
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT departure_date FROM datecircuit JOIN circuit ON datecircuit.circuit_id = circuit.circuit_id WHERE departing_city = '" + departureCity + "'");
                while (resultSet.next()) {
                    validDates.add(resultSet.getDate("departure_date").toLocalDate());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return validDates;
        }// end getValidDepartureDates

        private Set<LocalDate> getValidReturnDates(String arrivalCity, LocalDate minArrivalDate) {
            Set<LocalDate> validDates = new HashSet<>();
            try {
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT arrival_date FROM datecircuit JOIN circuit ON datecircuit.circuit_id = circuit.circuit_id WHERE arrival_city = '" + arrivalCity + "' AND arrival_date >= '" + minArrivalDate + "'");
                while (resultSet.next()) {
                    LocalDate arrivalDate = resultSet.getDate("arrival_date").toLocalDate();
                    validDates.add(arrivalDate);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return validDates;
        }

    }// end class CreateSimulation_generalData

    class CreateSimulation_selectHotels extends JFrame {
        String hotelSelected = "";

        public CreateSimulation_selectHotels(boolean isGuest, String[] selectedCities, int hotels, String[] selectedHotels, String[] selectedCircuits, String userName, String country, Date departureDate, Date arrivalDate, int travelers, int[] daysPerCity) {
            List<JCheckBox> checkBoxes = new ArrayList<>();

            setTitle("Select Hotels:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(600, 400);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Select Hotels:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel formPanel = new JPanel();
            formPanel.setLayout(new GridBagLayout());
            formPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            String city = selectedCities[hotels];

            gbc.gridy = hotels;
            JLabel cityPairLabel = new JLabel(city);
            cityPairLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            cityPairLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            formPanel.add(cityPairLabel, gbc);

            JPanel hotelPanel = new JPanel();
            hotelPanel.setLayout(new BoxLayout(hotelPanel, BoxLayout.Y_AXIS));
            gbc.gridx = 1;
            formPanel.add(hotelPanel, gbc);

            ButtonGroup buttonGroup = new ButtonGroup();

            try {
                ResultSet resultSet = conn.createStatement().executeQuery(
                        "SELECT hotel_Name, room_cost, breakfast_cost FROM Hotel  WHERE (city_name = '" + city + "')"
                );


                while (resultSet.next()) {
                    String hotelName = resultSet.getString("hotel_Name");
                    double roomCost = resultSet.getDouble("room_cost");
                    double breakfastCost = resultSet.getDouble("room_cost");


                    String checkBoxText = hotelName + ", $" + roomCost + "per room, $" + breakfastCost + " per breakfast";
                    JCheckBox checkBox = new JCheckBox(checkBoxText);
                    checkBox.setFont(new Font("Arial", Font.PLAIN, 18));
                    checkBox.setForeground(Color.WHITE);
                    checkBox.setBackground(Color.DARK_GRAY);
                    checkBoxes.add(checkBox);
                    buttonGroup.add(checkBox);

                    checkBox.addActionListener(e -> {
                        if (checkBox.isSelected()) {
                            hotelSelected = hotelName;
                        }
                    });

                    hotelPanel.add(checkBox);

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            //llamarse recursivamente
            if (hotels != selectedCities.length - 1) {
                JButton nextButton = new JButton("Next");
                nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
                nextButton.setMargin(new Insets(1, 1, 1, 1));
                nextButton.addActionListener(e -> {
                    selectedHotels[hotels] = hotelSelected;
                    CreateSimulation_selectHotels CreateSimulation_selectHotels = new CreateSimulation_selectHotels(isGuest,selectedCities, hotels + 1, selectedHotels, selectedCircuits, userName, country, departureDate, arrivalDate, travelers, daysPerCity);
                    CreateSimulation_selectHotels.setVisible(true);
                    this.dispose();
                });
                hotelPanel.add(nextButton);
            }


            mainPanel.add(formPanel, BorderLayout.CENTER);

            // Panel with "Back" and "Confirm" buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            // "Back" button
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                CreateSimulation_generalData CreateSimulation_generalData = new CreateSimulation_generalData(userName, true);
                CreateSimulation_generalData.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(backButton);

            if (hotels == selectedCities.length - 1) {
                // "Confirm" button
                JButton confirmButton = new JButton("Confirm");
                confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
                confirmButton.addActionListener(e -> {
                    selectedHotels[hotels] = hotelSelected;
                    //confirmation Window
                    ConfirmSimulation confirmSimulation = new ConfirmSimulation(isGuest,userName, country,departureDate,arrivalDate, travelers, selectedCities, selectedCircuits, selectedHotels, daysPerCity);
                    confirmSimulation.setVisible(true);
                    this.dispose();
                });
                buttonPanel.add(confirmButton);
            }
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(mainPanel);
            this.dispose();

        }//end class CreateSimulation_selectHotels
    }//end class CreateSimulation_selectHotels
    
    class CreateSimulation_selectCircuits extends JFrame {
        private String selectedCircuitIds = "";
        private int days = 0;

        public CreateSimulation_selectCircuits(boolean isGuest,String [] selectedCities, Date arrivalDate, Date departureDate, int circuits, String[] selectedCircuitsIdsArray, boolean initialCity, String userName, String selectedCountry, int travelers, Date variableDate, int[] daysPerCity) {
            // Initialize lists
            List<JCheckBox> checkBoxes = new ArrayList<>();

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

            Date[] departureDateCircuit = {new Date(variableDate.getTime())};
            System.out.println(departureDateCircuit[0]);

            String departingCity;
            String arrivalCity;
            if (initialCity) {
                departingCity = selectedCities[circuits];
                arrivalCity = selectedCities[circuits];
            } else {
                departingCity = selectedCities[circuits];
                arrivalCity = selectedCities[circuits + 1];
            }


            gbc.gridy = circuits;
            String circuitText = departingCity + " - " + arrivalCity + ":";
            JLabel cityPairLabel = new JLabel(circuitText);
            cityPairLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            cityPairLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            formPanel.add(cityPairLabel, gbc);

            JPanel circuitPanel = new JPanel();
            circuitPanel.setLayout(new BoxLayout(circuitPanel, BoxLayout.Y_AXIS));
            gbc.gridx = 1;
            formPanel.add(circuitPanel, gbc);

            ButtonGroup buttonGroup = new ButtonGroup();

            try {
                ResultSet resultSet = conn.createStatement().executeQuery(
                        "SELECT Circuit.circuit_id, description,departure_date,trip_duration,cost FROM circuit, datecircuit WHERE (circuit.circuit_id=datecircuit.circuit_id AND departing_city = '" + departingCity + "' AND arrival_city = '" + arrivalCity + "' AND departure_date= '" + departureDateCircuit[0] + "')");
                System.out.println(departureDateCircuit[0]);
                while (resultSet.next()) {
                    String circuitId = resultSet.getString("circuit_id");
                    String description = resultSet.getString("description");
                    Date date = resultSet.getDate("departure_date");
                    int tripDuration = resultSet.getInt("trip_duration");
                    double cost = resultSet.getDouble("cost");

                    if (initialCity) {
                        daysPerCity[circuits] = tripDuration;
                    }
                    if (!initialCity) {
                        daysPerCity[circuits+1] = tripDuration;
                    }

                    String checkBoxText = description + ", " + date + ", " + tripDuration + " days, $" + cost;
                    JCheckBox checkBox = new JCheckBox(checkBoxText);
                    checkBox.setFont(new Font("Arial", Font.PLAIN, 18));
                    checkBox.setForeground(Color.WHITE);
                    checkBox.setBackground(Color.DARK_GRAY);
                    checkBoxes.add(checkBox);
                    buttonGroup.add(checkBox);

                    checkBox.addActionListener(e -> {
                        if (checkBox.isSelected()) {
                            selectedCircuitIds = circuitId;
                            days = tripDuration;
                        }
                    });

                    checkBox.addActionListener(e -> {
                        if (checkBox.isSelected()) {
                            // Actualizar la variable departureDateCircuit[0]
                            departureDateCircuit[0] = new Date(date.getTime() + tripDuration * 86400000L); // tripDuration en días, 86400000L es el número de milisegundos en un día
                        }
                    });

                    circuitPanel.add(checkBox);

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            if (selectedCities.length==2) {
                if (initialCity) {
                    JButton nextButton = new JButton("Next");
                    nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
                    nextButton.setMargin(new Insets(1, 1, 1, 1));
                    nextButton.setEnabled(false);
                    nextButton.addActionListener(e -> {

                        selectedCircuitsIdsArray[circuits] = selectedCircuitIds;
                        daysPerCity[circuits] = days;

                        CreateSimulation_selectCircuits CreateSimulation_selectCircuits;
                        CreateSimulation_selectCircuits = new CreateSimulation_selectCircuits(isGuest, selectedCities, arrivalDate, departureDate, circuits, selectedCircuitsIdsArray, false, userName, selectedCountry, travelers, departureDateCircuit[0], daysPerCity);
                        CreateSimulation_selectCircuits.setVisible(true);
                        this.dispose();
                    });
                    buttonPanel.add(nextButton);

                    ActionListener circuitSelectedListener = e -> {
                        boolean anyCircuitSelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
                        nextButton.setEnabled(anyCircuitSelected);
                    };

                    for (JCheckBox checkBox : checkBoxes) {
                        checkBox.addActionListener(circuitSelectedListener);
                    }
                }
            } //end if
            if (selectedCities.length>2) {
                if (circuits != selectedCities.length - 2) {
                    JButton nextButton = new JButton("Next");
                    nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
                    nextButton.setMargin(new Insets(1, 1, 1, 1));
                    nextButton.setEnabled(false);
                    nextButton.addActionListener(e -> {

                        if (initialCity) {
                            selectedCircuitsIdsArray[circuits] = selectedCircuitIds;
                            daysPerCity[circuits] = days;
                            //System.out.println(selectedCircuitIds);
                            //System.out.println(selectedCircuitsIdsArray[circuits]);
                        }
                        if (!initialCity) {
                            selectedCircuitsIdsArray[circuits+1] = selectedCircuitIds;
                            daysPerCity[circuits+1] = days;
                            //System.out.println(selectedCircuitIds);
                            //System.out.println(selectedCircuitsIdsArray[circuits+1]);
                        }

                        CreateSimulation_selectCircuits CreateSimulation_selectCircuits;
                        if (initialCity) {

                            CreateSimulation_selectCircuits = new CreateSimulation_selectCircuits(isGuest, selectedCities, arrivalDate, departureDate, circuits, selectedCircuitsIdsArray, false, userName, selectedCountry, travelers, departureDateCircuit[0], daysPerCity);
                        } else {
                            CreateSimulation_selectCircuits = new CreateSimulation_selectCircuits(isGuest, selectedCities, arrivalDate, departureDate, circuits + 1, selectedCircuitsIdsArray, false, userName, selectedCountry, travelers,  departureDateCircuit[0], daysPerCity);
                        }
                        CreateSimulation_selectCircuits.setVisible(true);
                        this.dispose();
                    });
                    buttonPanel.add(nextButton);

                    ActionListener circuitSelectedListener = e -> {
                        boolean anyCircuitSelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
                        nextButton.setEnabled(anyCircuitSelected);
                    };

                    for (JCheckBox checkBox : checkBoxes) {
                        checkBox.addActionListener(circuitSelectedListener);
                    }
                }
            } //end if

            mainPanel.add(formPanel, BorderLayout.CENTER);

            // "Back" button
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                CreateSimulation_generalData CreateSimulation_generalData = new CreateSimulation_generalData(userName, true);
                CreateSimulation_generalData.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(backButton);

            if (selectedCities.length==1) {
                // "Confirm" button
                JButton confirmButton = new JButton("Confirm");
                confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
                confirmButton.setEnabled(false);
                confirmButton.addActionListener(e -> {
                    // Hotel Window
                    String[] selectedHotels = new String[selectedCities.length];
                    selectedCircuitsIdsArray[circuits] = selectedCircuitIds;
                    daysPerCity[circuits] = days;
                    CreateSimulation_selectHotels CreateSimulation_selectHotels = new CreateSimulation_selectHotels(isGuest, selectedCities, 0, selectedHotels, selectedCircuitsIdsArray, userName, selectedCountry, departureDate, departureDateCircuit[0], travelers, daysPerCity);
                    CreateSimulation_selectHotels.setVisible(true);
                    this.dispose();
                });
                buttonPanel.add(confirmButton);

                ActionListener circuitSelectedListener = e -> {
                    boolean anyCircuitSelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
                    confirmButton.setEnabled(anyCircuitSelected);
                };

                for (JCheckBox checkBox : checkBoxes) {
                    checkBox.addActionListener(circuitSelectedListener);
                }
            }
            if (selectedCities.length==2&&!initialCity) {
                JButton confirmButton = new JButton("Confirm");
                confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
                confirmButton.setEnabled(false);
                confirmButton.addActionListener(e -> {
                    // Hotel Window
                    String[] selectedHotels = new String[selectedCities.length];
                    selectedCircuitsIdsArray[circuits+1] = selectedCircuitIds;
                    daysPerCity[circuits+1] = days;
                    CreateSimulation_selectHotels CreateSimulation_selectHotels = new CreateSimulation_selectHotels(isGuest, selectedCities, 0, selectedHotels, selectedCircuitsIdsArray, userName, selectedCountry, departureDate, departureDateCircuit[0], travelers, daysPerCity);
                    CreateSimulation_selectHotels.setVisible(true);
                    this.dispose();
                });
                buttonPanel.add(confirmButton);

                ActionListener circuitSelectedListener = e -> {
                    boolean anyCircuitSelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
                    confirmButton.setEnabled(anyCircuitSelected);
                };

                for (JCheckBox checkBox : checkBoxes) {
                    checkBox.addActionListener(circuitSelectedListener);
                }
            }
            if (selectedCities.length>2) {
                if (circuits == selectedCities.length-2) {
                    JButton confirmButton = new JButton("Confirm");
                    confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
                    confirmButton.setEnabled(false);
                    confirmButton.addActionListener(e -> {
                        // Hotel Window
                        String[] selectedHotels = new String[selectedCities.length];
                        selectedCircuitsIdsArray[circuits+1] = selectedCircuitIds;
                        daysPerCity[circuits+1] = days;
                        CreateSimulation_selectHotels CreateSimulation_selectHotels = new CreateSimulation_selectHotels(isGuest, selectedCities, 0, selectedHotels, selectedCircuitsIdsArray, userName, selectedCountry, departureDate, departureDateCircuit[0], travelers, daysPerCity);
                        CreateSimulation_selectHotels.setVisible(true);
                        this.dispose();
                    });
                    buttonPanel.add(confirmButton);
                    ActionListener circuitSelectedListener = e -> {
                        boolean anyCircuitSelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
                        confirmButton.setEnabled(anyCircuitSelected);
                    };

                    for (JCheckBox checkBox : checkBoxes) {
                        checkBox.addActionListener(circuitSelectedListener);
                    }
                }
            }

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(mainPanel);
            this.dispose();
        }// end CreateSimulation_selectCircuits

    }// end class CreateSimulation_selectCircuits

    class ConfirmSimulation extends JFrame {

        public ConfirmSimulation(boolean isGuest, String userName, String country, Date departureDate, Date arrivalDate, int travelers, String[] selectedCities,String[] selectedCircuits,String[] selectedHotels, int[] daysPerCity) {
            setTitle("Confirm simulation:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(600, 400);
            setResizable(true);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Confirm your simulation:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            // Replace the existing summaryPanel with a new JPanel
            JPanel summaryPanel = new JPanel();
            summaryPanel.setLayout(new GridBagLayout());
            summaryPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            mainPanel.add(summaryPanel, BorderLayout.CENTER);

            int i = 0;
            gbc.gridy = i;
            JLabel nameLabel = new JLabel("The simulation is at the name of: "+userName);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            nameLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            summaryPanel.add(nameLabel, gbc);
            i++;

            gbc.gridy = i;
            JLabel countryLabel = new JLabel("You will travel to: "+country);
            countryLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            countryLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            summaryPanel.add(countryLabel, gbc);
            i++;

            gbc.gridy = i;
            JLabel peopleLabel = new JLabel("Number of travelers: " + travelers);
            peopleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            peopleLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            summaryPanel.add(peopleLabel, gbc);
            i++;

            gbc.gridy = i;
            JLabel departureLabel = new JLabel("Your journey start date: " + departureDate);
            departureLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            departureLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            summaryPanel.add(departureLabel, gbc);
            i++;

            gbc.gridy = i;
            JLabel arrivalLabel = new JLabel("Your journey finish date : " + arrivalDate);
            arrivalLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            arrivalLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            summaryPanel.add(arrivalLabel, gbc);
            i++;

            for (int j = 0; j<selectedCities.length;j++) {
                gbc.gridy = i;
                JLabel city = new JLabel("The city number " + j + " is: " + selectedCities[j]);
                city.setFont(new Font("Arial", Font.PLAIN, 18));
                city.setForeground(Color.WHITE);
                gbc.gridx = 0;
                summaryPanel.add(city, gbc);
                i++;
            }

            double totalCost = 0.0;
            for (int j = 0; j<selectedCircuits.length;j++) {

                try {

                    //executeQuery("SELECT departing_city, arrival_city, cost FROM Circuit WHERE circuit_id = '"+selectedCircuits[j]+"'");
                    String departingCity;
                    String arrivalCity;
                    try (ResultSet resultSet = queryCircuit(selectedCircuits[j])) {
                        if (resultSet.next()) {
                            gbc.gridy = i;
                            departingCity = resultSet.getString("departing_city");
                            arrivalCity = resultSet.getString("arrival_city");
                            totalCost = totalCost + (resultSet.getDouble("cost") * travelers);
                            JLabel cityLabel = new JLabel("The circuit number " + j + " will visit: " + selectedCircuits[j] + ":" + departingCity + "-" + arrivalCity);
                            cityLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                            cityLabel.setForeground(Color.WHITE);
                            gbc.gridx = 0;
                            summaryPanel.add(cityLabel, gbc);
                            i++;
                        }
                    }

                } catch (SQLException e) {
                        e.printStackTrace();
                    }

            }

            for (int j = 0; j<selectedHotels.length;j++) {

                try {
                    //executeQuery("SELECT room_cost, breakfast_cost FROM Hotel WHERE hotel_name = '"+selectedHotels[j]+"'");
                    try (ResultSet resultSet = queryHotel(selectedHotels[j])) {
                        if (resultSet.next()) {
                            gbc.gridy = i;
                            Double roomCost = resultSet.getDouble("room_cost");
                            Double breakfastCost = resultSet.getDouble("breakfast_cost");
                            totalCost = totalCost + ((roomCost + breakfastCost) * travelers);
                            JLabel hotelLabel = new JLabel("The hotel number " + j + " you will stay in is: " + selectedHotels[j] + " for " + daysPerCity[j] + " days");
                            hotelLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                            hotelLabel.setForeground(Color.WHITE);
                            gbc.gridx = 0;
                            summaryPanel.add(hotelLabel, gbc);
                            i++;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            gbc.gridy = i;
            JLabel costPanel = new JLabel("Your travel will cost: $" + totalCost);
            costPanel.setFont(new Font("Arial", Font.PLAIN, 18));
            costPanel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            summaryPanel.add(costPanel, gbc);

            // Panel with "Back" and "Confirm" buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);

            // "Back" button
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                CreateSimulation_generalData CreateSimulation_generalData = new CreateSimulation_generalData(userName, true);
                CreateSimulation_generalData.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(backButton);

            // "Confirm" button

            JButton confirmButton = new JButton("Confirm");
            confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
            double finalTotalCost = totalCost;            confirmButton.addActionListener(e -> {
                FinishSimulation finishSimulation = new FinishSimulation(isGuest, departureDate, arrivalDate, travelers, selectedCities, selectedCircuits, daysPerCity, finalTotalCost);
                finishSimulation.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(confirmButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            mainPanel.add(summaryPanel, BorderLayout.CENTER);
            add(mainPanel);
        } //end confirmSimulation
        /**d
         * Run a query against the database to get information about a specific circuit.
         * @param selectedCircuit The identifier of the selected circuit.
         * @return The result of the query as a ResultSet object.
         * This method is responsible for executing a query in the database using the identifier of a selected circuit as a parameter.
         * The query seeks to obtain information about the city of departure, city of arrival and cost of the circuit
         */
        private ResultSet queryCircuit (String selectedCircuit) {
            ResultSet resultSet = null;
               try {

                     resultSet = conn.createStatement().executeQuery(
                            "SELECT departing_city, arrival_city, cost FROM Circuit WHERE circuit_id = '"+selectedCircuit+"'"
               );
               }catch (SQLException e) {
                    e.printStackTrace();
               }
               return resultSet;
        }
        /**
         * Run a database query to get information about a specific hotel
         * @param selectedHotel The name of the selected hotel.
         * @return The result of the query as a ResultSet object.
         * The query seeks to obtain information on the cost of the rooms and the cost of breakfast in that hotel.
         */
        private ResultSet queryHotel (String selectedHotel) {
            ResultSet resultSet = null;
            try {

                 resultSet = conn.createStatement().executeQuery(
                        "SELECT room_cost, breakfast_cost FROM Hotel WHERE hotel_name = '"+selectedHotel+"'"
                );
            }catch (SQLException e) {
                e.printStackTrace();
            }
            return resultSet;
        }

    }

    class FinishSimulation extends JFrame {
        public FinishSimulation (boolean isGuest, String userName, Date departureDate,Date arrivalDate,int travelers, String[] selectedCircuits, String[] selectedHotels, int[] daysPerCity, double cost) {
            try {
                ResultSet resultSet = conn.createStatement().executeQuery(
                        "SELECT client_name FROM Client WHERE client_name='"+userName+"'");
                if (!resultSet.next()) {
                    try (Connection connection = getConnection()) {
                        if (connection == null) {
                            System.err.println("Failed to establish a connection to the database.");
                            return;
                        }//end if
                        String simulationPotentialClient = "INSERT INTO PotentialClient(client_name) VALUES (?)";
                        PreparedStatement potentialClientPreparedStatement = connection.prepareStatement(simulationPotentialClient, Statement.RETURN_GENERATED_KEYS);
                        potentialClientPreparedStatement.setString(1,userName);
                        int rowsAffected = potentialClientPreparedStatement.executeUpdate();
                        if (rowsAffected == 0) {
                            JOptionPane.showMessageDialog(this, "Failed to add client.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }//end if
                    } catch (SQLException e)   {
                        throw new RuntimeException(e);
                    }
                } //end if

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            int simulationId;
            int client_id = NULL;

            if (!isGuest){
                String client = "NULL";
                String query = "SELECT client.client_id FROM Client, clientauth WHERE client.client_id = clientauth.client_id AND client.client_name = '" + userName + "'";
                try (Connection connection = getConnection()) {
                    if (connection == null) {
                        System.err.println("Failed to establish a connection to the database.");
                    }

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    if (resultSet.next()) {
                        client = resultSet.getString("client.client_id");
                    }
                } catch (SQLException f) {
                    f.printStackTrace();
                }
                client_id = Integer.parseInt(client);
            }

            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                    return;
                }//end if
                Date today = new Date(System.currentTimeMillis());
                String simulationQuery = "INSERT INTO Simulation (client_name, client_id, departure_date, arrival_date, num_people, cost, simulation_created_at, is_reservation) VALUES ( ?, ?, ?, ?, ?, ?, ?,?)";
                PreparedStatement queryPreparedStatement = connection.prepareStatement(simulationQuery, Statement.RETURN_GENERATED_KEYS);
                queryPreparedStatement.setString(1, userName);
                queryPreparedStatement.setInt(2, client_id);
                queryPreparedStatement.setDate(3, departureDate);
                queryPreparedStatement.setDate(4, arrivalDate);
                queryPreparedStatement.setInt(5, travelers);
                queryPreparedStatement.setInt(6, (int) cost);
                queryPreparedStatement.setDate(7, today);
                queryPreparedStatement.setBoolean(8, false);
                int rowsAffected = queryPreparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    JOptionPane.showMessageDialog(this, "Failed to add client.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    // Retrieve the auto increment ID
                    try (ResultSet generatedKeys = queryPreparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            simulationId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Failed to retrieve the generated simulation ID.");
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            //add tuple in SimulationCircuit
            for (String selectedCircuit : selectedCircuits) {
                try (Connection connection = getConnection()) {
                    if (connection == null) {
                        System.err.println("Failed to establish a connection to the database.");
                        return;
                    }//end if
                    Date today = new Date(System.currentTimeMillis());
                    String simulationCircuitQuery = "INSERT INTO SimulationCircuit (simulation_id, circuit_id) VALUES (?, ?)";
                    PreparedStatement queryPreparedStatement = connection.prepareStatement(simulationCircuitQuery, Statement.RETURN_GENERATED_KEYS);
                    queryPreparedStatement.setInt(1, simulationId);
                    queryPreparedStatement.setString(2, selectedCircuit);
                    int rowsAffected = queryPreparedStatement.executeUpdate();

                    if (rowsAffected == 0) {
                        JOptionPane.showMessageDialog(this, "Failed to add client.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }//end if
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } //end for

            Date departureFromHotel = departureDate;
            Date arrivalAtHotel = departureDate;

            for (int i = 0; i<selectedHotels.length;i++){
                try (Connection connection = getConnection()) {
                    if (connection == null) {
                        System.err.println("Failed to establish a connection to the database.");
                        return;
                    }//end if

                    departureFromHotel = arrivalAtHotel;
                    arrivalAtHotel = new Date(departureFromHotel.getTime() + daysPerCity[i] * 86400000L);

                    Date today = new Date(System.currentTimeMillis());
                    String simulationHotelQuery = "INSERT INTO SimulationHotel (simulation_id, hotel_name, departure_date, arrival_date) VALUES (?,?,?,?)";
                    PreparedStatement queryPreparedStatement = connection.prepareStatement(simulationHotelQuery, Statement.RETURN_GENERATED_KEYS);
                    queryPreparedStatement.setInt(1, simulationId);
                    queryPreparedStatement.setString(2, selectedHotels[i]);
                    queryPreparedStatement.setDate(3, departureFromHotel);
                    queryPreparedStatement.setDate(4, arrivalAtHotel);
                    int rowsAffected = queryPreparedStatement.executeUpdate();

                    if (rowsAffected == 0) {
                        JOptionPane.showMessageDialog(this, "Failed to add client.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }//end if

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } //end for

            ShowSimulationIdWindow showSimulationIdWindow = new ShowSimulationIdWindow(userName, simulationId);
            showSimulationIdWindow.setVisible(true);
            this.dispose();
        }

        public FinishSimulation(boolean isGuest, Date departureDate, Date arrivalDate, int travelers, String[] selectedCities, String[] selectedCircuits, int[] daysPerCity, double finalTotalCost) {
        }
    }

    static class ShowSimulationIdWindow extends JFrame{
        public ShowSimulationIdWindow (String name, int simulationId) {
            setTitle("Confirmed simulation:");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(600, 400);
            setResizable(true);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("You have confirmed your simulation:");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            // Replace the existing summaryPanel with a new JPanel
            JPanel simulationPanel = new JPanel();
            simulationPanel.setLayout(new GridBagLayout());
            simulationPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            mainPanel.add(simulationPanel, BorderLayout.CENTER);

            gbc.gridy = 0;
            JLabel simulationIdLabel = new JLabel(name + " this is your simulation Id: " + simulationId);
            simulationIdLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            simulationIdLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            simulationPanel.add(simulationIdLabel, gbc);

            gbc.gridy = 1;
            JLabel considerationLabel = new JLabel("If you forget it you can check it in the make reservation window");
            considerationLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            considerationLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            simulationPanel.add(considerationLabel, gbc);

            gbc.gridy = 2;
            JLabel considerationLabel2 = new JLabel("by typing the name you used to make the simulation");
            considerationLabel2.setFont(new Font("Arial", Font.PLAIN, 18));
            considerationLabel2.setForeground(Color.WHITE);
            gbc.gridx = 0;
            simulationPanel.add(considerationLabel2, gbc);

            gbc.gridy = 3;
            JLabel considerationLabel3 = new JLabel("Make your simulation a reality by making it a reservation ");
            considerationLabel3.setFont(new Font("Arial", Font.PLAIN, 18));
            considerationLabel3.setForeground(Color.WHITE);
            gbc.gridx = 0;
            simulationPanel.add(considerationLabel3, gbc);

            gbc.gridy = 4;
            JLabel considerationLabel4 = new JLabel("in the reservation window");
            considerationLabel4.setFont(new Font("Arial", Font.PLAIN, 18));
            considerationLabel4.setForeground(Color.WHITE);
            gbc.gridx = 0;
            simulationPanel.add(considerationLabel4, gbc);


            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(Color.DARK_GRAY);
            
            // "Confirm" button
            JButton confirmButton = new JButton("Confirm");
            confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
            confirmButton.addActionListener(e -> {
                Main goToHomePage = new Main();
                goToHomePage.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(confirmButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            mainPanel.add(simulationPanel, BorderLayout.CENTER);
            add(mainPanel);
        }
    }

    class SelectCountryToViewPamphlet extends JFrame {
        private static JComboBox<String> countryComboBox;

        public SelectCountryToViewPamphlet(String username) {
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
            countryComboBox.setPreferredSize(new Dimension(250, 30)); // Set preferred size
            countryComboBox.addActionListener(e -> {
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                System.out.println("Current Country: " + comboBox.getSelectedItem());
            });

            // Create a new JPanel with FlowLayout for the dropdown selector
            JPanel dropdownPanel = new JPanel(new FlowLayout());
            dropdownPanel.setBackground(Color.DARK_GRAY);
            dropdownPanel.add(countryComboBox);

            mainPanel.add(dropdownPanel, BorderLayout.CENTER); // Add dropdownPanel instead of countryComboBox

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(Color.DARK_GRAY);
            JButton nextButton = new JButton("Next");
            nextButton.setFont(new Font("Arial", Font.PLAIN, 18));
            nextButton.addActionListener(e -> {
                if (countryComboBox.getSelectedIndex() != -1) {
                    String selectedCountry = (String) countryComboBox.getSelectedItem();
                    System.out.println("Selected Country: " + selectedCountry);

                    // Add code for going to next step
                    SelectCityToViewPamphlet SelectCityToViewPamphlet = new SelectCityToViewPamphlet(selectedCountry, username);
                    SelectCityToViewPamphlet.setVisible(true);
                    this.dispose();

                } else {
                    JOptionPane.showMessageDialog(this, "Please select a country", "Error", JOptionPane.ERROR_MESSAGE);
                }//end if-else
            });
            buttonPanel.add(nextButton);

            // Back button
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                ClientOptionsWindow ClientOptionsWindow = new ClientOptionsWindow(username);
                ClientOptionsWindow.setVisible(true);
                this.dispose();
            });

            buttonPanel.add(backButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end SelectCountryToViewPamphlet
    }//end class SelectCountryToViewPamphlet
    /**
     * Class that represents a graphical user interface (GUI) for selecting a city and viewing a brochure.
     * Extends the JFrame class.
     */
    class SelectCityToViewPamphlet extends JFrame {
        private String selectedCountry;
        private List<String> cities;
        /**
         * Method that finds and returns a list of cities for a specific country
         * @param searchedCountry The country for which cities will be searched.
         * @return A list of city names found for the country.
         */
        public List<String> searchCities(String searchedCountry) {
            List<String> cities = new ArrayList<>();

            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                    return cities;
                }

                String query = "SELECT city_name FROM city WHERE city.country LIKE '" + searchedCountry + "'";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String city = resultSet.getString("city_name");
                    cities.add(city);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }//
            return cities;
        }

        public SelectCityToViewPamphlet(String selectedCountry, String username) {
            setTitle("Available cities for " + selectedCountry);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Available cities for " + selectedCountry);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            List<String> cities = searchCities(selectedCountry);

            if (cities.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "There are no simulations with the searched name: " + selectedCountry,
                        "No Simulations Found",
                        JOptionPane.WARNING_MESSAGE);
                // Go back to previous window
                SimulationSearch_requestName SimulationSearch_requestName = new SimulationSearch_requestName();
                SimulationSearch_requestName.setVisible(true);
                this.dispose();

            } else {
                JPanel simulationPanel = new JPanel();
                simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));
                simulationPanel.setBackground(Color.DARK_GRAY);

                for (String city : cities) {
                    JPanel singleSimulationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    singleSimulationPanel.setBackground(Color.DARK_GRAY);

                    JLabel simulationLabel = new JLabel("City: " + city);
                    simulationLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                    simulationLabel.setForeground(Color.WHITE);
                    singleSimulationPanel.add(simulationLabel);

                    JButton moreInfoButton = new JButton("Circuits and Hotels");
                    moreInfoButton.setFont(new Font("Arial", Font.PLAIN, 18));
                    moreInfoButton.addActionListener(e -> {
                        // Create and open a new window to show the simulation details
                        SelectCircuitAndHotel SelectCircuitAndHotel = new SelectCircuitAndHotel(city, selectedCountry, username);
                        SelectCircuitAndHotel.setVisible(true);
                        this.dispose();
                    });
                    singleSimulationPanel.add(moreInfoButton);

                    simulationPanel.add(singleSimulationPanel);
                }

                JScrollPane scrollPane = new JScrollPane(simulationPanel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                mainPanel.add(scrollPane, BorderLayout.CENTER);
            }

            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                SelectCountryToViewPamphlet SelectCountryToViewPamphlet = new SelectCountryToViewPamphlet(username);
                SelectCountryToViewPamphlet.setVisible(true);
                this.dispose();
            });
            backButtonPanel.add(backButton);

            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end SelectCityToViewPamphlet
    }//end class SelectCityToViewPamphlet

    class SelectCircuitAndHotel extends JFrame {
        /**
         * Method that finds and returns a list of places for a specific city and circuit
         * @param searchedCities The searched cities for which places will be searched.
         * @param circuit_id The identifier of the circuit for which places will be searched.
         * @return A list of place names found for the cities and circuit.
         */
        public List<String> searchedPlaces(String searchedCities, String circuit_id) {
            List<String> places = new ArrayList<>();

            try (Connection connection = getConnection()) {
                if (connection == null) {
                    System.err.println("Failed to establish a connection to the database.");
                    return places;
                }

                String query = "SELECT place_name FROM stage, circuit WHERE stage.circuit_id = circuit.circuit_id AND circuit.circuit_id " + circuit_id + "' AND  ( departing_city = '" + searchedCities + "' OR arrival_city = '" + searchedCities + "')";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String place = resultSet.getString("city_name");
                    places.add(place);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }//
            return places;
        }

        public SelectCircuitAndHotel(String selectedCity, String selectedCountry, String username) {
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

            JPanel contentPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            contentPanel.setBackground(Color.DARK_GRAY);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            /**
            * Prepares a parameterized query to select circuits from the "Circuit" table that match the specified departure city and departure country.
             * Sets the values of the query parameters with the values of the selectedCity and selectedCountry variables.
             * Executes the query and gets a ResultSet object containing the results.
             * Iterates through the results obtained using the next() method of the ResultSet.
             * On each iteration, it gets the value of the "description" column in the current row and adds it to the list of circuits.
             * In case an exception occurs during query execution, the exception trace is printed using e.printStackTrace
             */
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
                }//end while
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no circuit
            }//end try-catch

            JLabel circuitLabel = new JLabel("Available Circuits: ");
            circuitLabel.setFont(new Font("Arial", Font.BOLD, 12));
            circuitLabel.setForeground(Color.WHITE);
            contentPanel.add(circuitLabel, BorderLayout.SOUTH);

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
                }//end while
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no hotel
            }//end try-catch
            JLabel circuitHotels = new JLabel("Available Hotels: ");
            circuitHotels.setFont(new Font("Arial", Font.BOLD, 12));
            circuitHotels.setForeground(Color.WHITE);
            contentPanel.add(circuitHotels, BorderLayout.SOUTH);

            JList<String> hotelList = new JList<>(hotels.toArray(new String[0]));
            hotelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contentPanel.add(new JScrollPane(hotelList));

            // Query and display circuits based on the selected city and country
            ArrayList<String> places = new ArrayList<>();
            try {
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM placetovisit WHERE city_name = ? AND country = ?");
                pstmt.setString(1, selectedCity);
                pstmt.setString(2, selectedCountry);
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    String place = resultSet.getString("place_name");
                    places.add(place);
                }//end while
            } catch (SQLException e) {
                e.printStackTrace(); // Error case for no circuit
            }//end try-catch

            JLabel placesLabel = new JLabel("Available Places: ");
            placesLabel.setFont(new Font("Arial", Font.BOLD, 12));
            placesLabel.setForeground(Color.WHITE);
            contentPanel.add(placesLabel, BorderLayout.SOUTH);

            JList<String> placesList = new JList<>(places.toArray(new String[0]));
            placesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contentPanel.add(new JScrollPane(placesList));

            mainPanel.add(contentPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout()); // FlowLayout is the default layout for JPanel, so you could also just write new JPanel()
            buttonPanel.setBackground(Color.DARK_GRAY);

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Show the SelectCityToViewPamphlet again
                SelectCityToViewPamphlet selectCityToViewPamphlet = new SelectCityToViewPamphlet(selectedCountry, username);
                selectCityToViewPamphlet.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(backButton);

            // Home button
            JButton homeButton = new JButton("Home");
            homeButton.setFont(new Font("Arial", Font.PLAIN, 18));
            homeButton.addActionListener(e -> {
                ClientOptionsWindow ClientOptionsWindow = new ClientOptionsWindow(username);
                ClientOptionsWindow.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(homeButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end SelectCircuitAndHotel
    }//end class SelectCircuitAndHotel

    class ReservationsFromName extends JFrame {


        public ReservationsFromName(String username, List<String> reservationIds) {
            setTitle("Reservations of " + username);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(400, 250);
            setResizable(false);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.DARK_GRAY);

            JLabel titleLabel = new JLabel("Reservations of " + username);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

                JPanel simulationPanel = new JPanel();
                simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));
                simulationPanel.setBackground(Color.DARK_GRAY);

                for (String reservation : reservationIds) {
                    JPanel singleSimulationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    singleSimulationPanel.setBackground(Color.DARK_GRAY);

                    JLabel simulationLabel = new JLabel("Simulation ID: " + reservation);
                    simulationLabel.setFont(new Font("Arial", Font.PLAIN, 18));
                    simulationLabel.setForeground(Color.WHITE);
                    singleSimulationPanel.add(simulationLabel);

                    JButton moreInfoButton = new JButton("Check the data of your reservation");
                    moreInfoButton.setFont(new Font("Arial", Font.PLAIN, 18));
                    moreInfoButton.addActionListener(e -> {
                        // Create and open a new window to show the simulation details
                        SimulationDetailsWindow simulationDetailsWindow = new SimulationDetailsWindow(reservation);
                        simulationDetailsWindow.setVisible(true);
                    });
                    singleSimulationPanel.add(moreInfoButton);

                    simulationPanel.add(singleSimulationPanel);
                }

                JScrollPane scrollPane = new JScrollPane(simulationPanel);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                mainPanel.add(scrollPane, BorderLayout.CENTER);


            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                // Dispose the current window and return to the previous window
                SimulationSearch_requestName SimulationSearch_requestName = new SimulationSearch_requestName();
                SimulationSearch_requestName.setVisible(true);
                this.dispose();
            });
            backButtonPanel.add(backButton);

            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }// end ReservationsFromName
    }// end class ReservationsFromName

     class ClientOptionsWindow extends JFrame {
         public List<String> ReservationsFromNameList(String username) {
             List<String> reservations = new ArrayList<>();

             try (Connection connection = getConnection()) {
                 if (connection == null) {
                     System.err.println("Failed to establish a connection to the database.");
                     return reservations;
                 }

                 String query = "SELECT reservation_id FROM reservation WHERE client_id LIKE '" + username + "'";
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery();

                 while (resultSet.next()) {
                     String simulationId = resultSet.getString("simulation_id");
                     reservations.add(simulationId);
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
             }

             return reservations;
         }

        public ClientOptionsWindow(String username) {
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
            buttonPanel.setBackground(Color.DARK_GRAY);
            GridBagConstraints gbc = new GridBagConstraints();

            JButton seePamphletButton = new JButton("See pamphlet");
            seePamphletButton.setFont(new Font("Arial", Font.PLAIN, 18));
            seePamphletButton.addActionListener(e -> {
                // Add logic for "See pamphlet" here
                SelectCountryToViewPamphlet selectCountryToViewPamphlet = new SelectCountryToViewPamphlet(username);
                selectCountryToViewPamphlet.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(seePamphletButton, gbc);

            JButton doSimulationButton = new JButton("Do a simulation");
            doSimulationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            doSimulationButton.addActionListener(e -> {
                //Query to get name with username
                String name = "";
                String query = "SELECT client_name FROM Client, clientauth  WHERE username = '" + username + "' AND client.client_id = clientauth.client_id";

                try (Connection connection = getConnection()) {
                    if (connection == null) {
                        System.err.println("Failed to establish a connection to the database.");
                    }
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    if (resultSet.next()) {
                        name = resultSet.getString("client_name");
                    }
                } catch (SQLException f) {
                    f.printStackTrace();
                }

                CreateSimulation_selectCountry  CreateSimulation_selectCountry = new CreateSimulation_selectCountry(name, false);
                CreateSimulation_selectCountry.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(doSimulationButton, gbc);

            JButton doReservationButton = new JButton("Do a reservation");
            doReservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            doReservationButton.addActionListener(e -> {
                // Goes to reservation in guest interfaz

                Reservation_idRequest  Reservation_idRequest = new Reservation_idRequest(false);
                Reservation_idRequest.setVisible(true);
                this.dispose();
            });
            buttonPanel.add(doReservationButton, gbc);

            JButton checkReservationButton = new JButton("Check a reservation");
            checkReservationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            checkReservationButton.addActionListener(e -> {
                List<String> ReservationsList = ReservationsFromNameList(username);
                if (!ReservationsList.isEmpty()) {
                    ReservationsFromName ReservationsFromName = new ReservationsFromName(username, ReservationsList);
                    ReservationsFromName.setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "There are no reservations", "No reservationd", JOptionPane.ERROR_MESSAGE);
                }
            });

            buttonPanel.add(checkReservationButton, BorderLayout.CENTER);

            // Back button
            JPanel backButtonPanel = new JPanel();
            backButtonPanel.setBackground(Color.DARK_GRAY);
            backButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton backButton = new JButton("Back");
            backButton.setFont(new Font("Arial", Font.PLAIN, 18));
            backButton.addActionListener(e -> {
                Main Main = new Main();
                Main.setVisible(true);
                this.dispose();
            });

            backButtonPanel.add(backButton);

            mainPanel.add(buttonPanel, BorderLayout.CENTER);
            mainPanel.add(backButtonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }//end ClientOptionsWindow
    }//end class ClientOptionsWindow
}//end class Main