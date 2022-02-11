package com.api.parkingcontrol.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.model.ParkingSpot;
import com.api.parkingcontrol.model.ParkingSpotDto;
import com.api.parkingcontrol.service.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/parking-spots")
public class ParkingSpotController {
	
	final ParkingSpotService parkingSpotService;
	
	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}

	@PostMapping
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
		if (parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Licence Plate Car is already in use");
		}
		if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use");
		}
		if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot already registered for this apartament/block");
		}
		
		var parkingSpot = new ParkingSpot();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpot);
		parkingSpot.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpot));
	}
	
	@GetMapping
	public ResponseEntity<Page<ParkingSpot>> getAllParkingSpots(
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
	}
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable UUID id) {
		Optional<ParkingSpot> parkingSpot = parkingSpotService.findById(id);
		
		if (!parkingSpot.isPresent()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Parking Spot not found");
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpot);
	}
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable UUID id) {
		Optional<ParkingSpot> parkingSpot = parkingSpotService.findById(id);
		
		if (!parkingSpot.isPresent()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Parking Spot not found");
		}
		parkingSpotService.delete(parkingSpot.get());
		return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully");
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<Object> updateParkingSpot(@PathVariable UUID id,
			@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
		Optional<ParkingSpot> parkingSpotResult = parkingSpotService.findById(id);
		
		if (!parkingSpotResult.isPresent()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Parking Spot not found");
		}
		
		ParkingSpot parkingSpot = new ParkingSpot();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpot);
		parkingSpot.setId(id);
		parkingSpot.setRegistrationDate(parkingSpotResult.get().getRegistrationDate());
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpot));
	}
	
	
}
