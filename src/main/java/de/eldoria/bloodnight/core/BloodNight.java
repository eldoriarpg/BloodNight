package de.eldoria.bloodnight.core;

import de.eldoria.bloodnight.command.BloodNightCommand;
import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.config.BossBarSettings;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.Drop;
import de.eldoria.bloodnight.config.GeneralSettings;
import de.eldoria.bloodnight.config.MobSetting;
import de.eldoria.bloodnight.config.MobSettings;
import de.eldoria.bloodnight.config.NightSelection;
import de.eldoria.bloodnight.config.NightSettings;
import de.eldoria.bloodnight.config.WorldSettings;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.bloodnight.core.manager.NotificationManager;
import de.eldoria.bloodnight.core.manager.MobManager;
import de.eldoria.bloodnight.core.manager.NightManager;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.EnderCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.FlyingCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.NervousPoweredCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.SpeedCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.ToxicCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.creeper.UnstableCreeper;
import de.eldoria.bloodnight.specialmobs.mobs.enderman.FearfulEnderman;
import de.eldoria.bloodnight.specialmobs.mobs.enderman.ToxicEnderman;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.FearfulPhantom;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.FirePhantom;
import de.eldoria.bloodnight.specialmobs.mobs.phantom.PhantomSoul;
import de.eldoria.bloodnight.specialmobs.mobs.rider.BlazeRider;
import de.eldoria.bloodnight.specialmobs.mobs.rider.SpeedSkeletonRider;
import de.eldoria.bloodnight.specialmobs.mobs.rider.WitherSkeletonRider;
import de.eldoria.bloodnight.specialmobs.mobs.skeleton.InvisibleSkeleton;
import de.eldoria.bloodnight.specialmobs.mobs.skeleton.MagicSkeleton;
import de.eldoria.bloodnight.specialmobs.mobs.slime.ToxicSlime;
import de.eldoria.bloodnight.specialmobs.mobs.witch.FireWizard;
import de.eldoria.bloodnight.specialmobs.mobs.witch.ThunderWizard;
import de.eldoria.bloodnight.specialmobs.mobs.witch.WitherWizard;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.ArmoredZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.InvisibleZombie;
import de.eldoria.bloodnight.specialmobs.mobs.zombie.SpeedZombie;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Logger;

public class BloodNight extends JavaPlugin {

    @Getter
    private static BloodNight instance;
    private static Logger logger;
    private NightManager nightManager;
    private MobManager mobManager;
    private Localizer localizer;
    private Configuration configuration;
    private InventoryListener inventoryListener;

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @NotNull
    public static Logger logger() {
        return logger;
    }

    public static NamespacedKey getNamespacedKey(String string) {
        return new NamespacedKey(instance, string);
    }

    private boolean initialized = false;

    public static Localizer localizer() {
        return instance.localizer;
    }


    @Override
    public void onEnable() {
        if (!initialized) {
            instance = this;
            logger = getLogger();
            registerSerialization();
            registerMobs();
            configuration = new Configuration(this);

            localizer = new Localizer(this, configuration.getGeneralSettings().getLanguage(), "messages",
                    "messages", Locale.US, "de_DE", "en_US");
            MessageSender.create(this, "§4[BN] ", '2', 'c');
            registerListener();
            registerCommand("bloodnight",
                    new BloodNightCommand(configuration, localizer, this, nightManager, mobManager, inventoryListener));
        }

        onReload();

        if (initialized) {
            logger().info("BloodNight reloaded!");
        } else {
            logger().info("BloodNight enabled!");
            initialized = true;
        }
    }

    public void onReload() {
        localizer.setLocale(configuration.getGeneralSettings().getLanguage());
        nightManager.reload();
    }

    private void registerListener() {
        PluginManager pm = Bukkit.getPluginManager();

        MessageSender messageSender = MessageSender.get(this);
        nightManager = new NightManager(configuration);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, nightManager, 100, 5);
        pm.registerEvents(new NotificationManager(nightManager, messageSender), this);
        pm.registerEvents(nightManager, this);
        mobManager = new MobManager(nightManager, configuration);
        inventoryListener = new InventoryListener(configuration);
        pm.registerEvents(inventoryListener, this);
        pm.registerEvents(mobManager, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mobManager, 100, 5);
    }

    private void registerSerialization() {
        ConfigurationSerialization.registerClass(GeneralSettings.class);
        ConfigurationSerialization.registerClass(NightSelection.class);
        ConfigurationSerialization.registerClass(NightSettings.class);
        ConfigurationSerialization.registerClass(MobSettings.class);
        ConfigurationSerialization.registerClass(MobSetting.class);
        ConfigurationSerialization.registerClass(WorldSettings.class);
        ConfigurationSerialization.registerClass(Drop.class);
        ConfigurationSerialization.registerClass(BossBarSettings.class);
    }

    @Override
    public void onDisable() {
        if (nightManager != null) {
            nightManager.shutdown();
        }
        logger().info("Blood Night disabled!");
    }

    private void registerCommand(String command, TabExecutor executor) {
        PluginCommand cmd = getCommand(command);
        if (cmd != null) {
            cmd.setExecutor(executor);
            return;
        }
        logger().warning("Command " + command + " not found!");
    }

    private void registerMobs() {
        // Creeper
        SpecialMobRegistry.registerMob(EntityType.CREEPER, "Ender Creeper", e -> new EnderCreeper((Creeper) e));
        SpecialMobRegistry.registerMob(EntityType.CREEPER, "Flying Creeper", e -> new FlyingCreeper((Creeper) e));
        SpecialMobRegistry.registerMob(EntityType.CREEPER, "Nervous Powered Creeper", e -> new NervousPoweredCreeper((Creeper) e));
        SpecialMobRegistry.registerMob(EntityType.CREEPER, "Speed Creeper", e -> new SpeedCreeper((Creeper) e));
        SpecialMobRegistry.registerMob(EntityType.CREEPER, "Toxic Creeper", e -> new ToxicCreeper((Creeper) e));
        SpecialMobRegistry.registerMob(EntityType.CREEPER, "Unstable Creeper", e -> new UnstableCreeper((Creeper) e));

        // Enderman
        SpecialMobRegistry.registerMob(EntityType.ENDERMAN, "Fearful Enderman", e -> new FearfulEnderman((Enderman) e));
        SpecialMobRegistry.registerMob(EntityType.ENDERMAN, "Toxic Enderman", e -> new ToxicEnderman((Enderman) e));

        // Phantom
        SpecialMobRegistry.registerMob(EntityType.PHANTOM, "Fearful Phantom", e -> new FearfulPhantom((Phantom) e));
        SpecialMobRegistry.registerMob(EntityType.PHANTOM, "Fire Phantom", e -> new FirePhantom((Phantom) e));
        SpecialMobRegistry.registerMob(EntityType.PHANTOM, "Phantom Soul", e -> new PhantomSoul((Phantom) e));

        // Rider
        SpecialMobRegistry.registerMob(EntityType.SPIDER, "Blaze Rider", e -> new BlazeRider((Spider) e));
        SpecialMobRegistry.registerMob(EntityType.SPIDER, "Speed Skeleton Rider", e -> new SpeedSkeletonRider((Spider) e));
        SpecialMobRegistry.registerMob(EntityType.SPIDER, "Wither Skeleton Rider", e -> new WitherSkeletonRider((Spider) e));

        // Skeleton
        SpecialMobRegistry.registerMob(EntityType.SKELETON, "Invisible Skeleton", e -> new InvisibleSkeleton((Skeleton) e));
        SpecialMobRegistry.registerMob(EntityType.SKELETON, "Magic Skeleton", e -> new MagicSkeleton((Skeleton) e));

        // Slime
        SpecialMobRegistry.registerMob(EntityType.SLIME, "Toxic Slime", e -> new ToxicSlime((Slime) e));

        // Witch
        SpecialMobRegistry.registerMob(EntityType.WITCH, "Fire Wizard", e -> new FireWizard((Witch) e));
        SpecialMobRegistry.registerMob(EntityType.WITCH, "Thunder Wizard", e -> new ThunderWizard((Witch) e));
        SpecialMobRegistry.registerMob(EntityType.WITCH, "Wither Wizard", e -> new WitherWizard((Witch) e));

        // Zombie
        SpecialMobRegistry.registerMob(EntityType.ZOMBIE, "Armored Zombie", e -> new ArmoredZombie((Zombie) e));
        SpecialMobRegistry.registerMob(EntityType.ZOMBIE, "Invisible Zombie", e -> new InvisibleZombie((Zombie) e));
        SpecialMobRegistry.registerMob(EntityType.ZOMBIE, "Speed Zombie", e -> new SpeedZombie((Zombie) e));
    }
}
