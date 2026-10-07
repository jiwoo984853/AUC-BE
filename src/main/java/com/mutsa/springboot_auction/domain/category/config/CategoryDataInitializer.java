package com.mutsa.springboot_auction.domain.category.config;

import com.mutsa.springboot_auction.domain.category.entity.Category;
import com.mutsa.springboot_auction.domain.category.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_CATEGORY_NAMES = List.of(
            "디지털 기기",
            "가구/인테리어",
            "유아동",
            "생활가전",
            "스포츠",
            "가공식품",
            "취미/게임/음반",
            "도서",
            "남성패션",
            "여성패션",
            "식물"
    );

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> existingNames = categoryRepository.findAll().stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toSet());

        List<Category> missingCategories = DEFAULT_CATEGORY_NAMES.stream()
                .filter(name -> !existingNames.contains(name))
                .map(this::createCategory)
                .toList();

        categoryRepository.saveAll(missingCategories);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        return category;
    }
}
