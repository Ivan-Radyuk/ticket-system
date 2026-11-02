package model;

/**
 * Event — мероприятие, на которое продаются билеты.
 *
 * Поля:
 *  - id, name, date (название/дата проведения),
 *  - totalSeats     — общая вместимость,
 *  - availableSeats — сколько мест осталось.
 *
 * Бизнес-инвариант: 0 <= availableSeats <= totalSeats.
 *
 * Изменения availableSeats делаются ТОЛЬКО через decrementSeats
 * / incrementSeats — это обеспечивает инвариант.
 */
public class Event {
    private final int id;
    private final String name;
    private final String date;
    private final int totalSeats;
    private int availableSeats;

    public Event(int id, String name, String date, int totalSeats) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }

    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }

    /** Уменьшает количество доступных мест (при покупке билета). */
    public void decrementSeats() {
        if (availableSeats > 0) {
            availableSeats--;
        }
    }

    /**
     * Увеличивает количество доступных мест (при отмене билета).
     * Не превышает totalSeats.
     */
    public void incrementSeats() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", name='" + name + "', date='" + date
                + "', available=" + availableSeats + "/" + totalSeats + "}";
    }
}
