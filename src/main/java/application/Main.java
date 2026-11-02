package application;

import model.*;
import repository.*;
import service.TicketService;


public class Main {
    public static void main(String[] args) {

        EventRepository eventRepo = new InMemoryEventRepository();
        TicketRepository ticketRepo = new InMemoryTicketRepository();

        TicketService ticketService = new TicketService(ticketRepo, eventRepo);

        Event concert = new Event(1, "Rock Concert", "2026-12-01", 100);
        Customer ivan = new Customer(1, "Ivan", "ivan@mail.ru");
        eventRepo.save(concert);

        System.out.println("Available seats BEFORE purchase: "
                + concert.getAvailableSeats());

        Ticket ticket = ticketService.buyTicket(concert, ivan);
        System.out.println("Bought: " + ticket);
        System.out.println("Available seats AFTER purchase: "
                + concert.getAvailableSeats());

        ticketService.cancelTicket(ticket.getId());
        System.out.println("Ticket cancelled.");
        System.out.println("Available seats AFTER cancel: "
                + concert.getAvailableSeats());
    }
}
