package application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import model.Customer;
import model.Event;
import model.Ticket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import repository.EventRepository;
import repository.InMemoryEventRepository;
import repository.InMemoryTicketRepository;
import repository.TicketRepository;
import service.TicketService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/**
 * TicketServiceLoggingTest — проверка журналирования сервисного слоя (ЛР №5).
 *
 * К сообщениям Logger подключается ListAppender — он собирает
 * события журнала в список, поэтому тест может проверить уровень
 * и текст сообщения без чтения консоли.
 *
 * Проверяются:
 *   INFO  — успешная покупка билета;
 *   WARN  — попытка купить билет при отсутствии свободных мест;
 *   ERROR — отмена несуществующего билета и сбой хранилища.
 */
public class TicketServiceLoggingTest {

    private LoggerContext loggerContext;
    private ListAppender<ILoggingEvent> appender;
    private ch.qos.logback.classic.Logger serviceLogger;

    @BeforeEach
    void setUp() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        serviceLogger = loggerContext.getLogger(TicketService.class);
        appender = new ListAppender<>();
        appender.start();
        serviceLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        serviceLogger.detachAppender(appender);
        appender.stop();
    }

    private List<ILoggingEvent> events() {
        return appender.list;
    }

    private boolean hasEvent(Level level, String textPart) {
        return events().stream()
                .anyMatch(e -> e.getLevel() == level && e.getFormattedMessage().contains(textPart));
    }

    @Test
    void testSuccessfulPurchaseIsLoggedAsInfo() {
        EventRepository eventRepo = new InMemoryEventRepository();
        TicketRepository ticketRepo = new InMemoryTicketRepository();
        TicketService ticketService = new TicketService(ticketRepo, eventRepo);

        Event concert = new Event(1, "Rock Concert", "2026-12-01", 10);
        Customer ivan = new Customer(1, "Ivan", "ivan@mail.ru");
        eventRepo.save(concert);

        ticketService.buyTicket(concert, ivan);

        assertTrue(hasEvent(Level.INFO, "Билет куплен"),
                "Успешная покупка должна быть записана на уровне INFO");
        assertTrue(hasEvent(Level.INFO, "Rock Concert"),
                "В сообщении INFO должно быть имя мероприятия");
    }

    @Test
    void testPurchaseWithoutFreeSeatsIsLoggedAsWarn() {
        EventRepository eventRepo = new InMemoryEventRepository();
        TicketRepository ticketRepo = new InMemoryTicketRepository();
        TicketService ticketService = new TicketService(ticketRepo, eventRepo);

        Event concert = new Event(1, "Rock Concert", "2026-12-01", 1);
        Customer ivan = new Customer(1, "Ivan", "ivan@mail.ru");
        Customer anna = new Customer(2, "Anna", "anna@mail.ru");
        eventRepo.save(concert);
        ticketService.buyTicket(concert, ivan);

        assertThrows(IllegalStateException.class,
                () -> ticketService.buyTicket(concert, anna));

        assertTrue(hasEvent(Level.WARN, "свободных мест нет"),
                "Покупка при отсутствии мест должна быть записана на уровне WARN");
    }

    @Test
    void testCancelOfUnknownTicketIsLoggedAsError() {
        EventRepository eventRepo = new InMemoryEventRepository();
        TicketRepository ticketRepo = new InMemoryTicketRepository();
        TicketService ticketService = new TicketService(ticketRepo, eventRepo);

        assertThrows(IllegalStateException.class, () -> ticketService.cancelTicket(999));

        assertTrue(hasEvent(Level.ERROR, "не найден"),
                "Отмена несуществующего билета должна быть записана на уровне ERROR");
    }

    @Test
    void testStorageFailureIsLoggedAsError() {
        EventRepository eventRepo = new InMemoryEventRepository();
        TicketRepository failingRepo = new FailingTicketRepository();
        TicketService ticketService = new TicketService(failingRepo, eventRepo);

        Event concert = new Event(1, "Rock Concert", "2026-12-01", 10);
        Customer ivan = new Customer(1, "Ivan", "ivan@mail.ru");
        eventRepo.save(concert);

        assertThrows(IllegalStateException.class,
                () -> ticketService.buyTicket(concert, ivan));

        assertTrue(hasEvent(Level.ERROR, "Не удалось сохранить билет в хранилище"),
                "Сбой хранилища должен быть записан на уровне ERROR");
    }


    /**
     * Хранилище билетов, которое всегда отказывает при сохранении —
     * эмуляция сбоя хранилища для проверки уровня ERROR.
     */
    private static final class FailingTicketRepository implements TicketRepository {

        @Override
        public void save(Ticket ticket) {
            throw new IllegalStateException("Storage is unavailable");
        }

        @Override
        public Optional<Ticket> findById(int id) {
            return Optional.empty();
        }

        @Override
        public List<Ticket> findAll() {
            return List.of();
        }

        @Override
        public List<Ticket> findByEventId(int eventId) {
            return List.of();
        }

        @Override
        public List<Ticket> findByCustomerId(int customerId) {
            return List.of();
        }
    }
}
