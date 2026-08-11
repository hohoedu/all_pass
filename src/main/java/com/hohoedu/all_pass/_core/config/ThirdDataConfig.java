package com.hohoedu.all_pass._core.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@MapperScan(
        basePackages = "com.hohoedu.all_pass.third",
        sqlSessionFactoryRef = "thirdSqlSessionFactory"
)
public class ThirdDataConfig {

    @Bean(name = "thirdDataSourceProperties")
    @ConfigurationProperties(prefix = "third.datasource")
    public DataSourceProperties thirdDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "thirdDataSource")
    public DataSource thirdDataSource(
            @Qualifier("thirdDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "thirdSqlSessionFactory")
    public SqlSessionFactory thirdSqlSessionFactory(
            @Qualifier("thirdDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // 폴더 없어도 에러 안 나게 처리
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources("classpath:mapper/third/*.xml");
        } catch (IOException e) {
            resources = new Resource[0];
        }
        factory.setMapperLocations(resources);

        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(config);
        return factory.getObject();
    }

    @Bean(name = "thirdTransactionManager")
    public DataSourceTransactionManager thirdTransactionManager(
            @Qualifier("thirdDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
