/*
    Copyright (C) 2015  Jean-Loup Adde

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fr.juanwolf.mysqlbinlogreplicator.component;

import com.github.shyiko.mysql.binlog.event.deserialization.ColumnType;
import fr.juanwolf.mysqlbinlogreplicator.DomainClass;
import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import fr.juanwolf.mysqlbinlogreplicator.annotations.NestedMapping;
import fr.juanwolf.mysqlbinlogreplicator.nested.NestedRowMapper;
import fr.juanwolf.mysqlbinlogreplicator.nested.requester.SQLRequester;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class DomainClassAnalyzer {

    @Getter
    @Setter
    private Map<String, DomainClass> domainClassMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Setter
    @Value("${mysql.scanmapping}")
    private String scanMapping;

    @Getter
    private List<String> tableExpected;

    @Autowired
    private Environment environment;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final DateFormat BINLOG_DATETIME_FORMATTER = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.UK);

    public static final DateFormat BINLOG_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    @Getter
    public DateFormat binlogOutputDateFormatter;

    @PostConstruct
    public void postConstruct() throws BeansException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        tableExpected = new ArrayList<>();
        Reflections reflections = new Reflections(scanMapping);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(MysqlMapping.class);
        final Iterator<Class<?>> iterator = types.iterator();
        while (iterator.hasNext()) {
            Class classDomain = iterator.next();
            MysqlMapping mysqlMapping = (MysqlMapping) classDomain.getAnnotation(MysqlMapping.class);
            DomainClass domainClass = new DomainClass();
            domainClass.setDomainClass(classDomain);
            tableExpected.add(mysqlMapping.table());
            CrudRepository crudRepository = (CrudRepository) applicationContext.getBean(mysqlMapping.repository());
            domainClass.setCrudRepository(crudRepository);
            domainClass.setTable(mysqlMapping.table());
            Map<String, SQLRequester> nestedClassesMap = new HashMap<>();
            for (Field field : classDomain.getDeclaredFields()) {
                NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);
                if (nestedMapping != null) {
                    Class sqlRequesterClass = nestedMapping.sqlAssociaton().getRequesterClass();
                    Constructor sqlRequesterConstructor = sqlRequesterClass.getConstructor();
                    SQLRequester sqlRequester = (SQLRequester) sqlRequesterConstructor.newInstance();
                    sqlRequester.setEntryTableName(mysqlMapping.table());
                    sqlRequester.setExitTableName(nestedMapping.table());
                    sqlRequester.setForeignKey(nestedMapping.foreignKey());
                    sqlRequester.setPrimaryKeyForeignEntity(nestedMapping.primaryKey());
                    sqlRequester.setJdbcTemplate(jdbcTemplate);
                    NestedRowMapper currentClassNestedRowMapper = new NestedRowMapper(classDomain);
                    NestedRowMapper foreignClassNestedRowMapper = new NestedRowMapper(field.getType());
                    sqlRequester.setRowMapper(currentClassNestedRowMapper);
                    sqlRequester.setForeignRowMapper(foreignClassNestedRowMapper);
                    nestedClassesMap.put(field.getName(), sqlRequester);
                    tableExpected.add(nestedMapping.table());
                }
            }
            domainClass.setSqlRequesters(nestedClassesMap);
            domainClassMap.put(domainClass.getTable(), domainClass);
        }
        if (environment.getProperty("date.output") != null) {
            binlogOutputDateFormatter = new SimpleDateFormat(environment.getProperty("date.output"));
        }
    }

    public Object generateInstanceFromName(String name) throws ReflectiveOperationException {
        DomainClass domainClass = domainClassMap.get(name);
        if (domainClass == null) {
            log.error("Class with name {} not found.", name);
            throw new ReflectiveOperationException();
        }
        Class classAsked = domainClassMap.get(name).getDomainClass();
        try {
            Constructor classConstructor = classAsked.getConstructor();
            return classConstructor.newInstance();
        } catch (Exception e) {
            log.error("Impossible to instantiate an instance of {}: "
                    + "no empty constructor found or the constructor is private for class {}", name, name);
        }
        // Should never happen.
        return null;
    }

    public void instantiateField(Object object, Field field, Object value, int columnType, String tablename) throws ParseException, IllegalAccessException {
        field.setAccessible(true);
        if (columnType == ColumnType.DATETIME.getCode()  && field.getType() == Date.class) {
            Date date = BINLOG_DATETIME_FORMATTER.parse((String) value);
            field.set(object, date);
        } else if (columnType == ColumnType.DATE.getCode()  && field.getType() == Date.class) {
            Date date = BINLOG_DATE_FORMATTER.parse((String) value);
            field.set(object, date);
        } else if (columnType == ColumnType.DATETIME.getCode() && field.getType() == String.class) {
            Date date = BINLOG_DATETIME_FORMATTER.parse((String) value);
            if (binlogOutputDateFormatter != null) {
                field.set(object, binlogOutputDateFormatter.format(date));
            } else {
                log.warn("No date.output DateFormat found in your property file. If you want anything else than" +
                        "the timestamp as output of your date, set this property with a java DateFormat.");
                field.set(object, date.toString());
            }
        } else if (columnType == ColumnType.TIME.getCode()  && field.getType() == Time.class) {
            Time time = Time.valueOf((String) value);
            field.set(object, time);
        } else if (columnType == ColumnType.TIMESTAMP.getCode() || field.getType() == Timestamp.class) {
            Timestamp timestamp = Timestamp.valueOf((String) value);
            field.set(object, timestamp);
        } else if ((columnType == ColumnType.BIT.getCode() || columnType == ColumnType.TINY.getCode())
            && field.getType() == boolean.class) {
            boolean booleanField = ((Byte) value) != 0;
            field.set(object, booleanField);
        } else if (columnType == ColumnType.LONG.getCode() && field.getType() == long.class) {
            field.set(object, Long.parseLong((String) value));
        } else if (columnType == ColumnType.LONG.getCode() && isInteger(field)) {
            field.set(object, Integer.parseInt((String) value));
        } else if (columnType == ColumnType.FLOAT.getCode() && field.getType() == float.class) {
            field.set(object, Float.parseFloat((String) value));
        } else if (field.getType() == String.class){
            field.set(object, value);
        } else {
            Object nestedObject = generateNestedField(field, value, tablename);
            field.set(object, nestedObject);
        }
    }

    public Object generateNestedField(Field field, Object value, String tablename) {
        DomainClass currentDomainClass = domainClassMap.get(tablename);
        SQLRequester sqlRequester = currentDomainClass.getSqlRequesters().get(field.getName());
        return sqlRequester.queryForeignEntity(sqlRequester.getForeignKey(),
                sqlRequester.getPrimaryKeyForeignEntity(), (String) value);
    }

    // TOOLS

    static boolean isInteger(Field field) {
        return field.getType() == Integer.class || field.getType() == int.class;
    }
}
