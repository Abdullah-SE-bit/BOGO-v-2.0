package org.BOGO;

import javafx.animation.StrokeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Stop;
import org.BOGO.service.PathBuildingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TestMap extends Application {
    @FXML private Pane mapCanvas;
    @FXML private ComboBox<Stop> combo_source;
    @FXML private ComboBox<Stop> combo_destination;
    @FXML private Button btn_find_path;
    @FXML private Label lbl_path_summary;

    private org.BOGO.domain.transport.Map transportMap;
    private PathBuildingService pathBuildingService;
    private final java.util.Map<Integer, StackPane> stopNodes = new HashMap<>();
    private final java.util.Map<String, Line> mapLines = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(TestMap.class.getResource("/TestMap.fxml")));
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(Objects.requireNonNull(TestMap.class.getResource("/NeonTheme.css")).toExternalForm());
        stage.setTitle("BOGO Graph Visualization Test");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void initialize() {
        transportMap = buildDemoMap();
        pathBuildingService = new PathBuildingService(transportMap);

        configureStopCombo(combo_source);
        configureStopCombo(combo_destination);
        combo_source.setItems(FXCollections.observableArrayList(transportMap.getStops()));
        combo_destination.setItems(FXCollections.observableArrayList(transportMap.getStops()));
        combo_source.getSelectionModel().selectFirst();
        if (transportMap.getStops().size() > 1) {
            combo_destination.getSelectionModel().select(transportMap.getStops().size() - 1);
        }

        Platform.runLater(this::renderGraph);
        mapCanvas.widthProperty().addListener((obs, oldValue, newValue) -> renderGraph());
        mapCanvas.heightProperty().addListener((obs, oldValue, newValue) -> renderGraph());
    }

    @FXML
    private void findPath() {
        Stop source = combo_source.getSelectionModel().getSelectedItem();
        Stop destination = combo_destination.getSelectionModel().getSelectedItem();
        resetEdges();

        if (source == null || destination == null) {
            lbl_path_summary.setText("Select both stops");
            return;
        }

        Path path = pathBuildingService.buildPath(source.getStopID(), destination.getStopID());
        List<Stop> stops = path.getStops();
        if (stops.isEmpty()) {
            lbl_path_summary.setText("No path found");
            return;
        }

        for (int i = 0; i < stops.size() - 1; i++) {
            Line line = mapLines.get(edgeKey(stops.get(i), stops.get(i + 1)));
            if (line != null) {
                activateLine(line);
            }
        }

        lbl_path_summary.setText("Path: " + formatPath(stops) + " | Fare: " + path.getTotalCost());
    }

    private void renderGraph() {
        if (mapCanvas == null || mapCanvas.getWidth() <= 0 || mapCanvas.getHeight() <= 0) {
            return;
        }

        mapCanvas.getChildren().clear();
        stopNodes.clear();
        mapLines.clear();

        for (Stop stop : transportMap.getStops()) {
            stopNodes.put(stop.getStopID(), createStopNode(stop));
        }

        renderGpsBaseMap();

        Set<String> renderedEdges = new HashSet<>();
        for (Stop from : transportMap.getStops()) {
            for (Stop to : from.getConnections()) {
                String key = edgeKey(from, to);
                if (renderedEdges.add(key)) {
                    Line line = createMapLine(from, to);
                    mapLines.put(key, line);
                    mapCanvas.getChildren().add(line);
                }
            }
        }

        mapCanvas.getChildren().addAll(stopNodes.values());
    }

    private StackPane createStopNode(Stop stop) {
        double x = mapX(stop.getLocation().getLatitude());
        double y = mapY(stop.getLocation().getLongitude());

        Circle circle = new Circle(5.5);
        circle.setFill(Color.web("#dbeafe"));
        circle.setStroke(Color.web("#38bdf8"));
        circle.setStrokeWidth(1.6);

        StackPane node = new StackPane(circle);
        node.setLayoutX(x - 5.5);
        node.setLayoutY(y - 5.5);
        node.setUserData(circle);

        Tooltip.install(node, createStopTooltip(stop));

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

    private Tooltip createStopTooltip(Stop stop) {
        String stopName = stop.getStopName();
        int stopID = stop.getStopID();
        Location location = stop.getLocation();

        return new Tooltip(
                "Stop Name: " + stopName +
                        "\nID: " + stopID +
                        "\nCoordinates: (" + location.getLatitude() + ", " + location.getLongitude() + ")"
        );
    }

    private Line createMapLine(Stop from, Stop to) {
        StackPane fromNode = stopNodes.get(from.getStopID());
        StackPane toNode = stopNodes.get(to.getStopID());

        Line line = new Line();
        Circle fromCircle = (Circle) fromNode.getUserData();
        Circle toCircle = (Circle) toNode.getUserData();

        line.startXProperty().bind(fromNode.layoutXProperty().add(fromCircle.getRadius()));
        line.startYProperty().bind(fromNode.layoutYProperty().add(fromCircle.getRadius()));
        line.endXProperty().bind(toNode.layoutXProperty().add(toCircle.getRadius()));
        line.endYProperty().bind(toNode.layoutYProperty().add(toCircle.getRadius()));
        styleIdleLine(line, 2.2, 0.72);
        return line;
    }

    private void resetEdges() {
        for (Line line : mapLines.values()) {
            styleIdleLine(line, 2.2, 0.72);
            line.setEffect(null);
        }
    }

    private void activateLine(Line line) {
        StrokeTransition transition = new StrokeTransition(Duration.millis(260), line);
        transition.setToValue(Color.web("#00f2ff"));
        line.setStrokeWidth(4);
        line.setOpacity(1.0);
        line.setEffect(new Glow(0.9));
        transition.play();
    }

    private void styleIdleLine(Shape shape, double width, double opacity) {
        shape.setStroke(Color.web("#2d8cff"));
        shape.setStrokeWidth(width);
        shape.setOpacity(opacity);
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
    }

    private void renderGpsBaseMap() {
        addAreaLabel("North District", 1.4, 9.4);
        addAreaLabel("Central", 5.25, 5.3);
        addAreaLabel("South Park", 2.1, 1.1);
        addAreaLabel("Market", 8.15, 4.2);

        for (int i = 1; i <= 10; i++) {
            addRoad(0.65, i, 10.35, i, i == 5 || i == 8);
            addRoad(i, 0.65, i, 10.35, i == 3 || i == 7);
        }

        addRoad(1, 1, 10, 10, true);
        addRoad(1, 9.7, 9.7, 1, true);
        addRoad(2.4, 1, 2.4, 10, false);
        addRoad(5.7, 1, 10, 5.3, false);

        addLooseTrackPiece(1.25, 4.35, 1.75, 4.35);
        addLooseTrackPiece(2.65, 7.65, 3.25, 7.65);
        addLooseTrackPiece(4.45, 8.45, 5.05, 8.45);
        addLooseTrackPiece(6.05, 2.25, 6.65, 2.25);
        addLooseTrackPiece(7.35, 6.8, 8.05, 6.8);
        addLooseTrackPiece(8.35, 1.15, 8.9, 1.15);
        addLooseTrackPiece(0.95, 2.15, 1.5, 2.15);
        addLooseTrackPiece(3.85, 0.75, 4.45, 0.75);
    }

    private void addLooseTrackPiece(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        Line fragment = new Line(
                mapX(startLatitude),
                mapY(startLongitude),
                mapX(endLatitude),
                mapY(endLongitude)
        );
        fragment.setStroke(Color.web("#60a5fa"));
        fragment.setStrokeWidth(1.3);
        fragment.setOpacity(0.42);
        fragment.setStrokeLineCap(StrokeLineCap.ROUND);
        fragment.getStrokeDashArray().setAll(8.0, 8.0);
        mapCanvas.getChildren().add(fragment);
    }

    private void addRoad(double startLatitude, double startLongitude, double endLatitude, double endLongitude, boolean major) {
        Line casing = new Line(mapX(startLatitude), mapY(startLongitude), mapX(endLatitude), mapY(endLongitude));
        casing.setStroke(major ? Color.web("#1e3a5f") : Color.web("#172c4a"));
        casing.setStrokeWidth(major ? 9 : 5);
        casing.setOpacity(0.92);
        casing.setStrokeLineCap(StrokeLineCap.ROUND);

        Line road = new Line(mapX(startLatitude), mapY(startLongitude), mapX(endLatitude), mapY(endLongitude));
        road.setStroke(major ? Color.web("#3b82f6") : Color.web("#23527d"));
        road.setStrokeWidth(major ? 6 : 3);
        road.setOpacity(0.96);
        road.setStrokeLineCap(StrokeLineCap.ROUND);

        mapCanvas.getChildren().addAll(casing, road);
    }

    private void addAreaLabel(String label, double latitude, double longitude) {
        Text text = new Text(label);
        text.setFill(Color.web("#7dd3fc"));
        text.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        text.setLayoutX(mapX(latitude));
        text.setLayoutY(mapY(longitude));
        text.setOpacity(0.72);
        mapCanvas.getChildren().add(text);
    }

    private double mapX(double latitude) {
        double min = transportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLatitude()).min().orElse(0);
        double max = transportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLatitude()).max().orElse(1);
        return scale(latitude, min, max, 80, mapCanvas.getWidth() - 80);
    }

    private double mapY(double longitude) {
        double min = transportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLongitude()).min().orElse(0);
        double max = transportMap.getStops().stream().mapToDouble(stop -> stop.getLocation().getLongitude()).max().orElse(1);
        return scale(longitude, min, max, mapCanvas.getHeight() - 90, 90);
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
    }

    private String edgeKey(Stop a, Stop b) {
        int low = Math.min(a.getStopID(), b.getStopID());
        int high = Math.max(a.getStopID(), b.getStopID());
        return low + "-" + high;
    }

    private String formatPath(List<Stop> stops) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            if (i > 0) {
                builder.append(" -> ");
            }
            builder.append(stops.get(i).getStopName());
        }
        return builder.toString();
    }

    private org.BOGO.domain.transport.Map buildDemoMap() {
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

    public static void main(String[] args) {
        launch(args);
    }
}
