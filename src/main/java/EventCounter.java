package main.java;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public interface EventCounter {
    Map<Date, AtomicInteger> events = new ConcurrentHashMap<>();
    ThreadLocal<Calendar> calendar = ThreadLocal.withInitial(Calendar::getInstance);

    void stateEvent();
    BigInteger getEventCountForLastMinute();
    BigInteger getEventCountForLastHour();
    BigInteger getEventCountForLastDay();
}
