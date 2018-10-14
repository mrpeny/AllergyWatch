package eu.captaincode.allergywatch.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OffWebService {
    @GET("/api/v0/product/{code}")
    Call<ProductSearchResponse> getProduct(@Path("code") Long code);
}
