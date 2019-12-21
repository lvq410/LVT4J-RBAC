import java.io.File;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.h2.Driver;

import com.lvt4j.basic.TDB;

/**
 *
 * @author LV
 */
public class InitH2DbFile {

    public static void main(String[] args) throws Exception {
        File dbFile = new File("./src/main/resources/com/lvt4j/rbac/h2.mv.db");
        dbFile.delete();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriver(new Driver());
        dataSource.setUrl("jdbc:h2:./src/main/resources/com/lvt4j/rbac/h2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
        TDB db = new TDB(dataSource);
        String sqlSrc = IOUtils.toString(InitH2DbFile.class.getResourceAsStream("h2.sql"), "utf8");
        String[] sqls = sqlSrc.split(";");
        for(String sql : sqls){
            db.executeSQL(sql).execute();
        }
        dataSource.close();
    }
    
}