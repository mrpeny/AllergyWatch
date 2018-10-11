package eu.captaincode.allergywatch.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

import eu.captaincode.allergywatch.database.entity.Product;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ProductDao {
    @Insert(onConflict = REPLACE)
    void save(Product product);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Product> products);

    @Query("SELECT * FROM product WHERE code = :code")
    LiveData<Product> load(String code);

    @Query("SELECT * FROM product WHERE code = :code AND lastRefresh > :lastRefreshMax LIMIT 1")
    Product hasProduct(String code, Date lastRefreshMax);

    @Query("SELECT * FROM product WHERE code = :code LIMIT 1")
    Product loadByCode(String code);
}
