package space.sadfox.owlook.jaxb.adapters;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.components.logger.LogLevel;
import space.sadfox.owlook.jaxb.EntityLoader;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.utils.ErrorLogger;
import space.sadfox.owlook.utils.LoggerMessage;

public abstract class JAXBEntityAdapter<T extends JAXBEntity> extends XmlAdapter<String, T> {

	@Override
	public T unmarshal(String v) throws Exception {
		try {
			return EntityLoader.INSTANCE.loadEntity(v, getTarget());
		} catch (JAXBException e) {
			ErrorLogger.registerException(e);

			LoggerMessage message = new LoggerMessage(LogLevel.WARNING);
			message.setName("Instance not loaded");
			message.setMessage("Instance [" + v + "] not loaded in " + getTarget().getPackageName());

			//throw e;
		}
		return null;
	}

	@Override
	public String marshal(T v) throws Exception {
		return v.getId().toString();
	}

	protected abstract Class<T> getTarget();

}
