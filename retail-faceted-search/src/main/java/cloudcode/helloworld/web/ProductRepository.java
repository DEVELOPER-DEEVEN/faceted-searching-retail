package cloudcode.helloworld.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class ProductRepository {
   
    public static final String TOOLBOX_ENDPOINT = "https://retail-product-search-quality-<<YOUR_PROJECT_NUMBER>>.us-central1.run.app";
    public static final String ALL_FILTERS = "FILTERS";
    /*
   * Method called for invoking all filter values for the app.
   */
  public List<Map<String, String>> getDataMaps(String invokeString) throws Exception {
   // System.out.println("INSIDE getFiltersList");
    String filtersResponse  ="";
    String endPoint = TOOLBOX_ENDPOINT;
    try {
      URL url = new URL(endPoint);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      // Create JSON payload
      Gson gson = new Gson();
      Map<String, String> data = new HashMap<>();
      data.put("name", invokeString);
      String jsonInputString = gson.toJson(data);

      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        try {
           filtersResponse = response.toString();
        } catch (Exception e) { // Handle invalid JSON
          System.err.println("Error parsing response: " + e.getMessage());
        }
      } else {
        System.out.println("POST request of TOOLBOX not working");
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    Gson gson = new Gson();
    Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
    List<Map<String, String>> listOfMaps = gson.fromJson(filtersResponse, listType);
    if (listOfMaps == null) {
      return new ArrayList<>();
    }
    return listOfMaps;
  }


  public List<List<String>> getDataLists() throws Exception {
    List<Map<String, String>> listOfMaps = getDataMaps(ALL_FILTERS);
    return convertMapsToAttributeLists(listOfMaps);
  }

  private List<String> getDistinctAttribute(List<String> inputList) {
    // Use LinkedHashSet to maintain insertion order of filters and ensure uniqueness.
    Set<String> distinctValues = new LinkedHashSet<>();
    try {
      List<String> filterStrings = inputList;
      for (String filterString : filterStrings) {
         distinctValues.add(filterString);
      }
    } catch (Exception e) {
      // In a production app, you should use a logger instead of printStackTrace.
      e.printStackTrace();
    }
    return new ArrayList<>(distinctValues);
  }

  public List<String> getDistinctCategories() throws Exception {
    List<String> categoryList =  getDataLists().get(0);
    return getDistinctAttribute(categoryList);
  }

  public List<String> getDistinctSubCategories() throws Exception 
  {
    List<String> subcategoryList =  getDataLists().get(1);
    return getDistinctAttribute(subcategoryList);
  }

  public List<String> getDistinctColors() throws Exception {
    List<String> colorList = getDataLists().get(2);
    return getDistinctAttribute(colorList);
  }

  public List<String> getDistinctGenders() throws Exception {
    List<String> genderList = getDataLists().get(3);
    return getDistinctAttribute(genderList);
  }

  public List<List<String>> getFilteredResultList(Product productFilters) throws Exception {
    String searchText = productFilters.getSearchtext();
    List<Map<String, String>> filteredProducts = new ArrayList<>();
    if(searchText == null || searchText.trim().isEmpty()){
      List<String> categoryFilters = productFilters.getCategories();
      List<String> subCategoryFilters = productFilters.getSubCategories();
      List<String> colorFilters = productFilters.getColors();
      List<String> genderFilters = productFilters.getGenders();
      List<Map<String, String>> allProducts = getDataMaps(ALL_FILTERS);
    
      for (Map<String, String> product : allProducts) {
        // Check if the product matches the selected filters.
        // An empty/null filter list means no filter is applied for that attribute.
        boolean categoryMatch = (categoryFilters == null || categoryFilters.isEmpty() || categoryFilters.contains(product.get("category")));
        boolean subCategoryMatch = (subCategoryFilters == null || subCategoryFilters.isEmpty() || subCategoryFilters.contains(product.get("sub_category")));
        boolean colorMatch = (colorFilters == null || colorFilters.isEmpty() || colorFilters.contains(product.get("color")));
        boolean genderMatch = (genderFilters == null || genderFilters.isEmpty() || genderFilters.contains(product.get("gender")));
        
        
        // Check for search text match in product content (case-insensitive).
        boolean searchMatch = (searchText == null || searchText.trim().isEmpty() 
            || (product.get("content") != null && product.get("content").toLowerCase().contains(searchText.toLowerCase())));

        if (categoryMatch && subCategoryMatch && colorMatch && genderMatch && searchMatch) {
          filteredProducts.add(product);
        }
      }
     return convertMapsToAttributeLists(filteredProducts);
    }else{
      return(getFilteredVectorSearchResult(productFilters, searchText));
    }
  }
    

  public List<List<String>> getFilteredVectorSearchResult(Product productFilters, String searchText) throws Exception {
    //String searchText = productFilters.getSearchtext();
    List<Map<String, String>> filteredProducts = new ArrayList<>();
    List<String> categoryFilters = productFilters.getCategories();
    List<String> subCategoryFilters = productFilters.getSubCategories();
    List<String> colorFilters = productFilters.getColors();
    List<String> genderFilters = productFilters.getGenders();
     
    categoryFilters = (categoryFilters == null || categoryFilters.isEmpty())?getDistinctCategories() : categoryFilters;
    subCategoryFilters = (subCategoryFilters == null || subCategoryFilters.isEmpty())?getDistinctSubCategories() : subCategoryFilters;
    colorFilters = (colorFilters == null || colorFilters.isEmpty())?getDistinctColors() : colorFilters;
    genderFilters = (genderFilters == null || genderFilters.isEmpty())?getDistinctGenders() : genderFilters;
    System.out.println("CATEGORIES categoryFilters ************: " + categoryFilters.size());
     // Convert using streams
    String[] categories = categoryFilters.stream().toArray(String[]::new);
    String[] sub_categories = (String[]) subCategoryFilters.stream().toArray(String[]::new);
    String[] colors = (String[]) colorFilters.stream().toArray(String[]::new);
    String[] genders = (String[]) genderFilters.stream().toArray(String[]::new); 

    Product sendToToolbox = new Product();
    sendToToolbox.setCategories(categoryFilters);
    sendToToolbox.setSubCategories(subCategoryFilters);
    sendToToolbox.setColors(colorFilters);
    sendToToolbox.setGenders(genderFilters);
    sendToToolbox.setSearchtext(searchText);
    Gson gson = new Gson();
    String invokeParams = gson.toJson(sendToToolbox);
    filteredProducts = getDataMaps(invokeParams);
    return convertMapsToAttributeLists(filteredProducts);
  }

  

  /**
   * Converts a list of product maps to a list of lists of product attributes.
   * This is used to structure the data for the frontend.
   * @param products The list of product maps.
   * @return A list containing lists of each attribute.
   */
  private List<List<String>> convertMapsToAttributeLists(List<Map<String, String>> products) {
    List<String> categoryList = new ArrayList<>();
    List<String> subcategoryList = new ArrayList<>();
    List<String> colorList = new ArrayList<>();
    List<String> genderList = new ArrayList<>();
    List<String> contentList = new ArrayList<>();
    List<String> uriList = new ArrayList<>();
    for (Map<String, String> map : products) {
      categoryList.add(map.get("category"));
      subcategoryList.add(map.get("sub_category"));
      colorList.add(map.get("color"));
      genderList.add(map.get("gender"));
      contentList.add(map.get("content"));
      uriList.add(map.get("uri"));
    }
    List<List<String>> listOfLists = new ArrayList<>();
    listOfLists.add(categoryList);
    listOfLists.add(subcategoryList);
    listOfLists.add(colorList);
    listOfLists.add(genderList);
    listOfLists.add(contentList);
    listOfLists.add(uriList);
    return listOfLists;
  }
}
