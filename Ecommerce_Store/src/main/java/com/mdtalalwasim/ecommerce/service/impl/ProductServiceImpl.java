package com.mdtalalwasim.ecommerce.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.mdtalalwasim.ecommerce.controller.AdminViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.repository.ProductRepository;
import com.mdtalalwasim.ecommerce.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	ProductRepository productRepository;

    @Autowired
    private FileStorageService fileStorageService;


    @Override
	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		// TODO Auto-generated method stub
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(long id) {
		// TODO Auto-generated method stub
		 Optional<Product> product = productRepository.findById(id);
		 if(product.isPresent()) {
			 productRepository.deleteById(product.get().getId());
			 return true;
		 }else {
			 return false;
		 }
		 
	}

	@Override
	public Optional<Product> findById(long id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Product getProductById(long id) {
		// TODO Auto-generated method stub
		return productRepository.findById(id).orElse(null);
	}

    @Override
    public Product updateProductById(Product product, MultipartFile file) {
        Product db = getProductById(product.getId());

        if (file != null && !file.isEmpty()) {
            try {
                String path = fileStorageService.saveImage(file, "product_image"); // img/product_image/uuid.jpg
                db.setProductImage(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        db.setProductTitle(product.getProductTitle());
        db.setProductDescription(product.getProductDescription());
        db.setProductCategory(product.getProductCategory());
        db.setProductPrice(product.getProductPrice());
        db.setProductStock(product.getProductStock());
        db.setIsActive(product.getIsActive());

        db.setDiscount(product.getDiscount());
        Double discount = product.getProductPrice() * (product.getDiscount() / 100.0);
        db.setDiscountPrice(product.getProductPrice() - discount);

        return productRepository.save(db);
    }


    @Override
	public List<Product> findAllActiveProducts(String category) {
		List<Product> products = null;
		if(ObjectUtils.isEmpty(category)) {
			products = productRepository.findByIsActiveTrue();
		}else {
			products =productRepository.findByProductCategory(category);
		}
		
		return products;
	}

}
