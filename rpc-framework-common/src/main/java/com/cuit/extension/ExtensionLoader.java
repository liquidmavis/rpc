package com.cuit.extension;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.base.Utf8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:52
 */
@Slf4j
public class ExtensionLoader<T> {
    private static final String SERVICE_DIR = "META-INF/extensions/";
    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new HashMap<Class<?>,ExtensionLoader<?>>();
    private static final Map<Class<?>,Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>,Object>();

    private final Class<?> type;
    private final Map<String,Holder<Object>> cachedInstance = new ConcurrentHashMap();
    private final Holder<Map<String,Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    public static <S>ExtensionLoader<S> getExtensionLoader(Class<?> type){
        if(type == null){
            throw new IllegalArgumentException("扩展类型为空");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("扩展类必须为接口");
        }
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("扩展类没有SPI注解");
        }
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        if(extensionLoader == null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    public <T> T getExtension(String name){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("扩展类名字不能为空");
        }
        Holder<Object> objectHolder = cachedInstance.get(name);
        if(objectHolder == null){
            cachedInstance.putIfAbsent(name,new Holder<Object>());
            objectHolder = cachedInstance.get(name);
        }
        T value = (T)objectHolder.getValue();
        if(value == null){
            synchronized(objectHolder){
                value = (T)objectHolder.getValue();
                if(value == null){
                    value = createExtension(name);
                    objectHolder.setValue(value);
                }
            }
        }
        return (T)value;
    }

    private<T> T createExtension(String name){
        Class<?> aClass = getExtensionClasses().get(name);
        if(aClass == null){
            throw new RuntimeException("没有这个扩展类SPI"+name);
        }
        T instance = (T)EXTENSION_INSTANCES.get(aClass);
        if(instance == null){
            try {
                EXTENSION_INSTANCES.putIfAbsent(aClass,aClass.newInstance());
                instance = (T)EXTENSION_INSTANCES.get(aClass);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return instance;

    }

    private Map<String,Class<?>> getExtensionClasses(){
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if(classes == null){
            synchronized(cachedClasses){
                classes = cachedClasses.getValue();
                if(classes == null){
                    classes = new HashMap<>();
                    loadDir(classes);
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    private void loadDir(Map<String,Class<?>> extensionClasses){
        String filename = ExtensionLoader.SERVICE_DIR + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(filename);
            if(urls != null){
                while(urls.hasMoreElements()){
                    URL url = urls.nextElement();
                    loadResources(extensionClasses,url,classLoader);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void loadResources(Map<String,Class<?>> extensionClasses, URL url,ClassLoader classLoader){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))){
            String line;
            while ((line = reader.readLine()) != null){
                if(line.length() > 0){
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0,ei).trim();
                        String className = line.substring(ei + 1).trim();
                        //检查两个值必须不为空
                        if(name.length() > 0 && className.length() > 0){
                            Class<?> aClass = classLoader.loadClass(className);
                            extensionClasses.put(name,aClass);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }catch(IOException e){
            log.error(e.getMessage());
        }
    }
}
