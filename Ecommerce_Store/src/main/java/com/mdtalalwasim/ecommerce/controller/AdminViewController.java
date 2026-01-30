package com.mdtalalwasim.ecommerce.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.entity.Product;
import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.service.CartService;
import com.mdtalalwasim.ecommerce.service.CategoryService;
import com.mdtalalwasim.ecommerce.service.ProductService;
import com.mdtalalwasim.ecommerce.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminViewController {
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	ProductService productService;

	@Autowired
	UserService userService;
	
	@Autowired
	CartService cartService;

    @Value("${app.upload.base-dir}")
    private String baseDir;

	//to track which user is login right Now
	//by default call this method when any request come to this controller because of @ModelAttribut
	@ModelAttribute 
	public void getUserDetails(Principal principal, Model model) {
		if(principal != null) {
			String currenLoggedInUserEmail = principal.getName();
			User currentUserDetails = userService.getUserByEmail(currenLoggedInUserEmail);
			//System.out.println("Current Logged In User is :: ADMIN Controller :: "+currentUserDetails.toString());
			model.addAttribute("currentLoggedInUserDetails",currentUserDetails);
			
			//for showing user cart count
			Long countCartForUser = cartService.getCounterCart(currentUserDetails.getId());
			System.out.println("Admin Cart Count :"+countCartForUser);
			model.addAttribute("countCartForUser", countCartForUser);
			
		}
		List<Category> allActiveCategory = categoryService.findAllActiveCategory();
		model.addAttribute("allActiveCategory",allActiveCategory);
		
	}
	
	@GetMapping("/")
	public String adminIndex() {
		
		return "admin/admin-dashboard";
	}
	
	
	//CATEGORY-MODULE-START
	
	@GetMapping("/add-category")
	public String addCategory(Model model) {
		
		return "admin/category/category-add-form";
	}
	
	@PostMapping("/save-category")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) throws IOException {

        if (categoryService.existCategory(category.getCategoryName())) {
            session.setAttribute("errorMsg", "Category Name already Exists");
            return "redirect:/admin/category";
        }

        String imagePath = "img/category/default.jpg";

        if (file != null && !file.isEmpty()) {
            // create dir: baseDir/img/category
            Path dir = Paths.get(baseDir, "img", "category");
            Files.createDirectories(dir);

            //_toggle safe filename_
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf('.'))
                    : "";

            String filename = UUID.randomUUID() + ext;

            // save to disk
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // store in DB (relative path)
            imagePath = "img/category/" + filename;
        }

        category.setCategoryImage(imagePath);

        Category saved = categoryService.saveCategory(category);
        if (ObjectUtils.isEmpty(saved)) {
            session.setAttribute("errorMsg", "Not Saved! Internal Server Error!");
        } else {
            session.setAttribute("successMsg", "Category Save Successfully.");
        }

        return "redirect:/admin/category";
    }


    @GetMapping("/category")
	public String category(Model model) {
		System.out.println("category:WWWWWWWWW");
		List<Category> allCategories = categoryService.getAllCategories();
		System.out.println("category: "+allCategories.toString());
		for (Category category : allCategories) {
			//category.getCreatedAt();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
			String format = formatter.format(category.getCreatedAt());
			model.addAttribute("formattedDateTimeCreatedAt",format);
			
		}
		
		model.addAttribute("allCategoryList",allCategories);
		
		return "/admin/category/category-home";
	}
	
	
	@GetMapping("/edit-category/{id}")
	public String editCategoryForm(@PathVariable("id") long id, Model model) {
		//System.out.println("ID :"+id);
		Optional<Category> categoryObj = categoryService.findById(id);
		if(categoryObj.isPresent()) {
			Category category = categoryObj.get();
			model.addAttribute("category", category);
		}else {
			System.out.println("ELSEEEEE");
		}
		return "/admin/category/category-edit-form";
	}
	
	
	@PostMapping("/update-category")
	public String udateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		System.out.println("Category for UPDATE :"+category.toString());
		
		Optional<Category> categoryById = categoryService.findById(category.getId());
		System.out.println("Category obj"+categoryById.toString());
		
		
		if(categoryById.isPresent()) {
			System.out.println("Present:");
			Category oldCategory = categoryById.get();
			System.out.println("Category old Obj "+oldCategory.toString());
			oldCategory.setCategoryName(category.getCategoryName());
			oldCategory.setIsActive(category.getIsActive());
			//oldCategory.setUpdatedAt(LocalDateTime.now());
			
			
			String imageName =  file.isEmpty() ?  oldCategory.getCategoryImage() : file.getOriginalFilename();
			oldCategory.setCategoryImage(imageName);	
			
			Category updatedCategory = categoryService.saveCategory(oldCategory);
			
			if(!ObjectUtils.isEmpty(updatedCategory)) {
				//save File
				if(!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category"+File.separator+file.getOriginalFilename());
					System.out.println("File Update path: "+path);
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}
				
				session.setAttribute("successMsg", "Category Updated Successfully");
			}else {
				session.setAttribute("errorMsg", "Something wrong on server!");
			}
			
			
			
			//OR
//			if(file!=null) {
//				String newImageName = file.getOriginalFilename();
//				System.out.println("File name: "+newImageName);
//				oldCategory.setCategoryImage(newImageName);
//			}else {
//				String oldOriginalImg = oldCategory.getCategoryImage();
//				System.out.println("File name ELSE: "+oldOriginalImg);
//				oldCategory.setCategoryImage(oldOriginalImg);
//			}
			
			
		}else {
			System.out.println("Not Present:");
		}
		
		return "redirect:/admin/category";
	}
	
	@GetMapping("/delete-category/{id}")
	public String deleteCategory(@PathVariable("id") long id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("successMsg", "Category Deleted Successfully");
		}else {
			session.setAttribute("errorMsg", "Server Error");
		}
		
		return "redirect:/admin/category";
	}
	
	
	//PRODUCT-MODULE-START
	
	@GetMapping("/add-product")
	public String addProduct(Model model) {
		List<Category> allCategories = categoryService.getAllCategories();
		model.addAttribute("allCategoryList",allCategories);
		return "/admin/product/add-product";
	}
	

	
	@PostMapping("/save-product")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		String imageName = file !=null ? file.getOriginalFilename() : "default.png"; 
		
		product.setProductImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getProductPrice());
		
		Product saveProduct = productService.saveProduct(product);
		 
		if(!ObjectUtils.isEmpty(saveProduct)) {
			File savefile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(savefile.getAbsolutePath()+File.separator+"product_image"+File.separator+imageName);
			System.out.println("File save Path :"+path);
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			session.setAttribute("successMsg", "Product Save Successfully.");
		}else {
			session.setAttribute("errorMsg", "Something Wrong on server while save Product");
			//System.out.println("Something Wrong on server while save Product");
		}
		
		return "redirect:/admin/product-list";
	}
	
	@GetMapping("/product-list")
	public String productList(Model model) {
		model.addAttribute("productList", productService.getAllProducts());
		return "/admin/product/product-list";
	}
	
	@GetMapping("/delete-product/{id}")
	public String deleteProduct(@PathVariable("id") long id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		
		if(deleteProduct) {
			session.setAttribute("successMsg", "Product Deleted Successfully.");
		}else {
			session.setAttribute("errorMsg", "Something Wrong on server while deleting Product");
		}
		return "redirect:/admin/product-list";
		
	}
	
	@GetMapping("/edit-product/{id}")
	public String editProduct(@PathVariable long id,Model model) {
		Product product = productService.getProductById(id);
		model.addAttribute("product",product);
		model.addAttribute("allCategoryList",categoryService.getAllCategories());
		return "/admin/product/edit-product";
	}
	
	@PostMapping("/update-product")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession session, Model model) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "INVALID DISCOUNT!");
		} else {
			Product updateProduct = productService.updateProductById(product, file);
			if (!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("successMsg", "Product Updated Successfully.");
			} else {
				session.setAttribute("errorMsg", "Something Wrong on server while deleting Product");
			}
		}

		// return "redirect:/admin/product/edit-product";
		return "redirect:/admin/product-list";
	}
	
	
	
	//USER-WORK
	//get all users
	@GetMapping("/get-all-users")
	public String getAllUser(Model model) {
		
		List<User> allUsers = userService.getAllUsersByRole("ROLE_USER");
		for (User user : allUsers) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			String format = formatter.format(user.getCreatedAt());
			model.addAttribute("formattedDateTimeCreatedAt",format);
			
		}
		model.addAttribute("allUsers",allUsers);
		
		return "/admin/users/user-home";
		
	}
	

	@GetMapping("/edit-user-status")
	public String editUser(@RequestParam("status") Boolean status, @RequestParam("id") Long id, Model model, HttpSession session) {
		Boolean updateUserStatus = userService.updateUserStatus(status,id);
		if(updateUserStatus == true) {
			session.setAttribute("successMsg", "User Status Updated Successfully.");
		}
		else {
			session.setAttribute("errorMsg", "Something Wrong on server while Updating User status");
		}
		return "redirect:/admin/get-all-users";
		
	}
	


}
