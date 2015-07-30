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

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.management.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by juanwolf on 27/07/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MysqlBinLogServiceTest {

    @Mock
    DomainClassAnalyzer domainClassAnalyzer;

    @Mock
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    BinaryLogClient client;

    @Mock
    MBeanServer mBeanServer;

    @Autowired
    @InjectMocks
    MysqlBinLogService mysqlBinLogService;

    private final static String MYSQL_HOST = "localhost";
    private final static int MYSQL_PORT = 3306;
    private final static String MYSQL_USER = "root";
    private final static String MYSQL_PWD = "";


    @Before
    public void setUp() {
        mysqlBinLogService.setMysqlHost(MYSQL_HOST);
        mysqlBinLogService.setMysqlPort(MYSQL_PORT);
        mysqlBinLogService.setMysqlUser(MYSQL_USER);
        mysqlBinLogService.setMysqlPassword(MYSQL_PWD);

    }


    @Test
    public void getColumnName_should_add_columns_name_to_the_columnMap() {
        // Given
        List<String> tableExpectedList = new ArrayList<>();
        tableExpectedList.add("account");
        when(domainClassAnalyzer.getTableExpected()).thenReturn(tableExpectedList);
        List<Map<String, Object>> columnsMap = new ArrayList<>();
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("COLUMN_NAME", "column_name");
        columnsMap.add(columnMap);
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(columnsMap);
        // When
        mysqlBinLogService.getColumnName();
        // Then
        assertThat(mysqlBinLogService.columnMap.get("account")).contains("column_name");
    }

    @Test
    public void postConstruct_should_instanciate_a_new_binlogclientstatisticClient() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        // Given
        ObjectName statsObjectName = new ObjectName("mysql.binlog:type=BinaryLogClientStatistics");
        // When
        mysqlBinLogService.postConstruct();
        // Then
        verify(mBeanServer).registerMBean(any(), eq(statsObjectName));
    }

    @Test
    public void startReplication_should_instanciate_a_new_listener() throws IOException {
        // Given
        // When
        mysqlBinLogService.startReplication();
        // Then
        verify(client).registerEventListener(any());
    }

    @Test
    public void startReplication_should_connect_the_client() throws IOException {
        // Given
        // When
        mysqlBinLogService.startReplication();
        // Then
        verify(client).connect();
    }

}
