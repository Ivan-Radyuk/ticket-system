package repository;

import model.Ticket;
import java.util.*;


public class InMemoryTicketRepository implements TicketRepository {
    private final Map<Integer, Ticket> storage = new HashMap<>();

    @Override
    public void save(Ticket ticket) {
        storage.put(ticket.getId(), ticket);
    }

    @Override
    public Optional<Ticket> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Ticket> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Ticket> findByEventId(int eventId) {
        return storage.values().stream()
                .filter(t -> t.getEvent().getId() == eventId)
                .toList();
    }

    @Override
    public List<Ticket> findByCustomerId(int customerId) {
        return storage.values().stream()
                .filter(t -> t.getCustomer().getId() == customerId)
                .toList();
    }
}
