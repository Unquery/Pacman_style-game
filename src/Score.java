//import java.io.*;
//
//public class Score implements Serializable {
//    String name;
//    int score;
//
//    static FileOutputStream fos;
//    static ObjectOutputStream oos;
//    static File file = new File("High Scores");
//    Score(String name, int score) {
//
//        this.name = name;
//        this.score = score;
//        try{
//            if(!file.exists()){
//                file.createNewFile();
//            }
//            if(fos == null || oos == null) {
//                fos = new FileOutputStream("High Scores", true);
//                oos = new ObjectOutputStream(fos);
//            }
//            oos.writeObject(this);
//        }
//        catch (FileNotFoundException eF){
//            try {
//                fos = new FileOutputStream("High Scores",true);
//                oos = new ObjectOutputStream(fos);
//            }catch (Exception exc){
//                exc.printStackTrace();}
//        }
//        catch (Exception e){e.printStackTrace();}
//
//    }
//
//    static public void close(){
//        try {
//            fos.close();
//            oos.close();
//        }catch (Exception e){
//
//        }
//    }
//}
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Score implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String name;

    public final int score;

    private static FileOutputStream fos;
    private static ObjectOutputStream oos;

    public static final File FILE = new File("High Scores");

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }

    private static class AppendableObjectOutputStream extends ObjectOutputStream {
        AppendableObjectOutputStream(OutputStream out) throws IOException { super(out); }
        @Override protected void writeStreamHeader() throws IOException {
            reset();
        }
    }

    public static synchronized void openWriter() throws IOException {
        if (oos != null) return;

        File parent = FILE.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        boolean append = FILE.exists() && FILE.length() > 0;
        fos = new FileOutputStream(FILE, true);
        oos = append ? new AppendableObjectOutputStream(fos)
                : new ObjectOutputStream(fos);
    }

    public static synchronized void write(Score s) throws IOException {
        if (oos == null) {
            openWriter();
        }
        oos.writeObject(s);
        oos.flush();
    }

    public static synchronized void closeWriter() {
        try {
            if (oos != null) oos.close();
        } catch (IOException ignored) {}
        try {
            if (fos != null) fos.close();
        } catch (IOException ignored) {}
        oos = null;
        fos = null;
    }

    public static List<Score> readAll() {
        List<Score> scores = new ArrayList<>();
        if (!FILE.exists() || FILE.length() == 0) return scores;

        try (FileInputStream fis = new FileInputStream(FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            while (true) {
                try {
                    Object obj = ois.readObject();
                    if (obj instanceof Score s) {
                        scores.add(s);
                    }
                } catch (EOFException eof) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scores;
    }

    @Override
    public String toString() {
        return name + " - " + score;
    }
}
