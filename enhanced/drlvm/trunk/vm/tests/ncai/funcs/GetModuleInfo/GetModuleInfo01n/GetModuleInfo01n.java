package ncai.funcs;

/**
 * @author Petr Ivanov
 *
 */
public class GetModuleInfo01n {

    static public void main(String args[]) {
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


