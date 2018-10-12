package eu.captaincode.allergywatch.database.converter;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AllergenListConverter {
    @TypeConverter
    public String fromAllergenList(List<String> allergenList) {
        if (allergenList == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        return gson.toJson(allergenList, type);
    }

    @TypeConverter
    public List<String> toCountryLangList(String allergenString) {
        if (allergenString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(allergenString, type);
    }
}
