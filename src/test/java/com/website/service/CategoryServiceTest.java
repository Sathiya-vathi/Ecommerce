package com.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.website.Dao.CategoryRepository;
import com.website.dto.CategoryDTO;
import com.website.entities.Category;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Electronics");
    }

    @Test
    void testAddCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        
        CategoryDTO savedCategory = categoryService.addCategory(categoryDTO);
        
        assertNotNull(savedCategory);
        assertEquals("Electronics", savedCategory.getName());
    }

    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category));
        
        List<CategoryDTO> categories = categoryService.getAllCategories();
        
        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    void testGetCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        
        Optional<CategoryDTO> foundCategory = categoryService.getCategoryById(1L);
        
        assertTrue(foundCategory.isPresent());
        assertEquals("Electronics", foundCategory.get().getName());
    }

    @Test
    void testUpdateCategory() {
        CategoryDTO updatedCategoryDTO = new CategoryDTO();
        updatedCategoryDTO.setName("Updated Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        
        CategoryDTO updatedCategory = categoryService.updateCategory(1L, updatedCategoryDTO);
        
        assertNotNull(updatedCategory);
        assertEquals("Updated Electronics", updatedCategory.getName());
    }

    @Test
    void testUpdateCategoryNotFound() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.updateCategory(2L, categoryDTO);
        });
        
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void testDeleteCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository, times(1)).deleteById(1L);
    }


}

