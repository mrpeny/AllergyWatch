package eu.captaincode.allergywatch.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import eu.captaincode.allergywatch.database.converter.RatingConverter;

@Entity
public class ProductRating {

    @PrimaryKey
    private Long barCode;

    @TypeConverters(RatingConverter.class)
    private Rating rating = Rating.UNDEFINED;

    public Long getBarCode() {
        return barCode;
    }

    public void setBarCode(Long barCode) {
        this.barCode = barCode;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public enum Rating {
        UNDEFINED(0),
        SAFE(1),
        DANGEROUS(2);

        private int code;

        Rating(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}