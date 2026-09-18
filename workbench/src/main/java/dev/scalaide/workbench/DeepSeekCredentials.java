package dev.scalaide.workbench;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;

/** All methods are blocking and called on background threads, never during action update. */
final class DeepSeekCredentials {
    private static final CredentialAttributes ATTRIBUTES = new CredentialAttributes(
            CredentialAttributesKt.generateServiceName("Scala Learning", "DeepSeek API key"));
    private DeepSeekCredentials() {}
    static String load() {
        String saved = PasswordSafe.getInstance().getPassword(ATTRIBUTES);
        return saved != null && !saved.isBlank() ? saved : System.getenv("DEEPSEEK_API_KEY");
    }
    static void save(String key) { PasswordSafe.getInstance().setPassword(ATTRIBUTES, key); }
}
