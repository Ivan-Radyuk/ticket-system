package application;

import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.*;
import service.TicketService;



public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {

        EventRepository eventRepo = new InMemoryEventRepository();
        TicketRepository ticketRepo = new InMemoryTicketRepository();

        TicketService ticketService = new TicketService(ticketRepo, eventRepo);

        Event concert = new Event(1, "Rock Concert", "2026-12-01", 100);
        Event jazz = new Event(2, "Jazz Night", "2026-12-15", 50);
        Event soldOut = new Event(3, "Sold Out Gig", "2026-12-20", 1);
        Customer aleksandr = new Customer(1, "Aleksandr", "aleksandr@mail.ru");
        Customer anna = new Customer(2, "Anna", "anna@mail.ru");
        eventRepo.save(concert);
        eventRepo.save(jazz);
        eventRepo.save(soldOut);

        // Сценарий 1 (успех): покупка билета на мероприятие — INFO.
        LOG.info("Сценарий 1: покупка билета. Мероприятие={}, свободных мест до покупки={}",
                concert.getName(), concert.getAvailableSeats());

        Ticket ticket = ticketService.buyTicket(concert, aleksandr);
        LOG.info("Сценарий 1 завершён: билет id={}, покупатель={}, "
                + "свободных мест после покупки={}",
                ticket.getId(), aleksandr.getName(), concert.getAvailableSeats());

        ticketService.buyTicket(jazz, anna);
        LOG.info("Всего продано билетов={}, билетов у покупателя={}",
                ticketService.getAllTickets().size(),
                ticketService.getTicketsByCustomer(aleksandr.getId()).size());

        // Сценарий 2 (успех): отмена купленного билета — INFO.
        LOG.info("Сценарий 2: отмена билета id={}", ticket.getId());
        ticketService.cancelTicket(ticket.getId());
        LOG.info("Сценарий 2 завершён: свободных мест после отмены={}",
                concert.getAvailableSeats());

        // Сценарий 3 (предупреждение): покупка при отсутствии свободных мест — WARN.
        ticketService.buyTicket(soldOut, anna);
        try {
            ticketService.buyTicket(soldOut, aleksandr);
        } catch (IllegalStateException e) {
            LOG.warn("Сценарий 3: покупка отклонена - {}", e.getMessage());
        }

        // Сценарий 4 (ошибка): отмена билета, которого нет в хранилище — ERROR.
        try {
            ticketService.cancelTicket(999);
        } catch (IllegalStateException e) {
            LOG.error("Сценарий 4: отмена билета не выполнена - {}", e.getMessage());
        }
    }
}
