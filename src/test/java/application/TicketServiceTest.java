package application;

import model.*;
import repository.*;
import service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TicketServiceTest {

    private TicketService ticketService;
    private EventRepository eventRepo;
    private TicketRepository ticketRepo;
    private Event concert;
    private Customer customer;

    @BeforeEach
    void setUp() {
        eventRepo = new InMemoryEventRepository();
        ticketRepo = new InMemoryTicketRepository();
        ticketService = new TicketService(ticketRepo, eventRepo);

        concert = new Event(1, "Rock Concert", "2026-12-01", 10);
        customer = new Customer(1, "Ivan", "ivan@mail.ru");
        eventRepo.save(concert);
    }

    @Test
    void testBuyTicket() {
        Ticket ticket = ticketService.buyTicket(concert, customer);

        assertNotNull(ticket, "Ticket should not be null");
        assertEquals(Ticket.TicketStatus.ACTIVE, ticket.getStatus(),
            "New ticket should be ACTIVE");
        assertEquals(concert.getId(), ticket.getEvent().getId(),
            "Ticket should link to the correct event");
        assertEquals(customer.getId(), ticket.getCustomer().getId(),
            "Ticket should link to the correct customer");
        assertEquals(9, concert.getAvailableSeats(),
            "Available seats should decrease by 1");
    }

    @Test
    void testBuyTicketInsufficientCapacity() {
        for (int i = 0; i < 10; i++) {
            Customer c = new Customer(100 + i, "Cust" + i, "c" + i + "@mail.ru");
            ticketService.buyTicket(concert, c);
        }

        assertEquals(0, concert.getAvailableSeats(),
            "All seats should be sold");

        Customer extra = new Customer(999, "Extra", "extra@mail.ru");
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> ticketService.buyTicket(concert, extra),
            "Buying ticket should fail when no seats available"
        );
        assertTrue(ex.getMessage().contains("No available seats"));
    }

    @Test
    void testCancelTicketRestoresCapacity() {
        Ticket ticket = ticketService.buyTicket(concert, customer);

        assertEquals(9, concert.getAvailableSeats(),
            "Should have 9 seats after purchase");

        ticketService.cancelTicket(ticket.getId());

        assertEquals(Ticket.TicketStatus.CANCELLED, ticket.getStatus(),
            "Ticket should be CANCELLED after cancel");
        assertEquals(10, concert.getAvailableSeats(),
            "Available seats should be restored after cancellation");

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> ticketService.cancelTicket(ticket.getId()),
            "Cancelling already cancelled ticket should throw"
        );
        assertTrue(ex.getMessage().contains("already cancelled"));
    }
}
