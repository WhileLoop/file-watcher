package org.whileloop.filewatcher;

import org.whileloop.filewatcher.FileWatcher.CreatedHandler;
import org.whileloop.filewatcher.FileWatcher.DeletedHandler;
import org.whileloop.filewatcher.FileWatcher.ModifiedHandler;

public class UsageExample {

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("No arguments provided!");
			return;
		}
		// TODO: Validate path strings.
		String folderToWatch = args[0];
		FileWatcher f = new FileWatcher(folderToWatch);
		f.setCreatedHandler(new CreatedHandler() {
			
			@Override
			public void onCreated(String s) {
				System.out.println(s + " created!");
			}
		});
		
		f.setDeletedHandler(new DeletedHandler() {
			
			@Override
			public void onDeleted(String s) {
				System.out.println(s + " deleted!");
			}
		});
		
		f.setModifiedHandler(new ModifiedHandler() {
			
			@Override
			public void onModified(String s) {
				System.out.println(s + " modified!");
			}
		});
	}

}
