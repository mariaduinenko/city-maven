package com.citytransportanalysis.modeling;

import com.citytransportanalysis.modeling.entity.RouteSegment;
import com.citytransportanalysis.modeling.entity.Stop;
import com.citytransportanalysis.modeling.entity.Transport;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Modeling class for modeling all that shit.
 */
public class Modeling {

    public List<Event> eventsLog;       // список событий
    public Modeling() {
        System.out.println("Modeling init.");
        eventsLog = new ArrayList<>();
    }


    /**
     * Запуск расчета моделирования
     *
     * @param routeSegments список сегментов пути (между двумя остановками)
     * @param transportList список задействованного транспорта
     * @param startTime     время начала движения
     * @param endTime       время конца движения
     * @param minutesPeriod период между транспортами в минутах
     */
    public void LaunchMovement(LinkedList<RouteSegment> routeSegments, LinkedList<Transport> transportList, LocalTime startTime, LocalTime endTime, long minutesPeriod) {
        double moveTime = 0;
        double length = 0;

        LocalTime lastEndTime = startTime;

        boolean timeIsOver = false;

        while(!timeIsOver && lastEndTime.isBefore(endTime)) {       //пока время движения в заданных рамках
            ListIterator<Transport> transportListIterator = transportList.listIterator(0);  //начинаем с 1 транспорта
            while (transportListIterator.hasNext()) {                //пока транспорты еще есть
                Transport transport = transportListIterator.next();     //берем следующий
                // System.out.printf("Id:%s Time:%s\n", transport.getId(), transport.getStartTime());
                eventsLog.add(new Event(transport.getCurrentTime(), transport, null, null, Event.Type.RouteStart)); // запись начала движения в лог
                for (RouteSegment routeSegment : routeSegments) {       //для каждого сегмента пути
                    Stop stop1 = routeSegment.getTwoStops().get(0);     //берем 2 остановки
                    Stop stop2 = routeSegment.getTwoStops().get(1);

                    if (routeSegment == routeSegments.getFirst()) {     // если остановка первая
                        transport.addMoveTime(stop1.getWaitTime());     //добавляем к общему времени движения транспорта время ожидания на остановке
                        stop1.SettingInTransport(transport);            // садим пассажиров в транспорт
                        transport.addToAllPassengersCount(stop1.getSittedPassengers());     // добавляем к общему счетчику пассажиров за день
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop1, Event.Type.OnStop));        //запись в лог
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop1, Event.Type.SittingPassenger));
                    }

                    transport.addMoveTime(routeSegment.getPassingTime());   //добавляем к общему времени движения транспорта время езды между остановками
                    eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, null, Event.Type.OnWay));  //запись в лог

                    if (routeSegment == routeSegments.getLast()) {  // если остановка последняя
                        transport.addMoveTime(stop2.getWaitTime());    //добавляем к общему времени движения транспорта время ожидания на остановке
                        stop2.GettingOutFromTransport(transport);   // высаживаем пассажиров
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop2, Event.Type.OnStop));    //запись в лог
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop2, Event.Type.GettingOutPassenger));
                    } else {                                           // если остановка не последняя
                        transport.addMoveTime(stop2.getWaitTime());         //добавляем к общему времени движения транспорта время ожидания на остановке
                        stop2.GettingOutFromTransport(transport);           // высаживаем пассажиров
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop2, Event.Type.OnStop));            //запись в лог
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop2, Event.Type.GettingOutPassenger));
                        stop2.SettingInTransport(transport);                // садим пассажиров в транспорт
                        transport.addToAllPassengersCount(stop2.getSittedPassengers());     // добавляем к общему счетчику пассажиров за день
                        eventsLog.add(new Event(transport.getCurrentTime(), transport, routeSegment, stop2, Event.Type.SittingPassenger));      // добавляем к общему счетчику пассажиров за день
                    }

                    //length = routeSegment.getLength();
                    // moveTime = transport.getMoveTime();
                }
                lastEndTime = transport.getCurrentTime();           // сохраняем время конца движения транспорта

                transport.setEndTime(lastEndTime);          // записываем время конца движения транспорта
                eventsLog.add(new Event(transport.getCurrentTime(), transport, null, null, Event.Type.RouteFinish));    //запись в лог
                transport.cleanMoveTime();          // обнуляем время в движении
                transport.addTripCount();           // добавляем 1 к счетчику поездок за день

                /* Определяем время начала движения следующего транспорта */
                LocalTime newStartTime;
                if(transportListIterator.previousIndex()!=0){
                    Transport previousTransport = transportList.get(transportListIterator.previousIndex()-1);
                    newStartTime = previousTransport.getStartTime().plusMinutes(minutesPeriod);
                }
                else{
                    newStartTime = transportList.getLast().getStartTime().plusMinutes(minutesPeriod);
                }
                transport.setStartTime(newStartTime);

                /* Если подошло время конца движения - конец */
                if(lastEndTime.isAfter(endTime)) {
                    timeIsOver = true;
                    break;
                }
            }

        }
        //System.out.printf("Шлях %s метрів пройдений за %s секунд\n", length, moveTime);
        /*  Записываем для каждого транспорта кол-во раз пройденного пути и кол-во перевезенных людей*/
        for (Transport transport : transportList) {
            System.out.printf("%s %d по маршруту №%s проходив шлях %d разів і перевіз %d чел\n",
                    transport.getTransportType().name(), transport.getId(), transport.getRouteNumber(), transport.getTripCount(), transport.getAllPassengersCount());
            eventsLog.add(new Event(transport.getCurrentTime(), transport, null, null, Event.Type.EndDay));
        }
    }

    /**
     * Генерация транспорта
     *
     * @param count         количество транспорта
     * @param sitPlaces     мест для сидения
     * @param standPlaces   мест для стояния
     * @param startTime     время начала движения
     * @param minutesPeriod период движения в минутах
     * @return Список транспорта по порядку, учавствующего в поездке
     */
    public LinkedList<Transport> transportData(int count, int sitPlaces, int standPlaces, LocalTime startTime, long minutesPeriod) {
        LinkedList<Transport> transportList = new LinkedList<>();

        if (startTime != null) {
            //long minutesPeriod = 10;
            if (minutesPeriod > 0) {
                for (int i = 1; i <= count; i++) {
                    Transport marshrutka = new Transport();
                    marshrutka.setId(i);
                    marshrutka.setRouteNumber("--");
                    marshrutka.setSeatPlaces(sitPlaces);
                    marshrutka.setStandPlaces(standPlaces);
                    marshrutka.setStatus(Transport.Status.OnStop);
                    marshrutka.setTransportType(Transport.Type.Microbus);
                    marshrutka.setPassengers(new ArrayList<>());
                    marshrutka.setStartTime(startTime);
                    startTime = startTime.plusMinutes(minutesPeriod);

                    transportList.add(marshrutka);
                }
                return transportList;
            } else {
                throw new RuntimeException();
            }
        } else {
            throw new NullPointerException();
        }
    }

    private Map<LocalTime, Double> passengerComingTimeGen(){
        return new HashMap<LocalTime, Double>() {
            {
                put(LocalTime.parse("06:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("07:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("08:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("09:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("10:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("11:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("12:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("13:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("14:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("15:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("16:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("17:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("18:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("19:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("20:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("21:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
                put(LocalTime.parse("22:00"), ThreadLocalRandom.current().nextDouble(100.0, 300.0));
            }
        };
    }

    private Map<LocalTime, Double> passengerExitProbabilityGen(){
        return new HashMap<LocalTime, Double>() {
            {
                put(LocalTime.parse("06:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("07:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("08:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("09:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("10:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("11:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("12:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("13:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("14:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("15:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("16:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("17:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("18:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("19:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("20:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("21:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
                put(LocalTime.parse("22:00"), ThreadLocalRandom.current().nextDouble(0.12, 0.30));
            }
        };
    }

    public LinkedList<RouteSegment> routeData() {

        Map<LocalTime, Double> passengerComingTimeLast = new HashMap<LocalTime, Double>() {
            {
                put(LocalTime.parse("06:00"), 0.0);
                put(LocalTime.parse("07:00"), 0.0);
                put(LocalTime.parse("08:00"), 0.0);
                put(LocalTime.parse("09:00"), 0.0);
                put(LocalTime.parse("10:00"), 0.0);
                put(LocalTime.parse("11:00"), 0.0);
                put(LocalTime.parse("12:00"), 0.0);
                put(LocalTime.parse("13:00"), 0.0);
                put(LocalTime.parse("14:00"), 0.0);
                put(LocalTime.parse("15:00"), 0.0);
                put(LocalTime.parse("16:00"), 0.0);
                put(LocalTime.parse("17:00"), 0.0);
                put(LocalTime.parse("18:00"), 0.0);
                put(LocalTime.parse("19:00"), 0.0);
                put(LocalTime.parse("20:00"), 0.0);
                put(LocalTime.parse("21:00"), 0.0);
                put(LocalTime.parse("22:00"), 0.0);
            }
        };
        Map<LocalTime, Double> passengerExitProbability = new HashMap<LocalTime, Double>() {
            {
                put(LocalTime.parse("06:00"), 0.0);
                put(LocalTime.parse("07:00"), 0.0);
                put(LocalTime.parse("08:00"), 0.0);
                put(LocalTime.parse("09:00"), 0.0);
                put(LocalTime.parse("10:00"), 0.0);
                put(LocalTime.parse("11:00"), 0.0);
                put(LocalTime.parse("12:00"), 0.0);
                put(LocalTime.parse("13:00"), 0.0);
                put(LocalTime.parse("14:00"), 0.0);
                put(LocalTime.parse("15:00"), 0.0);
                put(LocalTime.parse("16:00"), 0.0);
                put(LocalTime.parse("17:00"), 0.0);
                put(LocalTime.parse("18:00"), 0.0);
                put(LocalTime.parse("19:00"), 0.0);
                put(LocalTime.parse("20:00"), 0.0);
                put(LocalTime.parse("21:00"), 0.0);
                put(LocalTime.parse("22:00"), 0.0);
            }
        };
        Map<LocalTime, Double> passengerExitProbabilityLast = new HashMap<LocalTime, Double>() {
            {
                put(LocalTime.parse("06:00"), 1.0);
                put(LocalTime.parse("07:00"), 1.0);
                put(LocalTime.parse("08:00"), 1.0);
                put(LocalTime.parse("09:00"), 1.0);
                put(LocalTime.parse("10:00"), 1.0);
                put(LocalTime.parse("11:00"), 1.0);
                put(LocalTime.parse("12:00"), 1.0);
                put(LocalTime.parse("13:00"), 1.0);
                put(LocalTime.parse("14:00"), 1.0);
                put(LocalTime.parse("15:00"), 1.0);
                put(LocalTime.parse("16:00"), 1.0);
                put(LocalTime.parse("17:00"), 1.0);
                put(LocalTime.parse("18:00"), 1.0);
                put(LocalTime.parse("19:00"), 1.0);
                put(LocalTime.parse("20:00"), 1.0);
                put(LocalTime.parse("21:00"), 1.0);
                put(LocalTime.parse("22:00"), 1.0);
            }
        };
        Stop stop1 = new Stop("Станція метро \"Лівобережна\"", passengerComingTimeGen(), passengerExitProbability, 30);
        Stop stop2 = new Stop("Вулиця Ентузіастів", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop3 = new Stop("Пішохідний міст", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop4 = new Stop("Бібліотека", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop5 = new Stop("Бювет", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop6 = new Stop("Пошта №154", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop7 = new Stop("Бульвар Олексія Давидова", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop8 = new Stop("Готель \"Славутич\"", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop9 = new Stop("Бульвар Олексія Давидова", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop10 = new Stop("Пошта №154", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop11 = new Stop("Залізнична платформа Київ-Русанівка", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop12 = new Stop("Бібліотека", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop13 = new Stop("Пішохідний міст", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop14 = new Stop("Вулиця Ентузіастів", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop15 = new Stop("Вулиця Раїси Окіпної", passengerComingTimeGen(), passengerExitProbabilityGen(), 20);
        Stop stop16 = new Stop("Станція метро \"Лівобережна\"", passengerComingTimeLast, passengerExitProbabilityLast, 30);


        LinkedList<RouteSegment> routeSegments = new LinkedList<>();
        routeSegments.add(new RouteSegment(stop1, stop2, 200, 1000));
        routeSegments.add(new RouteSegment(stop2, stop3, 100, 500));
        routeSegments.add(new RouteSegment(stop3, stop4, 100, 500));
        routeSegments.add(new RouteSegment(stop4, stop5, 60, 300));
        routeSegments.add(new RouteSegment(stop5, stop6, 60, 300));
        routeSegments.add(new RouteSegment(stop6, stop7, 120, 600));
        routeSegments.add(new RouteSegment(stop7, stop8, 80, 400));
        routeSegments.add(new RouteSegment(stop8, stop9, 80, 400));
        routeSegments.add(new RouteSegment(stop9, stop10, 80, 400));
        routeSegments.add(new RouteSegment(stop10, stop11, 80, 400));
        routeSegments.add(new RouteSegment(stop11, stop12, 60, 300));
        routeSegments.add(new RouteSegment(stop12, stop13, 120, 600));
        routeSegments.add(new RouteSegment(stop13, stop14, 100, 500));
        routeSegments.add(new RouteSegment(stop14, stop15, 100, 500));
        routeSegments.add(new RouteSegment(stop15, stop16, 120, 600));

        return routeSegments;
    }

    /**
     * Класс для вывода событий
     */

    public static class Event {
        private LocalTime time;
        //private String description;
        private Stop stop;
        private Transport transport;
        private RouteSegment routeSegment;
        private Type type;
        private int filledPlaces;
        private int passengersOnStop;
        private int passengersLeft;
        private SimpleStringProperty textTime;
        private SimpleStringProperty description;
        private SimpleIntegerProperty transportId;

        public Transport getTransport() {
            return transport;
        }

        public Type getType() {
            return type;
        }

        /**
         * @return Заполненность транспорта в момент события
         */
        public int getFilledPlaces() {
            return filledPlaces;
        }

        /**
         * @return число пассажиров на остановке во время подъезда транспорта
         */
        public int getPassengersOnStop() {
            return passengersOnStop;
        }

        /**
         * @return число пассажиров не влезших в транспорт
         */
        public int getPassengersLeft() {
            return passengersLeft;
        }

        /**
         * Тип события
         */
        public enum Type {
            /**
             * Начало движения
             */
            RouteStart,
            /**
             * Конец движения
             */
            RouteFinish,
            /**
             * На остановке
             */
            OnStop,
            /**
             * В пути
             */
            OnWay,
            /**
             * Посадка пассажиров
             */
            SittingPassenger,
            /**
             * Высадка пассажиров
             */
            GettingOutPassenger,
            /**
             * Конец рабочего дня
             */
            EndDay
        }

        /**
         * Инициализация объекта события
         * @param time текущее время    {@link LocalTime}
         * @param transport текущий транспорт   {@link Transport}
         * @param routeSegment текущий сегмент пути {@link RouteSegment}
         * @param stop текущая остановка    {@link Stop}
         * @param type тип события {@link Type}
         */
        Event(LocalTime time, Transport transport, RouteSegment routeSegment, Stop stop, Type type) {
            this.time = time;
            this.transport = transport;
            if (type.equals(Type.OnWay)) filledPlaces = transport.getPassengers().size();
            if (type.equals(Type.OnStop)) {
                passengersOnStop = stop.getPassengersOnStop();
                passengersLeft = stop.getPassengersLeft();
            }
            this.routeSegment = routeSegment;
            this.stop = stop;
            this.type = type;


            this.textTime = new SimpleStringProperty(time.toString());
            this.transportId = new SimpleIntegerProperty(transport.getId());
            this.description = new SimpleStringProperty(eventDescription(type));
        }

        /**
         * Вывод текстового описания события
         * @param eventType тип события {@link Type}
         * @return Описание события
         */
        private String eventDescription(Type eventType) {
            switch (eventType) {
                case RouteStart:
                    return String.format("Початок руху о %s\n", transport.getStartTime());
                case RouteFinish:
                    return String.format("Кінець руху о %s. Пройдено за %s секунд\n", transport.getCurrentTime(), transport.getMoveTime());
                case OnStop:
                    return String.format("Зупинка на зупинці \"%s\" %s секунд\n", stop.getName(), stop.getWaitTime());
                case OnWay:
                    return String.format("Їде між зупинками \"%s\" і \"%s\" %s секунд\n", routeSegment.getTwoStops().get(0).getName(), routeSegment.getTwoStops().get(1).getName(), routeSegment.getPassingTime());
                case SittingPassenger:
                    return String.format("Посадка. Зайшло %d чел, зайнято %d/%d місць. Не зайшло %d чел\n", stop.getSittedPassengers(), transport.getOccupiedPlaces(), transport.getTotalPlaces(), stop.getPassengers().size());
                case GettingOutPassenger:
                    return String.format("Висадка. Вийшло %d чел, зайнято %d/%d місць.\n", stop.getGettedOutPassengers(), transport.getOccupiedPlaces(), transport.getTotalPlaces());
                default:
                    return "NONE";
            }
        }

        /**
         * @return Текущая остановка
         */
        public Stop getStop() {
            return stop;
        }

        /**
         * @return Текущее время
         */
        public LocalTime getTime() {
            return time;
        }

        public String getDescription() {
            return description.get();
        }

        public int getTransportId() {
            return transportId.get();
        }

        public SimpleStringProperty textTimeProperty() {
            return textTime;
        }

        public void setTextTime(String textTime) {
            this.textTime.set(textTime);
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        @Override
        public String toString() {
            return (String.format("[%s](Транспорт №%d) %s", textTime.get(), transportId.get(), description.get()));
        }
    }

}

