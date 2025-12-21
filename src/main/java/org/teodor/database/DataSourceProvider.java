package org.teodor.database;

import javax.sql.DataSource;

import lombok.NoArgsConstructor;
import org.postgresql.ds.PGSimpleDataSource;
import org.teodor.config.ConfigManager;

@NoArgsConstructor
public final class DataSourceProvider {

    private static final String DB_URL = ConfigManager.getConfig().getDbName();
    private static final String USERNAME = ConfigManager.getConfig().getDbUsername();
    private static final String PASSWORD = ConfigManager.getConfig().getDbPassword();
    private static volatile DataSource dataSource = create();


    private static DataSource create() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(DB_URL);
        ds.setUser(USERNAME);
        ds.setPassword(PASSWORD);
        return ds;
    }

    public static DataSource get() {
        return dataSource;
    }
}
