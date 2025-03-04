package com.postgresql.springlab.service;

import com.postgresql.springlab.model.Servicing;
import com.postgresql.springlab.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public Servicing createService(Servicing service) {
        return serviceRepository.save(service);
    }

    public Servicing getServiceById(Long id) {
        return serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
    }

    public List<Servicing> getAllServices() {
        return serviceRepository.findAll();
    }

    public Servicing updateService(Long id, Servicing updatedService) {
        Servicing service = getServiceById(id);
        service.setName(updatedService.getName());
        return serviceRepository.save(service);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public List<Servicing> findByName(String name) {
        return serviceRepository.findByName(name);
    }
}