package space.sadfox.owlook.owlery;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.logger.LogLevel;
import space.sadfox.owlook.utils.LoggerMessage;
import space.sadfox.owlook.utils.Logger;

public abstract class OwlAdapter<T extends OwlEntity> extends XmlAdapter<String, Owl<T>> {

	@Override
	public Owl<T> unmarshal(String v) throws Exception {
		try {
			return OwlLoader.INSTANCE.loadOwl(v, getTarget());
		}  catch (OwlNotFoundException e) {
			Logger.registerException(1, e);
			LoggerMessage message = new LoggerMessage(LogLevel.WARNING);
			message.setName("Owl Not Found [" + v + "]");
			message.setMessage(e.getMessage());
			message.setNotification(true);
			Logger.registerMessage(message);
		} catch (Exception e) {
			Logger.registerException(1, e);
			LoggerMessage message = new LoggerMessage(LogLevel.WARNING);
			message.setName("Owl Not loaded [" + v + "]");
			message.setMessage(e.getMessage());
			message.setNotification(true);
			Logger.registerMessage(message);
		} 
		return null;
	}

	@Override
	public String marshal(Owl<T> v) throws Exception {
		return v.fileName();
	}
	
	protected abstract Class<T> getTarget();

}
