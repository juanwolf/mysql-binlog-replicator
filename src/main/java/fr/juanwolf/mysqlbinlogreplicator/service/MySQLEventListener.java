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
import com.github.shyiko.mysql.binlog.event.*;
import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MySQLEventListener implements BinaryLogClient.EventListener {

    //All columnNames for all tables
    @Setter
    private Map<String, Object[]> columnMap;
    // All the column type for the table
    @Getter
    @Setter
    private Map<String, byte[]> columnsTypes;
    //Current tableName
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String tableName;

    @Getter
    private DomainClassAnalyzer domainClassAnalyzer;

    public MySQLEventListener(Map<String, Object[]> columnMap, DomainClassAnalyzer domainClassAnalyzer) {
        this.columnMap = columnMap;
        this.domainClassAnalyzer = domainClassAnalyzer;
        this.columnsTypes =  new HashMap<>();
    }

    @Override
    public void onEvent(Event event) {
        try {
            this.actionOnEvent(event);
        } catch (Exception e) {
            log.error("An exception occurred during OnEvent.", e);
        }
    }

    public void actionOnEvent(Event event) throws Exception {
        CrudRepository currentRepository = domainClassAnalyzer.getRepositoryMap().get(tableName);
        Class currentClass = domainClassAnalyzer.getDomainNameMap().get(tableName);
        if (EventType.isDelete(event.getHeader().getEventType())) {
            if (isTableConcern()) {
                Object domainObject = currentClass.cast(generateDomainObjectForDeleteEvent(event, tableName));
                currentRepository.delete(domainObject);
                log.debug("Object deleted : {}", domainObject.toString());
            }
        } else if (EventType.isUpdate(event.getHeader().getEventType())) {
            if (isTableConcern()) {
                UpdateRowsEventData data = event.getData();
                log.debug("Update event received data = {}", data);
                Object domainObject = currentClass.cast(generateDomainObjectForUpdateEvent(event, tableName));
                currentRepository.save(domainObject);
            }
        } else if (EventType.isWrite(event.getHeader().getEventType())) {
            if (isTableConcern()) {
                WriteRowsEventData data = event.getData();
                log.debug("Write event received with data = {}", data);
                Object currentClassInstance = currentClass.cast(generateDomainObjectForWriteEvent(event, tableName));
                currentRepository.save(currentClassInstance);
            }
        } else if (event.getHeader().getEventType() == EventType.TABLE_MAP) {
            TableMapEventData tableMapEventData = event.getData();
            tableName = tableMapEventData.getTable();
            if (!columnsTypes.containsKey(tableName)) {
                columnsTypes.put(tableName, tableMapEventData.getColumnTypes());
            }
        }
    }

    boolean isTableConcern() {
        return domainClassAnalyzer.getTableExpected().contains(tableName);
    }

    Object generateDomainObjectForUpdateEvent(Event event, String tableName) throws ReflectiveOperationException {
        UpdateRowsEventData data = event.getData();
        Serializable[] afterValues = data.getRows().get(0).getValue();
        return getObjectFromRows(afterValues, tableName);
    }

    Object generateDomainObjectForWriteEvent(Event event, String tableName) throws ReflectiveOperationException {
        WriteRowsEventData data = event.getData();
        Serializable[] rows = data.getRows().get(0);
        return getObjectFromRows(rows, tableName);
    }

    Object generateDomainObjectForDeleteEvent(Event event, String tableName) throws ReflectiveOperationException {
        DeleteRowsEventData data = event.getData();
        Serializable[] rows = data.getRows().get(0);
        return getObjectFromRows(rows, tableName);
    }

    Object getObjectFromRows(Serializable[] rows, String tableName) throws ReflectiveOperationException {
        Object[] columns = columnMap.get(tableName);
        Object object = domainClassAnalyzer.generateInstanceFromName(tableName);
        String debugLogObject = "";
        byte[] columnsType = columnsTypes.get(tableName);
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] != null) {
                try {
                    Field field = object.getClass().getDeclaredField(columns[i].toString());
                    domainClassAnalyzer.instantiateField(object, field, rows[i].toString(), columnsType[i]);
                    if (log.isDebugEnabled()) {
                        debugLogObject += columns[i] + "=" + rows[i].toString() + ", ";
                    }
                } catch (NoSuchFieldException exception) {
                    log.warn("No field found for {}", columns[i].toString(), exception);
                }
            }
        }
        log.debug("Object generated :  {{}}", debugLogObject);
        return object;
    }

}
