package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.obj.ExtensibleImpl;

/**
 * Check if the specified OModel Object is newest. Used as a visitor of 
 * {@link ObjectTraverser}.
 * 
 * @see ObjectTraverser
 */
public class UpgradeChecker extends AbstractObjectVisitor{
    private static final Logger __log = LoggerFactory.getLogger(UpgradeChecker.class);

    private boolean newest = true;
	@Override
	public Object visitPojo(Object obj) {
		if (! (obj instanceof ExtensibleImpl)){
			return null;
		}
		visitExtensible(obj);
		return null;
	}

	private void visitExtensible(Object obj) {
		ExtensibleImpl eobj = (ExtensibleImpl)obj;
		int currentVersion = 1;
		try {
			currentVersion = (Integer) eobj.getClass().getField("CURRENT_CLASS_VERSION").get(obj);
		} catch (Exception e) {
			// should never get here
			e.printStackTrace();
		}
		if (eobj.getClassVersion() != currentVersion){
			newest = false;
			__log.debug(obj.getClass() + "hasn't upgraded to newest version. current: " 
					+ eobj.getClassVersion() + ", newest: " + currentVersion);
		}
		List<Field> fields = MigUtils.getAllFields(obj.getClass());
		for (Field f : fields){
			f.setAccessible(true);
			try {
				Object value = f.get(obj);
				if (value != null){
					traverse.traverseObject(value);
				}
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isNewest() {
		return newest;
	}
}
