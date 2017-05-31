package com.citytransportanalysis.gui;

import com.citytransportanalysis.modeling.Modeling;
import com.citytransportanalysis.modeling.entity.RouteSegment;
import com.citytransportanalysis.modeling.entity.Transport;
import com.citytransportanalysis.modeling.entity.Stop;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GUI main controller
 */
public class Controller {
    public Button button;
    //public Label label;
    public TableView logTable;
    public ListView<String> stopListView;
    public ListView<String> transportListView;
    public TextField timeFromTextField;
    public TextField timeToTextField;
    public Spinner<Integer> transportCountSpinner;
    public Spinner<Integer> periodSpinner;
    public Spinner<Integer> transportPlacesSit;
    public Spinner<Integer> transportPlacesStand;
    public Spinner<Integer> maxPercentPlaces;
    public Label totalPlacesCountLabel;
    public Label percentPlacesCountLabel;
    public TextFlow textFlowStationInfo;
    public Button resetFilter;
    public LineChart transportLineChart;
    public LineChart stopsLineChart;
    public CategoryAxis timeAxis;
    public NumberAxis passengersAxis;
    public WebView gmaps;
    ArrayList<Transport> overUsedTransport;
    ArrayList<Stop> overUsedStops;
    ObservableList<String> olist, tlist;

    /** Список событий {@link com.citytransportanalysis.modeling.Modeling.Event}  */
    private List<Modeling.Event> eventsLog;

    private Modeling modeling;

    private int totalPlaces;
    private int percentPlaces;

    public void initialize() {
        /* Стартовые значения */
        timeFromTextField.setText("06:00");     // Начальное время
        timeToTextField.setText("22:00");       // Конец движения
        transportCountSpinner.getValueFactory().setValue(5);    //Кол-во транспорта
        periodSpinner.getValueFactory().setValue(10);       // Период движения в минутах
        transportPlacesSit.getValueFactory().setValue(22);    //Сидячих мест
        transportPlacesStand.getValueFactory().setValue(21);       // Стоячих мест
        maxPercentPlaces.getValueFactory().setValue(75);            //відсоток місць

        totalPlaces = transportPlacesSit.getValueFactory().getValue() + transportPlacesStand.getValueFactory().getValue();
        totalPlacesCountLabel.setText(Integer.toString(totalPlaces));
        percentPlaces = (int) ((totalPlaces * (maxPercentPlaces.getValueFactory().getValue() / 100.0)) + 0.5);
        percentPlacesCountLabel.setText(Integer.toString(percentPlaces));

        transportPlacesStand.valueProperty().addListener(((observable, oldValue, newValue) -> {
            totalPlaces = transportPlacesSit.getValueFactory().getValue() + transportPlacesStand.getValueFactory().getValue();
            totalPlacesCountLabel.setText(Integer.toString(totalPlaces));
            percentPlaces = (int) ((totalPlaces * (maxPercentPlaces.getValueFactory().getValue() / 100.0)) + 0.5);
            percentPlacesCountLabel.setText(Integer.toString(percentPlaces));
        }));

        transportPlacesSit.valueProperty().addListener(((observable, oldValue, newValue) -> {
            totalPlaces = transportPlacesSit.getValueFactory().getValue() + transportPlacesStand.getValueFactory().getValue();
            totalPlacesCountLabel.setText(Integer.toString(totalPlaces));
            percentPlaces = (int) ((totalPlaces * (maxPercentPlaces.getValueFactory().getValue() / 100.0)) + 0.5);
            percentPlacesCountLabel.setText(Integer.toString(percentPlaces));
        }));

        maxPercentPlaces.valueProperty().addListener(((observable, oldValue, newValue) -> {
            percentPlaces = (int) ((totalPlaces * (maxPercentPlaces.getValueFactory().getValue() / 100.0)) + 0.5);
            percentPlacesCountLabel.setText(Integer.toString(percentPlaces));
            transportListView.setItems(null);
            transportListView.setItems(tlist);
        }));

        stopListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                //System.out.println("Selected item: " + newValue);
                Text text1 = new Text(String.format("Інформація про зупинку %s\n", newValue));
                text1.setFill(Color.RED);
                int passengersCount = 0;
                int leftPassengersCount = 0;
                for (Modeling.Event event : modeling.eventsLog) {
                    if (event.getType() == Modeling.Event.Type.SittingPassenger && newValue.equals(event.getStop().getName())) {
                        passengersCount += event.getStop().getSittedPassengers();
                        leftPassengersCount += event.getStop().getPassengers().size();
                    }
                }
                Text text2 = new Text(String.format("Пройшло %d пасажирів, не змогло зайти %d пасажирів", passengersCount, leftPassengersCount));
                textFlowStationInfo.getChildren().clear();
                textFlowStationInfo.getChildren().addAll(text1, text2);
                //List<Modeling.Event> events = modeling.eventsLog;
                //List<Modeling.Event> eventsnew = modeling.eventsLog.stream().filter(u -> u.getStop() != null && u.getStop().getName().equals(newValue.getName())).collect(Collectors.toList());
                ObservableList<Modeling.Event> data = FXCollections.observableList(modeling.eventsLog.stream().filter(u -> u.getStop() != null && u.getStop().getName().equals(newValue)).collect(Collectors.toList()));
                initTable(data);

            }
        }); //вывод инфы про выбранную остановку
        stopListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> stringListView) {
                return new ListCell<String>() {

                    @Override
                    protected void updateItem(String s, boolean b) {
                        super.updateItem(s, b);    //To change body of overridden methods use File | Settings | File Templates.
                        if (b) {
                            setText(null);
                            setGraphic(null);
                            setStyle(null);
                        } else if (s != null && overUsedStops.stream().filter(stop -> stop.getName().equals(s)).count() > 0) {
                            setStyle("-fx-background-color: red");
                            setText(s);
                        } else if (s != null) {
                            setStyle(null);
                            setText(s);
                        }
                    }
                };
            }
        });     // отмечает остановки не подходящие по требованиям

        transportListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                //System.out.println("Selected item: " + newValue);
                Text text1 = new Text(String.format("Інформація про транспорт %s\n", newValue));
                text1.setFill(Color.RED);
                Transport transport = null;
                for (Modeling.Event event : modeling.eventsLog)
                    if (event.getType() == Modeling.Event.Type.EndDay && newValue.equals(event.getTransport().toString())) {
                        transport = event.getTransport();
                        break;
                    }

                if (transport != null) {
                    Text text2 = new Text(String.format("Проходив шлях %d разів і перевіз %d чел\n", transport.getTripCount(), transport.getAllPassengersCount()));
                    textFlowStationInfo.getChildren().clear();
                    textFlowStationInfo.getChildren().addAll(text1, text2);
                }

                //List<Modeling.Event> events = modeling.eventsLog;
                //List<Modeling.Event> eventsnew = modeling.eventsLog.stream().filter(u -> u.getStop() != null && u.getStop().getName().equals(newValue.getName())).collect(Collectors.toList());
                ObservableList<Modeling.Event> data = FXCollections.observableList(modeling.eventsLog.stream().filter(u -> u.getTransport() != null && u.getTransport().toString().equals(newValue)).collect(Collectors.toList()));
                initTable(data);


            }
        });     //вывод инфы про выбранный транспорт
        transportListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> stringListView) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String s, boolean b) {
                        super.updateItem(s, b);    //To change body of overridden methods use File | Settings | File Templates.
                        if (b) {
                            setText(null);
                            setGraphic(null);
                            setStyle(null);
                        } else if (s != null && overUsedTransport.stream().filter(transport -> transport.toString().equals(s)).count() > 0) {
                            setStyle("-fx-background-color: red");
                            setText(s);
                        } else if (s != null) {
                            setStyle(null);
                            setText(s);
                        }
                    }

                };
            }
        });     // отмечает транспорт не подходящие по требованиям
        gmaps.getEngine().load(getClass().getResource("/gmaps/index.html").toExternalForm());
    }



    public void startModellingButtonClicked(ActionEvent actionEvent) {
        /* Тест
        try{gmaps.getEngine().executeScript("calcRoute('Kyiv, Pravdi str', 'Kyiv, Pobedi str');");}
        catch (Exception ignored){}
        transportListView.setItems(null);
        */

        /* Получения данных моделирования */
        modeling = new Modeling();
        LocalTime startTime = LocalTime.parse(timeFromTextField.getText());
        LocalTime endTime = LocalTime.parse(timeToTextField.getText());
        LinkedList<RouteSegment> routeSegments = modeling.routeData();
        int transportCount = transportCountSpinner.getValueFactory().getValue();
        int periodCount = periodSpinner.getValueFactory().getValue();
        int sitPlacesCount = transportPlacesSit.getValueFactory().getValue();
        int standPlacesCount = transportPlacesStand.getValueFactory().getValue();
        //System.out.printf("New transport count %d period %d", transportCount, periodCount);
        LinkedList<Transport> transportList = modeling.transportData(transportCount, sitPlacesCount, standPlacesCount, startTime, periodCount);
        modeling.LaunchMovement(routeSegments, transportList, startTime, endTime, periodCount);
        LinkedList<RouteSegment> route = modeling.routeData();

        eventsLog = modeling.eventsLog;

        /* Заполнения списка остановок */
        LinkedList<Stop> stopsList = new LinkedList<>();
        for (RouteSegment r : route)
            if (r == route.getFirst()) {
                stopsList.add(r.getTwoStops().get(0));
                stopsList.add(r.getTwoStops().get(1));
            } else stopsList.add(r.getTwoStops().get(1));

        olist = FXCollections.observableArrayList();
        for (Stop s : stopsList)
            olist.add(s.getName());
        stopListView.setItems(olist);

        /* Заполнения списка транспорта */
        tlist = FXCollections.observableArrayList();
        for (Transport t : transportList)
            tlist.add(t.toString());
        transportListView.setItems(tlist);

        /* Заполнения списка событий лог */
        ObservableList<Modeling.Event> data = FXCollections.observableList(modeling.eventsLog.stream().filter(event ->
                !event.getType().equals(Modeling.Event.Type.EndDay)).collect(Collectors.toList()));
        initTable(data);

        overUsedTransport = new ArrayList<>();
        overUsedStops = new ArrayList<>();

        /* Заполнение графика заполненности транспорта */
        transportLineChart.getData().clear();
        transportLineChart.getXAxis().setAnimated(false);
        for (Transport t : transportList) {
            XYChart.Series series = new XYChart.Series();
            series.setName(t.toString());
            LocalTime curTime = startTime;
            do {
                final int hours = curTime.getHour();
                List<Modeling.Event> curEvents = modeling.eventsLog.stream().filter(event ->
                        event.getType().equals(Modeling.Event.Type.OnWay) &&
                                event.getTransport().equals(t) &&
                                event.getTime().getHour() == hours).collect(Collectors.toList());
                if (curEvents.size() != 0) {
                    Modeling.Event curEvent = Collections.max(curEvents, Comparator.comparing(Modeling.Event::getFilledPlaces));
                    series.getData().add(new XYChart.Data<>(curTime.toString(), curEvent.getFilledPlaces()));
                    if (curEvent.getFilledPlaces() > percentPlaces)
                        if (!overUsedTransport.contains(curEvent.getTransport()))
                            overUsedTransport.add(curEvent.getTransport());
                }
                curTime = curTime.plusHours(1);
            } while (curTime.isBefore(endTime));
            transportLineChart.getData().add(series);
        }

        /* Заполнение графика заполненности остановок */
        stopsLineChart.getData().clear();
        stopsLineChart.getXAxis().setAnimated(false);
        for (Stop s : stopsList) {
            XYChart.Series series = new XYChart.Series();
            series.setName(s.getName());
            LocalTime curTime = startTime;
            do {
                final int hours = curTime.getHour();
                List<Modeling.Event> curEvents = modeling.eventsLog.stream().filter(event ->
                        event.getType().equals(Modeling.Event.Type.OnStop) &&
                                event.getStop().getName().equals(s.getName()) &&
                                event.getTime().getHour() == hours).collect(Collectors.toList());
                if (curEvents.size() != 0) {
                    Modeling.Event curEvent = Collections.max(curEvents, Comparator.comparing(Modeling.Event::getPassengersOnStop));
                    series.getData().add(new XYChart.Data<>(curTime.toString(), curEvent.getPassengersOnStop()));
                    if (curEvent.getPassengersLeft() > 0)
                        if (!overUsedStops.contains(curEvent.getStop()))
                            overUsedStops.add(curEvent.getStop());
                }
                curTime = curTime.plusHours(1);
            } while (curTime.isBefore(endTime));
            stopsLineChart.getData().add(series);
        }

        /* Обновляем списки транспорта и остановок с подсветкой */
        transportListView.refresh();
        stopListView.refresh();

    }

    public void resetFilterClicked(ActionEvent actionEvent) {
        stopListView.getSelectionModel().clearSelection();
        transportListView.getSelectionModel().clearSelection();
        ObservableList<Modeling.Event> data = FXCollections.observableList(modeling.eventsLog.stream().filter(event ->
                !event.getType().equals(Modeling.Event.Type.EndDay)).collect(Collectors.toList()));
        initTable(data);
    }


    public void initTable(ObservableList<Modeling.Event> data) {
        logTable.setItems(data);
        TableColumn timeCol = new TableColumn("Час");
        timeCol.setCellValueFactory(new PropertyValueFactory("textTime"));
        TableColumn idCol = new TableColumn("№");
        idCol.setCellValueFactory(new PropertyValueFactory("transportId"));
        TableColumn descrCol = new TableColumn("Опис події");
        descrCol.setCellValueFactory(new PropertyValueFactory("description"));

        logTable.getColumns().setAll(timeCol, idCol, descrCol);
        //logTable.setPrefWidth(450);
        //logTable.setPrefHeight(300);
        //logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


    }
}