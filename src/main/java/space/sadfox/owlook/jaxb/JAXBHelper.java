package space.sadfox.owlook.jaxb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import space.sadfox.owlook.logger.LogLevel;
import space.sadfox.owlook.utils.LoggerMessage;
import space.sadfox.owlook.utils.OwlLogger;

public class JAXBHelper<T extends JAXBEntity> {

	private class SaveDuration extends Thread {
		private int timeNeed = 1;
		private int time = 0;

		public SaveDuration() {
			this.start();
		}

		@Override
		public void run() {
			while (time <= timeNeed) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					OwlLogger.registerException(1, e);
				}
				time++;
			}
			System.out.println( instance.getPath() + " save: " + new Date(System.currentTimeMillis()));
			marshalInstance();

		}

		public void refresh() {
			time = 0;
		}

		public void stopAndSave() {
			time = 5;
		}
	}

	private T instance;

	private Path path;

	private JAXBContext context;

	private SaveDuration saveDuration;

	@SuppressWarnings("unchecked")
	public JAXBHelper(Path path, Class<T> target) throws JAXBException, FileNotFoundException {
		this.path = path;
		context = JAXBContext.newInstance(target);

		if (Files.exists(path)) {
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (T) unmarshaller.unmarshal(new FileInputStream(path.toFile()));
		} else {
			try {
				instance = target.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				OwlLogger.registerException(1, e);
			}
		}
		instance.setJaxbHelper(this);
		instance.getChangeHistory();
		instance.validate();
		instance.initialize();
		// AutoSave here
		instance.addEntityChangeListener(change -> {
			if (change.wasModify()) {
				instance.save();
			}
		});

	}

	public T getInstance() {
		return instance;
	}

	public void save() {
		if (saveDuration == null || !saveDuration.isAlive())
			saveDuration = new SaveDuration();
		else
			saveDuration.refresh();
	}

	public void saveImmediately() {
		if (saveDuration == null || !saveDuration.isAlive())
			marshalInstance();
		else
			saveDuration.stopAndSave();
	}

	private void marshalInstance() {
		try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			if (!Files.exists(path.getParent()))
				Files.createDirectory(path.getParent());

			try (FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
				marshaller.marshal(instance, outputStream);
			}
		} catch (JAXBException | IOException e) {
			OwlLogger.registerException(1, e);
		}
	}
	
	void validateAndfixID() {
		try {
			getInstance().getId();
		} catch (IllegalArgumentException e) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e1) {}
			String oldName = path.getFileName().toString();
			path = path.getParent().resolve(EntityLoader.INSTANCE.generateFileName());
			saveImmediately();
			
			LoggerMessage massage = new LoggerMessage(LogLevel.WARNING);
			massage.setName("Bad ID instance [" + oldName + "]");
			massage.setMessage("Instance [" + oldName+ "] is bad ID. New ID=[" + instance.getId() + "]");
			OwlLogger.registerMessage(massage);
		}
	}

	public Path getPath() {
		return path;
	}
}
