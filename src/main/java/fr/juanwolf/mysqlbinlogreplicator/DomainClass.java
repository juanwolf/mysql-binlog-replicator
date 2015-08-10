package fr.juanwolf.mysqlbinlogreplicator;

import lombok.Data;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by juanwolf on 07/08/15.
 */
@Data
public class DomainClass {

    private String table;

    private CrudRepository crudRepository;

    private Class domainClass;

    private List<Class> nestedDocumentsList;

}
