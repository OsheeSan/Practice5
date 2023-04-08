import java.io.*;
import java.util.*;

public class BSBIIndexer {

	public static TreeMap<String, ArrayList<Integer>> index = new TreeMap<>();
	public static ArrayList<Integer> tempID = new ArrayList<>();
	public static int countHash_doc = 0;
	public static HashMap<String, Integer> docID = new HashMap<>();
	public static int fileName = 0;

	public static void main(String[] args) throws IOException {
		// delete all previous files
		Arrays.stream(new File("//Applications/NaUKMA/Practice/Separate/").listFiles()).forEach(File::delete);
		long startTime = System.currentTimeMillis();
		long chunkSize = 20_000_000L; // read 20 MB at a time
		long usedSize = 0;
		String chunk = "";
		String folderPath = "//Applications/NaUKMA/Practice/texts";  //txt files to invert
		List<File> files = listTxtFiles(new File(folderPath));

		for (File file : files) {
			docID.put(file.getAbsolutePath(), countHash_doc++);

			if (usedSize + file.length() < chunkSize) {
				System.out.println(file.getAbsolutePath());
				chunk = readFromFile(file);
				System.out.println("Reading file");
				usedSize += file.length();
				System.out.println(usedSize + " bytes ");
				buildIndex(docID.get(file.getAbsolutePath()), chunk);
			} else {
				if (chunk != null) {
					writeChunkInFile();
					System.out.println("Document created!");
					index.clear();
					tempID.clear();
					usedSize = 0;
					// Зберегти проміжний файл
					chunk = readFromFile(file);
					usedSize += file.length();
					buildIndex(docID.get(file.getAbsolutePath()), chunk);
				}
			}
		}

		writeChunkInFile();
		System.out.println("Document created!");

		long endTime = System.currentTimeMillis();
		long totalTimeInSeconds = (endTime - startTime) / 1000;
		System.out.println("Total time taken by the code for PART_1: " + totalTimeInSeconds + " seconds");

		startTime = System.currentTimeMillis();
		mergeIndex();

		endTime = System.currentTimeMillis();
		totalTimeInSeconds = (endTime - startTime) / 1000;
		System.out.println("Total time taken by the code for PART_2: " + totalTimeInSeconds + " seconds");
		writeDocumentsIdInFile();
	}

	private static String readFromFile(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String tempString;
			while ((tempString = br.readLine()) != null) {
				sb.append(tempString);
			}
			try {
				br.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}

	private static void mergeIndex() throws IOException {
		index = new TreeMap<>();
		String folderPath = "//Applications/NaUKMA/Practice/Separate";
		List<File> files = listTxtFiles(new File(folderPath));

		for (File file : files) {
			BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line;
			while ((line = reader.readLine()) != null) {
				tempID = new ArrayList<>();
				String[] word_ids = line.split(":");
				String[] ids = word_ids[1].split(",");


				if (index.containsKey(word_ids[0])) {
					tempID = index.get(word_ids[0]);

					for (String id : ids) {
						tempID.add(Integer.parseInt(id));
					}

					index.put(word_ids[0], tempID);
					tempID = new ArrayList<>();
				} else {
					for (String id : ids) {
						tempID.add(Integer.parseInt(id));
					}

					index.put(word_ids[0], tempID);
					tempID = new ArrayList<>();
				}
			}
			reader.close();
		}

		File fileInp = new File("//Applications/NaUKMA/Practice/Separate/BigIndex.txt");
		FileWriter fileInpPostingList = new FileWriter(fileInp);
		for (String key : index.keySet()) {
			int count = 1;
			String tmp;
			List<Integer> delete = index.get(key);
			tmp = key + ":";
			for (Integer b : delete) {
				if (count++ == 1) {
					tmp += b;
					continue;
				}
				tmp += "," + b;
			}
			fileInpPostingList.write(tmp + "\n");
		}
		fileInpPostingList.close();

	}

	private static void writeChunkInFile() throws IOException {
		if (!index.isEmpty()) {
			File fileInp = new File("//Applications/NaUKMA/Practice/Separate/" + fileName++ + ".txt");
			FileWriter fileInpPostingList = new FileWriter(fileInp);

			for (String key : index.keySet()) {
				int count = 1;
				String tmp;
				List<Integer> delete = index.get(key);
				tmp = key + ":";
				for (Integer b : delete) {
					if (count++ == 1) {
						tmp += b;
						continue;
					}
					tmp += "," + b;
				}
				fileInpPostingList.write(tmp + "\n");
			}
			fileInpPostingList.close();
		}
	}

	private static void writeDocumentsIdInFile() throws IOException {
		File fileInp = new File("//Applications/NaUKMA/Practice/Separate/" + "DocumentsID.txt");
		FileWriter fileInpPostingList = new FileWriter(fileInp);

		for (String key : docID.keySet()) {
			int count = 1;
			String tmp;
			int delete = docID.get(key);
			tmp = key + " = > " + delete;

			fileInpPostingList.write(tmp + "\n");
		}
		fileInpPostingList.close();
	}

	private static void buildIndex(int idDoc, String chunk) {
		String[] tmp = chunk.split("[^a-zA-Z]+");
		System.out.println();

		for (String word : tmp) {
			word = word.toLowerCase();
			if (word.equals(""))
				continue;
			if (index.containsKey(word)) {
				tempID = index.get(word);
				if (!tempID.contains(idDoc)) {
					tempID.add(idDoc);
					index.put(word, tempID);
				} else {
					tempID = new ArrayList<>();
				}
			} else {
				tempID = new ArrayList<>();
				tempID.add(idDoc);
				index.put(word, tempID);
			}
		}
	}

	private static String readFile(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line;
		String res = "";
		while ((line = reader.readLine()) != null) {
			res += line;
		}

		reader.close();
		return res;
	}

	private static List<File> listTxtFiles(File folder) {
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				files.addAll(listTxtFiles(file));
			} else if (file.getName().endsWith(".txt")) {
				files.add(file);
			}
		}
		return files;
	}
}