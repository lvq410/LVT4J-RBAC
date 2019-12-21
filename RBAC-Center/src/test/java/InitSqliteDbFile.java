import java.io.File;

import org.apache.commons.io.IOUtils;
import org.sqlite.SQLiteDataSource;

import com.lvt4j.basic.TDB;

/**
 *
 * @author LV
 */
public class InitSqliteDbFile {

    public static void main(String[] args) throws Exception {
        File dbFile = new File("./src/main/resources/com/lvt4j/rbac/sqlite.db");
        dbFile.delete();
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:./src/main/resources/com/lvt4j/rbac/sqlite.db");
        dataSource.setEnforceForeignKeys(true);
        dataSource.setIncrementalVacuum(1000);
        dataSource.setCacheSize(-200000);
        TDB db = new TDB(dataSource);
        String sqlSrc = IOUtils.toString(InitH2DbFile.class.getResourceAsStream("sqlite.sql"), "utf8");
        String[] sqls = sqlSrc.split(";");
        for(String sql : sqls){
            db.executeSQL(sql).execute();
        }
        db.executeSQL("vacuum").execute();
    }
    
}