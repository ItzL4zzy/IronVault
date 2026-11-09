import com.ironvault.model.ItemType;
import com.ironvault.model.VaultItem;
import com.ironvault.ui.DetailDrawer;
import com.ironvault.ui.Theme;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;

public class InspectDetail {
    static void dump(Component c, int depth) {
        if (depth > 8) return;
        String pad = "  ".repeat(depth);
        System.out.println(pad + c.getClass().getSimpleName() + " x=" + c.getX() + " y=" + c.getY()
                + " w=" + c.getWidth() + " h=" + c.getHeight()
                + " pw=" + c.getPreferredSize().width + " ax=" + c.getAlignmentX());
        if (c instanceof Container ct) for (Component child : ct.getComponents()) dump(child, depth + 1);
    }

    public static void main(String[] args) throws Exception {
        Theme.install();
        SwingUtilities.invokeAndWait(() -> {
            VaultItem item = new VaultItem(ItemType.PASSWORD);
            item.setTitle("Google");
            item.setUsername("alice@gmail.com");
            item.setPassword("Aurora!Sable-42");
            item.setWebsite("https://google.com");
            item.setNotes("Основная почта. Двухфакторная аутентификация включена.");
            DetailDrawer drawer = new DetailDrawer(() -> {
            }, i -> {
            }, i -> {
            }, s -> {
            });
            drawer.showItem(item);
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setSize(452, 788);
            frame.setContentPane(drawer);
            frame.setVisible(true);
            frame.validate();
            dump(drawer, 0);
            frame.dispose();
        });
        System.exit(0);
    }
}
