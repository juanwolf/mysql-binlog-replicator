package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.NestedRowMapper;
import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

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
        final String query = "SELECT * FROM " + getForeignTablePath() + " "
                + "WHERE " + super.getForeignTablePath() + "." + foreignKey + "=" + value;
        List<Map<String, Object>> rows =  jdbcTemplate.queryForList(query);
        NestedRowMapper nestedRowMapper = (NestedRowMapper) foreignRowMapper;
        return (List<N>) nestedRowMapper.getList(rows);
    }

    @Override
    public T reverseQueryEntity(String foreignKey, String primaryKey, String value) {
        String idValue = jdbcTemplate.queryForObject("SELECT " + foreignKey + " FROM " + getForeignTablePath() + " "
                + "WHERE " + getForeignTablePath() + "." + primaryKeyForeignEntity + "=" + value, String.class);
        // The value string is equal to the id of the foreign object
        String query = "SELECT * FROM " + getEntryTablePath() + " "
                + "WHERE " + getEntryTablePath() + "." + primaryKey + "=" + idValue;
        Object object = jdbcTemplate.queryForObject(query, rowMapper);
        return (T) object;
    }
}
