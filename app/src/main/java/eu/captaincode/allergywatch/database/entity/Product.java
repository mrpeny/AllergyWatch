package eu.captaincode.allergywatch.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

@Entity
public class Product {

    @PrimaryKey
    @SerializedName("code")
    @Expose
    private Long code;

    @SerializedName("product_name")
    @Expose
    private String productName;

    @SerializedName("allergens")
    @Expose
    private String allergens;

    @SerializedName("allergens_tags")
    @Expose
    private List<String> allergensTags;

    @SerializedName("image_front_url")
    @Expose
    private String imageFrontUrl;

    @SerializedName("image_front_small_url")
    @Expose
    private String imageFrontSmallUrl;

    @SerializedName("image_front_thumb_url")
    @Expose
    private String imageFrontThumbUrl;

    @SerializedName("allergens_from_ingredients")
    @Expose
    private String allergensFromIngredients;

    @SerializedName("ingredients_text")
    @Expose
    private String ingredientsText;

    @SerializedName("ingredients_ids_debug")
    @Expose
    private List<String> ingredientsIds;

    @SerializedName("quantity")
    @Expose
    private String quantity;

    @SerializedName("https://www.nutella.com/de/de")
    @Expose
    private String link;

    private Date lastRefresh;

    public Product() {
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Date getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(Date lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public List<String> getAllergensTags() {
        return allergensTags;
    }

    public void setAllergensTags(List<String> allergensTags) {
        this.allergensTags = allergensTags;
    }

    public String getImageFrontUrl() {
        return imageFrontUrl;
    }

    public void setImageFrontUrl(String imageFrontUrl) {
        this.imageFrontUrl = imageFrontUrl;
    }

    public String getImageFrontSmallUrl() {
        return imageFrontSmallUrl;
    }

    public void setImageFrontSmallUrl(String imageFrontSmallUrl) {
        this.imageFrontSmallUrl = imageFrontSmallUrl;
    }

    public String getImageFrontThumbUrl() {
        return imageFrontThumbUrl;
    }

    public void setImageFrontThumbUrl(String imageFrontThumbUrl) {
        this.imageFrontThumbUrl = imageFrontThumbUrl;
    }

    public String getAllergensFromIngredients() {
        return allergensFromIngredients;
    }

    public void setAllergensFromIngredients(String allergensFromIngredients) {
        this.allergensFromIngredients = allergensFromIngredients;
    }

    public String getIngredientsText() {
        return ingredientsText;
    }

    public void setIngredientsText(String ingredientsText) {
        this.ingredientsText = ingredientsText;
    }

    public List<String> getIngredientsIds() {
        return ingredientsIds;
    }

    public void setIngredientsIds(List<String> ingredientsIds) {
        this.ingredientsIds = ingredientsIds;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
