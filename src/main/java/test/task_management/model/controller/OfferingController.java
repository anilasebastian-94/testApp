package test.task_management.model.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.task_management.model.dto.OfferingDto;
import test.task_management.model.request.CreateOfferingRequest;
import test.task_management.model.response.RestResponse;
import test.task_management.model.service.OfferingService;

import java.util.List;

@RestController
@RequestMapping("/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    public ResponseEntity<RestResponse<OfferingDto>> createOffering(@RequestBody CreateOfferingRequest request) {
        OfferingDto created = offeringService.createOffering(request);
        return new ResponseEntity<>(RestResponse.ok("Offering created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<OfferingDto>> getOfferingById(@PathVariable Long id) {
        OfferingDto offering = offeringService.getOfferingById(id);
        return ResponseEntity.ok(RestResponse.ok(offering));
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<OfferingDto>>> getAllOfferings() {
        List<OfferingDto> offerings = offeringService.getAllOfferings();
        return ResponseEntity.ok(RestResponse.ok(offerings));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RestResponse<OfferingDto>> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        OfferingDto updated = offeringService.updateOfferingStatus(id, active);
        return ResponseEntity.ok(RestResponse.ok("Offering status updated", updated));
    }
}

