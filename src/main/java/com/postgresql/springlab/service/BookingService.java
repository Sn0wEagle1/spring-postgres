package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Booking;
import com.postgresql.springlab.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking updateBooking(Long id, Booking updatedBooking) {
        Booking booking = getBookingById(id);
        booking.setCheckInDate(updatedBooking.getCheckInDate());
        booking.setCheckOutDate(updatedBooking.getCheckOutDate());
        booking.setGuest(updatedBooking.getGuest());
        booking.setRoom(updatedBooking.getRoom());
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public List<Booking> findByCheckInDate(LocalDate checkInDate) {
        return bookingRepository.findByCheckInDate(checkInDate);
    }

    public List<Booking> findByCheckOutDate(LocalDate checkOutDate) {
        return bookingRepository.findByCheckOutDate(checkOutDate);
    }
}
