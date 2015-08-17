package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import org.springframework.jdbc.core.RowMapper;

/**
 * Created by juanwolf on 10/08/15.
 */
public class OneToOneRequester<T, N> extends SQLRequester {

    public OneToOneRequester() {
        super();
        super.sqlRelationship = SQLRelationship.ONE_TO_ONE;
    }

    public OneToOneRequester(String entryTableName, String exitTableName, RowMapper<T> rowMapper,
                             RowMapper<N> foreignRowMapper) {
        super(entryTableName, exitTableName, rowMapper, foreignRowMapper);
        super.sqlRelationship = SQLRelationship.ONE_TO_ONE;
    }

    @Override
    public N queryForeignEntity(String foreignKey, String primaryKey, String value) {
        return (N) jdbcTemplate.queryForObject("SELECT FROM " + exitTableName
                + "INNER JOIN " + super.entryTableName + " ON "
                + super.entryTableName + "." + foreignKey + "=" + exitTableName + "." + primaryKey
                + "WHERE " + primaryKey + "=" + value , foreignRowMapper);
    }

    @Override
    public T reverseQueryEntity(String foreignKey, String primaryKey, String value) {
        return (T) jdbcTemplate.queryForObject("SELECT FROM " + entryTableName
                + "INNER JOIN " + super.entryTableName + " ON "
                + exitTableName + "." + foreignKey + "=" + entryTableName + "." + primaryKey
                + "WHERE " + primaryKey + "=" + value , rowMapper);
    }
}
