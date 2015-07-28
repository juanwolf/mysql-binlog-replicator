package fr.juanwolf.mysqlbinlogreplicator.component;


import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import com.github.shyiko.mysql.binlog.event.deserialization.ColumnType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class DomainClassAnalyzer {

    @Getter
    private Map<String, Class> domainNameMap = new HashMap<>();

    @Getter
    private Map<String, CrudRepository> repositoryMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Setter
    @Value("${mysql.scanmapping}")
    private String scanMapping;

    @Getter
    private List<String> tableExpected;

    public static final DateFormat BINLOG_DATE_FORMATTER = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.UK);

    @PostConstruct
    public void postConstruct() throws BeansException {
        tableExpected = new ArrayList<>();
        Reflections reflections = new Reflections(scanMapping);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(MysqlMapping.class);
        final Iterator<Class<?>> iterator = types.iterator();
        while (iterator.hasNext()) {
            Class classDomain = iterator.next();
            MysqlMapping mysqlMapping = (MysqlMapping) classDomain.getAnnotation(MysqlMapping.class);
            tableExpected.add(mysqlMapping.table());
            domainNameMap.put(mysqlMapping.table(), classDomain);
            CrudRepository crudRepository = (CrudRepository) applicationContext.getBean(mysqlMapping.repository());
            repositoryMap.put(mysqlMapping.table(), crudRepository);
        }
    }

    public Object generateInstanceFromName(String name) {
        Class classAsked = domainNameMap.get(name);
        if (classAsked == null) {
            log.error("Class with name {} not found.", name);
        }
        try {
            Constructor classConstructor = classAsked.getConstructor();
            return classConstructor.newInstance();
        } catch (Exception e) {
            log.error("No empty constructor found or the constructor is private for class {}", name);
        }
        // Should never happen.
        return null;
    }

    public void instantiateField(Object object, Field field, Object value, int columnType) {
        field.setAccessible(true);
        try {
            if (columnType == ColumnType.DATETIME.getCode() && field.getType() == Date.class) {
                try {
                    Date date = BINLOG_DATE_FORMATTER.parse((String) value);
                    field.set(object, date);
                } catch (ParseException e) {
                    log.error("Could not parse the date {} : {}", value, e.getMessage());
                }
            } else if (columnType == ColumnType.LONG.getCode() && field.getType() == long.class) {
                field.set(object, Long.parseLong((String) value));
            } else if (columnType == ColumnType.FLOAT.getCode() && field.getType() == float.class) {
                field.set(object, Float.parseFloat((String) value));
            } else {
                field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            log.error(e.getMessage());
        }
    }
}
