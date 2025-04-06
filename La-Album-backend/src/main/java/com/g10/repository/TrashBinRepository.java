package com.g10.repository;

import com.g10.model.Photo;
import com.g10.model.TrashBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrashBinRepository extends JpaRepository<TrashBin, Long> {

}