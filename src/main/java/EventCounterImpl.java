package main.java;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventCounterImpl implements EventCounter {
    private volatile static EventCounterImpl instance;

    private EventCounterImpl() {
    }

    private static class TaskCleaningMap extends TimerTask {
        @Override
        public void run() {
            events.keySet().removeIf(key -> {
                long diff = getTrimmedDateToSeconds().getTime() - key.getTime();
                return TimeUnit.MILLISECONDS.toHours(diff) > 24 ||
                        (TimeUnit.MILLISECONDS.toHours(diff) == 24 &&
                                (TimeUnit.MILLISECONDS.toMinutes(diff) > 0 || TimeUnit.MILLISECONDS.toSeconds(diff) > 0));
            });
        }
    }

    public static EventCounterImpl getInstance() {
        if (instance == null) {
            synchronized (EventCounterImpl.class) {
                if (instance == null) {
                    instance = new EventCounterImpl();
                    new Timer().schedule(new TaskCleaningMap(), 0, 1000 * 60 * 60 * 24);
                }
            }
        }

        return instance;
    }

    @Override
    public void stateEvent() {
        events.computeIfAbsent(getTrimmedDateToSeconds(), date -> new AtomicInteger(0)).incrementAndGet();
    }

    private static Date getTrimmedDateToSeconds() {
        Date date = new Date();
        calendar.get().setTime(date);
        calendar.get().set(Calendar.MILLISECOND, 0);

        return calendar.get().getTime();
    }

    @Override
    public BigInteger getEventCountForLastMinute() {
        return events.entrySet().parallelStream().
                filter(entry -> TimeUnit.MILLISECONDS.toSeconds(
                        getTrimmedDateToSeconds().getTime() - entry.getKey().getTime()) <= 60).
                mapToInt(entry -> entry.getValue().intValue()).
                mapToObj(BigInteger::valueOf).
                reduce(BigInteger.ZERO, BigInteger::add);
    }

    @Override
    public BigInteger getEventCountForLastHour() {
        return events.entrySet().parallelStream().
                filter(entry -> {
                    long diff = getTrimmedDateToSeconds().getTime() - entry.getKey().getTime();
                    return TimeUnit.MILLISECONDS.toMinutes(diff) < 60 ||
                            (TimeUnit.MILLISECONDS.toMinutes(diff) == 60 && TimeUnit.MILLISECONDS.toSeconds(diff) == 0);
                }).
                mapToInt(entry -> entry.getValue().intValue()).
                mapToObj(BigInteger::valueOf).
                reduce(BigInteger.ZERO, BigInteger::add);
    }

    @Override
    public BigInteger getEventCountForLastDay() {
        return events.entrySet().parallelStream().
                filter(entry -> {
                    long diff = getTrimmedDateToSeconds().getTime() - entry.getKey().getTime();
                    return TimeUnit.MILLISECONDS.toHours(diff) < 24 ||
                            (TimeUnit.MILLISECONDS.toHours(diff) == 24 && TimeUnit.MILLISECONDS.toMinutes(diff) == 0 &&
                                    TimeUnit.MILLISECONDS.toSeconds(diff) == 0);
                }).
                mapToInt(entry -> entry.getValue().intValue()).
                mapToObj(BigInteger::valueOf).
                reduce(BigInteger.ZERO, BigInteger::add);
    }
}
