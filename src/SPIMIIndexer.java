import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPIMIIndexer {
	private int blockSize; // розмір блоку даних
	private int dictionarySize; // максимальний розмір словника
	private String indexPath; // шлях для збереження індексу

	public SPIMIIndexer(int blockSize, int dictionarySize, String indexPath) {
		this.blockSize = blockSize;
		this.dictionarySize = dictionarySize;
		this.indexPath = indexPath;
	}
	public void buildIndex() throws IOException {
		// Створити словник для зберігання списків з індексами документів
		Map<String, List<Integer>> dictionary = new HashMap<>();

		// Обробити кожен документ у колекції
		for (int docId = 1; docId <= numDocs; docId++) {
			// Отримати текст документу
			String docText = getDocumentText(docId);

			// Розбити текст на слова
			List<String> terms = tokenize(docText);

			// Обчислити частоту кожного слова в документі
			Map<String, Integer> termFreqs = computeTermFrequencies(terms);

			// Додати інформацію про документ до словника з індексами документів
			for (String term : termFreqs.keySet()) {
				if (!dictionary.containsKey(term)) {
					dictionary.put(term, new ArrayList<Integer>());
				}
				dictionary.get(term).add(docId);
				dictionary.get(term).add(termFreqs.get(term));
			}

			// Якщо словник перевищив максимальний розмір, записати його до тимчасового файлу
			if (dictionary.size() > MAX_MEMORY_SIZE) {
				writeIndexToDisk(dictionary);
				dictionary.clear();
			}
		}

		// Записати залишки словника до тимчасового файлу
		if (!dictionary.isEmpty()) {
			writeIndexToDisk(dictionary);
			dictionary.clear();
		}

		// Об'єднати тимчасові файли індексу у великий файл з індексом
		mergeIndexes();
	}

}