package ch.skyfy.tinyeconomy.database;

import ch.skyfy.tinyeconomy.Configurator;
import ch.skyfy.tinyeconomy.features.Economy;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ch.skyfy.tinyeconomy.TinyEconomy.DISABLED;
import static ch.skyfy.tinyeconomy.TinyEconomy.MOD_CONFIG_DIR;

public class Database {

    private static class DatabaseHolder {
        public static final Database INSTANCE = new Database();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Database getInstance() {
        return Database.DatabaseHolder.INSTANCE;
    }

    private final File dbFile;
    private final String url;

    public Database() {
        dbFile = MOD_CONFIG_DIR.resolve("tinyeconomy.db").toFile();
        url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        registerEvents();
    }

    public void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            createNewDatabase();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            addPlayer(handler.player);
        });
    }

    /**
     * If database not exist, will create the database
     * else will update database
     */
    private void createNewDatabase() {
        if (dbFile.exists()) {
            updateDatabase();
            return;
        }
        DbUtils.executeQuery(url, statement -> {
            System.out.println("[Tiny Economy] Database has been created");
            var player_table = """
                    create table if not exists player
                    (
                        id   integer      not null
                            constraint player_pk
                                primary key autoincrement,
                        uuid varchar(255) not null,
                        name varchar(255) not null,
                        monney float not null
                    );
                    """;
            var unique_player = """
                    create unique index player_uuid_uindex
                        on player (uuid);""";
            var item_table = """
                    create table if not exists item
                    (
                        id             integer      not null
                            constraint item_pk
                                primary key autoincrement,
                        translationKey varchar(255) not null
                    );
                    """;
            var unique_item = """
                    create unique index if not exists item_translationKey_uindex
                        on item (translationKey);""";
            var entity_table = """
                    create table if not exists entity
                    (
                        id             integer      not null
                            constraint entity_pk
                                primary key autoincrement,
                        translationKey varchar(255) not null
                    );
                    """;
            var unique_entity = """
                    create unique index if not exists entity_translationKey_uindex
                        on entity (translationKey);""";
            var advancement_table = """
                    create table if not exists advancement
                    (
                        id   integer      not null
                            constraint advancement_pk
                                primary key autoincrement,
                        minecraftId varchar(255) not null
                    );
                    """;
            var unique_advancement = """
                    create unique index if not exists advancement_minecraftId_uindex
                        on advancement (minecraftId);""";

            var blockMinedReward_table = """
                    create table if not exists blockMinedReward
                    (
                        id      integer not null
                            constraint blockMinedReward_pk
                                primary key autoincrement,
                        amount  float   not null,
                        item_id integer not null
                    );""";

            var entityReward_table = """
                    create table if not exists entityReward
                    (
                        id      integer not null
                            constraint entityReward_pk
                                primary key autoincrement,
                        amount  float   not null,
                        entity_id integer not null
                    );""";

            var advancementReward_table = """
                    create table if not exists advancementReward
                    (
                        id      integer not null
                            constraint advancementReward_pk
                                primary key autoincrement,
                        amount  float   not null,
                        advancement_id integer not null
                    );""";

            var shopTransaction = """
                    create table if not exists shopTransaction
                    (
                        id         integer      not null
                            constraint shopTransaction_pk
                                primary key autoincrement,
                        buyerUUID  varchar(255) not null,
                        sellerUUID varchar(255) not null,
                        price      integer      not null,
                        itemAmount integer      not null,
                        item_id    integer      not null
                    );""";

            var entityKilledTransaction = """
                    create table if not exists entityKilledTransaction
                    (
                        id        integer not null
                            constraint deposit_pk
                                primary key autoincrement,
                        price     float   not null,
                        player_id integer not null,
                        entity_id integer not null
                    );""";

            var blockMinedTransaction = """
                    create table if not exists blockMinedTransaction
                    (
                        id        integer not null
                            constraint blockMinedTransaction_pk
                                primary key autoincrement,
                        price     float   not null,
                        player_id integer not null,
                        item_id   integer not null
                    );""";

            var advancementDoneTransaction = """
                    create table if not exists advancementDoneTransaction
                    (
                        id             integer not null
                            constraint advancementDoneTransaction_pk
                                primary key autoincrement,
                        price          float   not null,
                        player_id      integer not null,
                        advancement_id integer not null
                    );""";


            try {
                statement.execute(player_table);

                statement.execute(unique_player);

                statement.execute(item_table);
                statement.execute(unique_item);

                statement.execute(entity_table);
                statement.execute(unique_entity);

                statement.execute(advancement_table);
                statement.execute(unique_advancement);

                statement.execute(blockMinedReward_table);
                statement.execute(entityReward_table);
                statement.execute(advancementReward_table);

                statement.execute(shopTransaction);
                statement.execute(entityKilledTransaction);
                statement.execute(blockMinedTransaction);
                statement.execute(advancementDoneTransaction);

                insertEntity(statement);
                insertItem(statement);
                insertAdvancement(statement);

                insertBlockMinedReward(statement);
                insertEntityRewards(statement);
                insertAdvancementRewards(statement);
            } catch (SQLException e) {
                e.printStackTrace();
                DISABLED.set(true);
            }
        });
    }

    /**
     * Configurator class red data from different json file like advancementRewards.json and store in advancementRewards Map<String, Double> object
     * we will now compare value in database with the values that the configurator class has read
     * and update the value to the database if value changed
     */
    private void updateDatabase() {
        updateAdvancementReward();
        updateEntityReward();
        updateBlockMinedReward();
    }

    private void updateAdvancementReward() {
        var getRewardQuery = "SELECT advancement_id, amount FROM advancementReward where advancement_id == (select advancement.id from advancement where advancement.minecraftId like '%s');";
        var updateRewardQuery = "UPDATE advancementReward SET amount = %s WHERE advancement_id == %d;";
        DbUtils.updateRewards(url, getRewardQuery, updateRewardQuery, "amount", "advancement_id", Configurator.getInstance().earnData.advancementRewards);
    }

    private void updateEntityReward() {
        var getRewardQuery = "SELECT entity_id, amount FROM entityReward where entity_id == (select entity.id from entity where entity.translationKey like '%s');";
        var updateRewardQuery = "UPDATE entityReward SET amount = %s WHERE entity_id == %d;";
        DbUtils.updateRewards(url, getRewardQuery, updateRewardQuery, "amount", "entity_id", Configurator.getInstance().earnData.entityRewards);
    }

    private void updateBlockMinedReward() {
        var getRewardQuery = "SELECT item_id, amount FROM blockMinedReward where item_id == (select item.id from item where item.translationKey like '%s');";
        var updateRewardQuery = "UPDATE blockMinedReward SET amount = %s WHERE item_id == %d;";
        DbUtils.updateRewards(url, getRewardQuery, updateRewardQuery, "amount", "item_id", Configurator.getInstance().earnData.blockMinedRewards);
    }

    private void insertAdvancement(Statement statement) throws SQLException {
        var sb = new StringBuilder("\n");
        var array = Configurator.getInstance().earnData.advancementRewards.keySet().toArray(String[]::new);
        for (int i = 0; i < array.length; i++) {
            var advancement = array[i];
            if (i == array.length - 1) {
                sb.append("('").append(advancement).append("');");
            } else {
                sb.append("('").append(advancement).append("'),\n");
            }
        }
        var query = "INSERT INTO advancement (minecraftId) VALUES" + sb;
        statement.execute(query);
    }

    private void insertEntity(Statement statement) throws SQLException {
        var sb = new StringBuilder("\n");
        var array = Configurator.getInstance().earnData.entityRewards.keySet().toArray(String[]::new);
        for (int i = 0; i < array.length; i++) {
            var entity = array[i];
            if (i == array.length - 1) {
                sb.append("('").append(entity).append("');");
            } else {
                sb.append("('").append(entity).append("'),\n");
            }
        }
        var query = "INSERT INTO entity (translationKey) VALUES" + sb;
        statement.execute(query);
    }

    private void insertItem(Statement statement) throws SQLException {
        var sb = new StringBuilder("\n");
        var array = Configurator.getInstance().earnData.blockMinedRewards.keySet().toArray(String[]::new);
        for (int i = 0; i < array.length; i++) {
            var item = array[i];
            if (i == array.length - 1) {
                sb.append("('").append(item).append("');");
            } else {
                sb.append("('").append(item).append("'),\n");
            }
        }
        var query = "INSERT INTO item (translationKey) VALUES" + sb;
        statement.execute(query);
    }

    private void insertAdvancementRewards(Statement statement) {
        Configurator.getInstance().earnData.advancementRewards.forEach((minecraftId, amount) -> {
            var query = "INSERT INTO advancementReward (amount, advancement_id) VALUES (%s, (select id from advancement where advancement.minecraftId like '%s'));";
            var formattedQuery = query.formatted(String.valueOf(amount).replace(",", "."), minecraftId);
            try {
                statement.execute(formattedQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void insertEntityRewards(Statement statement) {
        Configurator.getInstance().earnData.entityRewards.forEach((translationKey, amount) -> {
            var query = "INSERT INTO entityReward (amount, entity_id) VALUES (%s, (select id from entity where entity.translationKey like '%s'));";
            var formattedQuery = query.formatted(String.valueOf(amount).replace(",", "."), translationKey);
            try {
                statement.execute(formattedQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void insertBlockMinedReward(Statement statement) {
        Configurator.getInstance().earnData.blockMinedRewards.forEach((translationKey, amount) -> {
            var query = "INSERT INTO blockMinedReward (amount, item_id) VALUES (%s, (select id from item where item.translationKey like '%s'));";
            var formattedQuery = query.formatted(String.valueOf(amount).replace(",", "."), translationKey);
            try {
                statement.execute(formattedQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void advancementDoneTransaction(String playerUUID, String advancementPath, double price) {
        var query = """
                insert into advancementDoneTransaction (price, player_id, advancement_id)
                values (%s, (select id from player where player.uuid like '%s'), (select id from advancement where advancement.minecraftId like '%s'));""";
        var priceStr = String.valueOf(price).replace(",", ".");
        var formattedQuery = query.formatted(priceStr, playerUUID, advancementPath);
        DbUtils.executeQuery(url, formattedQuery);
    }

    public void entityKilledTransaction(String playerUUID, String translationKey, double price) {
        var query = """
                insert into entityKilledTransaction (price, player_id, entity_id)
                values (%s,  (select id from player where player.uuid like '%s'),  (select id from entity where entity.translationKey like '%s'));""";

        var priceStr = String.valueOf(price).replace(",", ".");
        var formattedQuery = query.formatted(priceStr, playerUUID, translationKey);
        DbUtils.executeQuery(url, formattedQuery);
    }

    public void blockMinedTransaction(String playerUUID, String translationKey, double price) {
        var query = """
                insert into blockMinedTransaction (price, player_id, item_id)
                values (%s, (select id from player where player.uuid like '%s'),  (select id from item where item.translationKey like '%s'));""";
        var priceStr = String.valueOf(price).replace(",", ".");
        var formattedQuery = query.formatted(priceStr, playerUUID, translationKey);
        DbUtils.executeQuery(url, formattedQuery);
    }

    public void shopTransaction(String buyerUUID, String sellerUUID, String translationKey, double price, int itemAmount) {
        var query = """
                insert into shopTransaction (buyerUUID, sellerUUID, price, item_id, itemAmount)
                values ((select id from player where player.uuid like '%s'),
                        (select id from player where player.uuid like '%s'),
                        '%s',
                        '%s',
                        (select id from item where item.translationKey like '%s'));""";
        var priceStr = String.valueOf(price).replace(",", ".");
        var itemAmountStr = String.valueOf(itemAmount).replace(",", ".");
        var formattedQuery = query.formatted(buyerUUID, sellerUUID, priceStr, itemAmountStr, translationKey);
        DbUtils.executeQuery(url, formattedQuery);
    }

    public List<String> getAdvancementDone(String uuid) {
        return DbUtils.executeQuery(url, statement -> {
            var query = """
                    select a.minecraftId from advancementDoneTransaction
                        inner join advancement a on a.id = advancementDoneTransaction.advancement_id
                        inner join player p on p.id = advancementDoneTransaction.player_id where p.uuid like '%s';""".formatted(uuid);
            try {
                var resultSet = statement.executeQuery(query);
                var list = new ArrayList<String>();
                while (resultSet.next()) {
                    var path = resultSet.getString("minecraftId");
                    list.add(path);
                }
                return list;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }, Collections.emptyList());
    }

    private void addPlayer(PlayerEntity player) {
        DbUtils.executeQuery(url, statement -> {
            var sql = "INSERT INTO player (uuid, name, monney) VALUES ('" + player.getUuidAsString() + "', '" + player.getEntityName() + "', '0')";
            try {
                statement.execute(sql);
            } catch (SQLException ignored) {
            } // Every time the player will join, the database will try to insert it, but player is unique on database
        });
    }

    public boolean isPlayerInDatabase(String uuid) {
        return DbUtils.executeQuery(url, statement -> {
            var query = "select id from player where uuid like '%s'";
            try {
                var resultSet = statement.executeQuery(query.formatted(uuid));
                return resultSet.getInt("id") != 0; // return zero if null
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }, false);
    }

    public boolean deposit(String uuid, double amount) {
        return DbUtils.<Boolean>executeQuery(url, statement -> {
            try {
                var monney = getBalance(uuid);
                var updateMonneyQuery = "UPDATE player SET monney = %s WHERE uuid like '%s';".formatted("%.5f".formatted(monney + amount).replace(',', '.'), uuid);
                statement.execute(updateMonneyQuery);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }, false);
    }

    public Economy.Result withdraw(String uuid, double amount) {
        return DbUtils.executeQuery(url, statement -> {
            try {
                var monney = getBalance(uuid);
                if (monney < amount) return Economy.Result.NO_ENOUGH_MONNEY;
                var updateMonneyQuery = "UPDATE player SET monney = %s WHERE uuid like '%s';".formatted("%.5f".formatted(monney - amount).replace(',', '.'), uuid);
                statement.execute(updateMonneyQuery);
                return Economy.Result.SUCCESS;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Economy.Result.IOEXCEPTION;
        }, Economy.Result.IOEXCEPTION);
    }

    public Float getBalance(String uuid) {
        return DbUtils.<Float>executeQuery(url, statement -> {
            try {
                var getMonneyQuery = "SELECT monney FROM player where uuid like '%s';".formatted(uuid);
                var resultSet = statement.executeQuery(getMonneyQuery);
                return resultSet.getFloat("monney");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1f;
        }, -1f);
    }

}
