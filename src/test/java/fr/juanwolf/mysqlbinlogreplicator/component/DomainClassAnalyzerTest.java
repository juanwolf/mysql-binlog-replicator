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
import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import fr.juanwolf.mysqlbinlogreplicator.dao.AccountRepository;
import fr.juanwolf.mysqlbinlogreplicator.domain.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.fail;
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
    private AccountRepository accountRepository;

    @Mock
    private Environment environment;

    @Before
    public void setUp() {
        when(applicationContext.getBean("accountRepository")).thenReturn(accountRepository);
        when(environment.getProperty("date.output")).thenReturn(null);
        domainClassAnalyzer.setScanMapping(scanMapping);
        domainClassAnalyzer.postConstruct();
    }

    @Test
    public void constructor_should_initialize_a_map_of_domain_with_proper_fields() {
        // given
        Class _class = Account.class;
        // When
        // domainClassAnalyzer.postConstruct();
        // Then
        assertThat(domainClassAnalyzer.getDomainNameMap()).containsValue(_class);
    }

    @Test
    public void postConstruct_should_add_accountTable_to_table_expected() {
        // given
        MysqlMapping mysqlMapping = (MysqlMapping) Account.class.getAnnotation(MysqlMapping.class);
        String valueExpected = mysqlMapping.table();
        // when
        //domainClassAnalyzer.postConstruct();
        // then
        assertThat(domainClassAnalyzer.getTableExpected()).contains(valueExpected);
    }

    @Test
    public void postContruct_should_set_the_dateOutputFormatter_to_null_if_no_dateoutput_given() {
        // given
        // when
        // then
        assertThat(domainClassAnalyzer.getBinlogOutputDateFormatter()).isNull();
    }

    @Test
    public void postContruct_should_set_the_dateOutputFormatter_if_a_dateoutput_is_given() {
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
        Object account = domainClassAnalyzer.generateInstanceFromName("account");
        // Then
        assertThat(account.getClass()).isEqualTo(Account.class);
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
        Map<String, Class> domainMap = domainClassAnalyzer.getDomainNameMap();
        domainMap.put("noConstructorClass", NoConstructorClass.class);
        domainClassAnalyzer.setDomainNameMap(domainMap);
        // When
        NoConstructorClass noConstructorObject = (NoConstructorClass) domainClassAnalyzer.generateInstanceFromName("noConstructorClass");
        // Then
        assertThat(noConstructorObject).isNull();

    }

    @Test
    public void instantiateField_should_set_mail_with_string_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String mail = "awesome_identifier";
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("mail"), mail, 0);

        // Then
        assertThat(account.getMail()).isEqualTo(mail);
    }

    @Test
    public void instantiateField_should_set_creationDate_with_date_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String date = "Wed Jul 22 13:00:00 CEST 2015";
        Date dateExpected = BINLOG_DATETIME_FORMATTER.parse(date);
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("creationDate"), date, ColumnType.DATETIME.getCode());

        // Then
        assertThat(account.getCreationDate()).isEqualTo(dateExpected);
    }

    @Test
    public void instantiateField_should_not_set_creationDate_if_date_is_not_parsable() throws ReflectiveOperationException {
        // Given
        String date = "My nice date";
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        try {
            domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("creationDate"), date, ColumnType.DATETIME.getCode());
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
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("identifier"), longValue, ColumnType.LONG.getCode());
        // Then
        assertThat(valueExpected).isEqualTo(account.getIdentifier());

    }

    @Test
    public void instantiateField_should_set_cart_amount_with_float_specified() throws ReflectiveOperationException, ParseException {
        // Given
        String floatValue = "4.5";
        float valueExpected = 4.5f;
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("cartAmount"), floatValue, ColumnType.FLOAT.getCode());
        // Then
        assertThat(valueExpected).isEqualTo(account.getCartAmount());
    }

    @Test
    public void isInteger_should_return_true_for_integer_Type() throws NoSuchFieldException {
        // Given
        Account account = new Account();
        account.setId(0);
        // When
        boolean isInteger = DomainClassAnalyzer.isInteger(account.getClass().getDeclaredField("id"));
        // Then
        assertThat(isInteger).isTrue();
    }

    @Test
    public void instantiateField_should_set_id_for_with_the_specific_value() throws ReflectiveOperationException, ParseException {
        // Given
        String id = "15";
        int idExpected = Integer.parseInt(id);
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("id"), id, ColumnType.LONG.getCode());

        // Then
        assertThat(account.getId()).isEqualTo(idExpected);
    }

    @Test
    public void instantiateField_should_set_boolean_to_true_if_value_is_1_for_byte_type() throws ReflectiveOperationException, ParseException {
        // Given
        byte isAdmin = 1;
        boolean valueExpected = true;
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("isAdmin"), isAdmin, ColumnType.BIT.getCode());

        // Then
        assertThat(account.isAdmin()).isEqualTo(valueExpected);
    }

    @Test
    public void instantiateField_should_set_boolean_to_true_if_value_is_1_for_tiny_type() throws ReflectiveOperationException, ParseException {
        // Given
        byte isAdmin = 1;
        boolean valueExpected = true;
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("isAdmin"), isAdmin, ColumnType.TINY.getCode());

        // Then
        assertThat(account.isAdmin()).isEqualTo(valueExpected);
    }

    @Test
    public void instantiateField_should_set_boolean_to_false_if_value_is_0_for_tiny_type() throws ReflectiveOperationException, ParseException {
        // Given
        byte isAdmin = 0;
        boolean valueExpected = false;
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("isAdmin"), isAdmin, ColumnType.TINY.getCode());

        // Then
        assertThat(account.isAdmin()).isEqualTo(valueExpected);
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
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("dateString"), date, ColumnType.DATETIME.getCode());

        // Then
        assertThat(account.getDateString()).isEqualTo(dateStringExpected);
    }

    @Test
    public void instantiateField_should_set_the_string_with_the_same_output_as_received_if_no_dateouput_()
            throws ReflectiveOperationException, ParseException {
        // Given
        // We need to reload the domainClassAnalyzer
        String date = "Wed Jul 22 13:00:00 CEST 2015";
        domainClassAnalyzer.postConstruct();
        // When
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("dateString"), date, ColumnType.DATETIME.getCode());

        // Then
        assertThat(account.getDateString()).isEqualTo(date);
    }

    @Test
    public void instantiateField_should_set_the_date_with_the_specific_date_for_date_type() throws ReflectiveOperationException, ParseException {
        // Given
        // We need to reload the domainClassAnalyzer
        String date = "2015-07-09";
        Date dateExpected = BINLOG_DATE_FORMATTER.parse(date);
        domainClassAnalyzer.postConstruct();
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("creationDate"), date, ColumnType.DATE.getCode());

        // Then
        assertThat(account.getCreationDate()).isEqualTo(dateExpected);
    }


}
