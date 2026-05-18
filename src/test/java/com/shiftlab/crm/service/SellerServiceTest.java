package com.shiftlab.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiftlab.crm.dto.request.SellerCreateRequest;
import com.shiftlab.crm.dto.response.SellerResponse;
import com.shiftlab.crm.entity.Seller;
import com.shiftlab.crm.mapper.SellerMapper;
import com.shiftlab.crm.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository repository;

    @Mock
    private SellerMapper mapper;

    @InjectMocks
    private SellerService service;

    @Test
    void create_shouldKeepProvidedRegistrationDate() {
        LocalDateTime registrationDate = LocalDateTime.of(2026, 5, 17, 0, 0);
        SellerCreateRequest request = new SellerCreateRequest(
            "Egor", "big@Pencil123", registrationDate
        );
        Seller mapped = new Seller();
        mapped.setName("Egor");
        mapped.setContactInfo("big@Pencil123");
        mapped.setRegistrationDate(registrationDate);

        Seller saved = new Seller();
        saved.setId(1L);
        saved.setName("Egor");
        saved.setContactInfo("big@Pencil123");
        saved.setRegistrationDate(registrationDate);

        SellerResponse expected = new SellerResponse(
            1L, "Egor", "big@Pencil123", registrationDate, null, null
        );

        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(expected);

        SellerResponse response = service.create(request);

        assertThat(response).isEqualTo(expected);
        assertThat(mapped.getRegistrationDate()).isEqualTo(registrationDate);
    }

    @Test
    void create_shouldFillRegistrationDate_whenNullProvided() {
        SellerCreateRequest request = new SellerCreateRequest(
            "Egor", "big@Pencil123", null
        );
        Seller mapped = new Seller();
        mapped.setName("Egor");
        mapped.setContactInfo("big@Pencil123");
        mapped.setRegistrationDate(null);

        LocalDateTime before = LocalDateTime.now().minusNanos(1);

        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(any(Seller.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(mapper.toResponse(any(Seller.class))).thenReturn(
            new SellerResponse(1L, "Egor", "big@Pencil123", null, null, null)
        );

        service.create(request);

        ArgumentCaptor<Seller> captor = ArgumentCaptor.forClass(Seller.class);
        verify(repository).save(captor.capture());
        LocalDateTime assigned = captor.getValue().getRegistrationDate();
        assertThat(assigned).isNotNull();
        assertThat(assigned).isBetween(before, LocalDateTime.now().plusNanos(1));
    }

    @Test
    void getById_shouldReturnResponse_whenSellerExists() {
        Seller seller = new Seller();
        seller.setId(42L);
        seller.setName("Egor");
        SellerResponse expected = new SellerResponse(
            42L, "Egor", null, null, null, null
        );

        when(repository.findById(42L)).thenReturn(Optional.of(seller));
        when(mapper.toResponse(seller)).thenReturn(expected);

        SellerResponse response = service.getById(42L);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getById_shouldThrow_whenSellerMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(mapper, never()).toResponse(any());
    }
}
