package de.eldoria.bloodnight.webservice.sessions;

import de.eldoria.bloodnight.bloodmob.serialization.container.MobEditorPayload;

public class MobEditorSession extends Session<MobEditorPayload> {
    public MobEditorSession(MobEditorPayload sessionData) {
        super(sessionData);
    }
}
