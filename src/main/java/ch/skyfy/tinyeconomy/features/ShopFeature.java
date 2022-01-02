package ch.skyfy.tinyeconomy.features;

import ch.skyfy.tinyeconomy.database.Database;
import ch.skyfy.tinyeconomy.features.shop.ShopResult;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("UnusedReturnValue")
public class ShopFeature {
    private enum PlayerState {
        ONLINE,
        OFFLINE,
        NO_EXIST
    }

    private static class ShopFeatureHolder {
        public static final ShopFeature INSTANCE = new ShopFeature();
    }

    public static ShopFeature getInstance() {
        return ShopFeature.ShopFeatureHolder.INSTANCE;
    }

    public ShopFeature() {
        initialize();
    }

    public void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> useBlockCallback(player, world, hitResult));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> attackBlockCallback(player, world, pos));
    }

    /**
     * When player will righ click a block
     */
    public ActionResult useBlockCallback(PlayerEntity player, World world, BlockHitResult hitResult) {
        // Prevents a player from robbing a store with a hopper
        // There is a trick, player can steal with hopper, I'll leave this trick available for crafty players
        for (var itemStack : player.getItemsHand()) {
            if (itemStack.getItem().getTranslationKey().equals("block.minecraft.hopper")) {
                var shopResult = isAShop(new BlockPos(hitResult.getPos().x, hitResult.getPos().y + 1, hitResult.getPos().z), world, player, UseBlockCallback.class);
                if (shopResult.isShop() && !shopResult.vendorName().equals(player.getName().asString()))
                    return ActionResult.FAIL;
            }
        }
        var shopResult = isAShop(hitResult.getBlockPos(), world, player, UseBlockCallback.class);
        var blockEn = world.getBlockEntity(hitResult.getBlockPos());
        if (blockEn != null)
            if (blockEn.getType() == BlockEntityType.BARREL && shopResult.isShop() && !shopResult.vendorName().equals(player.getName().asString()))
                return ActionResult.CONSUME;
        return ActionResult.PASS;
    }

    /**
     * @param player The player who clicked
     * @param world  World in wich the player is
     * @param pos    Block clicked position
     * @return ActionResult.PASS or ActionResult.CONSUME if
     */
    public ActionResult attackBlockCallback(PlayerEntity player, World world, BlockPos pos) {
        var result = isAShop(pos, world, player, AttackBlockCallback.class);
        if (result.isShop() && result.cancel())
            return ActionResult.CONSUME;
        return ActionResult.PASS;
    }

    public <T> ShopResult isAShop(BlockPos blockPos, World world, @Nullable PlayerEntity buyer, @Nullable Class<T> tClass) {
        if (world.getServer() == null) return new ShopResult(false, false, null, "");

        var isBarrelNear = false;
        BarrelBlockEntity barrelBlockEntity = null;
        SignBlockEntity signBlockEntity = null;
        var signFirstClicked = false;

        // If player right or left clicked on the wall sign, we will call getBlockNear, to try to find a Barrel block entity
        if (world.getBlockState(blockPos).getBlock() instanceof WallSignBlock) {
            if (!(getBlockNear(blockPos, world, BarrelBlockEntity.class) instanceof BarrelBlockEntity barrelBlockEntityFound))
                return new ShopResult(false, false, null, "");
            isBarrelNear = true;
            signBlockEntity = (SignBlockEntity) world.getBlockEntity(blockPos);
            barrelBlockEntity = barrelBlockEntityFound;
            signFirstClicked = true;
        } else if (world.getBlockState(blockPos).getBlock() instanceof BarrelBlock) { // If player clicked on a barrel, we will call getBlockNear, to try to find a Wall Sign Entity
            if (!(getBlockNear(blockPos, world, SignBlockEntity.class) instanceof SignBlockEntity signBlockEntityFound))
                return new ShopResult(false, false, null, "");
            barrelBlockEntity = (BarrelBlockEntity) world.getBlockEntity(blockPos);
            signBlockEntity = signBlockEntityFound;
        }

        // If one of this two values are null, the player click on a sign or a barrel that is not a shop
        if (barrelBlockEntity == null || signBlockEntity == null) return new ShopResult(false, false, null, "");

        var args = signBlockEntity.getTextOnRow(2, false).asString().split(" ");
        int itemAmount, price;
        try {
            itemAmount = Integer.parseInt(args[0]);
            price = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            // If minecraft user didn't respect TinyEconomy shop convention, this is not a shop
            return new ShopResult(false, false, null, "");
        }

        // Check if vendor exist on the server
        var vendorName = signBlockEntity.getTextOnRow(0, false).asString();
        var optGP = world.getServer().getUserCache().findByName(vendorName);
        var vendor = world.getServer().getPlayerManager().getPlayer(vendorName);
        var state = getPlayerState(vendorName, world.getServer());

        // If vendor not exist, it's not a shop
        if (state == PlayerState.NO_EXIST) return new ShopResult(false, false, null, vendorName);

        // If tclass is not null, so tclass is either UseBlockCallback.class or AttackBlockCallback.class
        if (tClass != null && buyer != null) { // Dans le cas ou l'on cancel un joueur de voler, le buyer est null
            // A player You cannot buy from herself
            // If the player is different, but tried to break the block with left click, we cancel block breaking
            if (buyer.getName().asString().equals(vendorName) && tClass.equals(UseBlockCallback.class)) {
                buyer.sendMessage(Text.of("You can't buy from yourself!"), false);
                return new ShopResult(true, false, null, vendorName);
            } else if (!buyer.getName().asString().equals(vendorName) && tClass.equals(AttackBlockCallback.class)) {
                return new ShopResult(true, true, null, vendorName);
            }

            // Checking if inventory is valid
            // Reminder, only inventory with same item are valid (SURPRISE SHOP IS NOT A GOOD THING)
            if (!isBarrelInventoryValid(barrelBlockEntity)) return new ShopResult(false, false, null, vendorName);

            // If player right-clicked on wall sign we will call shopImpl to process the transaction
            if (tClass.equals(UseBlockCallback.class) && signFirstClicked)
                if (optGP.isPresent())
                    shopImpl(vendor, optGP.get().getId().toString(), vendorName, buyer, itemAmount, price, barrelBlockEntity);
                else System.out.println("UUID OF VENDOR PLAYER IS NOT AVAILABLE");
        }

        return new ShopResult(true, false, isBarrelNear ? signBlockEntity : barrelBlockEntity, vendorName);
    }

    private boolean isBarrelInventoryValid(BarrelBlockEntity barrelBlockEntity) {
        var translationKey = "";
        boolean once = false;
        for (var i = 0; i < barrelBlockEntity.size(); i++) {
            var it = barrelBlockEntity.getStack(i);
            if (!it.isEmpty()) {
                if (translationKey.isEmpty()) {
                    // we get the first non-empty item in the inventory
                    if (!once) {
                        translationKey = it.getTranslationKey();
                        once = true;
                    }
                    // if we found another item, different from the first one, this is not a shop
                    if (!it.getTranslationKey().equalsIgnoreCase(translationKey))
                        return false;
                }
            }
        }
        return true;
    }

    private void shopImpl(ServerPlayerEntity vendor, String vendorUUID, String vendorName, PlayerEntity buyer, int itemAmount, int price, BarrelBlockEntity barrelBlockEntity) {
        var economy = Economy.getInstance();

        var playerBalance = economy.getBalance(buyer.getUuidAsString());
        if (playerBalance == -1f) return;
        if (playerBalance - price < 0) {
            buyer.sendMessage(Text.of("You don't have enough money"), false);
            return;
        }

        // Count the numbers of item to sell
        int availableItemStack = 0;
        for (var i = 0; i < barrelBlockEntity.size(); i++) {
            var it = barrelBlockEntity.getStack(i);
            if (!it.isEmpty()) availableItemStack += it.getCount();
        }

        if (availableItemStack < itemAmount) {
            buyer.sendMessage(Text.of("There are not enough items in stock!"), false);
            return;
        }

        var transfer = new ArrayList<ItemStack>();

        var remainingPiece = itemAmount;
        for (int i = 0; i < barrelBlockEntity.size(); i++) {
            var originItemStack = barrelBlockEntity.getStack(i);
            if (!originItemStack.isEmpty()) {
                if (remainingPiece <= 0) break;
                var newIt = new ItemStack(originItemStack::getItem);
                if (originItemStack.getCount() - remainingPiece <= 0) {
                    barrelBlockEntity.setStack(i, ItemStack.EMPTY);
                } else {
                    newIt.setCount(remainingPiece);
                    originItemStack.setCount(originItemStack.getCount() - remainingPiece);
                }
                transfer.add(newIt);
                remainingPiece -= originItemStack.getCount();
            }
        }

        if (remainingPiece <= 0) {
            var result = economy.withdraw(buyer.getUuidAsString(), price);
            if (result == Economy.Result.SUCCESS) {
                var result2 = economy.deposit(vendorUUID, price);
                if (result2 == Economy.Result.SUCCESS) {
                    var args = transfer.get(0).getItem().getTranslationKey().split("\\.");
                    var itemName = args[args.length - 1];
                    Database.getInstance().shopTransaction(buyer.getUuidAsString(), vendorUUID, transfer.get(0).getItem().getTranslationKey(), price, itemAmount);
                    if (vendor != null)
                        vendor.sendMessage(Text.of("You sold for " + itemAmount + " of " + itemName + " to " + buyer.getName().asString()), false);
                    buyer.sendMessage(Text.of("You have bought for " + itemAmount + " of " + itemName + " to " + vendorName), false);
                    for (var itemStack : transfer) buyer.dropItem(itemStack, false);
                }
            }

        }
    }

    public <T> BlockEntity getBlockNear(BlockPos blockPos, World world, Class<T> tClass) {
        for (var entity : new ArrayList<>(Arrays.asList(
                world.getBlockEntity(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1)),
                world.getBlockEntity(new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ() - 1)),
                world.getBlockEntity(new BlockPos(blockPos.getX() - 1, blockPos.getY(), blockPos.getZ())),
                world.getBlockEntity(new BlockPos(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ()))
        )))
            if (entity != null && entity.getClass() == tClass) return entity;
        return null;
    }

    /**
     * Will return a player state that describe if a player is connected, not connected, or unknown on the server
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public PlayerState getPlayerState(String name, MinecraftServer server) {
        var opt = server.getUserCache().findByName(name);
        var uuid = opt.get().getId().toString();
        if (!Database.getInstance().isPlayerInDatabase(uuid)) return PlayerState.NO_EXIST;
        var player = server.getPlayerManager().getPlayer(name);
        if (player == null) return PlayerState.OFFLINE;
        return PlayerState.ONLINE;
    }

}
