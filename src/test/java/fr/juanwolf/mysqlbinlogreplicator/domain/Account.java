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


package fr.juanwolf.mysqlbinlogreplicator.domain;

import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;

/**
 * Created by juanwolf on 17/07/15.
 */
@Document(indexName = "account")
@Mapping(mappingPath = "account")
@MysqlMapping(table = "account", repository="accountRepository")
public class Account {
    @Id
    @Setter
    @Getter
    int id;

    @Getter
    @Field(type = FieldType.Long, index = FieldIndex.analyzed)
    long identifier;

    @Getter
    @Setter
    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    String mail;

    @Getter
    @Field(index = FieldIndex.analyzed, type = FieldType.Date)
    Date creationDate;

    @Getter
    @Field(index = FieldIndex.analyzed, type = FieldType.Float)
    float cartAmount;
}
