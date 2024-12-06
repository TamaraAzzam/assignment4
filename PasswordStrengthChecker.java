import java.io.*;
import java.util.*;

public class PasswordStrengthChecker {

    static final int M_CHAINING = 1000;
    static final int M_PROBING = 20000;
    static class Node {
        String key;
        int value;
        Node next;

        Node(String key, int value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
    static class HashTableChaining {
        Node[] table;
        int comparisons;

        HashTableChaining(int size) {
            table = new Node[size];
            comparisons = 0;
        }

        int hash(String key, boolean useJavaHashCode) {
            int hash = 0;
            if (useJavaHashCode) {
                for (int i = 0; i < key.length(); i++)
                    hash = (hash * 31) + key.charAt(i);
            } else {
                int skip = Math.max(1, key.length() / 8);
                for (int i = 0; i < key.length(); i += skip)
                    hash = (hash * 37) + key.charAt(i);
            }
            return Math.abs(hash) % table.length;
        }
        void insert(String key, int value, boolean useJavaHashCode) {
            int index = hash(key, useJavaHashCode);
            Node newNode = new Node(key, value);
            if (table[index] == null) {
                table[index] = newNode;
            } else {
                Node current = table[index];
                while (current.next != null) {
                    current = current.next;
                }
                current.next = newNode;
            }
        }
        boolean search(String key, boolean useJavaHashCode) {
            comparisons = 0;
            int index = hash(key, useJavaHashCode);
            Node current = table[index];
            while (current != null) {
                comparisons++;
                if (current.key.equals(key))
                    return true;
                current = current.next;
            }
            return false;
        }
    }
    static class HashTableProbing {
        String[] table;
        int[] values;
        int comparisons;

        HashTableProbing(int size) {
            table = new String[size];
            values = new int[size];
            comparisons = 0;
        }

        int hash(String key, boolean useJavaHashCode) {
            int hash = 0;
            if (useJavaHashCode) {
                for (int i = 0; i < key.length(); i++)
                    hash = (hash * 31) + key.charAt(i);
            } else {
                int skip = Math.max(1, key.length() / 8);
                for (int i = 0; i < key.length(); i += skip)
                    hash = (hash * 37) + key.charAt(i);
            }
            return Math.abs(hash) % table.length;
        }

        void insert(String key, int value, boolean useJavaHashCode) {
            int index = hash(key, useJavaHashCode);
            while (table[index] != null) {
                index = (index + 1) % table.length;
            }
            table[index] = key;
            values[index] = value;
        }

        boolean search(String key, boolean useJavaHashCode) {
            comparisons = 0;
            int index = hash(key, useJavaHashCode);
            while (table[index] != null) {
                comparisons++;
                if (table[index].equals(key))
                    return true;
                index = (index + 1) % table.length;
            }
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("wordlist.10000"));
        List<String> words = new ArrayList<>();
        String line;
        int lineNum = 1;
        while ((line = br.readLine()) != null) {
            words.add(line.trim());
            lineNum++;
        }
        br.close();

        HashTableChaining chaining1 = new HashTableChaining(M_CHAINING);
        HashTableChaining chaining2 = new HashTableChaining(M_CHAINING);
        HashTableProbing probing1 = new HashTableProbing(M_PROBING);
        HashTableProbing probing2 = new HashTableProbing(M_PROBING);

        int lineNumber = 1;
        for (String word : words) {
            chaining1.insert(word, lineNumber, false);
            chaining2.insert(word, lineNumber, true);
            probing1.insert(word, lineNumber, false);
            probing2.insert(word, lineNumber, true);
            lineNumber++;
        }

        // Test passwords
        String[] passwords = {
                "account8",
                "accountability",
                "9a$D#qW7!uX&Lv3zT",
                "B@k45*W!c$Y7#zR9P",
                "X$8vQ!mW#3Dz&Yr4K5"
        };
        for (String password : passwords) {
            System.out.println("Testing password: " + password);

            boolean strongPwd = password.length() >= 8;
            boolean foundInChaining1 = chaining1.search(password, false);
            boolean foundInChaining2 = chaining2.search(password, true);
            boolean foundInProbing1 = probing1.search(password, false);
            boolean foundInProbing2 = probing2.search(password, true);

            for (int i = 0; i <= 9; i++) {
                String modified = password + i;
                foundInChaining1 |= chaining1.search(modified, false);
                foundInChaining2 |= chaining2.search(modified, true);
                foundInProbing1 |= probing1.search(modified, false);
                foundInProbing2 |= probing2.search(modified, true);
            }
            strongPwd &= !foundInChaining1 && !foundInProbing1;
            System.out.println("Strong: " + strongPwd);
            System.out.println("Comparisons (Chaining, Old): " + chaining1.comparisons);
            System.out.println("Comparisons (Chaining, New): " + chaining2.comparisons);
            System.out.println("Comparisons (Probing, Old): " + probing1.comparisons);
            System.out.println("Comparisons (Probing, New): " + probing2.comparisons);
            System.out.println();
        }
    }
}
