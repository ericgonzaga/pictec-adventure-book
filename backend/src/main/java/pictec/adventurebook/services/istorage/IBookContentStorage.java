package pictec.adventurebook.services.istorage;

public interface IBookContentStorage {
    void save(String key, byte[] content);

    byte[] load(String key);

    void delete(String key);
}