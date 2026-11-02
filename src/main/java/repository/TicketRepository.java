package repository;

import model.Ticket;
import java.util.List;
import java.util.Optional;


public interface TicketRepository {
    void save(Ticket ticket);
    Optional<Ticket> findById(int id);
    List<Ticket> findAll();
    List<Ticket> findByEventId(int eventId);
    List<Ticket> findByCustomerId(int customerId);
}
