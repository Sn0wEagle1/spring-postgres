package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByName(String name);  // Найти отель по названию
    List<Hotel> findByLocation(String location);  // Найти отель по местоположению
}