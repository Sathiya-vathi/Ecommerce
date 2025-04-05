package com.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.website.Dao.ProductRepository;
import com.website.dto.ProductDTO;
import com.website.entities.Category;
import com.website.entities.Product;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product product;
    private ProductDTO productDTO;
    private Category category;
    
    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("High-performance laptop");
        product.setImageUrl("image.jpg");
        product.setPrice(new BigDecimal("1000.00"));
        product.setCategory(category);
        product.setDiscountType("PERCENTAGE");
        product.setDiscountValue(new BigDecimal("10"));

        productDTO = new ProductDTO();
        productDTO.setName("Laptop");
        productDTO.setDescription("High-performance laptop");
        productDTO.setImageUrl("image.jpg");
        productDTO.setPrice(new BigDecimal("1000.00"));
        productDTO.setCategoryId(1L);
        productDTO.setDiscountType("PERCENTAGE");
        productDTO.setDiscountValue(new BigDecimal("10"));
    }
    
    @Test
    void testAddProduct() {
        when(categoryRepository.findById(productDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        ProductDTO savedProduct = productService.addProduct(productDTO);
        
        assertNotNull(savedProduct);
        assertEquals("Laptop", savedProduct.getName());
        assertEquals(new BigDecimal("1000.00").setScale(2, RoundingMode.HALF_UP), savedProduct.getPrice());
    }

    @Test
    void testAddProduct_CategoryNotFound() {
        when(categoryRepository.findById(productDTO.getCategoryId())).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(RuntimeException.class, () -> productService.addProduct(productDTO));
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        
        List<ProductDTO> products = productService.getAllProducts();
        
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }
    
    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        Optional<ProductDTO> foundProduct = productService.getProductById(1L);
        
        assertTrue(foundProduct.isPresent());
        assertEquals("Laptop", foundProduct.get().getName());
    }
    
    @Test
    void testUpdateProduct() {
        Long productId = 1L;
        
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Product");
        existingProduct.setDescription("Old Description");
        existingProduct.setPrice(new BigDecimal("100.00"));
        existingProduct.setCategory(category);

        ProductDTO updatedProductDTO = new ProductDTO();
        updatedProductDTO.setName("Updated Product");
        updatedProductDTO.setDescription("Updated Description");
        updatedProductDTO.setPrice(new BigDecimal("150.00"));
        updatedProductDTO.setCategoryId(category.getId());

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(updatedProductDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductDTO result = productService.updateProduct(productId, updatedProductDTO);

        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(new BigDecimal("150.00").setScale(2, RoundingMode.HALF_UP), result.getPrice());
    }

    @Test
    void testUpdateProduct_CategoryNotFound() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> productService.updateProduct(productId, productDTO));
        assertEquals("Category not found", exception.getMessage());
    }
    
    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).deleteById(1L);
    }

    
    @Test
    void testDiscountCalculation_Percentage() {
        product.setDiscountType("PERCENTAGE");
        product.setDiscountValue(new BigDecimal("10")); // 10% discount

        BigDecimal expectedPrice = new BigDecimal("900.00").setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualPrice = product.getDiscountedPrice().setScale(2, RoundingMode.HALF_UP);

        assertEquals(expectedPrice, actualPrice);
    }

    @Test
    void testDiscountCalculation_FixedAmount() {
        product.setDiscountType("FIXED");
        product.setDiscountValue(new BigDecimal("200.00")); // $200 discount

        BigDecimal expectedPrice = new BigDecimal("800.00").setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualPrice = product.getDiscountedPrice().setScale(2, RoundingMode.HALF_UP);

        assertEquals(expectedPrice, actualPrice);
    }

    @Test
    void testDiscountCalculation_NoDiscount() {
        product.setDiscountType(null);
        product.setDiscountValue(null);

        BigDecimal expectedPrice = new BigDecimal("1000.00").setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualPrice = product.getDiscountedPrice().setScale(2, RoundingMode.HALF_UP);

        assertEquals(expectedPrice, actualPrice);
    }
}
