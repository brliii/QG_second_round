package com.example.backend.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConvertUtil {//统一转换工具，用于实体类与dto和vo的转换
    //单个对象转换
    public static <T> T convert(Object source,Class<T> targetClass){
        if(source == null){
            return null;
        }
        try{
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            T target = constructor.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        }catch(Exception e){
            throw new RuntimeException("对象转换失败: " + e.getMessage(), e);
        }
    }

    //批量转换
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return Collections.emptyList();
        }
        List<T> targetList = new ArrayList<>();
        for (S source : sourceList) {
            T target = convert(source, targetClass);
            targetList.add(target);
        }
        return targetList;
    }

    //分页对象转换
    public static <S, T> Page<T> convertPage(Page<S> sourcePage, Class<T> targetClass) {
        Page<T> targetPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        List<T> targetRecords = convertList(sourcePage.getRecords(), targetClass);
        targetPage.setRecords(targetRecords);
        return targetPage;
    }

}
