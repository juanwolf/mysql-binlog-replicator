package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.NestedRowMapper;
import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

/**
 * Created by juanwolf on 10/08/15.
 */
public class ManyToOneRequester<T, N> extends SQLRequester {

    public ManyToOneRequester() {
        super();
        super.sqlRelationship = SQLRelationship.MANY_TO_ONE;
    }

    public ManyToOneRequester(String entryTableName, String exitTableName, RowMapper<T> rowMapper,
                              RowMapper<N> foreignMapper) {
        super(entryTableName, exitTableName, rowMapper, foreignMapper);
        super.sqlRelationship = SQLRelationship.MANY_TO_ONE;
    }

    @Override
    public N queryForeignEntity(String foreignKey, String primaryKey, String value) {
        String query = "SELECT * FROM " + getForeignTablePath() + " "
                + "WHERE " + getForeignTablePath() + "." + primaryKey + "=" + value;
        return  (N) jdbcTemplate.queryForObject(query, foreignRowMapper);
    }

    @Override
    public List<T> reverseQueryEntity(String foreignKey, String primaryKey, String value) {
        String sql = "SELECT * FROM " + getEntryTablePath() + " "
                + "WHERE " + getEntryTablePath() + "." + foreignKey + "=" + value;
        List<Map<String, Object>> rows =  jdbcTemplate.queryForList(sql);
        NestedRowMapper nestedRowMapper = (NestedRowMapper) rowMapper;
        return (List<T>) nestedRowMapper.getList(rows);
    }
}
