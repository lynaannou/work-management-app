package dz.usthb.eclipseworkspace.common.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public final class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            // 🔒 Never reflect into LocalDate
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonSerializer<LocalDate>)
                            (src, typeOfSrc, context) ->
                                    src == null ? null : context.serialize(src.toString()))
            .create();

    private GsonProvider() {}

    public static Gson get() {
        return GSON;
    }
}
