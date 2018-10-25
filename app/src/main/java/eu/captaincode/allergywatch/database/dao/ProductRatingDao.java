package eu.captaincode.allergywatch.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import eu.captaincode.allergywatch.database.entity.ProductRating;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ProductRatingDao {

    @Insert(onConflict = REPLACE)
    void save(ProductRating productRating);
}