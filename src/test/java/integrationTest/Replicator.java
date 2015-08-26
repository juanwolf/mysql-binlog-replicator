package integrationTest;

import fr.juanwolf.mysqlbinlogreplicator.service.MysqlBinLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by juanwolf on 27/07/15.
 */
@SpringBootApplication
@ComponentScan("fr.juanwolf.mysqlbinlogreplicator")
public class Replicator implements CommandLineRunner {

    @Autowired
    private MysqlBinLogService mysqlBinLogService;


    public static void main(String[] args) {
        SpringApplication.run(Replicator.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        mysqlBinLogService.startReplication();
    }
}
