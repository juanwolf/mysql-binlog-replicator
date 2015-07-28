package fr.juanwolf.mysqlbinlogreplicator.dao;

import fr.juanwolf.mysqlbinlogreplicator.domain.Account;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

/**
 * Created by juanwolf on 17/07/15.
 */
public interface AccountRepository extends ElasticsearchCrudRepository<Account, String> {

}
