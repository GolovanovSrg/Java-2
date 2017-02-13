package ru.spbau.mit.kernel.server;

import ru.spbau.mit.kernel.server.exceptions.ServerException;

import java.util.ArrayList;
import java.util.List;

public abstract class Server {
    protected static final int PORT = 3666;

    public abstract void start() throws ServerException;

    public abstract void stop() throws ServerException;

    protected long sortArray(ArrayList<Integer> array) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < array.size(); i++) {
            for(int j = array.size() - 1; j > i; j--) {
                if (array.get(j - 1) > array.get(j)) {
                    int tmp = array.get(j - 1);
                    array.set(j - 1, array.get(j));
                    array.set(j, tmp);
                }
            }
        }

        return System.currentTimeMillis() - startTime;
    }
}
