import com.ironvault.ui.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;

public class InspectLayout {
    static void dump(Component c, int d) {
        String pad = "  ".repeat(d);
        System.out.println(pad + c.getClass().getSimpleName() + " x=" + c.getX() + " w=" + c.getWidth()
                + " pw=" + c.getPreferredSize().width + " ax=" + c.getAlignmentX());
        if (c instanceof Container ct) for (Component ch : ct.getComponents()) dump(ch, d + 1);
    }

    public static void main(String[] a) throws Exception {
        Theme.install();
        SwingUtilities.invokeAndWait(() -> {
            LockScreen lock = new LockScreen(Paths.get("nonexistent-vault.iron"), m -> {
            });
            JFrame f = new JFrame();
            f.setUndecorated(true);
            f.setSize(1240, 820);
            f.setContentPane(lock);
            f.setVisible(true);
            f.validate();
            dump(lock, 0);
            f.dispose();
        });
        System.exit(0);
    }
}
