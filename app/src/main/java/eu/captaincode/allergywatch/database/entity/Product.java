package eu.captaincode.allergywatch.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

@Entity
public class Product {

/*    @PrimaryKey(autoGenerate = true)
    private int productId;*/

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

    private Date lastRefresh;

    public Product() {
    }

/*    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    */

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
}
