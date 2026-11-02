package model;

/**
 * Ticket — билет, выданный покупателю на конкретное мероприятие.
 * Имеет жизненный цикл: ACTIVE → CANCELLED.
 *
 * Связь с Event и Customer — агрегация: билет хранит ссылки,
 * но не владеет ими «жёстко».
 */
public class Ticket {
    private final int id;
    private final Event event;
    private final Customer customer;
    private TicketStatus status;

    public enum TicketStatus {
        ACTIVE,
        CANCELLED
    }

    public Ticket(int id, Event event, Customer customer) {
        this.id = id;
        this.event = event;
        this.customer = customer;
        this.status = TicketStatus.ACTIVE;
    }

    public int getId() { return id; }
    public Event getEvent() { return event; }
    public Customer getCustomer() { return customer; }
    public TicketStatus getStatus() { return status; }

    /** Переводит билет в статус CANCELLED. */
    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == TicketStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return "Ticket{id=" + id + ", event=" + event.getName()
                + ", customer=" + customer.getName() + ", status=" + status + "}";
    }
}
