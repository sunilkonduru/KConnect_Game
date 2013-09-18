
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Thomas Bennett
 */
public class Tester {

    public static void main(String[] args) {
        args= new String[]{"DummyAI.class", "c2.exe"};
        ArrayList<String> l = new ArrayList<String>();
        for (int i =0; i<3; i++){
            l.add("c2.exe");
            l.add("DummyAI.class");
        }
        args = l.toArray(new String[0]);
        Tournament.main(args);

    }
}
