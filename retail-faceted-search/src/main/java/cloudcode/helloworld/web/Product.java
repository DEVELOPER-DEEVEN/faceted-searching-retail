package cloudcode.helloworld.web;

import java.util.List;
public class Product {
    private List<String> categories;
    private List<String> subCategories;
    private List<String> colors;
    private List<String> genders;
    private String searchtext;
    public List<String> getCategories() {
        return categories;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    public List<String> getSubCategories() {
        return subCategories;
    }
    public void setSubCategories(List<String> subCategories) {
        this.subCategories = subCategories;
    }
    public List<String> getColors() {
        return colors;
    }
    public void setColors(List<String> colors) {
        this.colors = colors;
    }
    public List<String> getGenders() {
        return genders;
    }
    public void setGenders(List<String> genders) {
        this.genders = genders;
    }
    public String getSearchtext() {
        return searchtext;
    }
    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

}
