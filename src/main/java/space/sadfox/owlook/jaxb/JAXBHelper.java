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
import space.sadfox.owlook.utils.ErrorLogger;

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
					ErrorLogger.registerException(e);
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
				ErrorLogger.registerException(e);
			}
		}
		instance.setJaxbHelper(this);
		instance.setPath(path);
		instance.getChangeHistory().register(instance);
		// AutoSave here
		instance.getChangeHistory().addChangeListener(() -> instance.save());

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
			ErrorLogger.registerException(e);
		}
	}
}
