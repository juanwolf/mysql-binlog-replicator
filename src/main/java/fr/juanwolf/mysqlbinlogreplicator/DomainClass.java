package fr.juanwolf.mysqlbinlogreplicator;

import fr.juanwolf.mysqlbinlogreplicator.nested.requester.SQLRequester;
import lombok.Data;
import org.springframework.data.repository.CrudRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by juanwolf on 07/08/15.
 */
@Data
public class DomainClass {

    private String table;

    private CrudRepository crudRepository;

    private Class domainClass;

    private Map<String, SQLRequester> sqlRequesters;

}
