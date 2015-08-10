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
    String table();
    String foreignKey();
    SQLRelationship sqlAssociaton();
}
