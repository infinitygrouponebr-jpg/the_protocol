package Infinitygroup.the_protocol.profession;

import Infinitygroup.the_protocol.config.CommonConfig;
import net.minecraft.server.level.ServerPlayer;

public final class ProfessionManager {
    private ProfessionManager() {
    }

    public static ProfessionData get(ServerPlayer player) {
        return player.getData(PlayerProfessionProvider.PROFESSION_DATA);
    }

    public static void setProfession(ServerPlayer player, ProfessionType profession) {
        player.setData(PlayerProfessionProvider.PROFESSION_DATA, new ProfessionData(profession, 0, 0, 0));
    }

    public static void addExperience(ServerPlayer player, int amount) {
        if (amount <= 0 || !CommonConfig.ENABLE_PROFESSION_SYSTEM.get()) {
            return;
        }

        ProfessionData data = get(player);
        if (data.profession() == ProfessionType.NONE) {
            return;
        }

        int level = data.level();
        int experience = data.experience() + amount;
        int perkPoints = data.perkPoints();
        while (level < CommonConfig.MAX_PROFESSION_LEVEL.get() && experience >= experienceRequiredForNextLevel(level)) {
            experience -= experienceRequiredForNextLevel(level);
            level++;
            perkPoints++;
        }
        player.setData(PlayerProfessionProvider.PROFESSION_DATA, new ProfessionData(data.profession(), level, experience, perkPoints));
    }

    public static void reset(ServerPlayer player) {
        player.setData(PlayerProfessionProvider.PROFESSION_DATA, ProfessionData.empty());
    }

    public static int experienceRequiredForNextLevel(int currentLevel) {
        return CommonConfig.XP_PER_LEVEL_BASE.get() * Math.max(1, currentLevel + 1);
    }
}
