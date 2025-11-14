package com.mutsa.springboot_auction.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterRequest {
    private List<String> conditions;
    private String minYear;
    private String maxYear;
    private Integer minPrice;
    private Integer maxPrice;
    private List<Long> categoryIds;
}
