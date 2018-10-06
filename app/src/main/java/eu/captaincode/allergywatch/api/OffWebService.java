package eu.captaincode.allergywatch.api;

import eu.captaincode.allergywatch.database.entity.Product;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OffWebService {
    @GET("/api/v0/product/{code}")
    Call<Product> getProduct(@Path("code") String code);
}
