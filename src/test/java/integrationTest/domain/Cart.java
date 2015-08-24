package integrationTest.domain;

import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

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
}
