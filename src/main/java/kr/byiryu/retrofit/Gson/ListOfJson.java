package kr.byiryu.retrofit.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by dduck on 2018-05-13 0013.
 */
public class ListOfJson<T> implements ParameterizedType
{
    private Class<?> wrapped;

    public ListOfJson(Class<T> wrapper)
    {
        this.wrapped = wrapper;
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return new Type[] { wrapped };
    }

    @Override
    public Type getRawType()
    {
        return List.class;
    }

    @Override
    public Type getOwnerType()
    {
        return null;
    }
}