import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class MD5File implements Runnable {
	private ConcurrentMap<String, String> map = null;
	private String filename = null;

	public MD5File(String file, ConcurrentMap<String, String> map) {
		this.filename = file;
		this.map = map;
	}

	@Override
	public void run() {
		File file = new File(filename);

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println("MessageDigest.getInstance");
			e.printStackTrace();
		}
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("new FileInputStream");
			e.printStackTrace();
		}
		BufferedInputStream bs = new BufferedInputStream(fs);
		byte[] buffer = new byte[1024];
		int bytesRead;

		try {
			while ((bytesRead = bs.read(buffer, 0, buffer.length)) != -1) {
				md.update(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("bs.read");
			e.printStackTrace();
		}
		byte[] digest = md.digest();

		StringBuilder sb = new StringBuilder();
		for (byte bite : digest) {
			sb.append(String.format("%02x", bite & 0xff));
		}
		map.put(filename, sb.toString());
	}

}

public class ConcurrentDirMD5 {
	private static final ConcurrentMap<String, String> map = new ConcurrentHashMap<String, String>();

	public static ArrayList<String> fileArr = new ArrayList<String>();

	public static void scanDir(String path) throws Exception {
		File currentDir = new File(path);
		System.out.println(currentDir.getAbsolutePath());

		if (currentDir.isFile() && currentDir.canRead() && currentDir.length() <= 1024 * 1024 * 10) {

			fileArr.add(currentDir.getAbsolutePath());
			// System.out.println(currentDir.getAbsolutePath());
		} else {
			// System.out.println(currentDir.getAbsolutePath());
		}

		if (currentDir.isDirectory() && currentDir.canRead()) {
			String[] items = currentDir.list();
			if (items != null) {
				for (String name : items) {
					scanDir(path + "/" + name);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		long starttime = System.currentTimeMillis();
		scanDir("c:\\windows\\appcompat\\Programs\\");
		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (String line : fileArr) {

			service.execute(new MD5File(line, map));
		}

		service.shutdown();
		while (!service.isTerminated()) {
			Thread.sleep(1);
		}

		long stoptime = System.currentTimeMillis();
		System.out.println(stoptime - starttime);

		List<String> values = new ArrayList<>(map.keySet());
		for (String val : values) {
			System.out.println(val + "," + map.get(val));
		}
	}
}
