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
        Event jazz = new Event(2, "Jazz Night", "2026-12-15", 50);
        Customer ivan = new Customer(1, "Ivan", "ivan@mail.ru");
        Customer anna = new Customer(2, "Anna", "anna@mail.ru");
        eventRepo.save(concert);
        eventRepo.save(jazz);

        System.out.println("Available seats BEFORE purchase: "
                + concert.getAvailableSeats());

        Ticket ticket = ticketService.buyTicket(concert, ivan);
        System.out.println("Bought: " + ticket);
        System.out.println("Available seats AFTER purchase: "
                + concert.getAvailableSeats());

        ticketService.buyTicket(jazz, anna);
        System.out.println("All tickets: " + ticketService.getAllTickets().size());
        System.out.println("Tickets of Ivan: "
                + ticketService.getTicketsByCustomer(ivan.getId()).size());

        ticketService.cancelTicket(ticket.getId());
        System.out.println("Ticket cancelled.");
        System.out.println("Available seats AFTER cancel: "
                + concert.getAvailableSeats());
    }
}
