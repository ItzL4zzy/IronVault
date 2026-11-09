package com.ironvault;

import com.ironvault.ui.AppWindow;
import com.ironvault.ui.Theme;
import com.ironvault.util.AppPaths;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class IronVaultApp {
    private IronVaultApp() {
    }

    public static void main(String[] args) {
        Path vault = AppPaths.vaultPath();
        for (String arg : args) {
            if (arg.startsWith("--vault=")) {
                vault = Paths.get(arg.substring("--vault=".length())).toAbsolutePath();
            }
        }
        System.setProperty("ironvault.vault", vault.toString());
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        Theme.install();
        final Path vaultPath = vault;
        SwingUtilities.invokeLater(() -> {
            AppWindow window = new AppWindow(vaultPath);
            window.setVisible(true);
        });
    }
}
