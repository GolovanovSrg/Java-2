package ru.spbau.mit.benchmark.client;

public class BenchmarkIntArray {
    private final Integer begin;
    private final Integer end;
    private final Integer step;
    private int current;

    public BenchmarkIntArray(int begin, int end, int step) {
        this.begin = begin;
        this.end = end;
        this.step = step;
        current = begin;
    }

    public boolean hasNext() {
        return current <= end;
    }

    public Integer next() {
        if (current > end) {
            return null;
        }

        int result = current;
        current += step;
        return result;
    }

    public void reset() {
        current = begin;
    }
}
