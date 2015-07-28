package fr.juanwolf.mysqlbinlogreplicator.service;

import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.jmx.BinaryLogClientStatistics;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

@Service
@Slf4j
public class MysqlBinLogService {

    private static final String SQL = "SELECT COLUMN_NAME FROM information_schema.columns WHERE table_schema = :tableschema AND table_name  = :tablename ORDER BY 'ordinal_position'";

    @Setter
    @Value("${mysql.host}")
    private String mysqlHost;

    @Setter
    @Value("${mysql.port}")
    private Integer mysqlPort;

    @Setter
    @Value("${spring.datasource.username}")
    private String mysqlUser;

    @Setter
    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${mysql.schema}")
    private String schemaExpected;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private DomainClassAnalyzer domainClassAnalyzer;

    BinaryLogClient client;

    Map<String, Object[]> columnMap;

    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();;

    @PostConstruct
    public void postConstruct() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {

        log.debug("Create BinaryLogClient {}@{}:{}", mysqlUser, mysqlHost, mysqlPort);
        client = new BinaryLogClient(mysqlHost, mysqlPort, mysqlUser, mysqlPassword);
        log.debug("Getting columns names.");
        getColumnName();
        log.debug("Activate JMXBean");

        ObjectName objectName = new ObjectName("mysql.binlog:type=BinaryLogClient");
        mBeanServer.registerMBean(client, objectName);

        BinaryLogClientStatistics stats = new BinaryLogClientStatistics(client);
        ObjectName statsObjectName = new ObjectName("mysql.binlog:type=BinaryLogClientStatistics");
        mBeanServer.registerMBean(stats, statsObjectName);
    }

    public void startReplication() throws IOException {
        log.debug("Register Event listener.");
        client.registerEventListener(new MySQLEventListener(columnMap, domainClassAnalyzer));
        log.info("Start Replication.");
        client.connect();
        log.debug("Client connected.");
    }

    void getColumnName() {
        log.info("Retrieving columns informations from the database.");
        final List<String> tableExpected = domainClassAnalyzer.getTableExpected();
        columnMap = new HashMap<>();
        for (String tableName : tableExpected) {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("tableschema", schemaExpected);
            paramMap.put("tablename", tableName);
            List<Map<String, Object>> columnsMap = namedParameterJdbcTemplate.queryForList(SQL, paramMap);
            ArrayList<String> columnsList = new ArrayList<>();
            for (Map<String, Object> column : columnsMap) {
                columnsList.add((String) column.get("COLUMN_NAME"));
            }
            log.debug("Columns found for {} : {}", tableName, Arrays.toString(columnsList.toArray()));
            columnMap.put(tableName, columnsList.toArray());
        }

    }
}
