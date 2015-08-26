package fr.juanwolf.mysqlbinlogreplicator.nested;

import fr.juanwolf.mysqlbinlogreplicator.nested.requester.*;

import javax.persistence.OneToMany;

/**
 * Created by juanwolf on 10/08/15.
 */
public enum SQLRelationship {

    ONE_TO_ONE {
        @Override
        public Class getRequesterClass() {
            return OneToOneRequester.class;
        }
    },
    ONE_TO_MANY {
        @Override
        public Class getRequesterClass() {
            return OneToManyRequester.class;
        }
    },
    MANY_TO_ONE {
        @Override
        public Class getRequesterClass() {
            return ManyToOneRequester.class;
        }
    },
    MANY_TO_MANY {
        @Override
        public Class getRequesterClass() {
            return ManyToManyRequester.class;
        }
    };

    private SQLRelationship() {}

    public abstract Class getRequesterClass();



}
