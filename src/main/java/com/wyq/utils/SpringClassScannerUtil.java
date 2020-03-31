package com.wyq.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @program: dev
 * @description: 扫描包工具类
 * @author: heyede
 * @create: 2019-09-17
 **/
@Slf4j
public class SpringClassScannerUtil {

    private ResourcePatternResolver resolver;

    private MetadataReaderFactory metaReader;

    public SpringClassScannerUtil(ResourcePatternResolver resolver, MetadataReaderFactory metaReader){
        this.resolver = resolver;
        this.metaReader = metaReader;
    }

    /**
     * 扫描指定包路径下所有的类 ( 包括内部类 )
     *
     * @param basePackage 扫描路径
     * @return 类集合
     */
    public  List<Class<?>> scanClass(List<String> basePackage) {
        return scanClass(basePackage, null);
    }

    /**
     * 扫描指定包路径下所有的类 ( 包括内部类 )
     *
     * @param basePackages 扫描路径 多个包用英文,分开
     * @param predicate   过滤条件
     * @return 类集合
     */
    public  List<Class<?>> scanClass(List<String> basePackages, Predicate<Class<?>> predicate) {
        List<Class<?>> list = new ArrayList<Class<?>>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String basePackage : basePackages){
            for (String className : scanClassName(basePackage)) {
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (predicate == null || predicate.test(clazz)) {
                        list.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("", e);
                }
            }
        }

        return list;
    }

    /**
     * 扫描指定包路径下所有的类 ( 包括内部类 )
     *
     * @param basePackage 扫描路径
     * @return 类名集合
     */
    public  List<String> scanClassName(String basePackage) {
        List<String> list = new ArrayList<>();
        try {
            String path = packageToPath(basePackage);
            String locationPattern = String.format("classpath*:%s/**/*.class", path);
            Resource[] resources = resolver.getResources(locationPattern);

            for (Resource r : resources) {
                MetadataReader reader = metaReader.getMetadataReader(r);
                list.add(reader.getClassMetadata().getClassName());
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return list;
    }

    /**
     * 包名转换为路径名
     *
     * @param pkg 包名
     * @return 路径名
     */
    private  String packageToPath(String pkg) {
        return pkg.replaceAll("\\.", "/");
    }

}
