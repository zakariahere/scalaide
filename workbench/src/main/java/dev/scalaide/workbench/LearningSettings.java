package dev.scalaide.workbench;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

/** Contains preferences only. The API key belongs exclusively in PasswordSafe. */
@Service(Service.Level.APP)
@State(name = "ScalaLearning", storages = @Storage(value = "scalaLearning.xml", roamingType = RoamingType.DISABLED))
public final class LearningSettings implements PersistentStateComponent<LearningSettings.Values> {
    public static final class Values {
        public String model = "deepseek-flash";
        public String language = "English";
        public boolean compareJava = true;
    }
    private Values state = new Values();
    static LearningSettings get() { return ApplicationManager.getApplication().getService(LearningSettings.class); }
    @Override public @NotNull Values getState() { return state; }
    @Override public void loadState(@NotNull Values state) { this.state = state; }
}
