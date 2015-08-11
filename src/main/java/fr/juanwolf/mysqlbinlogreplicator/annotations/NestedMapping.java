package fr.juanwolf.mysqlbinlogreplicator.annotations;

import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by juanwolf on 07/08/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NestedMapping {

    /**
     * tablename for the foreign entity.
     */
    String table();

    /**
     * @return The name of the foreign key for the current pojo
     */
    String foreignKey();

    /**
     * @return The association between both objects
     */
    SQLRelationship sqlAssociaton();

    /**
     * @return The name of the column for the primary key of the nested field.
     */
    String primaryKey() default "id";
}
