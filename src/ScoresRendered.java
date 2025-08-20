import javax.swing.*;
import java.awt.*;

public class ScoresRendered extends JLabel implements ListCellRenderer {

    ScoresRendered(){
        setOpaque(true);
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        setText((String) value);
        setForeground(Color.WHITE);
        setBackground(Color.BLACK);
        return this;
    }
}
