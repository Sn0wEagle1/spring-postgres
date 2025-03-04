package com.postgresql.springlab.repository;

import com.postgresql.springlab.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByRoomNumber(String roomNumber);  // Найти комнату по номеру
    List<Room> findByType(String type);  // Найти комнаты по типу (например, "Стандарт" или "Люкс")
}