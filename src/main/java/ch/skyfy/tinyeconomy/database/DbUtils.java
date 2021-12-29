package ch.skyfy.tinyeconomy.database;

import ch.skyfy.tinyeconomy.TinyEconomy;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class DbUtils {

    public static void executeQuery(String url, String query) {
        try (var connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                connection.createStatement().execute(query);
            }
        } catch (SQLException e) {
//            System.out.println(e.getMessage());
        }
    }

    public static void executeQuery(String url, Consumer<Statement> consumer) {
        try (var connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                consumer.accept(connection.createStatement());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static <R> R executeQuery(String url, Function<Statement, R> function, R defaultReturnValue) {
        try (var connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                return function.apply(connection.createStatement());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return defaultReturnValue;
    }

    public static void updateRewards(String url, String getRewardQuery, String updateRewardQuery, String rewardAmount, String id, Map<String, Double>rewards) {
        if(id.contains("adva")){
            System.out.println();
        }
        executeQuery(url, statement -> {
            rewards.forEach((key, value) -> {
                var formattedGetRewardQuery = getRewardQuery.formatted(key);
                try {
                    var resultSet = statement.executeQuery(formattedGetRewardQuery);
                    var dbReward = resultSet.getFloat(rewardAmount);
                    var advancementId = resultSet.getInt(id);
                    var dbRewardAsString = "%.5f".formatted(value).replace(',', '.');
                    if (dbReward != value) {
                        var formattedupdateQuery = updateRewardQuery.formatted(dbRewardAsString, advancementId);
                        statement.execute(formattedupdateQuery);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    TinyEconomy.DISABLED.set(true);
                }
            });
        });
    }

}
