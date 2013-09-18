
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tournament {

    static final String JAVA_SUFFIX = ".class";

    /**
     * @param args A list of <? extends CKPlayer> classes
     */
    public static void main(String[] args) {
        if (args.length==0)
            System.out.println("You must supply the names of the AIs to compete against each other. Please see the included readme.");
        BoardModel model;
        List<Class<? extends CKPlayer>> players = new ArrayList<Class<? extends CKPlayer>>();
        HashMap <Integer, String> cppProcessFiles = new HashMap<Integer, String>();
        URLClassLoader classLoader;
        for (int i = 0; i < args.length; ++i) {
            try {
                if (args[i].endsWith(JAVA_SUFFIX)) {
                    File file = new File(args[i]);
                    if (file.getParent() == null)
                        classLoader = URLClassLoader.newInstance(new URL[]{new File(".").toURI().toURL()});
                    else
                        classLoader = URLClassLoader.newInstance(new URL[]{file.getParentFile().toURI().toURL()});
                    Class<?> cls = Class.forName(file.getName().split(".class")[0], true, classLoader);
                    if (CKPlayer.class.isAssignableFrom(cls)) {
                        model = BoardModel.newBoard(7, 6, 4, true);
                        Constructor<? extends CKPlayer> c = (Constructor<? extends CKPlayer>) cls.getConstructor(Byte.TYPE, BoardModel.class);
                        c.newInstance((byte) 1, model);
                        players.add((Class<? extends CKPlayer>) cls);
                    }
                } else { //we will assume a C++ executable.
                    cppProcessFiles.put(players.size(), args[i]);
                    players.add(ProcessPlayer.class); //this will be used as a sentinel value.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        byte[][] winners = new byte[players.size()][players.size()];
        for (int i = 0; i < winners.length; ++i) {
            for (int j = 0; j < winners[i].length; ++j) {
                if (i != j) { //do not play against self
                    Constructor<? extends CKPlayer> c;
                    Runtime.getRuntime().gc();
                    model = BoardModel.newBoard(7, 6, 4, true);
                    try {
                        CKPlayer player1, player2;
                        if (players.get(i).equals(ProcessPlayer.class)) {
                            player1 = new ProcessPlayer ((byte)1, (BoardModel)model.clone(), cppProcessFiles.get(i));
                        } else {
                            c = players.get(i).getConstructor(Byte.TYPE, BoardModel.class);
                            player1 = c.newInstance((byte) 1, model.clone());
                        }
                        if (players.get(j).equals(ProcessPlayer.class)) {
                            player2 = new ProcessPlayer((byte)2, (BoardModel)model.clone(), cppProcessFiles.get(j));
                        } else {
                            c = players.get(j).getConstructor(Byte.TYPE, BoardModel.class);
                            player2 = c.newInstance((byte) 2, model.clone());
                        }
                        System.out.println("Game " + i + ", " + j + ", player 1: " + player1
                                + ", player 2: " + player2);
                        ConnectKGUI view = new ConnectKGUI(model);
                        ConnectK game = new ConnectK(model, player1, player2, view);
                        winners[i][j] = game.play();
                        System.out.println("Player " + winners[i][j] + " wins game "
                                + i + ", " + j);
                        String scoresString = "";
                        for (int l = winners[0].length - 1; l >= 0; --l) {
                            for (int k = 0; k < winners.length; ++k) {
                                scoresString += winners[k][l];
                            }
                            scoresString += "\n";
                        }
                        System.out.println(scoresString);
                        Thread.sleep(1000);
                        view.dispose();
                        if (player1 instanceof ProcessPlayer){
                            ProcessPlayer pp = (ProcessPlayer) player1;
                            pp.cleanup();

                        }
                        if (player2 instanceof ProcessPlayer){
                            ProcessPlayer pp = (ProcessPlayer) player2;
                            pp.cleanup();

                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        for (int i = 0; i < winners.length; ++i) {
            int score = 0;
            for (int j = 0; j < winners[i].length; ++j) {
                if (i != j)
                    if (winners[i][j] == 1)
                        score += 2;
                    else if (winners[i][j] == 0)
                        score += 1;
            }
            for (int j = 0; j < winners[i].length; ++j) {
                if (i != j)
                    if (winners[j][i] == 2)
                        score += 2;
                    else if (winners[j][i] == 0)
                        score += 1;
            }
            model = BoardModel.newBoard(7, 6, 4, true);
            CKPlayer player1 = null;
            try {
                Constructor<? extends CKPlayer> c = players.get(i).getConstructor(Byte.TYPE, BoardModel.class);
                player1 = c.newInstance((byte) 1, model.clone());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String teamName = player1 != null ? player1.teamName : players.get(i).toString();
            System.out.println("Player " + i + " = " + teamName
                    + " = " + args[i]);
            System.out.println("Player " + i + " score: " + score / 2.0 + "\n");
        }
    }
}
