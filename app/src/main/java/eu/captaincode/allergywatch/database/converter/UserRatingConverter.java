package eu.captaincode.allergywatch.database.converter;

import android.arch.persistence.room.TypeConverter;

import eu.captaincode.allergywatch.database.entity.Product.UserRating;

import static eu.captaincode.allergywatch.database.entity.Product.UserRating.DANGEROUS;
import static eu.captaincode.allergywatch.database.entity.Product.UserRating.SAFE;
import static eu.captaincode.allergywatch.database.entity.Product.UserRating.UNDEFINED;

public class UserRatingConverter {
    @TypeConverter
    public static UserRating toUserRating(int userRating) {
        if (userRating == UNDEFINED.getCode()) {
            return UNDEFINED;
        } else if (userRating == SAFE.getCode()) {
            return SAFE;
        } else if (userRating == DANGEROUS.getCode()) {
            return DANGEROUS;
        } else {
            throw new IllegalArgumentException("Could not recognize UserRating: " + userRating);
        }
    }

    @TypeConverter
    public static int toInteger(UserRating userRating) {
        return userRating.getCode();
    }
}
