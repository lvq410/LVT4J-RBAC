import java.io.File;
import java.sql.Connection;

import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcDataSource;

/**
 *
 * @author LV
 */
public class InitH2DbFile {

    public static void main(String[] args) throws Exception {
        File dbFile = new File("./src/main/resources/com/lvt4j/rbac/h2.mv.db");
        dbFile.delete();
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:./src/main/resources/com/lvt4j/rbac/h2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
        Connection cnn = dataSource.getConnection();
        String sqlSrc = IOUtils.toString(InitH2DbFile.class.getResourceAsStream("h2.sql"), "utf8");
        String[] sqls = sqlSrc.split(";");
        for(String sql : sqls){
            cnn.prepareStatement(sql).execute();
        }
        cnn.close();
    }
    
}