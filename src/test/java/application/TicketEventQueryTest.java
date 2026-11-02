package application;

import model.*;
import repository.*;
import service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TicketEventQueryTest — ИНДИВИДУАЛЬНОЕ ЗАДАНИЕ ЛР №4, вариант 9.
 *
 * Вариант 9 — «Система продажи билетов»:
 *   «Добавить новый тест для работы с билетом или событием,
 *    продемонстрировать автоматический запуск Pipeline после изменения
 *    проекта».
 *
 * Этот класс проверяет работу сервиса с БИЛЕТАМИ и СОБЫТИЯМИ:
 *   - билет привязан ровно к одному мероприятию;
 *   - продажа билета на одно мероприятие не влияет на другое;
 *   - отмена билета возвращает места только в его мероприятии;
 *   - новый метод TicketService.getTicketsByEvent() отбирает билеты
 *     по мероприятию;
 *   - инвариант 0 &lt;= availableSeats &lt;= totalSeats не нарушается.
 *
 * Файл добавлен в ЛР №4 и зафиксирован в Git-репозитории:
 *   git add .  ->  git commit -m "Add TicketEventQueryTest (variant 9)"
 *   ->  git push  ->  Jenkins (Poll SCM)  ->  стадия Test
 */
public class TicketEventQueryTest {

    private TicketService ticketService;
    private EventRepository eventRepo;
    private TicketRepository ticketRepo;

    private Event rockConcert;   // мероприятие №1
    private Event jazzNight;      // мероприятие №2
    private Customer ivan;
    private Customer anna;

    @BeforeEach
    void setUp() {
        eventRepo = new InMemoryEventRepository();
        ticketRepo = new InMemoryTicketRepository();
        ticketService = new TicketService(ticketRepo, eventRepo);

        rockConcert = new Event(1, "Rock Concert", "2026-12-01", 10);
        jazzNight = new Event(2, "Jazz Night", "2026-12-15", 4);
        eventRepo.save(rockConcert);
        eventRepo.save(jazzNight);

        ivan = new Customer(1, "Ivan", "ivan@mail.ru");
        anna = new Customer(2, "Anna", "anna@mail.ru");
    }

    /**
     * Билет должен принадлежать ровно одному мероприятию,
     * и это мероприятие должно лежать в репозитории.
     */
    @Test
    void testTicketBelongsToItsOwnEvent() {
        Ticket ticket = ticketService.buyTicket(rockConcert, ivan);

        assertNotNull(ticket, "Ticket should not be null");
        assertEquals(rockConcert.getId(), ticket.getEvent().getId(),
            "Ticket must be linked to the event it was bought for");
        assertEquals("Rock Concert", ticket.getEvent().getName(),
            "Ticket should point to the Rock Concert event");

        assertTrue(eventRepo.findById(rockConcert.getId()).isPresent(),
            "Event must be stored in the event repository");
    }

    /**
     * Продажа билета на Rock Concert не должна уменьшать количество
     * свободных мест у другого мероприятия (Jazz Night).
     */
    @Test
    void testBuyingTicketDoesNotAffectOtherEvent() {
        ticketService.buyTicket(rockConcert, ivan);

        assertEquals(9, rockConcert.getAvailableSeats(),
            "Rock Concert should lose one seat");
        assertEquals(4, jazzNight.getAvailableSeats(),
            "Jazz Night must keep all its seats");
    }

    /**
     * Новый метод сервиса: билеты, выданные на конкретное мероприятие.
     */
    @Test
    void testGetTicketsByEventReturnsOnlyTicketsOfThatEvent() {
        ticketService.buyTicket(rockConcert, ivan);
        ticketService.buyTicket(rockConcert, anna);
        ticketService.buyTicket(jazzNight, anna);

        List<Ticket> rockTickets = ticketService.getTicketsByEvent(rockConcert.getId());
        List<Ticket> jazzTickets = ticketService.getTicketsByEvent(jazzNight.getId());

        assertEquals(2, rockTickets.size(),
            "Rock Concert should have exactly 2 tickets");
        assertEquals(1, jazzTickets.size(),
            "Jazz Night should have exactly 1 ticket");
        assertTrue(rockTickets.stream().allMatch(t -> t.getEvent().getId() == rockConcert.getId()),
            "Every returned ticket must belong to Rock Concert");
        assertEquals(2, jazzTickets.get(0).getCustomer().getId(),
            "Jazz Night ticket belongs to Anna");
    }

    /**
     * Отмена билета возвращает место только в «своём» мероприятии.
     */
    @Test
    void testCancelTicketRestoresSeatsOnlyInItsOwnEvent() {
        Ticket ticket = ticketService.buyTicket(rockConcert, ivan);
        ticketService.buyTicket(jazzNight, anna);

        assertEquals(9, rockConcert.getAvailableSeats(), "Rock Concert: 9 seats left");
        assertEquals(3, jazzNight.getAvailableSeats(), "Jazz Night: 3 seats left");

        ticketService.cancelTicket(ticket.getId());

        assertEquals(10, rockConcert.getAvailableSeats(),
            "Rock Concert should get its seat back");
        assertEquals(3, jazzNight.getAvailableSeats(),
            "Jazz Night must be untouched by the cancellation");
        assertEquals(Ticket.TicketStatus.CANCELLED, ticket.getStatus(),
            "Cancelled ticket should have CANCELLED status");
    }

    /**
     * Бизнес-инвариант мероприятия: свободных мест не может быть
     * больше, чем вместимость, и не может быть меньше нуля.
     */
    @Test
    void testAvailableSeatsNeverExceedTotalSeats() {
        Event smallEvent = new Event(3, "Small Gig", "2026-12-20", 2);
        eventRepo.save(smallEvent);

        ticketService.buyTicket(smallEvent, ivan);
        ticketService.buyTicket(smallEvent, anna);

        assertEquals(0, smallEvent.getAvailableSeats(), "No seats should be left");
        assertTrue(smallEvent.getAvailableSeats() <= smallEvent.getTotalSeats(),
            "availableSeats must never exceed totalSeats");
        assertTrue(smallEvent.getAvailableSeats() >= 0,
            "availableSeats must never be negative");

        // Дополнительный билет купить уже нельзя.
        Customer third = new Customer(3, "Third", "third@mail.ru");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> ticketService.buyTicket(smallEvent, third),
            "Buying a ticket must fail when the event is sold out");
        assertTrue(ex.getMessage().contains("No available seats"));
    }
}
