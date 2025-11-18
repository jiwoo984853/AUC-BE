package com.mutsa.springboot_auction.domain.category.repository;

import com.mutsa.springboot_auction.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
