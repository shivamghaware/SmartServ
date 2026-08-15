package com.smartserv.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.dto.jobCard.AddItemToJobCardDto;
import com.smartserv.dto.jobCard.AssignMechanicDto;
import com.smartserv.dto.jobCard.CreateJobCardDto;
import com.smartserv.dto.jobCard.JobCardItemDto;
import com.smartserv.dto.jobCard.JobCardResponseDto;
import com.smartserv.client.InventoryClient;
import com.smartserv.entity.Appointment;
import com.smartserv.entity.JobCard;
import com.smartserv.entity.JobCardItem;
import com.smartserv.entity.JobCardStatus;
import com.smartserv.entity.Role;
import com.smartserv.entity.Status;
import com.smartserv.entity.User;
import com.smartserv.entity.Vehicle;
import com.smartserv.exceptions.DuplicateJobCreationException;
import com.smartserv.exceptions.InsufficientStockException;
import com.smartserv.exceptions.InvalidOperationException;
import com.smartserv.exceptions.InvalidRoleException;
import com.smartserv.exceptions.JobCardNotFoundException;
import com.smartserv.exceptions.ResourceNotFoundException;
import com.smartserv.exceptions.StockConflictException;
import com.smartserv.exceptions.UnauthorizedException;
import com.smartserv.exceptions.UserNotFoundException;
import com.smartserv.repository.AppointmentRepository;
import com.smartserv.repository.JobCardItemRepository;
import com.smartserv.repository.JobCardRepository;
import com.smartserv.repository.UserRepository;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JobCardServiceImpl implements JobCardService {

	private final JobCardRepository jobCardRepo;
	private final AppointmentRepository appointmentRepo;
	private final UserRepository userRepo;
	private final InventoryClient inventoryClient;
	private final JobCardItemRepository jobCardItemRepo;

	@Override
	public JobCardResponseDto createJobCard(@Valid CreateJobCardDto dto) {
		Appointment appointment = appointmentRepo.findById(dto.getAppointmentId()).orElseThrow(
				() -> new ResourceNotFoundException("Appointment does not exist with the given appointment"));

		if (appointment.getStatus() != Status.APPROVED) {
			throw new InvalidOperationException("Appointment must be approved for creating job card");
		}

		if (jobCardRepo.existsByAppointmentId(dto.getAppointmentId())) {
			throw new DuplicateJobCreationException("Job card already exists for the given appointment id.");
		}

		User manager = userRepo.findById(dto.getManagerId())
				.orElseThrow(() -> new UserNotFoundException("manager not found"));

		if (manager.getUserRole() != Role.MANAGER) {
			throw new InvalidRoleException("User is not a manager");
		}

		User mechanic = null;
		if (dto.getMechanicId() != null) {
			mechanic = validateAndGetMechanic(dto.getMechanicId(), dto.getManagerId());
		}

		JobCard jobCard = new JobCard();
		jobCard.setAppointment(appointment);
		jobCard.setManager(manager);
		jobCard.setMechanic(mechanic);
		jobCard.setJobCardStatus(JobCardStatus.CREATED);
		jobCard.setEstimatedCompletionDate(dto.getEstimatedCompletionDate());

		JobCard saved = jobCardRepo.save(jobCard);

		appointment.setStatus(Status.IN_PROGRESS);
		appointmentRepo.save(appointment);

		log.info("Job card created with id : {} ", saved.getId());

		return mapResponseToDto(jobCard);
	}

	@Override
	public JobCardResponseDto getJobCardById(Long jobCardId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Job card not found for specified id."));

		return mapResponseToDto(jobCard);
	}

	@Override
	public JobCardResponseDto getJobCardByAppointmentId(Long appointmentId) {
		Appointment appointment = appointmentRepo.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("appointment id not found."));
		JobCard jobCard = jobCardRepo.findByAppointment(appointment);

		return mapResponseToDto(jobCard);
	}

	@Override
	public List<JobCardResponseDto> getAllJobCards() {
		List<JobCard> jobCards = jobCardRepo.findAll();
		return jobCards.stream().map(this::mapResponseToDto).collect(Collectors.toList());
	}

	@Override
	public JobCardResponseDto updateMechanic(Long jobCardId, AssignMechanicDto dto) {

		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("job card does not exist with given id"));

		if (jobCard.getJobCardStatus() == JobCardStatus.COMPLETED) {
			throw new InvalidOperationException("Cannot reassign completed job cards");
		}

		User manager = jobCard.getManager();

		User mechanic = validateAndGetMechanic(dto.getMechanicId(), manager.getId());

		jobCard.setMechanic(mechanic);

		JobCard updated = jobCardRepo.save(jobCard);

		return mapResponseToDto(updated);
	}

	@Override
	public JobCardResponseDto startWork(Long jobCardId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("job card not found"));

		if (jobCard.getJobCardStatus() == JobCardStatus.COMPLETED || jobCard.getJobCardStatus() == JobCardStatus.CANCELLED) {
			throw new InvalidOperationException(
					"Cannot start work on COMPLETED or CANCELLED job cards.");
		}

		if (jobCard.getMechanic() == null) {
			throw new InvalidOperationException("cannot start work without mechanic.");
		}

		jobCard.setJobCardStatus(JobCardStatus.IN_PROGRESS);
		jobCard.setStartTime(LocalDateTime.now());
		JobCard updated = jobCardRepo.save(jobCard);
		return mapResponseToDto(updated);
	}

	@Override
	public JobCardResponseDto completeWork(Long jobCardId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Job card not found"));

		if (jobCard.getJobCardStatus() != JobCardStatus.IN_PROGRESS) {
			throw new InvalidOperationException(
					"job card is not in IN_PROGRESS status. Current status: " + jobCard.getJobCardStatus());
		}

		if (jobCard.getItems().isEmpty()) {
			throw new InvalidOperationException("job card cannot be completed without items/parts added");
		}

		jobCard.setJobCardStatus(JobCardStatus.COMPLETED);
		jobCard.setCompletionTime(LocalDateTime.now());

		JobCard updated = jobCardRepo.save(jobCard);

		Appointment appointment = jobCard.getAppointment();
		appointment.setStatus(Status.COMPLETED);
		appointmentRepo.save(appointment);
		return mapResponseToDto(updated);
	}

	@Override
	public JobCardResponseDto cancelJobCard(Long jobCardId, String reason) {
		log.info("Cancelling job card {} with reason {} ", jobCardId, reason);

		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("job card not found"));

		if (jobCard.getJobCardStatus() == JobCardStatus.COMPLETED) {
			throw new InvalidOperationException("cannot delete COMPLETED job card");
		}

		if (jobCard.getJobCardStatus() == JobCardStatus.CANCELLED) {
			throw new InvalidOperationException("job card already CANCELLED.");
		}

		if (reason == null || reason.trim().isEmpty()) {
			throw new IllegalArgumentException("Cancellation reason is required");
		}

		for (JobCardItem item : jobCard.getItems()) {
			inventoryClient.addStock(item.getInventoryItemId(), item.getQuantity());
			log.info("returned {} units of product ID {} to inventory.", item.getQuantity(), item.getInventoryItemId());
		}

		jobCard.setJobCardStatus(JobCardStatus.CANCELLED);
		jobCard.setCancellationReason(reason);

		JobCard updated = jobCardRepo.save(jobCard);

		Appointment appointment = jobCard.getAppointment();
		appointment.setStatus(Status.CANCELLED);
		appointmentRepo.save(appointment);

		log.info("Job Card {} cancelled. Reason: {} ", jobCardId, reason);

		return mapResponseToDto(updated);
	}

	@Override
	public JobCardResponseDto addItemToJobCard(Long jobCardId, AddItemToJobCardDto dto) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("job card not found"));

		if (jobCard.getJobCardStatus() == JobCardStatus.CANCELLED
				|| jobCard.getJobCardStatus() == JobCardStatus.COMPLETED) {
			throw new InvalidOperationException("cannot add items to CANCELLED or COMPLETED job cards");
		}

		com.smartserv.dto.inventory.InventoryResponseDto inventoryItem = inventoryClient.getItemById(dto.getInventoryItemId());
		if (inventoryItem == null) {
			throw new ResourceNotFoundException("inventory item does not exist");
		}

		if (Boolean.TRUE.equals(inventoryItem.isDeleted())) {
			throw new InvalidOperationException("cannot use deleted inventory items");
		}

		if (inventoryItem.getStockQuantity() < dto.getQuantity()) {
			throw new InsufficientStockException(
					"Insufficient stock for " + inventoryItem.getItemName() + " available quantity is "
							+ inventoryItem.getStockQuantity() + ", requested quantity is " + dto.getQuantity());
		}

		JobCardItem jobCardItem = new JobCardItem();
		jobCardItem.setJobCard(jobCard);
		jobCardItem.setInventoryItemId(dto.getInventoryItemId());
		jobCardItem.setQuantity(dto.getQuantity());
		jobCardItem.setSnapshotItemName(inventoryItem.getItemName());
		jobCardItem.setSnapshotPrice(inventoryItem.getCurrentPrice());
		jobCardItem.setTotalPrice(inventoryItem.getCurrentPrice() * dto.getQuantity());

		jobCard.getItems().add(jobCardItem);

		inventoryClient.deductStock(dto.getInventoryItemId(), dto.getQuantity());
		log.info("Deducted {} units of product ID {} from inventory.", dto.getQuantity(), dto.getInventoryItemId());

		JobCard updated = jobCardRepo.save(jobCard);

		log.info("Item added to job card {}. Total items:{} ", jobCardId, updated.getItems().size());

		return mapResponseToDto(updated);
	}

	@Override
	public JobCardResponseDto removeItemsFromJobCard(Long jobCardId, Long itemId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Job card not found."));

		if (jobCard.getJobCardStatus() == JobCardStatus.COMPLETED) {
			throw new InvalidOperationException("Cannot delete items from COMPLETED job card.");
		}

		if (jobCard.getJobCardStatus() == JobCardStatus.CANCELLED) {
			throw new InvalidOperationException("Cannot delete items from CANCELLED job card.");
		}

		JobCardItem itemToRemove = jobCardItemRepo.findByIdAndJobCardId(itemId, jobCardId)
				.orElseThrow(() -> new JobCardNotFoundException("item not found in this job card"));

		inventoryClient.addStock(itemToRemove.getInventoryItemId(), itemToRemove.getQuantity());
		log.info("Returned {} units of product ID {} to inventory.", itemToRemove.getQuantity(), itemToRemove.getInventoryItemId());

		jobCard.getItems().remove(itemToRemove);

		JobCard updated = jobCardRepo.save(jobCard);

		log.info("item removed from job card {}. Remaining items: {}", jobCardId, updated.getItems().size());

		return mapResponseToDto(updated);
	}

	@Override
	public JobCardResponseDto getJobCardItems(Long jobCardId) {
		JobCard jobCard = jobCardRepo.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Job card not found."));

		return mapResponseToDto(jobCard);
	}


	@Override
	public List<JobCardResponseDto> getJobCardByManager(Long managerId) {
		User manager = userRepo.findById(managerId)
				.orElseThrow(() -> new ResourceNotFoundException("Manager not found."));

		if (manager.getUserRole() != Role.MANAGER) {
			throw new InvalidRoleException("user: " + managerId + " does not have a manager role.");
		}

		List<JobCard> jobCard = jobCardRepo.findByManager(manager);

		return jobCard.stream().map(this::mapResponseToDto).collect(Collectors.toList());
	}

	@Override
	public List<JobCardResponseDto> getJobCardByMechanic(Long mechanicId) {
		User mechanic = userRepo.findById(mechanicId)
				.orElseThrow(() -> new ResourceNotFoundException("mechanic not found"));

		if (mechanic.getUserRole() != Role.MECHANIC) {
			throw new InvalidRoleException("user " + mechanicId + " is not a mechanic.");
		}

		List<JobCard> jobCard = jobCardRepo.findByMechanic(mechanic);

		return jobCard.stream().map(this::mapResponseToDto).collect(Collectors.toList());
	}

	@Override
	public List<JobCardResponseDto> getJobCardByStatus(JobCardStatus status) {
		List<JobCard> jobCard = jobCardRepo.findByJobCardStatus(status);

		return jobCard.stream().map(this::mapResponseToDto).collect(Collectors.toList());
	}

	@Override
	public List<JobCardResponseDto> getManagerJobCardsByStatus(Long managerId, JobCardStatus status) {
		User manager = userRepo.findById(managerId)
				.orElseThrow(() -> new ResourceNotFoundException("Manager not found."));

		if (manager.getUserRole() != Role.MANAGER) {
			throw new InvalidRoleException("user: " + managerId + " does not have a manager role.");
		}

		List<JobCard> jobCards = jobCardRepo.findByManagerIdAndJobCardStatus(managerId, status);

		return jobCards.stream().map(this::mapResponseToDto).collect(Collectors.toList());
	}

	@Override
	public List<JobCardResponseDto> getMechanicJobCardsByStatus(Long mechanicId, JobCardStatus status) {
		User mechanic = userRepo.findById(mechanicId)
				.orElseThrow(() -> new ResourceNotFoundException("mechanic not found."));

		if (mechanic.getUserRole() != Role.MECHANIC) {
			throw new InvalidRoleException("User: " + mechanicId + " does not have a role mechanic.");
		}

		List<JobCard> jobCards = jobCardRepo.findByMechanicIdAndJobCardStatus(mechanicId, status);
		return jobCards.stream().map(this::mapResponseToDto).collect(Collectors.toList());
	}

	@Override
	public Long getJobCardCount() {
		return jobCardRepo.count();

	}

	@Override
	public Long getInProgressCount() {

		return jobCardRepo.countByJobCardStatus(JobCardStatus.IN_PROGRESS);
	}

	@Override
	public Long getCompletedCount() {

		return jobCardRepo.countByJobCardStatus(JobCardStatus.COMPLETED);
	}

	@Override
	public Long getManagerJobCardCount(Long managerId) {
		return jobCardRepo.countByManagerId(managerId) - jobCardRepo.countByManagerIdAndJobCardStatus(managerId, JobCardStatus.BILLED);
	}

	@Override
	public Long getMechanicJobCardCount(Long mechanicId) {

		return jobCardRepo.countByMechanicId(mechanicId);
	}

	@Override
	public Long countManagerJobCardByStatus(Long managerId, JobCardStatus status) {

		return jobCardRepo.countByManagerIdAndJobCardStatus(managerId, status);
	}

	@Override
	public Long countMechanicJobCardByStatus(Long mechanicId, JobCardStatus status) {

		return jobCardRepo.countByMechanicIdAndJobCardStatus(mechanicId, status);
	}

	// ------------------Helper Methods-------------------

	private User validateAndGetMechanic(Long mechanicId, Long managerId) {
		User mechanic = userRepo.findById(mechanicId)
				.orElseThrow(() -> new UserNotFoundException("mechanic not found"));

		if (mechanic.getUserRole() != Role.MECHANIC) {
			throw new InvalidRoleException("user is not a mechanic");
		}

		if (mechanic.getManager() == null || !mechanic.getManager().getId().equals(managerId)) {
			throw new UnauthorizedException(
					"this mechanic does not report to you. Mechanic can only be assigned by manager");
		}
		return mechanic;
	}

	private JobCardResponseDto mapResponseToDto(JobCard jobCard) {
		Appointment appointment = jobCard.getAppointment();
		Vehicle vehicle = appointment.getVehicleDetails();
		User customer = vehicle.getCustomer();
		User manager = jobCard.getManager();
		User mechanic = jobCard.getMechanic();

		List<JobCardItemDto> itemDtos = jobCard.getItems().stream().map(this::mapItemToDto)
				.collect(Collectors.toList());


		Double totalAmount = itemDtos.stream().mapToDouble(JobCardItemDto::getTotalPrice).sum();

		return JobCardResponseDto.builder().id(jobCard.getId())

				.appointmentId(appointment.getId()).problemDescription(appointment.getProblemDescription())
				.appointmentDate(appointment.getRequestDate())

				.vehicleId(vehicle.getId()).licensePlate(vehicle.getLicensePlate()).brand(vehicle.getBrand())
				.model(vehicle.getModel())

				.customerId(customer.getId()).customerName(customer.getUserName()).customerPhone(customer.getMobile())

				.managerId(manager.getId()).managerName(manager.getUserName())

				.mechanicId(mechanic != null ? mechanic.getId() : null)
				.mechanicName(mechanic != null ? mechanic.getUserName() : "Unassigned")

				.status(jobCard.getJobCardStatus()).cancellationReason(jobCard.getCancellationReason())
				.estimatedCompletionDate(jobCard.getEstimatedCompletionDate())
				.startTime(jobCard.getStartTime())
				.completionTime(jobCard.getCompletionTime()).createdAt(jobCard.getCreatedOn())
				.updatedAt(jobCard.getLastUpdated())

				.items(itemDtos).totalAmount(totalAmount)

				.build();

	}

	private JobCardItemDto mapItemToDto(JobCardItem jobCardItem) {
		return JobCardItemDto.builder().id(jobCardItem.getId()).itemName(jobCardItem.getSnapshotItemName())
				.itemPrice(jobCardItem.getSnapshotPrice()).quantity(jobCardItem.getQuantity())
				.totalPrice(jobCardItem.getSnapshotPrice() * jobCardItem.getQuantity()).build();
	}


}
