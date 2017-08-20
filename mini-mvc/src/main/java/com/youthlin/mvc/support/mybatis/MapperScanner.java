package com.youthlin.mvc.support.mybatis;

import com.youthlin.ioc.annotaion.AnnotationUtil;
import com.youthlin.ioc.annotaion.Dao;
import com.youthlin.ioc.context.Context;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-18 18:07.
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
@Resource
public class MapperScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperScanner.class);
    static final String SCAN_ANNOTATION = "mybatis-scan-annotation";
    static final String SCAN_PACKAGES = "mybatis-scan-packages";
    static final String CONFIG_FILE = "mybatis-config-file";
    static final String INIT_SQL = "mybatis-init-sql";
    static final String INIT_FILE = "mybatis-init-file";
    private Context context;
    private String scanAnnotation = Dao.class.getName();
    private String[] scanPackages = {""};
    private String configFile = "mybatis/config.xml";
    private String initSql;
    private String initSqlFile;
    private SqlSessionFactory factory;
    private SqlSessionManager manager;
    private Map<Class, Object> mappers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public void scan(Context context) {
        this.context = context;
        Class<? extends Annotation> scanAnnotation;
        try {
            scanAnnotation = (Class<? extends Annotation>) Class.forName(this.scanAnnotation);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
        if (!scanAnnotation.isAnnotation()) {
            throw new IllegalArgumentException("scan annotation class name error.");
        }
        InputStream in;
        try {
            in = Resources.getResourceAsStream(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        factory = new SqlSessionFactoryBuilder().build(in);
        manager = SqlSessionManager.newInstance(factory);//MyBatis 自带的 线程安全的 SqlSession
        context.registerBean(factory);
        context.registerBean(manager);
        Set<String> classNames = AnnotationUtil.getClassNames(scanPackages);
        for (String className : classNames) {
            try {
                Class<?> aClass = Class.forName(className);
                Annotation annotation = AnnotationUtil.getAnnotation(aClass, scanAnnotation);
                if (annotation != null) {
                    Object mapper = manager.getMapper(aClass);
                    registerMapper(mapper);
                }
            } catch (Throwable e) {
                LOGGER.debug("", e);
            }
        }
        initSql();
        initSqlFile();
    }

    private void registerMapper(Object mapper) {
        context.registerBean(mapper);
        mappers.put(mapper.getClass(), mapper);
    }

    private void initSql() {
        if (initSql != null && !initSql.isEmpty()) {
            try (SqlSession sqlSession = factory.openSession();
                 Connection connection = sqlSession.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(initSql);
                connection.commit();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }
    }

    private void initSqlFile() {
        if (initSqlFile != null && !initSqlFile.isEmpty()) {
            try {
                Reader sqlFileReader = Resources.getResourceAsReader(initSqlFile);
                try (SqlSession sqlSession = factory.openSession();
                     Connection connection = sqlSession.getConnection()) {
                    // java 执行 sql 脚本的 3 种方式 (ant,ibatis,ScriptRunner)
                    // http://mxm910821.iteye.com/blog/1701822
                    ScriptRunner scriptRunner = new ScriptRunner(connection);
                    scriptRunner.runScript(sqlFileReader);
                    connection.commit();
                } catch (SQLException e) {
                    throw new PersistenceException(e);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    //region getter and setter
    public String getScanAnnotation() {
        return scanAnnotation;
    }

    public MapperScanner setScanAnnotation(String scanAnnotation) {
        this.scanAnnotation = scanAnnotation;
        return this;
    }

    public String[] getScanPackages() {
        return scanPackages;
    }

    public MapperScanner setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
        return this;
    }

    public String getConfigFile() {
        return configFile;
    }

    public MapperScanner setConfigFile(String configFile) {
        this.configFile = configFile;
        return this;
    }

    public Map<Class, Object> getMappers() {
        return mappers;
    }

    public MapperScanner setMappers(Map<Class, Object> mappers) {
        this.mappers = mappers;
        return this;
    }

    public String getInitSql() {
        return initSql;
    }

    public MapperScanner setInitSql(String initSql) {
        this.initSql = initSql;
        return this;
    }

    public String getInitSqlFile() {
        return initSqlFile;
    }

    public MapperScanner setInitSqlFile(String initSqlFile) {
        this.initSqlFile = initSqlFile;
        return this;
    }

    public SqlSessionFactory getFactory() {
        return factory;
    }

    public SqlSessionManager getManager() {
        return manager;
    }
    //endregion
}
