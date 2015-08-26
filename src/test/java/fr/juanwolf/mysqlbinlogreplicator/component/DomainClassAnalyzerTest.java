/*
    Copyright (C) 2015  Jean-Loup Adde

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package fr.juanwolf.mysqlbinlogreplicator.component;


import com.github.shyiko.mysql.binlog.event.deserialization.ColumnType;
import fr.juanwolf.mysqlbinlogreplicator.DomainClass;
import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import fr.juanwolf.mysqlbinlogreplicator.annotations.NestedMapping;
import fr.juanwolf.mysqlbinlogreplicator.dao.UserRepository;
import fr.juanwolf.mysqlbinlogreplicator.domain.Bill;
import fr.juanwolf.mysqlbinlogreplicator.domain.Cart;
import fr.juanwolf.mysqlbinlogreplicator.domain.User;
import fr.juanwolf.mysqlbinlogreplicator.nested.NestedRowMapper;
import fr.juanwolf.mysqlbinlogreplicator.nested.requester.OneToOneRequester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DomainClassAnalyzerTest {

    public static final DateFormat BINLOG_DATETIME_FORMATTER = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.UK);
    public static final DateFormat BINLOG_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    @Autowired
    @InjectMocks
    private DomainClassAnalyzer domainClassAnalyzer;

    private String scanMapping = "fr.juanwolf.mysqlbinlogreplicator.domain";

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Environment environment;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        when(applicationContext.getBean("userRepository")).thenReturn(userRepository);
        when(environment.getProperty("date.output")).thenReturn(null);
        domainClassAnalyzer.setScanMapping(scanMapping);
        domainClassAnalyzer.postConstruct();
    }

    @Test
    public void constructor_should_initialize_a_map_of_domain_with_proper_fields() {
        // given
        Class _class = User.class;
        // When
        // domainClassAnalyzer.postConstruct();
        // Then
        assertThat(domainClassAnalyzer.getDomainClassMap().get("user").getDomainClass()).isEqualTo(_class);
    }

    @Test
    public void postConstruct_should_add_userTable_to_table_expected() {
        // given
        MysqlMapping mysqlMapping = (MysqlMapping) User.class.getAnnotation(MysqlMapping.class);
        String valueExpected = mysqlMapping.table();
        // when
        //domainClassAnalyzer.postConstruct();
        // then
        assertThat(domainClassAnalyzer.getMappingTablesExpected()).contains(valueExpected);
    }

    @Test
    public void postContruct_should_set_the_dateOutputFormatter_to_null_if_no_dateoutput_given() {
        // given
        // when
        // then
        assertThat(domainClassAnalyzer.getBinlogOutputDateFormatter()).isNull();
    }

    @Test
    public void postContruct_should_set_the_dateOutputFormatter_if_a_dateoutput_is_given() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // given
        String dateOutput = "YYYY-MM-dd";
        when(environment.getProperty("date.output")).thenReturn(dateOutput);
        // when
        domainClassAnalyzer.postConstruct();
        // then
        assertThat(domainClassAnalyzer.getBinlogOutputDateFormatter()).isNotNull();
    }

    @Test
    public void generateInstance_from_name_should_return_a_serviceRequest() throws ReflectiveOperationException {
        // Given
        // When
        Object user = domainClassAnalyzer.generateInstanceFromName("user");
        // Then
        assertThat(user.getClass()).isEqualTo(User.class);
    }

    @Test
    public void generateInstance_from_name_should_return_an_exception_if_no_class_has_been_found() {
        // Given
        // When
        try {
            Object none = domainClassAnalyzer.generateInstanceFromName("none");
            fail("ReflectiveOperationException expected.");
        } catch (ReflectiveOperationException e) {
            assertThat(true).isTrue();
        }
        // Then
    }

    @Test
    public void generateInstanceFromName_should_return_null_if_no_constructor_exist() throws ReflectiveOperationException {
        // Given
        class NoConstructorClass {}
        Map<String, DomainClass> domainMap = domainClassAnalyzer.getDomainClassMap();
        DomainClass domainClass = new DomainClass();
        domainClass.setDomainClass(NoConstructorClass.class);
        domainMap.put("noConstructorClass", domainClass);
        domainClassAnalyzer.setDomainClassMap(domainMap);
        // When
        NoConstructorClass noConstructorObject = (NoConstructorClass) domainClassAnalyzer.generateInstanceFromName("noConstructorClass");
        // Then
        assertThat(noConstructorObject).isNull();

    }

    @Test
    public void instantiateField_should_set_mail_with_string_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String mail = "awesome_identifier";
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("mail"), mail, 0, "user");

        // Then
        assertThat(user.getMail()).isEqualTo(mail);
    }

    @Test
    public void instantiateField_should_set_creationDate_with_date_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String date = "Wed Jul 22 13:00:00 CEST 2015";
        Date dateExpected = BINLOG_DATETIME_FORMATTER.parse(date);
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("creationDate"), date, ColumnType.DATETIME.getCode(), "user");

        // Then
        assertThat(user.getCreationDate()).isEqualTo(dateExpected);
    }

    @Test
    public void instantiateField_should_not_set_creationDate_if_date_is_not_parsable() throws ReflectiveOperationException {
        // Given
        String date = "My nice date";
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        try {
            domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("creationDate"), date, ColumnType.DATETIME.getCode(), "user");
            fail("ParseException expected.");
        } catch (ParseException e) {
            assertThat(e).hasMessage("Unparseable date: \"My nice date\"");
        }
    }

    @Test
    public void instantiateField_should_set_identifier_with_long_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String longValue = "4";
        long valueExpected = 4;
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("identifier"), longValue, ColumnType.LONG.getCode(), "user");
        // Then
        assertThat(valueExpected).isEqualTo(user.getIdentifier());

    }

    @Test
    public void instantiateField_should_set_cart_amount_with_float_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String floatValue = "4.5";
        float valueExpected = 4.5f;
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("cartAmount"), floatValue, ColumnType.FLOAT.getCode(), "user");
        // Then
        assertThat(valueExpected).isEqualTo(user.getCartAmount());
    }

    @Test
    public void isInteger_should_return_true_for_integer_Type() throws NoSuchFieldException {
        // Given
        User user = new User();
        user.setId(0);
        // When
        boolean isInteger = DomainClassAnalyzer.isInteger(user.getClass().getDeclaredField("id"));
        // Then
        assertThat(isInteger).isTrue();
    }

    @Test
    public void instantiateField_should_set_id_for_with_the_specific_value() throws ReflectiveOperationException, ParseException {
        // Given
        String id = "15";
        int idExpected = Integer.parseInt(id);
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("id"), id, ColumnType.LONG.getCode(), "user");

        // Then
        assertThat(user.getId()).isEqualTo(idExpected);
    }

    @Test
    public void instantiateField_should_set_boolean_to_true_if_value_is_1_for_byte_type() throws ReflectiveOperationException, ParseException {
        // Given
        byte isAdmin = 1;
        boolean valueExpected = true;
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("isAdmin"), isAdmin, ColumnType.BIT.getCode(), "user");

        // Then
        assertThat(user.isAdmin()).isEqualTo(valueExpected);
    }

    @Test
    public void instantiateField_should_set_boolean_to_true_if_value_is_1_for_tiny_type() throws ReflectiveOperationException, ParseException {
        // Given
        byte isAdmin = 1;
        boolean valueExpected = true;
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("isAdmin"), isAdmin, ColumnType.TINY.getCode(), "user");

        // Then
        assertThat(user.isAdmin()).isEqualTo(valueExpected);
    }

    @Test
    public void instantiateField_should_set_boolean_to_false_if_value_is_0_for_tiny_type() throws ReflectiveOperationException, ParseException {
        // Given
        byte isAdmin = 0;
        boolean valueExpected = false;
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("isAdmin"), isAdmin, ColumnType.TINY.getCode(), "user");

        // Then
        assertThat(user.isAdmin()).isEqualTo(valueExpected);
    }

    @Test
    public void instantiateField_should_set_the_string_with_the_specified_output_of_the_date() throws ReflectiveOperationException, ParseException {
        // Given
        String outputFormat = "YYYY-MM-dd";
        when(environment.getProperty("date.output")).thenReturn(outputFormat);
        // We need to reload the domainClassAnalyzer
        String date = "Wed Jul 22 13:00:00 CEST 2015";
        String dateStringExpected = "2015-07-22";
        domainClassAnalyzer.postConstruct();
        // When
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("dateString"), date, ColumnType.DATETIME.getCode(), "user");

        // Then
        assertThat(user.getDateString()).isEqualTo(dateStringExpected);
    }

    @Test
    public void instantiateField_should_set_the_string_with_the_same_output_as_received_if_no_dateouput_()
            throws ReflectiveOperationException, ParseException {
        // Given
        // We need to reload the domainClassAnalyzer
        String date = "Wed Jul 22 13:00:00 CEST 2015";
        domainClassAnalyzer.postConstruct();
        // When
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("dateString"), date, ColumnType.DATETIME.getCode(), "user");

        // Then
        assertThat(user.getDateString()).isEqualTo(date);
    }

    @Test
    public void instantiateField_should_set_the_date_with_the_specific_date_for_date_type() throws ReflectiveOperationException, ParseException {
        // Given
        // We need to reload the domainClassAnalyzer
        String date = "2015-07-09";
        Date dateExpected = BINLOG_DATE_FORMATTER.parse(date);
        domainClassAnalyzer.postConstruct();
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("creationDate"), date, ColumnType.DATE.getCode(), "user");

        // Then
        assertThat(user.getCreationDate()).isEqualTo(dateExpected);
    }

    @Test
    public void instantiateField_should_set_the_timestamp_with_the_specific_timestamp() throws ReflectiveOperationException, ParseException {
        // Given
        String timestampString = "2015-09-26 12:30:15";
        Timestamp timestamp = Timestamp.valueOf(timestampString);
        domainClassAnalyzer.postConstruct();
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("creationTimestamp"),
                timestampString, ColumnType.TIMESTAMP.getCode(), "user");
        // Then
        assertThat(user.getCreationTimestamp()).isEqualTo(timestamp);
    }


    @Test
    public void instantiateField_should_set_the_time_with_the_specific_time() throws ReflectiveOperationException, ParseException {
        // Given
        String timeString = "12:30:15";
        Time time = Time.valueOf(timeString);
        domainClassAnalyzer.postConstruct();
        User user = (User) domainClassAnalyzer.generateInstanceFromName("user");
        // When
        domainClassAnalyzer.instantiateField(user, user.getClass().getDeclaredField("creationTime"),
                timeString, ColumnType.TIME.getCode(), "user");
        // Then
        assertThat(user.getCreationTime()).isEqualTo(time);
    }

    @Test
    public void postConstruct_should_set_the_DomainClass_nested_list_with_annotated_fields() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        // Given
        NestedRowMapper userRowMapper = new NestedRowMapper(User.class, domainClassAnalyzer);
        NestedRowMapper cartRowMapper = new NestedRowMapper(Cart.class, domainClassAnalyzer);
        OneToOneRequester<User, Cart> userToClassRequester = new OneToOneRequester<>("user", "cart",
                userRowMapper , cartRowMapper);
        userToClassRequester.setJdbcTemplate(jdbcTemplate);
        userToClassRequester.setEntryType(User.class);
        userToClassRequester.setForeignType(Cart.class);
        userToClassRequester.setForeignKey("cart_id");
        userToClassRequester.setPrimaryKeyForeignEntity("id");
        userToClassRequester.setAssociatedField(User.class.getDeclaredField("cart"));
        // When
        domainClassAnalyzer.postConstruct();
        // Then
        DomainClass domainClass = domainClassAnalyzer.getDomainClassMap().get("user");
        assertThat(domainClass.getSqlRequesters()).containsValue(userToClassRequester);
    }
    
    @Test
    public void postConstruct_should_add_to_the_tableExpectedList_the_name_of_the_table() throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Given
        String tableNameExpected = User.class.getDeclaredField("cart").getAnnotation(NestedMapping.class).table();
        // When
        domainClassAnalyzer.postConstruct();
        // Then
        assertThat(domainClassAnalyzer.getNestedTables()).contains(tableNameExpected);
    }
    
    @Test
    public void generateNestedField_should_create_an_instance_of_Cart() throws NoSuchFieldException {
        // Given
        User user = new User();
        Field field  = user.getClass().getDeclaredField("cart");
        Cart cartExpected = new Cart();
        cartExpected.setAmount(1500);
        when(jdbcTemplate.queryForObject(anyString(), (RowMapper<Object>) any())).thenReturn(cartExpected);
        // When
        Cart cart = (Cart) domainClassAnalyzer.generateNestedField(field, "1", "user");
        // Then
        assertThat(cart).isEqualToComparingFieldByField(cartExpected);
    }

//    @Test
//    public void generateNestedField_should_create_an_instance_of_list_of_bills() throws NoSuchFieldException {
//        // Given
//        User user = new User();
//        String requestExpected = "SELECT * FROM bill INNER JOIN user ON user.id=bill.pk_user WHERE user.id=1";
//        Field field  = user.getClass().getDeclaredField("bills");
//        List<Map<String, Object>> billMapList = new ArrayMap<>();
//        Map<String, Object>
//        when(jdbcTemplate.queryForList(requestExpected, Bill.class)).thenReturn(billList);
//        // When
//        List<Bill> bills = (List<Bill>) domainClassAnalyzer.generateNestedField(field, "1", "user");
//        // Then
//         assertThat(bills).isEqualTo(billList);
//    }

}
