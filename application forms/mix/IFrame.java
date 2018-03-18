package mix;

import javax.swing.*;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sguldemond on 15/03/2018.
 */
public abstract class IFrame extends JFrame {

    public abstract void add(Serializable component, String corrId);
}
