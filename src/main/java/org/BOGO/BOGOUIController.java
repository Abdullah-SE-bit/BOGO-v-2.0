package org.BOGO;

import javafx.animation.StrokeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Passenger;
import org.BOGO.domain.user.User;
import org.BOGO.service.PathBuildingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BOGOUIController {
    public Label lbl_total_Drivers;
    @FXML private StackPane content_area;
    @FXML private Button btn_nav_dashboard;
    @FXML private Button btn_nav_analytics;
    @FXML private Button btn_nav_alerts;
    @FXML private Button btn_nav_add_resource;
    @FXML private Button btn_nav_send_alert;
    @FXML private Button btn_nav_view_route;
    @FXML private Button btn_nav_bus_metrics;
    @FXML private Button btn_nav_book;
    @FXML private Button btn_nav_multi_stop;

    @FXML private ListView<String> list_system_alerts;
    @FXML private ListView<String> list_employees;
    @FXML private ListView<String> list_drivers;
    @FXML private ListView<String> list_active_alerts;
    @FXML private ListView<String> list_boarding_passengers;
    @FXML private Pane pane_detail_overlay;
    @FXML private Pane pane_resolution_dialog;
    @FXML private Pane pane_cancel_ride;
    @FXML private Label lbl_detail_name;
    @FXML private Label lbl_detail_id;
    @FXML private Label lbl_total_passengers;
    @FXML private Label lbl_total_revenue;
    @FXML private TextField txt_search_employee;
    @FXML private TextField txt_search_driver;
    @FXML private ComboBox<Stop> combo_stop_1;
    @FXML private ComboBox<Stop> combo_stop_2;
    @FXML private ComboBox<String> choice_max_capacity;
    @FXML private ComboBox<Integer> combo_stop_count;
    @FXML private VBox box_dynamic_stops;
    @FXML private ProgressIndicator gauge_tyres;
    @FXML private ProgressIndicator gauge_engine;
    @FXML private ProgressIndicator gauge_chassis;
    @FXML private Label lbl_tyres_percent;
    @FXML private Label lbl_engine_percent;
    @FXML private Label lbl_chassis_percent;
    @FXML private ToggleButton toggle_login;
    @FXML private ToggleButton toggle_signup;
    @FXML private TextField txt_email;
    @FXML private PasswordField txt_password;
    @FXML private TextField txt_login_email;
    @FXML private PasswordField txt_login_password;
    @FXML private TextField txt_fname;
    @FXML private TextField txt_lname;
    @FXML private TextField txt_signup_email;
    @FXML private PasswordField pf_password;
    @FXML private PasswordField pf_confirm_password;
    @FXML private Pane pane_login;
    @FXML private Pane pane_signup;
    @FXML private Pane booking_map_canvas;

    private org.BOGO.domain.transport.Map bookingTransportMap;
    private PathBuildingService bookingPathService;
    private final java.util.Map<Integer, StackPane> bookingStopNodes = new HashMap<>();
    private final java.util.Map<String, Line> bookingMapLines = new HashMap<>();

    @FXML
    private void initialize() {
        seedPreviewData();
        if (content_area != null) {
            if (btn_nav_dashboard != null && btn_nav_book != null) {
                loadPassengerDashboard();
            } else if (btn_nav_dashboard != null && btn_nav_send_alert != null) {
                loadDriverDashboard();
            } else if (btn_nav_dashboard != null) {
                loadAdminDashboard();
            }
        }
    }

    @FXML private void goMain() { switchScene("/Main.fxml"); }
    @FXML private void goAdminLogin() { switchScene("/AdminUI/AdminLogin.fxml"); }
    @FXML private void goPassengerLogin() { switchScene("/PassengerUI/PassengerLogin_SignUp.fxml"); }
    @FXML private void goDriverLogin() { switchScene("/DriverUI/DriverSignIn.fxml"); }

    @FXML
    private void loginAdmin() {
        if (authenticateFromFields(txt_email, txt_password)) {
            switchScene("/AdminUI/AdminShell.fxml");
        }
    }

    @FXML
    private void loginDriver() {
        if (authenticateFromFields(txt_email, txt_password)) {
            switchScene("/DriverUI/DriverShell.fxml");
        } else {
            switchScene("/DriverUI/DriverShell.fxml");
        }
    }

    @FXML
    private void loginPassenger() {
        if (authenticateFromFields(txt_login_email, txt_login_password)) {
            switchScene("/PassengerUI/PassengerShell.fxml");
        }
    }

    @FXML
    private void signupPassenger() {
        if (pf_password == null || pf_confirm_password == null || !pf_password.getText().equals(pf_confirm_password.getText())) {
            return;
        }
        String name = ((txt_fname == null ? "" : txt_fname.getText()) + " " + (txt_lname == null ? "" : txt_lname.getText())).trim();
        User user = BOGOApplication.getInstance().getUserController().registerPassenger(
                name,
                txt_signup_email == null ? "" : txt_signup_email.getText(),
                pf_password.getText(),
                ""
        );
        if (user != null) {
            BOGOApplication.getInstance().setCurrentUser(user);
            switchScene("/PassengerUI/PassengerShell.fxml");
        }
    }

    private boolean authenticateFromFields(TextField emailField, PasswordField passwordField) {
        if (emailField == null || passwordField == null) {
            return false;
        }
        User user = BOGOApplication.getInstance().getUserController().login(emailField.getText(), passwordField.getText());
        if (user == null) {
            return false;
        }
        BOGOApplication.getInstance().setCurrentUser(user);
        return true;
    }

    @FXML private void loadAdminDashboard() { loadContent("/AdminUI/Admin_Dashboard.fxml", btn_nav_dashboard); }
    @FXML private void loadAdminAnalytics() { loadContent("/AdminUI/Admin_Analytics.fxml", btn_nav_analytics); }
    @FXML private void loadAdminAlerts() { loadContent("/AdminUI/Admin_ResolveAlert.fxml", btn_nav_alerts); }
    @FXML private void loadAdminAddResource() { loadContent("/AdminUI/Admin_AddResource.fxml", btn_nav_add_resource); }
    @FXML private void loadDriverDashboard() { loadContent("/DriverUI/Driver_Dashboard.fxml", btn_nav_dashboard); }
    @FXML private void loadDriverSendAlert() { loadContent("/DriverUI/Driver_SendAlerts.fxml", btn_nav_send_alert); }
    @FXML private void loadDriverRoute() { loadContent("/DriverUI/Driver_ViewRoute.fxml", btn_nav_view_route); }
    @FXML private void loadDriverMetrics() { loadContent("/DriverUI/Driver_BusMetrics.fxml", btn_nav_bus_metrics); }
    @FXML private void loadPassengerDashboard() { loadContent("/PassengerUI/Passenger_Dashboard.fxml", btn_nav_dashboard); }
    @FXML private void loadPassengerBookRide() { loadContent("/PassengerUI/Passenger_BookRide.fxml", btn_nav_book); }
    @FXML private void loadPassengerMultiStop() { loadContent("/PassengerUI/Passenger_MultiStopBooking.fxml", btn_nav_multi_stop); }

    @FXML
    private void showLoginPane() {
        if (pane_login != null && pane_signup != null) {
            pane_login.setVisible(true);
            pane_signup.setVisible(false);
        }
    }

    @FXML
    private void showSignupPane() {
        if (pane_login != null && pane_signup != null) {
            pane_login.setVisible(false);
            pane_signup.setVisible(true);
        }
    }

    @FXML private void showResolutionDialog() { if (pane_resolution_dialog != null) pane_resolution_dialog.setVisible(true); }
    @FXML private void hideResolutionDialog() { if (pane_resolution_dialog != null) pane_resolution_dialog.setVisible(false); }
    @FXML private void showCancelRidePane() { if (pane_cancel_ride != null) pane_cancel_ride.setVisible(true); }
    @FXML private void hideCancelRidePane() { if (pane_cancel_ride != null) pane_cancel_ride.setVisible(false); }

    private void loadContent(String resource, Button activeButton) {
        if (content_area == null) {
            return;
        }

        // TODO: Navigation Logic - Update style of active button and load respective FXML into content_area.
        try {
            content_area.getChildren().setAll((Node) FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resource))));
            setActive(activeButton);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load " + resource, ex);
        }
    }

    private void setActive(Button activeButton) {
        Button[] buttons = {
                btn_nav_dashboard, btn_nav_analytics, btn_nav_alerts, btn_nav_add_resource,
                btn_nav_send_alert, btn_nav_view_route, btn_nav_bus_metrics,
                btn_nav_book, btn_nav_multi_stop
        };
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("nav-button-active");
                if (!button.getStyleClass().contains("nav-button")) {
                    button.getStyleClass().add("nav-button");
                }
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    private void switchScene(String resource) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resource)));
            Scene scene = new Scene(root, 1180, 760);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/NeonTheme.css")).toExternalForm());
            Stage stage = Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage && window.isShowing())
                    .map(window -> (Stage) window)
                    .findFirst()
                    .orElseThrow();
            stage.setScene(scene);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load " + resource, ex);
        }
    }

    private void seedPreviewData() {
        if (list_system_alerts != null) {
            // TODO: Fetch current Message objects (Alerts) from backend.
            list_system_alerts.setItems(FXCollections.observableArrayList("Route A-12 delay", "Bus BOG-241 tyre warning", "Driver change requested"));
        }
        if (lbl_total_passengers != null) lbl_total_passengers.setText("12,480");
        if (lbl_total_revenue != null) lbl_total_revenue.setText("Rs. 3.4M");
        if (list_employees != null) list_employees.setItems(FXCollections.observableArrayList("EMP-1201  Areeba Khan", "EMP-1202  Hassan Ali", "EMP-1203  Fatima Noor"));
        if (list_drivers != null) list_drivers.setItems(FXCollections.observableArrayList("DRV-2201  Imran Qureshi", "DRV-2202  Sana Malik", "DRV-2203  Bilal Ahmed"));
        if (txt_search_employee != null) {
            // TODO: Search Filter Logic - Mock data for preview, link to DB for production. Toggle pane_detail_overlay visibility on list click.
            list_employees.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> showDetail(value));
            list_drivers.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> showDetail(value));
        }
        if (list_active_alerts != null) {
            // TODO: Backend Resource Check. If (resourceAvailable) { show "Resolved" fade-out toast, remove from list } else { show Error }
            list_active_alerts.setItems(FXCollections.observableArrayList("HIGH | Engine temperature | 08:15", "LOW | Route crowding | 08:30"));
        }
        if (list_boarding_passengers != null) {
            // TODO: Refresh list every time a passenger scans in/out.
            list_boarding_passengers.setItems(FXCollections.observableArrayList("Ayesha R. - Stop 4", "Usman T. - Stop 7", "Mina S. - Stop 9"));
        }
        if (combo_stop_1 != null) {
            initializeBookingMap();
        }
        if (combo_stop_count != null) {
            combo_stop_count.setItems(FXCollections.observableArrayList(3, 4, 5));
            combo_stop_count.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
                if (value != null) {
                    populateDynamicStops(value);
                }
            });
            combo_stop_count.getSelectionModel().select(Integer.valueOf(3));
        }
        if (choice_max_capacity != null) {
            choice_max_capacity.setItems(FXCollections.observableArrayList("30", "50"));
            choice_max_capacity.getSelectionModel().selectFirst();
        }
        if (gauge_tyres != null) {
            // TODO: Sync with Bus hardware sensors/Mock data.
            setGaugeValue(gauge_tyres, lbl_tyres_percent, 0.88);
            setGaugeValue(gauge_engine, lbl_engine_percent, 0.62);
            setGaugeValue(gauge_chassis, lbl_chassis_percent, 0.34);
        }
    }

    private void initializeBookingMap() {
        bookingTransportMap = buildBookingDemoMap();
        bookingPathService = new PathBuildingService(bookingTransportMap);

        configureStopCombo(combo_stop_1);
        configureStopCombo(combo_stop_2);
        combo_stop_1.setItems(FXCollections.observableArrayList(bookingTransportMap.getStops()));
        combo_stop_2.setItems(FXCollections.observableArrayList(bookingTransportMap.getStops()));
        combo_stop_1.getSelectionModel().selectFirst();
        combo_stop_2.getSelectionModel().select(bookingTransportMap.getStops().size() - 1);
        combo_stop_1.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> highlightBookingPath());
        combo_stop_2.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> highlightBookingPath());

        if (booking_map_canvas != null) {
            Platform.runLater(() -> {
                renderBookingMap();
                highlightBookingPath();
            });
            booking_map_canvas.widthProperty().addListener((obs, oldValue, newValue) -> {
                renderBookingMap();
                highlightBookingPath();
            });
            booking_map_canvas.heightProperty().addListener((obs, oldValue, newValue) -> {
                renderBookingMap();
                highlightBookingPath();
            });
        }
    }

    @FXML
    private void highlightBookingPath() {
        if (bookingPathService == null || combo_stop_1 == null || combo_stop_2 == null) {
            return;
        }

        resetBookingMapLines();

        Stop source = combo_stop_1.getSelectionModel().getSelectedItem();
        Stop destination = combo_stop_2.getSelectionModel().getSelectedItem();
        if (source == null || destination == null) {
            return;
        }

        Path path = bookingPathService.buildPath(source.getStopID(), destination.getStopID());
        List<Stop> stops = path.getStops();
        for (int i = 0; i < stops.size() - 1; i++) {
            Line line = bookingMapLines.get(bookingEdgeKey(stops.get(i), stops.get(i + 1)));
            if (line != null) {
                activateBookingLine(line);
            }
        }
    }

    @FXML
    private void handleBookingAction() {
        highlightBookingPath();
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (!(currentUser instanceof Passenger passenger)) {
            return;
        }
        Stop start = combo_stop_1 == null ? null : combo_stop_1.getValue();
        Stop end = combo_stop_2 == null ? null : combo_stop_2.getValue();
        BOGOApplication.getInstance().getBookingController().bookRide(passenger, start, end);
    }

    private void renderBookingMap() {
        if (booking_map_canvas == null || bookingTransportMap == null ||
                booking_map_canvas.getWidth() <= 0 || booking_map_canvas.getHeight() <= 0) {
            return;
        }

        booking_map_canvas.getChildren().clear();
        bookingStopNodes.clear();
        bookingMapLines.clear();

        for (Stop stop : bookingTransportMap.getStops()) {
            bookingStopNodes.put(stop.getStopID(), createBookingStopNode(stop));
        }

        renderBookingGpsBaseMap();

        Set<String> renderedEdges = new HashSet<>();
        for (Stop from : bookingTransportMap.getStops()) {
            for (Stop to : from.getConnections()) {
                String key = bookingEdgeKey(from, to);
                if (renderedEdges.add(key)) {
                    Line line = createBookingMapLine(from, to);
                    bookingMapLines.put(key, line);
                    booking_map_canvas.getChildren().add(line);
                }
            }
        }

        booking_map_canvas.getChildren().addAll(bookingStopNodes.values());
    }

    private StackPane createBookingStopNode(Stop stop) {
        double x = bookingMapX(stop.getLocation().getLatitude());
        double y = bookingMapY(stop.getLocation().getLongitude());

        Circle circle = new Circle(5.5);
        circle.setFill(Color.web("#dbeafe"));
        circle.setStroke(Color.web("#38bdf8"));
        circle.setStrokeWidth(1.6);

        StackPane node = new StackPane(circle);
        node.setLayoutX(x - 5.5);
        node.setLayoutY(y - 5.5);
        node.setUserData(circle);
        Tooltip.install(node, createBookingStopTooltip(stop));

        Glow hoverGlow = new Glow(0.8);
        node.setOnMouseEntered(event -> {
            node.setScaleX(1.18);
            node.setScaleY(1.18);
            node.setEffect(hoverGlow);
        });
        node.setOnMouseExited(event -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            node.setEffect(null);
        });

        return node;
    }

    private Tooltip createBookingStopTooltip(Stop stop) {
        String stopName = stop.getStopName();
        Location location = stop.getLocation();
        return new Tooltip(
                "Stop Name: " + stopName +
                        "\nID: " + stop.getStopID() +
                        "\nCoordinates: (" + location.getLatitude() + ", " + location.getLongitude() + ")"
        );
    }

    private Line createBookingMapLine(Stop from, Stop to) {
        StackPane fromNode = bookingStopNodes.get(from.getStopID());
        StackPane toNode = bookingStopNodes.get(to.getStopID());
        Circle fromCircle = (Circle) fromNode.getUserData();
        Circle toCircle = (Circle) toNode.getUserData();

        Line line = new Line();
        line.startXProperty().bind(fromNode.layoutXProperty().add(fromCircle.getRadius()));
        line.startYProperty().bind(fromNode.layoutYProperty().add(fromCircle.getRadius()));
        line.endXProperty().bind(toNode.layoutXProperty().add(toCircle.getRadius()));
        line.endYProperty().bind(toNode.layoutYProperty().add(toCircle.getRadius()));
        styleBookingIdleLine(line, 2.2, 0.72);
        return line;
    }

    private void resetBookingMapLines() {
        for (Line line : bookingMapLines.values()) {
            styleBookingIdleLine(line, 2.2, 0.72);
            line.setEffect(null);
        }
    }

    private void activateBookingLine(Line line) {
        StrokeTransition transition = new StrokeTransition(Duration.millis(260), line);
        transition.setToValue(Color.web("#00f2ff"));
        line.setStrokeWidth(4);
        line.setOpacity(1.0);
        line.setEffect(new Glow(0.9));
        transition.play();
    }

    private void styleBookingIdleLine(Shape shape, double width, double opacity) {
        shape.setStroke(Color.web("#2d8cff"));
        shape.setStrokeWidth(width);
        shape.setOpacity(opacity);
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
    }

    private void renderBookingGpsBaseMap() {
        addBookingAreaLabel("North District", 1.4, 9.4);
        addBookingAreaLabel("Central", 5.25, 5.3);
        addBookingAreaLabel("South Park", 2.1, 1.1);
        addBookingAreaLabel("Market", 8.15, 4.2);

        for (int i = 1; i <= 10; i++) {
            addBookingRoad(0.65, i, 10.35, i, i == 5 || i == 8);
            addBookingRoad(i, 0.65, i, 10.35, i == 3 || i == 7);
        }

        addBookingRoad(1, 1, 10, 10, true);
        addBookingRoad(1, 9.7, 9.7, 1, true);
        addBookingRoad(2.4, 1, 2.4, 10, false);
        addBookingRoad(5.7, 1, 10, 5.3, false);

        addBookingLooseTrackPiece(1.25, 4.35, 1.75, 4.35);
        addBookingLooseTrackPiece(2.65, 7.65, 3.25, 7.65);
        addBookingLooseTrackPiece(4.45, 8.45, 5.05, 8.45);
        addBookingLooseTrackPiece(6.05, 2.25, 6.65, 2.25);
        addBookingLooseTrackPiece(7.35, 6.8, 8.05, 6.8);
        addBookingLooseTrackPiece(8.35, 1.15, 8.9, 1.15);
        addBookingLooseTrackPiece(0.95, 2.15, 1.5, 2.15);
        addBookingLooseTrackPiece(3.85, 0.75, 4.45, 0.75);
    }

    private void addBookingLooseTrackPiece(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        Line fragment = new Line(
                bookingMapX(startLatitude),
                bookingMapY(startLongitude),
                bookingMapX(endLatitude),
                bookingMapY(endLongitude)
        );
        fragment.setStroke(Color.web("#60a5fa"));
        fragment.setStrokeWidth(1.3);
        fragment.setOpacity(0.42);
        fragment.setStrokeLineCap(StrokeLineCap.ROUND);
        fragment.getStrokeDashArray().setAll(8.0, 8.0);
        booking_map_canvas.getChildren().add(fragment);
    }

    private void addBookingRoad(double startLatitude, double startLongitude, double endLatitude, double endLongitude, boolean major) {
        Line casing = new Line(bookingMapX(startLatitude), bookingMapY(startLongitude), bookingMapX(endLatitude), bookingMapY(endLongitude));
        casing.setStroke(major ? Color.web("#1e3a5f") : Color.web("#172c4a"));
        casing.setStrokeWidth(major ? 9 : 5);
        casing.setOpacity(0.92);
        casing.setStrokeLineCap(StrokeLineCap.ROUND);

        Line road = new Line(bookingMapX(startLatitude), bookingMapY(startLongitude), bookingMapX(endLatitude), bookingMapY(endLongitude));
        road.setStroke(major ? Color.web("#3b82f6") : Color.web("#23527d"));
        road.setStrokeWidth(major ? 6 : 3);
        road.setOpacity(0.96);
        road.setStrokeLineCap(StrokeLineCap.ROUND);

        booking_map_canvas.getChildren().addAll(casing, road);
    }

    private void addBookingAreaLabel(String label, double latitude, double longitude) {
        Text text = new Text(label);
        text.setFill(Color.web("#7dd3fc"));
        text.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        text.setLayoutX(bookingMapX(latitude));
        text.setLayoutY(bookingMapY(longitude));
        text.setOpacity(0.72);
        booking_map_canvas.getChildren().add(text);
    }

    private double bookingMapX(double latitude) {
        double min = bookingTransportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLatitude()).min().orElse(0);
        double max = bookingTransportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLatitude()).max().orElse(1);
        return scale(latitude, min, max, 80, booking_map_canvas.getWidth() - 80);
    }

    private double bookingMapY(double longitude) {
        double min = bookingTransportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLongitude()).min().orElse(0);
        double max = bookingTransportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLongitude()).max().orElse(1);
        return scale(longitude, min, max, booking_map_canvas.getHeight() - 90, 90);
    }

    private double scale(double value, double min, double max, double targetMin, double targetMax) {
        if (Double.compare(min, max) == 0) {
            return (targetMin + targetMax) / 2.0;
        }
        return targetMin + ((value - min) / (max - min)) * (targetMax - targetMin);
    }

    private void configureStopCombo(ComboBox<Stop> comboBox) {
        comboBox.setButtonCell(new StopListCell());
        comboBox.setCellFactory(listView -> new StopListCell());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stop stop) {
                return stop == null ? "" : "Stop " + stop.getStopName();
            }

            @Override
            public Stop fromString(String text) {
                if (text == null || bookingTransportMap == null) {
                    return null;
                }

                String normalized = text.trim().replaceFirst("(?i)^stop\\s+", "");
                return bookingTransportMap.getStops().stream()
                        .filter(stop -> stop.getStopName().equalsIgnoreCase(normalized))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private String bookingEdgeKey(Stop a, Stop b) {
        int low = Math.min(a.getStopID(), b.getStopID());
        int high = Math.max(a.getStopID(), b.getStopID());
        return low + "-" + high;
    }

    private org.BOGO.domain.transport.Map buildBookingDemoMap() {
        org.BOGO.domain.transport.Map map = new org.BOGO.domain.transport.Map();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int stopID = row * 10 + col + 1;
                ArrayList<Integer> connections = new ArrayList<>();
                ArrayList<Double> fares = new ArrayList<>();
                ArrayList<Integer> routeIds = new ArrayList<>();

                if (col > 0) {
                    connections.add(stopID - 1);
                    fares.add(1.0);
                    routeIds.add(row + 1);
                }
                if (row > 0) {
                    connections.add(stopID - 10);
                    fares.add(1.1);
                    routeIds.add(20 + col);
                }
                if (row > 0 && col > 0 && (row == col || row + col == 9 || col == 4)) {
                    connections.add(stopID - 11);
                    fares.add(1.6);
                    routeIds.add(50 + row);
                }

                map.addStop(
                        stopID,
                        String.valueOf(stopID),
                        new Location(gpsLatitude(row, col), gpsLongitude(row, col)),
                        connections,
                        fares,
                        routeIds
                );
            }
        }

        return map;
    }

    private double gpsLatitude(int row, int col) {
        double offset = ((row * 17 + col * 11) % 7 - 3) * 0.025;
        return col + 1 + offset;
    }

    private double gpsLongitude(int row, int col) {
        double offset = ((row * 13 + col * 19) % 7 - 3) * 0.025;
        return row + 1 + offset;
    }

    private static class StopListCell extends ListCell<Stop> {
        @Override
        protected void updateItem(Stop stop, boolean empty) {
            super.updateItem(stop, empty);
            setText(empty || stop == null ? null : "Stop " + stop.getStopName());
        }
    }

    private void setGaugeValue(ProgressIndicator gauge, Label percentLabel, double value) {
        gauge.setProgress(value);
        if (percentLabel != null) {
            percentLabel.setText(Math.round(value * 100) + "%");
        }
    }

    private void populateDynamicStops(int stopCount) {
        if (box_dynamic_stops == null) {
            return;
        }

        box_dynamic_stops.getChildren().clear();
        for (int i = 1; i <= stopCount; i++) {
            ComboBox<String> stopInput = new ComboBox<>();
            stopInput.setEditable(true);
            stopInput.setPromptText("Stop " + i);
            stopInput.setMaxWidth(Double.MAX_VALUE);
            stopInput.setItems(FXCollections.observableArrayList("Liberty Market", "Gulberg Main", "Model Town", "Railway Station"));
            box_dynamic_stops.getChildren().add(stopInput);
        }
    }

    private void showDetail(String value) {
        if (value != null && pane_detail_overlay != null) {
            lbl_detail_name.setText(value.substring(value.indexOf(' ') + 1).trim());
            lbl_detail_id.setText(value.substring(0, value.indexOf(' ')).trim());
            pane_detail_overlay.setVisible(true);
        }
    }
}
