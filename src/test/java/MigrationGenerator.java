import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;

public class MigrationGenerator {

    public static void main(String[] args) throws IOException {
        DbMigration dbMigration = DbMigration.create();
        dbMigration.setPlatform(Platform.SQLITE);
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setName("dungeon");
        databaseConfig.setDefaultServer(false);
        dbMigration.setServerConfig(databaseConfig);
        dbMigration.generateMigration();
    }
}
