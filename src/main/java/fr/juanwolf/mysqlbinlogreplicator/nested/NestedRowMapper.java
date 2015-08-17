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
        fields = this.nestedObjectClass.getDeclaredFields();
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            Constructor nestedConstructor = nestedObjectClass.getConstructor();
            Object nestedObject = nestedConstructor.newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType() == int.class) {
                    int intValue = resultSet.getInt(field.getName());
                    field.set(nestedObject, intValue);
                } else if (field.getType() == String.class) {
                    String resultString = resultSet.getString(field.getName());
                    field.set(nestedObject, resultString);
                } else if (field.getType() == Date.class) {
                    Date dateValue = resultSet.getDate(field.getName());
                    field.set(nestedObject, dateValue);
                } else if (field.getType() == long.class) {
                    long longValue = resultSet.getLong(field.getName());
                    field.set(nestedObject, longValue);
                } else if (field.getType() == boolean.class) {
                    boolean booleanValue = resultSet.getBoolean(field.getName());
                    field.set(nestedObject, booleanValue);
                } else if (field.getType() == Time.class) {
                    Time timeValue = resultSet.getTime(field.getName());
                    field.set(nestedObject, timeValue);
                } else if (field.getType() == float.class) {
                    float floatValue = resultSet.getFloat(field.getName());
                    field.set(nestedObject, floatValue);
                } else if (field.getType() == double.class) {
                    double doubleValue = resultSet.getDouble(field.getName());
                    field.set(nestedObject, doubleValue);
                }
                field.setAccessible(false);
            }
            return nestedObject;
        } catch (NoSuchMethodException e) {
            log.error("No empty constructor for the class {}. Please, create one to make this object instantiable.",
                    nestedObjectClass, e);
        } catch (Exception exception) {
            log.error("An exception occurred: ", exception);
        }
        return null;
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
