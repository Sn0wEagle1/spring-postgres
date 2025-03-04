package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Hotel;
import com.postgresql.springlab.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel updateHotel(Long id, Hotel updatedHotel) {
        Hotel hotel = getHotelById(id);
        hotel.setName(updatedHotel.getName());
        hotel.setLocation(updatedHotel.getLocation());
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    public List<Hotel> findByName(String name) {
        return hotelRepository.findByName(name);
    }

    public List<Hotel> findByLocation(String location) {
        return hotelRepository.findByLocation(location);
    }
}
