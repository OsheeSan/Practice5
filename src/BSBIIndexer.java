import java.io.*;
import java.util.*;

public class BSBIIndexer {
	private static final int BLOCK_SIZE = 1000; // розмір блоку
	private static final String TEMP_DIR = "temp/"; // директорія для тимчасових файлів

	public void buildIndex(String documentsDir, String indexFile) throws IOException {
		File dir = new File(documentsDir);
		File[] files = dir.listFiles();
		Arrays.sort(files); // сортуємо документи за іменем
		int numBlocks = (int) Math.ceil((double) files.length / BLOCK_SIZE); // обчислюємо кількість блоків

		// для кожного блоку
		for (int i = 0; i < numBlocks; i++) {
			List<String> block = new ArrayList<>();
			int start = i * BLOCK_SIZE;
			int end = Math.min(start + BLOCK_SIZE, files.length);
			for (int j = start; j < end; j++) {
				String text = readFile(files[j]); // читаємо документ
				block.add(text);
			}
			Map<String, List<Integer>> invertedIndex = buildInvertedIndex(block); // побудова інвертованого індексу
			writeInvertedIndex(invertedIndex, TEMP_DIR + "block-" + i + ".txt"); // записуємо інвертований індекс у файл
		}

		// об'єднуємо інвертовані індекси
		List<BufferedReader> readers = new ArrayList<>();
		for (int i = 0; i < numBlocks; i++) {
			BufferedReader reader = new BufferedReader(new FileReader(TEMP_DIR + "block-" + i + ".txt"));
			readers.add(reader);
		}
		Map<String, List<Integer>> index = mergeInvertedIndexes(readers);
		writeIndex(index, indexFile);

		// видаляємо тимчасові файли
		for (int i = 0; i < numBlocks; i++) {
			File file = new File(TEMP_DIR + "block-" + i + ".txt");
			file.delete();
		}
	}

	private String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line).append("\n");
		}
		reader.close();
		return builder.toString();
	}

	private Map<String, List<Integer>> buildInvertedIndex(List<String> documents) {
		Map<String, List<Integer>> invertedIndex = new HashMap<>();
		int docId = 0;
		for (String document : documents) {
			docId++;
			String[] words = document.split("\\s+");
			for (String word : words) {
				List<Integer> docIds = invertedIndex.getOrDefault(word, new ArrayList<>());
				docIds.add(docId);
				invertedIndex.put(word, docIds);
			}
		}
		return invertedIndex;
	}

	private void writeInvertedIndex(Map<String, List<Integer>> invertedIndex, String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
			writer.write(entry.getKey() + ":" + entry.getValue().toString() + "\n");
		}
		writer.close();
	}

	private Map<String, List<Integer>> mergeInvertedIndexes(List<BufferedReader> readers) throws IOException {
		Map<String, List<Integer>> index = new HashMap<>();
		PriorityQueue<WordPosting> pq = new PriorityQueue<>();
		int docId = 0;
		for (BufferedReader reader : readers) {
			String line = reader.readLine();
			if (line != null) {
				String[] parts = line.split(":");
				String word = parts[0];
				List<Integer> docIds = parseDocIds(parts[1]);
				WordPosting wp = new WordPosting(word, docIds, reader);
				pq.offer(wp);
			}
		}
		while (!pq.isEmpty()) {
			WordPosting wp = pq.poll();
			List<Integer> docIds = wp.getDocIds();
			for (int i = 0; i < docIds.size(); i++) {
				int id = docIds.get(i);
				docId++;
				List<Integer> postings = index.getOrDefault(wp.getWord(), new ArrayList<>());
				postings.add(docId);
				index.put(wp.getWord(), postings);
			}
			BufferedReader reader = wp.getReader();
			String line = reader.readLine();
			if (line != null) {
				String[] parts = line.split(":");
				String word = parts[0];
				List<Integer> nextDocIds = parseDocIds(parts[1]);
				WordPosting nextWp = new WordPosting(word, nextDocIds, reader);
				pq.offer(nextWp);
			}
		}
		return index;
	}

	private List<Integer> parseDocIds(String s) {
		List<Integer> docIds = new ArrayList<>();
		s = s.replace("[", "").replace("]", "").replace(",", "");
		String[] parts = s.split("\\s+");
		for (String part : parts) {
			docIds.add(Integer.parseInt(part));
		}
		return docIds;
	}

	private void writeIndex(Map<String, List<Integer>> index, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(index.keySet());
		oos.close();
	}

	private class WordPosting implements Comparable<WordPosting> {
		private String word;
		private List<Integer> docIds;
		private BufferedReader reader;

		public WordPosting(String word, List<Integer> docIds, BufferedReader reader) {
			this.word = word;
			this.docIds = docIds;
			this.reader = reader;
		}

		public String getWord() {
			return word;
		}

		public List<Integer> getDocIds() {
			return docIds;
		}

		public BufferedReader getReader() {
			return reader;
		}

		@Override
		public int compareTo(WordPosting other) {
			return word.compareTo(other.getWord());
		}
	}
}