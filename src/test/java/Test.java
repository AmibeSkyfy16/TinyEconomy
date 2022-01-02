import ch.skyfy.tinyeconomy.database.DbUtils;
import net.minecraft.entity.mob.HostileEntity;
import org.reflections.Reflections;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;

@SuppressWarnings("NewClassNamingConvention")
public class Test {

    private static final String url = "jdbc:sqlite:" + "X:\\Tech\\Projects\\MC\\MTEA\\Projects\\FabricMods\\TinyEconomy\\run\\config\\TinyEconomy\\tinyeconomy.db";
//    private static final String url = "jdbc:sqlite:" + "C:\\Users\\colin\\Desktop\\transaction.db";

    public void getInfo1(){
        //        var sb = new StringBuilder();
//        var map = new HashMap<String, String>();
//        for (var declaredField : Blocks.class.getDeclaredFields()) {
//            try {
//                var block = (Block)declaredField.get(null);
//                map.computeIfAbsent(block.getTranslationKey(), k -> "put(\""+k+"\", 0);\n");
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        map.values().forEach(sb::append);
//        System.out.println(sb);
    }

    public void getInfo2(){
        //        var sb = new StringBuilder();
//        var map = new HashMap<String, String>();
//        for (var declaredField : EntityType.class.getDeclaredFields()) {
//            declaredField.setAccessible(true);
//            try {
//                var block = (EntityType) declaredField.get(null);
//                map.computeIfAbsent(block.getTranslationKey(), k -> "put(\"" + k + "\", 0);\n");
//            } catch (IllegalAccessException | ClassCastException | NullPointerException e) {
////                e.printStackTrace();
//            }
//        }
//        map.values().forEach(sb::append);
//        System.out.println(sb);
    }

//    @org.junit.jupiter.api.Test
//    public void test2() {
//        if(0 == 0)return;
//        DbUtils.<List<String>>executeQuery(url, statement -> {
//            var query = """
//                    select a.path from advancementDoneTransaction
//                        inner join advancement a on a.id = advancementDoneTransaction.advancement_id
//                        inner join player p on p.id = advancementDoneTransaction.player_id where p.uuid like '%s';""".formatted("ebb5c153-3f6f-4fb6-9062-20ac564e7490");
//            try {
//                var resultSet = statement.executeQuery(query);
//                var list = new ArrayList<String>();
//                while (resultSet.next()) {
//                    var path = resultSet.getString("path");
//                    list.add(path);
//                }
//
//
//                System.out.println();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//
//            return Collections.<String>emptyList();
//        }, Collections.<String>emptyList());
//
////        var r = new String("%.4f").formatted(19.12121 + 21.99).replace(',', '.');
////        System.out.println();
//    }

//    @org.junit.jupiter.api.Test
//    public void test() throws IOException {
//        if (0 == 0) return;
//        var str = """
//                ('nether/distract_piglin', 'minecraft'),
//                ('story/obtain_armor', 'minecraft'),
//                ('adventure/very_very_frightening', 'minecraft'),
//                ('story/lava_bucket', 'minecraft'),
//                ('husbandry/ride_a_boat_with_a_goat', 'minecraft'),
//                ('end/kill_dragon', 'minecraft'),
//                ('adventure/lightning_rod_with_villager_no_fire', 'minecraft'),
//                ('nether/all_potions', 'minecraft'),
//                ('husbandry/tame_an_animal', 'minecraft'),
//                ('husbandry/make_a_sign_glow', 'minecraft'),
//                ('nether/create_beacon', 'minecraft'),
//                ('story/deflect_arrow', 'minecraft'),
//                ('story/iron_tools', 'minecraft'),
//                ('nether/brew_potion', 'minecraft'),
//                ('end/dragon_egg', 'minecraft'),
//                ('husbandry/fishy_business', 'minecraft'),
//                ('nether/explore_nether', 'minecraft'),
//                ('adventure/fall_from_world_height', 'minecraft'),
//                ('nether/ride_strider', 'minecraft'),
//                ('adventure/sniper_duel', 'minecraft'),
//                ('nether/root', 'minecraft'),
//                ('end/levitate', 'minecraft'),
//                ('nether/all_effects', 'minecraft'),
//                ('adventure/bullseye', 'minecraft'),
//                ('nether/get_wither_skull', 'minecraft'),
//                ('husbandry/bred_all_animals', 'minecraft'),
//                ('story/mine_stone', 'minecraft'),
//                ('adventure/two_birds_one_arrow', 'minecraft'),
//                ('story/enter_the_nether', 'minecraft'),
//                ('adventure/whos_the_pillager_now', 'minecraft'),
//                ('story/upgrade_tools', 'minecraft'),
//                ('adventure/walk_on_powder_snow_with_leather_boots', 'minecraft'),
//                ('husbandry/tactical_fishing', 'minecraft'),
//                ('story/cure_zombie_villager', 'minecraft'),
//                ('end/find_end_city', 'minecraft'),
//                ('story/form_obsidian', 'minecraft'),
//                ('end/enter_end_gateway', 'minecraft'),
//                ('nether/obtain_blaze_rod', 'minecraft'),
//                ('nether/loot_bastion', 'minecraft'),
//                ('adventure/shoot_arrow', 'minecraft'),
//                ('husbandry/silk_touch_nest', 'minecraft'),
//                ('adventure/arbalistic', 'minecraft'),
//                ('end/respawn_dragon', 'minecraft'),
//                ('story/smelt_iron', 'minecraft'),
//                ('nether/charge_respawn_anchor', 'minecraft'),
//                ('story/shiny_gear', 'minecraft'),
//                ('end/elytra', 'minecraft'),
//                ('husbandry/wax_off', 'minecraft'),
//                ('adventure/summon_iron_golem', 'minecraft'),
//                ('nether/return_to_sender', 'minecraft'),
//                ('adventure/sleep_in_bed', 'minecraft'),
//                ('end/dragon_breath', 'minecraft'),
//                ('adventure/root', 'minecraft'),
//                ('adventure/kill_all_mobs', 'minecraft'),
//                ('story/enchant_item', 'minecraft'),
//                ('adventure/voluntary_exile', 'minecraft'),
//                ('story/follow_ender_eye', 'minecraft'),
//                ('end/root', 'minecraft'),
//                ('adventure/spyglass_at_parrot', 'minecraft'),
//                ('husbandry/obtain_netherite_hoe', 'minecraft'),
//                ('adventure/totem_of_undying', 'minecraft'),
//                ('adventure/kill_a_mob', 'minecraft'),
//                ('adventure/adventuring_time', 'minecraft'),
//                ('husbandry/plant_seed', 'minecraft'),
//                ('nether/find_bastion', 'minecraft'),
//                ('husbandry/axolotl_in_a_bucket', 'minecraft'),
//                ('adventure/spyglass_at_dragon', 'minecraft'),
//                ('nether/ride_strider_in_overworld_lava', 'minecraft'),
//                ('adventure/trade_at_world_height', 'minecraft'),
//                ('husbandry/wax_on', 'minecraft'),
//                ('adventure/play_jukebox_in_meadows', 'minecraft'),
//                ('adventure/hero_of_the_village', 'minecraft'),
//                ('nether/obtain_ancient_debris', 'minecraft'),
//                ('nether/create_full_beacon', 'minecraft'),
//                ('nether/summon_wither', 'minecraft'),
//                ('husbandry/balanced_diet', 'minecraft'),
//                ('nether/fast_travel', 'minecraft'),
//                ('husbandry/root', 'minecraft'),
//                ('nether/use_lodestone', 'minecraft'),
//                ('husbandry/safely_harvest_honey', 'minecraft'),
//                ('adventure/trade', 'minecraft'),
//                ('adventure/spyglass_at_ghast', 'minecraft'),
//                ('nether/uneasy_alliance', 'minecraft'),
//                ('story/mine_diamond', 'minecraft'),
//                ('husbandry/kill_axolotl_target', 'minecraft'),
//                ('nether/find_fortress', 'minecraft'),
//                ('adventure/throw_trident', 'minecraft'),
//                ('story/root', 'minecraft'),
//                ('adventure/honey_block_slide', 'minecraft'),
//                ('adventure/ol_betsy', 'minecraft'),
//                ('nether/netherite_armor', 'minecraft'),
//                ('story/enter_the_end', 'minecraft'),
//                ('husbandry/breed_an_animal', 'minecraft'),
//                ('husbandry/complete_catalogue', 'minecraft'),""";
//
//        var args = str.split("\n");
//        var list = Arrays.stream(args).sorted((o1, o2) -> {
//            if (o1.contains("husbandry") && !o2.contains("husbandry"))
//                return 1;
//            else
//                return -1;
//        }).sorted((o1, o2) -> {
//            if (o1.contains("story") && !o2.contains("story"))
//                return 1;
//            else
//                return -1;
//        }).sorted((o1, o2) -> {
//            if (o1.contains("adventure") && !o2.contains("adventure"))
//                return 1;
//            else
//                return -1;
//        }).sorted((o1, o2) -> {
//            if (o1.contains("end") && !o2.contains("end"))
//                return 1;
//            else
//                return -1;
//        }).sorted((o1, o2) -> {
//            if (o1.contains("nether") && !o2.contains("nether"))
//                return 1;
//            else
//                return -1;
//        }).toList();
//
//        StringBuilder sb = new StringBuilder("INSERT INTO advancement (path, namespace) VALUES");
//        for (String s : list) {
//            sb.append(s).append("\n");
//        }
//        System.out.println(sb);
//
//        if (0 == 0) return;
//
//        Reflections reflections = new Reflections("net.minecraft.entity");
//        Set<Class<?>> subTypes =
//                reflections.get(SubTypes.of(HostileEntity.class).asClass());
//        for (Class<?> subType : subTypes) {
//            System.out.println("Type: " + subType.getName());
//        }
//    }
}
