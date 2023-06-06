package space.sadfox.owlook.jaxb;


@FunctionalInterface
public interface EntityChangeListener {
	public abstract static class Change {
		public abstract boolean wasModify();
		public abstract boolean wasRemoved();
	}

	void change(Change change);
}
