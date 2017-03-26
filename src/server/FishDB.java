package server;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * This data access object (DAO) encapsulates all database calls in the bank
 * application. No code outside this class shall have any knowledge about the
 * database.
 */
public class FishDB {

    private static final String datasource = "Fish";
    private static final String dbms = "derby";
    private static final String TABLE_NAME = "FILES";
    private static final String FILENAME_COLUMN_NAME = "FILENAME";
    private static final String OWNERID_COLUMN_NAME = "OWNERID";

    /**
     *
     */
    public static final int FILENAME = 0;

    /**
     *
     */
    public static final int OWNERID = 1;
    private PreparedStatement addFileStmt;
    private PreparedStatement deleteFileStmt;
    Connection connection;

    /**
     * Constructs a new DAO object connected to the specified database.
     *
     * @throws server.FishDBException
     */
    public FishDB() throws FishDBException {
        try {
            connection = createDatasource(dbms, datasource);
            prepareStatements(connection);
        } catch (ClassNotFoundException | SQLException exception) {
            throw new FishDBException("Could not connect to datasource.", exception);
        }
    }

    private Connection createDatasource(String dbms, String datasource) throws
            ClassNotFoundException, SQLException, FishDBException {
        Connection connection = getConnection(dbms, datasource);
        boolean exist = false;
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        for (ResultSet rs = dbm.getTables(null, null, null, null); rs.next();) {
            if (rs.getString(tableNameColumn).equals(TABLE_NAME)) {
                exist = true;
                rs.close();
                break;
            }
        }

        Statement statement = connection.createStatement();
        if (exist) {
            statement.executeUpdate("DROP TABLE " + TABLE_NAME);
        }
        statement.executeUpdate("CREATE TABLE " + TABLE_NAME + " (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), filename VARCHAR(64), ownerid VARCHAR(64))");
        return connection;
    }

    private Connection getConnection(String dbms, String datasource)
            throws ClassNotFoundException, SQLException, FishDBException {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else if (dbms.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + datasource, "root", "javajava");
        } else {
            throw new FishDBException("Unable to create datasource, unknown dbms.");
        }
    }

    /**
     *
     * @param filename
     * @param ownerid
     * @throws SQLException
     */
    public void addFile(String filename, String ownerid) throws SQLException {
        addFileStmt.setString(1, filename);
        addFileStmt.setString(2, ownerid);

        int rows = addFileStmt.executeUpdate();
        if (rows == 1) {
            System.out.println("File " + filename + " from " + ownerid + " inserted");
        } else {
            System.err.println("File " + filename + " from " + ownerid + " not inserted");
        }
    }
    
    /**
     *
     * @param ownerid
     * @throws SQLException
     */
    public void deleteFiles(String ownerid) throws SQLException {
        addFileStmt.setString(1, ownerid);
        addFileStmt.executeUpdate();
    }

    /**
     *
     * @param keywords
     * @return
     * @throws SQLException
     */
    public ArrayList<String[]> searchFile(String[] keywords) throws SQLException {

        String sqlQuery = "Select * from FILES where";
        ArrayList<String[]> results = new ArrayList<String[]>();

        for (int i = 1; true; i++) {
            sqlQuery += " filename like '%" + keywords[i] + "%'";
            if (i == keywords.length - 1) {
                break;
            }
            sqlQuery += "  and";
        }

        System.out.println("Creating statement... " + sqlQuery);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlQuery);

        while (rs.next()) {
            String[] row = new String[2];

            //Retrieve by column name
            row[FILENAME] = rs.getString("filename");
            row[OWNERID] = rs.getString("ownerid");
            results.add(row);

            //Display values
            System.out.print("filename: " + row[FILENAME]);
            System.out.println(", ownerid: " + row[OWNERID]);

        }
        return results;
    }

    private void prepareStatements(Connection connection) throws SQLException {
        addFileStmt = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (filename, ownerid) VALUES (?, ?)");
        deleteFileStmt = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE ownerid = ?");
    }

}
