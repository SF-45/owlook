package space.sadfox.owlook.logger;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import space.sadfox.owlook.base.jaxb.JAXBEntity;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Logger extends JAXBEntity {

	private List<LoggerEntry> logs = new ArrayList<>();

	@XmlElement(name = "log")
	public List<LoggerEntry> getLogs() {
		return logs;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void validate() {
	}

	@Override
	public void syncWith(JAXBEntity entity) {
		if (!(entity instanceof Logger)) {
			return;
		}	
		Logger l = (Logger) entity;
		getLogs().addAll(l.getLogs());
	}
	
	

}
