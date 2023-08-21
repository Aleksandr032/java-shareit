package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, PageRequest pageRequest);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, PageRequest pageRequest);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                              PageRequest pageRequest);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, PageRequest pageRequest);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime start,
                                                                 PageRequest pageRequest);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime end,
                                                                PageRequest pageRequest);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status, PageRequest pageRequest);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status, PageRequest pageRequest);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                          LocalDateTime start, LocalDateTime end,
                                                                          PageRequest pageRequest);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId,
                                                                             LocalDateTime start, LocalDateTime end,
                                                                             PageRequest pageRequest);

    List<Booking> findByBookerIdAndItemIdAndEndBeforeOrderByEndDesc(Long bookerId, Long itemId, LocalDateTime end);

    //Для поиска последней брони и следующей брони
    Booking findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(Long itemId, Status status,
                                                                   LocalDateTime start);

    Booking findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(Long itemId, Status status,
                                                                   LocalDateTime start);
}
