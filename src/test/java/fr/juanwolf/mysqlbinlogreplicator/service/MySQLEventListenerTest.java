package fr.juanwolf.mysqlbinlogreplicator.service;

import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.ColumnType;
import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import fr.juanwolf.mysqlbinlogreplicator.domain.Account;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by juanwolf on 22/07/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MySQLEventListenerTest {

    @Spy
    DomainClassAnalyzer domainClassAnalyzer;

    @Mock
    Map<String, CrudRepository> repositoryMap;

    @Mock
    CrudRepository crudRepository;

    Event tableMapEvent;

    @Mock
    EventHeader tableMapEventHeader;

    @Mock
    EventHeader eventHeader;

    final String tableName = "account";

    @InjectMocks
    MySQLEventListener mySQLEventListener;

    @Before
    public void setUp() {
        when(repositoryMap.get("newTable")).thenReturn(crudRepository);
        when(domainClassAnalyzer.getRepositoryMap()).thenReturn(repositoryMap);
        when(tableMapEventHeader.getEventType()).thenReturn(EventType.TABLE_MAP);
        TableMapEventData tableMapEventData = new TableMapEventData();
        tableMapEventData.setTable(tableName);
        this.tableMapEvent = new Event(tableMapEventHeader, tableMapEventData);
        mySQLEventListener = new MySQLEventListener(new HashMap<>(), domainClassAnalyzer);
    }

    @Test
    public void constructor_should_initialize_an_empty_columnType_map() {
        // given
        // when
        MySQLEventListener mySQLEventListener = new MySQLEventListener(null, null);
        // then
        assertThat(mySQLEventListener.getColumnsTypes()).isEmpty();
    }

    @Test
    public void onEvent_should_add_entry_to_columnTypes_with_specific_type_array() {
        // given
        MySQLEventListener mySQLEventListener = new MySQLEventListener(new HashMap<>(), domainClassAnalyzer);

        // when
        mySQLEventListener.onEvent(this.tableMapEvent);
        // then
        assertThat(mySQLEventListener.getColumnsTypes()).containsKey(tableName);
    }

    @Test
    public void onEvent_should_change_the_tableName_when_a_new_TableMapEvent_is_received() {
        // given
        String newTable = "newtable";
        TableMapEventData tableMapEventData = this.tableMapEvent.getData();
        tableMapEventData.setTable(newTable);
        Event newtableMapEvent = new Event(tableMapEventHeader, tableMapEventData);
        MySQLEventListener mySQLEventListener = new MySQLEventListener(new HashMap<>(), domainClassAnalyzer);
        mySQLEventListener.onEvent(this.tableMapEvent);

        // when
        mySQLEventListener.onEvent(newtableMapEvent);
        // then
        assertThat(mySQLEventListener.getTableName()).isEqualTo(newTable);
    }

    @Test
    public void getObjectFromRows_should_return_an_object_with_field_equals_to_null_for_empty_array() {
        // given
        Serializable[] rows = {};
        when(mySQLEventListener.getDomainClassAnalyzer().generateInstanceFromName("account")).thenReturn(new Account());
        // when
        Account object = (Account) mySQLEventListener.getObjectFromRows(rows, "account");
        // then
        assertThat(object).isEqualToComparingFieldByField(new Account());
    }

    @Test
    public void getObjectFromRows_should_return_an_object_with_fields_set_to_specificValue() {
        // given
        String email = "this.is.my.mail@devel.com";
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"mail"};
        setUpMySqlEventListener(columns, types);
        Serializable[] rows = {email};
        when(domainClassAnalyzer.generateInstanceFromName("account")).thenReturn(new Account());
        // when
        Account object = (Account) mySQLEventListener.getObjectFromRows(rows, "account");
        // then
        assertThat(object.getMail()).isEqualTo(email);
    }

    @Test
    public void getObjectFromRows_should_return_an_empty_object_if_a_no_such_field_exception_occures() {
        // given
        String email = "this.is.my.mail@devel.com";
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"no field"};
        setUpMySqlEventListener(columns, types);
        Serializable[] rows = {email};
        when(domainClassAnalyzer.generateInstanceFromName("account")).thenReturn(new Account());
        // when
        Account object = (Account) mySQLEventListener.getObjectFromRows(rows, "account");
        // then
        assertThat(object).isEqualToComparingFieldByField(new Account());
    }

    @Test
    public void isTableConcern_should_return_true_if_the_table_is_contain_in_the_list() {
        // Given
        List<String> tablesExpected = new ArrayList<>();
        tablesExpected.add("account");
        when(domainClassAnalyzer.getTableExpected()).thenReturn(tablesExpected);
        mySQLEventListener.setTableName("account");
        // When
        boolean isTableConcern = mySQLEventListener.isTableConcern();
        // Then
        assertThat(isTableConcern).isTrue();

    }

    @Test
    public void isTableConcern_should_return_false_if_the_table_is_not_contained_in_the_list() {
        // Given
        List<String> tablesExpected = new ArrayList<>();
        when(domainClassAnalyzer.getTableExpected()).thenReturn(tablesExpected);
        mySQLEventListener.setTableName("account");
        // When
        boolean isTableConcern = mySQLEventListener.isTableConcern();
        // Then
        assertThat(isTableConcern).isFalse();

    }

    @Test
    public void generateDomainObjectForUpdateEvent_should_return_the_specify_object_for_the_specific_rows() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Map<String, byte[]> columnsType = new HashMap<>();
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        columnsType.put("account", types);
        Map<String, Object[]> columnMap = new HashMap<>();
        Object[] columns = {"mail"};
        columnMap.put("account", columns);
        mySQLEventListener.setColumnMap(columnMap);
        mySQLEventListener.setColumnsTypes(columnsType);
        when(domainClassAnalyzer.generateInstanceFromName("account")).thenReturn(new Account());
        UpdateRowsEventData updateRowsEventData = new UpdateRowsEventData();
        Map<Serializable[], Serializable[]> map = new HashMap<>();
        List<Map.Entry<Serializable[], Serializable[]>> datas = new ArrayList();
        Serializable[] serializables = {"john@zen.com"};
        map.put(serializables, serializables);
        datas.add(map.entrySet().stream().findFirst().get());
        updateRowsEventData.setRows(datas);
        Event event = new Event(tableMapEventHeader, updateRowsEventData);
        Account accountExpected = new Account();
        accountExpected.setMail("john@zen.com");
        // When
        Object updatedObject = mySQLEventListener.generateDomainObjectForUpdateEvent(event, "account");
        // Then
        assertThat(updatedObject).isEqualToComparingFieldByField(accountExpected);

    }

    @Test
    public void generateDomainObjectForDeleteEvent_should_return_the_specify_object_for_the_specific_rows() {
        // Given
        Map<String, byte[]> columnsType = new HashMap<>();
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        columnsType.put("account", types);
        Map<String, Object[]> columnMap = new HashMap<>();
        Object[] columns = {"mail"};
        columnMap.put("account", columns);
        mySQLEventListener.setColumnMap(columnMap);
        mySQLEventListener.setColumnsTypes(columnsType);
        when(domainClassAnalyzer.generateInstanceFromName("account")).thenReturn(new Account());
        DeleteRowsEventData deleteRowsEventData = new DeleteRowsEventData();
        List<Serializable[]> datas = new ArrayList();
        Serializable[] serializables = {"john@zen.com"};
        datas.add(serializables);
        deleteRowsEventData.setRows(datas);
        Event event = new Event(tableMapEventHeader, deleteRowsEventData);
        Account accountExpected = new Account();
        accountExpected.setMail("john@zen.com");
        // When
        Object updatedObject = mySQLEventListener.generateDomainObjectForDeleteEvent(event, "account");
        // Then
        assertThat(updatedObject).isEqualToComparingFieldByField(accountExpected);

    }

    @Test
    public void generateDomainObjectForWriteEvent_should_return_the_specify_object_for_the_specific_rows() {
        // Given
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"mail"};
        setUpMySqlEventListener(columns, types);
        setUpDomainClassAnalyzer(null, new Account());
        WriteRowsEventData updateRowsEventData = new WriteRowsEventData();
        List<Serializable[]> datas = new ArrayList();
        Serializable[] serializables = {"john@zen.com"};
        datas.add(serializables);
        updateRowsEventData.setRows(datas);
        Event event = new Event(tableMapEventHeader, updateRowsEventData);
        Account accountExpected = new Account();
        accountExpected.setMail("john@zen.com");
        // When
        Object updatedObject = mySQLEventListener.generateDomainObjectForWriteEvent(event, "account");
        // Then
        assertThat(updatedObject).isEqualToComparingFieldByField(accountExpected);

    }

    @Test
    public void onEvent_should_remove_the_object_from_the_event_received_if_the_table_is_concerned() {
        // given
        when(eventHeader.getEventType()).thenReturn(EventType.DELETE_ROWS);
        StubRepository stubRepository = new StubRepository();
        Account accountToDelete = new Account();
        stubRepository.save(accountToDelete);
        setUpDomainClassAnalyzer(stubRepository, accountToDelete);
        byte[] types = {};
        Object[] columns = {};
        setUpMySqlEventListener(columns, types);
        DeleteRowsEventData deleteRowsEventData = new DeleteRowsEventData();
        List<Serializable[]> datas = new ArrayList();
        Serializable[] serializables = {};
        datas.add(serializables);
        deleteRowsEventData.setRows(datas);
        Event event = new Event(eventHeader, deleteRowsEventData);
        // when
        mySQLEventListener.onEvent(event);
        // then
        assertThat(stubRepository.count()).isEqualTo(0);

    }

    @Test
    public void onEvent_should_insert_the_object_from_the_event_received_if_the_table_is_concerned() {
        // given
        when(eventHeader.getEventType()).thenReturn(EventType.WRITE_ROWS);
        StubRepository stubRepository = new StubRepository();
        Account accountToAdd = new Account();
        accountToAdd.setMail("test@test.com");
        setUpDomainClassAnalyzer(stubRepository, accountToAdd);
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = { "mail" };
        setUpMySqlEventListener(columns, types);
        WriteRowsEventData writeRowsEventData = new WriteRowsEventData();
        List<Serializable[]> datas = new ArrayList();
        Serializable[] serializables = {};
        datas.add(serializables);
        writeRowsEventData.setRows(datas);
        Event event = new Event(eventHeader, writeRowsEventData);
        // when
        mySQLEventListener.onEvent(event);
        // then
        assertThat(stubRepository.count()).isEqualTo(1);
    }

    @Test
    public void onEvent_should_update_the_object_from_the_event_received_if_the_table_is_concerned() {
        // given
        String emailExpected = "test@test.com";
        when(eventHeader.getEventType()).thenReturn(EventType.UPDATE_ROWS);
        StubRepository stubRepository = new StubRepository();
        Account accountToUpdate = new Account();
        setUpDomainClassAnalyzer(stubRepository, accountToUpdate);
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = { "mail" };
        setUpMySqlEventListener(columns, types);
        UpdateRowsEventData updateRowsEventData = new UpdateRowsEventData();
        List<Map.Entry<Serializable[], Serializable[]>> datas = new ArrayList<>();
        Serializable[] serializables = { emailExpected };
        Serializable[] beforeValues = {};
        datas.add(new AbstractMap.SimpleEntry<>(beforeValues, serializables));
        updateRowsEventData.setRows(datas);
        Event event = new Event(eventHeader, updateRowsEventData);
        // when
        mySQLEventListener.onEvent(event);
        // then
        String emailOfUpdatedAccount = ((Account)stubRepository.getElementList().get(0)).getMail();
        assertThat(emailOfUpdatedAccount).isEqualTo(emailExpected);
    }

    // TOOLS ----------------------------------------------------------------------

    public void setUpMySqlEventListener(Object[] columns, byte[] types) {
        mySQLEventListener.setTableName("account");
        Map<String, byte[]> columnsType = new HashMap<>();
        columnsType.put("account", types);
        Map<String, Object[]> columnMap = new HashMap<>();
        columnMap.put("account", columns);
        mySQLEventListener.setColumnMap(columnMap);
        mySQLEventListener.setColumnsTypes(columnsType);
    }

    public void setUpDomainClassAnalyzer(CrudRepository crudRepository, Account account) {
        Map<String, Class> domainMap = new HashMap<>();
        List<String> tablesExpected = new ArrayList<>();
        tablesExpected.add("account");
        domainMap.put("account", Account.class);
        when(domainClassAnalyzer.getRepositoryMap().get(tableName)).thenReturn(crudRepository);
        when(domainClassAnalyzer.getDomainNameMap()).thenReturn(domainMap);
        when(domainClassAnalyzer.getTableExpected()).thenReturn(tablesExpected);
        when(domainClassAnalyzer.generateInstanceFromName("account")).thenReturn(account);
    }


    public class StubRepository<T, ID> implements CrudRepository {
        @Getter
        private List<Object> elementList;

        public StubRepository() {
            elementList = new ArrayList<>();
        }

        @Override
        public Object save(Object o) {
            elementList.add(o);
            return o;
        }

        @Override
        public Iterable save(Iterable iterable) {
            elementList.add(iterable);
            return iterable;
        }

        @Override
        public Object findOne(Serializable serializable) {
            return null;
        }

        @Override
        public boolean exists(Serializable serializable) {
            return true;
        }

        @Override
        public Iterable findAll() {
            return elementList;
        }

        @Override
        public Iterable findAll(Iterable iterable) {
            return null;
        }

        @Override
        public long count() {
            return elementList.size();
        }

        @Override
        public void delete(Serializable serializable) {

        }

        @Override
        public void delete(Object o) {
            elementList.remove(o);
        }

        @Override
        public void delete(Iterable iterable) {

        }

        @Override
        public void deleteAll() {

        }
    }
}
