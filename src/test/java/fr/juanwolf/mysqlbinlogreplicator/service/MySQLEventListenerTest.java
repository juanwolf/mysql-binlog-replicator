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



package fr.juanwolf.mysqlbinlogreplicator.service;

import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.ColumnType;
import fr.juanwolf.mysqlbinlogreplicator.DomainClass;
import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import fr.juanwolf.mysqlbinlogreplicator.dao.UserRepository;
import fr.juanwolf.mysqlbinlogreplicator.nested.requester.OneToOneRequester;
import fr.juanwolf.mysqlbinlogreplicator.nested.requester.SQLRequester;
import integrationTest.domain.User;
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
import java.text.ParseException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Created by juanwolf on 22/07/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MySQLEventListenerTest {

    Map<String, DomainClass> domainClassMap;

    @Spy
    DomainClassAnalyzer domainClassAnalyzer;

    @Mock
    CrudRepository crudRepository;

    Event tableMapEvent;

    @Mock
    DomainClass domainClass;

    @Mock
    EventHeader tableMapEventHeader;

    @Mock
    EventHeader eventHeader;

    final String tableName = "user";

    @InjectMocks
    MySQLEventListener mySQLEventListener;

    @Before
    public void setUp() throws NoSuchFieldException {
        domainClassMap = new HashMap<>();
        DomainClass domainClassAccount = new DomainClass();
        domainClassAccount.setDomainClass(User.class);
        SQLRequester cartSQLRequester = new OneToOneRequester<>();
        cartSQLRequester.setAssociatedField(User.class.getDeclaredField("cart"));
        Map<String, SQLRequester> sqlRequesterMap = new HashMap<>();
        sqlRequesterMap.put("cart", cartSQLRequester);
        domainClassAccount.setSqlRequesters(sqlRequesterMap);
        domainClassMap.put("user", domainClassAccount);
        domainClassMap.put("newTable", domainClass);
        when(domainClassAnalyzer.getDomainClassMap()).thenReturn(domainClassMap);
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
    public void actionOnEvent_should_add_entry_to_columnTypes_with_specific_type_array() throws Exception {
        // given
        Map<String, DomainClass> localDomainClassMap = new HashMap<>();
        when(domainClass.getCrudRepository()).thenReturn(crudRepository);
        localDomainClassMap.put(tableName, domainClass);
        domainClassAnalyzer.setDomainClassMap(localDomainClassMap);
//        when(domainClassAnalyzer.getDomainClassMap()).thenReturn(localDomainClassMap);
        MySQLEventListener listener = new MySQLEventListener(new HashMap<>(), domainClassAnalyzer);
        // when
        listener.actionOnEvent(tableMapEvent);
        // then
        assertThat(listener.getColumnsTypes()).containsKey(tableName);
    }

    @Test
    public void actionOnEvent_should_change_the_tableName_when_a_new_TableMapEvent_is_received() throws Exception {
        // given
        String newTable = "newtable";
        TableMapEventData tableMapEventData = this.tableMapEvent.getData();
        tableMapEventData.setTable(newTable);
        Event newtableMapEvent = new Event(tableMapEventHeader, tableMapEventData);
        mySQLEventListener.actionOnEvent(this.tableMapEvent);
        when(domainClass.getCrudRepository()).thenReturn(crudRepository);
        when(domainClassAnalyzer.getTableExpected()).thenReturn(new ArrayList());
        // when
        mySQLEventListener.actionOnEvent(newtableMapEvent);
        // then
        assertThat(mySQLEventListener.getTableName()).isEqualTo(newTable);
    }

    @Test
    public void getObjectFromRows_should_return_an_object_with_field_equals_to_null_for_empty_array() throws ReflectiveOperationException, ParseException {
        // given
        Serializable[] rows = {};
        doReturn(new User()).when(mySQLEventListener.getDomainClassAnalyzer()).generateInstanceFromName("user");
        // when
        User object = (User) mySQLEventListener.getObjectFromRows(rows, "user");
        // then
        assertThat(object).isEqualToComparingFieldByField(new User());
    }

    @Test
    public void getObjectFromRows_should_return_an_object_with_fields_set_to_specificValue() throws ReflectiveOperationException, ParseException {
        // given
        String email = "this.is.my.mail@devel.com";
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"mail"};
        setUpMySqlEventListener(columns, types);
        Serializable[] rows = {email};
        doReturn(new User()).when(domainClassAnalyzer).generateInstanceFromName("user");
        // when
        User object = (User) mySQLEventListener.getObjectFromRows(rows, "user");
        // then
        assertThat(object.getMail()).isEqualTo(email);
    }

    @Test
    public void getObjectFromRows_should_return_an_empty_object_if_a_no_such_field_exception_occurred() throws ReflectiveOperationException, ParseException {
        // given
        String email = "this.is.my.mail@devel.com";
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"no field"};
        setUpMySqlEventListener(columns, types);
        Serializable[] rows = {email};
        doReturn(new User()).when(domainClassAnalyzer).generateInstanceFromName("user");
        // when
        User object = (User) mySQLEventListener.getObjectFromRows(rows, "user");
        // then
        assertThat(object).isEqualToComparingFieldByField(new User());
    }

    @Test
    public void getObjectFromRows_should_not_instanciate_a_field_with_a_null_value_in_rows() throws ReflectiveOperationException, ParseException {
        // given
        String email = null;
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"mail"};
        setUpMySqlEventListener(columns, types);
        Serializable[] rows = {email};
        doReturn(new User()).when(domainClassAnalyzer).generateInstanceFromName("user");
        // when
        User object = (User) mySQLEventListener.getObjectFromRows(rows, "user");
        // then
        assertThat(object).isEqualToComparingFieldByField(new User());
    }

    @Test
    public void isTableConcern_should_return_true_if_the_table_is_contain_in_the_list() {
        // Given
        List<String> tablesExpected = new ArrayList<>();
        tablesExpected.add("user");
        when(domainClassAnalyzer.getTableExpected()).thenReturn(tablesExpected);
        mySQLEventListener.setTableName("user");
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
        mySQLEventListener.setTableName("user");
        // When
        boolean isTableConcern = mySQLEventListener.isTableConcern();
        // Then
        assertThat(isTableConcern).isFalse();

    }

    @Test
    public void generateDomainObjectForUpdateEvent_should_return_the_specify_object_for_the_specific_rows() throws ReflectiveOperationException, ParseException {
        // Given
        Map<String, byte[]> columnsType = new HashMap<>();
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        columnsType.put("user", types);
        Map<String, Object[]> columnMap = new HashMap<>();
        Object[] columns = {"mail"};
        columnMap.put("user", columns);
        mySQLEventListener.setColumnMap(columnMap);
        mySQLEventListener.setColumnsTypes(columnsType);
        doReturn(new User()).when(domainClassAnalyzer).generateInstanceFromName("user");
        UpdateRowsEventData updateRowsEventData = new UpdateRowsEventData();
        Map<Serializable[], Serializable[]> map = new HashMap<>();
        List<Map.Entry<Serializable[], Serializable[]>> datas = new ArrayList();
        Serializable[] serializables = {"john@zen.com"};
        map.put(serializables, serializables);
        datas.add(map.entrySet().stream().findFirst().get());
        updateRowsEventData.setRows(datas);
        Event event = new Event(tableMapEventHeader, updateRowsEventData);
        User userExpected = new User();
        userExpected.setMail("john@zen.com");
        // When
        Object updatedObject = mySQLEventListener.generateDomainObjectForUpdateEvent(event, "user");
        // Then
        assertThat(updatedObject).isEqualToComparingFieldByField(userExpected);

    }

    @Test
    public void generateDomainObjectForDeleteEvent_should_return_the_specify_object_for_the_specific_rows() throws ReflectiveOperationException, ParseException {
        // Given
        Map<String, byte[]> columnsType = new HashMap<>();
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        columnsType.put("user", types);
        Map<String, Object[]> columnMap = new HashMap<>();
        Object[] columns = {"mail"};
        columnMap.put("user", columns);
        mySQLEventListener.setColumnMap(columnMap);
        mySQLEventListener.setColumnsTypes(columnsType);
        doReturn(new User()).when(domainClassAnalyzer).generateInstanceFromName("user");
        DeleteRowsEventData deleteRowsEventData = new DeleteRowsEventData();
        List<Serializable[]> datas = new ArrayList();
        Serializable[] serializables = {"john@zen.com"};
        datas.add(serializables);
        deleteRowsEventData.setRows(datas);
        Event event = new Event(tableMapEventHeader, deleteRowsEventData);
        User userExpected = new User();
        userExpected.setMail("john@zen.com");
        // When
        Object updatedObject = mySQLEventListener.generateDomainObjectForDeleteEvent(event, "user");
        // Then
        assertThat(updatedObject).isEqualToComparingFieldByField(userExpected);

    }

    @Test
    public void generateDomainObjectForWriteEvent_should_return_the_specify_object_for_the_specific_rows() throws ReflectiveOperationException, ParseException {
        // Given
        byte[] types = { (byte) ColumnType.VARCHAR.getCode()};
        Object[] columns = {"mail"};
        setUpMySqlEventListener(columns, types);
        setUpDomainClassAnalyzer(null, new User());
        WriteRowsEventData updateRowsEventData = new WriteRowsEventData();
        List<Serializable[]> datas = new ArrayList();
        Serializable[] serializables = {"john@zen.com"};
        datas.add(serializables);
        updateRowsEventData.setRows(datas);
        Event event = new Event(tableMapEventHeader, updateRowsEventData);
        User userExpected = new User();
        userExpected.setMail("john@zen.com");
        // When
        Object updatedObject = mySQLEventListener.generateDomainObjectForWriteEvent(event, "user");
        // Then
        assertThat(updatedObject).isEqualToComparingFieldByField(userExpected);

    }

    @Test
    public void onEvent_should_remove_the_object_from_the_event_received_if_the_table_is_concerned() throws ReflectiveOperationException {
        // given
        when(eventHeader.getEventType()).thenReturn(EventType.DELETE_ROWS);
        StubRepository stubRepository = new StubRepository();
        User userToDelete = new User();
        stubRepository.save(userToDelete);
        setUpDomainClassAnalyzer(stubRepository, userToDelete);
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
    public void onEvent_should_insert_the_object_from_the_event_received_if_the_table_is_concerned() throws ReflectiveOperationException {
        // given
        when(eventHeader.getEventType()).thenReturn(EventType.WRITE_ROWS);
        StubRepository stubRepository = new StubRepository();
        User userToAdd = new User();
        userToAdd.setMail("test@test.com");
        setUpDomainClassAnalyzer(stubRepository, userToAdd);
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
    public void onEvent_should_update_the_object_from_the_event_received_if_the_table_is_concerned() throws ReflectiveOperationException {
        // given
        String emailExpected = "test@test.com";
        when(eventHeader.getEventType()).thenReturn(EventType.UPDATE_ROWS);
        StubRepository stubRepository = new StubRepository();
        User userToUpdate = new User();
        setUpDomainClassAnalyzer(stubRepository, userToUpdate);
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
        String emailOfUpdatedAccount = ((User)stubRepository.getElementList().get(0)).getMail();
        assertThat(emailOfUpdatedAccount).isEqualTo(emailExpected);
    }

    // TOOLS ----------------------------------------------------------------------

    public void setUpMySqlEventListener(Object[] columns, byte[] types) {
        mySQLEventListener.setTableName("user");
        Map<String, byte[]> columnsType = new HashMap<>();
        columnsType.put("user", types);
        Map<String, Object[]> columnMap = new HashMap<>();
        columnMap.put("user", columns);
        mySQLEventListener.setColumnMap(columnMap);
        mySQLEventListener.setColumnsTypes(columnsType);
    }

    public void setUpDomainClassAnalyzer(CrudRepository crudRepository, User user) throws ReflectiveOperationException {
        Map<String, DomainClass> domainMap = new HashMap<>();
        List<String> tablesExpected = new ArrayList<>();
        tablesExpected.add("user");
        DomainClass domainClass = new DomainClass();
        domainClass.setDomainClass(User.class);
        domainClass.setCrudRepository(crudRepository);
        OneToOneRequester cartSQLRequester = new OneToOneRequester();
        cartSQLRequester.setAssociatedField(User.class.getDeclaredField("cart"));
        Map<String, SQLRequester> sqlRequesterMap = new HashMap<>();
        sqlRequesterMap.put("cart", cartSQLRequester);
        domainClass.setSqlRequesters(sqlRequesterMap);
        domainMap.put("user", domainClass);
        when(domainClassAnalyzer.getDomainClassMap()).thenReturn(domainMap);
        when(domainClassAnalyzer.getTableExpected()).thenReturn(tablesExpected);
        doReturn(user).when(domainClassAnalyzer).generateInstanceFromName("user");


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
