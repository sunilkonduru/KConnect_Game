/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Thomas Bennett
 */
public class ProcessDestroyThread extends Thread {
    private final Process process;

    public ProcessDestroyThread (Process process){
        this.process = process;

    }

    @Override
    public void run() {
        try{
            process.destroy();
        }
        catch (Exception ex){
            //do nothing.
        }
    }




}
