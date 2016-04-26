package org.whileloop.filewatcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class FileWatcher {
	
	public interface CreatedHandler {
		public void onCreated(String s);
	}
	
	public interface ModifiedHandler {
		public void onModified(String s);
	}
	
	public interface DeletedHandler {
		public void onDeleted(String s);
	}
	
	private CreatedHandler createdHandler;
	private DeletedHandler deletedHandler;
	private ModifiedHandler modifiedHandler;
	
	private WatchService watcher;
	private Path monitoredPath;
	
	public FileWatcher(String s) throws IOException {
		this(Paths.get(s));
	}
	
	public FileWatcher(Path p) throws IOException {
		this.monitoredPath = p;
		watcher = FileSystems.getDefault().newWatchService();
		monitoredPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		
		new Thread() {
			public void run() {
				try {
					monitor();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void setCreatedHandler(CreatedHandler h) {
		this.createdHandler = h;
	}
	
	public void setModifiedHandler(ModifiedHandler h) {
		this.modifiedHandler = h;
	}
	
	public void setDeletedHandler(DeletedHandler h) {
		this.deletedHandler = h;
	}
	
	private void fireDeleteEvent(String s) {
		if (deletedHandler != null)
			deletedHandler.onDeleted(s);
	}
	
	private void fireCreatedEvent(String s) {
		if (createdHandler != null)
			createdHandler.onCreated(s);
	}
	
	private void fireModifiedEvent(String s) {
		if (modifiedHandler != null)
			modifiedHandler.onModified(s);
	}
	
	private void monitor() {
		for (;;) {
		    // wait for key to be signaled
		    WatchKey key;
		    try {
		        key = watcher.take();
		    } catch (InterruptedException x) {
		        return;
		    }

		    for (WatchEvent<?> event: key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();

		        // This key is registered only
		        // for ENTRY_CREATE events,
		        // but an OVERFLOW event can
		        // occur regardless if events
		        // are lost or discarded.
		        if (kind == OVERFLOW) {
		            continue;
		        }

		        // The filename is the context of the event.
		        @SuppressWarnings("unchecked")
            WatchEvent<Path> ev = (WatchEvent<Path>)event;
		        Path filename = ev.context();

	            // Resolve the filename against the directory.
	            // If the filename is "test" and the directory is "foo",
	            // the resolved name is "test/foo".
	            Path child = monitoredPath.resolve(filename);

	            if (kind == ENTRY_CREATE) {
	            	fireCreatedEvent(child.toString());
	            } else if (kind == ENTRY_DELETE) {
	            	fireDeleteEvent(child.toString());
	            } else if (kind == ENTRY_MODIFY) {
	            	fireModifiedEvent(child.toString());
	            }
		    }

		    // Reset the key -- this step is critical if you want to
		    // receive further watch events.  If the key is no longer valid,
		    // the directory is inaccessible so exit the loop.
		    boolean valid = key.reset();
		    if (!valid) {
		        break;
		    }
		}
	}
}