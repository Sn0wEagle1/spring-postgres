package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCheckInDate(LocalDate checkInDate);  // Найти бронирования по дате заезда
    List<Booking> findByCheckOutDate(LocalDate checkOutDate);  // Найти бронирования по дате выезда
}
