package org.tarlaboratories.tartech.computer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Utils {
    public static void log(Object o) {
        System.out.println(o);
    }

    public static Scanner getScanner(String path) throws FileNotFoundException {
        File file = new File(path);
        return new Scanner(file);
    }
}
