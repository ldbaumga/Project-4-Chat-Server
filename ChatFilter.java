import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ChatFilter {
    private ArrayList<String> badWords = new ArrayList<String>();

    public ChatFilter(String badWordsFileName) {
        File f;
        FileReader fr;
        BufferedReader bf;

        try {
            f = new File(badWordsFileName);
            fr = new FileReader(f);
            bf = new BufferedReader(fr);

            String line;
            while ((line = bf.readLine()) != null) {
                badWords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String filter(String msg) {
        String rep = "";
        for (int x = 0; x < badWords.size(); x++) {
            rep = "";
            for (int v = 0; v < badWords.get(x).length(); v++) {
                rep += "*";
            }
            msg = msg.replaceAll(badWords.get(x), rep);
            msg = msg.replaceAll(badWords.get(x).toLowerCase(), rep);
            msg = msg.replaceAll(badWords.get(x).toUpperCase(), rep);
        }
        return msg;
    }
}
