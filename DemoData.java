import com.ironvault.model.ItemType;
import com.ironvault.model.VaultItem;
import com.ironvault.security.VaultManager;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DemoData {
    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length > 0 ? args[0] : "demo-vault.iron");
        char[] master = (args.length > 1 ? args[1] : "12345678").toCharArray();
        if (java.nio.file.Files.exists(path)) {
            System.out.println("Vault already exists: " + path.toAbsolutePath());
            return;
        }
        VaultManager manager = VaultManager.create(path, master);
        Instant now = Instant.now();

        manager.data().add(password("Google", "alice@gmail.com", "Aurora!Sable-42",
                "https://google.com", "Основная почта. Двухфакторная аутентификация включена.",
                logo("G", new Color(0x8FA8C0), new Color(0x5E7892)), now.minus(2, ChronoUnit.HOURS)));
        manager.data().add(password("GitHub", "alex.dev", "R3factor!Tiger#88",
                "https://github.com", "Рабочий аккаунт. SSH-ключ добавлен на MacBook.",
                logo("GH", new Color(0x8B8B93), new Color(0x5C5C66)), now.minus(7, ChronoUnit.HOURS)));
        manager.data().add(password("Figma", "alex@studio.design", "Canvas&Grid!19",
                "https://figma.com", "Командный проект IronVault UI.",
                logo("F", new Color(0xB69A8E), new Color(0x8A6F66)), now.minus(1, ChronoUnit.DAYS)));
        manager.data().add(password("Notion", "alex@studio.design", "Orbit!Paper-77",
                "https://notion.so", "",
                logo("N", new Color(0x9A9A92), new Color(0x6E6E68)), now.minus(2, ChronoUnit.DAYS)));
        manager.data().add(password("Steam", "alex_plays", "Pixel!Quest-2024",
                "https://store.steampowered.com", "Библиотека на 214 игр.",
                logo("S", new Color(0x8397A6), new Color(0x5A6D7C)), now.minus(3, ChronoUnit.DAYS)));
        manager.data().add(password("Cloudflare", "admin@alex.dev", "Edge!Guard#51",
                "https://dash.cloudflare.com", "DNS для личных проектов.",
                logo("CF", new Color(0xB59E7E), new Color(0x8B7558)), now.minus(5, ChronoUnit.DAYS)));

        manager.data().add(note("Идеи для проекта",
                "— Собрать тёмный менеджер паролей на Java Swing.\n"
                        + "— Плавная выезжающая панель деталей.\n"
                        + "— Генератор паролей и индикатор надёжности.\n"
                        + "— Локальное шифрование AES-256-GCM.",
                logo("ID", new Color(0x9C8FA8), new Color(0x6F637B)), now.minus(3, ChronoUnit.HOURS)));
        manager.data().add(note("Коды восстановления",
                "Google: 4F2A-9C11-77B0\nGitHub: 8H3K-2M9Q-XW41\nFigma: 19PL-6RTZ-0042\n\nХранить только здесь.",
                logo("RC", new Color(0xA8948A), new Color(0x7A675F)), now.minus(1, ChronoUnit.DAYS)));
        manager.data().add(note("Wi-Fi дома",
                "Сеть: IronNest\nПароль: CopperLamp!509\nРоутер: 192.168.1.1\n\nГостевая сеть: IronNest_Guest",
                logo("WF", new Color(0x8AA39A), new Color(0x5E776E)), now.minus(6, ChronoUnit.DAYS)));

        manager.save();
        manager.lock();
        System.out.println("Created " + path.toAbsolutePath() + " with 9 items");
    }

    private static VaultItem password(String title, String username, String password, String website,
                                      String notes, byte[] logo, Instant updated) {
        VaultItem item = new VaultItem(ItemType.PASSWORD);
        item.setTitle(title);
        item.setUsername(username);
        item.setPassword(password);
        item.setWebsite(website);
        item.setNotes(notes);
        item.setLogo(logo);
        item.setCreatedAt(updated.minus(20, ChronoUnit.DAYS));
        item.setUpdatedAt(updated);
        return item;
    }

    private static VaultItem note(String title, String text, byte[] logo, Instant updated) {
        VaultItem item = new VaultItem(ItemType.NOTE);
        item.setTitle(title);
        item.setNotes(text);
        item.setLogo(logo);
        item.setCreatedAt(updated.minus(10, ChronoUnit.DAYS));
        item.setUpdatedAt(updated);
        return item;
    }

    private static byte[] logo(String letters, Color top, Color bottom) {
        try {
            int size = 256;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, top, size, size, bottom));
                g.fillRect(0, 0, size, size);
                g.setColor(new Color(255, 255, 255, 26));
                g.drawLine(0, 1, size, 1);
                g.setFont(new Font("Segoe UI", Font.BOLD, letters.length() > 1 ? 88 : 118));
                FontMetrics fm = g.getFontMetrics();
                int x = (size - fm.stringWidth(letters)) / 2;
                int y = (size - fm.getHeight()) / 2 + fm.getAscent();
                g.setColor(new Color(0xE0E0E0));
                g.drawString(letters, x, y);
            } finally {
                g.dispose();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
