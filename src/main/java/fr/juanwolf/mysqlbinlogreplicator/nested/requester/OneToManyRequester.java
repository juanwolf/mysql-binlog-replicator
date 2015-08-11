package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * Created by juanwolf on 10/08/15.
 */
public class OneToManyRequester<T,N> extends SQLRequester {

    public OneToManyRequester() {
        super();
        super.sqlRelationship = SQLRelationship.ONE_TO_MANY;
    }

    public OneToManyRequester(String entryTableName, String exitTableName, RowMapper<T> rowMapper,
                              RowMapper<N> foreignMapper) {
        super(entryTableName, exitTableName, rowMapper, foreignMapper);
        super.sqlRelationship = SQLRelationship.ONE_TO_MANY;
    }

    @Override
    public List<N> queryForeignEntity(String foreignKey, String primaryKey, String value) {
        return (List<N>) jdbcTemplate.queryForList("SELECT FROM " + exitTableName
                + "INNER JOIN " + super.entryTableName + " ON "
                + super.entryTableName + "." + foreignKey + "=" + exitTableName + "." + primaryKey
                + "WHERE " + primaryKey + "=" + value, foreignRowMapper);
    }

    @Override
    public T reverseQueryEntity(String foreignKey, String primaryKey, String value) {
        return (T) jdbcTemplate.queryForObject("SELECT FROM " + entryTableName
                + "INNER JOIN " + super.entryTableName + " ON "
                + exitTableName + "." + foreignKey + "=" + entryTableName + "." + primaryKey
                + "WHERE " + primaryKey + "=" + value , rowMapper);
    }
}
