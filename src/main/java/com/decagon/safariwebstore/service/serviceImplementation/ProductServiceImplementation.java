package com.decagon.safariwebstore.service.serviceImplementation;

import com.decagon.safariwebstore.exceptions.BadRequestException;
import com.decagon.safariwebstore.exceptions.ResourceNotFoundException;
import com.decagon.safariwebstore.model.*;
import com.decagon.safariwebstore.payload.request.ProductRequest;
import com.decagon.safariwebstore.repository.*;
import com.decagon.safariwebstore.service.ProductService;
import com.decagon.safariwebstore.utils.MethodUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    @Cacheable(cacheNames = "products", sync = true)
    public Page<Product> getAllProducts(ProductPage productPage) {

        Pageable pageable = MethodUtils.getPageable(productPage);

        return productRepository.findAll(pageable);
    }

    @Override
    @Cacheable(cacheNames = "products", sync = true)
    public Page<Product> searchAllProducts(String keyword) {
        List<Product> searchedProduct = new ArrayList<>();

        List<Product> productList = productRepository.findAll();
        List<Product> productByCategory = productList.stream().filter(product -> product.getCategory().stream().anyMatch(category -> category.getName().contains(keyword))).collect(Collectors.toList());


        ProductPage productPage = new ProductPage();
        Pageable pageable = MethodUtils.getPageable(productPage);

        log.info("jgjbljgjlgtglhg");
        List<Product> products = productRepository.findByNameContains(keyword);
        log.info(products + "here's the list of product that macthes keyword");
        // List<Product> products1 = productRepository.findByCategoryIn(categories);
        log.info(productByCategory +"here's the products that matches category");

        searchedProduct.addAll(products);
        searchedProduct.addAll(productByCategory);


        log.info(products +" checking if we get the product list at all");
        log.info(productByCategory + "searched by category");
        log.info(searchedProduct.toString());
        log.info(products.stream().filter(product -> product.getCategory().contains(keyword)).collect(Collectors.toList()).toString()+" this is the fetch result");

        if (keyword != null) {
            return new PageImpl<>(searchedProduct);
        }
        return productRepository.findAll(pageable);
//
    }

    @Override
    @Cacheable(cacheNames = "productsCategory", sync = true)
    public Page<Product> getProductsByCategory(ProductPage productPage, String categoryName) {

        Category category = getCategory(categoryName);

        Pageable pageable = MethodUtils.getPageable(productPage);

        return productRepository.findAllByCategory(category, pageable);
    }

    @Override
    @Cacheable(cacheNames = "productsSubCategory", sync = true)
    public Page<Product> getProductsByCategoryAndSubCategory(ProductPage productPage,
                                                             String categoryName,
                                                             String subCategoryName) {

        Category category = getCategory(categoryName);

        SubCategory subCategory = getSubCategory(subCategoryName, category);

        Pageable pageable = MethodUtils.getPageable(productPage);

        return productRepository.findAllByCategoryAndSubCategory(category, subCategory, pageable);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsCategory", allEntries = true),
            @CacheEvict(value = "productsSubCategory", allEntries = true)
    })
    public Product saveProduct(ProductRequest productRequest) {

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setDescription(productRequest.getDescription());
        product.setCategory(setCategory(productRequest.getCategory()));
        product.setSubCategory(setSubCategory(productRequest.getCategory(), productRequest.getSubCategory()));
        product.setSizes(setSizeList(productRequest.getSizes()));
        product.setColors(setColorList(productRequest.getColors()));
        product.setProductImages(setImageList(productRequest.getProductImages()));

        return productRepository.save(product);
    }

    private Category getCategory(String categoryName) {
        return categoryRepository.findByName(categoryName).orElseThrow(
                () -> {
                    throw  new ResourceNotFoundException("Category not found!");
                });
    }


    private SubCategory getSubCategory(String subCategoryName, Category category){
        return subCategoryRepository
                .findByNameAndCategory(subCategoryName, category).orElseThrow(
                        () -> {
                            throw new ResourceNotFoundException("Sub-Category not found!");
                        });
    }

    private List<Category> setCategory(List<String> categoryList){
        return categoryList.stream().map(categoryItem -> {
            Category category = new Category();
            category.setName(categoryItem);
            return category;
        }).collect(Collectors.toList());
    }

    private List<SubCategory> setSubCategory(List<String> categoryList, List<String> subCategoryList){
        if(categoryList.size() != subCategoryList.size()) throw new BadRequestException("category and subCategory must have equal elements");

        List<SubCategory> subCategories = new ArrayList<>();
        for(int i = 0; i < categoryList.size(); i++){
            Category category = new Category(categoryList.get(i));
            subCategories.add(setCategoryAndSubCategory(category, subCategoryList.get(i)));
        }
        return subCategories;
    }

    private SubCategory setCategoryAndSubCategory(Category category, String subCategoryList){
        SubCategory subCategory = new SubCategory();
        subCategory.setName(subCategoryList);
//        subCategory.setCategory(category);
        return subCategory;
    }

    private List<Color> setColorList(List<String> colorList){
        return colorList.stream().map(colorItem -> {
            Color color = new Color();
            color.setColor(colorItem);
            return color;
        }).collect(Collectors.toList());
    }

    private List<Size> setSizeList(List<String> sizeList){
        return sizeList.stream().map(sizeItem -> {
            Size size = new Size();
            size.setSize(sizeItem);
            return size;
        }).collect(Collectors.toList());
    }

    private List<ProductImage> setImageList(List<String> productImageList){
        return productImageList.stream().map(productImage -> {
            ProductImage image = new ProductImage();
            image.setImage(productImage);
            return image;
        }).collect(Collectors.toList());
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long productId, ProductRequest productRequest) {

        Product product1 = productRepository.findById(productId).get();


        product1.setName(productRequest.getName());
        product1.setPrice(productRequest.getPrice());
        product1.setDescription(productRequest.getDescription());
        product1.setCategory(setCategory(productRequest.getCategory()));
        product1.setSubCategory(setSubCategory(productRequest.getCategory(), productRequest.getSubCategory()));
        product1.setSizes(setSizeList(productRequest.getSizes()));
        product1.setColors(setColorList(productRequest.getColors()));
        product1.setProductImages(setImageList(productRequest.getProductImages()));

        return productRepository.save(product1);
    }

    @Override
    @CacheEvict(value = "product", allEntries = true)
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

//    public Product findById(long id){
//       return productRepository.findById(id).get();
//    }

}


