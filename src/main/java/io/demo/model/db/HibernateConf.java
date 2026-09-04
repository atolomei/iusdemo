package io.demo.model.db;


import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.demo.service.Settings;
 
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

 
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class HibernateConf {

	
	    static final public String PACKAGES_TO_SCAN = "io.demo.model";

        private final Settings settings;

        public HibernateConf(Settings settings) {
            this.settings = settings;
        }

        @Bean
        public DataSource dataSource() {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(settings.getDatabaseDriver());
            ds.setUrl(settings.getDatabaseUrl());
            ds.setUsername(settings.getDatabaseUsername());
            ds.setPassword(settings.getDatabasePassword());
            return ds;
        }
        
     
        @Bean(name = "entityManagerFactory")
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
            emf.setDataSource(dataSource);
            emf.setPackagesToScan(PACKAGES_TO_SCAN);
            emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            // Spring Framework 7.x rejects SessionFactory as the EMF proxy interface because
            // SessionFactory.getSchemaManager() and EntityManagerFactory.getSchemaManager()
            // have incompatible return types. Explicitly set to plain EntityManagerFactory.
            emf.setEntityManagerFactoryInterface(EntityManagerFactory.class);

            Properties jpaProps = new Properties();
            jpaProps.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            jpaProps.setProperty("hibernate.hbm2ddl.auto", "none");

            emf.setJpaProperties(jpaProps);
            return emf;
        }

        @Bean
        public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }
}



