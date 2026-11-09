import com.ironvault.security.VaultManager;
import com.ironvault.ui.Theme;
import com.ironvault.ui.VaultShell;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.nio.file.Path;

public class InspectMain {
    static void dump(Component c, int depth) {
        if (depth > 3) return;
        String pad = "  ".repeat(depth);
        System.out.println(pad + c.getClass().getSimpleName()
                + " x=" + c.getX() + " y=" + c.getY()
                + " w=" + c.getWidth() + " h=" + c.getHeight()
                + " pref=" + c.getPreferredSize().width + "x" + c.getPreferredSize().height + " max=" + c.getMaximumSize().width + "x" + c.getMaximumSize().height
                + " alignX=" + c.getAlignmentX());
        if (c instanceof Container ct) {
            for (Component child : ct.getComponents()) dump(child, depth + 1);
        }
    }

    public static void main(String[] args) throws Exception {
        Theme.install();
        VaultManager manager = VaultManager.open(Path.of("demo-vault.iron"), "IronVault!2026".toCharArray());
        SwingUtilities.invokeAndWait(() -> {
            VaultShell shell = new VaultShell(manager, new VaultShell.Host() {
                public void showToast(String message) {
                }

                public void lockVault() {
                }

                public Window window() {
                    return null;
                }
            });
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setSize(1240, 820);
            frame.setContentPane(shell);
            frame.setVisible(true);
            frame.validate();
            dump(shell, 0);
            frame.dispose();
        });
        System.exit(0);
    }
}
