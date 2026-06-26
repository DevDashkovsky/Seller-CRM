package com.seller.crm.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seller.crm.dto.request.SellerCreateRequest;
import com.seller.crm.dto.request.SellerUpdateRequest;
import com.seller.crm.dto.response.SellerResponse;
import com.seller.crm.service.SellerService;
import com.seller.crm.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SellerController.class)
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private SellerService sellerService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void create_shouldReturn201_withLocationHeader() throws Exception {
        SellerCreateRequest request = new SellerCreateRequest(
            "Egor", "egor@example.com", LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        SellerResponse response = new SellerResponse(
            42L, "Egor", "egor@example.com",
            LocalDateTime.of(2026, 1, 1, 0, 0), null, null
        );
        when(sellerService.create(any(SellerCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/sellers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/v1/sellers/42")))
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.name").value("Egor"));
    }

    @Test
    void getById_shouldReturn200_whenSellerExists() throws Exception {
        SellerResponse response = new SellerResponse(
            1L, "Egor", null, null, null, null
        );
        when(sellerService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/sellers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Egor"));
    }

    @Test
    void getById_shouldReturn404_whenSellerMissing() throws Exception {
        when(sellerService.getById(99L))
            .thenThrow(new EntityNotFoundException("Seller with id 99 not found"));

        mockMvc.perform(get("/api/v1/sellers/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_shouldReturn200_whenValidRequest() throws Exception {
        SellerUpdateRequest request = new SellerUpdateRequest("New Name", "new@example.com");
        SellerResponse response = new SellerResponse(
            1L, "New Name", "new@example.com", null, null, null
        );
        when(sellerService.update(eq(1L), any(SellerUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/sellers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/sellers/1"))
            .andExpect(status().isNoContent());

        verify(sellerService).delete(1L);
    }
}
