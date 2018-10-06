package eu.captaincode.allergywatch.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

@Entity
public class Product {

 /*   @SerializedName("allergens_hierarchy")
    @Expose
    private List<String> allergensHierarchy = new ArrayList<>();*/
    @PrimaryKey(autoGenerate = true)
    private Long id;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("product_name")
    @Expose
    private String productName;
    @SerializedName("serving_size")
    @Expose
    private String servingSize;
    @SerializedName("allergens")
    @Expose
    private String allergens;

    private Date lastRefresh;

    public Product() {
    }

    public Product(Long id, String code, String productName, String servingSize, String allergens,
                   List<String> allergensHierarchy, Date lastRefresh) {
        this.id = id;
        this.code = code;
        this.productName = productName;
        this.servingSize = servingSize;
        this.allergens = allergens;
        //this.allergensHierarchy = allergensHierarchy;
        this.lastRefresh = lastRefresh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

/*    public List<String> getAllergensHierarchy() {
        return allergensHierarchy;
    }

    public void setAllergensHierarchy(List<String> allergensHierarchy) {
        this.allergensHierarchy = allergensHierarchy;
    }*/

    public Date getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(Date lastRefresh) {
        this.lastRefresh = lastRefresh;
    }
}
