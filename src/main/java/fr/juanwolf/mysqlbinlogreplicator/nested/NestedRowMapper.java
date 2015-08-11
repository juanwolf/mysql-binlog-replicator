package fr.juanwolf.mysqlbinlogreplicator.nested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;

/**
 * Created by juanwolf on 11/08/15.
 */
@Slf4j
public class NestedRowMapper implements RowMapper {

    Class nestedObjectClass;

    Field[] fields;

    public NestedRowMapper(Class nestedObjectClass) {
        this.nestedObjectClass = nestedObjectClass;
        fields = this.nestedObjectClass.getFields();
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            Constructor nestedConstructor = nestedObjectClass.getConstructor();
            Object nestedObject = nestedConstructor.newInstance();
            for (Field field : fields) {
                if (field.getType() == int.class) {
                    resultSet.getInt(field.getName());
                } else if (field.getType() == String.class) {
                    resultSet.getString(field.getName());
                } else if (field.getType() == Date.class) {
                    resultSet.getDate(field.getName());
                } else if (field.getType() == long.class) {
                    resultSet.getLong(field.getName());
                } else if (field.getType() == boolean.class) {
                    resultSet.getBoolean(field.getName());
                } else if (field.getType() == Time.class) {
                    resultSet.getTime(field.getName());
                } else if (field.getType() == float.class) {
                    resultSet.getFloat(field.getName());
                } else if (field.getType() == double.class) {
                    resultSet.getDouble(field.getName());
                }
            }
            return nestedObject;
        } catch (Exception e) {
            log.error("No empty constructor for the class {}", nestedObjectClass, e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NestedRowMapper that = (NestedRowMapper) o;

        return this.nestedObjectClass == that.nestedObjectClass;
    }

    @Override
    public int hashCode() {
        return nestedObjectClass.hashCode() * fields.hashCode() + 4;
    }
}
