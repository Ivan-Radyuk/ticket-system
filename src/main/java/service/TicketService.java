package service;

import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.TicketRepository;
import repository.EventRepository;
import java.util.List;



public class TicketService {

    /**
     * 111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111
     */
    private static final Logger LOG = LoggerFactory.getLogger(TicketService.class);

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
            LOG.warn("Покупка билета невозможна: свободных мест нет. "
                    + "Мероприятие={}, доступно мест={} ",
                    event.getName(), event.getAvailableSeats());
            throw new IllegalStateException(
                "No available seats for event: " + event.getName()
            );
        }
        event.decrementSeats();
        try {
            eventRepository.save(event);
        } catch (RuntimeException e) {
            LOG.error("Не удалось сохранить мероприятие в хранилище: {}",
                    event.getName(), e);
            throw e;
        }

        Ticket ticket = new Ticket(ticketIdCounter++, event, customer);
        try {
            ticketRepository.save(ticket);
        } catch (RuntimeException e) {
            LOG.error("Не удалось сохранить билет в хранилище: id={}",
                    ticket.getId(), e);
            throw e;
        }

        LOG.info("Билет куплен: id={}, мероприятие={}, дата={}, покупатель={}, "
                + "осталось мест={}",
                ticket.getId(), event.getName(), event.getDate(),
                customer.getName(), event.getAvailableSeats());
        return ticket;
    }


    public void cancelTicket(int ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            IllegalStateException error =
                    new IllegalStateException("Ticket not found: " + ticketId);
            LOG.error("Отмена билета невозможна: билет с id={} не найден в хранилище",
                    ticketId, error);
            throw error;
        }

        if (!ticket.isActive()) {
            IllegalStateException error =
                    new IllegalStateException(
                            "Ticket is already cancelled: " + ticketId);
            LOG.warn("Отмена билета невозможна: билет с id={} уже отменён",
                    ticketId, error);
            throw error;
        }

        ticket.cancel();
        ticket.getEvent().incrementSeats();
        try {
            eventRepository.save(ticket.getEvent());
            ticketRepository.save(ticket);
        } catch (RuntimeException e) {
            LOG.error("Не удалось сохранить отмену билета в хранилище: id={}",
                    ticketId, e);
            throw e;
        }

        LOG.info("Билет отменён: id={}, мероприятие={}, покупатель={}, свободных мест={}",
                ticket.getId(), ticket.getEvent().getName(),
                ticket.getCustomer().getName(),
                ticket.getEvent().getAvailableSeats());
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByCustomer(int customerId) {
        return ticketRepository.findByCustomerId(customerId);
    }

    public List<Ticket> getTicketsByEvent(int eventId) {
        return ticketRepository.findByEventId(eventId);
    }
}
