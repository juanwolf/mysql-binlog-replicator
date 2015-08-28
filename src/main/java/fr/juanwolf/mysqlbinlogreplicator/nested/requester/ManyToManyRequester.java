package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * Created by juanwolf on 10/08/15.
 */
public class ManyToManyRequester<T, N> extends SQLRequester {

    public ManyToManyRequester() {
        super();
    }

    public ManyToManyRequester(String entryTableName, String exitTableName, RowMapper<T> rowMapper,
                               RowMapper<N> foreignMapper) {
        super(entryTableName, exitTableName, rowMapper, foreignMapper);
        super.sqlRelationship = SQLRelationship.MANY_TO_MANY;
    }

    @Override
    public List<N> queryForeignEntity(String foreignKey, String primaryKey, String value) {
        return  (List<N>) jdbcTemplate.queryForList("SELECT" +
                " * FROM " + getForeignTablePath()
                + "INNER JOIN " + super.getEntryTablePath() + " ON "
                + super.getEntryTablePath() + "." + foreignKey + "=" + getForeignTablePath() + "." + primaryKey
                + "WHERE " + primaryKey + "=" + value, foreignRowMapper);
    }

    @Override
    public List<T> reverseQueryEntity(String foreignKey, String primaryKey, String value) {
        return (List<T>) jdbcTemplate.queryForList("SELECT * FROM " + getEntryTablePath()
                + "INNER JOIN " + super.getEntryTablePath() + " ON "
                + getForeignTablePath() + "." + foreignKey + "=" + getEntryTablePath() + "." + primaryKey
                + "WHERE " + primaryKey + "=" + value , rowMapper);
    }

}
