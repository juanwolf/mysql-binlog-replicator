package integrationTest.domain;

import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import fr.juanwolf.mysqlbinlogreplicator.annotations.NestedMapping;
import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

/**
 * Created by juanwolf on 10/08/15.
 */
@Data
@NoArgsConstructor
@Document(indexName = "cart")
@Mapping(mappingPath = "cart")
@MysqlMapping(table="cart", repository = "cartRepository")
public class Cart {

    @Id
    public Integer id;

    @Field(type = FieldType.Float, index = FieldIndex.analyzed)
    public float amount;

//    @NestedMapping(primaryKey = "id", foreignKey = "cart_id", sqlAssociaton=SQLRelationship.ONE_TO_MANY, table="user")
//    @Field(type = FieldType.Nested, index=FieldIndex.analyzed)
//    List<User> users;
}
