package de.eldoria.bloodnight.bloodmob.serialization.container;

public class MobEditorPayload {
    private SettingsContainer settingsContainer = null;

    public MobEditorPayload() {
    }

    public MobEditorPayload(SettingsContainer settingsContainer) {
        this.settingsContainer = settingsContainer;
    }

    public SettingsContainer settingsContainer() {
        return settingsContainer;
    }
}
