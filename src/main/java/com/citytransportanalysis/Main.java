package com.citytransportanalysis;

import com.citytransportanalysis.gui.Controller;
import com.citytransportanalysis.modeling.Modeling;
import com.citytransportanalysis.modeling.entity.RouteSegment;
import com.citytransportanalysis.modeling.entity.Transport;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.LinkedList;

import static javafx.application.Platform.exit;

public class Main extends Application {

    public static void main(String[] args) {

        // моделирование с выводом в консоль пока что
        Modeling modeling = new Modeling();

        LocalTime startTime = LocalTime.parse("06:00");     //время начала движения
        LocalTime endTime = LocalTime.parse("22:00");       //время конца движения
        LinkedList<RouteSegment> routeSegments = modeling.routeData();      //список участков пути (между двумя остановками)
        LinkedList<Transport> transportList = modeling.transportData(5, 22, 21, startTime, 10);    //список список маршруток (одинаковых), период движения

        /** TODO (Вове) надо сделать отдельный класс для загрузки начальных данных с XML файла
         *  На вход подается имя xml файла
         *  На выходе - 2 списка (LinkedList<RouteSegment> routeSegments и LinkedList<Transport> transportList,
         *  пример заполнения списков в функциях {@link Modeling.routeData()} и {@link Modeling.transportData()}, они сейчас генерируются рандомно)
         *  Так же с XML файла считывать время начала пути, конца. Пока что все, .
         */

        /** TODO (Ире) визуализация
         *  Вся нужная инфа для визуализации хранится тут List<Event> eventsLog {@link Modeling.eventsLog}
         *  Там находятся данные по порядку событий начиная с времени старта до конца всего движения (типа 06:00 начало движения, посадка пассажиров и т.д.)
         *  Так же там есть все участвующие объекты (Transport - текущая маршрутка, Stop - текущая остановка, RouteSegment - сегмент пути от 1 остановки к другой)
         *
         *  Граф. интерфейс в папке gui где main.fxml - шаблон формы и управляется она методами в классе {@link com.citytransportanalysis.gui.Controller}
         *  После нажатия на кнопку запуска моделирования список событий заносится сюда {@link com.citytransportanalysis.gui.Controller.eventsLog}
         *  Нужно будет использовать его
         *
         *  Если есть вопросы, пиши
         */

        //modeling.LaunchMovement(routeSegments, transportList, startTime, endTime, 10);  //запуск моделирования

        //вывод текстового лога в консоль
        //Collections.sort(modeling.eventsLog, Comparator.comparing(Event::getTime));    //сортировка лога по времени

        //for (Modeling.Event event : modeling.eventsLog) {     //сам вывод лога в консоль построчно
        //    System.out.printf(event.toString());
        //}


        launch(args);     //запустить граф. интерфейс.

        exit();     //завершение проги
    }

    /**
     * For GUI.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        System.out.println("koko" + getClass().getCanonicalName());
        loader.setLocation(getClass().getResource("/gui/main.fxml"));
        Parent root =  loader.load();
        primaryStage.setTitle("City Transport Analysis");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }
}
