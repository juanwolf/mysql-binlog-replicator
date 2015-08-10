package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * Created by juanwolf on 10/08/15.
 */
public abstract class SqlRequester<T, N> {

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

    public abstract N queryForeignEntity(String foreignKey, String primaryKey, String value);

    public abstract T reverseQueryEntity(String foreignKey, String primaryKey, String value);


}
