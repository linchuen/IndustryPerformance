package cooba.IndustryPerformance.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

//@Configuration
//@EnableTransactionManagement
public class DataSourceConfig {
    @Bean
    public DataSource mySqlDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/IndustryPerformance");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("example");
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        return dataSourceBuilder.build();
    }

    private Properties hibernateProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        return hibernateProperties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(mySqlDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setJpaProperties(hibernateProperties());
        factoryBean.setPackagesToScan("cooba.IndustryPerformance.database.entity.Jpa");
        return factoryBean;
    }

    @Bean(name = "txTransactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
