package fr.juanwolf.mysqlbinlogreplicator.nested.requester;

import fr.juanwolf.mysqlbinlogreplicator.nested.NestedRowMapper;
import fr.juanwolf.mysqlbinlogreplicator.nested.SQLRelationship;
import fr.juanwolf.mysqlbinlogreplicator.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by juanwolf on 28/08/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class OneToManyRequesterTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    NestedRowMapper nestedRowMapper;


    OneToManyRequester oneToManyRequester;

    @Test
    public void constructor_should_create_a_sql_relationship_with_one_to_many_type() {
        // Given

        // When
        OneToManyRequester oneToManyRequester = new OneToManyRequester("", "", null, null);
        // Then
        assertThat(oneToManyRequester.getSqlRelationship()).isEqualTo(SQLRelationship.ONE_TO_MANY);
    }
    
    @Test
    public void queryForeignEntity_should_return_list_of_foreign_objects() {
        // Given
        when(jdbcTemplate.queryForList(any())).thenReturn(new ArrayList());
        when(nestedRowMapper.getList(any())).thenReturn(new ArrayList());
        oneToManyRequester = new OneToManyRequester("cart", "user", null, null);
        oneToManyRequester.setJdbcTemplate(jdbcTemplate);
        oneToManyRequester.setForeignRowMapper(nestedRowMapper);
        // When
        List<User> users = oneToManyRequester.queryForeignEntity("cart_id", "id", "1");
        // Then
        assertThat(users).isNotNull();
    } 


}
