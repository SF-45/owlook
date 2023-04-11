package space.sadfox.owlook.components.logger;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.scene.Node;
import space.sadfox.owlook.jaxb.JAXBEntity;
import space.sadfox.owlook.jaxb.PreLoadAction;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Logger extends JAXBEntity {

	private List<LoggerEntry> logs = new ArrayList<>();

	@XmlElement(name = "log")
	public List<LoggerEntry> getLogs() {
		return logs;
	}

	@Override
	public String getExtension() {
		return ".log";
	}

	@Override
	public Node getSimpleConfigNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreLoadAction getPreLoadAction() {
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
