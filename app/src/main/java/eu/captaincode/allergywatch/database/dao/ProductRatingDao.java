package eu.captaincode.allergywatch.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import eu.captaincode.allergywatch.database.entity.ProductRating;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ProductRatingDao {

    @Insert(onConflict = REPLACE)
    void save(ProductRating productRating);

    @Query("SELECT * FROM ProductRating WHERE barcode = :code")
    LiveData<ProductRating> findBy(Long code);
}