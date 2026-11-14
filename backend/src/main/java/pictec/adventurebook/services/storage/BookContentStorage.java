package pictec.adventurebook.services.storage;

public interface BookContentStorage {
    void save(String key, byte[] content);

    byte[] load(String key);

    void delete(String key);
}