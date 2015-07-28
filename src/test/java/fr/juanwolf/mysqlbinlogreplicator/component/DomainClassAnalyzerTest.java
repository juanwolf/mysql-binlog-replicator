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

import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DomainClassAnalyzerTest {

    @Autowired
    @InjectMocks
    private DomainClassAnalyzer domainClassAnalyzer;

    private String scanMapping = "fr.juanwolf.mysqlbinlogreplicator.domain";

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private AccountRepository accountRepository;

    @Before
    public void setUp() {
        when(applicationContext.getBean("accountRepository")).thenReturn(accountRepository);
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
    public void generateInstance_from_name_should_return_a_serviceRequest() {
        // Given
        // When
        Object account = domainClassAnalyzer.generateInstanceFromName("account");
        // Then
        assertThat(account.getClass()).isEqualTo(Account.class);
    }

    @Test
    public void generateInstance_from_name_should_return_null_if_no_class_has_been_found() {
        // Given
        // When
        Object serviceRequest = domainClassAnalyzer.generateInstanceFromName("none");
        // Then
        assertThat(serviceRequest).isNull();
    }

    @Test
    public void generateInstanceFromName_should_return_null_if_no_empty_constructor_in_class_has_been_found() {
        // Given
        // When
        Object serviceRequest = domainClassAnalyzer.generateInstanceFromName("none");
        // Then
        assertThat(serviceRequest).isNull();
    }

    @Test
    public void instantiateField_should_set_mail_with_string_specified() throws NoSuchFieldException, ParseException {
        // Given
        String mail = "awesome_identifier";
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("mail"), mail, 0);

        // Then
        assertThat(account.getMail()).isEqualTo(mail);
    }

    @Test
    public void instantiateField_should_set_creationDate_with_date_specified() throws NoSuchFieldException, ParseException {
        // Given
        String date = "Wed Jul 22 13:00:00 CEST 2015";
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("creationDate"), date, ColumnType.DATETIME.getCode());

        // Then
        assertThat(date).isEqualTo(account.getCreationDate().toString());
    }

    @Test
    public void instantiateField_should_not_set_creationDate_if_date_is_not_parsable() throws NoSuchFieldException, ParseException {
        // Given
        String date = "My nice date";
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("creationDate"), date, ColumnType.DATETIME.getCode());

        // Then
        assertThat(account.getCreationDate()).isNull();
    }

    @Test
    public void instantiateField_should_set_identifier_with_long_specified() throws NoSuchFieldException, ParseException {
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
    public void instantiateField_should_set_cart_amount_with_float_specified() throws NoSuchFieldException, ParseException {
        // Given
        String floatValue = "4.5";
        float valueExpected = 4.5f;
        Account account = (Account) domainClassAnalyzer.generateInstanceFromName("account");
        // When
        domainClassAnalyzer.instantiateField(account, account.getClass().getDeclaredField("cartAmount"), floatValue, ColumnType.FLOAT.getCode());
        // Then
        assertThat(valueExpected).isEqualTo(account.getCartAmount());
    }


}
