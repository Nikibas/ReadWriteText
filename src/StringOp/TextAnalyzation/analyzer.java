package StringOp.TextAnalyzation;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// Регулярные выражения "[ \t\n\r\f'\u000B'\u001C\u001D\u001E\u001F]"; 253
// Задание на 5+
// Сохранить лог на ЖД
// https://metanit.com/java/tutorial/6.9.php
// Все файловые диалоги JAVA с мопеда
// Добавить метод исправления(замена) символа с записью в файл
public class analyzer{
    private final String pattern = "[" +
            (char)12 + (char)13 + (char)14 + (char)32 + (char)160 +
            "\t\n\r\f'\u000B'\u001C\u001D\u001E\u001F]";

    private File inFile, logger;
    private boolean singleton;

    public analyzer(String path, String logger) throws FileNotFoundException {
        inFile = new File(path);
        if (logger!=null) this.logger = new File(logger);
        if(!inFile.exists())
            throw new FileNotFoundException();
        allCharsCounter = inFile.length();
        singleton = false;
    }
    public analyzer(String path) throws FileNotFoundException {
        new analyzer(path, null);
    }

    private long allCharsCounter;
    private long charsCounter;
    public long getCharsCounter()
    {
        read();
        return charsCounter;
    }

    private long wordsCounter;
    public long getWordsCounter(){
        read();
        return wordsCounter;
    }

    private long linesCounter;
    public long getLinesCounter(){
        read();
        return linesCounter;
    }

    Map<Character, Long> dictionary;

    public String logger(boolean small, boolean clear){
        if(clear) singleton = true;
        read();

        String result = "В файле: " + inFile.getName() + "\n";

        if(allCharsCounter<1){
            return result + "\tне найденно ни одного символа, слова или строки";
        }

        if(linesCounter>0) {
            result += "\tнайденно строк: " + linesCounter + "\n";
        }

        if(wordsCounter < 1 && charsCounter < 1){
            return result + "\tне найденно ни одного слова или видимого символа";
        }

        if(wordsCounter > 0){
            result += "\tнайденно слов: " + wordsCounter + "\n";
        }

        result += "\tнайденно символов: " + allCharsCounter + "\n";
        if(charsCounter>0) {
            result += "\tнайденно видиммых символов: " + charsCounter + "\n";
            result += "\tнайденно уникальных видиммых символов: " + dictionary.size() + "\n";
        }
        else {
            small = true;
        }

        if(small) return result + "--------------------------------------";

        result += "\nСимвол            | Частота вхождения\n";
        result += "------------------|-------------------\n";

        TreeMap<Character, Long> sorted = new TreeMap<>(dictionary);

        for (Map.Entry<Character, Long> entry : sorted.entrySet()) {
            var ch = entry.getKey();
            String tempStr = ch + "                 ";
            if (ch == '\u0301') tempStr += " ";
            result += tempStr + "|";

            double d = (double) entry.getValue() / charsCounter;
            BigDecimal bd = new BigDecimal(d).setScale(15, RoundingMode.DOWN);
            tempStr = " " + fmt(bd.doubleValue());
            result += tempStr + "\n";
        }
        result += "--------------------------------------";
        writelog(result);
        return result;
    }

    private void writelog(String result) {
        if(logger!=null){
            try(FileOutputStream fos=new FileOutputStream(logger, true))
            {
                if(logger.exists() && logger.length()>0)
                    result = "\n\n" + result;
                byte[] buffer = result.getBytes();
                // считываем буфер
                fos.write(buffer, 0, buffer.length);
            }
            catch(IOException ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    public String logger(){
        return logger(false, false);
    }
    public String logger(boolean small){
        return logger(small, false);
    }

    static String fmt(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%1.15f",d);
    }

    private void read()
    {
        if(singleton) return;

        try(BufferedReader br = new BufferedReader(new FileReader(inFile)))
        {
            linesCounter = 0;
            charsCounter = 0;
            wordsCounter = 0;
            dictionary = new HashMap<>();

            //чтение построчно
            String s;
            while((s=br.readLine())!=null){
                linesCounter++;

                for(String word : s.split(pattern)) {
                    if (word.length() > 0 && Character.isLetter(word.charAt(0))) {
                        wordsCounter++;
                    }
                }

                for(Character ch : s.toCharArray()){
                    if(ch.toString().length() < 1
                            || Character.isWhitespace(ch)
                            || ch == (char)160
                    ) continue;

                    charsCounter++;
                    dictionary.put(ch,
                            dictionary.containsKey(ch) ? dictionary.get(ch) + 1 : 1);
                }
                // System.out.println(s);
            }
            singleton = false;
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}
