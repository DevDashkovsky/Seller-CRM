package com.shiftlab.crm.controller;

import com.shiftlab.crm.dto.request.SellerCreateRequest;
import com.shiftlab.crm.dto.response.SellerResponse;
import com.shiftlab.crm.service.SellerService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {
    private final SellerService service;

    public SellerController(SellerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SellerResponse> create(@Valid @RequestBody SellerCreateRequest request) {
        SellerResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
