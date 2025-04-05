package com.website.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.website.Dao.CategoryRepository;
import com.website.dto.CategoryDTO;
import com.website.entities.Category;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    //Add a new category
    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        logger.info("Adding new category: {}", categoryDTO.getName());
        Category category = new Category();
        category.setName(categoryDTO.getName());

        category = categoryRepository.save(category);
        logger.debug("Category saved with ID: {}", category.getId());
        return new CategoryDTO(category);
    }

    //Get all categories
    public List<CategoryDTO> getAllCategories() {
        logger.info("Fetching all categories");
        List<CategoryDTO> categories = categoryRepository.findAll()
                .stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        logger.debug("Total categories fetched: {}", categories.size());
        return categories;
    }

    //Get category by ID
    public Optional<CategoryDTO> getCategoryById(Long id) {
        logger.info("Fetching category by ID: {}", id);
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            logger.warn("Category not found for ID: {}", id);
        }
        return categoryOpt.map(CategoryDTO::new);
    }

    //Update category
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        logger.info("Updating category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Category not found with ID: {}", id);
                    return new RuntimeException("Category not found");
                });

        category.setName(categoryDTO.getName());
        category = categoryRepository.save(category);
        logger.debug("Category updated with ID: {}", category.getId());

        return new CategoryDTO(category);
    }

    //Delete a category
    public void deleteCategory(Long id) {
        logger.info("Deleting category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent category with ID: {}", id);
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
        logger.debug("Category deleted with ID: {}", id);
    }
}


