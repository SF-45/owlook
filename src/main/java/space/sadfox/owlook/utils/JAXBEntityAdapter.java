package space.sadfox.owlook.utils;

import java.io.FileNotFoundException;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.base.jaxb.JAXBEntity;
import space.sadfox.owlook.logger.LogLevel;

public abstract class JAXBEntityAdapter<T extends JAXBEntity> extends XmlAdapter<String, T> {

	@Override
	public final T unmarshal(String v) throws Exception {
		try {
			return EntityLoader.INSTANCE.loadEntity(v, getTarget());
		} catch (JAXBException e) {
			OwlLogger.registerException(1, e);
			LoggerMessage message = new LoggerMessage(LogLevel.WARNING);
			message.setName("Instance not loaded");
			message.setMessage("Instance [" + v + "] not loaded in " + getTarget().getPackageName());
			OwlLogger.registerMessage(message);
		} catch (FileNotFoundException e) {
			LoggerMessage message = new LoggerMessage(LogLevel.WARNING);
			message.setName("Instance Not Found");
			message.setMessage("Instance [" + v + "] not found in " + getTarget().getPackageName());
			OwlLogger.registerMessage(message);
		}
		return null;
	}

	@Override
	public final String marshal(T v) throws Exception {
		return v.getFileName();
	}

	protected abstract Class<T> getTarget();

}
