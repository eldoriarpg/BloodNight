package de.eldoria.bloodnight.core;

import de.eldoria.bloodnight.api.IBloodNightAPI;
import de.eldoria.bloodnight.command.BloodNightCommand;
import de.eldoria.bloodnight.command.InventoryListener;
import de.eldoria.bloodnight.config.Configuration;
import de.eldoria.bloodnight.config.generalsettings.GeneralSettings;
import de.eldoria.bloodnight.config.worldsettings.BossBarSettings;
import de.eldoria.bloodnight.config.worldsettings.NightSelection;
import de.eldoria.bloodnight.config.worldsettings.NightSettings;
import de.eldoria.bloodnight.config.worldsettings.WorldSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.DeathActionSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.MobDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PlayerDeathActions;
import de.eldoria.bloodnight.config.worldsettings.deathactions.PotionEffectSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.LightningSettings;
import de.eldoria.bloodnight.config.worldsettings.deathactions.subsettings.ShockwaveSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.Drop;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSetting;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.MobSettings;
import de.eldoria.bloodnight.config.worldsettings.mobsettings.VanillaMobSettings;
import de.eldoria.bloodnight.config.worldsettings.sound.SoundEntry;
import de.eldoria.bloodnight.config.worldsettings.sound.SoundSettings;
import de.eldoria.bloodnight.core.api.BloodNightAPI;
import de.eldoria.bloodnight.core.manager.NotificationManager;
import de.eldoria.bloodnight.core.manager.mobmanager.MobManager;
import de.eldoria.bloodnight.core.manager.nightmanager.CommandBlocker;
import de.eldoria.bloodnight.core.manager.nightmanager.NightManager;
import de.eldoria.bloodnight.core.mobfactory.MobFactory;
import de.eldoria.bloodnight.core.mobfactory.SpecialMobRegistry;
import de.eldoria.bloodnight.hooks.HookService;
import de.eldoria.bloodnight.util.Permissions;
import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Localizer;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.metrics.EldoMetrics;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import de.eldoria.eldoutilities.updater.Updater;
import de.eldoria.eldoutilities.updater.lynaupdater.LynaUpdateData;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bstats.charts.MultiLineChart;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BloodNight extends EldoPlugin {

    @Getter
    private static BloodNight instance;
    private NightManager nightManager;
    private MobManager mobManager;
    private Configuration configuration;
    private InventoryListener inventoryListener;
    private boolean initialized = false;
    private BloodNightAPI bloodNightAPI;
    private HookService hookService;

    public static NamespacedKey getNamespacedKey(String string) {
        return new NamespacedKey(instance, string.replace(" ", "_"));
    }

    public static Logger logger() {
        return getInstance().getLogger();
    }

    @Override
    public Level getLogLevel() {
        return null;
    }

    public static IBloodNightAPI getBloodNightAPI() {
        return instance.bloodNightAPI;
    }

    @Override
    public void onPluginEnable(boolean reload) {
        if (!initialized) {
            instance = this;
            setLoggerLevel();

            configuration = new Configuration(this);

            ILocalizer localizer = Localizer.create(this, "de_DE", "en_US", "es_ES", "tr", "zh_CN");

            Map<String, String> mobLocaleCodes = SpecialMobRegistry.getRegisteredMobs().stream()
                    .map(MobFactory::getMobName)
                    .collect(Collectors.toMap(
                            k -> "mob." + k,
                            k -> String.join(" ", k.split("(?<=.)(?=\\p{Lu})"))));
            localizer.addLocaleCodes(mobLocaleCodes);

            localizer.setLocale(configuration.getGeneralSettings().getLanguage());
            MessageSender.builder(this)
                    .prefix(configuration.getGeneralSettings().getPrefix())
                    .messageColor(NamedTextColor.GREEN)
                    .errorColor(NamedTextColor.RED)
                    .addTag(t ->{
                        t.tag("field", Tag.styling(NamedTextColor.AQUA));
                        t.tag("value", Tag.styling(NamedTextColor.GOLD));
                        t.tag("add", Tag.styling(NamedTextColor.DARK_GREEN));
                        t.tag("inactive", Tag.styling(NamedTextColor.GRAY));
                        t.tag("change", Tag.styling(NamedTextColor.YELLOW));
                        t.tag("weight", Tag.styling(NamedTextColor.GOLD));
                        t.tag("delete", Tag.styling(NamedTextColor.RED));
                        t.tag("header", Tag.styling(c -> c.decorate(TextDecoration.BOLD).color(NamedTextColor.GOLD)));
                    })
                    .register();

            registerListener();
            bloodNightAPI = new BloodNightAPI(nightManager, configuration);
            registerCommand("bloodnight",
                    new BloodNightCommand(configuration, this, nightManager, mobManager, inventoryListener));

            enableMetrics();

            if (configuration.getGeneralSettings().isUpdateReminder()) {
                Updater.lyna(LynaUpdateData.builder(this, 4).notifyPermission(Permissions.Admin.RELOAD).notifyUpdate(true).build()).start();
            }


            hookService = new HookService(this, configuration, nightManager);
            hookService.setup();

            lateInit();
        }

        onReload();

        if (initialized) {
            logger().info("BloodNight reloaded!");
        } else {
            logger().info("BloodNight enabled!");
            initialized = true;
        }

        IBloodNightAPI bloodNightAPI = BloodNight.getBloodNightAPI();
    }

    public void onReload() {
        configuration.reload();
        ILocalizer.getPluginLocalizer(this).setLocale(configuration.getGeneralSettings().getLanguage());

        logger().config("§cDebug mode active");

        nightManager.reload();
    }

    private void lateInit() {
        registerListener(new NotificationManager(configuration, nightManager, hookService));
    }

    private void registerListener() {
        nightManager = new NightManager(this, configuration);
        nightManager.runTaskTimer(this, 5, 1);
        mobManager = new MobManager(nightManager, configuration);
        inventoryListener = new InventoryListener(configuration);
        CommandBlocker commandBlocker = new CommandBlocker(nightManager, configuration);
        registerListener(commandBlocker, inventoryListener, mobManager, nightManager);
    }

    @Override
    public List<Class<? extends ConfigurationSerializable>> getConfigSerialization() {
        return Arrays.asList(GeneralSettings.class, NightSelection.class, NightSettings.class, MobSettings.class,
                MobSetting.class, VanillaMobSettings.class, WorldSettings.class, Drop.class, BossBarSettings.class,
                MobSettings.MobTypes.class, SoundSettings.class, SoundEntry.class, PotionEffectSettings.class,
                PlayerDeathActions.class, MobDeathActions.class, LightningSettings.class, ShockwaveSettings.class,
                DeathActionSettings.class);
    }

    private void enableMetrics() {
        EldoMetrics metrics = new EldoMetrics(this, 9123);
        if (metrics.isEnabled()) {
            logger().info("§2Metrics enabled. Thank you! (> ^_^ )>");

            metrics.addCustomChart(new MultiLineChart("update_settings", () -> {
                Map<String, Integer> map = new HashMap<>();
                map.put("Update Check", configuration.getGeneralSettings().isUpdateReminder() ? 1 : 0);
                if (configuration.getGeneralSettings().isUpdateReminder()) {
                    map.put("Auto Update", configuration.getGeneralSettings().isUpdateReminder() ? 1 : 0);
                    return map;
                }
                map.put("Auto Update", 0);
                return map;
            }));

            metrics.addCustomChart(new MultiLineChart("mob_types", () -> {
                Map<String, Integer> map = new HashMap<>();
                for (MobFactory factory : SpecialMobRegistry.getRegisteredMobs()) {
                    for (WorldSettings world : configuration.getWorldSettings().values()) {
                        if (!world.isEnabled()) continue;
                        Optional<MobSetting> mobByName = world.getMobSettings().getMobByName(factory.getMobName());
                        map.compute(mobByName.get().getMobName(), (key, value) -> {
                            if (value == null) {
                                return mobByName.get().isActive() ? 1 : 0;
                            }
                            return value + (mobByName.get().isActive() ? 1 : 0);
                        });
                    }
                }
                return map;
            }));

            metrics.addCustomChart(new MultiLineChart("night_selection", () -> {
                Map<String, Integer> map = new HashMap<>();
                for (WorldSettings world : configuration.getWorldSettings().values()) {
                    if (!world.isEnabled()) continue;
                    map.compute(world.getNightSelection().getNightSelectionType().toString(), (key, value) -> {
                        if (value == null) {
                            return 1;
                        }
                        return value + 1;
                    });
                }
                return map;
            }));

            return;
        }

        logger().info("§2Metrics are not enabled. Metrics help me to stay motivated. Please enable it.");
    }

    @Override
    public void onPluginDisable() {
        if (nightManager != null) {
            nightManager.shutdown();
        }
        if (hookService != null) {
            hookService.shutdown();
        }
        logger().info("Blood Night disabled!");
    }
}
