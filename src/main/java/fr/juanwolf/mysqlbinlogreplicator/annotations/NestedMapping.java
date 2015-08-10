package fr.juanwolf.mysqlbinlogreplicator.annotations;

/**
 * Created by juanwolf on 07/08/15.
 */
public @interface NestedMapping {
    String table();
    String foreign_key();
    SQLAssociation sqlAssociaton();
}
