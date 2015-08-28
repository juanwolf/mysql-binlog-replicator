package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * Created by juanwolf on 10/08/15.
 */
@Data
@Component
public abstract class SQLRequester<T, N> {

    @Getter
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Setter
    String databaseName;

    /**
     * Main class
     */
    Class<T> entryType;

    /**
     * Field which link the entryType to the foreignType
     */
    Field associatedField;

    /**
     * Foreign Class
     */
    Class<N> foreignType;

    /**
     * TableName for the entry T Class
     */
    String entryTableName;

    /**
     * Tablename for the N class
     */
    String exitTableName;

    public SQLRelationship sqlRelationship;

    /**
     * Mapper for the foreign Class
     */
    RowMapper<N> foreignRowMapper;

    /**
     * Mapper for the main class
     */
    RowMapper<T> rowMapper;

    /**
     * Foreign key column name in the main class
     */
    String foreignKey;

    /**
     * Primary key column name in the foreign class
     */
    String primaryKeyForeignEntity;

    public SQLRequester() {
    }

    public SQLRequester(String entryTableName, String exitTableName, RowMapper<T> rowMapper,
                        RowMapper<N> foreignRowMapper) {
        this.entryTableName = entryTableName;
        this.exitTableName = exitTableName;
        this.rowMapper = rowMapper;
        this.foreignRowMapper = foreignRowMapper;
    }

    public String getEntryTablePath() {
        return databaseName + "." + entryTableName;
    }

    public String getForeignTablePath() {
        return databaseName + "." + exitTableName;
    }

    public String getForeignKeyPath(String tablePath) {
        return tablePath + "." + foreignKey;
    }

    public String getPrimaryKey(String tablePath) {
        return tablePath + "." + primaryKeyForeignEntity;
    }

    public abstract N queryForeignEntity(String foreignKey, String primaryKey, String value);

    public abstract T reverseQueryEntity(String foreignKey, String primaryKey, String value);




}
