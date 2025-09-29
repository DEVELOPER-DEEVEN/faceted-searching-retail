package cloudcode.helloworld.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/** Defines a controller to handle HTTP requests */
@Controller
public final class RetailController {

  private static String project;
  private static final Logger logger = LoggerFactory.getLogger(RetailController.class);
  
  /**
   * Create an endpoint for the landing page
   *
   * @param model The model to which attributes will be added.
   * @return the index view template
   * @throws Exception if an error occurs while fetching filters.
   */
  @GetMapping("/")
  public String helloWorld(Model model) throws Exception {
      ProductRepository productRepository = new ProductRepository();
      // Fetch filter values directly from the repository and add to the model
      populateFilterModel(model, productRepository);
      // Initially, load all products.    
      List<List<String>> products = productRepository.getDataLists();
      model.addAttribute("products", products);
      // Add a message if no products are found.
      if (!products.isEmpty() && CollectionUtils.isEmpty(products.get(0))) {
        model.addAttribute("noProductsFound", "No products found.");
      }
      
      // Add an empty filters object to avoid errors in the view.
      model.addAttribute("selectedFilters", new Product());
      return "index";
  }
  
  @GetMapping("/search")
  public String vectorSearch(Product productFilters, Model model) throws Exception {
      ProductRepository productRepository = new ProductRepository();
      // Fetch all possible filter values to display on the page.
      populateFilterModel(model, productRepository);

      // Call the new method to get the filtered results.
      List<List<String>> filteredProducts = productRepository.getFilteredResultList(productFilters);
      model.addAttribute("products", filteredProducts);

      // Add the submitted filters to the model to re-check the selected boxes.
      model.addAttribute("selectedFilters", productFilters);
      return "index";
  }

  private void populateFilterModel(Model model, ProductRepository productRepository) throws Exception {
    model.addAttribute("categories", productRepository.getDistinctCategories());
    model.addAttribute("subCategories", productRepository.getDistinctSubCategories());
    model.addAttribute("colors", productRepository.getDistinctColors());
    model.addAttribute("genders", productRepository.getDistinctGenders());
  }

}
