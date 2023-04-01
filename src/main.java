import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;

public class main {

    public static TreeMap<String, ArrayList<Integer>> index = new TreeMap<>();
    public static ArrayList<Integer> tempID = new ArrayList<>();

    public static int countHash_doc = 0;
    public static HashMap<String, Integer> docID = new HashMap<>();

    public static int fileName = 0;

    public static void main(String[] args) throws IOException {
        long chunkSize = 3_000_000L; // read 8 GB at a time
        long usedSize = 0;
        String chunk = "";
        String folderPath = "temp";
        List<File> files = listTxtFiles(new File(folderPath));

        for (File file : files) {
            docID.put(file.getAbsolutePath(), countHash_doc++);

            if (usedSize + file.length() < chunkSize) {
                System.out.println(file.getAbsolutePath());


                chunk = readFile(file.getAbsolutePath(), chunkSize);
                usedSize += file.length();
                System.out.println((double) usedSize / 1_000_000);
                buildIndex(docID.get(file.getAbsolutePath()), chunk);
            } else {
                if (chunk != null) {
                    writeChunkInFile();
                    index = new TreeMap<>();
                    usedSize = 0;

                    // Зберегти проміжний файл
                    chunk = readFile(file.getAbsolutePath(), chunkSize);
                    usedSize += file.length();
                    buildIndex(docID.get(file.getAbsolutePath()), chunk);
                }
            }
        }
    }

    private static void writeChunkInFile() throws IOException {
        File fileInp = new File("D:\\gutenberg_txt\\Seperate\\" + fileName + ".txt");
        FileWriter fileInpPostingList = new FileWriter(fileInp);

        for (String key : index.keySet()) {
            int count = 1;
            String tmp = "";
            List<Integer> delete = index.get(key);
            tmp = key + "\t => {";
            for (Integer b : delete) {
                if (count++ == 1) {
                    tmp += b;
                    continue;
                }
                tmp += ", " + b;
            }
            tmp += "}";
            fileInpPostingList.write(tmp + "\n");
            System.out.println("СТРОКА СОЗДАНА!");
        }
        fileInpPostingList.close();
    }

    private static void buildIndex(int idDoc, String chunk) {
        String[] tmp = chunk.split("[^a-zA-Z]+");
        System.out.println();

        for(String word : tmp)
        {
            word = word.toLowerCase();

            if(index.containsKey(word))
            {
                tempID = index.get(word);
                tempID.add(idDoc);
                index.put(word, tempID);
            }
            else
            {
                tempID.add(idDoc);
                index.put(word, tempID);
            }
        }
    }

    private static String readFile(String path, long chunkSize) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        // Open the file for reading

        // Read each line of the file and print it to the console
        String line;
        String res = "";
        while ((line = reader.readLine()) != null) {
            res += line;
        }

        return res;
    }


    private static long getFileLength(String fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
            return file.length();
        }
    }

    private static byte[] toByteArray(List<Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
    public static List<File> listTxtFiles(File folder) throws IOException {
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