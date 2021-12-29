package ch.skyfy.tinyeconomy.features.shop;

import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public record ShopResult(boolean isShop, boolean cancel, @Nullable BlockEntity blockEntity, String vendorName) { }
