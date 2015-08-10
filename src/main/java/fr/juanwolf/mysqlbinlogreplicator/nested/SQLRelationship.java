package fr.juanwolf.mysqlbinlogreplicator.nested;

/**
 * Created by juanwolf on 10/08/15.
 */
public enum SQLRelationship {

    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY;

    private SQLRelationship() {}



}
