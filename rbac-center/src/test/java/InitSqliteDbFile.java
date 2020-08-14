import java.io.File;
import java.sql.Connection;

import org.apache.commons.io.IOUtils;
import org.sqlite.SQLiteDataSource;

/**
 *
 * @author LV
 */
public class InitSqliteDbFile {

    public static void main(String[] args) throws Exception {
        String path = "./src/main/resources/com/lvt4j/rbac/sqlite.db";
        File dbFile = new File(path);
        dbFile.delete();
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:"+path);
//        dataSource.setEnforceForeignKeys(true);
        dataSource.setIncrementalVacuum(1000);
        dataSource.setCacheSize(-200000);
        Connection cnn = dataSource.getConnection();
        String sqlSrc = IOUtils.toString(InitSqliteDbFile.class.getResourceAsStream("sqlite.sql"), "utf8");
        String[] sqls = sqlSrc.split(";");
        for(String sql : sqls){
            cnn.prepareStatement(sql).execute();
        }
        cnn.prepareStatement("vacuum").execute();
        cnn.close();
    }
    
}