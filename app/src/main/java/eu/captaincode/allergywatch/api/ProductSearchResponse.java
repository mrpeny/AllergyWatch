package eu.captaincode.allergywatch.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import eu.captaincode.allergywatch.database.entity.Product;

public class ProductSearchResponse {

    @SerializedName("status_verbose")
    @Expose
    private String statusVerbose;

    @SerializedName("status")
    @Expose
    private int status;

    @SerializedName("code")
    @Expose
    private String code;

    @SerializedName("product")
    @Expose
    private Product product;

    public ProductSearchResponse() {
    }

    public String getStatusVerbose() {
        return statusVerbose;
    }

    public void setStatusVerbose(String statusVerbose) {
        this.statusVerbose = statusVerbose;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
