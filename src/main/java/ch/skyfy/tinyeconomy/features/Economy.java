package ch.skyfy.tinyeconomy.features;

import ch.skyfy.tinyeconomy.database.Database;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class Economy {

    private static class EconomyHolder {
        public static final Economy INSTANCE = new Economy();
    }

    public static Economy getInstance() {
        return Economy.EconomyHolder.INSTANCE;
    }

    public enum Result {
        SUCCESS,
        IOEXCEPTION,
        NO_ENOUGH_MONNEY()
    }

    public Result deposit(String uuid, double amount) {
        if (Database.getInstance().deposit(uuid, amount)) {
            EconomyEvent.DEPOSIT.invoker().deposit(uuid, amount);
            return Result.SUCCESS;
        }
        return Result.IOEXCEPTION;
    }

    public Result withdraw(String uuid, double amount) {
        return Database.getInstance().withdraw(uuid, amount);
    }

    public Float getBalance(String uuid) {
        return Database.getInstance().getBalance(uuid);
    }

    public static class EconomyEvent {

        public static final Event<EconomyEvent.Deposit> DEPOSIT = EventFactory.createArrayBacked(EconomyEvent.Deposit.class, callbacks -> (uuid, amount) -> {
            for (EconomyEvent.Deposit callback : callbacks) {
                callback.deposit(uuid, amount);
            }
        });

        @FunctionalInterface
        public interface Deposit {
            void deposit(String uuid, double amount);
        }

    }

}
