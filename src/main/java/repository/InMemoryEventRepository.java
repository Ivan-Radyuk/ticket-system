package repository;

import model.Event;
import java.util.*;


public class InMemoryEventRepository implements EventRepository {
    private final Map<Integer, Event> storage = new HashMap<>();

    @Override
    public void save(Event event) {
        storage.put(event.getId(), event);
    }

    @Override
    public Optional<Event> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(storage.values());
    }
}
