package com.canteen.repository;

import com.canteen.model.Canteen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanteenRepository extends JpaRepository<Canteen, Long> {
    List<Canteen> findByName(String name);
}