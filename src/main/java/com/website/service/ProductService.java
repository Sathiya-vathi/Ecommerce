package com.website.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.website.Dao.CategoryRepository;
import com.website.Dao.ProductRepository;
import com.website.Dao.UserRepository;
import com.website.dto.ProductDTO;
import com.website.entities.Category;
import com.website.entities.Product;
import com.website.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public ProductDTO addProduct(ProductDTO productDTO) {
        logger.info("Adding new product: {}", productDTO.getName());
        
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> {
                    logger.error("Category not found with ID: {}", productDTO.getCategoryId());
                    return new RuntimeException("Category not found");
                });

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setImageUrl(productDTO.getImageUrl());
        product.setPrice(productDTO.getPrice());
        product.setCategory(category);
        product.setDiscountType(productDTO.getDiscountType());
        product.setDiscountValue(productDTO.getDiscountValue());

        product = productRepository.save(product);
        logger.debug("Product saved with ID: {}", product.getId());

        return new ProductDTO(product);
    }

    public User findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public List<ProductDTO> getAllProducts() {
        logger.info("Fetching all products");
        List<ProductDTO> products = productRepository.findAll()
                .stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        logger.debug("Total products found: {}", products.size());
        return products;
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        logger.info("Fetching products for category ID: {}", categoryId);
        List<ProductDTO> products = productRepository.findByCategoryId(categoryId)
                .stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        logger.debug("Total products found for category {}: {}", categoryId, products.size());
        return products;
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        logger.info("Updating product with ID: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new RuntimeException("Product not found");
                });

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setImageUrl(productDTO.getImageUrl());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDiscountType(productDTO.getDiscountType());
        existingProduct.setDiscountValue(productDTO.getDiscountValue());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> {
                        logger.error("Category not found with ID: {}", productDTO.getCategoryId());
                        return new RuntimeException("Category not found");
                    });
            existingProduct.setCategory(category);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        logger.debug("Product updated with ID: {}", updatedProduct.getId());

        return new ProductDTO(updatedProduct);
    }

    public Optional<ProductDTO> getProductById(Long id) {
        logger.info("Fetching product by ID: {}", id);
        Optional<ProductDTO> productDTO = productRepository.findById(id).map(ProductDTO::new);
        if (productDTO.isEmpty()) {
            logger.warn("Product not found for ID: {}", id);
        }
        return productDTO;
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent product with ID: {}", id);
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
        logger.debug("Product deleted with ID: {}", id);
    }
}
