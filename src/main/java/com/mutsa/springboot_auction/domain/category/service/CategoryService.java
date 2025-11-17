package com.mutsa.springboot_auction.domain.category.service;

import com.mutsa.springboot_auction.domain.category.dto.CategoryResponse;
import com.mutsa.springboot_auction.domain.category.entity.Category;
import com.mutsa.springboot_auction.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {

        List<Category> categories = categoryRepository.findAll();

        return categories
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
}
