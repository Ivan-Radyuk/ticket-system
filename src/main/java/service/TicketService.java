package service;

import model.*;
import repository.TicketRepository;
import repository.EventRepository;
import java.util.List;


public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private int ticketIdCounter = 1;

    public TicketService(TicketRepository ticketRepository,
                         EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }


    public Ticket buyTicket(Event event, Customer customer) {
        if (!event.hasAvailableSeats()) {
            throw new IllegalStateException(
                "No available seats for event: " + event.getName()
            );
        }
        event.decrementSeats();
        eventRepository.save(event);

        Ticket ticket = new Ticket(ticketIdCounter++, event, customer);
        ticketRepository.save(ticket);
        return ticket;
    }


    public void cancelTicket(int ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() ->
                new IllegalStateException("Ticket not found: " + ticketId));

        if (!ticket.isActive()) {
            throw new IllegalStateException(
                "Ticket is already cancelled: " + ticketId);
        }

        ticket.cancel();
        ticket.getEvent().incrementSeats();
        eventRepository.save(ticket.getEvent());
        ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByCustomer(int customerId) {
        return ticketRepository.findByCustomerId(customerId);
    }
}
