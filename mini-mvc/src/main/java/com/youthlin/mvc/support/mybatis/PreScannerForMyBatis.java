package com.youthlin.mvc.support.mybatis;

import com.youthlin.ioc.context.Context;
import com.youthlin.ioc.spi.IPreScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-18 23:03.
 */
public class PreScannerForMyBatis implements IPreScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreScannerForMyBatis.class);

    @Override
    public void preScan(Context context) {
        try {
            MapperScanner mapperScanner = new MapperScanner();
            ServletContext servletContext = context.getBean(ServletContext.class);
            if (servletContext != null) {
                setProperties(servletContext, mapperScanner);
            }
            mapperScanner.scan(context);
        } catch (Throwable e) {
            LOGGER.error("", e);
        }
    }

    private void setProperties(ServletContext servletContext, MapperScanner mapperScanner) {
        String configFile = servletContext.getInitParameter(MapperScanner.CONFIG_FILE);
        String scanAnnotation = servletContext.getInitParameter(MapperScanner.SCAN_ANNOTATION);
        String scanPackagesStr = servletContext.getInitParameter(MapperScanner.SCAN_PACKAGES);
        String initSql = servletContext.getInitParameter(MapperScanner.INIT_SQL);
        String initSqlFileName = servletContext.getInitParameter(MapperScanner.INIT_FILE);
        if (configFile != null) {
            mapperScanner.setConfigFile(configFile);
        }
        if (scanAnnotation != null) {
            mapperScanner.setScanAnnotation(scanAnnotation);
        }
        String[] scanPackagesArr = {""};
        if (scanPackagesStr != null) {
            scanPackagesArr = scanPackagesStr.split("\\s|,|;");
        }
        if (scanPackagesArr.length > 0) {
            mapperScanner.setScanPackages(scanPackagesArr);
        }
        if (initSql != null) {
            mapperScanner.setInitSql(initSql);
        }
        if (initSqlFileName != null) {
            mapperScanner.setInitSqlFile(initSqlFileName);
        }
    }
}