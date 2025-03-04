package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Guest;
import com.postgresql.springlab.repository.GuestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestService {
    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Guest createGuest(Guest guest) {
        return guestRepository.save(guest);
    }

    public Guest getGuestById(Long id) {
        return guestRepository.findById(id).orElseThrow(() -> new RuntimeException("Guest not found"));
    }

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Guest updateGuest(Long id, Guest updatedGuest) {
        Guest guest = getGuestById(id);
        guest.setName(updatedGuest.getName());
        guest.setEmail(updatedGuest.getEmail());
        return guestRepository.save(guest);
    }

    public void deleteGuest(Long id) {
        guestRepository.deleteById(id);
    }
}
