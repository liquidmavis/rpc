package com.cuit.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/24 21:04
 */
@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil(){

    }

    public static Properties readPropertiesFile(String fileName){
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpConfigPath = "";
        if(url != null){
            rpConfigPath = url.getPath()+fileName;
        }
        Properties properties = null;
        try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(rpConfigPath), StandardCharsets.UTF_8)){
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("当读取配置文件{}时候出错了",fileName);
        }
        return properties;
    }
}
