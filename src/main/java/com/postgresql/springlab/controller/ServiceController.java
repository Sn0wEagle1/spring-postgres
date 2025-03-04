package com.postgresql.springlab.controller;

import com.postgresql.springlab.model.Servicing;
import com.postgresql.springlab.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {
    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<Servicing> createService(@RequestBody Servicing service) {
        return ResponseEntity.ok(serviceService.createService(service));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicing> getService(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @GetMapping
    public ResponseEntity<List<Servicing>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicing> updateService(@PathVariable Long id, @RequestBody Servicing service) {
        return ResponseEntity.ok(serviceService.updateService(id, service));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
