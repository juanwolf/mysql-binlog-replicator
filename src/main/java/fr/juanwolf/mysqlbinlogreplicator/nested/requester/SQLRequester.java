package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by juanwolf on 10/08/15.
 */
@Data
@Component
public abstract class SQLRequester<T, N> {

    @Getter
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * TableName for the entry T Class
     */
    String entryTableName;

    /**
     * Tablename for the N class
     */
    String exitTableName;

    public SQLRelationship sqlRelationship;

    RowMapper<N> foreignRowMapper;

    RowMapper<T> rowMapper;

    String foreignKey;

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

    public abstract N queryForeignEntity(String foreignKey, String primaryKey, String value);

    public abstract T reverseQueryEntity(String foreignKey, String primaryKey, String value);




}
