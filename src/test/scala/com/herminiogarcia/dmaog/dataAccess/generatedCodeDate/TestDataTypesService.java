package com.herminiogarcia.dmaog.dataAccess.generatedCodeDate;

import com.herminiogarcia.dmaog.dataAccess.DataAccess;
import java.util.List;
import java.util.Optional;

public class TestDataTypesService {

    private DataAccess dataAccess;

    public TestDataTypesService() {
        this.dataAccess = DataAccessSingleton.getInstance();
    }

    public List<TestDataTypes> getAll() {
        return dataAccess.getAll(TestDataTypes.class);
    }

    public String getAll(String rdfFormat) {
        return dataAccess.getAll(TestDataTypes.class, rdfFormat);
    }

    public List<TestDataTypes> getAll(Long limit, Long offset) {
        return dataAccess.getAll(TestDataTypes.class, limit, offset);
    }

    public Long count() {
        return dataAccess.count(TestDataTypes.class);
    }

    public Optional<TestDataTypes> getById(String id) {
        return dataAccess.getById(TestDataTypes.class, id);
    }

    public String getById(String id, String rdfFormat) {
        return dataAccess.getById(TestDataTypes.class, id, rdfFormat);
    }

    public List<TestDataTypes> getByField(String fieldName, String value) {
        return dataAccess.getByField(TestDataTypes.class, fieldName, value);
    }

    public String getByField(String fieldName, String value, String rdfFormat) {
        return dataAccess.getByField(TestDataTypes.class, fieldName, value, rdfFormat);
    }

    public void commit(TestDataTypes instance) {
        dataAccess.delete(instance);
        dataAccess.insert(instance);
    }

    public void delete(TestDataTypes instance) {
        dataAccess.delete(instance);
    }

}