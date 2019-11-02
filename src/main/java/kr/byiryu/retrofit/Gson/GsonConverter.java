package kr.byiryu.retrofit.Gson;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by dduck on 2018-05-13 0013.
 */

public class GsonConverter {

    private static final Gson gson = new Gson();

    public static <T> List<T> toList(String json, Class<T> typeClass)
    {
        List<T> list = gson.fromJson(json, new ListOfJson<T>(typeClass));
        return list;
    }

    public static <T> T toBean(String json, Class<T> typeClass)
    {
        if(!json.startsWith("["))
            json = "[" + json + "]";
        List<T> list = toList(json, typeClass);

        return list.get(0);
    }
}
