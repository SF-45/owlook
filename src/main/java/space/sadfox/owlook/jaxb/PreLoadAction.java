package space.sadfox.owlook.jaxb;

@FunctionalInterface
public interface PreLoadAction {
	void action(JAXBEntity entity);
}
