import com.ironvault.model.ItemType;
import com.ironvault.model.VaultItem;
import com.ironvault.security.VaultManager;

import java.nio.file.Files;
import java.nio.file.Path;

public class VaultTests {
    public static void main(String[] args) throws Exception {
        Path vault = Path.of(args.length > 0 ? args[0] : "demo-vault.iron");
        char[] password = (args.length > 1 ? args[1] : "12345678").toCharArray();

        VaultManager manager = VaultManager.open(vault, password);
        int items = manager.data().items().size();
        long passwords = manager.data().count(ItemType.PASSWORD);
        long notes = manager.data().count(ItemType.NOTE);
        VaultItem first = manager.data().byType(ItemType.PASSWORD).get(0);
        System.out.println("items=" + items + " passwords=" + passwords + " notes=" + notes);
        System.out.println("first=" + first.title() + " logo=" + (first.logo() != null)
                + " logoBytes=" + (first.logo() == null ? 0 : first.logo().length));
        manager.save();
        manager.lock();

        VaultManager reopened = VaultManager.open(vault, password);
        System.out.println("reopened items=" + reopened.data().items().size());
        reopened.lock();

        try {
            VaultManager.open(vault, "wrong-password".toCharArray());
            System.out.println("wrong password: FAIL (opened)");
        } catch (VaultManager.InvalidPasswordException e) {
            System.out.println("wrong password: OK (" + e.getMessage() + ")");
        }

        Path tampered = Path.of("out", "tampered.iron");
        Files.createDirectories(tampered.toAbsolutePath().getParent());
        byte[] bytes = Files.readAllBytes(vault);
        bytes[bytes.length - 5] ^= 0x5A;
        Files.write(tampered, bytes);
        try {
            VaultManager.open(tampered, password);
            System.out.println("tampered file: FAIL (opened)");
        } catch (VaultManager.InvalidPasswordException e) {
            System.out.println("tampered file: OK (" + e.getMessage() + ")");
        }

        if (items != 9 || passwords != 6 || notes != 3) {
            throw new AssertionError("Unexpected demo vault contents");
        }
        if (first.logo() == null || first.logo().length == 0) {
            throw new AssertionError("Logo was not preserved");
        }
        System.out.println("ALL TESTS PASSED");
    }
}
