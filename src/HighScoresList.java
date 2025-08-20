//import javax.swing.*;
//import java.awt.*;
//import java.io.*;
//import java.util.Vector;
//
//public class HighScoresList extends JFrame {
//
//    Okno parent;
//    static FileInputStream fis;
//    static ObjectInputStream ois;
//    HighScoresList(Okno okno) {
//        this.parent = okno;
//        Vector<String> score = new Vector<>();
//
//        String arrow = "--------------------------->";
//        String arrow2 = "---------->";
//        score.add("Name" + arrow + "Score");
//        try {
//            if(fis == null || ois == null) {
//                fis = new FileInputStream("High Scores");
//                ois = new ObjectInputStream(fis);
//            }
//            Score s;
//
//            while (true) {
//                s = (Score)ois.readObject();
//                score.add(s.name + arrow2 + s.score);
//            }
//        } catch (EOFException eof) {
//            close();
//            fis = null;
//            ois = null;
//        } catch (FileNotFoundException fE) {
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        JList<String>list = new JList<>(score);
//        JScrollPane jsp = new JScrollPane(list);
//        list.setBackground(Color.BLACK);
//        list.setCellRenderer(new ScoresRendered());
//
//        getContentPane().add(jsp);
//        setVisible(true);
//        setSize(200,400);
//        setLocationRelativeTo(null);
//        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//    }
//
//    @Override
//    public void dispose(){
//        parent.setVisible(true);
//        super.dispose();
//    }
//    public static void close(){
//        try {
//            ois.close();
//            fis.close();
//        }catch (Exception e){}
//    }
//}


import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class HighScoresList extends JFrame {

    private final Window parent;

    public HighScoresList(Window window) {
        this.parent = window;

        Vector<String> rows = new Vector<>();
        String arrow = "--------------------------->";
        String arrow2 = "---------->";

        rows.add("Name" + arrow + "Score");

        List<Score> scores = Score.readAll();
        for (Score s : scores) {
            rows.add(s.name + arrow2 + s.score);
        }

        JList<String> list = new JList<>(rows);
        JScrollPane jsp = new JScrollPane(list);

        list.setBackground(Color.BLACK);
        list.setForeground(Color.WHITE);
        list.setCellRenderer(new ScoresRendered());

        getContentPane().add(jsp);
        setSize(240, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void dispose() {
        if (parent != null) parent.setVisible(true);
        super.dispose();
    }
}

