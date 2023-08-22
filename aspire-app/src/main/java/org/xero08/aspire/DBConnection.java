package org.xero08.aspire;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.internal.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class DBConnection {
    private final String dbConnectionStr;
    Logger logger = LoggerFactory.getLogger(DBConnection.class);
    public static MongoDatabase database;
    @Autowired
    public DBConnection(@Value("${db.connectionString}") String dbConnectionStr) {
        this.dbConnectionStr = dbConnectionStr;
        connect();
    }

    public void connect() {
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(dbConnectionStr)).serverApi(serverApi).build();
        MongoClient client = MongoClients.create(settings);
        database = client.getDatabase("aspire");
    }

    public static void setDatabase(MongoDatabase _db) {
        database = _db;
    }
}
