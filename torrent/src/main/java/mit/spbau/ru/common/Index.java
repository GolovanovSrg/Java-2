package mit.spbau.ru.common;

import mit.spbau.ru.common.Torrent;
import mit.spbau.ru.common.exceptions.IndexIOException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Index implements Serializable {
    private final List<Torrent> torrents = new ArrayList<>();

    public List<Torrent> getTorrents() {
        return torrents;
    }

    public void addTorrent(Torrent torrent) {
        if (!torrents.contains(torrent)) {
            torrents.add(torrent);
        }
    }

    public void removeTorrent(Torrent torrent) {
        torrents.remove(torrent);
    }

    public void save(String path) throws IndexIOException {
        Path indexPath = Paths.get(path).toAbsolutePath().normalize();
        try (FileOutputStream fileOutput = new FileOutputStream(indexPath.toFile());
             ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput)) {
            objectOutput.writeObject(this);
        } catch (IOException e) {
            IndexIOException exception = new IndexIOException("Can not save index to " + path);
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public static Index load(String path) throws IndexIOException {
        Path indexPath = Paths.get(path).toAbsolutePath().normalize();
        try (FileInputStream fileInput = new FileInputStream(indexPath.toFile());
             ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
            return (Index) objectInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            IndexIOException exception = new IndexIOException("Can not load index from " + path);
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
