import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author LV on 2019年12月21日
 */
public class SqlMerge {

    public static void main(String[] args) throws Exception {
        File folder = new File("C:/Users/lichenxi/Desktop/rbac");
        List<String> sqls = Arrays.asList("product.sql"
                ,"user.sql"
                ,"param.sql"
                ,"access.sql"
                ,"permission.sql"
                ,"role.sql"
                ,"role_access.sql"
                ,"role_permission.sql"
                ,"visitor_param.sql"
                ,"visitor_role.sql"
                ,"visitor_access.sql"
                ,"visitor_permission.sql"
                ,"user_param.sql"
                ,"user_role.sql"
                ,"user_access.sql"
                ,"user_permission.sql");
        StringBuilder allSql = new StringBuilder();
        
        for(String sqlFile : sqls){
            allSql.append(FileUtils.readFileToString(new File(folder, sqlFile), "utf8"));
        }
        
        FileUtils.write(new File(folder, "all.sql"), allSql, "utf8");
    }
    
}
