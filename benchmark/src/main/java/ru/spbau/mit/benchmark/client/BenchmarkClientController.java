package ru.spbau.mit.benchmark.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
import ru.spbau.mit.benchmark.client.exceptions.BenchmarkClientException;
import ru.spbau.mit.common.ServerArchitecture;
import ru.spbau.mit.common.TimeStatistics;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


public class BenchmarkClientController {
    private BenchmarkClient client = null;
    private ToggleGroup radioButtonGroup = new ToggleGroup();
    @FXML
    private Spinner<Integer> arraySizeBegin;
    @FXML
    private Spinner<Integer> arraySizeEnd;
    @FXML
    private Spinner<Integer> arraySizeStep;
    @FXML
    private Spinner<Integer> nClientsBegin;
    @FXML
    private Spinner<Integer> nClientsEnd;
    @FXML
    private Spinner<Integer> nClientsStep;
    @FXML
    private Spinner<Integer> deltaTimeBegin;
    @FXML
    private Spinner<Integer> deltaTimeEnd;
    @FXML
    private Spinner<Integer> deltaTimeStep;
    @FXML
    private Spinner<Integer> nRequests;
    @FXML
    private Button benchmarkStart;
    @FXML
    private TextField serverIp;
    @FXML
    private Label info;
    @FXML
    private RadioButton arraySizeButton;
    @FXML
    private RadioButton nClientsButton;
    @FXML
    private RadioButton timeDeltaButton;
    @FXML
    private VBox graphics;
    @FXML
    private ChoiceBox<ServerArchitecture> serverArch;
    @FXML
    private Tab mainTab;

    @FXML
    private void initialize() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    return null;
                }
            }

            return c;
        };

        arraySizeBegin.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        arraySizeEnd.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        arraySizeStep.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));

        nClientsBegin.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        nClientsEnd.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        nClientsStep.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));

        deltaTimeBegin.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        deltaTimeEnd.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));
        deltaTimeStep.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));

        nRequests.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, filter));

        arraySizeBegin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1000, 1));
        arraySizeEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 7000, 1));
        arraySizeStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1000, 1));

        nClientsBegin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1, 1));
        nClientsEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50, 1));
        nClientsStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10, 1));

        deltaTimeBegin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0, 100));
        deltaTimeEnd.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 1000, 100));
        deltaTimeStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 100, 100));

        nRequests.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 10, 1));

        arraySizeButton.setToggleGroup(radioButtonGroup);
        nClientsButton.setToggleGroup(radioButtonGroup);
        timeDeltaButton.setToggleGroup(radioButtonGroup);

        radioButtonGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (radioButtonGroup.getSelectedToggle() != null) {
                RadioButton chk = (RadioButton) radioButtonGroup.getSelectedToggle();

                if (chk == arraySizeButton) {
                    arraySizeBegin.setDisable(false);
                    arraySizeEnd.setDisable(false);
                    arraySizeStep.setDisable(false);
                    nClientsEnd.setDisable(true);
                    nClientsStep.setDisable(true);
                    deltaTimeEnd.setDisable(true);
                    deltaTimeStep.setDisable(true);

                } else if (chk == nClientsButton) {
                    nClientsBegin.setDisable(false);
                    nClientsEnd.setDisable(false);
                    nClientsStep.setDisable(false);
                    arraySizeEnd.setDisable(true);
                    arraySizeStep.setDisable(true);
                    deltaTimeEnd.setDisable(true);
                    deltaTimeStep.setDisable(true);

                } else if (chk == timeDeltaButton) {
                    deltaTimeBegin.setDisable(false);
                    deltaTimeEnd.setDisable(false);
                    deltaTimeStep.setDisable(false);
                    arraySizeEnd.setDisable(true);
                    arraySizeStep.setDisable(true);
                    nClientsEnd.setDisable(true);
                    nClientsStep.setDisable(true);
                }
            }
        });

        serverArch.setValue(ServerArchitecture.TCP_SERVER_ASYNC);
        serverArch.getItems().addAll(ServerArchitecture.TCP_SERVER_ASYNC,
                ServerArchitecture.TCP_SERVER_SINGLE_THREAD,
                ServerArchitecture.TCP_THREAD_ON_CLIENT,
                ServerArchitecture.TCP_SERVER_CACHED_THREAD_POOL,
                ServerArchitecture.TCP_SERVER_FIXED_THREAD_POOL,
                ServerArchitecture.UDP_SERVER_THREAD_ON_REQUEST,
                ServerArchitecture.UDP_SERVER_THREAD_POOL);

        serverIp.setText("127.0.0.1");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> showException((Exception) e)));
    }

    @FXML
    private void start(ActionEvent ignored) {
        Function<BenchmarkIntArray, List<Integer>> getArray = s -> {
            List<Integer> result = new ArrayList<>();
            while(s.hasNext()) {
                result.add(s.next());
            }

            return result;
        };

        ServerArchitecture architecture = serverArch.getValue();
        String ip = serverIp.getText();
        Integer nReq = nRequests.getValue();
        RadioButton chk = (RadioButton) radioButtonGroup.getSelectedToggle();

        List<Integer> data = new ArrayList<>();
        String dataName = null;

        BenchmarkIntArray sizeArray = new BenchmarkIntArray(arraySizeBegin.getValue(), arraySizeBegin.getValue(), 1);
        BenchmarkIntArray nClients = new BenchmarkIntArray(nClientsBegin.getValue(), nClientsBegin.getValue(), 1);
        BenchmarkIntArray timeDelta = new BenchmarkIntArray(deltaTimeBegin.getValue(), deltaTimeBegin.getValue(), 1);

        if (chk == arraySizeButton) {
            dataName = "array size";
            sizeArray = new BenchmarkIntArray(arraySizeBegin.getValue(), arraySizeEnd.getValue(), arraySizeStep.getValue());
            data = getArray.apply(sizeArray);
            sizeArray.reset();
        } else if (chk == nClientsButton) {
            dataName = "clients number";
            nClients = new BenchmarkIntArray(nClientsBegin.getValue(), nClientsEnd.getValue(), nClientsStep.getValue());
            data = getArray.apply(nClients);
            nClients.reset();
        } else if (chk == timeDeltaButton) {
            dataName = "delta time";
            timeDelta = new BenchmarkIntArray(deltaTimeBegin.getValue(), deltaTimeEnd.getValue(), deltaTimeStep.getValue());
            data = getArray.apply(timeDelta);
            timeDelta.reset();
        }

        BenchmarkClientConfiguration config = new BenchmarkClientConfiguration(architecture, nReq,
                sizeArray, nClients, timeDelta);

        List<Integer> finalData = data;
        String finalDataName = dataName;

        info.setText("Тест запущен. Подождите, пожалуйста...");
        mainTab.setDisable(true);

        client = new BenchmarkClient(ip, config);
        Thread thread = new Thread( () -> {
            try {
                List<TimeStatistics> stats = client.benchmark();
                Platform.runLater(() -> {
                    plotGraphics(finalData, stats, finalDataName);
                    info.setText("");
                    mainTab.setDisable(false);
                });
            } catch (BenchmarkClientException e) {
                Platform.runLater(() -> showException(e));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void showException(Exception e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        String message = e.getMessage();
        Exception otherExceptions[] = (Exception[]) e.getSuppressed();
        for (Exception exc : otherExceptions) {
            message += ": " + exc.getMessage();
        }
        alert.setContentText(message);
        alert.showAndWait();
        info.setText("");
        mainTab.setDisable(false);
    }

    private void plotGraphics(List<Integer> x, List<TimeStatistics> statisticses, String xName) {
        graphics.getChildren().clear();

        List<Long> arrayProsecingTime = statisticses.stream().map(TimeStatistics::getArrayProcessingTime).collect(Collectors.toList());
        List<Long> requesProsecingTime = statisticses.stream().map(TimeStatistics::getRequestProcessingTime).collect(Collectors.toList());
        List<Long> clientWorkTime = statisticses.stream().map(TimeStatistics::getClientWorkTime).collect(Collectors.toList());

        plot(x, arrayProsecingTime, xName, "Array processing time");
        plot(x, requesProsecingTime, xName, "Request processing time");
        plot(x, clientWorkTime, xName, "Client work time");
    }

    private void plot(List<Integer> x, List<Long> y, String xName, String title) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Time, ms");
        xAxis.setLabel(xName);
        LineChart<Number,Number> lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setTitle(title);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < y.size(); i++) {
            series.getData().add(new XYChart.Data<>(x.get(i), y.get(i)));
        }
        lineChart.getData().add(series);
        graphics.getChildren().add(lineChart);
    }
}
