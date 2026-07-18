package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Integer> {

    Optional<CaLamViec> findByMaCa(String maCa);
}