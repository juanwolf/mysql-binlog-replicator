package integrationTest.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by juanwolf on 10/08/15.
 */
@Data
@NoArgsConstructor
public class Cart {
    public int id;
    public float amount;
}
