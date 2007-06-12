package javax.swing.plaf.multi;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TableHeaderUI;

/**
 * All the methods described in public api
 */
public class MultiTableHeaderUI extends TableHeaderUI {

	protected Vector uis = new Vector();

	/**
	 * Used in cycles. numberOfUIs = Correct number of UIs + 1, but the variable
	 * used in that sence
	 */
	private int numberOfUIs;

	public static ComponentUI createUI(JComponent a) {
		MultiTableHeaderUI mui = new MultiTableHeaderUI();
		ComponentUI result = MultiLookAndFeel.createUIs(mui, mui.uis, a);
		mui.numberOfUIs = mui.uis.size();
		return result;
	}

	@Override
	public boolean contains(JComponent a, int b, int c) {
		for (int i = 1; i < numberOfUIs; i++) {
			((ComponentUI) uis.get(i)).contains(a, b, c);
		}
		return ((ComponentUI) uis.firstElement()).contains(a, b, c);
	}

	@Override
	public Accessible getAccessibleChild(JComponent a, int b) {
		for (int i = 1; i < numberOfUIs; i++) {
			((ComponentUI) uis.get(i)).getAccessibleChild(a, b);
		}
		return ((ComponentUI) uis.firstElement()).getAccessibleChild(a, b);
	}

	@Override
	public int getAccessibleChildrenCount(JComponent a) {
		for (int i = 1; i < numberOfUIs; i++) {
			((ComponentUI) uis.get(i)).getAccessibleChildrenCount(a);
		}
		return ((ComponentUI) uis.firstElement()).getAccessibleChildrenCount(a);
	}

	@Override
	public Dimension getMaximumSize(JComponent a) {
		for (int i = 1; i < numberOfUIs; i++) {
			((ComponentUI) uis.get(i)).getMaximumSize(a);
		}
		return ((ComponentUI) uis.firstElement()).getMaximumSize(a);
	}

	@Override
	public Dimension getMinimumSize(JComponent a) {
		for (int i = 1; i < numberOfUIs; i++) {
			((ComponentUI) uis.get(i)).getMinimumSize(a);
		}
		return ((ComponentUI) uis.firstElement()).getMinimumSize(a);
	}

	@Override
	public Dimension getPreferredSize(JComponent a) {
		for (int i = 1; i < numberOfUIs; i++) {
			((ComponentUI) uis.get(i)).getPreferredSize(a);
		}
		return ((ComponentUI) uis.firstElement()).getPreferredSize(a);
	}

	public ComponentUI[] getUIs() {
		return MultiLookAndFeel.uisToArray(uis);
	}

	@Override
	public void installUI(JComponent a) {
		for (Object ui : uis) {
			((ComponentUI) ui).installUI(a);
		}
	}

	@Override
	public void paint(Graphics a, JComponent b) {
		for (Object ui : uis) {
			((ComponentUI) ui).paint(a, b);
		}
	}

	@Override
	public void uninstallUI(JComponent a) {
		for (Object ui : uis) {
			((ComponentUI) ui).uninstallUI(a);
		}
	}

	@Override
	public void update(Graphics a, JComponent b) {
		for (Object ui : uis) {
			((ComponentUI) ui).update(a, b);
		}
	}
}
