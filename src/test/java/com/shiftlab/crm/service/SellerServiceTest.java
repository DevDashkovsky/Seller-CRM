package com.shiftlab.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiftlab.crm.dto.request.SellerCreateRequest;
import com.shiftlab.crm.dto.request.SellerUpdateRequest;
import com.shiftlab.crm.dto.response.SellerResponse;
import com.shiftlab.crm.entity.Seller;
import com.shiftlab.crm.mapper.SellerMapper;
import com.shiftlab.crm.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    void list_shouldMapPageContent_andPreserveMetadata() {
        Pageable pageable = PageRequest.of(0, 20);

        Seller s1 = new Seller();
        s1.setId(1L);
        Seller s2 = new Seller();
        s2.setId(2L);
        Page<Seller> entityPage = new PageImpl<>(List.of(s1, s2), pageable, 42);

        SellerResponse r1 = new SellerResponse(1L, "A", null, null, null, null);
        SellerResponse r2 = new SellerResponse(2L, "B", null, null, null, null);

        when(repository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toResponse(s1)).thenReturn(r1);
        when(mapper.toResponse(s2)).thenReturn(r2);

        Page<SellerResponse> page = service.list(pageable);

        assertThat(page.getContent()).containsExactly(r1, r2);
        assertThat(page.getTotalElements()).isEqualTo(42);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void update_shouldOverwriteMutableFields_andReturnResponse() {
        Seller existing = new Seller();
        existing.setId(1L);
        existing.setName("Old");
        existing.setContactInfo("old@example.com");
        existing.setRegistrationDate(LocalDateTime.of(2020, 1, 1, 0, 0));

        SellerUpdateRequest request = new SellerUpdateRequest("New", "new@example.com");

        SellerResponse expected = new SellerResponse(
            1L, "New", "new@example.com",
            LocalDateTime.of(2020, 1, 1, 0, 0), null, null
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(mapper.toResponse(existing)).thenReturn(expected);

        SellerResponse response = service.update(1L, request);

        assertThat(response).isEqualTo(expected);
        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getContactInfo()).isEqualTo("new@example.com");
        assertThat(existing.getRegistrationDate())
            .isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0));
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenSellerMissing() {
        SellerUpdateRequest request = new SellerUpdateRequest("New", null);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void delete_shouldDelegateToRepository_whenSellerExists() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenSellerMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(repository, never()).deleteById(any());
    }
}
