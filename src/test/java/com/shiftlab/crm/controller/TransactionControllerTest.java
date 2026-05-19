package com.shiftlab.crm.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shiftlab.crm.dto.request.TransactionCreateRequest;
import com.shiftlab.crm.dto.response.TransactionResponse;
import com.shiftlab.crm.entity.PaymentType;
import com.shiftlab.crm.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void create_shouldReturn201_withLocationHeader() throws Exception {
        TransactionCreateRequest request = new TransactionCreateRequest(
            1L, new BigDecimal("100.00"), PaymentType.CARD,
            LocalDateTime.of(2026, 5, 1, 12, 0)
        );
        TransactionResponse response = new TransactionResponse(
            10L, 1L, new BigDecimal("100.00"), PaymentType.CARD,
            LocalDateTime.of(2026, 5, 1, 12, 0), null, null
        );
        when(transactionService.create(any(TransactionCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/v1/transactions/10")))
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.sellerId").value(1));
    }

    @Test
    void create_shouldReturn400_whenAmountMissing() throws Exception {
        String invalidJson = """
            {
              "sellerId": 1,
              "paymentType": "CARD",
              "transactionDate": "2026-05-01T12:00:00"
            }
            """;

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200_whenTransactionExists() throws Exception {
        TransactionResponse response = new TransactionResponse(
            42L, 1L, new BigDecimal("50.00"), PaymentType.CASH,
            LocalDateTime.of(2026, 5, 1, 12, 0), null, null
        );
        when(transactionService.getById(42L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.sellerId").value(1))
            .andExpect(jsonPath("$.paymentType").value("CASH"));
    }
}
