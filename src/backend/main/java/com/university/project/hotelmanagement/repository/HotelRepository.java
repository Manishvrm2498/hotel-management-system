package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("""
SELECT h FROM Hotel h
WHERE (:state IS NULL OR h.state = :state)
AND (:district IS NULL OR h.district = :district)
AND (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%')))
""")
    List<Hotel> findByStateDistrictAndName(String state, String district,String name);

    List<Hotel> findByNameContainingIgnoreCase(String name);

    boolean existsByNameAndAddress(String trim, String trim1);

    List<Hotel> findAll();

    List<Hotel> findByDistrict(String district);

    @Modifying
    @Query("UPDATE Hotel h SET h.admin = null WHERE h.admin.id = :userId")
    void clearAdminByUserId(@Param("userId") Long userId);
}
