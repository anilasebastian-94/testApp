package test.task_management.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.task_management.model.dto.OfferingDto;
import test.task_management.model.entity.Offering;
import test.task_management.model.exception.ResourceNotFoundException;
import test.task_management.model.repository.OfferingRepository;
import test.task_management.model.request.CreateOfferingRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;

    @Transactional
    public OfferingDto createOffering(CreateOfferingRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Offering name cannot be empty");
        }
        if (request.getPrice() == null || request.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Offering price must be positive");
        }

        Offering offering = Offering.builder()
                .name(request.getName().trim())
                .price(request.getPrice())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return offeringRepository.save(offering).toDto();
    }

    @Transactional(readOnly = true)
    public OfferingDto getOfferingById(Long id) {
        Offering offering = offeringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found with id: " + id));
        return offering.toDto();
    }

    @Transactional(readOnly = true)
    public List<OfferingDto> getAllOfferings() {
        return offeringRepository.findAll().stream()
                .map(Offering::toDto)
                .toList();
    }

    @Transactional
    public OfferingDto updateOfferingStatus(Long id, boolean active) {
        Offering offering = offeringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found with id: " + id));
        offering.setActive(active);
        return offeringRepository.save(offering).toDto();
    }
}

