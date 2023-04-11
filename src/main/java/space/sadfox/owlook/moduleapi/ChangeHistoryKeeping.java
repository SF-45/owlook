package space.sadfox.owlook.moduleapi;

import java.util.List;

public interface ChangeHistoryKeeping {
	default List<Object> getProperties() {
		return null;
	}

}
