package ncai.funcs;

/**
 * @author Petr Ivanov
 *
 */
public class ReadMemory04 {
    public static native void TestFunction();
    static boolean NoLibrary = false;
    static {
        try{
            System.loadLibrary("ReadMemory04");
        }
        catch(Throwable e){
            NoLibrary = true;
        }
    }

    static public void main(String args[]) {
        if(NoLibrary) return;
        TestFunction();
        special_method();
        return;
    }

    static public void special_method() {
        /*
         * Transfer control to native part.
         */
        try {
            throw new InterruptedException();
        } catch (Throwable tex) { }
        return;
    }
}


