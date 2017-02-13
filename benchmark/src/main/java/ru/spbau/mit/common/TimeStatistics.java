package ru.spbau.mit.common;

import java.util.List;

public class TimeStatistics {
    private final long arrayProcessingTime;
    private final long requestProcessingTime;
    private final long clientWorkTime;

    public static TimeStatistics mean(List<TimeStatistics> statisticses) {
        double arrayProcessingTime = 0;
        double requestProcessingTime = 0;
        double clientWorkTime = 0;

        for (TimeStatistics s : statisticses) {
            arrayProcessingTime += s.getArrayProcessingTime() / statisticses.size();
            requestProcessingTime += s.getRequestProcessingTime() / statisticses.size();
            clientWorkTime += s.getClientWorkTime() / statisticses.size();
        }

        return new TimeStatistics((long) arrayProcessingTime, (long) requestProcessingTime, (long) clientWorkTime);
    }

    public TimeStatistics(long arrayProcessingTime, long requestProcessingTime, long clientWorkTime) {
        this.arrayProcessingTime = arrayProcessingTime;
        this.requestProcessingTime = requestProcessingTime;
        this.clientWorkTime = clientWorkTime;
    }

    public long getArrayProcessingTime() {
        return arrayProcessingTime;
    }

    public long getRequestProcessingTime() {
        return requestProcessingTime;
    }

    public long getClientWorkTime() {
        return clientWorkTime;
    }
}
