package eu.captaincode.allergywatch.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import eu.captaincode.allergywatch.database.converter.RatingConverter;

@Entity
public class ProductRating {

    @PrimaryKey
    private Long barcode;

    @TypeConverters(RatingConverter.class)
    private Rating rating = Rating.UNDEFINED;

    public Long getBarcode() {
        return barcode;
    }

    public void setBarcode(Long barcode) {
        this.barcode = barcode;
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