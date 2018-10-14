package eu.captaincode.allergywatch.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

import eu.captaincode.allergywatch.database.entity.Product;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ProductDao {
    @Insert(onConflict = REPLACE)
    void save(Product product);

    @Insert(onConflict = REPLACE)
    void saveBoth(Product product, Product product2);

    @Insert(onConflict = REPLACE)
    void saveAll(List<Product> products);

    @Update
    void update(Product product);

    @Query("SELECT * FROM product WHERE code = :code")
    LiveData<Product> load(Long code);

    @Query("SELECT * FROM product WHERE code = :code AND lastRefresh > :lastRefreshMax LIMIT 1")
    Product hasProduct(Long code, Date lastRefreshMax);

    @Query("SELECT * FROM product WHERE code = :code LIMIT 1")
    Product loadByCode(String code);

    @Query("SELECT * FROM product")
    LiveData<List<Product>> findAll();

    @Query("SELECT * FROM product")
    List<Product> findAllProducts();

}
