package org.xero08.aspire.models;

import com.mongodb.BasicDBObject;
import org.xero08.aspire.DBConnection;

public class AdminDao {
    private AdminDao() {}
    private static AdminDao instance;
    public static AdminDao getInstance() {
        instance = new AdminDao();
        return instance;
    }
    public boolean validateAdmin(String username, String token) {
        BasicDBObject findQuery = new BasicDBObject("username", username).append("token", token);
        return DBConnection.database.getCollection("admins").find(findQuery).cursor().hasNext();
    }
}
