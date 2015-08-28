package fr.juanwolf.mysqlbinlogreplicator.nested;

import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import fr.juanwolf.mysqlbinlogreplicator.domain.User;
import fr.juanwolf.mysqlbinlogreplicator.nested.NestedRowMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by juanwolf on 17/08/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class NestedRowMapperTest {

    @Mock
    ResultSet resultSet;

    @Mock
    DomainClassAnalyzer domainClassAnalyzer;

    NestedRowMapper nestedRowMapper;

    @Before
    public void setUp() {
        nestedRowMapper = new NestedRowMapper(User.class, domainClassAnalyzer, "user");
    }

    @Test
    public void mapRow_should_return_null_if_theres_no_empty_constructor() throws SQLException {
        // Given
        class emptyClass {

        }
        nestedRowMapper = new NestedRowMapper(emptyClass.class, domainClassAnalyzer, "empty");
        // When
        Object result =  nestedRowMapper.mapRow(null, 0);
        // Then
        assertThat(result).isNull();

    }

    @Test
    public void mapRow_should_return_a_string_set_with_specific_value_in_resultSet() throws SQLException {
        // Given
        String email = "HULK@shield.com";
        when(resultSet.getString("mail")).thenReturn(email);
        // When
        User user = (User) nestedRowMapper.mapRow(resultSet, 0);
        // Then
        assertThat(user.getMail()).isEqualTo(email);
    }

    @Test
    public void mapRow_should_return_a_date_set_with_specific_value_in_resultSet() throws SQLException {
        // Given
        Calendar calendar = Calendar.getInstance();
        java.util.Date currentDate = calendar.getTime();
        java.sql.Date date = new java.sql.Date(currentDate.getTime());

        when(resultSet.getDate("creationDate")).thenReturn(date);
        // When
        User user = (User) nestedRowMapper.mapRow(resultSet, 0);
        // Then
        assertThat(user.getCreationDate()).isEqualTo(date);
    }

    @Test
    public void mapRow_should_return_a_float_set_with_specific_value_in_resultSet() throws SQLException {
        // Given
        float cartAmount = 1500;
        when(resultSet.getFloat("cartAmount")).thenReturn(cartAmount);
        // When
        User user = (User) nestedRowMapper.mapRow(resultSet, 0);
        // Then
        assertThat(user.getCartAmount()).isEqualTo(cartAmount);
    }

    @Test
    public void mapRow_should_return_a_double_set_with_specific_value_in_resultSet() throws SQLException {
        // Given
        double giftCardAmount = 1500;
        when(resultSet.getDouble("giftCardAmount")).thenReturn(giftCardAmount);
        // When
        User user = (User) nestedRowMapper.mapRow(resultSet, 0);
        // Then
        assertThat(user.getGiftCardAmount()).isEqualTo(giftCardAmount);
    }
}
