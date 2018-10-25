package eu.captaincode.allergywatch.database.converter;

import android.arch.persistence.room.TypeConverter;

import eu.captaincode.allergywatch.database.entity.ProductRating;

import static eu.captaincode.allergywatch.database.entity.ProductRating.Rating.DANGEROUS;
import static eu.captaincode.allergywatch.database.entity.ProductRating.Rating.SAFE;
import static eu.captaincode.allergywatch.database.entity.ProductRating.Rating.UNDEFINED;


public class RatingConverter {
    @TypeConverter
    public static ProductRating.Rating toURating(int userRating) {
        if (userRating == UNDEFINED.getCode()) {
            return UNDEFINED;
        } else if (userRating == SAFE.getCode()) {
            return SAFE;
        } else if (userRating == DANGEROUS.getCode()) {
            return DANGEROUS;
        } else {
            throw new IllegalArgumentException("Could not recognize ProductRating: " + userRating);
        }
    }

    @TypeConverter
    public static int toInteger(ProductRating.Rating userRating) {
        return userRating.getCode();
    }

}